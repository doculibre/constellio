package com.constellio.app.api.cmis.accept;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class DeleteTreeAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
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
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		users.setUp(userServices);

		defineSchemasManager().using(zeCollectionSchemas);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUpdateUserCredential(
				userServices.getUserCredential(chuckNorris).withServiceKey(chuckNorrisKey).withSystemAdminPermission());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();

	}

	@Test
	public void whenDeleteTreeThenItIsDeleted()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionDeleteAccess(true).getWrappedRecord());
		CmisObject object = cmisSession.getObject(zeCollectionRecords.folder1.getId());

		cmisSession.getBinding().getObjectService()
				.deleteTree(cmisSession.getRepositoryInfo().getId(), object.getId(), true, UnfileObject.DELETE, false, null);

		try {
			recordServices.getDocumentById(zeCollectionRecords.folder1.getId());
			fail("Record still exist");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//OK
		}
	}

	@Test
	public void givenOnlyReadAccessWhenDeleteTreeThenItsNotDeleted()
			throws Exception {
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionReadAccess(true).getWrappedRecord());
		CmisObject object = cmisSession.getObject(zeCollectionRecords.folder1.getId());

		cmisSession.getBinding().getObjectService()
				.deleteTree(cmisSession.getRepositoryInfo().getId(), object.getId(), true, UnfileObject.DELETE, false, null);

		recordServices.refresh(zeCollectionRecords.folder1);
		assertThat(zeCollectionRecords.folder1.isActive()).isTrue();
	}
}
