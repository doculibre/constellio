package com.constellio.app.api.cmis.accept;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.FolderSchema;
import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup.Records;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@DriverTest
public class MoveObjectAcceptTest extends ConstellioTest {

	UserServices userServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	Users users = new Users();
	CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
	FolderSchema folderSchema = zeCollectionSchemas.new FolderSchema();
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

		userServices.addUpdateUserCredential(
				userServices.getUserCredential(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);

		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();

		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).getWrappedRecord());
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	@Test
	public void whenChangeParentOfSubFolderThenItIsMoved()
			throws Exception {
		Record record = zeCollectionRecords.folder2_1;
		String parentTargetId = zeCollectionRecords.folder1.getId();

		moveObject(record, parentTargetId);
		assertThat(record.getParentId()).isEqualTo(parentTargetId);
		assertParentAndPrincipalPath(record, parentTargetId);
	}

	@Test
	public void whenChangeParentOfFolderThenItIsMoved()
			throws Exception {

		Record record = zeCollectionRecords.folder4;
		String parentTargetId = zeCollectionRecords.taxo1_category1.getId();

		recordServices.update(record.set(folderSchema.taxonomy1(), parentTargetId));

		//moveObject(record, parentTargetId);
		assertParentAndPrincipalPath(record, parentTargetId);
	}

	@Test
	public void whenMovingConceptThenError()
			throws Exception {
		Record record = zeCollectionRecords.taxo1_category2;
		String parentTargetId = zeCollectionRecords.taxo1_fond1_1.getId();

		try {
			moveObject(record, parentTargetId);
			fail("Exception expected");

		} catch (CmisRuntimeException e) {
			//OK
		}
	}

	@Test
	public void whenChangeParentOfFolderToNonPrincipalConceptTaxonomyThenException()
			throws Exception {
		Record record = zeCollectionRecords.folder4;
		String parentTargetId = zeCollectionRecords.taxo2_station2.getId();

		try {
			moveObject(record, parentTargetId);
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Target " + parentTargetId + " record is not in a principal taxonomy");
		}
	}

	private void moveObject(Record record, String parentTargetId) {
		CmisObject object = cmisSession.getObject(record.getId());
		Holder<String> objectIdHolder = new Holder<String>(object.getId());

		cmisSession.getBinding().getObjectService()
				.moveObject(cmisSession.getRepositoryInfo().getId(), objectIdHolder, parentTargetId, record.getId(), null);

		recordServices.refresh(record);
	}

	private void assertParentAndPrincipalPath(Record record, String parentTargetId) {
		boolean flag = false;
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchemaOf(record);
		List<Metadata> parentReferencesMetadatas = schema.getParentReferences();
		List<Metadata> referencesMetadatas = schema.getTaxonomyRelationshipReferences(Arrays.asList(taxonomiesManager
				.getPrincipalTaxonomy(record.getCollection())));

		List<Metadata> allReferencesMetadatas = new ArrayList<>();
		allReferencesMetadatas.addAll(parentReferencesMetadatas);
		allReferencesMetadatas.addAll(referencesMetadatas);

		for (Metadata referenceMetadata : allReferencesMetadatas) {
			if (record.get(referenceMetadata) != null) {
				assertThat(record.<String>get(referenceMetadata)).isEqualTo(parentTargetId);
				assertThat(((String) record.get(Schemas.PRINCIPAL_PATH)).contains(parentTargetId)).isTrue();
				flag = true;
			}
		}
		if (!flag) {
			fail();
		}
	}
}
