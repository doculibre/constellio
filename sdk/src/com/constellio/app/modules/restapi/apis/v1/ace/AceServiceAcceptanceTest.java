package com.constellio.app.modules.restapi.apis.v1.ace;

import com.constellio.app.modules.restapi.apis.v1.resource.dto.AceDto;
import com.constellio.app.modules.restapi.apis.v1.resource.dto.AceListDto;
import com.constellio.app.modules.restapi.core.config.RestApiResourceConfigV1;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ApplicationHandler;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.restapi.core.util.Permissions.DELETE;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.core.util.Permissions.WRITE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.sdk.tests.TestUtils.comparingListAnyOrder;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class AceServiceAcceptanceTest extends ConstellioTest {

	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private UserServices userServices;
	private AuthorizationsServices authorizationsServices;

	@Inject
	private AceService aceService;

	private String dateFormat;
	private Document document;
	private AuthorizationAddRequest authorization1, authorization2;
	private User admin;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		new ApplicationHandler(new RestApiResourceConfigV1()).getInjectionManager().getInstance(ServiceLocator.class)
				.inject(this);

		dateFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);

		document = rm.newDocument().setFolder(records.folder_A01).setTitle("Title");
		recordServices.add(document);

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorization1 = authorizationInCollection(zeCollection)
				.forUsers(users.bobIn(zeCollection))
				.on(document).givingReadWriteDeleteAccess();
		authorizationsServices.add(authorization1, users.adminIn(zeCollection));
		authorization2 = authorizationInCollection(zeCollection)
				.forUsers(users.aliceIn(zeCollection))
				.on(document).givingReadWriteAccess();
		authorizationsServices.add(authorization2, users.adminIn(zeCollection));

		admin = users.adminIn(zeCollection);
	}

	@Test
	public void testGetAuthorizations() {
		AceListDto aces = aceService.getAces(document.getWrappedRecord());

		assertThat(aces).isNotNull();
		assertThat(aces.getDirectAces()).contains(
				AceDto.builder()
						.principals(toPrincipals(authorization1.getPrincipals()))
						.permissions(Sets.newLinkedHashSet(authorization1.getRoles()))
						.startDate(authorization1.getStart() != null ? DateUtils.format(authorization1.getStart(), dateFormat) : null)
						.endDate(authorization1.getEnd() != null ? DateUtils.format(authorization1.getEnd(), dateFormat) : null)
						.build(),
				AceDto.builder()
						.principals(toPrincipals(authorization2.getPrincipals()))
						.permissions(Sets.newLinkedHashSet(authorization2.getRoles()))
						.startDate(authorization2.getStart() != null ? DateUtils.format(authorization2.getStart(), dateFormat) : null)
						.endDate(authorization2.getEnd() != null ? DateUtils.format(authorization2.getEnd(), dateFormat) : null)
						.build());
	}

	@Test
	public void testAddAces() {
		List<AceDto> aces = asList(
				AceDto.builder().principals(singleton(alice)).permissions(newHashSet(READ, WRITE))
						.startDate(DateUtils.format(new LocalDate(), dateFormat))
						.endDate(DateUtils.format(new LocalDate().plusDays(365), dateFormat)).build(),
				AceDto.builder().principals(singleton(chuck)).permissions(singleton(READ)).build());
		aceService.addAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).extracting("principals").contains(
				toPrincipalIds(aces.get(0).getPrincipals()), toPrincipalIds(aces.get(1).getPrincipals()));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").contains(
				tuple(Lists.newArrayList(aces.get(0).getPermissions()), toLocalDate(aces.get(0).getStartDate()), toLocalDate(aces.get(0).getEndDate())),
				tuple(Lists.newArrayList(aces.get(1).getPermissions()), toLocalDate(aces.get(1).getStartDate()), toLocalDate(aces.get(1).getEndDate())));
	}

	@Test
	public void testUpdateAces() {
		List<AceDto> aces = asList(
				AceDto.builder().principals(Sets.newHashSet(alice, bob)).permissions(singleton(READ)).build(),
				AceDto.builder().principals(singleton(chuck)).permissions(singleton(READ)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(1);
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder)
				.containsOnly(toPrincipalIds(asList(chuck, alice, bob)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate")
				.containsOnly(tuple(singletonList(READ), null, null));
	}

	@Test
	public void testUpdateAcesWithSameAmountAsExisting() {
		List<AceDto> aces = asList(
				AceDto.builder().principals(singleton(alice)).permissions(newHashSet(READ, WRITE))
						.startDate(DateUtils.format(new LocalDate(), dateFormat))
						.endDate(DateUtils.format(new LocalDate().plusDays(365), dateFormat)).build(),
				AceDto.builder().principals(singleton(bob)).permissions(singleton(READ)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(2);
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder)
				.containsOnly(toPrincipalIds(aces.get(0).getPrincipals()), toPrincipalIds(aces.get(1).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder)
				.containsOnly(Lists.newArrayList(aces.get(0).getPermissions()), Lists.newArrayList(aces.get(1).getPermissions()));
		assertThat(authorizations).extracting("startDate", "endDate")
				.containsOnly(tuple(toLocalDate(aces.get(0).getStartDate()), toLocalDate(aces.get(0).getEndDate())),
						tuple(toLocalDate(aces.get(1).getStartDate()), toLocalDate(aces.get(1).getEndDate())));
	}

	@Test
	public void testUpdateAcesWithLowerAmountThanExisting() {
		List<AceDto> aces = singletonList(AceDto.builder().principals(Sets.newHashSet(alice, chuck, bob))
				.permissions(Sets.newHashSet(READ, WRITE, DELETE)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(1);
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder)
				.containsOnly(toPrincipalIds(asList(chuck, alice, bob)));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder)
				.containsOnly(asList(READ, WRITE, DELETE));
		assertThat(authorizations).extracting("startDate", "endDate")
				.containsOnly(tuple(null, null));
	}

	@Test
	public void testUpdateAcesWithGreaterAmountThanExisting() {
		List<AceDto> aces = asList(
				AceDto.builder().principals(singleton(alice)).permissions(singleton(READ)).build(),
				AceDto.builder().principals(singleton(bob)).permissions(Sets.newHashSet(READ, WRITE, DELETE)).build(),
				AceDto.builder().principals(singleton(chuck)).permissions(Sets.newHashSet(READ, WRITE))
						.startDate(DateUtils.format(new LocalDate(), dateFormat))
						.endDate(DateUtils.format(new LocalDate(), dateFormat)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(3);
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder).containsOnly(
				toPrincipalIds(aces.get(0).getPrincipals()),
				toPrincipalIds(aces.get(1).getPrincipals()),
				toPrincipalIds(aces.get(2).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(aces.get(0).getPermissions()),
				Lists.newArrayList(aces.get(1).getPermissions()),
				Lists.newArrayList(aces.get(2).getPermissions()));
		assertThat(authorizations).extracting("startDate", "endDate").containsOnly(
				tuple(toLocalDate(aces.get(0).getStartDate()), toLocalDate(aces.get(0).getEndDate())),
				tuple(toLocalDate(aces.get(1).getStartDate()), toLocalDate(aces.get(1).getEndDate())),
				tuple(toLocalDate(aces.get(2).getStartDate()), toLocalDate(aces.get(2).getEndDate())));
	}

	@Test
	public void testUpdateAcesWithAtLeastOneUnchangedAce() {
		List<AceDto> aces = asList(
				AceDto.builder().principals(toPrincipals(authorization2.getPrincipals()))
						.permissions(Sets.newLinkedHashSet(authorization2.getRoles()))
						.startDate(authorization2.getStart() != null ? DateUtils.format(authorization2.getStart(), dateFormat) : null)
						.endDate(authorization2.getEnd() != null ? DateUtils.format(authorization2.getEnd(), dateFormat) : null).build(),
				AceDto.builder().principals(singleton(bob)).permissions(singleton(READ)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(2);
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder)
				.containsOnly(toPrincipalIds(aces.get(0).getPrincipals()), toPrincipalIds(aces.get(1).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder)
				.containsOnly(Lists.newArrayList(aces.get(0).getPermissions()), Lists.newArrayList(aces.get(1).getPermissions()));
		assertThat(authorizations).extracting("startDate", "endDate")
				.containsOnly(tuple(toLocalDate(aces.get(0).getStartDate()), toLocalDate(aces.get(0).getEndDate())),
						tuple(toLocalDate(aces.get(1).getStartDate()), toLocalDate(aces.get(1).getEndDate())));
	}

	@Test
	public void testUpdateAcesWithDeletePermissionOnly() {
		List<AceDto> aces = singletonList(AceDto.builder().principals(singleton(chuck)).permissions(singleton(DELETE)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(1);
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder)
				.containsOnly(asList(READ, DELETE));
	}

	@Test
	public void testUpdateAcesWithWritePermissionOnly() {
		List<AceDto> aces = singletonList(AceDto.builder().principals(singleton(chuck)).permissions(singleton(WRITE)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(1);
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder)
				.containsOnly(asList(READ, WRITE));
	}

	@Test
	public void testUpdateAcesWithWriteDeletePermissions() {
		List<AceDto> aces = singletonList(AceDto.builder().principals(singleton(chuck)).permissions(Sets.newHashSet(WRITE, DELETE)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(1);
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder)
				.containsOnly(asList(READ, WRITE, DELETE));
	}

	@Test
	public void testUpdateAcesWithGroupingOfSimilarPermissions() {
		List<AceDto> aces = asList(
				AceDto.builder().principals(Sets.newHashSet(alice, bob)).permissions(singleton(WRITE)).build(),
				AceDto.builder().principals(singleton(chuck)).permissions(Sets.newHashSet(READ, WRITE)).build());
		aceService.updateAces(admin, document.getWrappedRecord(), aces);

		List<Authorization> authorizations = filterInherited(authorizationsServices.getRecordAuthorizations(document.getWrappedRecord()));
		assertThat(authorizations).hasSize(1);
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder)
				.containsOnly(toPrincipalIds(asList(chuck, alice, bob)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate")
				.containsOnly(tuple(asList(READ, WRITE), null, null));
	}

	private List<Authorization> filterInherited(List<Authorization> authorizations) {
		List<Authorization> filteredAuthorizations = Lists.newArrayList();
		for (Authorization authorization : authorizations) {
			if (authorization.getTarget().equals(document.getId())) {
				filteredAuthorizations.add(authorization);
			}
		}
		return filteredAuthorizations;
	}

	private LocalDate toLocalDate(String date) {
		return date != null ? DateUtils.parseLocalDate(date, dateFormat) : null;
	}

	private Set<String> toPrincipals(Collection<String> ids) {
		Set<String> principals = new HashSet<>();
		for (String id : ids) {
			Record record = recordServices.getDocumentById(id);
			if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
				principals.add(record.<String>get(rm.user.username()));
			} else {
				principals.add(record.<String>get(rm.group.code()));
			}
		}
		return principals;
	}

	private List<String> toPrincipalIds(Collection<String> principals) {
		List<String> principalIds = new ArrayList<>(principals.size());
		for (String principal : principals) {
			Record record = recordServices.getRecordByMetadata(rm.user.username(), principal);
			if (record == null) {
				record = recordServices.getRecordByMetadata(rm.group.code(), principal);
			}
			principalIds.add(record.getId());
		}
		return principalIds;
	}

}
