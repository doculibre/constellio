package com.constellio.model.services.users;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.XmlGlobalGroup;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalGroupsReaderTest extends ConstellioTest {

	Document document;
	GlobalGroupsWriter writer;
	GlobalGroupsReader reader;
	GlobalGroup globalGroup1, globalGroup1_1, globalGroup2;

	@Before
	public void setup()
			throws Exception {
		globalGroup1 = new XmlGlobalGroup("group1", null, GlobalGroupStatus.ACTIVE, true);
		globalGroup2 = new XmlGlobalGroup("group2", null, GlobalGroupStatus.ACTIVE, true);
		globalGroup1_1 = new XmlGlobalGroup("group1_1", "group1", GlobalGroupStatus.ACTIVE, true);

		Document document = new Document();
		writer = new GlobalGroupsWriter(document);

		writer.createEmptyGlobalGroups();
		writer.addUpdate(globalGroup1);
		writer.addUpdate(globalGroup2);
		writer.addUpdate(globalGroup1);
		writer.addUpdate(globalGroup1_1);
		reader = new GlobalGroupsReader(document);
	}

	@Test
	public void givenTwoGlobalGroupsWhenReadAllThenReturnThem()
			throws Exception {
		Map<String, GlobalGroup> globalGroups = reader.readAll();

		assertThat(globalGroups).hasSize(3);
		assertThat(globalGroups.containsKey("group1")).isTrue();
		assertThat(globalGroups.containsKey("group2")).isTrue();
		assertThat(globalGroups.containsKey("group1_1")).isTrue();
		assertThat(globalGroups.get("group1").getParent()).isNull();
		assertThat(globalGroups.get("group2").getParent()).isNull();
		assertThat(globalGroups.get("group1_1").getParent()).isEqualTo("group1");

	}
}
