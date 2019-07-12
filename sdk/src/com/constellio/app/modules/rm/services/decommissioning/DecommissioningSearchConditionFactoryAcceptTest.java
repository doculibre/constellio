package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory.ContainerSearchParameters;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningSearchConditionFactoryAcceptTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DecommissioningSearchConditionFactory factory;

	ContainerSearchParameters params;

	@Before
	public void setUp()
			throws Exception {

		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		factory = new DecommissioningSearchConditionFactory(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenSearchingWithoutClosingDateAndWithFixedPeriodThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(records.unitId_10)).isEmpty();
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(records.unitId_10a))
				.containsOnlyOnce(records.folder_A01, records.folder_A02, records.folder_A03);

		givenTimeIs(new LocalDate(2000, 11, 5));
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(records.unitId_10a))
				.containsOnlyOnce(records.folder_A01, records.folder_A02, records.folder_A03);

		givenTimeIs(new LocalDate(2000, 11, 4));
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(records.unitId_10a))
				.containsOnlyOnce(records.folder_A01, records.folder_A02);

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(records.unitId_30c))
				.containsOnlyOnce(records.folder_C01);
	}

	@Test
	public void whenSearchingWithoutClosingDateAndWith888PeriodThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(records.unitId_10a))
				.containsOnlyOnce(records.folder_A04, records.folder_A05, records.folder_A06);

		givenTimeIs(new LocalDate(2000, 11, 5));
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(records.unitId_10a))
				.containsOnlyOnce(records.folder_A04, records.folder_A05, records.folder_A06);

		givenTimeIs(new LocalDate(2000, 11, 04));
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(records.unitId_10a))
				.containsOnlyOnce(records.folder_A04, records.folder_A05);

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(records.unitId_30c))
				.containsOnlyOnce(records.folder_C02);
	}

	@Test
	public void whenSearchingWithoutClosingDateAndWith999PeriodThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(records.unitId_10a))
				.containsOnlyOnce(records.folder_A07, records.folder_A08, records.folder_A09);

		givenTimeIs(new LocalDate(2000, 11, 05));
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(records.unitId_10a))
				.containsOnlyOnce(records.folder_A07, records.folder_A08, records.folder_A09);

		givenTimeIs(new LocalDate(2000, 11, 04));
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(records.unitId_10a))
				.containsOnlyOnce(records.folder_A07, records.folder_A08);

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(records.unitId_30c))
				.containsOnlyOnce(records.folder_C03);
	}

	@Test
	public void whenSearchingActiveToDepositThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.activeToDeposit(records.unitId_10a))
				.contains(records.folder_A(10, 12)).contains(records.folder_A(16, 21)).contains(records.folder_A(25, 27))
				.hasSize(12);

		givenTimeIs(new LocalDate(2009, 10, 31));
		assertThatResultsOf(factory.activeToDeposit(records.unitId_10a))
				.contains(records.folder_A(10, 12)).contains(records.folder_A(16, 21)).contains(records.folder_A(25, 27))
				.hasSize(12);

		givenTimeIs(new LocalDate(2007, 10, 31));
		assertThatResultsOf(factory.activeToDeposit(records.unitId_10a))
				.containsOnlyOnce(records.folder_A16, records.folder_A17, records.folder_A19, records.folder_A20,
						records.folder_A21, records.folder_A25,
						records.folder_A26, records.folder_A27);

		givenTimeIs(new LocalDate(2007, 10, 30));
		assertThatResultsOf(factory.activeToDeposit(records.unitId_10a))
				.containsOnlyOnce(records.folder_A19, records.folder_A20, records.folder_A25, records.folder_A26);

		givenActualTime();
		assertThatResultsOf(factory.activeToDeposit(records.unitId_30c))
				.containsOnlyOnce(records.folder_C04, records.folder_C07, records.folder_C09);
	}

	@Test
	public void whenSearchingActiveToDestructionThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.activeToDestroy(records.unitId_10a))
				.contains(records.folder_A(10, 15)).contains(records.folder_A(19, 24)).contains(records.folder_A(45, 47))
				.hasSize(15);

		givenTimeIs(new LocalDate(2009, 10, 31));
		assertThatResultsOf(factory.activeToDestroy(records.unitId_10a))
				.contains(records.folder_A(10, 15)).contains(records.folder_A(19, 24)).contains(records.folder_A(45, 47))
				.hasSize(15);

		givenTimeIs(new LocalDate(2004, 10, 31));
		assertThatResultsOf(factory.activeToDestroy(records.unitId_10a))
				.containsOnlyOnce(records.folder_A(13, 15));

		givenTimeIs(new LocalDate(2004, 10, 30));
		assertThatResultsOf(factory.activeToDestroy(records.unitId_10a))
				.containsOnlyOnce(records.folder_A(13, 14));

		givenActualTime();
		assertThatResultsOf(factory.activeToDestroy(records.unitId_30c)).containsOnlyOnce(
				records.folder_C04, records.folder_C05, records.folder_C06, records.folder_C07, records.folder_C08);
	}

	@Test
	public void whenSearchingActiveToTransferThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.activeToTransferToSemiActive(records.unitId_10a))
				.containsOnlyOnce(records.folder_A(10, 12)).containsOnlyOnce(records.folder_A(16, 27));

		givenTimeIs(new LocalDate(2010, 11, 05));
		assertThatResultsOf(factory.activeToTransferToSemiActive(records.unitId_10a))
				.containsOnlyOnce(records.folder_A(10, 12)).containsOnlyOnce(records.folder_A(16, 27));

		givenTimeIs(new LocalDate(2003, 10, 31));
		assertThatResultsOf(factory.activeToTransferToSemiActive(records.unitId_10a))
				.containsOnlyOnce(records.folder_A16, records.folder_A17,
						records.folder_A18, records.folder_A19,
						records.folder_A20, records.folder_A21);

		givenTimeIs(new LocalDate(2003, 10, 30));
		assertThatResultsOf(factory.activeToTransferToSemiActive(records.unitId_10a))
				.containsOnlyOnce(records.folder_A16, records.folder_A17, records.folder_A19, records.folder_A20);

		givenActualTime();
		assertThatResultsOf(factory.activeToTransferToSemiActive(records.unitId_30c))
				.containsOnlyOnce(records.folder_C04, records.folder_C07,
						records.folder_C08, records.folder_C09);
	}

	@Test
	public void whenSearchingSemiActiveToDepositThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDeposit(records.unitId_10a))
				.contains(records.folder_A(42, 44)).contains(records.folder_A(48, 53)).contains(records.folder_A(57, 59))
				.hasSize(12);

		System.out.println(records.getFolder_A44().getExpectedDepositDate());

		givenTimeIs(new LocalDate(2010, 10, 31));
		assertThatResultsOf(factory.semiActiveToDeposit(records.unitId_10a))
				.contains(records.folder_A(42, 44)).contains(records.folder_A(48, 53)).contains(records.folder_A(57, 59))
				.hasSize(12);

		givenTimeIs(new LocalDate(2008, 10, 31));
		assertThatResultsOf(factory.semiActiveToDeposit(records.unitId_10a))
				.contains(records.folder_A(51, 52)).contains(records.folder_A(57, 59)).hasSize(5);

		givenTimeIs(new LocalDate(2008, 10, 30));
		assertThatResultsOf(factory.semiActiveToDeposit(records.unitId_10a))
				.containsOnlyOnce(records.folder_A57, records.folder_A58);

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDeposit(records.unitId_30c))
				.containsOnlyOnce(records.folder_C30, records.folder_C33, records.folder_C35);
	}

	@Test
	public void whenSearchingSemiActiveToDestructionThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDestroy(records.unitId_10a))
				.contains(records.folder_A(42, 44)).contains(records.folder_A(51, 56)).hasSize(9);

		givenTimeIs(new LocalDate(2010, 11, 5));
		assertThatResultsOf(factory.semiActiveToDestroy(records.unitId_10a))
				.contains(records.folder_A(42, 44)).contains(records.folder_A(51, 56)).hasSize(9);

		givenTimeIs(new LocalDate(2007, 10, 31));
		assertThatResultsOf(factory.semiActiveToDestroy(records.unitId_10a))
				.contains(records.folder_A(54, 55)).hasSize(2);

		givenTimeIs(new LocalDate(2007, 10, 30));
		assertThatResultsOf(factory.semiActiveToDestroy(records.unitId_10a)).isEmpty();

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDestroy(records.unitId_30c))
				.containsOnlyOnce(records.folder_C30, records.folder_C33, records.folder_C34);
	}

	@Test
	public void whenSearchingDocumentSemiActiveToDestructionThenObtainsValidResults()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenDisabledAfterTestValidations();
		waitForBatchProcess();
		reindexIfRequired();

		givenTimeIs(new LocalDate(2100, 11, 5));
		assertThatResultsOf(factory.documentSemiActiveToDestroy(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(42, 44))
				.contains(records.decommissionnableProcesInFolder_A(48, 50))
				.contains(records.decommissionnableProcesInFolder_A(54, 59)).hasSize(24);

		givenTimeIs(new LocalDate(2007, 10, 31));
		assertThatResultsOf(factory.documentSemiActiveToDestroy(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(48, 50)).hasSize(6);

		givenTimeIs(new LocalDate(2007, 10, 30));
		assertThatResultsOf(factory.documentSemiActiveToDestroy(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(48, 49)).hasSize(4);
	}

	@Test
	public void whenSearchingDocumentActiveToDestructionThenObtainsValidResults()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenDisabledAfterTestValidations();
		assertThat(getAppLayerFactory().getSystemGlobalConfigsManager().isReindexingRequired()).isTrue();
		//TODO Gabriel : Le changement de la config CALCULATED_CLOSING_DATE devrait demander une réindexation plutôt que partir un traîtement en lot
		reindexIfRequired();

		waitForBatchProcess();

		givenTimeIs(new LocalDate(2100, 11, 5));

		assertThat(records.getFolder_A07().getExpectedDepositDate()).isEqualTo(date(2007, 10, 31));

		assertThatResultsOf(factory.documentActiveToDestroy(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(1, 6))
				.contains(records.decommissionnableProcesInFolder_A(10, 12))
				.contains(records.decommissionnableProcesInFolder_A(16, 18))
				.contains(records.decommissionnableProcesInFolder_A(22, 27)).hasSize(36);

		givenTimeIs(new LocalDate(2005, 10, 31));
		assertThatResultsOf(factory.documentActiveToDestroy(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(4, 6))
				.contains(records.decommissionnableProcesInFolder_A(10, 12))
				.contains(records.decommissionnableProcesInFolder_A(16, 18))
				.contains(records.decommissionnableProcesInFolder_A(22, 23))
				.contains(records.decommissionnableProcesInFolder_A(25, 26)).hasSize(26);

		givenTimeIs(new LocalDate(2005, 10, 30));
		assertThatResultsOf(factory.documentActiveToDestroy(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(10, 11))
				.contains(records.decommissionnableProcesInFolder_A(16, 17)).hasSize(8);

	}

	@Test
	public void whenSearchingDocumentSemiActiveToConservationThenObtainsValidResults()
			throws Exception {
		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.disable();
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenDisabledAfterTestValidations();
		waitForBatchProcess();
		reindexIfRequired();
		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		givenTimeIs(new LocalDate(2100, 11, 5));
		assertThatResultsOf(factory.documentSemiActiveToDeposit(records.unitId_10a))
				.contains(records.decommissionnableContractsInFolder_A(42, 44))
				.contains(records.decommissionnableContractsInFolder_A(48, 50))
				.contains(records.decommissionnableContractsInFolder_A(54, 59)).hasSize(24);

		givenTimeIs(new LocalDate(2005, 10, 31));
		assertThatResultsOf(factory.documentSemiActiveToDeposit(records.unitId_10a))
				.contains(records.decommissionnableContractsInFolder_A(48, 50)).hasSize(6);

		givenTimeIs(new LocalDate(2005, 10, 30));
		assertThatResultsOf(factory.documentSemiActiveToDeposit(records.unitId_10a))
				.contains(records.decommissionnableContractsInFolder_A(48, 49)).hasSize(4);

	}

	@Test
	public void whenSearchingDocumentActiveToConservationThenObtainsValidResults()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);

		assertThat(getAppLayerFactory().getSystemGlobalConfigsManager().isReindexingRequired()).isTrue();
		//TODO Gabriel : Le changement de la config CALCULATED_CLOSING_DATE devrait demander une réindexation plutôt que partir un traîtement en lot
		reindexIfRequired();

		waitForBatchProcess();

		givenTimeIs(new LocalDate(2100, 11, 5));
		assertThatResultsOf(factory.documentActiveToDeposit(records.unitId_10a))
				.contains(records.decommissionnableContractsInFolder_A(1, 6))
				.contains(records.decommissionnableContractsInFolder_A(10, 12))
				.contains(records.decommissionnableContractsInFolder_A(16, 18))
				.contains(records.decommissionnableContractsInFolder_A(22, 27)).hasSize(36);

		givenTimeIs(new LocalDate(2005, 10, 31));
		assertThatResultsOf(factory.documentActiveToDeposit(records.unitId_10a))
				.contains(records.decommissionnableContractsInFolder_A(4, 6))
				.contains(records.decommissionnableContractsInFolder_A(10, 12))
				.contains(records.decommissionnableContractsInFolder_A(16, 18))
				.contains(records.decommissionnableContractsInFolder_A(22, 27)).hasSize(30);

		givenTimeIs(new LocalDate(2005, 10, 30));
		assertThatResultsOf(factory.documentActiveToDeposit(records.unitId_10a))
				.contains(records.decommissionnableContractsInFolder_A(4, 6))
				.contains(records.decommissionnableContractsInFolder_A(10, 12))
				.contains(records.decommissionnableContractsInFolder_A(16, 18))
				.contains(records.decommissionnableContractsInFolder_A(22, 23))
				.contains(records.decommissionnableContractsInFolder_A(25, 26)).hasSize(26);

	}

	@Test
	public void whenSearchingDocumentActiveToTransferThenObtainsValidResults()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenDisabledAfterTestValidations();
		assertThat(getAppLayerFactory().getSystemGlobalConfigsManager().isReindexingRequired()).isTrue();
		//TODO Gabriel : Le changement de la config CALCULATED_CLOSING_DATE devrait demander une réindexation plutôt que partir un traîtement en lot
		reindexIfRequired();

		waitForBatchProcess();

		givenTimeIs(new LocalDate(2100, 11, 5));
		assertThatResultsOf(factory.documentTransfer(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(1, 3))
				.contains(records.decommissionnableProcesInFolder_A(10, 12))
				.contains(records.decommissionnableProcesInFolder_A(22, 27)).hasSize(24);

		givenTimeIs(new LocalDate(2005, 10, 31));
		assertThatResultsOf(factory.documentTransfer(records.unitId_10a))
				.contains(records.decommissionnableProcesInFolder_A(10, 12))
				.contains(records.decommissionnableProcesInFolder_A(22, 27)).hasSize(18);

	}

	@Test
	public void givenAdminWhenSearchingContainersThenValidResults()
			throws Exception {

		assertThatContainersCountWith(unit10().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(5);
		assertThatContainersCountWith(unit10_filingA().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(3);
		assertThatContainersListWith(unit10_filingA().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).containsOnly(
				records.containerId_bac11, records.containerId_bac12, records.containerId_bac13);

		assertThatContainersCountWith(unit11().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(1);
		assertThatContainersCountWith(unit11_filingB().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(1);
		assertThatContainersListWith(unit11_filingB().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).containsOnly(
				records.containerId_bac09);

		assertThatContainersCountWith(unit10().setWithStorage(false).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(3);
		assertThatContainersCountWith(unit10_filingA().setWithStorage(false).setType(TRANSFERT_TO_SEMI_ACTIVE))
				.isEqualTo(3);
		assertThatContainersListWith(unit10_filingA().setWithStorage(false).setType(TRANSFERT_TO_SEMI_ACTIVE)).containsOnly(
				records.containerId_bac10, records.containerId_bac14, records.containerId_bac15);

		assertThatContainersCountWith(unit10().setWithStorage(true).setType(DEPOSIT)).isEqualTo(3);
		assertThatContainersCountWith(unit10_filingA().setWithStorage(true).setType(DEPOSIT)).isEqualTo(2);
		assertThatContainersListWith(unit10_filingA().setWithStorage(true).setType(DEPOSIT)).containsOnly(
				records.containerId_bac04, records.containerId_bac05);

		assertThatContainersCountWith(unit10().setWithStorage(false).setType(DEPOSIT)).isEqualTo(3);
		assertThatContainersCountWith(unit12().setWithStorage(false).setType(DEPOSIT)).isEqualTo(1);
		assertThatContainersCountWith(unit12_filingB().setWithStorage(false).setType(DEPOSIT)).isEqualTo(1);
		assertThatContainersListWith(unit12_filingB().setWithStorage(false).setType(DEPOSIT)).containsOnly(
				records.containerId_bac02);

	}

	@Test
	public void givenAdminWhenSearchingSubAdministrativeUnitThenOk()
			throws Exception {

		assertThat(factory.getVisibleSubAdministrativeUnitCount(records.unitId_10)).isEqualTo(3);
	}

	public org.assertj.core.api.LongAssert assertThatContainersCountWith(ContainerSearchParameters params) {
		long containers = factory.getVisibleContainersCount(params);
		return assertThat(containers);
	}

	public org.assertj.core.api.ListAssert<String> assertThatContainersListWith(ContainerSearchParameters params) {
		LogicalSearchCondition condition = factory.getVisibleContainersCondition(params);
		List<String> results = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery(condition));
		return assertThat(results);
	}

	public org.assertj.core.api.ListAssert<String> assertThatResultsOf(LogicalSearchCondition condition) {
		List<String> results = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery(condition));
		return assertThat(results);
	}

	private ContainerSearchParameters params() {
		return params = new ContainerSearchParameters();
	}

	private ContainerSearchParameters unit10() {
		return params().setUserId(records.getAdmin().getId()).setAdminUnitId(records.unitId_10);
	}

	private ContainerSearchParameters unit10_filingA() {
		return params().setUserId(records.getAdmin().getId()).setAdminUnitId(records.unitId_10a);
	}

	private ContainerSearchParameters unit11() {
		return params().setUserId(records.getAdmin().getId()).setAdminUnitId(records.unitId_11);
	}

	private ContainerSearchParameters unit11_filingB() {
		return params().setUserId(records.getAdmin().getId()).setAdminUnitId(records.unitId_11b);
	}

	private ContainerSearchParameters unit12() {
		return params().setUserId(records.getAdmin().getId()).setAdminUnitId(records.unitId_12);
	}

	private ContainerSearchParameters unit12_filingB() {
		return params().setUserId(records.getAdmin().getId()).setAdminUnitId(records.unitId_12b);
	}

}
