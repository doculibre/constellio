package com.constellio.model.services.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.XmlGlobalGroup;
import com.constellio.sdk.tests.ConstellioTest;

public class GlobalGroupsWriterTest extends ConstellioTest {

	public static final String PARENT = "parent";
	public static final String STATUS = "status";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS = "usersAutomaticallyAddedToCollections";
	GlobalGroupsWriter writer;
	Document document;
	GlobalGroup globalGroup1;
	GlobalGroup globalGroup1_1;
	GlobalGroup globalGroup1_1_1;
	GlobalGroup globalGroup2;

	@Before
	public void setup()
			throws Exception {
		document = new Document();
		writer = new GlobalGroupsWriter(document);
		writer.createEmptyGlobalGroups();

		globalGroup1 = new XmlGlobalGroup("group1", null, GlobalGroupStatus.ACTIVE, true);
		globalGroup1_1 = new XmlGlobalGroup("group1_1", "group1", GlobalGroupStatus.ACTIVE, true);
		globalGroup1_1_1 = new XmlGlobalGroup("group1_1_1", "group1_1", GlobalGroupStatus.ACTIVE, true);
		globalGroup2 = new XmlGlobalGroup("group2", null, GlobalGroupStatus.ACTIVE, true);
	}

	@Test
	public void whenCreateEmptyGloblalGroupsThenItIsCreated()
			throws Exception {

		assertThat(document.getRootElement().getChildren()).isEmpty();
	}

	@Test
	public void whenAddGlobalGroupsThenTheyAreAdded()
			throws Exception {

		writer.addUpdate(globalGroup1);
		writer.addUpdate(globalGroup1_1);
		writer.addUpdate(globalGroup1_1_1);
		writer.addUpdate(globalGroup2);

		assertThat(document.getRootElement().getChildren()).hasSize(4);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(CODE)).isEqualTo(globalGroup1.getCode());
		assertThat(document.getRootElement().getChildren().get(0).getChild(NAME).getText()).isEqualTo(globalGroup1.getName());
		assertThat(document.getRootElement().getChildren().get(0).getChild(PARENT).getText()).isEqualTo("");
		assertThat(document.getRootElement().getChildren().get(0).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.ACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(0).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getAttributeValue(CODE)).isEqualTo(globalGroup1_1.getCode());
		assertThat(document.getRootElement().getChildren().get(1).getChild(NAME).getText()).isEqualTo(globalGroup1_1.getName());
		assertThat(document.getRootElement().getChildren().get(1).getChild(PARENT).getText()).isEqualTo(globalGroup1.getCode());
		assertThat(document.getRootElement().getChildren().get(1).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.ACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(1).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();
		assertThat(document.getRootElement().getChildren().get(2).getAttributeValue(CODE))
				.isEqualTo(globalGroup1_1_1.getCode());
		assertThat(document.getRootElement().getChildren().get(2).getChild(NAME).getText())
				.isEqualTo(globalGroup1_1_1.getName());
		assertThat(document.getRootElement().getChildren().get(2).getChild(PARENT).getText())
				.isEqualTo(globalGroup1_1.getCode());
		assertThat(document.getRootElement().getChildren().get(0).getChild(STATUS).getText()).isEqualTo(
				GlobalGroupStatus.ACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(2).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();
		assertThat(document.getRootElement().getChildren().get(3).getAttributeValue(CODE)).isEqualTo(globalGroup2.getCode());
		assertThat(document.getRootElement().getChildren().get(3).getChild(NAME).getText()).isEqualTo(globalGroup2.getName());
		assertThat(document.getRootElement().getChildren().get(3).getChild(PARENT).getText()).isEqualTo("");
		assertThat(document.getRootElement().getChildren().get(3).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.ACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(3).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();
	}

	@Test
	public void givenGlobalGroupWhenUpdateItThenItIsUpdated()
			throws Exception {

		writer.addUpdate(globalGroup1);
		writer.addUpdate(globalGroup2);

		globalGroup1 = new XmlGlobalGroup("group1", "name1", Arrays.asList("user1"), null, GlobalGroupStatus.INACTIVE, true);
		writer.addUpdate(globalGroup1);

		assertThat(document.getRootElement().getChildren()).hasSize(2);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(CODE)).isEqualTo(globalGroup2.getCode());
		assertThat(document.getRootElement().getChildren().get(0).getChild(NAME).getText()).isEqualTo("group2");
		assertThat(document.getRootElement().getChildren().get(0).getChild(PARENT).getText()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(0).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.ACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(0).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getAttributeValue(CODE)).isEqualTo(globalGroup1.getCode());
		assertThat(document.getRootElement().getChildren().get(1).getChild(NAME).getText()).isEqualTo("name1");
		assertThat(document.getRootElement().getChildren().get(1).getChild(PARENT).getText()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.INACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(1).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.hasSize(1);
	}

	@Test
	public void givenGlobalGroupWhenRemoveItThenItIsRemoved()
			throws Exception {

		writer.addUpdate(globalGroup1);
		writer.addUpdate(globalGroup2);

		writer.logicallyRemove(globalGroup1);

		assertThat(document.getRootElement().getChildren()).hasSize(2);
		assertThat(document.getRootElement().getChildren().get(0).getAttributeValue(CODE)).isEqualTo(globalGroup2.getCode());
		assertThat(document.getRootElement().getChildren().get(0).getChild(NAME).getText()).isEqualTo("group2");
		assertThat(document.getRootElement().getChildren().get(0).getChild(PARENT).getText()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(0).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.ACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(0).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();

		assertThat(document.getRootElement().getChildren().get(1).getAttributeValue(CODE)).isEqualTo(globalGroup1.getCode());
		assertThat(document.getRootElement().getChildren().get(1).getChild(NAME).getText()).isEqualTo("group1");
		assertThat(document.getRootElement().getChildren().get(1).getChild(PARENT).getText()).isEmpty();
		assertThat(document.getRootElement().getChildren().get(1).getChild(STATUS).getText())
				.isEqualTo(GlobalGroupStatus.INACTIVE.name());
		assertThat(
				document.getRootElement().getChildren().get(1).getChild(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS).getChildren())
				.isEmpty();
	}
}
