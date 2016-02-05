package demo.scripts;

import static java.util.Arrays.asList;

import java.util.List;

import demo.DemoInitScript;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.setups.Users;

public class Demo_feb_2015 implements DemoInitScript {

	@Override
	public void setup(AppLayerFactory appLayerFactory, ModelLayerFactory modelLayerFactory)
			throws Exception {

		String zeCollection = "zeCollection";
		String demoCollection = "enterpriseCollection";

		givenCollectionWithRMModuleAndUsers(appLayerFactory, modelLayerFactory, zeCollection, "Collection de test");
		givenCollectionWithRMModuleAndUsers(appLayerFactory, modelLayerFactory, demoCollection, "Collection d'entreprise");

		new RMTestRecords(zeCollection).setup(modelLayerFactory).withFoldersAndContainersOfEveryStatus().withEvents();
		new DemoTestRecords(demoCollection).setup(modelLayerFactory).withFoldersAndContainersOfEveryStatus();

	}

	@Override
	public List<InstallableModule> getModules() {
		ConstellioRMModule rmModule = new ConstellioRMModule();
		return asList((InstallableModule) rmModule);
	}

	private void givenCollectionWithRMModuleAndUsers(AppLayerFactory appLayerFactory,
			ModelLayerFactory modelLayerFactory, String code, String title)
			throws Exception {

		//CREATE COLLECTION
		Record collectionRecord = appLayerFactory.getCollectionsManager()
				.createCollectionInCurrentVersion(code, asList("fr"));
		modelLayerFactory.newRecordServices().update(collectionRecord.set(Schemas.TITLE, title));

		//SETUP MODULES
		appLayerFactory.getModulesManager().enableValidModuleAndGetInvalidOnes(code, new ConstellioRMModule());

		//modelLayerFactory.newMigrationServices().migrate(code, null);

		// SETUP USERS
		new Users()
				.setUp(modelLayerFactory.newUserServices())
				.withPhotos(modelLayerFactory.newUserPhotosServices())
				.withPasswords(modelLayerFactory.newAuthenticationService());

	}

}
