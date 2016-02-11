package com.constellio.model.services.schemas.builders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.constellio.model.utils.DefaultClassProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataSchemaBuildersAcceptanceTest extends ConstellioTest {

	private static final String theMetadataCode = "zeMetadata";
	private static final String theCustomSchemaCode = "zeCustomSchema";

	@Mock MetadataSchemaTypesBuilder typesBuilder;
	@Mock DataStoreTypesFactory typesFactory;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock ModelLayerFactory modelLayerFactory;

	@Before
	public void setUp()
			throws Exception {
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(typesBuilder.getClassProvider()).thenReturn(new DefaultClassProvider());
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);

	}

	@Test
	public void whenCustomMetadataBuildThenInheritFromSameMetadataInstanceAndSameValuesThanDefaultSchema()
			throws Exception {

		MetadataSchemaTypeBuilder typeBuilder = createMetadataSchemaTypeBuilder();

		assertThat(typeBuilder.getDefaultSchema().getMetadata("zeMetadata").getCode()).isEqualTo(
				"zetype_default_zeMetadata");
		assertThat(typeBuilder.getCustomSchema("zeCustomSchema").getMetadata("zeMetadata").getCode()).isEqualTo(
				"zetype_zeCustomSchema_zeMetadata");

		MetadataSchemaType metadataSchemaType = typeBuilder.build(typesFactory, modelLayerFactory);

		Metadata defaultMetadata = metadataSchemaType.getDefaultSchema().getMetadata(theMetadataCode);

		Metadata customMetadata = metadataSchemaType.getCustomSchema(theCustomSchemaCode).getMetadata(theMetadataCode);

		assertSameInheritance(defaultMetadata, customMetadata);
	}

	@Test
	public void whenCreateCustomSchemaThenInheritAllMetadata()
			throws Exception {

		MetadataSchemaTypeBuilder typeBuilder = createMetadataSchemaTypeBuilder();

		assertZeMetadataCreatedAndInherited(typeBuilder);

		MetadataSchemaType metadataSchemaType = typeBuilder.build(typesFactory, modelLayerFactory);

		Metadata defaultMetadata = metadataSchemaType.getDefaultSchema().getMetadata(theMetadataCode);
		Metadata customMetadata = metadataSchemaType.getCustomSchema(theCustomSchemaCode).getMetadata(theMetadataCode);

		assertSameInheritance(defaultMetadata, customMetadata);
	}

	@Test
	public void whenAddingMetadataToDefaultSchemaThenCustomInheritsNewMetadata()
			throws Exception {

		MetadataSchemaTypeBuilder typeBuilder = createMetadataSchemaTypeBuilder();

		assertZeMetadataCreatedAndInherited(typeBuilder);

		MetadataSchemaType metadataSchemaType = typeBuilder.build(typesFactory, modelLayerFactory);

		Metadata defaultMetadata = metadataSchemaType.getDefaultSchema().getMetadata(theMetadataCode);
		Metadata customMetadata = metadataSchemaType.getCustomSchema(theCustomSchemaCode).getMetadata(theMetadataCode);

		assertSameInheritance(defaultMetadata, customMetadata);

		typeBuilder.getDefaultSchema().create("newMetadata").setType(MetadataValueType.TEXT);
		metadataSchemaType = typeBuilder.build(typesFactory, modelLayerFactory);

		metadataSchemaType.getSchema(theCustomSchemaCode).hasMetadataWithCode("newMetadata");
	}

	public MetadataSchemaTypeBuilder createMetadataSchemaTypeBuilder() {
		MetadataSchemaTypeBuilder typeBuilder = MetadataSchemaTypeBuilder
				.createNewSchemaType(zeCollection, "zetype", typesBuilder);
		typeBuilder.getDefaultSchema().create(theMetadataCode).setLabel("zeMetadata")
				.setType(MetadataValueType.STRING);
		typeBuilder.createCustomSchema(theCustomSchemaCode);
		return typeBuilder;
	}

	public void assertZeMetadataCreatedAndInherited(MetadataSchemaTypeBuilder typeBuilder) {
		assertThat(typeBuilder.getMetadata("zetype_default_zeMetadata").getCode()).isEqualTo("zetype_default_zeMetadata");
		assertThat(typeBuilder.getMetadata("zetype_zeCustomSchema_zeMetadata").getCode()).isEqualTo(
				"zetype_zeCustomSchema_zeMetadata");
	}

	public void assertSameInheritance(Metadata defaultMetadata, Metadata customMetadata) {
		assertThat(customMetadata.getInheritance()).isSameAs(defaultMetadata);
		assertThat(defaultMetadata.getCode()).isEqualTo("zetype_default_zeMetadata");
		assertThat(customMetadata.getCode()).isEqualTo("zetype_zeCustomSchema_zeMetadata");
	}
}
