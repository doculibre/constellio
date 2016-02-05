package com.constellio.app.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.List;

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
}
