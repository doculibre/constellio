package com.constellio.app.api.cmis.accept;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

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
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		userServices = getModelLayerFactory().newUserServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
		searchServices = getModelLayerFactory().newSearchServices();
		users.setUp(userServices, zeCollection);

		defineSchemasManager().using(zeCollectionSchemas);
		CmisAcceptanceTestSetup.allSchemaTypesSupported(getAppLayerFactory());
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
		taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
		zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

		userServices.execute(
				userServices.addUpdate(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.admin().getUsername(), (req) -> req.addToCollection(zeCollection));
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();

		recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));

		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
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

	@Test
	public void whenDeletingConceptThenError()
			throws RecordServicesException {
		String parentId = "folder2";

		cmisSession = newCMISSessionAsUserInZeCollection(admin);

		int initialRecordsCount = countRecords();

		for (Record record : asList(zeCollectionRecords.taxo1_category2, zeCollectionRecords.taxo1_category2_1,
				zeCollectionRecords.taxo1_fond1, zeCollectionRecords.taxo1_fond1_1, zeCollectionRecords.taxo2_station1,
				zeCollectionRecords.taxo2_unit1_1)) {

			System.out.println(record.getId());
			Folder folder = cmisFolder(record);

			assertThat(folder.getAllowableActions().getAllowableActions())
					.doesNotContain(Action.CAN_DELETE_OBJECT, Action.CAN_DELETE_TREE);

			//This service is returning a list of failed record
			assertThat(folder.deleteTree(true, UnfileObject.DELETE, false)).isNotEmpty();
			assertThat(countRecords()).isEqualTo(initialRecordsCount);
			try {
				folder.delete(true);
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unsupported operation");
			}
			try {
				folder.delete();
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unsupported operation");
			}
		}
	}

	private Folder cmisFolder(Record record) {
		return (Folder) cmisSession.getObject(record.getId());
	}

	private int countRecords() {
		return (int) searchServices.getResultsCount(LogicalSearchQueryOperators.fromAllSchemasIn(zeCollection).returnAll());
	}
}
