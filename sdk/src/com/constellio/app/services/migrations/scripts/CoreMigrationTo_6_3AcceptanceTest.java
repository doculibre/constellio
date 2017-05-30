package com.constellio.app.services.migrations.scripts;

import static com.constellio.data.conf.HashingEncoding.BASE64;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.conf.DigitSeparatorMode;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class CoreMigrationTo_6_3AcceptanceTest extends ConstellioTest {
	@Before
	public void setup() {
		MetadataSchemasManager.cacheEnabled = false;
	}

	@Test
	public void whenMigratingFromASystemWithPopulatorsThanPopulatorsReadWriteCorrectly()
			throws ConfigManagerException.OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException, MetadataSchemasManagerException.OptimisticLocking {

		givenSystemAtVersion5_1_2withTokens();
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		MetadataList populated = types.getAllMetadatas().onlyPopulated();
		assertThat(populated).extracting("localCode")
				.containsOnly("author", "emailObject", "emailCCTo", "subject", "company", "emailTo", "emailFrom", "emailBCCTo");
		MetadataSchemaTypesBuilder builder = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection);
		builder.getMetadata("document_default_author")
				.setLabels(asMap(Language.French, "zAuthor fr", Language.English, "zAuthor en"));
		schemasManager.saveUpdateSchemaTypes(builder);
		MetadataList allMetadata = schemasManager.getSchemaTypes(zeCollection).getAllMetadatas();
		assertThat(allMetadata.onlyPopulated()).extracting("localCode")
				.containsOnly("author", "emailObject", "emailCCTo", "subject", "company", "emailTo", "emailFrom", "emailBCCTo");
		assertThat(allMetadata.getMetadataWithLocalCode("author").getLabels()).containsOnly(
				entry(Language.French, "zAuthor fr"),
				entry(Language.English, "zAuthor en")
		);

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("zMeta").setType(MetadataValueType.STRING)
						.getPopulateConfigsBuilder()
						.setStyles(asList("zStyle")).setRegexes(
						asList(new RegexConfig(Schemas.TITLE_CODE, Pattern.compile("title"), "zTitle",
								RegexConfig.RegexConfigType.SUBSTITUTION)));
				types.getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.BORROW_USER).getPopulateConfigsBuilder()
						.setStyles(asList("zStyle"));
			}
		});
		allMetadata = schemasManager.getSchemaTypes(zeCollection).getAllMetadatas();
		assertThat(allMetadata.onlyPopulated()).extracting("localCode")
				.containsOnly("author", "emailObject", "emailCCTo", "subject", "company", "emailTo", "emailFrom", "emailBCCTo",
						"borrowUser", "zMeta");

	}

	@Test
	public void whenMigratingFromASystemWithSingleLanguageMetadataGroupsThenContinueToWorkNormally()
			throws ConfigManagerException.OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException, MetadataSchemasManagerException.OptimisticLocking {

		givenSystemAtVersion5_1_2withTokens();

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.getMetadataGroup()).hasSize(1);
		assertThat(typeDisplay.getMetadataGroup().keySet()).containsOnly("Métadonnées");

		typeDisplay = typeDisplay
				.withMetadataGroup(configureLabels(Arrays.asList("zeGroup", "zeRequiredGroup", "zeOptionalGroup")));
		manager.saveType(typeDisplay);

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.getMetadataGroup()).hasSize(3);
		assertThat(typeDisplay.getMetadataGroup().keySet()).containsOnly("zeGroup", "zeRequiredGroup", "zeOptionalGroup");

		typeDisplay = typeDisplay
				.withMetadataGroup(configureLabels(Arrays.asList("group1", "group2", "group3", "group4")));
		manager.saveType(typeDisplay);

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.getMetadataGroup()).hasSize(4);
		assertThat(typeDisplay.getMetadataGroup().keySet()).containsOnly("group1", "group2", "group3", "group4");

	}

	@Test
	public void whenMigratingFromAPreviousSystemThenDoNotUseBase64Url()
			throws ConfigManagerException.OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException, MetadataSchemasManagerException.OptimisticLocking {

		configure(new DataLayerConfigurationAlteration() {

			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setHashingEncoding(null);
				configuration.setContentDaoFileSystemDigitsSeparatorMode(null);
			}
		});
		givenSystemAtVersion5_1_2withTokens();

		assertThat(getModelLayerFactory().getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding())
				.isEqualTo(BASE64);

		assertThat(getModelLayerFactory().getDataLayerFactory().getDataLayerConfiguration()
				.getContentDaoFileSystemDigitsSeparatorMode()).isEqualTo(DigitSeparatorMode.TWO_DIGITS);
	}

	private void givenSystemAtVersion5_1_2withTokens() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "veryOlds");
		File state = new File(statesFolder, "given_system_in_5.1.2.2_with_tasks,rm_modules__with_tokens.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private Map<String, Map<Language, String>> configureLabels(List<String> values) {
		Map<String, Map<Language, String>> groups = new HashMap<>();
		Map<Language, String> labels;

		for (String value : values) {
			labels = new HashMap<>();
			labels.put(Language.French, value);
			groups.put(value, labels);
		}
		return groups;
	}

}
