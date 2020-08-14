package com.constellio.dev;

import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader1;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader2;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLWriter3;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TrimSchemasAcceptTest extends ConstellioTest {

	@Before
	public void setUp()
			throws Exception {

	}

	//@Test
	public void zeTest()
			throws Exception {

		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		prepareSystem(
				withCollection("collection").withConstellioRMModule()
		);

		String inputFilePath = "/Users/francisbaril/Workspaces/new-schema-xml/before-default.xml";
		String outputFilePath = "/Users/francisbaril/Workspaces/new-schema-xml/after-default.xml";

		File inputFile = new File(inputFilePath);
		File outputFile = new File(outputFilePath);

		CollectionInfo collectionInfo = new CollectionInfo((byte) 0, "collection", "fr", Arrays.asList("fr"));
		Document originalDocument = getDocumentFromFile(inputFile);
		long fileLength = inputFile.length();
		System.out.println("fileLength before " + fileLength);
		MetadataSchemaTypesBuilder typesBuilder = new MetadataSchemaXMLReader1(new DefaultClassProvider())
				.read(collectionInfo, originalDocument, new SolrDataStoreTypesFactory(), getModelLayerFactory());
		MetadataSchemaTypes types = typesBuilder.build(new SolrDataStoreTypesFactory());

		Document trimmedDocument = new MetadataSchemaXMLWriter3().write(types);
		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

		xmlOutputter.output(trimmedDocument, fileOutputStream);
		fileOutputStream.close();
		long newFileLength = outputFile.length();
		System.out.println("fileLength after " + newFileLength);
		System.out.println(fileLength / newFileLength);

		MetadataSchemaTypesBuilder types2Builder = new MetadataSchemaXMLReader2(new DefaultClassProvider())
				.read(collectionInfo, getDocumentFromFile(outputFile), new SolrDataStoreTypesFactory(), getModelLayerFactory());
		MetadataSchemaTypes types2 = types2Builder.build(new SolrDataStoreTypesFactory());

		for (MetadataSchemaType type1 : types.getSchemaTypes()) {
			MetadataSchemaType type2 = types2.getSchemaType(type1.getCode());

			for (MetadataSchema schema1 : type1.getAllSchemas()) {
				MetadataSchema schema2 = type2.getSchema(schema1.getCode());

				for (Metadata metadata1 : schema1.getMetadatas()) {
					Metadata metadata2 = schema2.getMetadata(metadata1.getCode());

					ensureSameMetadata(metadata1, metadata2);

				}
				assertThat(schema1).describedAs("schema '" + schema1.getCode() + "' is unmodified").isEqualTo(schema2);
			}
			assertThat(type1).describedAs("type '" + type1.getCode() + "' is unmodified").isEqualTo(type2);
		}
	}

	private void ensureSameMetadata(Metadata metadata1, Metadata metadata2) {
		assertThat(metadata1).describedAs("metadata '" + metadata1.getCode() + "' is unmodified").isEqualTo(metadata2);

	}

	Document getDocumentFromFile(File file) {
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(file);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}
}
