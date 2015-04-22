/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory.ContainerSearchParameters;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class DecommissioningSearchConditionFactoryAcceptTest extends ConstellioTest {

	RMSchemasRecordsServices schemas;
	RMTestRecords rm = new RMTestRecords(zeCollection);
	DecommissioningSearchConditionFactory factory;

	ContainerSearchParameters params;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		factory = new DecommissioningSearchConditionFactory(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenSearchingWithoutClosingDateAndWithFixedPeriodThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A01, rm.folder_A02, rm.folder_A03);

		givenTimeIs(new LocalDate(2000, 11, 5));
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A01, rm.folder_A02, rm.folder_A03);

		givenTimeIs(new LocalDate(2000, 11, 4));
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A01, rm.folder_A02);

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWithFixedPeriod(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C01);
	}

	@Test
	public void whenSearchingWithoutClosingDateAndWith888PeriodThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A04, rm.folder_A05, rm.folder_A06);

		givenTimeIs(new LocalDate(2000, 11, 5));
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A04, rm.folder_A05, rm.folder_A06);

		givenTimeIs(new LocalDate(2000, 11, 04));
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A04, rm.folder_A05);

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith888Period(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C02);
	}

	@Test
	public void whenSearchingWithoutClosingDateAndWith999PeriodThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A07, rm.folder_A08, rm.folder_A09);

		givenTimeIs(new LocalDate(2000, 11, 05));
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A07, rm.folder_A08, rm.folder_A09);

		givenTimeIs(new LocalDate(2000, 11, 04));
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A07, rm.folder_A08);

		givenActualTime();
		assertThatResultsOf(factory.withoutClosingDateAndWith999Period(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C03);
	}

	@Test
	public void whenSearchingActiveToDepositThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.activeToDeposit(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(10, 12)).contains(rm.folder_A(16, 21)).contains(rm.folder_A(25, 27)).hasSize(12);

		givenTimeIs(new LocalDate(2009, 10, 31));
		assertThatResultsOf(factory.activeToDeposit(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(10, 12)).contains(rm.folder_A(16, 21)).contains(rm.folder_A(25, 27)).hasSize(12);

		givenTimeIs(new LocalDate(2007, 10, 31));
		assertThatResultsOf(factory.activeToDeposit(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A16, rm.folder_A17, rm.folder_A19, rm.folder_A20, rm.folder_A21, rm.folder_A25,
						rm.folder_A26, rm.folder_A27);

		givenTimeIs(new LocalDate(2007, 10, 30));
		assertThatResultsOf(factory.activeToDeposit(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A19, rm.folder_A20, rm.folder_A25, rm.folder_A26);

		givenActualTime();
		assertThatResultsOf(factory.activeToDeposit(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C04, rm.folder_C07, rm.folder_C09);
	}

	@Test
	public void whenSearchingActiveToDestructionThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.activeToDestroy(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(10, 15)).contains(rm.folder_A(19, 24)).hasSize(12);

		givenTimeIs(new LocalDate(2009, 10, 31));
		assertThatResultsOf(factory.activeToDestroy(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(10, 15)).contains(rm.folder_A(19, 24)).hasSize(12);

		givenTimeIs(new LocalDate(2004, 10, 31));
		assertThatResultsOf(factory.activeToDestroy(rm.filingId_A, rm.unitId_10)).containsOnlyOnce(rm.folder_A(13, 15));

		givenTimeIs(new LocalDate(2004, 10, 30));
		assertThatResultsOf(factory.activeToDestroy(rm.filingId_A, rm.unitId_10)).containsOnlyOnce(rm.folder_A(13, 14));

		givenActualTime();
		assertThatResultsOf(factory.activeToDestroy(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C04, rm.folder_C05, rm.folder_C06, rm.folder_C07, rm.folder_C08);
	}

	@Test
	public void whenSearchingActiveToTransferThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.activeToTransferToSemiActive(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A(10, 27));

		givenTimeIs(new LocalDate(2010, 11, 05));
		assertThatResultsOf(factory.activeToTransferToSemiActive(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A(10, 27));

		givenTimeIs(new LocalDate(2003, 10, 31));
		assertThatResultsOf(factory.activeToTransferToSemiActive(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A13, rm.folder_A14, rm.folder_A16, rm.folder_A17, rm.folder_A18, rm.folder_A19,
						rm.folder_A20, rm.folder_A21);

		givenTimeIs(new LocalDate(2003, 10, 30));
		assertThatResultsOf(factory.activeToTransferToSemiActive(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A16, rm.folder_A17, rm.folder_A19, rm.folder_A20);

		givenActualTime();
		assertThatResultsOf(factory.activeToTransferToSemiActive(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C04, rm.folder_C05, rm.folder_C06, rm.folder_C07, rm.folder_C08, rm.folder_C09);
	}

	@Test
	public void whenSearchingSemiActiveToDepositThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDeposit(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(42, 44)).contains(rm.folder_A(48, 53)).contains(rm.folder_A(57, 59)).hasSize(12);

		System.out.println(rm.getFolder_A44().getExpectedDepositDate());

		givenTimeIs(new LocalDate(2010, 10, 31));
		assertThatResultsOf(factory.semiActiveToDeposit(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(42, 44)).contains(rm.folder_A(48, 53)).contains(rm.folder_A(57, 59)).hasSize(12);

		givenTimeIs(new LocalDate(2008, 10, 31));
		assertThatResultsOf(factory.semiActiveToDeposit(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(51, 52)).contains(rm.folder_A(57, 59)).hasSize(5);

		givenTimeIs(new LocalDate(2008, 10, 30));
		assertThatResultsOf(factory.semiActiveToDeposit(rm.filingId_A, rm.unitId_10))
				.containsOnlyOnce(rm.folder_A57, rm.folder_A58);

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDeposit(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C30, rm.folder_C33, rm.folder_C35);
	}

	@Test
	public void whenSearchingSemiActiveToDestructionThenObtainsValidResults()
			throws Exception {

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDestroy(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(42, 47)).contains(rm.folder_A(51, 56)).hasSize(12);

		givenTimeIs(new LocalDate(2010, 11, 05));
		assertThatResultsOf(factory.semiActiveToDestroy(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(42, 47)).contains(rm.folder_A(51, 56)).hasSize(12);

		givenTimeIs(new LocalDate(2007, 10, 31));
		assertThatResultsOf(factory.semiActiveToDestroy(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(45, 47)).contains(rm.folder_A(54, 55)).hasSize(5);

		givenTimeIs(new LocalDate(2007, 10, 30));
		assertThatResultsOf(factory.semiActiveToDestroy(rm.filingId_A, rm.unitId_10))
				.contains(rm.folder_A(45, 47)).hasSize(3);

		givenActualTime();
		assertThatResultsOf(factory.semiActiveToDestroy(rm.filingId_C, rm.unitId_30))
				.containsOnlyOnce(rm.folder_C30, rm.folder_C31, rm.folder_C32, rm.folder_C33, rm.folder_C34);
	}

	@Test
	public void givenAdminWhenCountingFilingSpacesInAdministrativeUnitThenIncludeChildAdministrativeUnits()
			throws RecordServicesException {

		AdministrativeUnit unit10 = rm.getUnit10();
		AdministrativeUnit unit12 = rm.getUnit12();

		List<String> unit10Spaces = new ArrayList<>(unit10.getFilingSpaces());
		unit10Spaces.add("filing10d");
		List<String> unit12Spaces = new ArrayList<>(unit12.getFilingSpaces());
		unit12Spaces.add("filing12c");
		unit12Spaces.add("filing12d");

		Transaction transaction = new Transaction();
		transaction.add(schemas.newFilingSpaceWithId("filing12c").setTitle("12c").setCode("12c"));
		transaction.add(schemas.newFilingSpaceWithId("filing12d").setTitle("12d").setCode("12d"));
		transaction.add(schemas.newFilingSpaceWithId("filing10d").setTitle("10d").setCode("10d"));
		transaction.addUpdate(rm.getUnit10().setFilingSpaces(unit10Spaces).getWrappedRecord());
		transaction.addUpdate(rm.getUnit12().setFilingSpaces(unit12Spaces).getWrappedRecord());
		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(factory.getVisibleFilingSpacesCount(rm.unitId_10)).isEqualTo(6);
		assertThat(factory.getVisibleFilingSpacesCount(rm.unitId_11)).isEqualTo(1);
		assertThat(factory.getVisibleFilingSpacesCount(rm.unitId_12)).isEqualTo(4);
	}

	@Test
	public void givenAdminWhenSearchingContainersThenValidResults()
			throws Exception {

		assertThatContainersCountWith(unit10().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(5);
		assertThatContainersCountWith(unit10_filingA().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(3);
		assertThatContainersListWith(unit10_filingA().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).containsOnly(
				rm.containerId_bac11, rm.containerId_bac12, rm.containerId_bac13);

		assertThatContainersCountWith(unit11().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(1);
		assertThatContainersCountWith(unit11_filingB().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(1);
		assertThatContainersListWith(unit11_filingB().setWithStorage(true).setType(TRANSFERT_TO_SEMI_ACTIVE)).containsOnly(
				rm.containerId_bac09);

		assertThatContainersCountWith(unit10().setWithStorage(false).setType(TRANSFERT_TO_SEMI_ACTIVE)).isEqualTo(3);
		assertThatContainersCountWith(unit10_filingA().setWithStorage(false).setType(TRANSFERT_TO_SEMI_ACTIVE))
				.isEqualTo(3);
		assertThatContainersListWith(unit10_filingA().setWithStorage(false).setType(TRANSFERT_TO_SEMI_ACTIVE)).containsOnly(
				rm.containerId_bac10, rm.containerId_bac14, rm.containerId_bac15);

		assertThatContainersCountWith(unit10().setWithStorage(true).setType(DEPOSIT)).isEqualTo(3);
		assertThatContainersCountWith(unit10_filingA().setWithStorage(true).setType(DEPOSIT)).isEqualTo(2);
		assertThatContainersListWith(unit10_filingA().setWithStorage(true).setType(DEPOSIT)).containsOnly(
				rm.containerId_bac04, rm.containerId_bac05);

		assertThatContainersCountWith(unit10().setWithStorage(false).setType(DEPOSIT)).isEqualTo(3);
		assertThatContainersCountWith(unit12().setWithStorage(false).setType(DEPOSIT)).isEqualTo(1);
		assertThatContainersCountWith(unit12_filingB().setWithStorage(false).setType(DEPOSIT)).isEqualTo(1);
		assertThatContainersListWith(unit12_filingB().setWithStorage(false).setType(DEPOSIT)).containsOnly(
				rm.containerId_bac02);

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
		return params().setUserId(rm.getAdmin().getId()).setAdminUnitId(rm.unitId_10);
	}

	private ContainerSearchParameters unit10_filingA() {
		return params().setUserId(rm.getAdmin().getId()).setAdminUnitId(rm.unitId_10).setFilingSpaceId(rm.filingId_A);
	}

	private ContainerSearchParameters unit11() {
		return params().setUserId(rm.getAdmin().getId()).setAdminUnitId(rm.unitId_11);
	}

	private ContainerSearchParameters unit11_filingB() {
		return params().setUserId(rm.getAdmin().getId()).setAdminUnitId(rm.unitId_11).setFilingSpaceId(rm.filingId_B);
	}

	private ContainerSearchParameters unit12() {
		return params().setUserId(rm.getAdmin().getId()).setAdminUnitId(rm.unitId_12);
	}

	private ContainerSearchParameters unit12_filingB() {
		return params().setUserId(rm.getAdmin().getId()).setAdminUnitId(rm.unitId_12).setFilingSpaceId(rm.filingId_B);
	}

}
