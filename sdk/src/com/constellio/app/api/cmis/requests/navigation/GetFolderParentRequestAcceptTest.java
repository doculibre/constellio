package com.constellio.app.api.cmis.requests.navigation;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DriverTest
public class GetFolderParentRequestAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	Session cmisSession;

	String chuckNorrisKey = "chuckNorris-key";
	String chuckNorrisToken;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), schemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.execute(
				userServices.addUpdate(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addCollection(zeCollection));
		cmisSession = givenAdminSessionOnZeCollection();
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void whenGetParentOfSubFolderThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.folder2_1;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.folder2.getId());
	}

	@Test
	public void whenGetParentOfSubCategoryThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.taxo1_category2_1;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.taxo1_category2.getId());
	}

	@Test
	public void whenGetParentOfTaxonomyThenRootIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.taxo1_category2;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), "taxo_taxo1", null, null);

		assertThat(objectDataParent.getId()).isEqualTo("zeCollection");
	}

	@Test
	public void whenGetParentOfCategoryThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.taxo1_category2;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.taxo1_fond1.getId());
	}

	@Test
	public void whenGetParentOfFolderThenItIsReturned()
			throws Exception {
		Record record = zeCollectionRecords.folder4;

		ObjectData objectDataParent = cmisSession.getBinding().getNavigationService()
				.getFolderParent(cmisSession.getRepositoryInfo().getId(), record.getId(), null, null);

		assertThat(objectDataParent.getId()).isEqualTo(zeCollectionRecords.taxo1_category2.getId());
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		return newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();
	}
}
