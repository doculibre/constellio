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
package com.constellio.model.services.schemas;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.COPIED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.sdk.tests.TestUtils.getElementsClasses;
import static com.constellio.sdk.tests.TestUtils.onlyElementsOfClass;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50Characters;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50CharactersInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsThirdSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirementInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasLabel;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasLabelInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasNoDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasNoDefaultRequirementInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasStructureFactory;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsChildOfRelationship;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabledInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabledInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSchemaAutocomplete;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsTaxonomyRelationship;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUndeletable;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.api.impl.schemas.validation.impl.CreationDateIsBeforeOrEqualToLastModificationDateValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.CannotCreateTwoMetadataWithSameNameInDifferentCustomSchemasOfTheSameType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.testimpl.TestMetadataValidator3;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator2;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator2;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator3;
import com.constellio.model.services.schemas.testimpl.TestStructureFactory1;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataSchemasManagerAcceptanceTest extends ConstellioTest {

	TaxonomiesManager taxonomiesManager;

	MetadataSchemasManager schemasManager;

	ConfigManager configManager;

	TestsSchemasSetup defaultSchema, schemas;
	ZeSchemaMetadatas zeSchema;
	ZeCustomSchemaMetadatas zeCustomSchema;
	AnotherSchemaMetadatas anotherSchema;

	SearchServices searchServices;
	DataStoreTypesFactory typesFactory;
	CollectionsListManager collectionsListManager;

	@Mock MetadataSchemasManagerListener schemasManagerFirstCollection1Listener, schemasManagerSecondCollection1Listener, otherSchemasManagerFirstCollection1Listener, otherSchemasManagerSecondCollection1Listener, schemasManagerCollection2Listener, otherSchemasManagerCollection2Listener;

	@Before
	public void setUp()
			throws Exception {

		configManager = getDataLayerFactory().getConfigManager();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		givenCollection("zeCollection");
		schemas = defaultSchema = new TestsSchemasSetup();
		zeSchema = defaultSchema.new ZeSchemaMetadatas();
		zeCustomSchema = defaultSchema.new ZeCustomSchemaMetadatas();
		anotherSchema = defaultSchema.new AnotherSchemaMetadatas();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		typesFactory = getDataLayerFactory().newTypesFactory();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();
	}

	@Test
	public void givenSchemasInMultipleCollectionsThenAllIndependent()
			throws Exception {

		givenCollection("collection1");
		givenCollection("collection2");
		MetadataSchemaTypesBuilder collection1Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection1"));
		MetadataSchemaTypesBuilder collection2Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection2"));
		collection1Builder.createNewSchemaType("a");
		collection2Builder.createNewSchemaType("b");

		schemasManager.saveUpdateSchemaTypes(collection1Builder);
		schemasManager.saveUpdateSchemaTypes(collection2Builder);

		MetadataSchemaTypes typesCollection1 = schemasManager.getSchemaTypes("collection1");
		MetadataSchemaTypes typesCollection2 = schemasManager.getSchemaTypes("collection2");
		MetadataSchemaTypes zeCollectionTypes = schemasManager.getSchemaTypes(zeCollection);
		assertThat(schemasManager.getAllCollectionsSchemaTypes())
				.containsOnly(typesCollection1, typesCollection2, zeCollectionTypes);
		assertThat(typesCollection1.getCollection()).isEqualTo("collection1");
		assertThat(typesCollection1.getSchemaTypes()).hasSize(7);
		assertThat(typesCollection1.getSchemaType("a")).isNotNull();
		assertThat(typesCollection2.getCollection()).isEqualTo("collection2");
		assertThat(typesCollection2.getSchemaTypes()).hasSize(7);
		assertThat(typesCollection2.getSchemaType("b")).isNotNull();
	}

	@Test
	public void givenSchemasManagerIsInstanciatedThenLoadTypesAndListenThem()
			throws Exception {

		givenCollection("collection1");
		givenCollection("collection2");
		MetadataSchemaTypesBuilder collection1Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection1"));
		MetadataSchemaTypesBuilder collection2Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection2"));
		collection1Builder.createNewSchemaType("a");
		collection2Builder.createNewSchemaType("b");

		MetadataSchemasManager otherManager = new MetadataSchemasManager(configManager, typesFactory, taxonomiesManager,
				collectionsListManager);
		otherManager.initialize();

		schemasManager.saveUpdateSchemaTypes(collection1Builder);
		schemasManager.saveUpdateSchemaTypes(collection2Builder);

		MetadataSchemaTypes typesCollection1 = otherManager.getSchemaTypes("collection1");
		MetadataSchemaTypes typesCollection2 = otherManager.getSchemaTypes("collection2");
		assertThat(typesCollection1.getCollection()).isEqualTo("collection1");
		assertThat(typesCollection1.getSchemaTypes()).hasSize(7);
		assertThat(typesCollection1.getSchemaType("a")).isNotNull();
		assertThat(typesCollection2.getCollection()).isEqualTo("collection2");
		assertThat(typesCollection2.getSchemaTypes()).hasSize(7);
		assertThat(typesCollection2.getSchemaType("b")).isNotNull();
	}

	@Test
	public void givenSchemasInMultipleCollectionsModifiedThenOtherManagerNotified()
			throws Exception {

		MetadataSchemasManager otherManager = new MetadataSchemasManager(configManager, typesFactory, taxonomiesManager,
				collectionsListManager);
		otherManager.initialize();

		givenCollection("collection1");
		givenCollection("collection2");
		MetadataSchemaTypesBuilder collection1Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection1"));
		MetadataSchemaTypesBuilder collection2Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection2"));
		collection1Builder.createNewSchemaType("a");
		collection2Builder.createNewSchemaType("b");

		schemasManager.saveUpdateSchemaTypes(collection1Builder);
		schemasManager.saveUpdateSchemaTypes(collection2Builder);

		MetadataSchemaTypes typesCollection1 = otherManager.getSchemaTypes("collection1");
		MetadataSchemaTypes typesCollection2 = otherManager.getSchemaTypes("collection2");
		assertThat(typesCollection1.getCollection()).isEqualTo("collection1");
		assertThat(typesCollection1.getSchemaTypes()).hasSize(7);
		assertThat(typesCollection1.getSchemaType("a")).isNotNull();
		assertThat(typesCollection2.getCollection()).isEqualTo("collection2");
		assertThat(typesCollection2.getSchemaTypes()).hasSize(7);
		assertThat(typesCollection2.getSchemaType("b")).isNotNull();
	}

	@Test
	public void givenSchemasInMultipleCollectionsModifiedThenAllListenersNotified()
			throws Exception {

		MetadataSchemasManager otherManager = new MetadataSchemasManager(configManager, typesFactory, taxonomiesManager,
				collectionsListManager);
		otherManager.initialize();

		givenCollection("collection1");
		givenCollection("collection2");

		schemasManager.registerListener("collection1", schemasManagerFirstCollection1Listener);
		schemasManager.registerListener("collection1", schemasManagerSecondCollection1Listener);
		schemasManager.registerListener("collection2", schemasManagerCollection2Listener);
		otherManager.registerListener("collection1", otherSchemasManagerFirstCollection1Listener);
		otherManager.registerListener("collection1", otherSchemasManagerSecondCollection1Listener);
		otherManager.registerListener("collection2", otherSchemasManagerCollection2Listener);

		MetadataSchemaTypesBuilder collection1Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection1"));
		collection1Builder.createNewSchemaType("a");
		schemasManager.saveUpdateSchemaTypes(collection1Builder);

		verify(schemasManagerFirstCollection1Listener).onCollectionSchemasModified("collection1");
		verify(schemasManagerSecondCollection1Listener).onCollectionSchemasModified("collection1");
		verify(schemasManagerCollection2Listener, never()).onCollectionSchemasModified(anyString());
		verify(otherSchemasManagerFirstCollection1Listener).onCollectionSchemasModified("collection1");
		verify(otherSchemasManagerSecondCollection1Listener).onCollectionSchemasModified("collection1");
		verify(otherSchemasManagerCollection2Listener, never()).onCollectionSchemasModified(anyString());
	}

	@Test
	public void whenSavingMetadataWithAccessRestrictionsThenSaved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineAccessRestrictions().withRequiredReadRole("read").withRequiredWriteRole("write")
						.withRequiredModificationRole("modification").withRequiredDeleteRole("delete");
			}
		}));

		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredReadRoles()).containsOnly("read");
		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredWriteRoles()).containsOnly("write");
		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredModificationRoles())
				.containsOnly("modification");
		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredDeleteRoles()).containsOnly("delete");

	}

	@Test
	public void whenSavingDefaultSchemaMetadataThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getLocalCode()).isEqualTo("stringMetadata");
	}

	@Test
	public void whenSavingDefaultSchemaMetadataThenCompleteCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getCode()).isEqualTo("zeSchemaType_default_stringMetadata");
	}

	@Test
	public void whenSavingDefaultSchemaMetadataThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasLabel("aLabel")));

		assertThat(zeSchema.stringMetadata().getLabel()).isEqualTo("aLabel");
	}

	@Test
	public void whenSavingDefaultSchemaMetadataWithDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasDefaultRequirement));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isTrue();
	}

	@Test
	public void whenSavingDefaultSchemaMetadataWithoutDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasNoDefaultRequirement));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isFalse();
	}

	@Test
	public void whenSavingDefaultSchemaEnabledMetadataThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsEnabled));

		assertThat(zeSchema.stringMetadata().isEnabled()).isTrue();
	}

	@Test
	public void whenSavingDefaultSchemaDisabledMetadataWithoutDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsDisabled));

		assertThat(zeSchema.stringMetadata().isEnabled()).isFalse();
	}

	@Test
	public void whenSavingSchemaSingleMetadatThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata());

		assertThat(zeSchema.stringMetadata().isMultivalue()).isFalse();
		assertThat(schemas.getMetadata("zeSchemaType_custom_stringMetadata").isMultivalue()).isFalse();
	}

	@Test
	public void whenSavingSchemaWithEnumMetadataThenConserved()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.andCustomSchema().withAnEnumMetadata(MetadataBuilder_EnumClassTest.AValidEnum.class)
						.withAStringMetadata());

		assertThat(zeSchema.enumMetadata().getType()).isEqualTo(MetadataValueType.ENUM);
		assertThat(zeSchema.enumMetadata().getEnumClass()).isEqualTo(MetadataBuilder_EnumClassTest.AValidEnum.class);
		assertThat(zeCustomSchema.enumMetadata().getType()).isEqualTo(MetadataValueType.ENUM);
		assertThat(zeCustomSchema.enumMetadata().getEnumClass()).isEqualTo(MetadataBuilder_EnumClassTest.AValidEnum.class);
	}

	@Test
	public void whenSavingSchemaMultivalueMetadatThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichIsMultivalue));

		assertThat(zeSchema.stringMetadata().isMultivalue()).isTrue();
		assertThat(schemas.getMetadata("zeSchemaType_custom_stringMetadata").isMultivalue()).isTrue();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingDefaultSchemaMetadataWithValidationThenValidatorConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(limitedTo50Characters));

		assertThat(zeSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingDefaultSchemaWithValidationThenValidatorConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withCreationAndModificationDateInZeSchema());

		assertThat(zeSchema.instance().getValidators()).has(
				onlyElementsOfClass(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class));
		assertThat(zeCustomSchema.instance().getValidators()).has(
				onlyElementsOfClass(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class));
		assertThat(zeSchema.instance().getValidators()).isNotEmpty();
		assertThat(zeCustomSchema.instance().getValidators()).isNotEmpty();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingCustomSchemaWithValidationThenDefaultSchemaHasNoValidator()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withCreationAndModificationDateInZeCustomSchema());

		assertThat(zeSchema.instance().getValidators()).isEmpty();
		assertThat(zeCustomSchema.instance().getValidators()).isNotEmpty();
		assertThat(zeCustomSchema.instance().getValidators()).has(
				onlyElementsOfClass(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class));
	}

	@Test
	public void whenSavingDeletableMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().isUndeletable()).isFalse();
	}

	@Test
	public void whenSavingUndeletableMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsUndeletable));

		assertThat(zeSchema.stringMetadata().isUndeletable()).isTrue();
	}

	@Test
	public void whenSavingSchemaAutocompleteMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsSchemaAutocomplete));

		assertThat(zeSchema.stringMetadata().isSchemaAutocomplete()).isTrue();
	}

	@Test
	public void whenSavingSingleValueMetadataThenMultivalueFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().isMultivalue()).isFalse();
	}

	@Test
	public void whenSavingMultiValueMetadataThenMultivalueFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsMultivalue));

		assertThat(zeSchema.stringMetadata().isMultivalue()).isTrue();
	}

	@Test
	public void whenSavingNumberMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withANumberMetadata());

		assertThat(zeSchema.numberMetadata().getType()).isEqualTo(NUMBER);
	}

	@Test
	public void whenSavingTextMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getType()).isEqualTo(STRING);
	}

	@Test
	public void whenSavingLargeTextMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withALargeTextMetadata());

		assertThat(zeSchema.largeTextMetadata().getType()).isEqualTo(TEXT);
	}

	@Test
	public void whenSavingContentMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAContentMetadata());

		assertThat(zeSchema.contentMetadata().getType()).isEqualTo(CONTENT);
		assertThat(zeSchema.contentMetadata().getStructureFactory().getClass()).isEqualTo(ContentFactory.class);
	}

	@Test
	public void whenSavingTextMetadataWithStructureFactoryThenSettingConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasStructureFactory));

		assertThat(zeSchema.stringMetadata().getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);
	}

	@Test
	public void whenSavingTextMetadataWithStructureFactoryThenSettingConservedInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichHasStructureFactory));

		assertThat(zeCustomSchema.stringMetadata().getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);
	}

	@Test
	public void whenSavingDoubleMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withANumberMetadata());

		assertThat(zeSchema.numberMetadata().getDataStoreType()).isEqualTo("d");
	}

	@Test
	public void whenSavingMultivalueStringMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsMultivalue));

		assertThat(zeSchema.stringMetadata().getDataStoreType()).isEqualTo("ss");
	}

	@Test
	public void whenSavingDoubleMetadataThenHasCorrectDataStoreTypeInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withANumberMetadata());

		assertThat(zeCustomSchema.numberMetadata().getDataStoreType()).isEqualTo("d");
	}

	@Test
	public void whenSavingMultivalueStringMetadataThenHasCorrectDataStoreTypeInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichIsMultivalue));

		assertThat(zeCustomSchema.stringMetadata().getDataStoreType()).isEqualTo("ss");
	}

	@Test
	public void whenSavingCustomSchemaDateMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withADateTimeMetadataInCustomSchema());

		assertThat(zeCustomSchema.customDateMetadata().getDataStoreType()).isEqualTo("dt");
	}

	@Test
	public void whenSavingCustomSchemaStringMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadataInCustomSchema(whichIsMultivalue));

		assertThat(zeCustomSchema.customStringMetadata().getDataStoreType()).isEqualTo("ss");
	}

	@Test
	public void whenSavingReferencedMetadataThenAllowedSchemasConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType));

		assertThat(zeSchema.referenceMetadata().getAllowedReferences().getAllowedSchemaType()).isEqualTo(
				schemas.aThirdSchemaTypeCode());
		assertThat(zeSchema.referenceMetadata().isChildOfRelationship()).isFalse();
	}

	@Test
	public void whenSavingChildOfReferencedMetadataThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType, whichIsChildOfRelationship));

		assertThat(zeSchema.referenceMetadata().isChildOfRelationship()).isTrue();
	}

	@Test
	public void whenSavingTaxonomyReferencedMetadataThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType, whichIsTaxonomyRelationship));

		assertThat(zeSchema.referenceMetadata().isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void whenSavingReferencedMetadataThenAllowedSchemaTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAReferenceMetadata(whichAllowsAnotherDefaultSchema));

		assertThat(zeSchema.referenceMetadata().getAllowedReferences().getAllowedSchemas()).containsOnly(
				schemas.anotherDefaultSchemaCode());
	}

	@Test
	public void whenSavingManualEntryMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getDataEntry().getType()).isEqualTo(MANUAL);
	}

	@Test
	public void whenSavingCopiedMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false,
						false));

		DataEntry dataEntry = zeSchema.stringCopiedFromFirstReferenceStringMeta().getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(COPIED);
		assertThat(((CopiedDataEntry) dataEntry).getCopiedMetadata()).isEqualTo(anotherSchema.stringMetadata().getCode());
		assertThat(((CopiedDataEntry) dataEntry).getReferenceMetadata()).isEqualTo(
				zeSchema.firstReferenceToAnotherSchema().getCode());
	}

	@Test
	public void whenSavingCalculatedMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));

		DataEntry dataEntry = zeSchema.calculatedDaysBetween().getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(CALCULATED);
		assertThat(((CalculatedDataEntry) dataEntry).getCalculator()).isInstanceOf(
				DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.class);
	}

	@Test
	public void whenSavingCustomSchemaMetadataThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata());

		assertThat(zeCustomSchema.stringMetadata().getLocalCode()).isEqualTo("stringMetadata");
	}

	@Test
	public void whenSavingCustomSchemaMetadataThenCompleteCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata());

		assertThat(zeCustomSchema.stringMetadata().getCode()).isEqualTo("zeSchemaType_custom_stringMetadata");
	}

	@Test
	public void whenSavingCustomSchemaMetadataThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichHasLabelInCustomSchema("customLabel")));

		assertThat(zeSchema.stringMetadata().getLabel()).isNotEqualTo("customLabel");
		assertThat(zeCustomSchema.stringMetadata().getLabel()).isEqualTo("customLabel");
	}

	@Test
	public void whenSavingCustomSchemaMetadataWithDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichHasDefaultRequirementInCustomSchema));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isFalse();
		assertThat(zeCustomSchema.stringMetadata().isDefaultRequirement()).isTrue();
	}

	@Test
	public void whenSavingCustomSchemaMetadataWithoutDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichHasDefaultRequirement,
						whichHasNoDefaultRequirementInCustomSchema));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isTrue();
		assertThat(zeCustomSchema.stringMetadata().isDefaultRequirement()).isFalse();
	}

	@Test
	public void whenSavingCustomSchemaEnabledMetadataThenEnabledFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichIsDisabledInCustomSchema));

		assertThat(zeSchema.stringMetadata().isEnabled()).isTrue();
		assertThat(zeCustomSchema.stringMetadata().isEnabled()).isFalse();
	}

	@Test
	public void whenSavingCustomSchemaDisabledMetadataThenEnabledFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichIsDisabled, whichIsEnabledInCustomSchema));

		assertThat(zeSchema.stringMetadata().isEnabled()).isFalse();
		assertThat(zeCustomSchema.stringMetadata().isEnabled()).isTrue();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingCustomSchemaWithInheritedValidatorThenValidatorConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(limitedTo50Characters));

		assertThat(zeSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class));
		assertThat(zeCustomSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingCustomSchemaWithCustomValidatorThenValidatorConserved()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.andCustomSchema().withAStringMetadata(limitedTo50CharactersInCustomSchema));

		assertThat(zeSchema.stringMetadata().getValidators()).isEmpty();
		assertThat(zeCustomSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class));
	}

	@Test
	public void whenSavingDefaultSchemaThenDefaultCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema);

		assertThat(zeSchema.instance().getLocalCode()).isEqualTo("default");
	}

	@Test
	public void whenSavingDefaultSchemaThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema);

		assertThat(zeSchema.instance().getCode()).isEqualTo("zeSchemaType_default");
	}

	@Test
	public void whenSavingDefaultSchemaThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withSchemaLabel("zeLabel"));

		assertThat(zeSchema.instance().getLabel()).isEqualTo("zeLabel");
	}

	@Test
	public void whenSavingCustomSchemaThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema());

		assertThat(zeCustomSchema.instance().getLocalCode()).isEqualTo("custom");
	}

	@Test
	public void whenSavingCustomSchemaWithThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema());

		assertThat(zeCustomSchema.instance().getCode()).isEqualTo("zeSchemaType_custom");
	}

	@Test
	public void whenSavingCustomSchemaThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withCustomSchemaLabel("zeLabel"));

		assertThat(zeCustomSchema.instance().getLabel()).isEqualTo("zeLabel");
	}

	@Test
	public void whenSavingCustomUndeletableSchemaThenItIsUndeletable()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().whichCustomSchemaIsUndeletable());

		assertThat(zeCustomSchema.instance().isUndeletable()).isTrue();
	}

	@Test
	public void whenSavingCustomDeletableSchemaThenUndeletableIsConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().whichCustomSchemaIsDeletable());

		assertThat(zeCustomSchema.instance().isUndeletable()).isFalse();
	}

	@Test()
	public void whenSavingSchemaTypesThenCanRetreiveThemWithManager()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = modifySchemaTypesAddingTwoNewTypes();

		MetadataSchemaTypes types = saveAndLoadSavedSchemaTypes(typesBuilder);
		MetadataSchemaTypes builtTypes = typesBuilder.build(getDataLayerFactory().newTypesFactory(), taxonomiesManager);
		assertThat(types.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.USER))
				.isEqualTo(builtTypes.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.USER));

		Metadata content = types.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.CONTENT);
		Metadata builtContent = builtTypes.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.CONTENT);

		assertThat(content).isEqualTo(builtContent);
		assertThat(types.getSchema(UserDocument.DEFAULT_SCHEMA)).isEqualTo(
				builtTypes.getSchema(UserDocument.DEFAULT_SCHEMA));
		assertThat(types).isEqualTo(builtTypes);
	}

	@Test
	public void whenSavingDefaultSchemaWithAMetadataThenCustomSchemaHasThisMetadata()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata().andCustomSchema());

		assertThat(zeCustomSchema.stringMetadata().getLocalCode()).isEqualTo(zeSchema.stringMetadata().getLocalCode());
	}

	@Test
	public void whenModifyingDefaultMetadataThenCustomMetadataIsModified()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAModifiedStringMetadata().andCustomSchema());

		assertThat(zeSchema.stringMetadata().getLabel()).isEqualTo(zeCustomSchema.stringMetadata().getLabel());
	}

	@Test(expected = MetadataSchemasRuntimeException.NoSuchMetadata.class)
	public void whenSavingCustomSchemaWithAMetadataThenDefaultSchemaHasNotThisMetadata()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withADateTimeMetadataInCustomSchema());

		assertThat(zeCustomSchema.customStringMetadata()).isNotNull();
	}

	@Test()
	public void whenSavingMetadataWithInheritedValuesThenInheritDefaultValuesAfterSave()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isEqualTo(
				types.getMetadata("folder_employee_rule").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_default_rule").getLabel()).isEqualTo(
				types.getMetadata("folder_employee_rule").getLabel()).isEqualTo("Rule");
		assertThat(types.getMetadata("folder_default_rule").getAllowedReferences()).isEqualTo(
				types.getMetadata("folder_employee_rule").getAllowedReferences());
		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isEqualTo(
				types.getMetadata("folder_employee_rule").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isUndeletable()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isMultivalue()).isFalse();
	}

	@Test()
	public void whenUpdateDefaultMetadataThenCustomMetadataInheritingItHaveNew()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		types = modifyDefaultMetadata(types);

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isEqualTo(
				types.getMetadata("folder_employee_rule").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").getLabel()).isEqualTo(
				types.getMetadata("folder_employee_rule").getLabel()).isEqualTo("Ze Rule");
		assertThat(types.getMetadata("folder_default_rule").getAllowedReferences()).isEqualTo(
				types.getMetadata("folder_employee_rule").getAllowedReferences());
		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isEqualTo(
				types.getMetadata("folder_employee_rule").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_default_rule").isUndeletable()).isEqualTo(
				types.getMetadata("folder_employee_rule").isUndeletable()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isMultivalue()).isEqualTo(
				types.getMetadata("folder_employee_rule").isMultivalue()).isFalse();
	}

	@Test()
	public void givenInheritingMetadataWhenChangeEnabledThenStopInheritingDefaultValueUntilResetToNull()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_employee_rule").setEnabled(false);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_employee_rule").isEnabled()).isFalse();

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_employee_rule").setEnabled(null);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_employee_rule").isEnabled()).isTrue();
	}

	@Test()
	public void givenInheritingMetadataWhenChangeDefaultRequirementThenStopInheritingDefaultValueUntilResetToNull()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_employee_rule").setDefaultRequirement(true);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_employee_rule").isDefaultRequirement()).isTrue();

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_employee_rule").setDefaultRequirement(null);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_employee_rule").isDefaultRequirement()).isFalse();
	}

	@Test()
	public void givenInheritingMetadataWhenChangeLabelThenStopInheritingDefaultValueUntilResetToNull()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_employee_rule").setLabel("a custom rule");
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").getLabel()).isEqualTo("Rule");
		assertThat(types.getMetadata("folder_employee_rule").getLabel()).isEqualTo("a custom rule");

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_default_rule").setLabel("Ze Rule");
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").getLabel()).isEqualTo("Ze Rule");
		assertThat(types.getMetadata("folder_employee_rule").getLabel()).isEqualTo("a custom rule");

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_employee_rule").setLabel(null);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").getLabel()).isEqualTo("Ze Rule");
		assertThat(types.getMetadata("folder_employee_rule").getLabel()).isEqualTo("Ze Rule");
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata.class)
	public void whenModifyUndeletableOnInheritingMetadataThenUnmodifiableAttributeException()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);

		typesBuilder.getMetadata("folder_employee_rule").setUndeletable(false);
		typesBuilder.getSchema("folder_employee").getMetadata("rule").setUndeletable(false);
	}

	@Test
	public void whenCreatingASchemaTypeWith2MetadataWithASameNameInTwoCustomSchemasThenException()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);

		typesBuilder.createNewSchemaType("zeType").createCustomSchema("schema1").create("zeMeta");

		try {
			typesBuilder.getSchemaType("zeType").createCustomSchema("schema2").create("zeMeta");
			fail("CannotCreateTwoMetadataWithSameNameInDifferentCustomSchemasOfTheSameType expected");
		} catch (CannotCreateTwoMetadataWithSameNameInDifferentCustomSchemasOfTheSameType e) {
			//OK
		}
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata.class)
	public void whenModifyReferencesOnInheritingMetadataThenUnmodifiableAttributeException()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);

		typesBuilder.getMetadata("folder_employee_rule").defineReferences();
	}

	@Test()
	public void givenInheritingMetadataThenHasItsValidatorsAndItsInheritedRecordMetadataValidators()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_default_rule").defineValidators().add(TestRecordMetadataValidator1.class)
				.add(TestRecordMetadataValidator2.class);
		typesBuilder.getMetadata("folder_employee_rule").defineValidators().add(TestMetadataValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);
		//
		assertThat(getElementsClasses(types.getMetadata("folder_default_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class);
		assertThat(getElementsClasses(types.getMetadata("folder_employee_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class, TestMetadataValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_default_rule").defineValidators().remove(TestRecordMetadataValidator2.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getMetadata("folder_default_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class);
		assertThat(getElementsClasses(types.getMetadata("folder_employee_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestMetadataValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getMetadata("folder_default_rule").defineValidators().add(TestRecordMetadataValidator2.class);
		typesBuilder.getMetadata("folder_employee_rule").defineValidators().remove(TestMetadataValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getMetadata("folder_default_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class);
		assertThat(getElementsClasses(types.getMetadata("folder_employee_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class);
	}

	@Test()
	public void givenInheritingSchemaThenHasItsValidatorsAndItsInheritedSchemaValidators()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getSchema("folder_default").defineValidators().add(TestRecordValidator1.class)
				.add(TestRecordValidator2.class);
		typesBuilder.getSchema("folder_employee").defineValidators().add(TestRecordValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getSchema("folder_default").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class);
		assertThat(getElementsClasses(types.getSchema("folder_employee").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class, TestRecordValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getSchema("folder_default").defineValidators().remove(TestRecordValidator2.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getSchema("folder_default").getValidators())).containsOnly(
				TestRecordValidator1.class);
		assertThat(getElementsClasses(types.getSchema("folder_employee").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getSchema("folder_default").defineValidators().add(TestRecordValidator2.class);
		typesBuilder.getSchema("folder_employee").defineValidators().remove(TestRecordValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getSchema("folder_default").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class);
		assertThat(getElementsClasses(types.getSchema("folder_employee").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class);
	}

	@Test
	public void givenSecurityFlagsWhenSavingAndModifyingThenFlagsConserved()
			throws Exception {

		MetadataSchemaTypes types = createTwoSchemas();
		assertThat(types.getSchemaType("folder").hasSecurity()).isTrue();
		assertThat(types.getSchemaType("rule").hasSecurity()).isFalse();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getSchemaType("folder").hasSecurity()).isTrue();
		assertThat(types.getSchemaType("rule").hasSecurity()).isFalse();
	}

	@Test
	public void givenNewSchemaTypesThenSchemaHasCommonMetadatas()
			throws Exception {

		MetadataSchema aSchema = createTwoSchemas().getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();

		Metadata idMetadata = aSchema.getMetadata("id");
		assertThat(idMetadata.isUndeletable()).isTrue();
		assertThat(idMetadata.isSystemReserved()).isTrue();
		assertThat(idMetadata.isUnmodifiable()).isTrue();
		assertThat(idMetadata.isMultivalue()).isFalse();
		assertThat(idMetadata.isSortable()).isTrue();
		assertThat(idMetadata.isSearchable()).isTrue();
		assertThat(idMetadata.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
		assertThat(idMetadata.getType()).isSameAs(MetadataValueType.STRING);
		assertThat(idMetadata.getLocalCode()).isEqualTo("id");
		assertThat(idMetadata.getCode()).isEqualTo("folder_default_id");
		assertThat(idMetadata.isEnabled()).isTrue();
		assertThat(idMetadata.isDefaultRequirement()).isTrue();
		assertThat(idMetadata.getDataStoreCode()).isEqualTo("id");

		Metadata schemaMetadata = aSchema.getMetadata("schema");
		assertThat(schemaMetadata.isUndeletable()).isTrue();
		assertThat(schemaMetadata.isSystemReserved()).isTrue();
		assertThat(schemaMetadata.isUnmodifiable()).isFalse();
		assertThat(schemaMetadata.isMultivalue()).isFalse();
		assertThat(schemaMetadata.isSortable()).isFalse();
		assertThat(schemaMetadata.isSearchable()).isFalse();
		assertThat(schemaMetadata.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
		assertThat(schemaMetadata.getType()).isSameAs(MetadataValueType.STRING);
		assertThat(schemaMetadata.getLocalCode()).isEqualTo("schema");
		assertThat(schemaMetadata.getCode()).isEqualTo("folder_default_schema");
		assertThat(schemaMetadata.isEnabled()).isTrue();
		assertThat(schemaMetadata.isDefaultRequirement()).isTrue();
		assertThat(schemaMetadata.getDataStoreCode()).isEqualTo("schema_s");

		Metadata titleMetadata = aSchema.getMetadata("title");
		assertThat(titleMetadata.isUndeletable()).isTrue();
		assertThat(titleMetadata.isSystemReserved()).isFalse();
		assertThat(titleMetadata.isUnmodifiable()).isFalse();
		assertThat(titleMetadata.isMultivalue()).isFalse();
		assertThat(titleMetadata.isSortable()).isFalse();
		assertThat(titleMetadata.isSearchable()).isTrue();
		assertThat(titleMetadata.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
		assertThat(titleMetadata.getType()).isSameAs(MetadataValueType.STRING);
		assertThat(titleMetadata.getLocalCode()).isEqualTo("title");
		assertThat(titleMetadata.getCode()).isEqualTo("folder_default_title");
		assertThat(titleMetadata.isEnabled()).isTrue();
		assertThat(titleMetadata.isDefaultRequirement()).isFalse();
		assertThat(titleMetadata.getDataStoreCode()).isEqualTo("title_s");
	}

	private MetadataSchemaTypes createTwoSchemas()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = modifySchemaTypesAddingTwoNewTypes();
		assertThat(typesBuilder.getMetadata("folder_default_rule").getInheritance()).isNull();
		assertThat(typesBuilder.getMetadata("folder_default_rule").isMultivalue()).isFalse();
		assertThat(typesBuilder.getMetadata("folder_default_path").isMultivalue()).isTrue();
		assertThat(typesBuilder.getMetadata("folder_employee_path").isMultivalue()).isTrue();
		MetadataSchemaTypes types = saveAndLoadSavedSchemaTypes(typesBuilder);
		assertThat(types.getMetadata("folder_default_rule").inheritDefaultSchema()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isMultivalue()).isFalse();
		return types;
	}

	private MetadataSchemaTypes modifyDefaultMetadata(MetadataSchemaTypes types)
			throws MetadataSchemasManagerException {
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		MetadataBuilder rule = typesBuilder.getMetadata("folder_default_rule");
		rule.setEnabled(false).setLabel("Ze Rule").setDefaultRequirement(true);
		// rule.defineReferences().add(types.getDefaultSchema("rule"));
		types = saveAndLoadSavedSchemaTypes(typesBuilder);
		return types;
	}

	private MetadataSchemaTypesBuilder modifySchemaTypesAddingTwoNewTypes()
			throws Exception {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes("zeCollection");
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		MetadataSchemaTypeBuilder folderType = addFolderSchemaTypeBuilderWithoutRuleMetadatas(typesBuilder);
		MetadataSchemaTypeBuilder ruleType = addRuleSchemaTypeBuilder(typesBuilder);

		MetadataBuilder rule = folderType.getDefaultSchema().create("rule").setLabel("Rule").setType(REFERENCE)
				.setMultivalue(false);
		rule.defineReferences().set(ruleType);

		folderType.getDefaultSchema().create("ruleCode").setLabel("Rule code").setType(STRING).defineDataEntry()
				.asCopied(rule, ruleType.getDefaultSchema().getMetadata("code"));

		return typesBuilder;
	}

	private MetadataSchemaTypeBuilder addFolderSchemaTypeBuilderWithoutRuleMetadatas(MetadataSchemaTypesBuilder types)
			throws Exception {

		MetadataSchemaTypeBuilder folderBuilder = types.createNewSchemaType("folder").setLabel("Folder").setSecurity(true);
		folderBuilder.getDefaultSchema().create("zetitle").setType(STRING).setLabel("Title").setUndeletable(true)
				.setUnmodifiable(true);
		folderBuilder.createCustomSchema("employee").create("employeeName").setType(STRING).setLabel("Name of employee")
				.setUndeletable(true).setSystemReserved(true).setMultivalue(false).setUniqueValue(true);

		return folderBuilder;
	}

	private MetadataSchemaTypeBuilder addRuleSchemaTypeBuilder(MetadataSchemaTypesBuilder types) {

		MetadataSchemaTypeBuilder ruleBuilder = types.createNewSchemaType("rule").setLabel("Rule").setSecurity(false);
		ruleBuilder.getDefaultSchema().create("zetitle").setType(STRING).setLabel("Title").setUndeletable(true);
		ruleBuilder.getDefaultSchema().create("code").setType(STRING).setLabel("Code").setUndeletable(true);

		return ruleBuilder;

	}

	private MetadataSchemaTypes saveAndLoadSavedSchemaTypes(MetadataSchemaTypesBuilder typesBuilder)
			throws MetadataSchemasManagerException {
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		MetadataSchemaTypes newTypes = schemasManager.getSchemaTypes("zeCollection");
		return newTypes;
	}

	public static class CalculatorWithOtherSchemaTitleDependency implements MetadataValueCalculator<String> {

		ReferenceDependency<String> title = ReferenceDependency.toAString("zeType_default_ref",
				"anotherType_default_title");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return null;
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(title);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

	}

	public static class CalculatorWithOtherSchemaCodeDependency implements MetadataValueCalculator<String> {

		ReferenceDependency<String> refCode = ReferenceDependency.toAString("zeType_default_ref",
				"anotherType_default_code");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return null;
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(refCode);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

	}

	public static class CalculatorWithOtherSchemaCodeAndTitleDependency implements MetadataValueCalculator<String> {

		ReferenceDependency<String> title = ReferenceDependency.toAString("zeType_default_ref",
				"anotherType_default_title");
		ReferenceDependency<String> refCode = ReferenceDependency.toAString("zeType_default_ref",
				"anotherType_default_code");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return null;
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(refCode, title);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

	}

}
