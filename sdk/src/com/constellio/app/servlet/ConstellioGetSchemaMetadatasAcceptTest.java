package com.constellio.app.servlet;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedDdvContainerMetadatas;
import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedDdvDocumentMetadatas;
import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedDocumentDefaultSchemaMetadatas;
import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedDocumentEmailSchemaMetadatas;
import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedFolderDefaultSchemaMetadatas;
import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedKeywordMetadatas;
import static com.constellio.app.servlet.ConstellioGetSchemaMetadatasAcceptTestRessources.expectedTypeMetadatas;
import static org.assertj.core.api.Assertions.assertThat;

public class ConstellioGetSchemaMetadatasAcceptTest extends ConstellioTest {

	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;
	UserServices userServices;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users));
		startApplication();
	}

	@After
	public void stopApplicationAfterTest() {
		stopApplication();
	}

	@Test
	public void whenGetDefaultFolderSchemaMetadatasThenValidReturnedDocument()
			throws Exception {

		Document document = callGetSchemaMetadatas(zeCollection, "folder_default");
		assertThat(document.getRootElement().getAttributeValue("language")).isEqualTo("fr");
		assertThat(document.getRootElement().getAttributeValue("code")).isEqualTo("folder_default");
		assertThat(document.getRootElement().getAttributeValue("collection")).isEqualTo("zeCollection");
		assertThat(document.getRootElement().getAttributeValue("search-field")).isEqualTo("search_txt_fr");

		List<Element> metadatas = document.getRootElement().getChildren("metadata");

		Element keywordMetadata = find("keywords", metadatas);
		assertThat(keywordMetadata.getAttributeValue("code")).isEqualTo("keywords");
		assertThat(keywordMetadata.getAttributeValue("title")).isEqualTo("Mots-clés");
		assertThat(keywordMetadata.getAttributeValue("type")).isEqualTo("STRING");
		assertThat(keywordMetadata.getAttributeValue("multivalue")).isEqualTo("true");
		assertThat(keywordMetadata.getAttributeValue("solr-field")).isEqualTo("keywords_ss");
		assertThat(keywordMetadata.getAttributeValue("solr-analyzed-field")).isEqualTo("keywords_txt_fr");

		Element typeMetadata = find("type", metadatas);
		assertThat(typeMetadata.getAttributeValue("code")).isEqualTo("type");
		assertThat(typeMetadata.getAttributeValue("title")).isEqualTo("Type");
		assertThat(typeMetadata.getAttributeValue("type")).isEqualTo("REFERENCE");
		assertThat(typeMetadata.getAttributeValue("multivalue")).isEqualTo("false");
		assertThat(typeMetadata.getAttributeValue("solr-field")).isEqualTo("typeId_s");
	}

	@Test
	public void whenGetDefaultFolderSchemaMetadatasFromTypeAndLabelThenValidReturnedDocument()
			throws Exception {

		Document document = callGetSchemaMetadatasWithType(zeCollection, "folder", "Dossier");
		assertThat(document.getRootElement().getAttributeValue("code")).isEqualTo(zeCollection);

		Element schema = document.getRootElement().getChildren("schema").get(0);
		assertThat(schema.getAttributes()).extracting("name", "value").contains(
				expectedFolderDefaultSchemaMetadatas()
		);

		List<Element> metadatas = schema.getChildren("metadata");
		Element keywordMetadata = find("keywords", metadatas);
		assertThat(keywordMetadata.getAttributes()).extracting("name", "value").contains(
				expectedKeywordMetadatas()
		);

		Element typeMetadata = find("type", metadatas);
		assertThat(typeMetadata.getAttributes()).extracting("name", "value").contains(
				expectedTypeMetadatas()
		);
	}

	@Test
	public void whenGetAllFolderSchemasAndMetadatasFromTypeThenValidReturnedDocument()
			throws Exception {

		Document document = callGetSchemaMetadatasWithType(zeCollection, "document");
		assertThat(document.getRootElement().getAttributeValue("code")).isEqualTo(zeCollection);

		List<Element> schemas = document.getRootElement().getChildren("schema");
		Element schemaWithLabelDocument = find("document_default", schemas);
		Element schemaWithLabelCourriel = find("document_email", schemas);
		assertThat(schemaWithLabelDocument.getAttributes()).extracting("name", "value").contains(
				expectedDocumentDefaultSchemaMetadatas()
		);
		assertThat(schemaWithLabelCourriel.getAttributes()).extracting("name", "value").contains(
				expectedDocumentEmailSchemaMetadatas()
		);

		List<Element> documentMetadatas = schemaWithLabelDocument.getChildren("metadata");
		Element documentKeywordMetadata = find("keywords", documentMetadatas);
		assertThat(documentKeywordMetadata.getAttributes()).extracting("name", "value").contains(
				expectedKeywordMetadatas()
		);

		List<Element> courrielMetadatas = schemaWithLabelCourriel.getChildren("metadata");
		Element courrielKeywordMetadata = find("keywords", courrielMetadatas);
		assertThat(courrielKeywordMetadata.getAttributes()).extracting("name", "value").contains(
				expectedKeywordMetadatas()
		);
	}

	@Test
	public void whenGetDdvDocumentTypeFromTypeReThenValidReturnedDocument()
			throws Exception {

		Document document = callGetSchemaMetadatasWithType(zeCollection, "ddv", "Types de documents");
		assertThat(document.getRootElement().getAttributeValue("code")).isEqualTo(zeCollection);

		Element schema = document.getRootElement().getChildren("schema").get(0);

		assertThat(schema.getAttributes()).extracting("name", "value").contains(
				expectedDdvDocumentMetadatas()
		);

	}

	@Test
	public void whenGetAllDdvFromTypeThenValidReturnedDocument()
			throws Exception {

		Document document = callGetSchemaMetadatasWithType(zeCollection, "ddv");
		assertThat(document.getRootElement().getAttributeValue("code")).isEqualTo(zeCollection);

		List<Element> schemas = document.getRootElement().getChildren("schema");
		Element schemaWithLabelDocument = find("ddvDocumentType_default", schemas);
		Element schemaWithLabelContainer = find("ddvContainerRecordType_default", schemas);

		assertThat(schemaWithLabelDocument.getAttributes()).extracting("name", "value").contains(
				expectedDdvDocumentMetadatas()
		);
		assertThat(schemaWithLabelContainer.getAttributes()).extracting("name", "value").contains(
				expectedDdvContainerMetadatas()
		);


	}

	private Element find(String code, List<Element> metadatas) {
		for (Element metadata : metadatas) {
			if (code.equals(metadata.getAttributeValue("code"))) {
				return metadata;
			}
		}
		throw new RuntimeException("No such metadata with code '" + code + "'");
	}

	private Document callGetSchemaMetadatas(String collection, String schema)
			throws Exception {

		String url = "http://localhost:7070/constellio/getSchemaMetadatas?collection=" + collection + "&schema=" + schema;

		WebClient webClient = new WebClient();

		Page page = webClient.getPage(url);
		String response = page.getWebResponse().getContentAsString("UTF-8");

		SAXBuilder builder = new SAXBuilder();
		//builder.ge
		return builder.build(new StringReader(response));

	}

	private Document callGetSchemaMetadatasWithType(String collection, String type)
			throws Exception {

		String url = "http://localhost:7070/constellio/getSchemaMetadatas?collection=" + collection + "&type=" + type;

		WebClient webClient = new WebClient();

		Page page = webClient.getPage(url);
		String response = page.getWebResponse().getContentAsString("UTF-8");

		SAXBuilder builder = new SAXBuilder();
		//builder.ge
		return builder.build(new StringReader(response));

	}

	private Document callGetSchemaMetadatasWithType(String collection, String type, String label)
			throws Exception {
		label = label.replace(" ", "%20");
		String url = "http://localhost:7070/constellio/getSchemaMetadatas?collection=" + collection
					 + "&type=" + type + "&label=" + label;

		WebClient webClient = new WebClient();

		Page page = webClient.getPage(url);
		String response = page.getWebResponse().getContentAsString("UTF-8");

		SAXBuilder builder = new SAXBuilder();
		//builder.ge
		return builder.build(new StringReader(response));

	}
}
