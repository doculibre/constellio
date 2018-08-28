package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.modules.rm.model.enums.CopyType.SECONDARY;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DESTRUCTION;
import static com.constellio.app.modules.rm.model.enums.DisposalType.SORT;
import static com.constellio.app.modules.rm.model.enums.RetentionType.FIXED;
import static com.constellio.app.modules.rm.model.enums.RetentionType.OPEN;
import static com.constellio.app.modules.rm.model.enums.RetentionType.UNTIL_REPLACED;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class FolderCopyRuleAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
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

	CopyRetentionRuleBuilder copyBuilder = new CopyRetentionRuleBuilderWithDefinedIds();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
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

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(records.unitId_11b);
		folder.setDescription("Ze description");
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(records.ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setOpenDate(new LocalDate(2014, 11, 4));

		folder = saveAndLoad(folder);

		assertThat(folder.getAdministrativeUnitEntered()).isEqualTo(records.unitId_11b);
		assertThat(folder.getDescription()).isEqualTo("Ze description");
		assertThat(folder.getCategoryEntered()).isEqualTo(records.categoryId_X110);
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(records.ruleId_2);
		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder.getTitle()).isEqualTo("Ze folder");

	}

	@Test
	public void givenFolderAdministrativeUnitIsInTheRetentionRuleAdministrativeUnitsListThenPrincipalCopyTypeAndValidCopyRule()
			throws Exception {
		givenRuleWithAdminUnitsAndCopyRules(principal("2-888-D", PA), secondary("0-999-C", MD));

		Folder folder = saveAndLoad(folderWith(aPrincipalAdminUnit, noEnteredCopyType, PA));

		assertThat(folder.getCopyStatusEntered()).isNull();
		assertThat(folder.getCopyStatus()).isSameAs(PRINCIPAL);
		assertThat(folder.getApplicableCopyRules()).containsOnlyOnce(principal("2-888-D", PA));
		assertThat(folder.getMainCopyRule()).isEqualTo(principal("2-888-D", PA));
		assertThat(folder.getActiveRetentionCode()).isNull();
		assertThat(folder.getSemiActiveRetentionCode()).isEqualTo("888");
		assertThat(folder.getActiveRetentionType()).isEqualTo(FIXED);
		assertThat(folder.getSemiActiveRetentionType()).isEqualTo(OPEN);
		assertThat(folder.getInactiveDisposalType()).isEqualTo(DESTRUCTION);
	}

	@Test
	public void givenFolderAdministrativeUnitIsNotInTheRetentionRuleAdministrativeUnitsListThenSecondaryCopyTypeAndValidCopyRule()
			throws Exception {
		givenRuleWithAdminUnitsAndCopyRules(principal("2-888-D", PA), secondary("0-999-C", MD));

		Folder folder = saveAndLoad(folderWith(aSecondaryAdminUnit, noEnteredCopyType, PA));

		assertThat(folder.getCopyStatusEntered()).isNull();
		assertThat(folder.getCopyStatus()).isSameAs(SECONDARY);
		assertThat(folder.getApplicableCopyRules()).containsOnlyOnce(secondary("0-999-C", MD));
		assertThat(folder.getMainCopyRule()).isEqualTo(secondary("0-999-C", MD));
		assertThat(folder.getActiveRetentionType()).isEqualTo(FIXED);
		assertThat(folder.getSemiActiveRetentionType()).isEqualTo(UNTIL_REPLACED);
		assertThat(folder.getInactiveDisposalType()).isEqualTo(DEPOSIT);
	}

	@Test
	public void whenAFolderHasAnEnteredCopyTypeThenItReplaceDefaultCalculatedValue()
			throws Exception {

		givenRuleWithAdminUnitsAndCopyRules(principal("999-0-T", PA), secondary("1-888-D", MD, MV));

		Folder principalFolderWithEnteredSecondaryCopyStatus = saveAndLoad(folderWith(aPrincipalAdminUnit, SECONDARY, PA));
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getCopyStatusEntered()).isSameAs(SECONDARY);
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getCopyStatus()).isSameAs(SECONDARY);
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getApplicableCopyRules()).containsOnlyOnce(secondary("1-888-D",
				MD, MV));
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getMainCopyRule()).isEqualTo(secondary("1-888-D", MD, MV));
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getActiveRetentionType()).isEqualTo(FIXED);
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getSemiActiveRetentionType()).isEqualTo(OPEN);
		assertThat(principalFolderWithEnteredSecondaryCopyStatus.getInactiveDisposalType()).isEqualTo(DESTRUCTION);

		Folder secondaryFolderWithEnteredPrincipalCopyStatus = saveAndLoad(folderWith(aSecondaryAdminUnit, PRINCIPAL, PA));
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getCopyStatusEntered()).isSameAs(PRINCIPAL);
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getCopyStatus()).isSameAs(PRINCIPAL);
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getApplicableCopyRules()).containsOnlyOnce(
				principal("999-0-T", PA));
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getMainCopyRule()).isEqualTo(principal("999-0-T", PA));
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getActiveRetentionCode()).isEqualTo("999");
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getSemiActiveRetentionCode()).isNull();
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getActiveRetentionType()).isEqualTo(UNTIL_REPLACED);
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getSemiActiveRetentionType()).isEqualTo(FIXED);
		assertThat(secondaryFolderWithEnteredPrincipalCopyStatus.getInactiveDisposalType()).isEqualTo(SORT);
	}

	@Test
	public void whenAFolderHasAnEnteredCopyTypeAndRuleWithResponsibleFlagThenThenTheEnteredCopyTypeIsChoosed()
			throws Exception {

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("7-0-T", PA), secondary("1-12-D", MD, MV));

		Folder secondaryFolder = saveAndLoad(folderWith(aPrincipalAdminUnit, SECONDARY, MD, MV));
		assertThat(secondaryFolder.getCopyStatusEntered()).isSameAs(SECONDARY);
		assertThat(secondaryFolder.getCopyStatus()).isSameAs(SECONDARY);
		assertThat(secondaryFolder.getApplicableCopyRules()).containsOnlyOnce(secondary("1-12-D", MD, MV));
		assertThat(secondaryFolder.getMainCopyRule()).isEqualTo(secondary("1-12-D", MD, MV));
		assertThat(secondaryFolder.getActiveRetentionType()).isEqualTo(FIXED);
		assertThat(secondaryFolder.getSemiActiveRetentionType()).isEqualTo(FIXED);
		assertThat(secondaryFolder.getInactiveDisposalType()).isEqualTo(DESTRUCTION);

		Folder principalFolder = saveAndLoad(folderWith(aPrincipalAdminUnit, PRINCIPAL, PA));
		assertThat(principalFolder.getCopyStatusEntered()).isSameAs(PRINCIPAL);
		assertThat(principalFolder.getCopyStatus()).isSameAs(PRINCIPAL);
		assertThat(principalFolder.getApplicableCopyRules()).containsOnlyOnce(principal("7-0-T", PA));
		assertThat(principalFolder.getMainCopyRule()).isEqualTo(principal("7-0-T", PA));
		assertThat(principalFolder.getActiveRetentionType()).isEqualTo(FIXED);
		assertThat(principalFolder.getSemiActiveRetentionType()).isEqualTo(FIXED);
		assertThat(principalFolder.getInactiveDisposalType()).isEqualTo(SORT);

	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void whenSavingAFolderWithRuleWithResponsibleFlafAndNoEnteredCopyTypeThenValidationError()
			throws Exception {

		givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(principal("7-0-T", PA), secondary("1-12-D", MD, MV));

		saveAndLoad(folderWith(aPrincipalAdminUnit, noEnteredCopyType, PA));
	}

	// -------------------------------------------------------------------------

	private Folder folderWith(String administrativeUnit, CopyType copyType, String... mediumTypes) {

		if (!transaction.getRecords().isEmpty()) {
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			transaction = new Transaction();
		}

		Folder folder = rm.newFolder();
		folder.setAdministrativeUnitEntered(administrativeUnit);
		folder.setCategoryEntered(records.categoryId_X110);
		folder.setTitle("Ze folder");
		folder.setRetentionRuleEntered(zeRule);
		folder.setMediumTypes(mediumTypes);
		folder.setCopyStatusEntered(copyType);
		folder.setOpenDate(new LocalDate(2014, 11, 4));
		return folder;
	}

	private Folder saveAndLoad(Folder folder)
			throws RecordServicesException {
		recordServices.add(folder.getWrappedRecord());
		return rm.getFolder(folder.getId());
	}

	private RetentionRule givenRuleWithResponsibleAdminUnitsFlagAndCopyRules(CopyRetentionRule... rules) {
		return givenRetentionRule(rules).setResponsibleAdministrativeUnits(true);
	}

	private RetentionRule givenRuleWithAdminUnitsAndCopyRules(CopyRetentionRule... rules) {
		return givenRetentionRule(rules).setAdministrativeUnits(Arrays.asList(aPrincipalAdminUnit, anotherPrincipalAdminUnit));
	}

	private RetentionRule givenRetentionRule(CopyRetentionRule... rules) {
		RetentionRule retentionRule = rm.newRetentionRuleWithId(zeRule);

		retentionRule.setCode("Ze rule");
		retentionRule.setTitle("Ze rule");
		retentionRule.setApproved(true);
		retentionRule.setCopyRetentionRules(rules);

		return transaction.add(retentionRule);
	}

	private CopyRetentionRule principal(String status, String... mediumTypes) {
		return copyBuilder.newPrincipal(asList(mediumTypes), status);
	}

	private CopyRetentionRule secondary(String status, String... mediumTypes) {
		return copyBuilder.newSecondary(asList(mediumTypes), status);
	}
}
