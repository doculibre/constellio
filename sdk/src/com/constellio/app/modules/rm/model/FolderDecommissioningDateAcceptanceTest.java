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
package com.constellio.app.modules.rm.model;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.CLOSE_DATE;
import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.OPEN_DATE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.RetentionType.FIXED;
import static com.constellio.app.modules.rm.model.enums.RetentionType.OPEN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderDecommissioningDateAcceptanceTest extends ConstellioTest {

	LocalDate november4_2009 = new LocalDate(2009, 11, 4);
	LocalDate november4_2010 = new LocalDate(2010, 11, 4);
	LocalDate november4_2011 = new LocalDate(2011, 11, 4);
	LocalDate november4_2012 = new LocalDate(2012, 11, 4);
	LocalDate november4_2013 = new LocalDate(2013, 11, 4);
	LocalDate november4_2014 = new LocalDate(2014, 11, 4);
	LocalDate november4_2015 = new LocalDate(2015, 11, 4);
	LocalDate november4_2016 = new LocalDate(2016, 11, 4);

	LocalDate december31_2010 = new LocalDate(2010, 12, 31);
	LocalDate december31_2012 = new LocalDate(2012, 12, 31);
	LocalDate december31_2013 = new LocalDate(2013, 12, 31);
	LocalDate december31_2014 = new LocalDate(2014, 12, 31);
	LocalDate december31_2015 = new LocalDate(2015, 12, 31);
	LocalDate december31_2016 = new LocalDate(2016, 12, 31);

	LocalDate december12_2009 = new LocalDate(2009, 12, 12);
	LocalDate january12_2010 = new LocalDate(2010, 1, 12);
	LocalDate january12_2012 = new LocalDate(2012, 1, 12);
	LocalDate january12_2014 = new LocalDate(2014, 1, 12);

	LocalDate february15_2015 = new LocalDate(2015, 2, 15);
	LocalDate february16_2015 = new LocalDate(2015, 2, 16);
	LocalDate february17_2015 = new LocalDate(2015, 2, 17);
	LocalDate february15_2017 = new LocalDate(2017, 2, 15);
	LocalDate february16_2017 = new LocalDate(2017, 2, 16);
	LocalDate february17_2017 = new LocalDate(2017, 2, 17);

	RMSchemasRecordsServices schemas;
	RMTestRecords records;
	RecordServices recordServices;

	Transaction transaction = new Transaction();

	String zeCategory;
	String aPrincipalAdminUnit;
	String anotherPrincipalAdminUnit;
	String aSecondaryAdminUnit;

	String PA;
	String MV;
	String MD;

	CopyType noEnteredCopyType = null;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();
		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		zeCategory = records.categoryId_ZE42;
		aPrincipalAdminUnit = records.unitId_10;
		anotherPrincipalAdminUnit = records.unitId_20;
		aSecondaryAdminUnit = records.unitId_30;
		PA = records.PA;
		MV = records.MV;
		MD = records.MD;
	}

	@Test
	public void givenCloseDateNotCaculatedThenEnteredCloseDateIsChoosed()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, false);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);

		Folder folderWithCloseDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(january12_2012));
		assertThat(folderWithCloseDate.getCloseDate()).isEqualTo(january12_2012);

		Folder folderWithoutCloseDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009));
		assertThat(folderWithoutCloseDate.getCloseDate()).isNull();
	}

	@Test
	public void givenDecommissioningDateBasedOnOpenDateThenOpenDateChoosed()
			throws Exception {

		givenConfig(RMConfigs.YEAR_END_DATE, "12/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 90);

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, OPEN_DATE);

		//		Folder folderWithoutOpenDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA)));
		//		assertThat(folderWithoutOpenDate.getDecommissioningDate()).isNull();
		//		assertThat(folderWithoutOpenDate.getOpenDate()).isNull();
		//		assertThat(folderWithoutOpenDate.getCloseDate()).isNull();
		//		assertThat(folderWithoutOpenDate.getCloseDateEntered()).isNull();

		Folder folderWithOpenDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009));

		assertThat(folderWithOpenDate.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folderWithOpenDate.getCloseDate()).isEqualTo(december31_2015);
		assertThat(folderWithOpenDate.getCloseDateEntered()).isNull();
		assertThat(folderWithOpenDate.getDecommissioningDate()).isEqualTo(december31_2010);
	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateThenCloseDateChoosed()
			throws Exception {

		givenConfig(RMConfigs.YEAR_END_DATE, "12/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 30);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);

		//		Folder folderWithoutOpenDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA)));
		//		assertThat(folderWithoutOpenDate.getDecommissioningDate()).isNull();
		//		assertThat(folderWithoutOpenDate.getOpenDate()).isNull();
		//		assertThat(folderWithoutOpenDate.getCloseDate()).isNull();
		//		assertThat(folderWithoutOpenDate.getCloseDateEntered()).isNull();

		Folder folderWithOpenDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009));

		assertThat(folderWithOpenDate.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folderWithOpenDate.getCloseDate()).isEqualTo(december31_2014);
		assertThat(folderWithOpenDate.getCloseDateEntered()).isNull();
		assertThat(folderWithOpenDate.getDecommissioningDate()).isEqualTo(december31_2014);
	}

	@Test
	public void givenDecommissioningDateBasedOnCloseDateAndEnteredCloseDateThenEnteredCloseDateChoosed()
			throws Exception {

		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);

		Folder folderWithEnteredCloseDate = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(january12_2012));

		assertThat(folderWithEnteredCloseDate.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folderWithEnteredCloseDate.getCloseDate()).isEqualTo(january12_2012);
		assertThat(folderWithEnteredCloseDate.getCloseDateEntered()).isEqualTo(january12_2012);
		assertThat(folderWithEnteredCloseDate.getDecommissioningDate()).isEqualTo(december31_2012);
	}

	@Test
	public void givenFolderInSemiActivePhaseWithRealTransferDateThenChoosedAsDecommissioningDate()
			throws Exception {

		givenTimeIs(february17_2015);

		Folder folder = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(january12_2012)
				.setActualTransferDate(february16_2015));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getDecommissioningDate()).isEqualTo(december31_2015);
		assertThat(folder.getCopyRulesExpectedTransferDates()).isEqualTo(asList(new LocalDate[] { null }));

	}

	@Test
	public void givenFolderInInactivePhaseWithRealTransferDateThenCloseDateChoosedAsDecommissioningDate()
			throws Exception {

		givenTimeIs(february17_2017);

		Folder folder = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(january12_2012)
				.setActualTransferDate(february16_2015)
				.setActualDepositDate(february16_2017));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVATE_DEPOSITED);
		assertThat(folder.getDecommissioningDate()).isEqualTo(december31_2012);
		assertThat(folder.getCopyRulesExpectedTransferDates()).isEqualTo(asList(new LocalDate[] { null }));
		assertThat(folder.getCopyRulesExpectedDepositDates()).isEqualTo(asList(new LocalDate[] { null }));

	}

	//	@Test
	//	public void givenFolderWithFixedCopyRuleThenChoosedAsPlanifiedTransferDate()
	//			throws Exception {
	//
	//		Folder folder = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
	//				.setOpenDate(november4_2009)
	//				.setCloseDateEntered(january12_2012)
	//				.setActualTransferDate(february16_2015));
	//
	//		assertThat(folder.getDecommissioningDate()).isEqualTo(january12_2012);
	//		assertThat(folder.getPlanifiedTransferDate()).isEqualTo(february16_2015);
	//
	//	}

	//@Test
	public void givenActiveFolderInSameUnitThanItsRuleWith()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);

		givenRuleWithAdminUnitsAndCopyRules(principal("2-888-D", PA), secondary("1-0-D"));

		Folder folder = saveAndLoad(folderWithCopyRule(principal("5-0-D", PA))
				.setOpenDate(november4_2009));

		assertThat(folder.getCopyStatus()).isEqualTo(PRINCIPAL);
		assertThat(folder.getApplicableCopyRules()).containsOnlyOnce(principal("2-888-D", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("2-888-D", PA));
		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDate()).isEqualTo(november4_2011);
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly((LocalDate) null);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly((LocalDate) null);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly((LocalDate) null);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getArchivisticStatus()).isSameAs(ACTIVE);
		assertThat(folder.getCopyStatus()).isSameAs(PRINCIPAL);
		assertThat(folder.getActiveRetentionType()).isSameAs(FIXED);
		assertThat(folder.getSemiActiveRetentionType()).isSameAs(OPEN);
	}

	// -------------------------------------------------------------------------

	private Folder folderWithCopyRule(CopyRetentionRule copyRetentionRule) {

		RetentionRule rule = givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(copyRetentionRule, secondary("888-888-D", PA));

		if (!transaction.getRecords().isEmpty()) {
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			transaction = new Transaction();
		}

		Folder folder = schemas.newFolder();
		folder.setAdministrativeUnitEntered(aPrincipalAdminUnit);
		folder.setFilingSpaceEntered(records.filingId_A);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(rule);
		folder.setCopyStatusEntered(copyRetentionRule.getCopyType());
		folder.setOpenDate(new LocalDate(2014, 11, 4));
		return folder;
	}

	private Folder saveAndLoad(Folder folder)
			throws RecordServicesException {
		recordServices.add(folder.getWrappedRecord());
		return schemas.getFolder(folder.getId());
	}

	private RetentionRule givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(CopyRetentionRule... rules) {
		return givenRetentionRule(rules).setResponsibleAdministrativeUnits(true);
	}

	private RetentionRule givenRuleWithAdminUnitsAndCopyRules(CopyRetentionRule... rules) {
		return givenRetentionRule(rules).setAdministrativeUnits(Arrays.asList(aPrincipalAdminUnit, anotherPrincipalAdminUnit));
	}

	private RetentionRule givenRetentionRule(CopyRetentionRule... rules) {
		RetentionRule retentionRule = schemas.newRetentionRule();

		retentionRule.setCode("Rule " + anInteger());
		retentionRule.setTitle("Ze rule");
		retentionRule.setApproved(true);
		retentionRule.setCopyRetentionRules(rules);

		return transaction.add(retentionRule);
	}

	private CopyRetentionRule principal(String status, String... mediumTypes) {
		return CopyRetentionRule.newPrincipal(asList(mediumTypes), status);
	}

	private CopyRetentionRule secondary(String status, String... mediumTypes) {
		return CopyRetentionRule.newSecondary(asList(mediumTypes), status);
	}

}
