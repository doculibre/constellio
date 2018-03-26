package com.constellio.app.ui.acceptation;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.constellio.sdk.dev.tools.CompareI18nKeys;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class I18NAcceptationAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	RMSchemasRecordsServices schemas;

	List<String> missingKeys = new ArrayList<>();
	Locale locale;
	Locale defaultLocale;

	private static List<String> keysWithSameFrenchEnglishValue = new ArrayList<>();

	static {
		keysWithSameFrenchEnglishValue.add("SystemConfigurationGroup.agent");
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

	@Test
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

	@Test
	public void ensureArabicAndFrenchLanguageFilesHaveSameKeys()
			throws Exception {
		ListComparisonResults<String> results = CompareI18nKeys.compare(Language.Arabic);

		if (!results.getNewItems().isEmpty() || !results.getRemovedItems().isEmpty()) {
			String comparisonMessage = CompareI18nKeys.getComparisonMessage(Language.Arabic, results);
			fail("Missing i18n keys\n" + comparisonMessage);
		}
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

	private void findMissingKeys() {

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
		List<String> missingKeys = new ArrayList<>();

		for (Taxonomy taxonomy : getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection)) {
			addIfNoValue(taxonomy.getTitle());
		}

	}

	private void addIfNoValueInMainI18N(String key) {
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
			if (!missingKeys.contains(title)) {
				missingKeys.add(title);
			}
		}
	}
}
