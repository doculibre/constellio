package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.FIELD;
import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.TEXTAREA;
import static com.constellio.app.services.schemasDisplay.SchemasDisplayManager.REQUIRED_METADATA_IN_FORM_LIST;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SchemasDisplayManagerAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager schemasManager;
	SchemasDisplayManager manager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);
		manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
	}

	@Test
	public void givenNewCollectionWhenGetAndSetSchemaTypesThenInformationsConserved()
			throws Exception {

		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).isEmpty();

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title")));

		typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title");
	}

	@Test
	public void givenNewCollectionWhenUpdatingSchemaTypesThenInformationsConservedUniquely()
			throws Exception {

		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).isEmpty();

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title")));

		typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title");

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title2")));

		typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title2");
	}

	@Test
	public void givenTwoNewCollectionWhenGetAndSetSchemaTypesThenInformationsConservedWithoutCrossingBondaries()
			throws Exception {

		String zeCollection2 = "zeCollection2";

		givenCollection(zeCollection2);

		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(zeCollection);
		SchemaTypesDisplayConfig typesDisplay2 = manager.getTypes(zeCollection2);
		assertThat(typesDisplay.getFacetMetadataCodes()).isEmpty();
		assertThat(typesDisplay2.getFacetMetadataCodes()).isEmpty();

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title")));
		manager.saveTypes(typesDisplay2.withFacetMetadataCodes(asList("user_default_title2")));

		typesDisplay = manager.getTypes(zeCollection);
		typesDisplay2 = manager.getTypes(zeCollection2);

		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title");
		assertThat(typesDisplay2.getFacetMetadataCodes()).containsOnly("user_default_title2");
	}

	@Test
	public void givenNewCollectionWhenGetAndSetSchemaTypeThenInformationsConserved()
			throws Exception {

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay.isSimpleSearch()).isFalse();
		assertThat(typeDisplay.isManageable()).isFalse();

		manager.saveType(typeDisplay.withAdvancedSearchStatus(true).withSimpleSearchStatus(true)
				.withManageableStatus(true));

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isTrue();
		assertThat(typeDisplay.isSimpleSearch()).isTrue();
		assertThat(typeDisplay.isManageable()).isTrue();
	}

	@Test
	public void givenNewCollectionWhenGetAndSetMultipleSchemaTypeThenInformationsConserved()
			throws Exception {

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay.isSimpleSearch()).isFalse();
		assertThat(typeDisplay.isManageable()).isFalse();

		SchemaTypeDisplayConfig typeDisplay2 = manager.getType(zeCollection, "user2");
		assertThat(typeDisplay2.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay2.isSimpleSearch()).isFalse();
		assertThat(typeDisplay2.isManageable()).isFalse();

		manager.saveType(typeDisplay.withAdvancedSearchStatus(true).withSimpleSearchStatus(true)
				.withManageableStatus(true));
		manager.saveType(typeDisplay2.withAdvancedSearchStatus(true).withSimpleSearchStatus(true)
				.withManageableStatus(true));

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isTrue();
		assertThat(typeDisplay.isSimpleSearch()).isTrue();
		assertThat(typeDisplay.isManageable()).isTrue();

		typeDisplay2 = manager.getType(zeCollection, "user2");
		assertThat(typeDisplay2.isAdvancedSearch()).isTrue();
		assertThat(typeDisplay2.isSimpleSearch()).isTrue();
		assertThat(typeDisplay2.isManageable()).isTrue();
	}

	@Test
	public void givenNewCollectionWhenUpdatingSchemaTypeThenInformationsOverwrittenAndConserved()
			throws Exception {

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay.isSimpleSearch()).isFalse();
		assertThat(typeDisplay.isManageable()).isFalse();

		manager.saveType(typeDisplay.withAdvancedSearchStatus(true).withSimpleSearchStatus(true)
				.withManageableStatus(true));

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isTrue();
		assertThat(typeDisplay.isSimpleSearch()).isTrue();
		assertThat(typeDisplay.isManageable()).isTrue();

		manager.saveType(typeDisplay.withAdvancedSearchStatus(false).withSimpleSearchStatus(true)
				.withManageableStatus(false));

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay.isSimpleSearch()).isTrue();
		assertThat(typeDisplay.isManageable()).isFalse();
	}

	@Test
	public void givenNewCollectionWhenGetAndSetSchemaThenInformationsConserved()
			throws Exception {

		SchemaDisplayConfig schemaDisplay = manager.getSchema(zeCollection, "group_default");
		List<String> displayMetadataCodes = new ArrayList<>(schemaDisplay.getDisplayMetadataCodes());
		List<String> formMetadataCodes = new ArrayList<>(schemaDisplay.getFormMetadataCodes());
		List<String> searchResultsMetadataCodes = new ArrayList<>(schemaDisplay.getSearchResultsMetadataCodes());
		List<String> tableMetadataCodes = new ArrayList<>(schemaDisplay.getTableMetadataCodes());

		Collections.shuffle(displayMetadataCodes);
		Collections.shuffle(formMetadataCodes);
		Collections.shuffle(searchResultsMetadataCodes);
		Collections.shuffle(tableMetadataCodes);
		manager.saveSchema(schemaDisplay.withDisplayMetadataCodes(displayMetadataCodes)
				.withFormMetadataCodes(formMetadataCodes).withSearchResultsMetadataCodes(searchResultsMetadataCodes)
				.withTableMetadataCodes(tableMetadataCodes));

		schemaDisplay = manager.getSchema(zeCollection, "group_default");
		assertThat(schemaDisplay.getDisplayMetadataCodes()).isEqualTo(displayMetadataCodes);
		assertThat(schemaDisplay.getFormMetadataCodes()).isEqualTo(formMetadataCodes);
		assertThat(schemaDisplay.getSearchResultsMetadataCodes()).isEqualTo(searchResultsMetadataCodes);
		assertThat(schemaDisplay.getTableMetadataCodes()).isEqualTo(tableMetadataCodes);

	}

	@Test
	public void givenNewCollectionWhenGetAndSetMetadataThenInformationsConserved()
			throws Exception {

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.HIDDEN)
				.withVisibleInAdvancedSearchStatus(true));

		metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.HIDDEN);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isTrue();

	}

	@Test
	public void givenNewCollectionWhenGetAndSetMetadataThenDisplayTypeConserved()
			throws Exception {

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "user_default_groups");
		assertThat(metadataDisplay.getDisplayType()).isEqualTo(MetadataDisplayType.VERTICAL);
		MetadataDisplayConfig config = metadataDisplay.withDisplayType(MetadataDisplayType.HORIZONTAL);
		assertThat(config.getDisplayType()).isEqualTo(MetadataDisplayType.HORIZONTAL);

		manager.saveMetadata(config);

		metadataDisplay = manager.getMetadata(zeCollection, "user_default_groups");
		assertThat(metadataDisplay.getDisplayType()).isEqualTo(MetadataDisplayType.HORIZONTAL);

		MetadataDisplayConfig config2 = metadataDisplay.withDisplayType(MetadataDisplayType.VERTICAL);
		assertThat(config2.getDisplayType()).isEqualTo(MetadataDisplayType.VERTICAL);

		manager.saveMetadata(config2);

		metadataDisplay = manager.getMetadata(zeCollection, "user_default_groups");
		assertThat(metadataDisplay.getDisplayType()).isEqualTo(MetadataDisplayType.VERTICAL);
	}

	@Test
	public void givenNewCollectionWhenGetAndSetMultipleMetadataThenInformationsConserved()
			throws Exception {

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.HIDDEN)
				.withVisibleInAdvancedSearchStatus(true));

		metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.HIDDEN);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isTrue();

		metadataDisplay = manager.getMetadata(zeCollection, "user_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.RICHTEXT)
				.withVisibleInAdvancedSearchStatus(false));

		metadataDisplay = manager.getMetadata(zeCollection, "user_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.RICHTEXT);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();
	}

	@Test
	public void givenNewCollectionWhenGetAndSetOnMultipleFieldsThenInformationsConserved()
			throws Exception {

		// SchemaTypesDisplay
		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).isEmpty();

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title")));

		typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title");

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title2")));

		typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title2");

		// SchemaTypeDisplay

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay.isSimpleSearch()).isFalse();
		assertThat(typeDisplay.isManageable()).isFalse();

		SchemaTypeDisplayConfig typeDisplay2 = manager.getType(zeCollection, "user2");
		assertThat(typeDisplay2.isAdvancedSearch()).isFalse();
		assertThat(typeDisplay2.isSimpleSearch()).isFalse();
		assertThat(typeDisplay2.isManageable()).isFalse();

		manager.saveType(typeDisplay.withAdvancedSearchStatus(true).withSimpleSearchStatus(true)
				.withManageableStatus(true));
		manager.saveType(typeDisplay2.withAdvancedSearchStatus(true).withSimpleSearchStatus(true)
				.withManageableStatus(true));

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.isAdvancedSearch()).isTrue();
		assertThat(typeDisplay.isSimpleSearch()).isTrue();
		assertThat(typeDisplay.isManageable()).isTrue();

		typeDisplay2 = manager.getType(zeCollection, "user2");
		assertThat(typeDisplay2.isAdvancedSearch()).isTrue();
		assertThat(typeDisplay2.isSimpleSearch()).isTrue();
		assertThat(typeDisplay2.isManageable()).isTrue();

		// SchemaDisplayConfig

		SchemaDisplayConfig schemaDisplay = manager.getSchema(zeCollection, "group_default");
		List<String> displayMetadataCodes = new ArrayList<>(schemaDisplay.getDisplayMetadataCodes());
		List<String> formMetadataCodes = new ArrayList<>(schemaDisplay.getFormMetadataCodes());
		List<String> searchResultsMetadataCodes = new ArrayList<>(schemaDisplay.getSearchResultsMetadataCodes());
		List<String> tableMetadataCodes = new ArrayList<>(schemaDisplay.getTableMetadataCodes());

		Collections.shuffle(displayMetadataCodes);
		Collections.shuffle(formMetadataCodes);
		Collections.shuffle(searchResultsMetadataCodes);
		Collections.shuffle(tableMetadataCodes);
		manager.saveSchema(schemaDisplay.withDisplayMetadataCodes(displayMetadataCodes)
				.withFormMetadataCodes(formMetadataCodes).withSearchResultsMetadataCodes(searchResultsMetadataCodes)
				.withTableMetadataCodes(tableMetadataCodes));

		schemaDisplay = manager.getSchema(zeCollection, "group_default");
		assertThat(schemaDisplay.getDisplayMetadataCodes()).isEqualTo(displayMetadataCodes);
		assertThat(schemaDisplay.getFormMetadataCodes()).isEqualTo(formMetadataCodes);
		assertThat(schemaDisplay.getSearchResultsMetadataCodes()).isEqualTo(searchResultsMetadataCodes);
		assertThat(schemaDisplay.getTableMetadataCodes()).isEqualTo(tableMetadataCodes);

		// MetadataDisplay

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();
		assertThat(metadataDisplay.getHelpMessages()).isEmpty();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.HIDDEN)
				.withVisibleInAdvancedSearchStatus(true)
				.withFrenchHelpMessage("BeepBeepBoop"));

		metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.HIDDEN);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isTrue();
		assertThat(metadataDisplay.getFrenchHelpMessage()).isEqualTo("BeepBeepBoop");

	}

	@Test
	public void whenSaveTwoSchemasThenTheyAreUpdatedCorrectly()
			throws Exception {

		SchemaDisplayConfig schemaGroupDisplay = manager.getSchema(zeCollection, Group.DEFAULT_SCHEMA);
		SchemaDisplayConfig schemaUserDisplay = manager.getSchema(zeCollection, User.DEFAULT_SCHEMA);

		int initialFormSize = schemaGroupDisplay.getFormMetadataCodes().size();
		int initialDisplaySize = schemaUserDisplay.getFormMetadataCodes().size();

		manager.saveSchema(schemaGroupDisplay);
		manager.saveSchema(schemaUserDisplay);

		schemaGroupDisplay = manager.getSchema(zeCollection, Group.DEFAULT_SCHEMA);
		schemaUserDisplay = manager.getSchema(zeCollection, User.DEFAULT_SCHEMA);

		assertThat(schemaGroupDisplay.getFormMetadataCodes()).hasSize(initialFormSize);
		assertThat(schemaUserDisplay.getFormMetadataCodes()).hasSize(initialDisplaySize);

		manager.saveSchema(manager.getSchema(zeCollection, Group.DEFAULT_SCHEMA).withFormMetadataCodes(asList(
				Group.DEFAULT_SCHEMA + "_" + Group.TITLE, Group.DEFAULT_SCHEMA + "_" + Group.CODE)));
		manager.saveSchema(manager.getSchema(zeCollection, User.DEFAULT_SCHEMA).withFormMetadataCodes(asList(
				User.DEFAULT_SCHEMA + "_" + User.FIRSTNAME)));

		schemaGroupDisplay = manager.getSchema(zeCollection, Group.DEFAULT_SCHEMA);
		schemaUserDisplay = manager.getSchema(zeCollection, User.DEFAULT_SCHEMA);

		assertThat(schemaGroupDisplay.getFormMetadataCodes())
				.containsOnly("group_default_title", "group_default_code");
		assertThat(schemaUserDisplay.getFormMetadataCodes())
				.containsOnly(User.DEFAULT_SCHEMA + "_" + User.FIRSTNAME);
	}

	@Test
	public void whenSavingFormMetadatasWithoutEssentialMetadataThenValidationException()
			throws Exception {

		Map<String, Object> anEssentialMetadataParams = asMap(
				"code", "mySchemaType_default_anEssentialMetadata");
		anEssentialMetadataParams.put("label", TestUtils.asMap("fr", "zeEssentialMetadata", "en", "anEssentialMetadata"));

		Map<String, Object> aMetadataThatWillOneDayBeEssentialParams = asMap(
				"code", "mySchemaType_default_aMetadataThatWillOneDayBeEssential");
		aMetadataThatWillOneDayBeEssentialParams
				.put("label",
						TestUtils.asMap("fr", "zeMetadataThatWillOneDayBeEssential", "en", "aMetadataThatWillOneDayBeEssential"));

		Map<String, Object> aTrivialMetadataParams = asMap(
				"code", "mySchemaType_default_aTrivialMetadata");

		Map<String, Object> titleParams = asMap(
				"code", "mySchemaType_default_title");
		Map<String, Object> titleLabels = asMap("fr", "Ze title");
		titleLabels.put("en", "Ze title en");
		titleParams.put("label", titleLabels);

		Map<String, Object> codeParams = asMap(
				"code", "mySchemaType_default_code");
		codeParams.put("label", TestUtils.asMap("fr", "Ze code", "en", "code"));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schemaBuilder = types.createNewSchemaType("mySchemaType").getDefaultSchema();
				schemaBuilder.create("aMetadataThatWillOneDayBeEssential").setType(TEXT).setEssential(false)
						.addLabel(Language.French, "zeMetadataThatWillOneDayBeEssential");
				schemaBuilder.create("anEssentialMetadata").setType(TEXT).setEssential(true)
						.addLabel(Language.French, "zeEssentialMetadata");
				schemaBuilder.create("aTrivialMetadata").setType(TEXT).setEssential(false)
						.addLabel(Language.French, "ZeTrivialMetadata");
				schemaBuilder.create("code").setType(TEXT).addLabel(Language.French, "Ze code");
				schemaBuilder.get("title").addLabel(Language.French, "Ze title");
				schemaBuilder.get("title").addLabel(Language.English, "Ze title en");
			}
		});

		SchemaDisplayConfig schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
		assertThat(schemaUserDisplay.getFormMetadataCodes()).containsOnly(
				"mySchemaType_default_anEssentialMetadata",
				"mySchemaType_default_aTrivialMetadata",
				"mySchemaType_default_aMetadataThatWillOneDayBeEssential",
				"mySchemaType_default_title",
				"mySchemaType_default_code");

		try {
			schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
			manager.saveSchema(schemaUserDisplay.withFormMetadataCodes(asList(
					"mySchemaType_default_anEssentialMetadata",
					"mySchemaType_default_code")));
			fail("ValidationRuntimeException expected");
		} catch (ValidationRuntimeException e) {
			assertThat(e.getValidationErrorsList()).containsOnly(
					error(REQUIRED_METADATA_IN_FORM_LIST, titleParams));
		}

		try {
			schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
			manager.saveSchema(schemaUserDisplay.withFormMetadataCodes(new ArrayList<String>()));
			fail("ValidationRuntimeException expected");
		} catch (ValidationRuntimeException e) {
			assertThat(e.getValidationErrorsList()).containsOnly(
					error(REQUIRED_METADATA_IN_FORM_LIST, anEssentialMetadataParams),
					error(REQUIRED_METADATA_IN_FORM_LIST, titleParams),
					error(REQUIRED_METADATA_IN_FORM_LIST, codeParams));
		}

		schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
		manager.saveSchema(schemaUserDisplay.withFormMetadataCodes(asList(
				"mySchemaType_default_anEssentialMetadata",
				"mySchemaType_default_title",
				"mySchemaType_default_code")));

		schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
		assertThat(schemaUserDisplay.getFormMetadataCodes()).containsOnly(
				"mySchemaType_default_anEssentialMetadata",
				"mySchemaType_default_title",
				"mySchemaType_default_code");

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schemaBuilder = types.getSchema("mySchemaType_default");
				schemaBuilder.get("aMetadataThatWillOneDayBeEssential").setEssential(true);
			}
		});

		schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
		assertThat(schemaUserDisplay.getFormMetadataCodes()).containsOnly(
				"mySchemaType_default_anEssentialMetadata",
				"mySchemaType_default_aMetadataThatWillOneDayBeEssential",
				"mySchemaType_default_title",
				"mySchemaType_default_code");

		try {
			schemaUserDisplay = manager.getSchema(zeCollection, "mySchemaType_default");
			manager.saveSchema(schemaUserDisplay.withFormMetadataCodes(new ArrayList<String>()));
			fail("ValidationRuntimeException expected");
		} catch (ValidationRuntimeException e) {
			assertThat(e.getValidationErrorsList()).containsOnly(
					error(REQUIRED_METADATA_IN_FORM_LIST, aMetadataThatWillOneDayBeEssentialParams),
					error(REQUIRED_METADATA_IN_FORM_LIST, anEssentialMetadataParams),
					error(REQUIRED_METADATA_IN_FORM_LIST, titleParams),
					error(REQUIRED_METADATA_IN_FORM_LIST, codeParams));
		}

	}

	@Test
	public void givenUnconfiguredSchemaDisplayWhenGetValueThenReturnDefaultValue()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(zeCollection);
		MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaType("myType");
		MetadataSchemaBuilder defaultSchema = typeBuilder.getDefaultSchema();
		MetadataSchemaBuilder customSchema = typeBuilder.createCustomSchema("custom");
		defaultSchema.create("metadata1").setType(MetadataValueType.STRING);
		defaultSchema.create("metadata2").setType(MetadataValueType.STRING);
		customSchema.create("customMetadata1").setType(MetadataValueType.STRING);
		customSchema.create("customMetadata2").setType(MetadataValueType.STRING);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		SchemaDisplayConfig myTypeDefaultSchema = manager.getSchema(zeCollection, "myType_default");
		SchemaDisplayConfig myTypeCustomSchema = manager.getSchema(zeCollection, "myType_custom");

		assertThat(myTypeDefaultSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_default_title", "myType_default_createdBy", "myType_default_createdOn", "myType_default_modifiedBy",
				"myType_default_modifiedOn", "myType_default_metadata1", "myType_default_metadata2"));

		assertThat(myTypeCustomSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_custom_title", "myType_custom_createdBy", "myType_custom_createdOn", "myType_custom_modifiedBy",
				"myType_custom_modifiedOn", "myType_custom_metadata1", "myType_custom_metadata2",
				"myType_custom_customMetadata1", "myType_custom_customMetadata2"));

		manager.saveSchema(myTypeDefaultSchema.withDisplayMetadataCodes(asList(
				"myType_default_title", "myType_default_createdBy", "myType_default_modifiedBy", "myType_default_metadata2")));

		myTypeDefaultSchema = manager.getSchema(zeCollection, "myType_default");
		myTypeCustomSchema = manager.getSchema(zeCollection, "myType_custom");
		assertThat(myTypeDefaultSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_default_title", "myType_default_createdBy", "myType_default_modifiedBy", "myType_default_metadata2"));

		assertThat(myTypeCustomSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_custom_title", "myType_custom_createdBy", "myType_custom_modifiedBy", "myType_custom_metadata2",
				"myType_custom_customMetadata1", "myType_custom_customMetadata2"));

		manager.resetSchema(zeCollection, "myType_default");
		myTypeDefaultSchema = manager.getSchema(zeCollection, "myType_default");
		myTypeCustomSchema = manager.getSchema(zeCollection, "myType_custom");

		assertThat(myTypeDefaultSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_default_title", "myType_default_createdBy", "myType_default_createdOn", "myType_default_modifiedBy",
				"myType_default_modifiedOn", "myType_default_metadata1", "myType_default_metadata2"));

		assertThat(myTypeCustomSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_custom_title", "myType_custom_createdBy", "myType_custom_createdOn", "myType_custom_modifiedBy",
				"myType_custom_modifiedOn", "myType_custom_metadata1", "myType_custom_metadata2",
				"myType_custom_customMetadata1", "myType_custom_customMetadata2"));
	}

	@Test
	public void givenUnconfiguredMetadataDisplayWhenGetValueThenReturnDefaultValue()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(zeCollection);
		MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaType("myType");
		MetadataSchemaBuilder defaultSchema = typeBuilder.getDefaultSchema();
		MetadataSchemaBuilder customSchema = typeBuilder.createCustomSchema("custom");
		defaultSchema.create("metadata").setType(MetadataValueType.STRING);
		customSchema.create("customMetadata").setType(MetadataValueType.STRING);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		Map<String, Map<Language, String>> groups = configureLabels(Arrays.asList("zeGroup", "Default", "zeCustomGroup"));

		SchemaTypeDisplayConfig typeConfig = manager.getType(zeCollection, "myType");
		manager.saveType(typeConfig.withMetadataGroup(groups));

		MetadataDisplayConfig defaultSchemaMetadata = manager.getMetadata(zeCollection, "myType_default_metadata");
		MetadataDisplayConfig customSchemaMetadata = manager.getMetadata(zeCollection, "myType_custom_metadata");
		MetadataDisplayConfig customSchemaCustomMetadata = manager.getMetadata(zeCollection, "myType_custom_customMetadata");
		assertThat(defaultSchemaMetadata.getMetadataGroupCode()).isEqualTo("");
		assertThat(defaultSchemaMetadata.getInputType()).isEqualTo(FIELD);
		assertThat(customSchemaMetadata.getMetadataGroupCode()).isEqualTo("");
		assertThat(customSchemaMetadata.getInputType()).isEqualTo(FIELD);
		assertThat(customSchemaCustomMetadata.getMetadataGroupCode()).isEqualTo("");
		assertThat(customSchemaCustomMetadata.getInputType()).isEqualTo(FIELD);

		manager.saveMetadata(defaultSchemaMetadata.withMetadataGroup("zeGroup").withInputType(TEXTAREA));

		defaultSchemaMetadata = manager.getMetadata(zeCollection, "myType_default_metadata");
		customSchemaMetadata = manager.getMetadata(zeCollection, "myType_custom_metadata");
		customSchemaCustomMetadata = manager.getMetadata(zeCollection, "myType_custom_customMetadata");
		assertThat(defaultSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeGroup");
		assertThat(defaultSchemaMetadata.getInputType()).isEqualTo(TEXTAREA);
		assertThat(customSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeGroup");
		assertThat(customSchemaMetadata.getInputType()).isEqualTo(TEXTAREA);
		assertThat(customSchemaCustomMetadata.getMetadataGroupCode()).isEqualTo("");
		assertThat(customSchemaCustomMetadata.getInputType()).isEqualTo(FIELD);

		manager.saveMetadata(customSchemaMetadata.withMetadataGroup("zeCustomGroup").withInputType(FIELD));

		defaultSchemaMetadata = manager.getMetadata(zeCollection, "myType_default_metadata");
		customSchemaMetadata = manager.getMetadata(zeCollection, "myType_custom_metadata");
		customSchemaCustomMetadata = manager.getMetadata(zeCollection, "myType_custom_customMetadata");
		assertThat(defaultSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeGroup");
		assertThat(defaultSchemaMetadata.getInputType()).isEqualTo(TEXTAREA);
		assertThat(customSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeCustomGroup");
		assertThat(customSchemaMetadata.getInputType()).isEqualTo(FIELD);
		assertThat(customSchemaCustomMetadata.getMetadataGroupCode()).isEqualTo("");
		assertThat(customSchemaCustomMetadata.getInputType()).isEqualTo(FIELD);

		manager.saveMetadata(customSchemaCustomMetadata.withMetadataGroup("zeCustomGroup").withInputType(TEXTAREA));

		defaultSchemaMetadata = manager.getMetadata(zeCollection, "myType_default_metadata");
		customSchemaMetadata = manager.getMetadata(zeCollection, "myType_custom_metadata");
		customSchemaCustomMetadata = manager.getMetadata(zeCollection, "myType_custom_customMetadata");
		assertThat(defaultSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeGroup");
		assertThat(defaultSchemaMetadata.getInputType()).isEqualTo(TEXTAREA);
		assertThat(customSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeCustomGroup");
		assertThat(customSchemaMetadata.getInputType()).isEqualTo(FIELD);
		assertThat(customSchemaCustomMetadata.getMetadataGroupCode()).isEqualTo("zeCustomGroup");
		assertThat(customSchemaCustomMetadata.getInputType()).isEqualTo(TEXTAREA);

		manager.resetSchema(zeCollection, "myType_custom");

		defaultSchemaMetadata = manager.getMetadata(zeCollection, "myType_default_metadata");
		customSchemaMetadata = manager.getMetadata(zeCollection, "myType_custom_metadata");
		customSchemaCustomMetadata = manager.getMetadata(zeCollection, "myType_custom_customMetadata");
		assertThat(defaultSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeGroup");
		assertThat(defaultSchemaMetadata.getInputType()).isEqualTo(TEXTAREA);
		assertThat(customSchemaMetadata.getMetadataGroupCode()).isEqualTo("zeGroup");
		assertThat(customSchemaMetadata.getInputType()).isEqualTo(TEXTAREA);
		assertThat(customSchemaCustomMetadata.getMetadataGroupCode()).isEqualTo("");
		assertThat(customSchemaCustomMetadata.getInputType()).isEqualTo(FIELD);
	}

	@Test
	public void givenUnconfiguredSchemaDisplayInheritingSchemaWithRemovedMetadataFromDisplayWhenGetValueThenReturnDefaultValue()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(zeCollection);
		MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaType("myType");
		MetadataSchemaBuilder defaultSchema = typeBuilder.getDefaultSchema();
		defaultSchema.create("metadata1").setType(MetadataValueType.STRING);
		defaultSchema.create("metadata2").setType(MetadataValueType.STRING);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		SchemaDisplayConfig myTypeDefaultSchema = manager.getSchema(zeCollection, "myType_default");

		manager.saveSchema(myTypeDefaultSchema.withDisplayMetadataCodes(asList(
				"myType_default_title", "myType_default_createdBy", "myType_default_modifiedBy", "myType_default_metadata2")));

		typesBuilder = schemasManager.modify(zeCollection);
		typeBuilder = typesBuilder.getOrCreateNewSchemaType("myType");
		MetadataSchemaBuilder customSchema = typeBuilder.createCustomSchema("custom");
		customSchema.create("customMetadata1").setType(MetadataValueType.STRING);
		customSchema.create("customMetadata2").setType(MetadataValueType.STRING);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		SchemaDisplayConfig myTypeCustomSchema = manager.getSchema(zeCollection, "myType_custom");
		assertThat(myTypeCustomSchema.getDisplayMetadataCodes()).isEqualTo(asList(
				"myType_custom_title", "myType_custom_createdBy", "myType_custom_modifiedBy", "myType_custom_metadata2",
				"myType_custom_customMetadata1", "myType_custom_customMetadata2"));
	}

	private ValidationError error(final String code, final Map<String, Object> params) {
		return new ValidationError(SchemasDisplayManager.class, code, params);
	}

	@Test
	public void givenNewCollectionWhenAddModifyMetadataGroupsInSchemaTypeThenInformationsConserved()
			throws Exception {

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.getMetadataGroup()).hasSize(1);

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
	public void givenMetadataWhenWithAddedHelpMessageThenMessageConserved()
		throws Exception {

		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(zeCollection);
		MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaType("myType");
		MetadataSchemaBuilder defaultSchema = typeBuilder.getDefaultSchema();
		defaultSchema.create("metadata").setType(MetadataValueType.STRING);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "myType_default_metadata");
		assertThat(metadataDisplay.getHelpMessages()).isEmpty();

		manager.saveMetadata(metadataDisplay
				.withEnglishHelpMessage("help")
				.withFrenchHelpMessage("aide")
		);

		metadataDisplay = manager.getMetadata(zeCollection, "myType_default_metadata");
		assertThat(metadataDisplay.getEnglishHelpMessage()).isEqualTo("help");
		assertThat(metadataDisplay.getFrenchHelpMessage()).isEqualTo("aide");
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

	private Map<String, Object> asMap(String key1, String value1) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}
}