package com.constellio.model.entities.schemas;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataSchemaTypesAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	private MetadataSchemasManager schemaManager;
	private MetadataSchemaBuilder folderSchema, customFolderSchema, documentSchema, documentCustomSchema;
	private Metadata mdInFolderSchemaWithLocalCode1, mdInCustomFolderSchemaWithLocalCode1, mdInDocumentSchemaWithLocalCode1, mdInDocumentCustomSchemaWithLocalCode1,
			mdInFolderSchemaWithLocalCode2, mdInCustomFolderSchemaWithLocalCode3, mdInDocumentSchemaWithLocalCode4, mdInDocumentCustomSchemaWithLocalCode5,
			mdInCustomFolderSchema2WithLocalCode3;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus()
		);
		schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		initTestData();
	}

	//
	private void initTestData()
			throws OptimisticLocking {
		MetadataSchemaTypesBuilder schemaTypesBuilder = schemaManager
				.modify(zeCollection);
		MetadataSchemaTypeBuilder folderSchemaType = schemaTypesBuilder.getSchemaType(Folder.SCHEMA_TYPE);
		folderSchema = schemaTypesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
		customFolderSchema = folderSchemaType.createCustomSchema("custom");
		MetadataSchemaBuilder customFolderSchema2 = folderSchemaType.createCustomSchema("custom2");

		MetadataSchemaTypeBuilder documetSchemaType = schemaTypesBuilder.getSchemaType(Document.SCHEMA_TYPE);
		documentSchema = documetSchemaType.getDefaultSchema();
		documentCustomSchema = documetSchemaType.createCustomSchema("custom");

		documentSchema.create("code1").setType(STRING)
				.setDefaultValue("default");
		folderSchema.create("code1").setType(STRING)
				.setDefaultValue("default");

		folderSchema.create("code2").setType(STRING)
				.setDefaultValue("default");

		customFolderSchema.create("code3").setType(STRING)
				.setDefaultValue("default");
		customFolderSchema2.create("code3").setType(STRING)
				.setDefaultValue("default");

		documentSchema.create("code4").setType(STRING)
				.setDefaultValue("default");

		documentCustomSchema.create("code5").setType(STRING)
				.setDefaultValue("default");

		schemaManager.saveUpdateSchemaTypes(schemaTypesBuilder);

		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		mdInFolderSchemaWithLocalCode1 = types.getMetadata(folderSchema.getCode() + "_" + "code1");
		mdInCustomFolderSchemaWithLocalCode1 = types.getMetadata(customFolderSchema.getCode() + "_" + "code1");
		mdInDocumentSchemaWithLocalCode1 = types.getMetadata(documentSchema.getCode() + "_" + "code1");
		mdInDocumentCustomSchemaWithLocalCode1 = types.getMetadata(documentCustomSchema.getCode() + "_" + "code1");

		mdInFolderSchemaWithLocalCode2 = types.getMetadata(folderSchema.getCode() + "_" + "code2");
		mdInCustomFolderSchemaWithLocalCode3 = types.getMetadata(customFolderSchema.getCode() + "_" + "code3");
		mdInCustomFolderSchema2WithLocalCode3 = types.getMetadata(customFolderSchema2.getCode() + "_" + "code3");
		mdInDocumentSchemaWithLocalCode4 = types.getMetadata(documentSchema.getCode() + "_" + "code4");
		mdInDocumentCustomSchemaWithLocalCode5 = types.getMetadata(documentCustomSchema.getCode() + "_" + "code5");

	}

	//TODO Francis fix @Test
	public void whenGetAllMetadatasThenOk() {
		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		assertThat(types.getAllMetadatas()).extracting("code")
				.contains(mdInFolderSchemaWithLocalCode1.getCode(), mdInDocumentSchemaWithLocalCode1.getCode(),
						mdInFolderSchemaWithLocalCode2.getCode(), mdInCustomFolderSchemaWithLocalCode3.getCode(),
						mdInDocumentSchemaWithLocalCode4.getCode(), mdInDocumentCustomSchemaWithLocalCode5.getCode()
						, mdInCustomFolderSchema2WithLocalCode3.getCode());
	}

	//TODO Francis fix @Test
	public void whenGetAllMetadatasIncludingThoseWithInheritanceThenOk() {
		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		assertThat(types.getAllMetadatasIncludingThoseWithInheritance()).extracting("code").contains(
				mdInFolderSchemaWithLocalCode1.getCode(), mdInDocumentSchemaWithLocalCode1.getCode(),
				mdInCustomFolderSchemaWithLocalCode1.getCode(), mdInDocumentCustomSchemaWithLocalCode1.getCode(),
				mdInFolderSchemaWithLocalCode2.getCode(), mdInCustomFolderSchemaWithLocalCode3.getCode(),
				mdInDocumentSchemaWithLocalCode4.getCode(), mdInDocumentCustomSchemaWithLocalCode5.getCode(),
				mdInCustomFolderSchema2WithLocalCode3.getCode()
		);
	}

	@Test
	public void displayOrderIsCoherent() {
		MetadataSchemaTypes types = schemaManager.getSchemaTypes(zeCollection);
		List<MetadataSchemaType> newTypeList = types.getSchemaTypesInDisplayOrder();

		Iterator<MetadataSchemaType> i = newTypeList.iterator();
		while (i.hasNext()) {
			if (i.next().equals(types.getSchemaType("folder"))) {
				assertThat(i.next())
						.as("Document is right after Folder in the display order")
						.isEqualTo(types.getSchemaType("document"));
			}
		}
	}

}
