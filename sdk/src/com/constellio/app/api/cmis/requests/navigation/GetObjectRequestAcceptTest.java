package com.constellio.app.api.cmis.requests.navigation;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DriverTest
public class GetObjectRequestAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	// Session cmisSession;

	String chuckNorrisKey = "chuckNorris-key";
	String chuckNorrisToken;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices, zeCollection);

		defineSchemasManager().using(zeCollectionSchemas);
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.execute(
				userServices.addUpdate(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(zeCollection));
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void givenObjectCreatedThenObjectCanBeObtainedByRequestAndFieldsAreCorrect()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		recordServices.update(zeCollectionRecords.folder1.set(Schemas.CREATED_BY, users.chuckNorrisIn(zeCollection).getId()));
		CmisObject object = cmisSession.getObject(zeCollectionRecords.folder1.getId());

		thenFolderOneObjectHasCorrectFields(object);
	}

	@Test
	public void givenObjectCreatedWithUserThenObjectHasCorrectCreatedByValue()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true)
				.getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		Record record = new TestRecord(zeCollectionSchemas.getSchema("folder_default"), "aTestFolder");
		recordServices.execute(new Transaction(record).setUser(users.chuckNorrisIn(zeCollection)));
		CmisObject object = cmisSession.getObject(record.getId());

		assertThat(object.getCreatedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
	}

	@Test
	public void givenTaxoCreatedThenTaxoCanBeObtainedByRequestAndFieldsAreCorrect()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		CmisObject object = cmisSession.getObject("taxo_" + zeCollectionSchemas.getTaxonomy1().getCode());

		thenTaxoOneObjectHasCorrectFields(object);
	}

	@Test
	public void givenConceptCreatedThenConceptCanBeObtainedByRequestAndFieldsAreCorrect()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		CmisObject object = cmisSession.getObject(zeCollectionRecords.taxo1_category2.getId());

		thenCategoryTwoObjectHasCorrectFields(object);
	}

	@Test
	public void whenGetRootFolderThenValidActionsAndProperties()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		Session cmisSession = givenAdminSessionOnZeCollection();
		Folder rootFolder = cmisSession.getRootFolder();

		assertThat(rootFolder.getAllowableActions().getAllowableActions()).containsOnly(
				Action.CAN_GET_CHILDREN,
				Action.CAN_GET_PROPERTIES
		);

		assertThat(rootFolder.<String>getPropertyValue(PropertyIds.NAME)).isEqualTo("Collection de test");
	}

	private void thenFolderOneObjectHasCorrectFields(CmisObject object) {
		assertThat(object).isNotNull();
		assertThat(object.getId()).isEqualTo("folder1");
		assertThat(object.getProperty("id").<String>getValue()).isEqualTo("folder1");
		assertThat(object.getProperty("title").<String>getValue()).isEqualTo("folder1");
		assertThat(object.getProperty("schema").<String>getValue()).isEqualTo("folder_default");
		assertThat(object.getProperty("path").getFirstValue()).isEqualTo(
				"/taxo1/zetaxo1_fond1/zetaxo1_fond1_1/zetaxo1_category1/folder1");
		assertThat(object.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo("zetaxo1_category1");
		assertThat(object.getProperty(PropertyIds.PATH).<String>getValue()).isNotNull();
	}

	private void thenCategoryTwoObjectHasCorrectFields(CmisObject object) {
		assertThat(object).isNotNull();
		assertThat(object.getId()).isEqualTo("zetaxo1_category2");
		//assertThat(object.getProperty("id").getValue()).isEqualTo("taxo1_category2");
		assertThat(object.getProperty("title").<String>getValue()).isEqualTo("zetaxo1_category2");
		assertThat(object.getProperty("schema").<String>getValue()).isEqualTo("category_default");
		assertThat(object.getProperty("path").getFirstValue())
				.isEqualTo("/taxo1/zetaxo1_fond1/zetaxo1_category2");
		assertThat(object.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo("zetaxo1_fond1");
	}

	private void thenTaxoOneObjectHasCorrectFields(CmisObject object) {
		assertThat(object).isNotNull();
		assertThat(object.getId()).isEqualTo("taxo_taxo1");
		assertThat(object.getName()).isEqualTo("zeTaxo");
		assertThat(object.getProperty(PropertyIds.PARENT_ID).<String>getValue()).isEqualTo(zeCollection);
		assertThat(object.getProperty(PropertyIds.PATH).<String>getValue()).isEqualTo("/taxo_taxo1");
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		return newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();
	}

}
