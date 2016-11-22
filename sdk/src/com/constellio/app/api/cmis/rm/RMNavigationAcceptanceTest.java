package com.constellio.app.api.cmis.rm;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.MutableProperties;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisSinglevalueContentManagementAcceptTest;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RMNavigationAcceptanceTest extends ConstellioTest {

	AuthorizationsServices authorizationsServices;
	RecordServices recordServices;
	Users users = new Users();
	Session session;
	RMSchemasRecordsServices rm;

	RMTestRecords records = new RMTestRecords(zeCollection);

	private final String PDF_MIMETYPE = "application/pdf";
	private long pdf1Length = 170039L;
	private long pdf2Length = 167347L;
	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
	private String pdf2Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withDocumentsHavingContent());

		recordServices = getModelLayerFactory().newRecordServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		users.setUp(getModelLayerFactory().newUserServices());

		recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.gandalfIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenUserDoRequestsOnAFolderThenAllActionsLogged()
			throws Exception {

		clearLogs();

		as(gandalf).session.getObject(records.folder_A02);
		as(chuckNorris).session.getObjectByPath("/taxo_plan/categoryId_X/categoryId_X100/" + records.folder_C30);
		as(admin).session.getBinding().getObjectService()
				.getProperties(session.getRepositoryInfo().getId(), records.folder_A06, null, null);
		recordServices.flush();
		assertThatRecords(rm.searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
				tuple("view_folder", "gandalf", "A02"),
				tuple("view_folder", "chuck", "C30"),
				tuple("view_folder", "admin", "A06")
		);

		clearLogs();

		as(gandalf).session.getObject(records.folder_A02).updateProperties(asMap("title", "newTitle1"))
				.updateProperties(asMap("title", "newTitle2"));
		MutableProperties properties = new PropertiesImpl();

		as(chuckNorris).session.getObject(records.folder_C30).rename("newTitle3");
		((Folder) as(admin).session.getObject(records.folder_A05))
				.move(new ObjectIdImpl(records.unitId_10a), new ObjectIdImpl(records.unitId_20));
		String newFolderId = ((Folder) as(gandalf).session.getObject(records.folder_A07)).createFolder(
				asMap("title", "test", PropertyIds.OBJECT_TYPE_ID, "folder_default", "openingDate", Calendar.getInstance()))
				.getId();
		((Folder) as(chuckNorris).session.getObject(records.folder_A12)).deleteTree(true, UnfileObject.DELETE, true);
		recordServices.flush();

		List<String> documentsInA05 = getModelLayerFactory().newSearchServices().searchRecordIds(from(rm.document.schemaType())
				.where(rm.document.folder()).isEqualTo(records.folder_A05));

		assertThatRecords(rm.searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
				tuple("view_folder", "gandalf", "A02"),
				tuple("modify_folder", "gandalf", "A02"),
				tuple("modify_folder", "gandalf", "A02"),
				tuple("view_folder", "chuck", "C30"),
				tuple("modify_folder", "chuck", "C30"),
				tuple("view_folder", "admin", "A05"),
				tuple("modify_folder", "admin", "A05"),
				tuple("view_folder", "gandalf", "A07"),
				tuple("view_folder", "chuck", "A12"),
				tuple("delete_folder", "chuck", "A12"),
				tuple("create_folder", "gandalf", newFolderId),
				tuple("view_folder", "gandalf", newFolderId),
				tuple("modify_document", "admin", documentsInA05.get(0)),
				tuple("modify_document", "admin", documentsInA05.get(1)),
				tuple("modify_document", "admin", documentsInA05.get(2))
		);

	}

	@Test
	public void whenUserDoRequestsOnADocumentThenAllActionsLogged()
			throws Exception {

		clearLogs();

		String contentId = ((Folder) as(gandalf).session.getObject(records.document_B30)).getChildren().iterator().next().getId();
		as(chuckNorris).session
				.getObjectByPath("/taxo_plan/categoryId_X/categoryId_X100/" + records.folder_A49 + "/" + records.document_A49);
		as(admin).session.getBinding().getObjectService().getProperties(zeCollection, records.document_A79, null, null);
		as(aliceWonderland).session.getObject(contentId);
		recordServices.flush();
		assertThatRecords(rm.searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
				tuple("view_document", "gandalf", records.document_B30),
				tuple("view_document", "chuck", records.document_A49),
				tuple("view_document", "admin", records.document_A79),
				tuple("view_document", "alice", records.document_B30)
		);

		clearLogs();

		as(gandalf).session.getObject(records.document_B30).updateProperties(asMap("title", "newTitle1"))
				.updateProperties(asMap("title", "newTitle2"));
		MutableProperties properties = new PropertiesImpl();
		((Folder) as(chuckNorris).session.getObject(records.document_B33)).deleteTree(true, UnfileObject.DELETE, true);
		as(chuckNorris).session.getObject(records.document_A49).rename("newTitle3");
		((Folder) as(admin).session.getObject(records.document_A79))
				.move(new ObjectIdImpl(records.folder_A79), new ObjectIdImpl(records.folder_A80));
		as(chuckNorris).session.getObject(contentId).delete();
		String newDocumentId = ((Folder) as(gandalf).session.getObject(records.folder_A07))
				.createFolder(asMap("title", "test", PropertyIds.OBJECT_TYPE_ID, "document_default")).getId();

		Document contentA19 = (Document) ((Folder) as(chuckNorris).session.getObject(records.document_A19)).getChildren()
				.iterator().next();
		contentA19.checkOut();
		contentA19.checkIn(false, new HashMap<String, Object>(), null, null);

		contentA19 = (Document) ((Folder) as(gandalf).session.getObject(records.document_A19)).getChildren().iterator().next();
		contentA19.checkOut();
		contentA19.checkIn(false, new HashMap<String, Object>(), pdf2ContentStream(), null);

		recordServices.flush();
		assertThatRecords(rm.searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
				tuple("view_document", "gandalf", "docB30"),
				tuple("modify_document", "gandalf", "docB30"),
				tuple("modify_document", "gandalf", "docB30"),
				tuple("modify_document", "chuck", "docB30"),
				tuple("view_document", "chuck", "docB33"),
				tuple("delete_document", "chuck", "docB33"),
				tuple("view_document", "chuck", "docA49"),
				tuple("modify_document", "chuck", "docA49"),
				tuple("view_document", "admin", "docA79"),
				tuple("modify_document", "admin", "docA79"),
				tuple("view_folder", "gandalf", "A07"),
				tuple("view_document", "chuck", "docB30"),
				tuple("view_folder", "admin", "A80"),
				tuple("view_document", "chuck", "docA19"),
				tuple("borrow_document", "chuck", "docA19"),
				tuple("return_document", "chuck", "docA19"),
				tuple("modify_document", "chuck", "docA19"),
				tuple("view_document", "gandalf", "docA19"),
				tuple("borrow_document", "gandalf", "docA19"),
				tuple("return_document", "gandalf", "docA19"),
				tuple("modify_document", "gandalf", "docA19"),
				tuple("create_document", "gandalf", newDocumentId),
				tuple("view_document", "gandalf", newDocumentId)
		);
	}

	private void clearLogs() {
		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		try {
			solrClient.deleteByQuery("schema_s:event_*");
			solrClient.commit();
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void whenNavigatingInTaxonomiesThenOK()
			throws Exception {

		for (String user : asList(admin, aliceWonderland, chuckNorris, gandalf)) {
			session = newCMISSessionAsUserInZeCollection(user);
			Folder folder = session.getRootFolder();

			assertThatChildrensOf(folder).describedAs("seen by " + user).containsOnly(
					tuple("taxo_plan", "taxonomy", "Plan de classification"),
					tuple("taxo_containers", "taxonomy", "Emplacements"),
					tuple("taxo_admUnits", "taxonomy", "Unités administratives")
			);

			assertThatChildrensOf(folder("/taxo_plan")).describedAs("seen by " + user).containsOnly(
					tuple("categoryId_Z", "category_default", "Ze category"),
					tuple("categoryId_X", "category_default", "Xe category")
			);

			assertThatChildrensOf(folder("/taxo_plan/categoryId_X")).describedAs("seen by " + user).containsOnly(
					tuple("categoryId_X100", "category_default", "X100"),
					tuple("categoryId_X13", "category_default", "Agent Secreet")
			);

			assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X13")).describedAs("seen by " + user).isEmpty();

		}

		for (String user : asList(admin, aliceWonderland, chuckNorris)) {
			as(user).assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X100")).containsOnly(
					tuple("categoryId_X110", "category_default", "X110"),
					tuple("categoryId_X120", "category_default", "X120"),
					tuple("B06", "folder_default", "Framboise"),
					tuple("C06", "folder_default", "Chou-fleur"),
					tuple("A18", "folder_default", "Cheval"),
					tuple("A16", "folder_default", "Chat"),
					tuple("B32", "folder_default", "Pêche"),
					tuple("A17", "folder_default", "Chauve-souris"),
					tuple("C32", "folder_default", "Maïs")
			);
		}

		as(gandalf).assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X100")).containsOnly(
				tuple("categoryId_X110", "category_default", "X110"),
				tuple("categoryId_X120", "category_default", "X120"),
				tuple("B06", "folder_default", "Framboise"),
				tuple("C06", "folder_default", "Chou-fleur"),
				tuple("A18", "folder_default", "Cheval"),
				tuple("A16", "folder_default", "Chat"),
				tuple("B32", "folder_default", "Pêche"),
				tuple("A17", "folder_default", "Chauve-souris"),
				tuple("C32", "folder_default", "Maïs")
		);

		as(sasquatch).assertThatChildrensOf(folder("/taxo_plan/categoryId_X/categoryId_X100")).containsOnly(
				tuple("categoryId_X110", "category_default", "X110"),
				tuple("categoryId_X120", "category_default", "X120")
		);
	}

	private Folder folder(String path) {
		return (Folder) session.getObjectByPath(path);
	}

	private RMNavigationAcceptanceTest as(String user) {
		session = newCMISSessionAsUserInCollection(user, zeCollection);
		return this;
	}

	private ContentStream pdf2ContentStream()
			throws IOException {
		String filename = "pdf2.pdf";
		BigInteger length = BigInteger.valueOf(pdf2Length);
		String mimetype = PDF_MIMETYPE;
		InputStream stream = getTestResourceInputStream(CmisSinglevalueContentManagementAcceptTest.class, "pdf2.pdf");
		return new ContentStreamImpl(filename, length, mimetype, stream);
	}

	private ListAssert<Tuple> assertThatChildrensOf(Folder folder) {
		List<Tuple> tuples = new ArrayList<>();
		for (Iterator<CmisObject> objectIterator = folder.getChildren().iterator(); objectIterator.hasNext(); ) {
			CmisObject child = objectIterator.next();
			Tuple tuple = new Tuple();
			tuples.add(tuple);

			tuple.addData(child.getId());
			tuple.addData(child.getType().getId());
			tuple.addData(child.getName());
		}
		return assertThat(tuples);
	}
}
