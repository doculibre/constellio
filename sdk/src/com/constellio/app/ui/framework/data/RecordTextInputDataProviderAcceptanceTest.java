package com.constellio.app.ui.framework.data;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class RecordTextInputDataProviderAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RecordTextInputDataProvider dataProvider;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
				.withFoldersAndContainersOfEveryStatus());

	}

	private RecordTextInputDataProvider newDataProvider(User user, String schemaType, boolean writeAccess) {
		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(user);
		return new RecordTextInputDataProvider(ConstellioFactories.getInstance(), sessionContext, schemaType, writeAccess);
	}

	@Test
	public void whenSearchingCategoriesThenGoodBehavior()
			throws Exception {

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Category.DEFAULT_SCHEMA).get(Category.KEYWORDS).setSchemaAutocomplete(true).setSearchable(true);
			}
		});

		Transaction transaction = new Transaction();
		transaction.add(records.getCategory_X100()).setKeywords(asList("majestueux bateaux"));
		transaction.add(records.getCategory_X13()).setKeywords(asList("magnifiques bateaux"));
		transaction.add(records.getCategory_X120()).setCode("b");
		transaction.add(records.getCategory_ZE42()).setCode("Ze-42.05");
		transaction.add(records.getCategory_Z110()).setTitle("Dossier d'étudiants au baccalauréat");
		transaction.add(records.getCategory_Z111()).setTitle("Dossier étudiant en maîtrise");
		getModelLayerFactory().newRecordServices().execute(transaction);

		dataProvider = newDataProvider(users.adminIn(zeCollection), Category.SCHEMA_TYPE, false);
		assertThat(dataProvider.getData("X", 0, 20)).containsOnly(
				"categoryId_X", "categoryId_X100", "categoryId_X110", "categoryId_X120", "categoryId_X13"
		);

		assertThat(dataProvider.getData("X1", 0, 20)).containsOnly(
				"categoryId_X100", "categoryId_X110", "categoryId_X120", "categoryId_X13"
		);

		assertThat(dataProvider.getData("Ze", 0, 20)).containsOnly("categoryId_ZE42", "categoryId_Z");
		assertThat(dataProvider.getData("Ze-", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-4", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.0", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.05", 0, 20)).containsOnly("categoryId_ZE42");
		assertThat(dataProvider.getData("Ze-42.051", 0, 20)).isEmpty();
		assertThat(dataProvider.getData("Ze-42.04", 0, 20)).isEmpty();

		assertThat(dataProvider.getData("c", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("ca", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cât", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cate", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cateGor", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");
		assertThat(dataProvider.getData("cateGorY", 0, 20)).containsOnly("categoryId_X", "categoryId_Z");

		assertThat(dataProvider.getData("b", 0, 20))
				.containsOnly("categoryId_X100", "categoryId_X13", "categoryId_X120", "categoryId_Z110");
		assertThat(dataProvider.getData("bateau", 0, 20)).containsOnly("categoryId_X100", "categoryId_X13");
		//assertThat(dataProvider.getData("bateaux", 0, 20)).containsOnly("categoryId_X100", "categoryId_X13");

		assertThat(dataProvider.getData("magnifique bateau", 0, 20)).containsOnly("categoryId_X13");
		assertThat(dataProvider.getData("magnifique bateaux", 0, 20)).containsOnly("categoryId_X13");
		assertThat(dataProvider.getData("magnifiques bateau", 0, 20)).containsOnly("categoryId_X13");
		assertThat(dataProvider.getData("magnifiques bateaux", 0, 20)).containsOnly("categoryId_X13");

		assertThat(dataProvider.getData("majestueu bateau", 0, 20)).containsOnly("categoryId_X100");
		assertThat(dataProvider.getData("majestueu bateaux", 0, 20)).containsOnly("categoryId_X100");
		assertThat(dataProvider.getData("majestueux bateau", 0, 20)).containsOnly("categoryId_X100");
		assertThat(dataProvider.getData("majestueux bateaux", 0, 20)).containsOnly("categoryId_X100");

		assertThat(dataProvider.getData("dossier étudiant", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		//assertThat(dataProvider.getData("dossier d'étudiant", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		assertThat(dataProvider.getData("dossier étudiants", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		//assertThat(dataProvider.getData("dossier d'étudiants", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		assertThat(dataProvider.getData("dossiers étudiant", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		//assertThat(dataProvider.getData("dossiers d'étudiant", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		assertThat(dataProvider.getData("dossiers étudiants", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
		//assertThat(dataProvider.getData("dossiers d'étudiants", 0, 20)).containsOnly("categoryId_Z110", "categoryId_Z111");
	}

	@Test
	public void whenSearchingSecurisedItemsThenSecurityFilter()
			throws Exception {
		assertThat(newDataProvider(users.adminIn(zeCollection), Folder.SCHEMA_TYPE, true).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02", "C02", "C55");

		assertThat(newDataProvider(users.charlesIn(zeCollection), Folder.SCHEMA_TYPE, true).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02");

		assertThat(newDataProvider(users.aliceIn(zeCollection), Folder.SCHEMA_TYPE, true).getData("b", 0, 1000)).isEmpty();

		assertThat(newDataProvider(users.charlesIn(zeCollection), Folder.SCHEMA_TYPE, false).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02");

		assertThat(newDataProvider(users.aliceIn(zeCollection), Folder.SCHEMA_TYPE, false).getData("b", 0, 1000))
				.containsOnly("A04", "A05", "A06", "A07", "A08", "A09", "B02", "C02", "C55");
	}
}
