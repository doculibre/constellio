package com.constellio.model.services.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.sdk.tests.ConstellioTest;

public class GlobalGroupsReaderTest extends ConstellioTest {

	Document document;
	GlobalGroupsWriter writer;
	GlobalGroupsReader reader;
	GlobalGroup globalGroup1, globalGroup1_1, globalGroup2;

	@Before
	public void setup()
			throws Exception {
		globalGroup1 = new GlobalGroup("group1", null, GlobalGroupStatus.ACTIVE);
		globalGroup2 = new GlobalGroup("group2", null, GlobalGroupStatus.ACTIVE);
		globalGroup1_1 = new GlobalGroup("group1_1", "group1", GlobalGroupStatus.ACTIVE);

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
