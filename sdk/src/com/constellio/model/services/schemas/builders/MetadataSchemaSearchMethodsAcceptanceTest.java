package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class MetadataSchemaSearchMethodsAcceptanceTest extends ConstellioTest {

	static String givenBuilder = "givenBuilder";
	static String givenFinalEnity = "givenFinalEnity";
	static String givenBuilderObtainedFromManager = "givenBuilderObtainedFromManager";
	static String givenFinalEnityObtainedFromManager = "givenFinalEnityObtainedFromManager";
	@Mock DataStoreTypesFactory typesFactory;
	boolean builderMode;
	String testCase;

	MetadataSchemaTypesBuilder typesBuilder;
	MetadataSchemaTypes types;

	public MetadataSchemaSearchMethodsAcceptanceTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{{givenBuilder}, {givenFinalEnity}, {givenBuilderObtainedFromManager},
											{givenFinalEnityObtainedFromManager}});
	}

	@SuppressWarnings("unused")
	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection);

		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();

		typesBuilder = (new MetadataSchemaTypesBuilder(manager.getSchemaTypes("zeCollection").getCollectionInfo()))
				.modify(manager.getSchemaTypes("zeCollection"), getModelLayerFactory(), new DefaultClassProvider());

		MetadataSchemaTypeBuilder type1Builder = typesBuilder.getOrCreateNewSchemaType("type1")
				.addLabel(Language.French, "type1");
		MetadataSchemaTypeBuilder type2Builder = typesBuilder.getOrCreateNewSchemaType("type2")
				.addLabel(Language.French, "type2");
		MetadataSchemaBuilder type1Default = type1Builder.getDefaultSchema().addLabel(Language.French, "schemaDefault1");
		MetadataSchemaBuilder type1Schema1Builder = type1Builder.createCustomSchema("schema1")
				.addLabel(Language.French, "schema1");
		MetadataSchemaBuilder type1Schema2Builder = type1Builder.createCustomSchema("schema2")
				.addLabel(Language.French, "schema2");
		MetadataSchemaBuilder type2Default = type2Builder.getDefaultSchema().addLabel(Language.French, "schemaDefault2");
		MetadataSchemaBuilder type2Schema1Builder = type2Builder.createCustomSchema("schema1")
				.addLabel(Language.French, "schema1");
		MetadataSchemaBuilder type2Schema2Builder = type2Builder.createCustomSchema("schema2")
				.addLabel(Language.French, "schema2");
		MetadataBuilder type1DefaultSchemaMetadata1Builder = newMetadata(type1Default, "metadata1");
		MetadataBuilder type1DefaultSchemaMetadata2Builder = newMetadata(type1Default, "metadata2");
		MetadataBuilder type1Schema1Metadata1Builder = newMetadata(type1Schema1Builder, "customMetadata11");
		MetadataBuilder type1Schema1Metadata2Builder = newMetadata(type1Schema1Builder, "customMetadata12");
		MetadataBuilder type1Schema2Metadata1Builder = newMetadata(type1Schema2Builder, "customMetadata21");
		MetadataBuilder type1Schema2Metadata2Builder = newMetadata(type1Schema2Builder, "customMetadata22");
		MetadataBuilder type2DefaultSchemaMetadata1Builder = newMetadata(type2Default, "metadata1");
		MetadataBuilder type2DefaultSchemaMetadata2Builder = newMetadata(type2Default, "metadata2");
		MetadataBuilder type2Schema1Metadata1Builder = newMetadata(type2Schema1Builder, "customMetadata11");
		MetadataBuilder type2Schema1Metadata2Builder = newMetadata(type2Schema1Builder, "customMetadata12");
		MetadataBuilder type2Schema2Metadata1Builder = newMetadata(type2Schema2Builder, "customMetadata21");
		MetadataBuilder type2Schema2Metadata2Builder = newMetadata(type2Schema2Builder, "customMetadata22");

		types = typesBuilder.build(typesFactory);

		if (testCase.equals(givenBuilderObtainedFromManager) || testCase.equals(givenFinalEnityObtainedFromManager)) {
			manager.saveUpdateSchemaTypes(typesBuilder);
			types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes("zeCollection");
			typesBuilder = (new MetadataSchemaTypesBuilder(manager.getSchemaTypes("zeCollection").getCollectionInfo()))
					.modify(types, getModelLayerFactory(), new DefaultClassProvider());
		}
		builderMode = testCase.equals(givenBuilder) || testCase.equals(givenBuilderObtainedFromManager);
	}

	private MetadataBuilder newMetadata(MetadataSchemaBuilder schema, String code) {
		return schema.create(code).setType(MetadataValueType.STRING).setDefaultRequirement(true);
	}

	@Test
	public void whenSearchingTypeFromTypesThenFindIt()
			throws Exception {

		validateCanFindSchemaTypeFromSchemaTypes("type1");
		validateCanFindSchemaTypeFromSchemaTypes("type2");
		validateCannotFindSchemaTypeFromSchemaTypes("type3");
		validateCannotFindSchemaTypeFromSchemaTypes("type2_schema3");
		validateCannotFindSchemaTypeFromSchemaTypes("type2_schema2_customMetadata22");

	}

	@Test
	public void whenSearchingSchemaFromTypesThenFindIt()
			throws Exception {
		validateCanFindSchemaFromSchemaTypes("type1_default");
		validateCanFindSchemaFromSchemaTypes("type1_schema1");
		validateCanFindSchemaFromSchemaTypes("type1_schema2");
		validateCanFindSchemaFromSchemaTypes("type2_default");
		validateCanFindSchemaFromSchemaTypes("type2_schema1");
		validateCanFindSchemaFromSchemaTypes("type2_schema2");
		validateCannotFindSchemaFromSchemaTypes("type2");
		validateCannotFindSchemaFromSchemaTypes("type2_schema3");
		validateCannotFindSchemaFromSchemaTypes("type2_schema2_customMetadata22");
	}

	@Test
	public void whenSearchingMetadataFromTypesThenFindIt()
			throws Exception {
		validateCanFindMetadataFromSchemaTypes("type1_default_metadata1");
		validateCanFindMetadataFromSchemaTypes("type1_default_metadata2");
		validateCanFindMetadataFromSchemaTypes("type1_schema1_metadata1");
		validateCanFindMetadataFromSchemaTypes("type1_schema1_metadata2");
		validateCanFindMetadataFromSchemaTypes("type1_schema2_metadata1");
		validateCanFindMetadataFromSchemaTypes("type1_schema2_metadata2");
		validateCanFindMetadataFromSchemaTypes("type1_schema1_customMetadata11");
		validateCanFindMetadataFromSchemaTypes("type1_schema1_customMetadata12");
		validateCanFindMetadataFromSchemaTypes("type1_schema2_customMetadata21");
		validateCanFindMetadataFromSchemaTypes("type1_schema2_customMetadata22");
		validateCanFindMetadataFromSchemaTypes("type2_default_metadata1");
		validateCanFindMetadataFromSchemaTypes("type2_default_metadata2");
		validateCanFindMetadataFromSchemaTypes("type2_schema1_metadata1");
		validateCanFindMetadataFromSchemaTypes("type2_schema1_metadata2");
		validateCanFindMetadataFromSchemaTypes("type2_schema2_metadata1");
		validateCanFindMetadataFromSchemaTypes("type2_schema2_metadata2");
		validateCanFindMetadataFromSchemaTypes("type2_schema1_customMetadata11");
		validateCanFindMetadataFromSchemaTypes("type2_schema1_customMetadata12");
		validateCanFindMetadataFromSchemaTypes("type2_schema2_customMetadata21");
		validateCanFindMetadataFromSchemaTypes("type2_schema2_customMetadata22");
		validateCannotFindMetadataFromSchemaTypes("type2");
		validateCannotFindMetadataFromSchemaTypes("type2_schema2");
		validateCannotFindMetadataFromSchemaTypes("type2_schema2_customMetadata3");
	}

	@Test
	public void whenSearchingSchemaFromTypeThenFindIt()
			throws Exception {

		validateCanFindCustomSchemaFromSchemaType("type1_default", "type1");
		validateCanFindCustomSchemaFromSchemaType("default", "type1");
		validateCanFindCustomSchemaFromSchemaType("type1_schema1", "type1");
		validateCanFindCustomSchemaFromSchemaType("schema1", "type1");
		validateCanFindCustomSchemaFromSchemaType("type1_schema2", "type1");
		validateCanFindCustomSchemaFromSchemaType("schema2", "type1");
		validateCannotFindCustomSchemaFromSchemaType("type2_default", "type1");
		validateCannotFindCustomSchemaFromSchemaType("type2_schema1", "type1");
		validateCannotFindCustomSchemaFromSchemaType("type2_schema2", "type1");
		validateCannotFindCustomSchemaFromSchemaType("type1_default", "type2");
		validateCannotFindCustomSchemaFromSchemaType("type1_schema1", "type2");
		validateCannotFindCustomSchemaFromSchemaType("type1_schema2", "type2");
		validateCanFindCustomSchemaFromSchemaType("type2_default", "type2");
		validateCanFindCustomSchemaFromSchemaType("default", "type2");
		validateCanFindCustomSchemaFromSchemaType("type2_schema1", "type2");
		validateCanFindCustomSchemaFromSchemaType("schema1", "type2");
		validateCanFindCustomSchemaFromSchemaType("type2_schema2", "type2");
		validateCanFindCustomSchemaFromSchemaType("schema2", "type2");

	}

	@Test
	public void whenSearchingMetadataFromTypeThenFindIt()
			throws Exception {

		validateCanFindMetadataFromSchemaType("type1_default_metadata1", "type1");
		validateCanFindMetadataFromSchemaType("type1_default_metadata2", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema1_metadata1", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema1_metadata2", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema2_metadata1", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema2_metadata2", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema1_customMetadata11", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema1_customMetadata12", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema2_customMetadata21", "type1");
		validateCanFindMetadataFromSchemaType("type1_schema2_customMetadata22", "type1");
		validateCannotFindMetadataFromSchemaType("type2_default_metadata1", "type1");
		validateCannotFindMetadataFromSchemaType("type2_default_metadata2", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema1_metadata1", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema1_metadata2", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema2_metadata1", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema2_metadata2", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema1_customMetadata11", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema1_customMetadata12", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema2_customMetadata21", "type1");
		validateCannotFindMetadataFromSchemaType("type2_schema2_customMetadata22", "type1");
		validateCannotFindMetadataFromSchemaType("type1_default_metadata1", "type2");
		validateCannotFindMetadataFromSchemaType("type1_default_metadata2", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema1_metadata1", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema1_metadata2", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema2_metadata1", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema2_metadata2", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema1_customMetadata11", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema1_customMetadata12", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema2_customMetadata21", "type2");
		validateCannotFindMetadataFromSchemaType("type1_schema2_customMetadata22", "type2");
		validateCanFindMetadataFromSchemaType("type2_default_metadata1", "type2");
		validateCanFindMetadataFromSchemaType("type2_default_metadata2", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema1_metadata1", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema1_metadata2", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema2_metadata1", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema2_metadata2", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema1_customMetadata11", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema1_customMetadata12", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema2_customMetadata21", "type2");
		validateCanFindMetadataFromSchemaType("type2_schema2_customMetadata22", "type2");
		validateCannotFindMetadataFromSchemaTypes("schema3");
		validateCannotFindMetadataFromSchemaTypes("type2_schema3");
		validateCannotFindMetadataFromSchemaTypes("type2");
		validateCannotFindMetadataFromSchemaTypes("type2_schema2_customMetadata3");
	}

	@Test
	public void whenSearchingMetadataFromSchemaThenFindIt()
			throws Exception {

		validateCanFindMetadataFromSchema("type1_default_metadata1", "type1_default");
		validateCanFindMetadataFromSchema("metadata1", "type1_default");
		validateCanFindMetadataFromSchema("type1_default_metadata2", "type1_default");
		validateCanFindMetadataFromSchema("metadata2", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata1", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata2", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata1", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata2", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata11", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata12", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata21", "type1_default");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata22", "type1_default");
		validateCannotFindMetadataFromSchema("type2_default_metadata1", "type1_default");
		validateCannotFindMetadataFromSchema("type2_default_metadata2", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata1", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata2", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata1", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata2", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata11", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata12", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata21", "type1_default");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata22", "type1_default");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata1", "type1_schema1",
				"type1_schema1_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata1", "type1_schema1",
				"type1_schema1_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata2", "type1_schema1",
				"type1_schema1_metadata2");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata2", "type1_schema1",
				"type1_schema1_metadata2");
		validateCanFindMetadataFromSchema("type1_schema1_metadata1", "type1_schema1");
		validateCanFindMetadataFromSchema("metadata1", "type1_schema1");
		validateCanFindMetadataFromSchema("type1_schema1_metadata2", "type1_schema1");
		validateCanFindMetadataFromSchema("metadata2", "type1_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata1", "type1_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata2", "type1_schema1");
		validateCanFindMetadataFromSchema("type1_schema1_customMetadata11", "type1_schema1");
		validateCanFindMetadataFromSchema("customMetadata11", "type1_schema1");
		validateCanFindMetadataFromSchema("type1_schema1_customMetadata12", "type1_schema1");
		validateCanFindMetadataFromSchema("customMetadata12", "type1_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata21", "type1_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata22", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_default_metadata1", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_default_metadata2", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata1", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata2", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata1", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata2", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata11", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata12", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata21", "type1_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata22", "type1_schema1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata1", "type1_schema2",
				"type1_schema2_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata1", "type1_schema2",
				"type1_schema2_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata2", "type1_schema2",
				"type1_schema2_metadata2");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type1_default_metadata2", "type1_schema2",
				"type1_schema2_metadata2");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata1", "type1_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata2", "type1_schema2");
		validateCanFindMetadataFromSchema("type1_schema2_metadata1", "type1_schema2");
		validateCanFindMetadataFromSchema("metadata1", "type1_schema2");
		validateCanFindMetadataFromSchema("type1_schema2_metadata2", "type1_schema2");
		validateCanFindMetadataFromSchema("metadata2", "type1_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata11", "type1_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata12", "type1_schema2");
		validateCanFindMetadataFromSchema("type1_schema2_customMetadata21", "type1_schema2");
		validateCanFindMetadataFromSchema("customMetadata21", "type1_schema2");
		validateCanFindMetadataFromSchema("type1_schema2_customMetadata22", "type1_schema2");
		validateCanFindMetadataFromSchema("customMetadata22", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_default_metadata1", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_default_metadata2", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata1", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata2", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata1", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata2", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata11", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata12", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata21", "type1_schema2");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata22", "type1_schema2");
		validateCannotFindMetadataFromSchema("type1_default_metadata1", "type2_default");
		validateCannotFindMetadataFromSchema("type1_default_metadata2", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata1", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata2", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata1", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata2", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata11", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata12", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata21", "type2_default");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata22", "type2_default");
		validateCanFindMetadataFromSchema("type2_default_metadata1", "type2_default");
		validateCanFindMetadataFromSchema("metadata1", "type2_default");
		validateCanFindMetadataFromSchema("type2_default_metadata2", "type2_default");
		validateCanFindMetadataFromSchema("metadata2", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata1", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata2", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata1", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata2", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata11", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata12", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata21", "type2_default");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata22", "type2_default");
		validateCannotFindMetadataFromSchema("type1_default_metadata1", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_default_metadata2", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata1", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata2", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata1", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata2", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata11", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata12", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata21", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata22", "type2_schema1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata1", "type2_schema1",
				"type2_schema1_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata1", "type2_schema1",
				"type2_schema1_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata2", "type2_schema1",
				"type2_schema1_metadata2");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata2", "type2_schema1",
				"type2_schema1_metadata2");
		validateCanFindMetadataFromSchema("type2_schema1_metadata1", "type2_schema1");
		validateCanFindMetadataFromSchema("metadata1", "type2_schema1");
		validateCanFindMetadataFromSchema("type2_schema1_metadata2", "type2_schema1");
		validateCanFindMetadataFromSchema("metadata2", "type2_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata1", "type2_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_metadata2", "type2_schema1");
		validateCanFindMetadataFromSchema("type2_schema1_customMetadata11", "type2_schema1");
		validateCanFindMetadataFromSchema("customMetadata11", "type2_schema1");
		validateCanFindMetadataFromSchema("type2_schema1_customMetadata12", "type2_schema1");
		validateCanFindMetadataFromSchema("customMetadata12", "type2_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata21", "type2_schema1");
		validateCannotFindMetadataFromSchema("type2_schema2_customMetadata22", "type2_schema1");
		validateCannotFindMetadataFromSchema("type1_default_metadata1", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_default_metadata2", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata1", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_metadata2", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata1", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema2_metadata2", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata11", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema1_customMetadata12", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata21", "type2_schema2");
		validateCannotFindMetadataFromSchema("type1_schema2_customMetadata22", "type2_schema2");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata1", "type2_schema2",
				"type2_schema2_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata1", "type2_schema2",
				"type2_schema2_metadata1");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata2", "type2_schema2",
				"type2_schema2_metadata2");
		validateCanFindMetadataFromSchemaAndMetadataHasCode("type2_default_metadata2", "type2_schema2",
				"type2_schema2_metadata2");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata1", "type2_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_metadata2", "type2_schema2");
		validateCanFindMetadataFromSchema("type2_schema2_metadata1", "type2_schema2");
		validateCanFindMetadataFromSchema("metadata1", "type2_schema2");
		validateCanFindMetadataFromSchema("type2_schema2_metadata2", "type2_schema2");
		validateCanFindMetadataFromSchema("metadata2", "type2_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata11", "type2_schema2");
		validateCannotFindMetadataFromSchema("type2_schema1_customMetadata12", "type2_schema2");
		validateCanFindMetadataFromSchema("type2_schema2_customMetadata21", "type2_schema2");
		validateCanFindMetadataFromSchema("customMetadata21", "type2_schema2");
		validateCanFindMetadataFromSchema("type2_schema2_customMetadata22", "type2_schema2");
		validateCanFindMetadataFromSchema("customMetadata22", "type2_schema2");

	}

	private void validateCannotFindSchemaTypeFromSchemaTypes(String typeName) {
		//		System.out.println("validateCannotFindSchemaTypeFromSchemaTypes(\"" + typeName + "\");");

		try {
			validateCanFindSchemaTypeFromSchemaTypes(typeName);
			fail("An exception was expected");
		} catch (Exception e) {

		}
	}

	private void validateCanFindSchemaTypeFromSchemaTypes(String typeName)
			throws Exception {
		//		System.out.println("validateCanFindSchemaTypeFromSchemaTypes(\"" + typeName + "\");");

		if (builderMode) {
			MetadataSchemaTypeBuilder typeBuilder = typesBuilder.getSchemaType(typeName);
			assertThat(typeBuilder.getCode()).isEqualTo(typeName);
		} else {
			MetadataSchemaType type = types.getSchemaType(typeName);
			assertThat(type.getCode()).isEqualTo(typeName);
		}
	}

	private void validateCannotFindSchemaFromSchemaTypes(String code) {
		//		System.out.println("validateCannotFindSchemaFromSchemaTypes(\"" + code + "\");");
		try {
			validateCanFindSchemaFromSchemaTypes(code);
			fail("An exception was expected");
		} catch (Exception e) {

		}
	}

	private void validateCanFindSchemaFromSchemaTypes(String code)
			throws Exception {
		//		System.out.println("validateCanFindSchemaFromSchemaTypes(\"" + code + "\");");
		if (builderMode) {
			MetadataSchemaBuilder schema = typesBuilder.getSchema(code);
			assertTrue("Invalid schema : " + schema.getCode(), schema.getCode().equals(code)
															   || schema.getCode().equals(code));
		} else {
			MetadataSchema schema = types.getSchema(code);
			assertTrue(schema.getCode().equals(code) || schema.getCode().equals(code));
		}
	}

	private void validateCannotFindMetadataFromSchemaTypes(String code) {
		//		System.out.println("validateCannotFindMetadataFromSchemaTypes(\"" + code + "\");");
		try {
			validateCanFindMetadataFromSchemaTypes(code);
			fail("An exception was expected");
		} catch (Exception e) {

		}
	}

	private void validateCanFindMetadataFromSchemaTypes(String code)
			throws Exception {
		//		System.out.println("validateCanFindMetadataFromSchemaTypes(\"" + code + "\");");
		if (builderMode) {
			MetadataBuilder metadata = typesBuilder.getMetadata(code);
			assertThat(metadata.getCode()).isEqualTo(code);

		} else {
			Metadata metadata = types.getMetadata(code);
			assertThat(metadata.getCode()).isEqualTo(code);
		}
	}

	private void validateCannotFindCustomSchemaFromSchemaType(String codeOrCode, String type) {
		//		System.out.println("validateCannotFindCustomSchemaFromSchemaType(\"" + codeOrCode + "\", \"" + type + "\");");
		try {
			validateCanFindCustomSchemaFromSchemaType(codeOrCode, type);
			fail("An exception was expected");
		} catch (Exception e) {

		}
	}

	//@formatter:off
	private void validateCanFindCustomSchemaFromSchemaType(String codeOrCode, String type)
			throws Exception {
		//		System.out.println("validateCanFindCustomSchemaFromSchemaType(\"" + codeOrCode + "\", \"" + type + "\");");
		if (builderMode) {
			MetadataSchemaTypeBuilder schemaTypeBuilder = typesBuilder.getSchemaType(type);
			MetadataSchemaBuilder schemaBuilder = schemaTypeBuilder.getSchema(codeOrCode);
			assertTrue("Unexpected schema : " + schemaBuilder.getCode() + " in type " + type + ", expected : "
					   + codeOrCode, schemaBuilder.getLocalCode().equals(codeOrCode)
									 || schemaBuilder.getCode().equals(codeOrCode));
		} else {
			MetadataSchemaType schemaType = types.getSchemaType(type);
			MetadataSchema schemaBuilder = schemaType.getSchema(codeOrCode);
			assertTrue("Unexpected schema : " + schemaBuilder.getCode() + " in type " + type + ", expected : "
					   + codeOrCode, schemaBuilder.getLocalCode().equals(codeOrCode)
									 || schemaBuilder.getCode().equals(codeOrCode));
		}
	}
	//@formatter:on

	private void validateCannotFindMetadataFromSchemaType(String codeOrCode, String type) {
		//		System.out.println("validateCannotFindMetadataFromSchemaType(\"" + codeOrCode + "\", \"" + type + "\");");
		try {
			validateCanFindMetadataFromSchemaType(codeOrCode, type);
			fail("An exception was expected");
		} catch (Exception e) {

		}
	}

	private void validateCanFindMetadataFromSchemaType(String code, String type)
			throws Exception {
		//		System.out.println("validateCanFindMetadataFromSchemaType(\"" + code + "\", \"" + type + "\");");
		if (builderMode) {
			MetadataSchemaTypeBuilder schemaTypeBuilder = typesBuilder.getSchemaType(type);
			MetadataBuilder metadata = schemaTypeBuilder.getMetadata(code);
			assertThat(metadata.getCode()).isEqualTo(code);
		} else {
			MetadataSchemaType schemaType = types.getSchemaType(type);
			Metadata metadata = schemaType.getMetadata(code);
			try {
				assertThat(metadata.getCode()).isEqualTo(code);
			} catch (RuntimeException e) {
				System.out.print("oh bobo");
			}
		}
	}

	private void validateCannotFindMetadataFromSchema(String codeOrCode, String type) {
		//		System.out.println("validateCannotFindMetadataFromSchema(\"" + codeOrCode + "\", \"" + type + "\");");
		try {
			validateCanFindMetadataFromSchema(codeOrCode, type);
			fail("An exception was expected");
		} catch (Exception e) {

		}
	}

	private void validateCanFindMetadataFromSchema(String codeOrCode, String schemaCode)
			throws Exception {
		validateCanFindMetadataFromSchemaAndMetadataHasCode(codeOrCode, schemaCode, codeOrCode);
	}

	private void validateCanFindMetadataFromSchemaAndMetadataHasCode(String codeOrCode, String schemaCode,
																	 String expectedCode)
			throws Exception {
		//		System.out.println("validateCanFindMetadataFromSchema(\"" + codeOrCode + "\", \"" + schemaCode + "\");");
		if (builderMode) {
			MetadataSchemaBuilder schemaBuilder = typesBuilder.getSchema(schemaCode);
			MetadataBuilder metadata = schemaBuilder.getMetadata(codeOrCode);

			if (expectedCode.contains("_")) {
				assertThat(metadata.getCode()).isEqualTo(expectedCode);
			} else {
				assertThat(metadata.getLocalCode()).isEqualTo(expectedCode);
			}

		} else {
			MetadataSchema schema = types.getSchema(schemaCode);
			assertEquals(schemaCode, schema.getCode());
			Metadata metadata = schema.getMetadata(codeOrCode);
			if (expectedCode.contains("_")) {
				if (!metadata.getCode().equals(expectedCode)) {
					throw new RuntimeException("Expected code not found");
				}
			} else {
				assertThat(metadata.getLocalCode()).isEqualTo(expectedCode);
			}

		}
	}

	private List<String> allMetadataCompleteNames() {
		List<String> names = new ArrayList<String>();
		names.add("type1_default_metadata1");
		names.add("type1_default_metadata2");
		names.add("type1_schema1_metadata1");
		names.add("type1_schema1_metadata2");
		names.add("type1_schema2_metadata1");
		names.add("type1_schema2_metadata2");
		names.add("type1_schema1_customMetadata11");
		names.add("type1_schema1_customMetadata12");
		names.add("type1_schema2_customMetadata21");
		names.add("type1_schema2_customMetadata22");
		names.add("type2_default_metadata1");
		names.add("type2_default_metadata2");
		names.add("type2_schema1_metadata1");
		names.add("type2_schema1_metadata2");
		names.add("type2_schema2_metadata1");
		names.add("type2_schema2_metadata2");
		names.add("type2_schema1_customMetadata11");
		names.add("type2_schema1_customMetadata12");
		names.add("type2_schema2_customMetadata21");
		names.add("type2_schema2_customMetadata22");

		return names;
	}

	private List<String> allMetadataSchemaCompleteNames() {
		List<String> names = new ArrayList<String>();
		names.add("type1_default");
		names.add("type1_schema1");
		names.add("type1_schema2");
		names.add("type2_default");
		names.add("type2_schema1");
		names.add("type2_schema2");
		return names;
	}

	private List<String> allMetadataSchemaTypesCompleteNames() {
		List<String> names = new ArrayList<String>();
		names.add("type1");
		names.add("type2");
		return names;
	}

}
