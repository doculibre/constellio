package com.constellio.app.modules.restapi.ace.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.UnresolvableOptimisticLockException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.AceListDto;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;

public class AceDao extends BaseDao {

	private AuthorizationsServices authorizationsServices;

	@PostConstruct
	protected void init() {
		super.init();
		authorizationsServices = ConstellioFactories.getInstance().getModelLayerFactory().newAuthorizationsServices();
	}

	public AceListDto getAces(Record record) {
		List<AceDto> directAces = Lists.newArrayList();
		List<AceDto> inheritedAces = Lists.newArrayList();

		List<Authorization> authorizations = authorizationsServices.getRecordAuthorizations(record);
		for (Authorization authorization : authorizations) {
			boolean direct = authorization.getTarget().equals(record.getId());

			AceDto ace = AceDto.builder()
					.authorizationId(authorization.getId())
					.principals(getPrincipals(authorization.getPrincipals()))
					.permissions(Sets.newLinkedHashSet(authorization.getRoles()))
					.startDate(authorization.getStartDate() != null ?
							   DateUtils.format(authorization.getStartDate(), getDateFormat()) : null)
					.endDate(authorization.getEndDate() != null ?
							 DateUtils.format(authorization.getEndDate(), getDateFormat()) : null)
					.build();
			if (direct) {
				directAces.add(ace);
			} else {
				inheritedAces.add(ace);
			}

		}
		return AceListDto.builder().directAces(directAces).inheritedAces(inheritedAces).build();
	}

	public void addAces(User user, Record record, List<AceDto> aces) {
		for (AceDto ace : aces) {
			AuthorizationAddRequest request = authorizationInCollection(record.getCollection())
					.forPrincipalsIds(getPrincipalIds(ace.getPrincipals(), record.getCollection()))
					.startingOn(ace.getStartDate() != null ? DateUtils.parseLocalDate(ace.getStartDate(), getDateFormat()) : null)
					.endingOn(ace.getEndDate() != null ? DateUtils.parseLocalDate(ace.getEndDate(), getDateFormat()) : null)
					.on(record)
					.giving(Lists.newArrayList(ace.getPermissions()));
			try {
				authorizationsServices.add(request, user);
			} catch (AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException e) {
				if (e.getCause() instanceof RecordServicesException.UnresolvableOptimisticLockingConflict) {
					throw new UnresolvableOptimisticLockException(record.getId());
				}
				throw e;
			}
		}
	}

	public void updateAces(User user, Record record, List<AceDto> aces) {
		for (AceDto ace : aces) {
			Authorization authorization = authorizationsServices.getAuthorization(record.getCollection(), ace.getAuthorizationId());

			AuthorizationModificationRequest request = AuthorizationModificationRequest.modifyAuthorization(authorization)
					.withNewPrincipalIds(getPrincipalIds(ace.getPrincipals(), record.getCollection()))
					.withNewAccessAndRoles(Lists.newArrayList(ace.getPermissions()))
					.withNewStartDate(ace.getStartDate() != null ? DateUtils.parseLocalDate(ace.getStartDate(), getDateFormat()) : null)
					.withNewEndDate(ace.getEndDate() != null ? DateUtils.parseLocalDate(ace.getEndDate(), getDateFormat()) : null)
					.setExecutedBy(user);
			try {
				authorizationsServices.execute(request);
			} catch (AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException e) {
				if (e.getCause() instanceof RecordServicesException.UnresolvableOptimisticLockingConflict) {
					throw new UnresolvableOptimisticLockException(record.getId());
				}
				throw e;
			}
		}
	}

	public void removeAces(User user, Record record, List<AceDto> aces) {
		for (AceDto ace : aces) {
			AuthorizationDeleteRequest request = authorizationDeleteRequest(ace.getAuthorizationId(), record.getCollection());
			try {
				authorizationsServices.execute(request.setExecutedBy(user));
			} catch (AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException e) {
				if (e.getCause() instanceof RecordServicesException.UnresolvableOptimisticLockingConflict) {
					throw new UnresolvableOptimisticLockException(record.getId());
				}
				throw e;
			}
		}
	}

	private List<String> getPrincipalIds(Set<String> principals, String collection) {
		List<String> principalIds = new ArrayList<>(principals.size());

		for (String principal : principals) {
			Record record = getUserByUsername(principal, collection);
			if (record == null) {
				record = getGroupByCode(principal, collection);
			}
			principalIds.add(record.getId());
		}
		return principalIds;
	}

	private Set<String> getPrincipals(List<String> principalIds) {
		Set<String> principals = new HashSet<>();

		for (String principalId : principalIds) {
			Record record = getRecordById(principalId);
			if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
				principals.add(this.<String>getMetadataValue(record, User.USERNAME));
			} else {
				principals.add(this.<String>getMetadataValue(record, Group.CODE));
			}
		}
		return principals;
	}

}
