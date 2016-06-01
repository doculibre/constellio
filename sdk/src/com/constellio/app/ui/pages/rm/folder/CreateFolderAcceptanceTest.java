package com.constellio.app.ui.pages.rm.folder;

import static java.lang.Thread.sleep;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class CreateFolderAcceptanceTest extends ConstellioTest {

	private boolean withAllResponsibleAdminUnitFlag = true;

	private boolean withAdministrativeUnits = false;

	String lastSavedFolderId;
	String classificationFinder;
	String administrativeUnitFinder;
	//	String filingSpaceFinderA;
	//	String filingSpaceFinderB;
	//	String filingSpaceFinderC;
	//String filingSpaceFinder;
	String retentionRule;
	String collection;

	RecordFormWebElement zeForm;
	RecordServices recordServices;
	ConstellioWebDriver driver;

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RetentionRule ruleCustom;
	RetentionRule backupRule;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus().withEvents(),
				withCollection("LaCollectionDeRida").withConstellioRMModule().withAllTestUsers().withRMTest(records2)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		getConfigurationManager().setValue(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED, false);
		//		filingSpaceFinderA = "A";
		//		filingSpaceFinderB = "B";
		//		filingSpaceFinderC = "C";
		//filingSpaceFinder = filingSpaceFinderB;
		retentionRule = "1";

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		getConfigurationManager().setValue(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED, false);

		recordServices = getModelLayerFactory().newRecordServices();

		backupRule = records.getRule1();
	}

	@Test
	public void testsInZeCollection()
			throws Exception {
		collection = zeCollection;
		classificationFinder = "X100";
		administrativeUnitFinder = "10A";

		givenUserAddFolderWhenAllFieldsAreCompleteAndClickSaveThenFolderIsCreated();
		givenUserCanAccessOneFilingSpaceWhenCreateFolderThenCantChooseAnotherFilingSpace();
		whenCreateFolderThenTitleCategoryFilingSpaceAndAdministrativeUnitAreRequired();

	}

	@Test
	public void testsInCollectionDeRida()
			throws Exception {
		collection = "LaCollectionDeRida";
		classificationFinder = "23 ";
		administrativeUnitFinder = "A";

		givenUserAddFolderWhenAllFieldsAreCompleteAndClickSaveThenFolderIsCreated();
		givenUserCanAccessOneFilingSpaceWhenCreateFolderThenCantChooseAnotherFilingSpace();
		whenCreateFolderThenTitleCategoryFilingSpaceAndAdministrativeUnitAreRequired();
	}

	@Test
	public void whenModifyingCategoryThenRetentionRuleAndCopyTypeVisibilityIsAdjusted()
			throws Exception {
		collection = zeCollection;

		navigateToAddFolderFormLoggedAs(gandalf, collection);
		assertThat(zeForm.isVisible("folder_default_categoryEntered")).isTrue();
		assertThat(zeForm.isVisible("folder_default_retentionRuleEntered")).isFalse();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.getDropDown("folder_default_categoryEntered").typeAndSelectFirst("X13");
		sleep(1000);
		assertThat(zeForm.isVisible("folder_default_retentionRuleEntered")).isTrue();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText("2");
		sleep(1000);
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isTrue();

		zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText("1");
		sleep(1000);
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.getDropDown("folder_default_categoryEntered").clear().typeAndSelectFirst("X100");
		sleep(1000);
		assertThat(zeForm.isVisible("folder_default_retentionRuleEntered")).isFalse();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();
	}

	@Test
	public void givenFlagCopyRuleTypeAlwaysModifiableWhenCreatePrincipalFolderWithAdministrativeUnitAndChangeForPrincipalRuleThenCopyFieldInvisibleAndSecondaryCopyInFolder()
			throws Exception {

		collection = zeCollection;
		classificationFinder = "X13";
		administrativeUnitFinder = "10A";
		retentionRule = "1";

		getConfigurationManager().setValue(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);

		navigateToAddFolderFormLoggedAs(admin, collection);

		completeFormAndSave();

		assertThat(driver.findElement(By.id("display-value-folder_default_copyStatus")).getText()).isEqualTo("Principal");

		clickModifyButton();

		modifyRule1RemovingPrincipalCopyRule(withAdministrativeUnits);

		modifyLastFolderRule();

		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();
	}

	private void clickModifyButton() {

		driver.navigateTo().url("editFolder/id=" + lastSavedFolderId);

		//		try {
		//			Thread.sleep(300);
		//		} catch (InterruptedException e) {
		//			throw new RuntimeException(e);
		//		}
		//		driver.find("v-slot-edit-button").printHierarchy();
		//		driver.find("v-slot-edit-button").();
		//		try {
		//			Thread.sleep(300);
		//		} catch (InterruptedException e) {
		//			throw new RuntimeException(e);
		//		}
	}

	@Test
	public void givenFolderPrincipalWhenChangeRuleThenCopyFieldInvisibleAndSecondaryCopy()
			throws Exception {

		collection = zeCollection;
		navigateToAddFolderFormLoggedAs(admin, collection);
		retentionRule = "1";
		classificationFinder = "X13";
		administrativeUnitFinder = "12B";

		completeFormAndSave();

		assertThat(driver.findElement(By.id("display-value-folder_default_copyStatus")).getText()).isEqualTo("Principal");

		clickModifyButton();

		modifyRule1RemovingPrincipalCopyRule(withAdministrativeUnits);

		administrativeUnitFinder = "10";
		modifyLastFolderRule();

		clickModifyButton();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

	}

	//TODO Maxime - Ne passe pas sur PhantomJS @Test
	public void givenFolderWithRuleWithResponsibleUnitWhenChangeRuleThenCopyFieldInvisibleAndSecondaryCopyInFolder()
			throws Exception {

		collection = zeCollection;
		classificationFinder = "X13";
		administrativeUnitFinder = "10A";
		navigateToAddFolderFormLoggedAs(admin, collection);
		retentionRule = "3";

		completeFormAndSave();

		assertThat(driver.findElement(By.id("display-value-folder_default_copyStatus")).getText()).isEqualTo("Principal");

		clickModifyButton();

		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isTrue();
		modifyRule1RemovingPrincipalCopyRule(withAllResponsibleAdminUnitFlag);

		clickModifyButton();

		retentionRule = "1";
		administrativeUnitFinder = "12";
		modifyLastFolderRule();

		clickModifyButton();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

	}

	@Test
	public void givenRuleWithoutPrincipalAndResponsibleListThenCopyFieldInvisibleAndFolderIsCreatedWithSecondaryCopy()
			throws Exception {

		collection = zeCollection;
		classificationFinder = "X13";
		administrativeUnitFinder = "12B";
		modifyRule1RemovingPrincipalCopyRule(withAdministrativeUnits);

		navigateToAddFolderFormLoggedAs(admin, collection);

		completeFormWithoutExemplary();

	}

	@Test
	public void givenCopyRuleTypeAlwaysModifiableWhenRuleWithoutPrincipalAndFlagResponsibleThenCopyFieldInvisibleAndFolderIsCreatedWithSecondaryCopy()
			throws Exception {

		collection = zeCollection;
		classificationFinder = "X13";
		administrativeUnitFinder = "12B";

		getConfigurationManager().setValue(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);

		modifyRule1RemovingPrincipalCopyRule(withAllResponsibleAdminUnitFlag);

		navigateToAddFolderFormLoggedAs(admin, collection);

		completeFormWithoutExemplary();

	}

	@Test
	public void givenRuleWithoutPrincipalAndFlagResponsibleThenFieldInvisibleAndFolderIsCreatedWithSecondaryCopy()
			throws Exception {
		collection = zeCollection;
		classificationFinder = "X13";
		administrativeUnitFinder = "12B";

		modifyRule1RemovingPrincipalCopyRule(withAllResponsibleAdminUnitFlag);

		navigateToAddFolderFormLoggedAs(admin, collection);

		completeFormWithoutExemplary();

	}

	@Test
	public void whenAddingFolderThenPreviewReturnDateIsInvisible()
			throws Exception {
		collection = zeCollection;
		classificationFinder = "X13";
		administrativeUnitFinder = "12B";

		modifyRule1RemovingPrincipalCopyRule(withAllResponsibleAdminUnitFlag);

		navigateToAddFolderFormLoggedAs(admin, collection);

		assertThat(zeForm.isVisible("folder_default_previewReturnDate")).isFalse();
	}

	@Test
	public void whenAddingFolderThenLinearSizeIsInvisible()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		driver.navigateTo().url(RMNavigationConfiguration.ADD_FOLDER);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));

		assertThat(zeForm.isVisible("folder_default_linearSize")).isFalse();
	}

	@Test
	public void whenModifyingFolderWithoutContainerThenLinearSizeInvisible()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		driver.navigateTo().url("editFolder/id=" + records.folder_A04);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));

		assertThat(zeForm.isVisible("folder_default_linearSize")).isFalse();
	}

	@Test
	public void whenModifyingFolderWithContainerThenLinearSizeVisible()
			throws Exception {
		assertThat(records.getFolder_A45().getLinearSize()).isNull();

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		driver.navigateTo().url("editFolder/id=" + records.folder_A45);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));

		assertThat(zeForm.isVisible("folder_default_linearSize")).isTrue();

		zeForm.getTextField("folder_default_linearSize").setValue("42");
		zeForm.clickSaveButtonAndWaitForPageReload();
		assertThat(records.getFolder_A45().getLinearSize()).isEqualTo(42);
	}

	private void modifyRule1RemovingPrincipalCopyRule(boolean isResponsibleUnit)
			throws Exception {

		getConfigurationManager().setValue(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED, false);

		CopyRetentionRule copyRetentionRuleSecondary = records.getRule1().getSecondaryCopy();
		ruleCustom = records.getRule1();

		ruleCustom.setCopyRetentionRules(copyRetentionRuleSecondary);

		if (isResponsibleUnit) {
			ruleCustom.setAdministrativeUnits(emptyList());
		} else {
			ruleCustom.setAdministrativeUnits(backupRule.getAdministrativeUnits());
		}
		ruleCustom.setResponsibleAdministrativeUnits(isResponsibleUnit);

		recordServices.update(ruleCustom);

		assertThat(ruleCustom.isResponsibleAdministrativeUnits()).isEqualTo(isResponsibleUnit);
		assertThat(ruleCustom.getCopyRetentionRules().size()).isEqualTo(1);
	}

	private void completeFormWithoutExemplary() {
		completeRequiredFieldWithRetentionRule();

		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThat(driver.findElement(By.id("display-value-folder_default_copyStatus")).getText()).isEqualTo("Secondaire");
	}

	private void modifyLastFolderRule()
			throws Exception {
		modifyRule1RemovingPrincipalCopyRule(withAllResponsibleAdminUnitFlag);

		modifyRetentionRule();

		assertThat(driver.findElement(By.id("display-value-folder_default_copyStatus")).getText()).isEqualTo("Secondaire");
	}

	private void modifyRetentionRule() {

		zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText(retentionRule);

		zeForm.clickSaveButtonAndWaitForPageReload();
	}

	public void givenUserAddFolderWhenAllFieldsAreCompleteAndClickSaveThenFolderIsCreated()
			throws Exception {
		navigateToAddFolderFormLoggedAs(admin, collection);
		completeFormAndSave();

		assertThat(folderTitle()).isEqualTo("Pokemon");
	}

	public void givenUserCanAccessOneFilingSpaceWhenCreateFolderThenCantChooseAnotherFilingSpace()
			throws Exception {
		navigateToAddFolderFormLoggedAs(charlesFrancoisXavier, collection);
		completeFormAndSave();

		assertThat(folderAdministrativeUnit()).contains("A");
	}

	public void whenCreateFolderThenTitleCategoryFilingSpaceAndAdministrativeUnitAreRequired()
			throws Exception {
		navigateToAddFolderFormLoggedAs(charlesFrancoisXavier, collection);
		completeNoRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThat(driver.getCurrentPage()).contains("addFolder");
	}

	private void navigateToAddFolderFormLoggedAs(String user, String collection) {
		driver = newWebDriver(loggedAsUserInCollection(user, collection));
		driver.navigateTo().url(RMNavigationConfiguration.ADD_FOLDER);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));
	}

	private void completeFormAndSave() {
		completeRequiredField();
		completeNoRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();
		String[] urlParts = driver.getCurrentUrl().split("/");
		lastSavedFolderId = urlParts[urlParts.length - 1];
	}

	public void completeRequiredField() {
		zeForm.getTextField("folder_default_title").setValue("Pokemon");

		zeForm.getLookupField("folder_default_categoryEntered").typeAndSelectFirst(classificationFinder);
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (zeForm.isVisible("folder_default_retentionRuleEntered")) {
			zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText(retentionRule);
		}
		zeForm.getDateField("folder_default_openingDate").setValue(new LocalDate(2015, 2, 21));
		zeForm.getDropDown("folder_default_administrativeUnitEntered").typeAndSelectFirst(administrativeUnitFinder);
		if (zeForm.isVisible("folder_default_copyStatusEntered")) {
			zeForm.getRadioButton("folder_default_copyStatusEntered").toggleContaining("P");
		}
	}

	public void completeRequiredFieldWithRetentionRule() {
		zeForm.getTextField("folder_default_title").setValue("Pokemon");

		zeForm.getLookupField("folder_default_categoryEntered").typeAndSelectFirst(classificationFinder);
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText(ruleCustom.getCode());
		zeForm.getDateField("folder_default_openingDate").setValue(new LocalDate(2015, 2, 21));
		zeForm.getDropDown("folder_default_administrativeUnitEntered").typeAndSelectFirst(administrativeUnitFinder);
	}

	private void completeNoRequiredField() {
		zeForm.getDateField("folder_default_enteredClosingDate").setValue(new LocalDate(2016, 2, 21));
		zeForm.toggleAllCheckbox();
		zeForm.getListAddRemoveTextField("folder_default_keywords").add("pokemon");
		zeForm.getTextField("folder_default_description").setValue("Attrapez les tous");
	}

	private String folderTitle() {
		return driver.findElement(By.id("display-value-folder_default_title")).getText();
	}

	private String folderAdministrativeUnit() {
		return driver.findElement(By.id("display-value-folder_default_administrativeUnit")).getText();
	}

	private SystemConfigurationsManager getConfigurationManager() {
		return getModelLayerFactory().getSystemConfigurationsManager();
	}
}
