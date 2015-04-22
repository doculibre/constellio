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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.joda.time.LocalDate;
import org.junit.Before;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

/**
 *
 * IntelliGID 4 LEGACY TEST
 *
 * If this test become painful to maintain, deleteLogically it, it won't affect test coverage
 *
 */
public class FolderLegacyAcceptanceTest extends ConstellioTest {

	private static String rule_888_2_C__888_2_C;
	private static String rule_999_2_C__999_2_C;
	private static String rule_2_1_C__2_1_C;
	private static String rule_888_2_D__888_2_D;
	private static String rule_999_2_D__999_2_D;
	private static String rule_2_1_D__2_1_D;
	private static String rule_888_0_C__888_0_C;
	private static String rule_999_0_C__999_0_C;
	private static String rule_2_0_C__2_0_C;
	private static String rule_888_0_D__888_0_D;
	private static String rule_999_0_D__999_0_D;
	private static String rule_2_0_D__2_0_D;

	String rule_888_888_C__888_888_C;
	String rule_999_888_C__999_888_C;
	String rule_2_888_C__2_888_C;

	String rule_888_888_D__888_888_D;
	String rule_999_888_D__999_888_D;
	String rule_2_888_D__2_888_D;

	String rule_888_999_C__888_999_C;
	String rule_999_999_C__999_999_C;
	String rule_2_999_C__2_999_C;

	String rule_888_999_D__888_999_D;
	String rule_999_999_D__999_999_D;
	String rule_2_999_D__2_999_D;

	LocalDate today = new LocalDate();

	LocalDate oneYearAgo = today.minusYears(1);

	LocalDate oneYearAgoMinus1Day = today.minusYears(1).plusDays(1);

	LocalDate twoYearAgoMinus1Day = today.minusYears(2).plusDays(1);

	LocalDate twoYearAgo = today.minusYears(2);

	LocalDate threeYearAgo = today.minusYears(3);

	LocalDate threeYearAgoMinus1Day = today.minusYears(3).plusDays(1);

	LocalDate fourYearAgo = today.minusYears(4);

	LocalDate tenYearAgo = today.minusYears(10);

	private String adminUnit;
	private String anotherAdminUnitWithoutFolders;

	String dossier888AvecLocalDateFermeture; //#1
	String dossier999AvecLocalDateFermeture; //#2
	String dossierADureeFixeAvecLocalDateFermeture; //#3

	String dossier888SansLocalDateFermeture; //#4
	String dossier999SansLocalDateFermeture; //#5
	String dossierADureeFixeSansLocalDateFermeture; //#6

	String dossier888_2_D_Principal; //#7
	String dossier999_2_D_Principal; //#8
	String dossier2_1_D_Principal; //#9
	String dossier2_1_D_Principal_DECLASSABLE_DEPUIS_1_AN; //#10

	String dossier888_2_D_Principal_NON_DECLASSABLE; //#11
	String dossier999_2_D_Principal_NON_DECLASSABLE; //#12
	String dossier2_1_D_Principal_NON_DECLASSABLE; //#13

	String dossier888_0_C_Principal; //#14
	String dossier999_0_C_Principal; //#15
	String dossier2_0_C_Principal; //#16
	String dossier2_0_C_Principal_DECLASSABLE_DEPUIS_1_AN; //#17

	String dossier888_0_C_Principal_NON_DECLASSABLE; //#18
	String dossier999_0_C_Principal_NON_DECLASSABLE; //#19
	String dossier2_0_C_Principal_NON_DECLASSABLE; //#20

	String dossier888_0_D_Principal; //#21
	String dossier999_0_D_Principal; //#22
	String dossier2_0_D_Principal; //#23
	String dossier2_0_D_Principal_DECLASSABLE_DEPUIS_1_AN; //#24

	String dossier888_0_D_Principal_NON_DECLASSABLE; //#25
	String dossier999_0_D_Principal_NON_DECLASSABLE; //#26
	String dossier2_0_D_Principal_NON_DECLASSABLE; //#27

	String dossier888_888_C_Principal; //#28
	String dossier999_888_C_Principal; //#29
	String dossier2_888_C_Principal; //#30
	String dossier2_888_C_Principal_DECLASSABLE_DEPUIS_1_AN; //#31

	String dossier888_888_C_Principal_NON_VERSABLE; //#32
	String dossier999_888_C_Principal_NON_VERSABLE; //#33
	String dossier2_888_C_Principal_NON_VERSABLE; //#34

	String dossier888_888_D_Principal; //#35
	String dossier999_888_D_Principal; //#36
	String dossier2_888_D_Principal; //#37
	String dossier2_888_D_Principal_DECLASSABLE_DEPUIS_1_AN; //#38

	String dossier888_888_D_Principal_NON_DESTRUCTIBLE; //#39
	String dossier999_888_D_Principal_NON_DESTRUCTIBLE; //#40
	String dossier2_888_D_Principal_NON_DESTRUCTIBLE; //#41

	String dossier888_2_D_Secondaire; //#42
	String dossier999_2_D_Secondaire; //#43
	String dossier2_1_D_Secondaire; //#44
	String dossier2_1_D_Secondaire_DECLASSABLE_DEPUIS_1_AN; //#45

	String dossier888_2_D_Secondaire_NON_DECLASSABLE; //#46
	String dossier999_2_D_Secondaire_NON_DECLASSABLE; //#47
	String dossier2_1_D_Secondaire_NON_DECLASSABLE; //#48

	String dossier888_0_C_Secondaire; //#49
	String dossier999_0_C_Secondaire; //#50
	String dossier2_0_C_Secondaire; //#51
	String dossier2_0_C_Secondaire_DECLASSABLE_DEPUIS_1_AN; //#52

	String dossier888_0_C_Secondaire_NON_DECLASSABLE; //#53
	String dossier999_0_C_Secondaire_NON_DECLASSABLE; //#54
	String dossier2_0_C_Secondaire_NON_DECLASSABLE; //#55

	String dossier888_0_D_Secondaire; //#56
	String dossier999_0_D_Secondaire; //#57
	String dossier2_0_D_Secondaire; //#58
	String dossier2_0_D_Secondaire_DECLASSABLE_DEPUIS_1_AN; //#59

	String dossier888_0_D_Secondaire_NON_DECLASSABLE; //#60
	String dossier999_0_D_Secondaire_NON_DECLASSABLE; //#61
	String dossier2_0_D_Secondaire_NON_DECLASSABLE; //#62

	String dossierSemiActif_2_C_Principal; //#63
	String dossierSemiActif_2_C_Principal_NON_DECLASSABLE; //#64
	String dossierSemiActif_888_C_Principal; //#65
	String dossierSemiActif_888_C_Principal_NON_DECLASSABLE; //#66
	String dossierSemiActif_2_D_Principal; //#67
	String dossierSemiActif_2_D_Principal_NON_DECLASSABLE; //#68
	String dossierSemiActif_888_D_Principal; //#69
	String dossierSemiActif_888_D_Principal_NON_DECLASSABLE; //#70

	private RMTestRecords records;

	private RMSchemasRecordsServices schemas;

	private Transaction transaction;

	DecommissioningSearchConditionFactory factory;

	@Before
	public void beforeTest() {

		givenTimeIs(today);
		givenCollection(zeCollection).withConstellioRMModule();

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		factory = new DecommissioningSearchConditionFactory(zeCollection, getModelLayerFactory());
		records = new RMTestRecords(zeCollection);

		transaction = new Transaction();
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, false);

		adminUnit = records.unitId_10;
		anotherAdminUnitWithoutFolders = records.unitId_12;

		rule_888_2_C__888_2_C = newDelai(888, 2, DisposalType.DEPOSIT);
		rule_999_2_C__999_2_C = newDelai(999, 2, DisposalType.DEPOSIT);
		rule_2_1_C__2_1_C = newDelai(2, 1, DisposalType.DEPOSIT);

		rule_888_2_D__888_2_D = newDelai(888, 2, DisposalType.DESTRUCTION);
		rule_999_2_D__999_2_D = newDelai(999, 2, DisposalType.DESTRUCTION);
		rule_2_1_D__2_1_D = newDelai(2, 1, DisposalType.DESTRUCTION);

		rule_888_0_C__888_0_C = newDelai(888, 0, DisposalType.DEPOSIT);
		rule_999_0_C__999_0_C = newDelai(999, 0, DisposalType.DEPOSIT);
		rule_2_0_C__2_0_C = newDelai(2, 0, DisposalType.DEPOSIT);

		rule_888_0_D__888_0_D = newDelai(888, 0, DisposalType.DESTRUCTION);
		rule_999_0_D__999_0_D = newDelai(999, 0, DisposalType.DESTRUCTION);
		rule_2_0_D__2_0_D = newDelai(2, 0, DisposalType.DESTRUCTION);

		rule_888_888_C__888_888_C = newDelai(888, 888, DisposalType.DEPOSIT);
		rule_999_888_C__999_888_C = newDelai(999, 888, DisposalType.DEPOSIT);
		rule_2_888_C__2_888_C = newDelai(2, 888, DisposalType.DEPOSIT);

		rule_888_888_D__888_888_D = newDelai(888, 888, DisposalType.DESTRUCTION);
		rule_999_888_D__999_888_D = newDelai(999, 888, DisposalType.DESTRUCTION);
		rule_2_888_D__2_888_D = newDelai(2, 888, DisposalType.DESTRUCTION);

		dossier888AvecLocalDateFermeture = newFolder(oneYearAgo, today, null, rule_888_2_C__888_2_C, true, adminUnit);
		dossier999AvecLocalDateFermeture = newFolder(oneYearAgo, today, null, rule_999_2_C__999_2_C, true, adminUnit);
		dossierADureeFixeAvecLocalDateFermeture = newFolder(twoYearAgo, today, null, rule_2_1_C__2_1_C, true,
				adminUnit);

		dossier888SansLocalDateFermeture = newFolder(oneYearAgo, null, null, rule_888_2_C__888_2_C, true, adminUnit);
		dossier999SansLocalDateFermeture = newFolder(oneYearAgo, null, null, rule_999_2_C__999_2_C, true, adminUnit);
		dossierADureeFixeSansLocalDateFermeture = newFolder(twoYearAgo, null, null, rule_2_1_C__2_1_C, true, adminUnit);

		dossier888_2_D_Principal = newFolder(oneYearAgo, today, null, rule_888_2_D__888_2_D, true, adminUnit);
		dossier999_2_D_Principal = newFolder(oneYearAgo, today, null, rule_999_2_D__999_2_D, true, adminUnit);
		dossier2_1_D_Principal = newFolder(twoYearAgo, today, null, rule_2_1_D__2_1_D, true, adminUnit);
		dossier2_1_D_Principal_DECLASSABLE_DEPUIS_1_AN = newFolder(threeYearAgo, today, null, rule_2_1_D__2_1_D, true,
				adminUnit);

		dossier888_2_D_Principal_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_888_2_D__888_2_D, true, adminUnit);
		dossier999_2_D_Principal_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_999_2_D__999_2_D, true, adminUnit);
		dossier2_1_D_Principal_NON_DECLASSABLE = newFolder(twoYearAgoMinus1Day, today, null, rule_2_1_D__2_1_D,
				true, adminUnit);

		dossier888_0_C_Principal = newFolder(oneYearAgo, today, null, rule_888_0_C__888_0_C, true, adminUnit);
		dossier999_0_C_Principal = newFolder(oneYearAgo, today, null, rule_999_0_C__999_0_C, true, adminUnit);
		dossier2_0_C_Principal = newFolder(twoYearAgo, today, null, rule_2_0_C__2_0_C, true, adminUnit);
		dossier2_0_C_Principal_DECLASSABLE_DEPUIS_1_AN = newFolder(threeYearAgo, today, null, rule_2_0_C__2_0_C, true,
				adminUnit);

		dossier888_0_C_Principal_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_888_0_C__888_0_C, true, adminUnit);
		dossier999_0_C_Principal_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_999_0_C__999_0_C, true, adminUnit);
		dossier2_0_C_Principal_NON_DECLASSABLE = newFolder(twoYearAgoMinus1Day, today, null, rule_2_0_C__2_0_C,
				true, adminUnit);

		dossier888_0_D_Principal = newFolder(oneYearAgo, today, null, rule_888_0_D__888_0_D, true, adminUnit);
		dossier999_0_D_Principal = newFolder(oneYearAgo, today, null, rule_999_0_D__999_0_D, true, adminUnit);
		dossier2_0_D_Principal = newFolder(twoYearAgo, today, null, rule_2_0_D__2_0_D, true, adminUnit);
		dossier2_0_D_Principal_DECLASSABLE_DEPUIS_1_AN = newFolder(threeYearAgo, today, null, rule_2_0_D__2_0_D, true,
				adminUnit);

		dossier888_0_D_Principal_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_888_0_D__888_0_D, true, adminUnit);
		dossier999_0_D_Principal_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_999_0_D__999_0_D, true, adminUnit);
		dossier2_0_D_Principal_NON_DECLASSABLE = newFolder(twoYearAgoMinus1Day, today, null, rule_2_0_D__2_0_D,
				true, adminUnit);

		dossier888_888_C_Principal = newFolder(twoYearAgo, today, null, rule_888_888_C__888_888_C, true, adminUnit);
		dossier999_888_C_Principal = newFolder(twoYearAgo, today, null, rule_999_888_C__999_888_C, true, adminUnit);
		dossier2_888_C_Principal = newFolder(threeYearAgo, today, null, rule_2_888_C__2_888_C, true, adminUnit);
		dossier2_888_C_Principal_DECLASSABLE_DEPUIS_1_AN = newFolder(fourYearAgo, today, null,
				rule_2_888_C__2_888_C, true, adminUnit);

		dossier888_888_C_Principal_NON_VERSABLE = newFolder(twoYearAgoMinus1Day, today, null,
				rule_888_888_C__888_888_C, true, adminUnit);
		dossier999_888_C_Principal_NON_VERSABLE = newFolder(twoYearAgoMinus1Day, today, null,
				rule_999_888_C__999_888_C, true, adminUnit);
		dossier2_888_C_Principal_NON_VERSABLE = newFolder(threeYearAgoMinus1Day, today, null,
				rule_2_888_C__2_888_C, true, adminUnit);

		dossier888_888_D_Principal = newFolder(twoYearAgo, today, null, rule_888_888_D__888_888_D, true, adminUnit);
		dossier999_888_D_Principal = newFolder(twoYearAgo, today, null, rule_999_888_D__999_888_D, true, adminUnit);
		dossier2_888_D_Principal = newFolder(threeYearAgo, today, null, rule_2_888_D__2_888_D, true, adminUnit);
		dossier2_888_D_Principal_DECLASSABLE_DEPUIS_1_AN = newFolder(fourYearAgo, today, null,
				rule_2_888_D__2_888_D, true, adminUnit);

		dossier888_888_D_Principal_NON_DESTRUCTIBLE = newFolder(twoYearAgoMinus1Day, today, null,
				rule_888_888_D__888_888_D, true, adminUnit);
		dossier999_888_D_Principal_NON_DESTRUCTIBLE = newFolder(twoYearAgoMinus1Day, today, null,
				rule_999_888_D__999_888_D, true, adminUnit);
		dossier2_888_D_Principal_NON_DESTRUCTIBLE = newFolder(threeYearAgoMinus1Day, today, null,
				rule_2_888_D__2_888_D, true, adminUnit);

		dossier888_2_D_Secondaire = newFolder(oneYearAgo, today, null, rule_888_2_D__888_2_D, false, adminUnit);
		dossier999_2_D_Secondaire = newFolder(oneYearAgo, today, null, rule_999_2_D__999_2_D, false, adminUnit);
		dossier2_1_D_Secondaire = newFolder(twoYearAgo, today, null, rule_2_1_D__2_1_D, false, adminUnit);
		dossier2_1_D_Secondaire_DECLASSABLE_DEPUIS_1_AN = newFolder(threeYearAgo, today, null, rule_2_1_D__2_1_D,
				false, adminUnit);

		dossier888_2_D_Secondaire_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_888_2_D__888_2_D, false, adminUnit);
		dossier999_2_D_Secondaire_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_999_2_D__999_2_D, false, adminUnit);
		dossier2_1_D_Secondaire_NON_DECLASSABLE = newFolder(twoYearAgoMinus1Day, today, null, rule_2_1_D__2_1_D,
				false, adminUnit);

		dossier888_0_C_Secondaire = newFolder(oneYearAgo, today, null, rule_888_0_C__888_0_C, true, adminUnit);
		dossier999_0_C_Secondaire = newFolder(oneYearAgo, today, null, rule_999_0_C__999_0_C, true, adminUnit);
		dossier2_0_C_Secondaire = newFolder(twoYearAgo, today, null, rule_2_0_C__2_0_C, true, adminUnit);
		dossier2_0_C_Secondaire_DECLASSABLE_DEPUIS_1_AN = newFolder(threeYearAgo, today, null, rule_2_0_C__2_0_C,
				true, adminUnit);

		dossier888_0_C_Secondaire_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_888_0_C__888_0_C, false, adminUnit);
		dossier999_0_C_Secondaire_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_999_0_C__999_0_C, false, adminUnit);
		dossier2_0_C_Secondaire_NON_DECLASSABLE = newFolder(twoYearAgoMinus1Day, today, null, rule_2_0_C__2_0_C,
				false, adminUnit);

		dossier888_0_D_Secondaire = newFolder(oneYearAgo, today, null, rule_888_0_D__888_0_D, false, adminUnit);
		dossier999_0_D_Secondaire = newFolder(oneYearAgo, today, null, rule_999_0_D__999_0_D, false, adminUnit);
		dossier2_0_D_Secondaire = newFolder(twoYearAgo, today, null, rule_2_0_D__2_0_D, false, adminUnit);
		dossier2_0_D_Secondaire_DECLASSABLE_DEPUIS_1_AN = newFolder(threeYearAgo, today, null, rule_2_0_D__2_0_D,
				false, adminUnit);

		dossier888_0_D_Secondaire_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_888_0_D__888_0_D, false, adminUnit);
		dossier999_0_D_Secondaire_NON_DECLASSABLE = newFolder(oneYearAgoMinus1Day, today, null,
				rule_999_0_D__999_0_D, false, adminUnit);
		dossier2_0_D_Secondaire_NON_DECLASSABLE = newFolder(twoYearAgoMinus1Day, today, null, rule_2_0_D__2_0_D,
				false, adminUnit);

		dossierSemiActif_2_C_Principal = newFolder(tenYearAgo, today, twoYearAgo, rule_888_2_C__888_2_C,
				true, adminUnit);
		dossierSemiActif_2_C_Principal_NON_DECLASSABLE = newFolder(tenYearAgo, today,
				twoYearAgoMinus1Day, rule_888_2_C__888_2_C, true, adminUnit);
		dossierSemiActif_888_C_Principal = newFolder(tenYearAgo, today, oneYearAgo,
				rule_888_888_C__888_888_C, true, adminUnit);
		dossierSemiActif_888_C_Principal_NON_DECLASSABLE = newFolder(tenYearAgo, today,
				oneYearAgoMinus1Day, rule_888_888_C__888_888_C, true, adminUnit);
		dossierSemiActif_2_D_Principal = newFolder(tenYearAgo, today, twoYearAgo, rule_888_2_D__888_2_D,
				true, adminUnit);
		dossierSemiActif_2_D_Principal_NON_DECLASSABLE = newFolder(tenYearAgo, today,
				twoYearAgoMinus1Day, rule_888_2_D__888_2_D, true, adminUnit);
		dossierSemiActif_888_D_Principal = newFolder(tenYearAgo, today, oneYearAgo,
				rule_888_888_D__888_888_D, true, adminUnit);
		dossierSemiActif_888_D_Principal_NON_DECLASSABLE = newFolder(tenYearAgo, today,
				oneYearAgoMinus1Day, rule_888_888_D__888_888_D, true, adminUnit);

	}

	private String newDelai(int actif, int semiActif, DisposalType disposalType) {
		String principal = actif + "-" + semiActif + "-" + disposalType.getCode();

		return newDelai(principal, principal);
	}

	private static int noDelai = 1;

	private String newDelai(String principalCopyRule, String secondaryCopyRule) {

		RetentionRule delai = schemas.newRetentionRule();

		delai.setApproved(true);
		delai.setCode("Delai #" + noDelai++);
		delai.setCode(delai.getCode());
		delai.setTitle(delai.getCode());
		delai.setResponsibleAdministrativeUnits(true);
		delai.setCopyRetentionRules(Arrays.asList(CopyRetentionRule.newPrincipal(new ArrayList<String>(), principalCopyRule)));
		delai.setCopyRetentionRules(Arrays.asList(CopyRetentionRule.newSecondary(new ArrayList<String>(), secondaryCopyRule)));

		transaction.add(delai);

		return delai.getId();
	}

	//@Test
	public void testListesEnFonctionCriteres() {
		List<Folder> dossiers;

		//		dossiers = rechercher(factory.withoutClosingDateAndWithFixedPeriod(adminUnit));
		//		assertThat(recordIds(dossiers)).containsOnly(dossierADureeFixeSansLocalDateFermeture);
		//
		//		dossiers = rechercher(factory.withoutClosingDateAndWithFixedPeriod(anotherAdminUnitWithoutFolders));
		//		assertThat(recordIds(dossiers)).isEmpty();
		//
		//		dossiers = rechercher(factory.withoutClosingDateAndWith888Period(adminUnit));
		//		assertEquals(Arrays.asList(dossier888SansLocalDateFermeture), recordIds(dossiers));
		//		assertThat(recordIds(dossiers)).containsOnly(dossierADureeFixeSansLocalDateFermeture);
		//
		//		dossiers = rechercher(factory.withoutClosingDateAndWith888Period(anotherAdminUnitWithoutFolders));
		//		assertThat(recordIds(dossiers)).isEmpty();
		//
		//		//Ajouter 28 à 41
		//		dossiers = rechercher(
		//				factory.activeToTransferToSemiActive(adminUnit));
		//		assertThat(recordIds(dossiers)).containsOnly(dossier888AvecLocalDateFermeture, dossier999AvecLocalDateFermeture,
		//				dossierADureeFixeAvecLocalDateFermeture, dossier888SansLocalDateFermeture, dossier999SansLocalDateFermeture,
		//				dossierADureeFixeSansLocalDateFermeture, dossier888_2_D_Principal, dossier999_2_D_Principal,
		//				dossier2_1_D_Principal, dossier2_1_D_Principal_DECLASSABLE_DEPUIS_1_AN, dossier888_2_D_Secondaire,
		//				dossier999_2_D_Secondaire, dossier2_1_D_Secondaire, dossier2_1_D_Secondaire_DECLASSABLE_DEPUIS_1_AN,
		//				dossier888_888_C_Principal, dossier999_888_C_Principal, dossier2_888_C_Principal,
		//				dossier2_888_C_Principal_DECLASSABLE_DEPUIS_1_AN, dossier888_888_C_Principal_NON_VERSABLE,
		//				dossier999_888_C_Principal_NON_VERSABLE, dossier2_888_C_Principal_NON_VERSABLE, dossier888_888_D_Principal,
		//				dossier999_888_D_Principal, dossier2_888_D_Principal, dossier2_888_D_Principal_DECLASSABLE_DEPUIS_1_AN,
		//				dossier888_888_D_Principal_NON_DESTRUCTIBLE, dossier999_888_D_Principal_NON_DESTRUCTIBLE,
		//				dossier2_888_D_Principal_NON_DESTRUCTIBLE);
		//
		//		dossiers = rechercher(factory.activeToTransferToSemiActive(anotherAdminUnitWithoutFolders));
		//		assertThat(recordIds(dossiers)).isEmpty();
		//
		//		//Ajouter 35 à 38
		//		dossiers = rechercher(factory.activeToDestroy(adminUnit));
		//		assertThat(recordIds(dossiers))
		//				.containsOnly(dossier2_1_D_Principal_DECLASSABLE_DEPUIS_1_AN, dossier888_0_D_Principal, dossier999_0_D_Principal,
		//						dossier2_0_D_Principal, dossier2_0_D_Principal_DECLASSABLE_DEPUIS_1_AN,
		//						dossier2_1_D_Secondaire_DECLASSABLE_DEPUIS_1_AN, dossier888_0_D_Secondaire, dossier999_0_D_Secondaire,
		//						dossier2_0_D_Secondaire, dossier2_0_D_Secondaire_DECLASSABLE_DEPUIS_1_AN, dossier888_888_D_Principal,
		//						dossier999_888_D_Principal, dossier2_888_D_Principal, dossier2_888_D_Principal_DECLASSABLE_DEPUIS_1_AN);
		//
		//		dossiers = rechercher(
		//				factory.activeToDestroy(anotherAdminUnitWithoutFolders));
		//		TestCase.assertEquals(Arrays.asList(), recordIds(dossiers));
		//
		//		//28 à 31
		//		dossiers = rechercher(factory.activeToDeposit(adminUnit));
		//		assertThat(recordIds(dossiers)).containsOnly(dossier888_0_C_Principal, dossier999_0_C_Principal, dossier2_0_C_Principal,
		//				dossier2_0_C_Principal_DECLASSABLE_DEPUIS_1_AN, dossier888_0_C_Secondaire, dossier999_0_C_Secondaire,
		//				dossier2_0_C_Secondaire, dossier2_0_C_Secondaire_DECLASSABLE_DEPUIS_1_AN, dossier888_888_C_Principal,
		//				dossier999_888_C_Principal, dossier2_888_C_Principal, dossier2_888_C_Principal_DECLASSABLE_DEPUIS_1_AN);
		//
		//		dossiers = rechercher(
		//				factory.activeToDeposit(anotherAdminUnitWithoutFolders));
		//		assertThat(recordIds(dossiers)).isEmpty();
		//
		//		dossiers = rechercher(factory.semiActiveToDeposit(adminUnit));
		//		assertThat(recordIds(dossiers)).containsOnly(dossierSemiActif_2_C_Principal, dossierSemiActif_888_C_Principal);
		//
		//		dossiers = rechercher(factory.semiActiveToDeposit(anotherAdminUnitWithoutFolders));
		//		assertThat(recordIds(dossiers)).isEmpty();
		//
		//		dossiers = rechercher(factory.semiActiveToDestroy(adminUnit));
		//		assertThat(recordIds(dossiers)).containsOnly(dossierSemiActif_2_D_Principal, dossierSemiActif_888_D_Principal);
		//
		//		dossiers = rechercher(factory.semiActiveToDestroy(anotherAdminUnitWithoutFolders));
		//		assertThat(recordIds(dossiers)).isEmpty();

		//Liste des dossiers à durée fixe sans LocalDate de fermeture : 6
		//Liste des dossiers visés par un code 888 sans LocalDate de fermeture : 4
		//Liste des dossiers visés par un code 999 sans LocalDate de fermeture : 5
		//Liste des dossiers actifs à transférer au semi-actif : 1,2,3,4,5,6,7,8,9
		//Liste des dossiers actifs avec durée fixe à détruire : 19,20,21
		//Liste des dossiers actifs avec durée fixe à verser : 13,14,15

		//		dossiers = rechercher(factory.activeToDestroy(null, adminUnit));
		//		TestCase.assertEquals(Arrays.asList(dossier999SansLocalDateFermeture), recordIds(dossiers));
		//
		//		dossiers = rechercher(factory.activeToDestroy(null, anotherAdminUnitWithoutFolders));
		//		TestCase.assertEquals(Arrays.asList(), recordIds(dossiers));
		//
		//		dossiers = rechercher(factory.activeToDestroy(rechercheSansResultatsPossibles, adminUnit));
		//		TestCase.assertEquals(Arrays.asList(), recordIds(dossiers));

		//TODO Test avec prefixe premier critère ) ou suffixe dernier critère )
	}

	private void assertEquals(List<String> expected, List<String> was) {
		Set<String> expectedSet = new TreeSet<String>();
		expectedSet.addAll(expected);
		Set<String> wasSet = new TreeSet<String>();
		wasSet.addAll(was);

		List<String> expectedOrderedList = new ArrayList<String>(expectedSet);
		Collections.sort(expectedOrderedList);

		List<String> wasOrderedList = new ArrayList<String>(wasSet);
		Collections.sort(wasOrderedList);

		TestCase.assertEquals(expectedOrderedList, wasOrderedList);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Folder> rechercher(LogicalSearchCondition condition) {
		return schemas.wrapFolders(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(condition)));
	}

	private List<String> recordIds(List<Folder> dossiers) {
		List<String> titres = new ArrayList<String>();
		for (Folder dossier : dossiers) {
			titres.add(dossier.getId());
		}

		return titres;
	}

	private static int noDossier = 1;

	private String newFolder(LocalDate ouverture, LocalDate fermeture, LocalDate traitement, String numeroDelai,
			boolean statutExemplairePrincipal, String administrativeUnitId) {

		Folder ficheDossier = schemas.newFolder();
		String noDossierStr = "0" + noDossier++;
		ficheDossier.setTitle("#" + noDossierStr.substring(noDossierStr.length() - 2));
		ficheDossier.setOpenDate(ouverture);
		ficheDossier.setCloseDateEntered(fermeture);
		ficheDossier.setActualTransferDate(traitement);
		ficheDossier.setCopyStatusEntered(statutExemplairePrincipal ? CopyType.PRINCIPAL : CopyType.SECONDARY);
		ficheDossier.setFilingSpaceEntered(administrativeUnitId);
		if (records.unitId_10.equals(administrativeUnitId)) {
			ficheDossier.setFilingSpaceEntered(records.filingId_A);
		} else {
			ficheDossier.setFilingSpaceEntered(records.filingId_B);
		}
		ficheDossier.setRetentionRuleEntered(numeroDelai);
		ficheDossier.setCategoryEntered(records.categoryId_ZE42);

		transaction.add(ficheDossier);

		return ficheDossier.getId();
	}
}
