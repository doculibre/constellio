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
	public void ensureAllLanguageFilesHaveSameKeys()
			throws Exception {
		ListComparisonResults<String> results = CompareI18nKeys.compare(Language.English);

		if (!results.getNewItems().isEmpty() || !results.getRemovedItems().isEmpty()) {
			String comparisonMessage = CompareI18nKeys.getComparisonMessage(Language.English, results);
			fail("Missing i18n keys\n" + comparisonMessage);
		}
	}

	private void givenEnglishSystem() {
		givenSystemLanguageIs("en");
		givenTransactionLogIsEnabled();
		givenCollectionWithTitle(zeCollection, asList("en"), "Collection de test").withConstellioRMModule().withAllTestUsers().withConstellioESModule();
		i18n.setLocale(Locale.ENGLISH);
		locale = Locale.ENGLISH;
	}

	private void givenFrenchSystem() {
		givenSystemLanguageIs("fr");
		givenTransactionLogIsEnabled();
		givenCollectionWithTitle(zeCollection, asList("fr"), "Collection de test").withConstellioRMModule().withAllTestUsers().withConstellioESModule();
		i18n.setLocale(Locale.FRENCH);
		locale = Locale.FRENCH;
	}

	private void findMissingKeys() {

		findTypesMissingKeys();
		findTaxonomiesMissingKeys();
		findMissingConfigKeys();
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

	private void findMissingConfigKeys() {
		List<String> missingKeys = new ArrayList<>();

		for (SystemConfiguration config : getModelLayerFactory().getSystemConfigurationsManager().getAllConfigurations()) {

			//SystemConfigurationGroup.decommissioning.calculatedCloseDateNumberOfYearWhenVariableRule
			String key = "SystemConfigurationGroup." + config.getConfigGroupCode() + "." + config.getCode();
			addIfNoValueInMainI18N(key);

			if (config.getEnumClass() != null) {
				findEnumMissingKeys(config.getEnumClass());
			}

		}

		for (SystemConfigurationGroup group : getModelLayerFactory().getSystemConfigurationsManager().getConfigurationGroups()) {
			String key = "SystemConfigurationGroup." + group.getCode();
			addIfNoValueInMainI18N(key);
		}

	}

	private void findTypesMissingKeys() {
		List<String> missingKeys = new ArrayList<>();

		for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaTypes()) {
			addIfNoValue(type.getLabel());

			for (MetadataSchema schema : type.getAllSchemas()) {
				addIfNoValue(schema.getLabel());

				for (Metadata metadata : schema.getMetadatas()) {
					addIfNoValue(metadata.getLabel());

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
