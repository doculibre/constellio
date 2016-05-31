package com.constellio.app.modules.rm.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class RMMigrationTo5_0_6_AcceptanceTest extends ConstellioTest {

	@Test
	public void whenUpdatingFrom5_0_5ThenMigrateCopyRetentionRules()
			throws OptimisticLockingConfiguration {

		givenDisabledAfterTestValidations();
		givenSystemAtVersion5_0_5();
		getAppLayerFactory().newMigrationServices().migrate(zeCollection, false);

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RMTestRecords rmTestRecords = new RMTestRecords(zeCollection).alreadySettedUp(getAppLayerFactory());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		VariableRetentionPeriod period888 = rm.PERIOD_888();
		VariableRetentionPeriod period999 = rm.PERIOD_999();
		assertThat(period888).isNotNull();
		assertThat(period999).isNotNull();
		assertThat(period888.getTitle()).isEqualTo("Ouvert");
		assertThat(period999.getTitle()).isEqualTo("Jusqu'à remplacement");

		//principal888_5_C, secondary888_0_D
		RetentionRule retentionRule1 = rmTestRecords.getRule1();
		CopyRetentionRule rule1PrincpalCopy = retentionRule1.getPrincipalCopies().get(0);
		assertThat(rule1PrincpalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule1PrincpalCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(5));
		assertThat(rule1PrincpalCopy.getInactiveDisposalType()).isEqualTo(DisposalType.DEPOSIT);

		CopyRetentionRule rule1SecondaryCopy = retentionRule1.getSecondaryCopy();
		assertThat(rule1SecondaryCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule1SecondaryCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(0));
		assertThat(rule1SecondaryCopy.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);

		//principal999_4_T, secondary1_0_D
		RetentionRule retentionRule3 = rmTestRecords.getRule3();
		CopyRetentionRule rule3PrincpalCopy = retentionRule3.getPrincipalCopies().get(0);
		assertThat(rule3PrincpalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		assertThat(rule3PrincpalCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(4));
		assertThat(rule3PrincpalCopy.getInactiveDisposalType()).isEqualTo(DisposalType.SORT);

		CopyRetentionRule rule3SecondaryCopy = retentionRule3.getSecondaryCopy();
		assertThat(rule3SecondaryCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(1));
		assertThat(rule3SecondaryCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(0));
		assertThat(rule3SecondaryCopy.getInactiveDisposalType()).isEqualTo(DisposalType.DESTRUCTION);

		//principal_PA_3_888_D, principal_MD_3_888_C, secondary888_0_D
		RetentionRule retentionRule4 = rmTestRecords.getRule4();
		CopyRetentionRule rule4FirstPrincipalCopy = retentionRule4.getPrincipalCopies().get(0);
		assertThat(rule4FirstPrincipalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(3));
		assertThat(rule4FirstPrincipalCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);

		CopyRetentionRule rule4SecondPrincipalCopy = retentionRule4.getPrincipalCopies().get(1);
		assertThat(rule4SecondPrincipalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(3));
		assertThat(rule4SecondPrincipalCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);

		CopyRetentionRule rule4SecondaryCopy = retentionRule4.getSecondaryCopy();
		assertThat(rule4SecondaryCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_888);
		assertThat(rule4SecondaryCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(0));
	}

	@Test
	public void whenUpdatingFrom5_0_5ThenChangeDefaultValueOfTreeConfig()
			throws Exception {

		givenDisabledAfterTestValidations();
		givenSystemAtVersion5_0_5();
		getAppLayerFactory().newMigrationServices().migrate(zeCollection, false);

		RMTestRecords rmTestRecords = new RMTestRecords(zeCollection).alreadySettedUp(getAppLayerFactory());

		assertThat(rmTestRecords.getCategory_X().isLinkable()).isTrue();
		assertThat(rmTestRecords.getCategory_Z().isLinkable()).isFalse();
	}

	@Test
	public void whenUpdatingFrom5_0_5AndRootLinkableThenStillLinkable()
			throws Exception {

		givenDisabledAfterTestValidations();
		givenSystemAtVersion5_0_5_withRootLinkable();
		getAppLayerFactory().newMigrationServices().migrate(zeCollection, false);

		RMTestRecords rmTestRecords = new RMTestRecords(zeCollection).alreadySettedUp(getAppLayerFactory());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		assertThat(rmTestRecords.getCategory_X().isLinkable()).isTrue();
		assertThat(rmTestRecords.getCategory_Z().isLinkable()).isFalse();
	}

	private void givenSystemAtVersion5_0_5() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder + File.separator + "olds",
				"given_system_in_5.0.5_with_rm_module__with_test_records_and_root_linkable.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenSystemAtVersion5_0_5_withRootLinkable() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder + File.separator + "olds",
				"given_system_in_5.0.5_with_rm_module__with_test_records.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
