package com.constellio.app.services.schemas.bulkImport.groups;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class ImportedGroupReaderAcceptTest extends ConstellioTest {
	@Test
	public void whenReadingGroupsXMLFileThenReadCorrectly()
			throws Exception {
		List<ImportedGroup> allGroups = readTestGroups();
		assertThat(allGroups.size()).isEqualTo(3);
		assertThat(allGroups).extracting("code", "parent", "title").containsOnly(
				tuple("001", null, "group 001"),
				tuple("002", "001", "group 002"),
				tuple("003", "002", "group 003")
		);
	}

	private List<ImportedGroup> readTestGroups()
			throws Exception {
		File groupsFile = getTestResourceFile("groups.xml");
		Document document = new SAXBuilder().build(groupsFile);
		ImportedGroupReader reader = new ImportedGroupReader(document);
		return reader.readAll();
	}
}
