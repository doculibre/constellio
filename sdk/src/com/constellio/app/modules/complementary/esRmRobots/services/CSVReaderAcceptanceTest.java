package com.constellio.app.modules.complementary.esRmRobots.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.sdk.tests.ConstellioTest;

public class CSVReaderAcceptanceTest extends ConstellioTest {

	ContentManager contentManager;
	User adminUser;
	CSVReader reader;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withAllTestUsers());

		adminUser = getModelLayerFactory().newUserServices().getUserInCollection(admin, zeCollection);
		contentManager = getModelLayerFactory().getContentManager();
		reader = new CSVReader(contentManager);
	}

	@Test
	public void givenCSVContentWhenParsingThenAllDataRetrieved()
			throws Exception {
		Content content = contentManager.createMajor(adminUser, "test.csv",
				contentManager.upload(getTestResourceInputStream("test.csv")));

		List<Map<String, String>> entries = reader.readCSVContent(content);

		verifyFirstEntry(entries.get(0));
		verifySecondEntry(entries.get(1));
	}

	private void verifyFirstEntry(Map<String, String> entry) {
		assertThat(entry.get("string1")).isEqualTo("value1");
		assertThat(entry.get("string2")).isEqualTo("value2");
		assertThat(entry.get("string3")).isEqualTo("value3");
	}

	private void verifySecondEntry(Map<String, String> entry) {
		assertThat(entry.get("string1")).isEqualTo("value4");
		assertThat(entry.get("string2")).isEqualTo("value5");
		assertThat(entry.get("string3")).isEqualTo("value6");
	}
}
