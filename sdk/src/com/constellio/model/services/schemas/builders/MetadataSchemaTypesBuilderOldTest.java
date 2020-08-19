package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MetadataSchemaTypesBuilderOldTest extends ConstellioTest {

	static final String UNDERSCORE = "_";
	static final String DEFAULT = "default";
	static final String INEXISTENT_CODE = "inexistentCode";
	static final String FOLDER = "folder";
	static int VERSION = 1;

	@Mock DataStoreTypesFactory typesFactory;
	MetadataSchemaTypesBuilder typesBuilder;
	MetadataSchemaTypesBuilder typesBuilder2;
	MetadataSchemaTypeBuilder folderTypeBuilder;
	MetadataSchemaTypeBuilder newTypeBuilder;
	MetadataSchemaTypes schemaTypes;
	MetadataSchemaBuilder employeeFolderSchemaBuilder;
	MetadataBuilder employeeName;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock ModelLayerFactory modelLayerFactory;

	@Before
	public void setup()
			throws Exception {
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		typesBuilder = (new MetadataSchemaTypesBuilder(zeCollectionInfo)).createWithVersion(zeCollectionInfo, modelLayerFactory, VERSION,
				new DefaultClassProvider(), Arrays.asList(Language.French));
		folderTypeBuilder = typesBuilder.createNewSchemaType(FOLDER);
		schemaTypes = typesBuilder.build(typesFactory);
		typesBuilder2 = (new MetadataSchemaTypesBuilder(zeCollectionInfo)).modify(schemaTypes, modelLayerFactory, new DefaultClassProvider());
		folderTypeBuilder.getDefaultSchema().create("zetitle");
		employeeFolderSchemaBuilder = folderTypeBuilder.createCustomSchema("employeeFolder");
		employeeName = employeeFolderSchemaBuilder.create("employeeName");
	}

	@Test
	public void whenCreateWithVersionThenItsIsCreatedWithVersion()
			throws Exception {

		assertThat(typesBuilder.getVersion()).isEqualTo(VERSION);
	}

	@Test
	public void whenCreateNewSchemaTypeThenItIsCanBeRetrieved()
			throws Exception {

		assertThat(typesBuilder.getSchemaType(FOLDER)).isEqualTo(folderTypeBuilder);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.SchemaTypeExistent.class)
	public void whenCreateNewSchemaTypeWithAExistentSchemaThenThrowException()
			throws Exception {

		folderTypeBuilder = typesBuilder.createNewSchemaType(FOLDER);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType.class)
	public void whenGetAInexistentSchemaTypeThenThrowException()
			throws Exception {

		folderTypeBuilder = typesBuilder.getSchemaType(INEXISTENT_CODE);
	}

	@Test
	public void whenGetOrCreateSchemaTypeThenItIsCanBeRetrieved()
			throws Exception {

		newTypeBuilder = typesBuilder.getOrCreateNewSchemaType(INEXISTENT_CODE);

		assertThat(typesBuilder.getSchemaType(FOLDER)).isEqualTo(folderTypeBuilder);
		assertThat(typesBuilder.getSchemaType(INEXISTENT_CODE)).isEqualTo(newTypeBuilder);
	}

	@Test
	public void whenModifyThenNewSchemaTypesBuilderIsReturned()
			throws Exception {

		assertThat(typesBuilder2.getVersion()).isEqualTo(schemaTypes.getVersion());
	}

	@Test
	public void whenBuildModifySchemaTypesThenNewSchemaTypesIsBuildWithNewVersion()
			throws Exception {

		assertThat(typesBuilder2.build(typesFactory).getVersion()).isEqualTo(schemaTypes.getVersion() + 1);
	}

	@Test
	public void whenGetSchemaThenItIsReturned() {

		assertThat(typesBuilder.getSchema(employeeFolderSchemaBuilder.getCode())).isNotNull();
	}

}
