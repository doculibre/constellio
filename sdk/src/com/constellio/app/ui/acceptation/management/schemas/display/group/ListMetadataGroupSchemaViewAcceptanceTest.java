package com.constellio.app.ui.acceptation.management.schemas.display.group;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElementRuntimeException.RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class ListMetadataGroupSchemaViewAcceptanceTest extends ConstellioTest {
	MetadataSchemasManager schemasManager;
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	MetadataGroupSchemaPage page;
	MetadataSchemaType type;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		type = schemasManager.getSchemaTypes(zeCollection).getSchemaTypes().get(0);
		page = new MetadataGroupSchemaPage(driver).navigateToPage(type.getCode());
	}

	@Test
	public void givenBaseConfigurationThenDisplayNoGroup()
			throws Exception {
		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(1);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
	}

	@Test
	public void givenBaseConfigurationThenAddingGroupThenDisplayGroup()
			throws Exception {
		page.addGroupWithName("zeGroup");
		page.addGroupWithName("zeOtherGroup");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(3);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
		assertThat(page.getGroupWithName("zeGroup")).isNotNull();
		assertThat(page.getGroupWithName("zeOtherGroup")).isNotNull();
	}

	@Test
	public void givenBaseConfigurationThenAddingTwoGroupWithTheSameNameThenOnlyOneAdded()
			throws Exception {
		page.addGroupWithName("zeGroup");
		page.addGroupWithName("zeGroup");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(2);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
		assertThat(page.getGroupWithName("zeGroup")).isNotNull();
	}

	@Test
	public void givenBaseConfigurationWhenRemoveLastGroupThenNotAble()
			throws Exception {
		RecordContainerWebElement typeTable = page.getGroupTable();

		page.removeGroupWithName("Métadonnées");
		assertThat(typeTable.countRows()).isEqualTo(1);
		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
	}

	@Test(expected = RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn.class)
	public void givenBaseConfigurationWithGroupThenDeleteThenDisplayGoodGroup()
			throws Exception {
		page.addGroupWithName("zeGroup");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(2);

		page.removeGroupWithName("zeGroup");

		//exception thrown here if it's correctly removed
		page.getGroupWithName("zeGroup");
	}

	@Test
	public void givenBaseConfigurationWhenAddingNoNameGroupThenNotAdded()
			throws Exception {
		page.addGroupWithName("");

		RecordContainerWebElement typeTable = page.getGroupTable();
		assertThat(typeTable.countRows()).isEqualTo(1);

		assertThat(page.getGroupWithName("Métadonnées")).isNotNull();
	}
}
