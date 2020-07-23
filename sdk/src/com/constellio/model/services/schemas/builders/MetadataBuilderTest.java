package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataBuilderTest extends ConstellioTest {

	static final CollectionInfo COLLECTION = new CollectionInfo((byte) 0, "zeUltimateCollection", "fr", asList("fr"));
	static final String CODE_DEFAULT_METADATA = "codeMetadata";
	static final String DEFAULT_SCHEMA_CODE = "codeSchema_default";
	static final String CUSTOM_SCHEMA_CODE = "codeSchema_codeSchema";
	static final MetadataValueType VALUE_TYPE = MetadataValueType.NUMBER;
	static final boolean ENABLE = true;
	static final boolean DISABLE = false;

	@Mock MetadataSchemaTypesBuilder typesBuilder;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock ModelLayerFactory modelLayerFactory;
	DataStoreTypesFactory typesFactory = new FakeDataStoreTypeFactory();
	MetadataBuilder metadataWithInheritanceBuilder, metadataWithoutInheritanceBuilder, inheritedMetadataBuilder;
	MetadataBuilder anotherSchemaMetadataBuilder, referenceOtherSchemaMetadataBuilder;
	Metadata metadataWithInheritance, metadataWithoutInheritance, inheritedMetadata;

	MetadataSchemaTypeBuilder schemaTypeBuilder, anotherSchemaTypeBuilder;
	MetadataSchemaBuilder schemaBuilder, anotherSchemaBuilder;

	String anotherSchemaTypeCompleteCode;
	String anotherSchemaCompleteCode;
	String anotherSchemaMetadataCompleteCode;

	@Before
	public void setup() {
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);
		when(typesBuilder.getClassProvider()).thenReturn(new DefaultClassProvider());
		when(typesBuilder.getLanguages()).thenReturn(asList(Language.French));
		schemaTypeBuilder = (new MetadataSchemaTypeBuilder()).createNewSchemaType(COLLECTION, "codeSchema", typesBuilder, modelLayerFactory);
		schemaBuilder = schemaTypeBuilder.getDefaultSchema();

		anotherSchemaTypeBuilder = (new MetadataSchemaTypeBuilder()).createNewSchemaType(COLLECTION, "anotherSchemaType", typesBuilder, modelLayerFactory);
		anotherSchemaBuilder = anotherSchemaTypeBuilder.getDefaultSchema();
		anotherSchemaMetadataBuilder = MetadataBuilder.createMetadataWithoutInheritance("anotherSchemaMetadata",
				anotherSchemaBuilder).setType(MetadataValueType.STRING);

		anotherSchemaTypeCompleteCode = anotherSchemaTypeBuilder.getCode();
		anotherSchemaCompleteCode = anotherSchemaBuilder.getCode();
		anotherSchemaMetadataCompleteCode = anotherSchemaMetadataBuilder.getCode();

		metadataWithoutInheritanceBuilder = MetadataBuilder.createMetadataWithoutInheritance(CODE_DEFAULT_METADATA,
				schemaBuilder);
		inheritedMetadataBuilder = metadataWithoutInheritanceBuilder;
		metadataWithInheritanceBuilder = MetadataBuilder
				.createCustomMetadataFromDefault(schemaBuilder, inheritedMetadataBuilder, "codeSchema");

		referenceOtherSchemaMetadataBuilder = MetadataBuilder.createMetadataWithoutInheritance("ref", schemaBuilder);
		referenceOtherSchemaMetadataBuilder.defineReferences().set(anotherSchemaTypeBuilder);

	}

	protected void build() {
		metadataWithoutInheritance = metadataWithoutInheritanceBuilder.buildWithoutInheritance(typesFactory, schemaTypeBuilder, (short) 42, modelLayerFactory);
		inheritedMetadata = metadataWithoutInheritance;
		metadataWithInheritance = metadataWithInheritanceBuilder.buildWithInheritance(inheritedMetadata);
	}

	protected void buildAndModify() {
		Metadata inheritedMetadata = metadataWithoutInheritanceBuilder.buildWithoutInheritance(typesFactory, schemaTypeBuilder, (short) 42, modelLayerFactory);
		Metadata metadataWithInheritance = metadataWithInheritanceBuilder.buildWithInheritance(inheritedMetadata);
		ClassProvider classProvider = new DefaultClassProvider();
		metadataWithoutInheritanceBuilder = MetadataBuilder
				.modifyMetadataWithoutInheritance(schemaBuilder, inheritedMetadata, classProvider);
		inheritedMetadataBuilder = metadataWithoutInheritanceBuilder;
		metadataWithInheritanceBuilder = MetadataBuilder.modifyMetadataWithInheritance(schemaBuilder, metadataWithInheritance,
				inheritedMetadataBuilder);
	}

}
