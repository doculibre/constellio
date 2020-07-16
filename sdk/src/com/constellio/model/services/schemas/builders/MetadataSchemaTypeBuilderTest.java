package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.util.Arrays;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataSchemaTypeBuilderTest extends ConstellioTest {

	static final String CODE_SCHEMA_TYPE = "codeSchemaType";
	static final MetadataValueType valueType = MetadataValueType.NUMBER;
	private static final String INEXISTENT_CODE = "inexistent_code";
	private static final String UNDERSCORE = "_";
	private static final String DEFAULT = "default";
	@Mock MetadataSchemaTypesBuilder typesBuilder;
	@Mock DataStoreTypesFactory typesFactory;

	String customSchemaCode = "zeCustomSchema";
	String metadataCode = "zeMetadata";
	String expectedCustomSchemaMetadataCompleteCode = CODE_SCHEMA_TYPE + UNDERSCORE + customSchemaCode + UNDERSCORE
													  + metadataCode;
	String expectedDefaultSchemaMetadataCompleteCode = CODE_SCHEMA_TYPE + UNDERSCORE + DEFAULT + UNDERSCORE + metadataCode;

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock TaxonomiesManager taxonomiesManager;

	MetadataSchemaTypeBuilder schemaTypeBuilder;

	MetadataSchemaType schemaType;

	MetadataSchemaBuilder defaultSchemaBuilder;

	@Before
	public void setup() {
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(typesBuilder.getClassProvider()).thenReturn(new DefaultClassProvider());
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);
		when(typesBuilder.getLanguages()).thenReturn(Arrays.asList(Language.French));
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, "zeUltimateCollection", "fr", Arrays.asList("fr"));
		schemaTypeBuilder = (new MetadataSchemaTypeBuilder()).createNewSchemaType(zeCollectionInfo, CODE_SCHEMA_TYPE, typesBuilder, getModelLayerFactory())
				.addLabel(Language.French, "aLabel");
	}

	@Test
	public void givenCodeWhenBuildingThenHasCorrectValue()
			throws Exception {
		build();

		assertThat(schemaType.getCode()).isEqualTo(CODE_SCHEMA_TYPE);
	}

	@Test
	public void givenCodeWhenModifyingThenHasCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(schemaTypeBuilder.getCode()).isEqualTo(CODE_SCHEMA_TYPE);
	}

	@Test
	public void givenCollectionWhenBuildingThenHasCorrectValue()
			throws Exception {
		build();

		assertThat(schemaType.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenCollectionWhenModifyingThenHasCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(schemaTypeBuilder.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenLabelWhenBuildingThenHasCorrectValue()
			throws Exception {
		schemaTypeBuilder.addLabel(Language.French, "zeLabel");

		build();

		assertThat(schemaType.getLabel(Language.French)).isEqualTo("zeLabel");
	}

	@Test
	public void givenLabelWhenModifyingThenHasCorrectValue()
			throws Exception {
		schemaTypeBuilder.addLabel(Language.French, "zeLabel");

		buildAndModify();

		assertThat(schemaTypeBuilder.getLabel(Language.French)).isEqualTo("zeLabel");
	}

	@Test(expected = MetadataSchemaTypeBuilderRuntimeException.LabelNotDefinedForLanguage.class)
	public void givenLabelEmptyWhenBuildingThenException()
			throws Exception {
		schemaTypeBuilder.addLabel(Language.French, "");

		build();
	}

	@Test
	public void givenDataStoreNotDefinedWhenBuildingThenRecords()
			throws Exception {
		build();

		assertThat(schemaType.getDataStore()).isEqualTo("records");
	}

	@Test
	public void givenDataStoreDefinedToNullWhenBuildingThenRecords()
			throws Exception {
		schemaTypeBuilder.setDataStore(null);
		build();

		assertThat(schemaType.getDataStore()).isEqualTo("records");
	}

	@Test
	public void givenDataStoreDefinedWhenBuildingThenRecords()
			throws Exception {
		schemaTypeBuilder.setDataStore("events");
		build();

		assertThat(schemaType.getDataStore()).isEqualTo("events");
	}

	@Test
	public void givenDataStoreDefinedWhenModifyingThenHasCorrectValue()
			throws Exception {
		schemaTypeBuilder.setDataStore("events");

		buildAndModify();

		assertThat(schemaTypeBuilder.getDataStore()).isEqualTo("events");
	}

	@Test
	public void givenUndeletableStatusNotDefinedWhenBuildingThenDeletable()
			throws Exception {
		build();

		assertThat(schemaType.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusDefinedToFalseWhenBuildingThenDeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(false);

		build();

		assertThat(schemaType.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusDefinedToFalseWhenModifyingThenDeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(false);

		buildAndModify();

		assertThat(schemaTypeBuilder.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusDefinedToTrueWhenBuildingThenUndeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(true);

		build();

		assertThat(schemaType.isUndeletable()).isTrue();
	}

	@Test
	public void givenUndeletableStatusDefinedToTrueWhenModifyingThenUndeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(true);

		buildAndModify();

		assertThat(schemaTypeBuilder.isUndeletable()).isTrue();
	}

	@Test
	public void givenSecurityStatusNotDefinedWhenBuildingThenSecurity()
			throws Exception {
		build();

		assertThat(schemaType.hasSecurity()).isTrue();
	}

	@Test
	public void givenSecurityStatusDefinedToFalseWhenBuildingThenNoSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(false);

		build();

		assertThat(schemaType.hasSecurity()).isFalse();
	}

	@Test
	public void givenSecurityStatusDefinedToFalseWhenModifyingThenNoSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(false);

		buildAndModify();

		assertThat(schemaTypeBuilder.isSecurity()).isFalse();
	}

	@Test
	public void givenSecurityStatusDefinedToTrueWhenBuildingThenSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(true);

		build();

		assertThat(schemaType.hasSecurity()).isTrue();
	}

	@Test
	public void givenSecurityStatusDefinedToTrueWhenModifyingThenSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(true);

		buildAndModify();

		assertThat(schemaTypeBuilder.isSecurity()).isTrue();
	}

	@Test
	public void givenReadOnlyLockedStatusNotDefinedWhenBuildingThenNoReadOnlyLocked()
			throws Exception {
		build();

		assertThat(schemaType.isReadOnlyLocked()).isFalse();
	}

	@Test
	public void givenReadOnlyLockedStatusDefinedToFalseWhenBuildingThenNoReadOnlyLocked()
			throws Exception {
		schemaTypeBuilder.setReadOnlyLocked(false);

		build();

		assertThat(schemaType.isReadOnlyLocked()).isFalse();
	}

	@Test
	public void givenReadOnlyLockedStatusDefinedToFalseWhenModifyingThenNoReadOnlyLocked()
			throws Exception {
		schemaTypeBuilder.setReadOnlyLocked(false);

		buildAndModify();

		assertThat(schemaTypeBuilder.isReadOnlyLocked()).isFalse();
	}

	@Test
	public void givenReadOnlyLockedStatusDefinedToTrueWhenBuildingThenReadOnlyLocked()
			throws Exception {
		schemaTypeBuilder.setReadOnlyLocked(true);

		build();

		assertThat(schemaType.isReadOnlyLocked()).isTrue();
	}

	@Test
	public void givenReadOnlyLockedStatusDefinedToTrueWhenModifyingThenReadOnlyLocked()
			throws Exception {
		schemaTypeBuilder.setReadOnlyLocked(true);

		buildAndModify();

		assertThat(schemaTypeBuilder.isReadOnlyLocked()).isTrue();
	}

	@Test
	public void givenInTransactionLogNotDefinedWhenBuildingThenSetToTrue()
			throws Exception {
		build();

		assertThat(schemaType.isInTransactionLog()).isTrue();
	}

	@Test
	public void givenInTransactionLogDefinedToFalseWhenBuildingThenSaved()
			throws Exception {
		schemaTypeBuilder.setInTransactionLog(false);

		build();

		assertThat(schemaType.isInTransactionLog()).isFalse();
	}

	@Test
	public void givenInTransactionLogDefinedToFalseWhenModifyingThenSaved()
			throws Exception {
		schemaTypeBuilder.setInTransactionLog(false);

		buildAndModify();

		assertThat(schemaTypeBuilder.isInTransactionLog()).isFalse();
	}

	@Test
	public void givenInTransactionLogDefinedToTrueWhenBuildingThenSaved()
			throws Exception {
		schemaTypeBuilder.setInTransactionLog(true);

		build();

		assertThat(schemaType.isInTransactionLog()).isTrue();
	}

	@Test
	public void givenInTransactionLogStatusDefinedToTrueWhenModifyingThenSaved()
			throws Exception {
		schemaTypeBuilder.setInTransactionLog(true);

		buildAndModify();

		assertThat(schemaTypeBuilder.isInTransactionLog()).isTrue();
	}

	@Test
	public void givenDefaultSchemaWithAMetadataWhenBuildingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		build();
		MetadataSchema schema = schemaType.getDefaultSchema();
		Metadata metadata = schema.getMetadata(metadataCode);

		assertThat(metadata.getCode()).isEqualTo(expectedDefaultSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}

	@Test
	public void givenDefaultSchemaWithAMetadataWhenModifyingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		buildAndModify();
		MetadataBuilder metadata = schemaTypeBuilder.getDefaultSchema().getMetadata(metadataCode);

		assertThat(metadata.getCode()).isEqualTo(expectedDefaultSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}

	public void givenNoCustomSchemasWhenModifyingThenCustomSchemasListIsEmpty()
			throws Exception {
		buildAndModify();

		assertThat(schemaTypeBuilder.getCustomSchemas()).isEmpty();
	}

	@Test
	public void givenCustomSchemaWithAMetadataWhenBuildingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode).create(metadataCode).setType(STRING);

		build();
		validateSchemaTypeHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenCustomSchemaWithAMetadataWhenModifyingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode).create(metadataCode).setType(STRING);

		buildAndModify();

		validateSchemaTypeBuilderHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedBeforeACustomSchemaWhenBuildingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);
		schemaTypeBuilder.createCustomSchema(customSchemaCode);

		build();

		validateSchemaTypeHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedBeforeACustomSchemaWhenModyfingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);
		schemaTypeBuilder.createCustomSchema(customSchemaCode);

		buildAndModify();

		validateSchemaTypeBuilderHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedAfterACustomSchemaWhenBuildingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode);
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		build();

		validateSchemaTypeHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedAfterACustomSchemaWhenModyfingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode);
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		buildAndModify();

		validateSchemaTypeBuilderHasCustomSchemaWithMetadata();
	}

	@Test(expected = MetadataSchemaTypeBuilderRuntimeException.NoSuchSchema.class)
	public void givenInexistentCodeWhenGetCustomSchemaThenThrowExcpetion()
			throws Exception {

		buildAndModify();

		schemaTypeBuilder.getCustomSchema(INEXISTENT_CODE);
	}

	@Test
	public void whenCalculatingDependenciesOfASchemaWithTwoDifferentTypeReferencesThenBothAreReturned()
			throws Exception {
		MetadataSchemaBuilder schema = schemaTypeBuilder.getDefaultSchema();
		MetadataSchemaBuilder customSchema = schemaTypeBuilder.createCustomSchema("custom");

		MetadataBuilder defaultSchemaMetadata = schema.create("first").setType(STRING);
		MetadataBuilder customSchemaMetadata = customSchema.create("second").setType(STRING);
		MetadataBuilder customSchemaMetadataInheritingDefaultSchemaMetadata = customSchema.get("first");

		assertThat(schemaTypeBuilder.getAllMetadatas()).contains(defaultSchemaMetadata, customSchemaMetadata)
				.doesNotContain(customSchemaMetadataInheritingDefaultSchemaMetadata);
	}

	private void build() {
		schemaType = schemaTypeBuilder.build(typesFactory, typesBuilder, modelLayerFactory);
	}

	private void buildAndModify() {
		MetadataSchemaType schemaType = schemaTypeBuilder.build(typesFactory, typesBuilder, modelLayerFactory);
		schemaTypeBuilder = (new MetadataSchemaTypeBuilder()).modifySchemaType(schemaType, getModelLayerFactory(), new DefaultClassProvider());
	}

	private void validateSchemaTypeHasCustomSchemaWithMetadata() {
		assertThat(schemaType.hasSchema(customSchemaCode)).isTrue();
		assertThat(schemaType.hasSchema(schemaType.getCode() + "_" + customSchemaCode)).isTrue();
		assertThat(schemaType.hasSchema("invalidSchema")).isFalse();
		try {
			assertThat(schemaType.hasSchema("otherType_" + customSchemaCode)).isTrue();
			fail("Exception expected");
		} catch (MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType e) {
			//OK
		}

		MetadataSchema customSchema = schemaType.getCustomSchema(customSchemaCode);
		Metadata metadata = customSchema.getMetadata(metadataCode);

		assertThat(metadata.getCode()).isEqualTo(expectedCustomSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}

	private void validateSchemaTypeBuilderHasCustomSchemaWithMetadata() {
		MetadataSchemaBuilder customSchemaBuilder = schemaTypeBuilder.getCustomSchema(customSchemaCode);
		MetadataBuilder metadata = customSchemaBuilder.getMetadata(metadataCode);

		assertThat(schemaTypeBuilder.getCustomSchemas()).hasSize(1);
		assertThat(metadata.getCode()).isEqualTo(expectedCustomSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}
}
