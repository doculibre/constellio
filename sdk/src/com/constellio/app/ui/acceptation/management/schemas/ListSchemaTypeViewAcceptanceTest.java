package com.constellio.app.ui.acceptation.management.schemas;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
public class ListSchemaTypeViewAcceptanceTest extends ConstellioTest {
	MetadataSchemasManager schemasManager;
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	SchemaTypePage page;
	List<MetadataSchemaType> types;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		page = new SchemaTypePage(driver).navigateToPage();
		types = schemasManager.getSchemaTypes(zeCollection).getSchemaTypes();
	}

	@Test
	public void givenBaseConfigurationThenDisplayCorrectInformation() {
		RecordContainerWebElement typeTable = page.getTypeTable();
		assertThat(typeTable.countRows()).isEqualTo(17);
		assertThat(typeTable.hasRowWithValueInColumn("Rubrique du plan", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Contenant", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de contenants", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de documents", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de dossiers", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types de supports", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Types d'emplacement", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Document", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Dossier", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Emplacement", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Unité administrative", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Règle de conservation", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Subdivision uniforme", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Délais variables", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Tâche", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Type de tâche", 0)).isTrue();
		assertThat(typeTable.hasRowWithValueInColumn("Statut d'une tâche", 0)).isTrue();
	}
}
