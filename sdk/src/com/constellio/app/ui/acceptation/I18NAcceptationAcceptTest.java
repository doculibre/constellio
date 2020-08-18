package com.constellio.app.ui.acceptation;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;
import com.constellio.sdk.dev.tools.CompareI18nKeys;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class I18NAcceptationAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;

	protected List<String> missingKeys = new ArrayList<>();
	protected List<String> duplicatedKeys = new ArrayList<>();
	Locale locale;
	Locale defaultLocale;

	private static List<String> keysWithSameFrenchEnglishValue = new ArrayList<>();

	static {
		keysWithSameFrenchEnglishValue.add("SystemConfigurationGroup.agent");
	}

	@Test
	public void whenUsingI18nThenAllBundlesFound() {
		givenEnglishSystem();

		assertThat(i18n.getDefaultBundle()).extracting("bundleName")
				.containsOnly("baseView", "imports", "managementViews", "model", "schemasManagementViews", "search", "security", "usersAndGroupsManagementViews", "userViews", "webservices", "i18n", "i18n", "audits", "decommissioningViews", "demo", "foldersAndDocuments", "managementViews", "model", "reports", "storageAndContainers", "userViews", "i18n", "model", "views", "workflowBeta", "guide");
	}

	@Test
	public void whenDetectDuplicatedKeyThenReturnTrue() {
		givenEnglishSystem();

		findDuplicateKeys();

		System.out.println("Found " + duplicatedKeys.size() + " duplicated keys :");
		duplicatedKeys.forEach(System.out::println);

		assertThat(duplicatedKeys).isEmpty();
	}

	private void findDuplicateKeys() {
		Set<String> allKeys = new HashSet<>();

		for (Utf8ResourceBundles bundle : i18n.getDefaultBundle()) {

			Enumeration<String> keyEnumeration = bundle.getBundle(Locale.ENGLISH).getKeys();
			while (keyEnumeration.hasMoreElements()) {
				String key = keyEnumeration.nextElement();
				if (allKeys.contains(key)) {
					duplicatedKeys.add(key);
				} else {
					allKeys.add(key);
				}
			}
		}
	}


	@Test
	public void givenEnglishSystemEnsureAllObjectsHasATitle()
			throws Exception {
		givenEnglishSystem();

		findMissingKeys();

		assertThat(missingKeys).isEmpty();
	}

	@Test
	public void givenFrenchSystemEnsureAllObjectsHasATitle()
			throws Exception {
		givenFrenchSystem();

		findMissingKeys();

		assertThat(missingKeys).isEmpty();
	}

	//@Test
	public void whenScanningAppModuleJavaSourcesThenAllI18n() {
		givenFrenchSystem();

		Set<String> keys = new HashSet<>();
		scanRetrievingKeys(keys, new FoldersLocator().getAppProject());

		for (String key : keys) {
			addIfNoValueInMainI18N(key);
		}

		assertThat(missingKeys).isEmpty();
	}

	protected void scanRetrievingKeys(Set<String> keys, File fileOrFolder) {
		if (fileOrFolder.isFile() && fileOrFolder.getName().endsWith(".java")) {
			scanJavaFileRetrievingKeys(keys, fileOrFolder);

		} else if (fileOrFolder.isDirectory()) {
			File[] files = fileOrFolder.listFiles();
			if (files != null) {
				for (File file : files) {
					scanRetrievingKeys(keys, file);
				}
			}
		}

		keys.removeAll(
				asList("documentsCount", "recentFolders", "RMRequestTaskButtonExtension.invalidBorrowDuration", "facetType",
						"Code", "AddEditCollectionPresenter.codeNonAvailable", "connectorType", "BatchProcessingEntryTable.value",
						"AddEditCollectionPresenter.codeChangeForbidden", "DisplayDocumentViewImpl.sign.sign",
						"DisplayFolderView.cannotReturnFolder", "DisplayDocumentWindow.sign.certificate",
						"AddEditContainerView.invalidNumberOfContainer", "DocumentEventSchemaToVOBuilder.modifiedOn",
						"ImportUsersFileViewImpl.errorWith", "SearchView.errorSavingSearch", "Reports.DecommissioningList",
						"DecomAskForValidationWindowButton.error.users", "FirstName",
						"TraversalSchedule.mustHaveValuesInAllFields", "TraversalSchedule.startTimeAfterEndTime",
						"BatchProcessingEntryTable.label", "BatchProcessingEntryTable.default", "TaskFollowerField.dirty",
						"AdvancedSearchView.tooManyClosedParentheses",
						"com.constellio.app.modules.rm.RMConfigs.EndYearValueCalculator", "username",
						"RedémarrageListEventsView.restarting", "OK", "DisplayFolderView.chooseDateToExtends", "Ok",
						"AddEditCollectionPresenter.invalidCode", "elementPerPage", "lastTraversalOn", "addAuthorization",
						"AdvancedSearchView.unclosedParentheses", "BatchProcessingEntryTable.index",
						"DisplayDocumentWindow.sign.password", "ContainersByAdministrativeUnitsView.tableTitle",
						"DocumentContextMenu.sign", "traversalCode", "Name", "ImportConfigsView.OnlyXmlAccepted",
						"BatchProcessingEntryTable.type", "facerOrder", "BatchProcessingEntryTable.mapping", "id", "guide"));
	}

	private void scanJavaFileRetrievingKeys(Set<String> keys, File javaFile) {

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(javaFile));

			String line;
			while ((line = reader.readLine()) != null) {
				scanJavaLineRetrievingKeys(keys, line);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	Pattern pattern = Pattern.compile("\\$\\(\\\"[\\w\\S]*\\\"\\)");

	private void scanJavaLineRetrievingKeys(Set<String> keys, String line) {

		if (line.contains("$")) {
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				String i18n = matcher.group();
				if (i18n.startsWith("$(\"") && i18n.endsWith("\")")) {
					i18n = i18n.substring(3, i18n.length() - 2);
					if (!i18n.contains("$") && !i18n.contains("\"") && !i18n.trim().isEmpty()) {
						keys.add(i18n);
					}
				}
			}

		}

	}

	//@Test
	public void givenArabicSystemEnsureAllObjectsHasATitle()
			throws Exception {
		givenArabicSystem();

		findMissingKeys();

		assertThat(missingKeys).isEmpty();
	}

	@Test
	public void ensureEnglishAndFrenchLanguageFilesHaveSameKeys()
			throws Exception {
		ListComparisonResults<String> results = CompareI18nKeys.compare(Language.English);

		if (!results.getNewItems().isEmpty() || !results.getRemovedItems().isEmpty()) {
			String comparisonMessage = CompareI18nKeys.getComparisonMessage(Language.English, results);
			fail("Missing i18n keys\n" + comparisonMessage);
		}
	}

	protected boolean areArabicI18nValidationsEnabled() {
		return "false".equalsIgnoreCase(getCurrentTestSession().getProperty("skip.arabicI18nTests"));
	}

	private void givenEnglishSystem() {
		givenSystemLanguageIs("en");
		givenTransactionLogIsEnabled();
		givenCollectionWithTitle(zeCollection, asList("en"), "Collection de test").withMockedAvailableModules(false)
				.withConstellioRMModule().withAllTestUsers()
				.withConstellioESModule().withRobotsModule();
		setupPlugins();
		i18n.setLocale(Locale.ENGLISH);
		locale = Locale.ENGLISH;
	}

	protected void givenFrenchSystem() {
		givenSystemLanguageIs("fr");
		givenTransactionLogIsEnabled();
		givenCollectionWithTitle(zeCollection, asList("fr"), "Collection de test").withMockedAvailableModules(false)
				.withConstellioRMModule().withAllTestUsers()
				.withConstellioESModule().withRobotsModule();
		setupPlugins();
		i18n.setLocale(Locale.FRENCH);
		locale = Locale.FRENCH;
	}

	protected void givenArabicSystem() {
		givenSystemLanguageIs("ar");
		givenTransactionLogIsEnabled();
		givenCollectionWithTitle(zeCollection, asList("ar"), "Collection de test").withMockedAvailableModules(false)
				.withConstellioRMModule().withAllTestUsers()
				.withConstellioESModule().withRobotsModule();
		setupPlugins();
		i18n.setLocale(new Locale("ar"));
		locale = new Locale("ar");
	}

	protected void setupPlugins() {

	}

	protected void findMissingKeys() {

		findTypesMissingKeys();
		findTaxonomiesMissingKeys();
		findMissingConfigKeys();
		//findMissingKeysInMainI18nFile();
		if (!missingKeys.isEmpty()) {

			System.out.println("###############################################################################");
			System.out.println("# New/Modified objects");
			System.out.println("###############################################################################");
			for (String missingKey : missingKeys) {
				System.out.println(missingKey + "=");
			}

			System.out.println("\n\n");
		}

	}

	//	private void findMissingKeysInMainI18nFile() {
	//		if (!locale.equals(new Locale("fr"))) {
	//
	//			FoldersLocator foldersLocator = new FoldersLocator();
	//
	//			File frenchI18n = new File(foldersLocator.getI18nFolder(), "i18n.properties");
	//
	//			File testedLanguageI18n = new File(foldersLocator.getI18nFolder(), "i18n_" + locale.getLanguage() + ".properties");
	//
	//			Map<String, String> frenchValues = PropertyFileUtils.loadKeyValues(frenchI18n);
	//			Set<String> frenchKeys = frenchValues.keySet();
	//			Set<String> testedLanguageKeys = PropertyFileUtils.loadKeyValues(testedLanguageI18n).keySet();
	//
	//			ListComparisonResults<String> results = LangUtils.compare(frenchKeys, testedLanguageKeys);
	//
	//			for (String missingKey : results.getRemovedItems()) {
	//
	//				if (!missingKey.endsWith(".icon") && StringUtils.isNotBlank(frenchValues.get(missingKey)))
	//					missingKeys.add(missingKey);
	//			}
	//
	//		}
	//	}

	private void findMissingConfigKeys() {
		List<String> missingKeys = new ArrayList<>();

		for (SystemConfiguration config : getModelLayerFactory().getSystemConfigurationsManager().getAllConfigurations()) {

			if (!config.isHidden()) {
				//SystemConfigurationGroup.decommissioning.calculatedCloseDateNumberOfYearWhenVariableRule
				String key = "SystemConfigurationGroup." + config.getConfigGroupCode() + "." + config.getCode();
				addIfNoValueInMainI18N(key);

				if (config.getEnumClass() != null) {
					findEnumMissingKeys(config.getEnumClass());
				}
			}
		}

		for (SystemConfigurationGroup group : getModelLayerFactory().getSystemConfigurationsManager().getConfigurationGroups()) {
			String key = "SystemConfigurationGroup." + group.getCode();
			addIfNoValueInMainI18N(key);
		}

	}

	private void findTypesMissingKeys() {
		List<String> missingKeys = new ArrayList<>();

		Language language = Language.withCode(locale.getLanguage());

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			addIfNoValue(type.getLabel(language));

			for (MetadataSchema schema : type.getAllSchemas()) {
				addIfNoValue(schema.getLabel(language));

				for (Metadata metadata : schema.getMetadatas()) {
					addIfNoValue(metadata.getLabel(language));

					if (metadata.getEnumClass() != null) {
						findEnumMissingKeys(metadata.getEnumClass());

					}
				}
			}
		}

	}

	private void findEnumMissingKeys(Class<? extends Enum<?>> enumClass) {

		if (enumClass.isAssignableFrom(EnumWithSmallCode.class)) {
			for (String smallCode : EnumWithSmallCodeUtils.toSmallCodeList(enumClass)) {
				addIfNoValueInMainI18N(enumClass.getSimpleName() + "." + smallCode);
			}
		}
	}

	private void findTaxonomiesMissingKeys() {
		for (Taxonomy taxonomy : getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection)) {
			addIfNoValue(taxonomy.getTitle().get(i18n.getLanguage()));
		}
	}

	protected void addIfNoValueInMainI18N(String key) {
		i18n.setLocale(locale);
		String label = $(key, locale);
		if (key.equals(label) && !missingKeys.contains(key)) {
			missingKeys.add(key);

		} else if (locale != Locale.FRENCH) {
			i18n.setLocale(Locale.FRENCH);
			String frenchLabel = $(key);
			if (label.equals(frenchLabel) && !missingKeys.contains(key) && !keysWithSameFrenchEnglishValue.contains(key)) {
				missingKeys.add(key);
			}
		}
	}

	private void addIfNoValue(String title) {
		if (title.startsWith("init.")) {

			if (Schemas.isGlobalMetadata(StringUtils.substringAfterLast(title, "."))) {
				String allTypesKey = "init.allTypes.allSchemas." + StringUtils.substringAfterLast(title, ".");
				if (!missingKeys.contains(allTypesKey)) {
					missingKeys.add(allTypesKey);
				}
			} else if (!missingKeys.contains(title)) {
				missingKeys.add(title);
			}
		}
	}
}
