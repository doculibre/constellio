package com.constellio.app.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.List;

import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

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

	@Test
	@InDevelopmentTest
	public void browserTest()
			throws Exception {
		newWebDriver();
		waitUntilICloseTheBrowsers();
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
		assertThat(schema.getAttributeValue("language")).isEqualTo("fr");
		assertThat(schema.getAttributeValue("code")).isEqualTo("folder_default");
		assertThat(schema.getAttributeValue("collection")).isEqualTo("zeCollection");
		assertThat(schema.getAttributeValue("search-field")).isEqualTo("search_txt_fr");

		List<Element> metadatas = schema.getChildren("metadata");
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
	public void whenGetAllFolderSchemasAndMetadatasFromTypeThenValidReturnedDocument()
			throws Exception {

		Document document = callGetSchemaMetadatasWithType(zeCollection, "document");
		assertThat(document.getRootElement().getAttributeValue("code")).isEqualTo(zeCollection);

		List<Element> schemas = document.getRootElement().getChildren("schema");
		Element schemaWithLabelDocument = find("document_default", schemas);
		Element schemaWithLabelCourriel = find("document_email", schemas);
		assertThat(schemaWithLabelDocument.getAttributeValue("language")).isEqualTo("fr");
		assertThat(schemaWithLabelDocument.getAttributeValue("code")).isEqualTo("document_default");
		assertThat(schemaWithLabelDocument.getAttributeValue("collection")).isEqualTo("zeCollection");
		assertThat(schemaWithLabelDocument.getAttributeValue("search-field")).isEqualTo("search_txt_fr");
		assertThat(schemaWithLabelCourriel.getAttributeValue("language")).isEqualTo("fr");
		assertThat(schemaWithLabelCourriel.getAttributeValue("code")).isEqualTo("document_email");
		assertThat(schemaWithLabelCourriel.getAttributeValue("collection")).isEqualTo("zeCollection");
		assertThat(schemaWithLabelCourriel.getAttributeValue("search-field")).isEqualTo("search_txt_fr");

		List<Element> metadatasDocument = schemaWithLabelDocument.getChildren("metadata");
		Element keywordMetadataDocument = find("keywords", metadatasDocument);
		assertThat(keywordMetadataDocument.getAttributeValue("code")).isEqualTo("keywords");
		assertThat(keywordMetadataDocument.getAttributeValue("title")).isEqualTo("Mots-clés");
		assertThat(keywordMetadataDocument.getAttributeValue("type")).isEqualTo("STRING");
		assertThat(keywordMetadataDocument.getAttributeValue("multivalue")).isEqualTo("true");
		assertThat(keywordMetadataDocument.getAttributeValue("solr-field")).isEqualTo("keywords_ss");
		assertThat(keywordMetadataDocument.getAttributeValue("solr-analyzed-field")).isEqualTo("keywords_txt_fr");

		List<Element> metadatasCourriel = schemaWithLabelDocument.getChildren("metadata");
		Element keywordMetadataCourriel = find("keywords", metadatasCourriel);
		assertThat(keywordMetadataCourriel.getAttributeValue("code")).isEqualTo("keywords");
		assertThat(keywordMetadataCourriel.getAttributeValue("title")).isEqualTo("Mots-clés");
		assertThat(keywordMetadataCourriel.getAttributeValue("type")).isEqualTo("STRING");
		assertThat(keywordMetadataCourriel.getAttributeValue("multivalue")).isEqualTo("true");
		assertThat(keywordMetadataCourriel.getAttributeValue("solr-field")).isEqualTo("keywords_ss");
		assertThat(keywordMetadataCourriel.getAttributeValue("solr-analyzed-field")).isEqualTo("keywords_txt_fr");
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
