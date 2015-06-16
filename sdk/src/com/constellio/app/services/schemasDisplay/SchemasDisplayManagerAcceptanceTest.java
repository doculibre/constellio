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
package com.constellio.app.services.schemasDisplay;

import static com.constellio.app.services.schemasDisplay.SchemasDisplayManager.REQUIRED_METADATA_IN_FORM_LIST;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class SchemasDisplayManagerAcceptanceTest extends ConstellioTest {

	SchemasDisplayManager manager;

	@Before
	public void setUp()
			throws Exception {

		manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
	}

	@Test
	public void givenNewCollectionWhenGetAndSetSchemaTypesThenInformationsConserved()
			throws Exception {

		givenCollection(zeCollection);

		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).isEmpty();

		manager.saveTypes(typesDisplay.withFacetMetadataCodes(asList("user_default_title")));

		typesDisplay = manager.getTypes(zeCollection);
		assertThat(typesDisplay.getFacetMetadataCodes()).containsOnly("user_default_title");
	}

	@Test
	public void givenNewCollectionWhenUpdatingSchemaTypesThenInformationsConservedUniquely()
			throws Exception {

		givenCollection(zeCollection);

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

		givenCollection(zeCollection);
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

		givenCollection(zeCollection);

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

		givenCollection(zeCollection);

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

		givenCollection(zeCollection);

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

		givenCollection(zeCollection);

		SchemaDisplayConfig schemaDisplay = manager.getSchema(zeCollection, "group_default");
		List<String> displayMetadataCodes = new ArrayList<>(schemaDisplay.getDisplayMetadataCodes());
		List<String> formMetadataCodes = new ArrayList<>(schemaDisplay.getFormMetadataCodes());
		List<String> searchResultsMetadataCodes = new ArrayList<>(schemaDisplay.getSearchResultsMetadataCodes());

		Collections.shuffle(displayMetadataCodes);
		Collections.shuffle(formMetadataCodes);
		Collections.shuffle(searchResultsMetadataCodes);
		manager.saveSchema(schemaDisplay.withDisplayMetadataCodes(displayMetadataCodes)
				.withFormMetadataCodes(formMetadataCodes).withSearchResultsMetadataCodes(searchResultsMetadataCodes));

		schemaDisplay = manager.getSchema(zeCollection, "group_default");
		assertThat(schemaDisplay.getDisplayMetadataCodes()).isEqualTo(displayMetadataCodes);
		assertThat(schemaDisplay.getFormMetadataCodes()).isEqualTo(formMetadataCodes);
		assertThat(schemaDisplay.getSearchResultsMetadataCodes()).isEqualTo(searchResultsMetadataCodes);

	}

	@Test
	public void givenNewCollectionWhenGetAndSetMetadataThenInformationsConserved()
			throws Exception {

		givenCollection(zeCollection);

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.HIDDEN)
				.withVisibleInAdvancedSearchStatus(true));

		metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.HIDDEN);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isTrue();

	}

	@Test
	public void givenNewCollectionWhenGetAndSetMultipleMetadataThenInformationsConserved()
			throws Exception {

		givenCollection(zeCollection);

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.HIDDEN)
				.withVisibleInAdvancedSearchStatus(true));

		metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.HIDDEN);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isTrue();

		metadataDisplay = manager.getMetadata(zeCollection, "user_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.FIELD);
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

		givenCollection(zeCollection);

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

		Collections.shuffle(displayMetadataCodes);
		Collections.shuffle(formMetadataCodes);
		Collections.shuffle(searchResultsMetadataCodes);
		manager.saveSchema(schemaDisplay.withDisplayMetadataCodes(displayMetadataCodes)
				.withFormMetadataCodes(formMetadataCodes).withSearchResultsMetadataCodes(searchResultsMetadataCodes));

		schemaDisplay = manager.getSchema(zeCollection, "group_default");
		assertThat(schemaDisplay.getDisplayMetadataCodes()).isEqualTo(displayMetadataCodes);
		assertThat(schemaDisplay.getFormMetadataCodes()).isEqualTo(formMetadataCodes);
		assertThat(schemaDisplay.getSearchResultsMetadataCodes()).isEqualTo(searchResultsMetadataCodes);

		// MetadataDisplay

		MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.FIELD);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isFalse();

		manager.saveMetadata(metadataDisplay.withInputType(MetadataInputType.HIDDEN)
				.withVisibleInAdvancedSearchStatus(true));

		metadataDisplay = manager.getMetadata(zeCollection, "group_default_title");
		assertThat(metadataDisplay.getInputType()).isEqualTo(MetadataInputType.HIDDEN);
		assertThat(metadataDisplay.isVisibleInAdvancedSearch()).isTrue();

	}

	@Test
	public void whenSaveTwoSchemasThenTheyAreUpdatedCorrectly()
			throws Exception {

		givenCollection(zeCollection);

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

		Map<String, String> anEssentialMetadataParams = asMap(
				"code", "mySchemaType_default_anEssentialMetadata",
				"label", "zeEssentialMetadata");

		Map<String, String> aMetadataThatWillOneDayBeEssentialParams = asMap(
				"code", "mySchemaType_default_aMetadataThatWillOneDayBeEssential",
				"label", "zeMetadataThatWillOneDayBeEssential");

		Map<String, String> aTrivialMetadataParams = asMap(
				"code", "mySchemaType_default_aTrivialMetadata",
				"label", "ZeTrivialMetadata");

		Map<String, String> titleParams = asMap(
				"code", "mySchemaType_default_title",
				"label", "Ze title");

		Map<String, String> codeParams = asMap(
				"code", "mySchemaType_default_code",
				"label", "Ze code");

		givenCollection(zeCollection);
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schemaBuilder = types.createNewSchemaType("mySchemaType").getDefaultSchema();
				schemaBuilder.create("aMetadataThatWillOneDayBeEssential").setType(TEXT).setEssential(false)
						.setLabel("zeMetadataThatWillOneDayBeEssential");
				schemaBuilder.create("anEssentialMetadata").setType(TEXT).setEssential(true).setLabel("zeEssentialMetadata");
				schemaBuilder.create("aTrivialMetadata").setType(TEXT).setEssential(false).setLabel("ZeTrivialMetadata");
				schemaBuilder.create("code").setType(TEXT).setLabel("Ze code");
				schemaBuilder.get("title").setLabel("Ze title");
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

	private ValidationError error(final String code, final Map<String, String> params) {
		return new ValidationError(SchemasDisplayManager.class.getName() + "_" + code, params);
	}

	private void assertThatValidationErrors(ValidationRuntimeException e) {

	}

	@Test
	public void givenNewCollectionWhenAddingMetadataGroupInSchemaTypeThenInformationsConserved()
			throws Exception {

		givenCollection(zeCollection);

		SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.getMetadataGroup()).hasSize(1);

		typeDisplay = typeDisplay.withMetadataGroup(Arrays.asList("zeGroup", "zeRequiredGroup", "zeOptionalGroup"));
		manager.saveType(typeDisplay);

		typeDisplay = manager.getType(zeCollection, "user");
		assertThat(typeDisplay.getMetadataGroup()).hasSize(3);
		assertThat(typeDisplay.getMetadataGroup()).containsOnly("zeGroup", "zeRequiredGroup", "zeOptionalGroup");
	}
}