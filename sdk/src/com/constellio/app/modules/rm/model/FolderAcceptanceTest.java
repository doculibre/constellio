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

import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.CLOSE_DATE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVATE_DEPOSITED;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DESTROYED;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
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
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class FolderAcceptanceTest extends ConstellioTest {

	LocalDate november4_2009 = new LocalDate(2009, 11, 4);
	LocalDate november4_2010 = new LocalDate(2010, 11, 4);

	LocalDate december12_2009 = new LocalDate(2009, 12, 12);
	LocalDate january12_2010 = new LocalDate(2010, 1, 12);
	LocalDate february16_2012 = new LocalDate(2012, 1, 12);

	LocalDate january1_2015 = new LocalDate(2015, 1, 1);
	LocalDate february2_2015 = new LocalDate(2015, 2, 1);
	LocalDate february11_2015 = new LocalDate(2015, 2, 11);
	LocalDate march31_2015 = new LocalDate(2015, 3, 31);
	LocalDate march31_2016 = new LocalDate(2016, 3, 31);
	LocalDate march31_2017 = new LocalDate(2017, 3, 31);
	LocalDate march31_2018 = new LocalDate(2018, 3, 31);
	LocalDate march31_2019 = new LocalDate(2019, 3, 31);
	LocalDate march31_2020 = new LocalDate(2020, 3, 31);
	LocalDate march31_2021 = new LocalDate(2021, 3, 31);
	LocalDate march31_2022 = new LocalDate(2022, 3, 31);
	LocalDate march31_2023 = new LocalDate(2023, 3, 31);
	LocalDate march31_2024 = new LocalDate(2024, 3, 31);
	LocalDate march31_2025 = new LocalDate(2025, 3, 31);

	LocalDate april1_2004 = new LocalDate(2004, 4, 1);
	LocalDate april1_2014 = new LocalDate(2014, 4, 1);
	LocalDate march31_2005 = new LocalDate(2005, 3, 31);
	LocalDate march31_2026 = new LocalDate(2026, 3, 31);
	LocalDate march31_2029 = new LocalDate(2029, 3, 31);
	LocalDate march31_2035 = new LocalDate(2035, 3, 31);
	LocalDate march31_2036 = new LocalDate(2036, 3, 31);
	LocalDate march31_2046 = new LocalDate(2046, 3, 31);
	LocalDate march31_2056 = new LocalDate(2056, 3, 31);
	LocalDate march31_2061 = new LocalDate(2061, 3, 31);
	LocalDate march31_2065 = new LocalDate(2065, 3, 31);
	LocalDate march31_2066 = new LocalDate(2066, 3, 31);

	RMSchemasRecordsServices schemas;
	RMTestRecords records;
	RecordServices recordServices;

	Transaction transaction = new Transaction();

	String zeRule = "zeRule";
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
	public void whenSaveFolderThenMetadataValuesSaved()
			throws Exception {

		Comment comment1 = new Comment("Ze message", records.getDakota_managerInA_userInB(), new LocalDateTime().minusWeeks(4));
		Comment comment2 = new Comment("An other message", records.getEdouard_managerInB_userInC(),
				new LocalDateTime().minusWeeks(1));

		Folder folder = schemas.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11);
		folder.setDescription("Ze description");
		folder.setFilingSpaceEntered(records.filingId_A);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle("Ze folder");
		folder.setMediumTypes(Arrays.asList(PA, MV));
		folder.setUniformSubdivisionEntered(records.subdivId_2);
		folder.setOpenDate(november4_2009);
		folder.setCloseDateEntered(december12_2009);
		folder.setComments(asList(comment1, comment2));

		folder = saveAndLoad(folder);

		assertThat(folder.getAdministrativeUnitEntered()).isEqualTo(records.unitId_11);
		assertThat(folder.getDescription()).isEqualTo("Ze description");
		assertThat(folder.getFilingSpaceEntered()).isEqualTo(records.filingId_A);
		assertThat(folder.getFilingSpaceCode()).isEqualTo(records.getFilingA().getCode());
		assertThat(folder.getUniformSubdivisionEntered()).isEqualTo(records.subdivId_2);
		assertThat(folder.getCategoryEntered()).isEqualTo(records.categoryId_X110);
		assertThat(folder.getCategoryCode()).isEqualTo(records.getCategory_X110().getCode());
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(records.ruleId_2);
		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder.getTitle()).isEqualTo("Ze folder");
		assertThat(folder.getMediumTypes()).isEqualTo(Arrays.asList(PA, MV));
		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getComments()).isEqualTo(asList(comment1, comment2));
		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isFalse();
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);

	}

	@Test
	public void givenFolderWithoutTransferDispoalAndDestructionDatesThenActive()
			throws RecordServicesException {
		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009));

		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isTrue();
		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(ACTIVE);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
	}

	@Test
	public void givenFolderWithTransferDateAndWithoutDestructionOrDepositThenSemiActive()
			throws Exception {

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setActualTransferDate(january12_2010));

		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(SEMI_ACTIVE);
		assertThat(folder.getActualTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
	}

	@Test
	public void givenFolderWithDepositDateThenInactive()
			throws Exception {

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setActualTransferDate(january12_2010)
				.setActualDepositDate(february16_2012));

		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(INACTIVATE_DEPOSITED);
		assertThat(folder.getActualTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getActualDepositDate()).isEqualTo(february16_2012);
		assertThat(folder.getActualDestructionDate()).isNull();
	}

	@Test
	public void givenFolderWithDestructionDateThenInactive()
			throws Exception {

		Folder folder = saveAndLoad(folderWithSingleCopyRule(principal("888-0-D", PA))
				.setOpenDate(november4_2009)
				.setCloseDateEntered(december12_2009)
				.setActualTransferDate(january12_2010)
				.setActualDestructionDate(february16_2012));

		assertThat(folder.getOpenDate()).isEqualTo(november4_2009);
		assertThat(folder.getCloseDateEntered()).isEqualTo(december12_2009);
		assertThat(folder.getArchivisticStatus()).isEqualTo(INACTIVE_DESTROYED);
		assertThat(folder.getActualTransferDate()).isEqualTo(january12_2010);
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isEqualTo(february16_2012);
	}

	@Test
	//Tested on IntelliGID 4!
	public void givenPrincipalFolderWithTwoMediumTypesAndYearEndInInsufficientPeriodThenHasValidCalculedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("2-2-C", PA), principal("5-5-D", MD),
				secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.hasAnalogicalMedium()).isTrue();
		assertThat(folder.hasElectronicMedium()).isTrue();
		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2016);
		assertThat(folder.getDecommissioningDate()).isEqualTo(march31_2016);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("2-2-C", PA), principal("5-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("2-2-C", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2018, march31_2021);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(null, march31_2026);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2020, null);
		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2018);
		//TODO Valider si la date de destruction ne devrait pas plutôt être non-nulle
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(null);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2020);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 60);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("5-5-D", MD), principal("2-2-C", PA),
				secondary("1-0-D", MD, PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(january1_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(january1_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2015);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("5-5-D", MD), principal("2-2-C", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("2-2-C", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2020, march31_2017);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2025, null);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(null, march31_2019);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2017);
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2019);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenSemiActiveFoldersThenHasValidCalculedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("888-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(april1_2004)
				.setActualTransferDate(april1_2014)
				.setMediumTypes(PA, MD));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(april1_2004);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2005);
		assertThat(folder.getActualTransferDate()).isEqualTo(april1_2014);
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).isEqualTo(asList(new LocalDate[] { null, null }));
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2020, march31_2020);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2020, null);

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2020);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2020);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, 0);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 0);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(PA, MD));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2016);
		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2016, march31_2016);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2021, march31_2021);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2021, null);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2016);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2021);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2021);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsAndDecommissioningDelaysThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2026);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2056, march31_2056);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2061, march31_2061);
		assertThat(folder.getCopyRulesExpectedDepositDates()).containsExactly(march31_2061, null);

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2056);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2061);
		assertThat(folder.getExpectedDepositDate()).isEqualTo(march31_2061);

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveSecondaryFoldersWithOpenPeriodsAndDecommissioningDelaysThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 30);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 20);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, 10);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, 30);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 40);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("3-3-T", PA), principal("888-888-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(secondaryFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(march31_2035);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(secondary("999-0-D", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(secondary("999-0-D", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).containsExactly(march31_2065);
		assertThat(folder.getCopyRulesExpectedDestructionDates()).containsExactly(march31_2065);
		assertThat(folder.getCopyRulesExpectedDepositDates()).isEqualTo(asList(new LocalDate[] { null }));

		assertThat(folder.getExpectedTransferDate()).isEqualTo(march31_2065);
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(march31_2065);
		assertThat(folder.getExpectedDepositDate()).isNull();

	}

	@Test
	//Tested on IntelliGID 4!
	public void givenActiveFoldersWithOpenPeriodsAndDisabledDecommissioningCalculationThenValidCalculatedDates()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK, 90);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE, -1);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, -1);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD, -1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, -1);
		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("888-5-T", PA), principal("888-5-D", MD),
				secondary("999-0-D", PA));

		Folder folder = saveAndLoad(principalFolderWithZeRule()
				.setOpenDate(february2_2015)
				.setCloseDateEntered(february11_2015)
				.setMediumTypes(MD, PA));

		assertThat(folder.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
		assertThat(folder.getOpenDate()).isEqualTo(february2_2015);
		assertThat(folder.getCloseDate()).isEqualTo(february11_2015);
		assertThat(folder.getDecommissioningDate()).isEqualTo(march31_2016);
		assertThat(folder.getActualTransferDate()).isNull();
		assertThat(folder.getActualDepositDate()).isNull();
		assertThat(folder.getActualDestructionDate()).isNull();
		assertThat(folder.getApplicableCopyRules()).containsExactly(principal("888-5-T", PA), principal("888-5-D", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("888-5-T", PA));
		assertThat(folder.getCopyRulesExpectedTransferDates()).isEqualTo(asList(new LocalDate[] { null, null }));
		assertThat(folder.getCopyRulesExpectedDestructionDates()).isEqualTo(asList(new LocalDate[] { null, null }));
		assertThat(folder.getCopyRulesExpectedDepositDates()).isEqualTo(asList(new LocalDate[] { null, null }));

		assertThat(folder.getExpectedTransferDate()).isNull();
		assertThat(folder.getExpectedDestructionDate()).isNull();
		assertThat(folder.getExpectedDepositDate()).isNull();

	}

	// -------------------------------------------------------------------------

	private Folder folderWithSingleCopyRule(CopyRetentionRule copyRetentionRule) {

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(copyRetentionRule, secondary("888-888-D", PA));

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
		folder.setRetentionRuleEntered(zeRule);
		folder.setCopyStatusEntered(copyRetentionRule.getCopyType());
		return folder;
	}

	private Folder principalFolderWithZeRule() {

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
		folder.setRetentionRuleEntered(zeRule);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		return folder;
	}

	private Folder secondaryFolderWithZeRule() {

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
		folder.setRetentionRuleEntered(zeRule);
		folder.setCopyStatusEntered(CopyType.SECONDARY);
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
		RetentionRule retentionRule = schemas.newRetentionRuleWithId(zeRule);

		retentionRule.setCode("Ze rule");
		retentionRule.setTitle("Ze rule");
		retentionRule.setApproved(true);
		//		retentionRule.setCategories(asList(zeCategory));
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
