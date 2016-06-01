package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.setups.Users;

@SlowTest
public class RMMigrationTo5_0_7_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemWithVariousTypeOfUnitAndFilingSpacesWhenUpdatingFrom5_0_6ThenRemoveFilingSpaces()
			throws OptimisticLockingConfiguration {

		givenSystemAtVersion5_0_6WithVariousTypeOfFilingSpaceAndUnitRelations();

		AdministrativeUnit unit10 = getExistingUnitWithCode("10");
		AdministrativeUnit unit10_10 = getExistingUnitWithCode("10-10");
		AdministrativeUnit unit20 = getExistingUnitWithCode("20");
		AdministrativeUnit unit20_A = getExistingUnitWithCode("20-A");
		AdministrativeUnit unit20_Ze = getExistingUnitWithCode("20-Ze");
		AdministrativeUnit unitO = getExistingUnitWithCode("O");
		AdministrativeUnit unit30 = getExistingUnitWithCode("30");
		AdministrativeUnit unitZe = getExistingUnitWithCode("Ze");

		assertThat(unit10.getParent()).isNull();
		assertThat(unit10_10.getParent()).isEqualTo(unit10.getId());
		assertThat(unit20.getParent()).isNull();
		assertThat(unit20_A.getParent()).isEqualTo(unit20.getId());
		assertThat(unit20_Ze.getParent()).isEqualTo(unit20.getId());
		assertThat(unitO.getParent()).isEqualTo(unit20.getId());
		assertThat(unit30.getParent()).isNull();
		assertThat(unitZe.getParent()).isNull();

	}

	@Test
	public void givenSystemWithLDAPConfigurationWhenUpdatingFrom5_0_6ThenConfigurationReadOK()
			throws OptimisticLockingConfiguration {

		givenSystemAtVersion5_0_6WithManualLDAPConfiguration();

		LDAPTestConfig expectedConfig = new LDAPTestConfig();
		LDAPServerConfiguration serverConfiguration = getModelLayerFactory().getLdapConfigurationManager()
				.getLDAPServerConfiguration();
		assertThat(serverConfiguration.getLdapAuthenticationActive()).isEqualTo(true);
		assertThat(serverConfiguration.getDirectoryType()).isEqualTo(LDAPTestConfig.getDirectoryType());
		assertThat(serverConfiguration.getDomains()).containsOnly("test.doculibre.ca");
		assertThat(serverConfiguration.getUrls()).containsOnly("ldap://sp2010.constellio.com:389");
		assertThat(serverConfiguration.getFollowReferences()).isEqualTo(false);

		LDAPUserSyncConfiguration usersSynchConfiguration = getModelLayerFactory().getLdapConfigurationManager()
				.getLDAPUserSyncConfiguration(true);
		assertThat(usersSynchConfiguration.getPassword()).isEqualTo(usersSynchConfiguration.getPassword());
		assertThat(usersSynchConfiguration.getUser()).isEqualTo(usersSynchConfiguration.getUser());
		assertThat(usersSynchConfiguration.getGroupBaseContextList())
				.containsExactlyElementsOf(usersSynchConfiguration.getGroupBaseContextList());
		assertThat(usersSynchConfiguration.getGroupsFilterAcceptanceRegex())
				.isEqualTo(usersSynchConfiguration.getGroupsFilterAcceptanceRegex());
		assertThat(usersSynchConfiguration.getGroupsFilterRejectionRegex())
				.isEqualTo(usersSynchConfiguration.getGroupsFilterRejectionRegex());
		assertThat(usersSynchConfiguration.getUsersWithoutGroupsBaseContextList())
				.containsExactlyElementsOf(usersSynchConfiguration.getUsersWithoutGroupsBaseContextList());
		assertThat(usersSynchConfiguration.getUsersFilterAcceptanceRegex())
				.isEqualTo(usersSynchConfiguration.getUsersFilterAcceptanceRegex());
		assertThat(usersSynchConfiguration.getUsersFilterRejectionRegex())
				.isEqualTo(usersSynchConfiguration.getUsersFilterRejectionRegex());
		assertThat(usersSynchConfiguration.getDurationBetweenExecution().getStandardDays()).isEqualTo(1);
		assertThat(usersSynchConfiguration.getSelectedCollectionsCodes()).isEmpty();
	}

	@Test
	public void givenSystemWithLogicallyDeletedFilingSpacesWhenUpdatingFrom5_0_6ThenCreateLogicallyAdministrativeUnits() {
		givenSystemAtVersion5_0_6WithDisabledFilingSpaces();

		AdministrativeUnit unit10a = getExistingUnitWithCode("A");
		assertThat((Boolean) unit10a.getWrappedRecord().get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
	}

	@Test
	public void whenUpdatingFrom5_0_6ThenRemoveFilingSpaces()
			throws OptimisticLockingConfiguration {

		givenSystemAtVersion5_0_6();
		Users users = new Users().setUp(getModelLayerFactory().newUserServices());
		User admin = users.adminIn(zeCollection);
		User alice = users.aliceIn(zeCollection);
		User bob = users.bobIn(zeCollection);
		User charles = users.charlesIn(zeCollection);
		User dakota = users.dakotaIn(zeCollection);
		User edouard = users.edouardIn(zeCollection);
		User gandalf = users.gandalfIn(zeCollection);

		AdministrativeUnit unit10 = getExistingUnitWithCode("10");
		AdministrativeUnit unit10a = getExistingUnitWithCode("A");
		AdministrativeUnit unit11 = getExistingUnitWithCode("11");
		AdministrativeUnit unit11b = getExistingUnitWithCode("11-B");
		AdministrativeUnit unit12 = getExistingUnitWithCode("12");
		AdministrativeUnit unit12b = getExistingUnitWithCode("12-B");
		AdministrativeUnit unit12c = getExistingUnitWithCode("12-C");
		AdministrativeUnit unit20 = getExistingUnitWithCode("20");
		AdministrativeUnit unit20d = getExistingUnitWithCode("D");
		AdministrativeUnit unit20e = getExistingUnitWithCode("E");
		AdministrativeUnit unit30 = getExistingUnitWithCode("30");
		AdministrativeUnit unit30c = getExistingUnitWithCode("30-C");

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<AdministrativeUnit> unit10Children = rm.wrapAdministrativeUnits(searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.administrativeUnit.schemaType()).where(rm.administrativeUnit.parent()).isEqualTo(unit10))));

		assertThat(unit10Children).extracting("code").containsOnly("11", "12", "A");

		assertThatAuthorizationsOf(unit10).isEmpty();
		assertThatAuthorizationsOf(unit10a).containsOnly(
				authorizationFor(bob, charles, admin).on(unit10a).givingReadWriteAccess(),
				authorizationFor(dakota, gandalf).on(unit10a).givingReadWriteDeleteAccess()
		);

		assertThatAuthorizationsOf(unit11).isEmpty();
		assertThatAuthorizationsOf(unit11b).containsOnly(
				authorizationFor(dakota, admin).on(unit11b).givingReadWriteAccess(),
				authorizationFor(edouard, gandalf).on(unit11b).givingReadWriteDeleteAccess(),
				authorizationFor(edouard).on(unit11b).giving(RMRoles.MANAGER)
		);

		assertThatAuthorizationsOf(unit12).isEmpty();
		assertThatAuthorizationsOf(unit12b).containsOnly(
				authorizationFor(dakota, admin).on(unit12b).givingReadWriteAccess(),
				authorizationFor(edouard, gandalf).on(unit12b).givingReadWriteDeleteAccess(),
				authorizationFor(edouard).on(unit12b).giving(RMRoles.MANAGER)
		);
		assertThatAuthorizationsOf(unit12c).containsOnly(
				authorizationFor(edouard, bob, admin).on(unit12c).givingReadWriteAccess(),
				authorizationFor(gandalf).on(unit12c).givingReadWriteDeleteAccess()
		);

		assertThat(authorizationsServices.getRecordAuthorizations(unit20)).isEmpty();
		assertThat(authorizationsServices.getRecordAuthorizations(unit20d)).isEmpty();
		assertThat(authorizationsServices.getRecordAuthorizations(unit20e)).isEmpty();
		assertThat(authorizationsServices.getRecordAuthorizations(unit30)).isEmpty();
		assertThatAuthorizationsOf(unit30c).containsOnly(
				authorizationFor(edouard, bob, admin).on(unit30c).givingReadWriteAccess(),
				authorizationFor(gandalf).on(unit30c).givingReadWriteDeleteAccess()
		);

		assertThat(alice.getUserRoles()).containsOnly(RMRoles.USER);
		assertThat(gandalf.getUserRoles()).containsOnly(RMRoles.MANAGER);

		assertThat(searchServices.getResultsCount(
				fromAllSchemasIn(zeCollection).where(SCHEMA).isStartingWithText("filingSpace"))).isZero();

		Metadata folderFilingSpace = rm.defaultFolderSchema().getMetadata(Folder.FILING_SPACE);
		Metadata containerFilingSpace = rm.defaultContainerRecordSchema().getMetadata(ContainerRecord.FILING_SPACE);
		Metadata decomListFilingSpace = rm.decommissioningList.schema().getMetadata(DecommissioningList.FILING_SPACE);

		assertThatRecord(searchServices.searchSingleResult(from(rm.folderSchemaType()).where(TITLE).is("Banane")))
				.hasMetadataValue(rm.folderAdministrativeUnit(), unit12b.getId())
				.hasNoMetadataValue(folderFilingSpace);

		assertThatRecord(searchServices.searchSingleResult(from(rm.folderSchemaType()).where(TITLE).is("Perroquet")))
				.hasMetadataValue(rm.folderAdministrativeUnit(), unit10a.getId())
				.hasNoMetadataValue(folderFilingSpace);

		assertThatRecord(searchServices.searchSingleResult(from(rm.containerRecordSchemaType()).where(TITLE).is("10_A_12")))
				.hasMetadataValue(rm.folderAdministrativeUnit(), unit10a.getId())
				.hasNoMetadataValue(containerFilingSpace);

		assertThatRecord(searchServices.searchSingleResult(
				from(rm.decommissioningList.schema()).where(IDENTIFIER).isEqualTo("list10")))
				.hasMetadataValue(rm.folderAdministrativeUnit(), unit10a.getId())
				.hasNoMetadataValue(decomListFilingSpace);

		assertThatRecord(searchServices.searchSingleResult(
				from(rm.decommissioningList.schema()).where(IDENTIFIER).isEqualTo("list08")))
				.hasMetadataValue(rm.folderAdministrativeUnit(), unit20.getId())
				.hasNoMetadataValue(decomListFilingSpace);
	}

	@Test
	public void whenUpdatingFrom5_0_6ThenMigrateDecomListFolderDetail()
			throws OptimisticLockingConfiguration {
		givenSystemAtVersion5_0_6();
		getAppLayerFactory().newMigrationServices().migrate(zeCollection);

		RMTestRecords rmTestRecords = new RMTestRecords(zeCollection).alreadySettedUp(getAppLayerFactory());
		List<DecomListFolderDetail> decomListFolderDetailList = rmTestRecords.getList01().getFolderDetails();

		assertThat(decomListFolderDetailList).isNotEmpty();
		DecomListFolderDetail decomListFolderDetail = decomListFolderDetailList.get(0);
		assertThat(decomListFolderDetail.isFolderIncluded()).isTrue();
	}

	private org.assertj.core.api.ListAssert<Authorization> assertThatAuthorizationsOf(RecordWrapper recordWrapper) {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		return assertThat(authorizationsServices.getRecordAuthorizations(recordWrapper))
				.usingElementComparator(new Comparator<Authorization>() {
					@Override
					public int compare(Authorization o1, Authorization o2) {
						Set<String> o1Principals = new HashSet<String>(o1.getGrantedToPrincipals());
						Set<String> o2Principals = new HashSet<String>(o2.getGrantedToPrincipals());
						Set<String> o1Targets = new HashSet<String>(o1.getGrantedOnRecords());
						Set<String> o2Targets = new HashSet<String>(o2.getGrantedOnRecords());
						Set<String> o1Roles = new HashSet<String>(o1.getDetail().getRoles());
						Set<String> o2Roles = new HashSet<String>(o2.getDetail().getRoles());

						boolean samePrincipals = o1Principals.equals(o2Principals);
						boolean sameTargets = o1Targets.equals(o2Targets);
						boolean sameRoles = o1Roles.equals(o2Roles);

						return samePrincipals && sameTargets && sameRoles ? 0 : 1;
					}
				});
	}

	private AuthorizationBuilder authorizationFor(User... users) {
		List<String> principals = new RecordUtils().toWrappedRecordIdsList(asList(users));
		return new AuthorizationBuilder(zeCollection).forPrincipalsIds(principals);
	}

	private AdministrativeUnit getExistingUnitWithCode(String code) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		AdministrativeUnit unit = rm.getAdministrativeUnitWithCode(code);
		assertThat(unit).describedAs("Unit with code '" + code + "'").isNotNull();
		return unit;
	}

	private void givenSystemAtVersion5_0_6() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.0.6_with_rm_module__with_test_records.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_0_6WithVariousTypeOfFilingSpaceAndUnitRelations() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.0.6_with_rm_module__with_some_administrativeUnits.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_0_6WithManualLDAPConfiguration() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.0.6_with_rm_module__with_manual_LDAPConfiguration.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_0_6WithDisabledFilingSpaces() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.0.6_with_rm_module__with_disabled_filingSpaces.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
