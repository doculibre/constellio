package com.constellio.app.api.cmis.accept;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DriverTest
public class CreateFolderRequestAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	Records zeCollectionRecords;
	TaxonomiesSearchServices taxonomiesSearchServices;

	Session cmisSession;

	String chuckNorrisKey = "chuckNorrisKey";
	String chuckNorrisToken;

	String bobKey = "bobKey";
	String bobToken;

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

		userServices.execute(users.bobAddUpdateRequest().setServiceKey(bobKey).setSystemAdminEnabled());
		userServices.execute(users.chuckNorrisAddUpdateRequest().setServiceKey(chuckNorrisKey).setSystemAdminEnabled());

		chuckNorrisToken = userServices.generateToken(chuckNorris);
		bobToken = userServices.generateToken(bobGratton);
		userServices.execute(users.bob().getUsername(), (req) -> req.addCollection(zeCollection));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addCollection(zeCollection));

		cmisSession = givenAdminSessionOnZeCollection();
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());

		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void whenCreatingAFolderThenFolderHasRightValues()
			throws Exception {
		givenTimeIs(new LocalDate(2014, 11, 04));
		String id = createNewFolderWithTestProperties("zetaxo1_category1", "folder_default");

		Record createdRecord = recordServices.getDocumentById(id);
		assertThat(createdRecord).isNotNull();
		assertThat(createdRecord.<String>get(Schemas.TITLE)).isEqualTo("testFolder");
		assertThat(((LocalDateTime) createdRecord.get(Schemas.CREATED_ON)).toDate()).isEqualTo(
				new LocalDate(2014, 11, 04).toDate());
		assertThat(createdRecord.<String>get(Schemas.CREATED_BY)).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(createdRecord.<String>get(zeCollectionSchemas.folderSchema.taxonomy1())).isEqualTo("zetaxo1_category1");
		assertThat(createdRecord.<Double>get(zeCollectionSchemas.folderSchema.numberMeta())).isEqualTo(42.666);
	}

	@Test
	public void whenCreatingACategoryThenCategoryHasRightValues()
			throws Exception {
		givenTimeIs(new LocalDate(2014, 11, 04));
		String id = createNewCategoryWithTestProperties("zetaxo1_fond1", "category_default");

		Record createdRecord = recordServices.getDocumentById(id);
		assertThat(createdRecord).isNotNull();
		assertThat(createdRecord.<String>get(Schemas.TITLE)).isEqualTo("testCategory");
		assertThat(((LocalDateTime) createdRecord.get(Schemas.CREATED_ON)).toDate()).isEqualTo(
				new LocalDate(2014, 11, 04).toDate());
		assertThat(createdRecord.<String>get(Schemas.CREATED_BY)).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(createdRecord.<String>get(zeCollectionSchemas.category.parentOfDocumentFond())).isEqualTo("zetaxo1_fond1");
	}

	@Test
	public void whenCreatingACollectionThenException()
			throws Exception {
		String rootId = cmisSession.getRootFolder().getId();
		try {
			createNewCollection(rootId, "collection_default");
		} catch (CmisRuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("Cannot create collection via CMIS.");
		}
	}

	@Test
	public void whenCreatingATaxonomyThenException()
			throws Exception {
		try {
			createNewTaxonomy(zeCollection, "taxonomy");
		} catch (CmisRuntimeException ex) {
			assertThat(ex.getMessage()).isEqualTo("Cannot create taxonomy via CMIS.");
		}
	}

	private String createNewFolderWithTestProperties(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("title", "testFolder");
		newFolderProperties.put("numberMeta", 42.666);
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private String createNewCategoryWithTestProperties(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("title", "testCategory");
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private String createNewCollection(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		newFolderProperties.put("title", "testCollection");
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private String createNewTaxonomy(String parent, String objectType) {
		ObjectId parentFolderId = new ObjectIdImpl(parent);
		Map<String, Object> newFolderProperties = new HashMap<>();
		newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
		return cmisSession.createFolder(newFolderProperties, parentFolderId).getId();
	}

	private Session givenAdminSessionOnZeCollection()
			throws RecordServicesException {
		return newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection).build();
	}
}
