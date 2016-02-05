package com.constellio.app.api.cmis.accept;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;

@DriverTest
public class UpdatePropertiesRequestAcceptTest extends ConstellioTest {

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
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), schemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.addUpdateUserCredential(
				userServices.getUserCredential(chuckNorris).withServiceKey(chuckNorrisKey).withSystemAdminPermission());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();

		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
	}

	@Test
	public void whenUpdatingFolderThenPropertiesUpdated()
			throws Exception {
		Map<String, Object> properties = new HashMap<>();
		properties.put("title", "updatedTitle");
		//		properties.put(PropertyIds.CHANGE_TOKEN, recordServices.getDocumentById("folder1").getVersionLabel());
		CmisObject initialObject = cmisSession.getObject("folder1");
		CmisObject updatedObject = initialObject.updateProperties(properties);

		assertThat(updatedObject.getProperty("title").getFirstValue()).isEqualTo("updatedTitle");
	}

	@Test
	public void whenUpdatingCategoryThenPropertiesUpdated()
			throws Exception {
		Map<String, Object> properties = new HashMap<>();
		properties.put("title", "updatedTitle");
		//		properties.put(PropertyIds.CHANGE_TOKEN, recordServices.getDocumentById("folder1").getVersionLabel());
		CmisObject initialObject = cmisSession.getObject("zetaxo1_category1");
		CmisObject updatedObject = initialObject.updateProperties(properties);

		assertThat(updatedObject.getProperty("title").getFirstValue()).isEqualTo("updatedTitle");
	}

	@Test
	public void whenUpdatingCollectionThenException()
			throws Exception {
		Map<String, Object> properties = new HashMap<>();
		properties.put("title", "updatedTitle");
		//		properties.put(PropertyIds.CHANGE_TOKEN, recordServices.getDocumentById("folder1").getVersionLabel());
		CmisObject initialObject = cmisSession.getRootFolder();
		try {
			initialObject.updateProperties(properties);
		} catch (CmisRuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("Cannot update collection via CMIS.");
		}
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		getModelLayerFactory().newAuthenticationService().changePassword(chuckNorris, "1qaz2wsx");
		return newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
	}
}
