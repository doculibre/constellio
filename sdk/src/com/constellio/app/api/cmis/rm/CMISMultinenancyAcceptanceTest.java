package com.constellio.app.api.cmis.rm;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.api.cmis.accept.CmisSinglevalueContentManagementAcceptTest;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.data.utils.TenantUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.MutableProperties;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.apache.chemistry.opencmis.commons.enums.IncludeRelationships.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class CMISMultinenancyAcceptanceTest extends ConstellioTest {

	TenantLocal<AuthorizationsServices> authorizationsServices = new TenantLocal<>();
	TenantLocal<RecordServices> recordServices = new TenantLocal<>();
	TenantLocal<Users> users = new TenantLocal<>();
	TenantLocal<Session> session = new TenantLocal<>();
	TenantLocal<RMSchemasRecordsServices> rm = new TenantLocal<>();

	TenantLocal<RMTestRecords> records = new TenantLocal<>();

	private final String PDF_MIMETYPE = "application/pdf";
	private long pdf1Length = 170039L;
	private long pdf2Length = 167347L;
	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
	private String pdf2Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";

	@Before
	public void setUp()
			throws Exception {
		givenTwoTenants(() -> {
			givenDisabledAfterTestValidations();
			records.set(new RMTestRecords(zeCollection));
			users.set(new Users());
			prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users.get())
					.withRMTest(records.get()).withFoldersAndContainersOfEveryStatus()
					.withDocumentsHavingContent());

			recordServices.set(getModelLayerFactory().newRecordServices());
			authorizationsServices.set(getModelLayerFactory().newAuthorizationsServices());


			users.get().setUp(getModelLayerFactory().newUserServices(), zeCollection);

			recordServices.get().update(users.get().adminIn(zeCollection).setCollectionAllAccess(true));
			recordServices.get().update(users.get().aliceIn(zeCollection).setCollectionReadAccess(true));
			recordServices.get().update(users.get().chuckNorrisIn(zeCollection).setCollectionAllAccess(true));
			recordServices.get().update(users.get().gandalfIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

			givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);
			CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());

			rm.set(new RMSchemasRecordsServices(zeCollection, getAppLayerFactory()));
		});

	}


	@Test
	public void givenDifferentRecordsWithSameIdInTwoTenantsThenRetrieveTheGoodOne()
			throws Exception {

		TenantUtils.setTenant("1");
		recordServices.get().add(records.get().getFolder_A42().setTitle("Dossier 42 du tenant 1"));

		TenantUtils.setTenant("2");
		recordServices.get().add(records.get().getFolder_A42().setTitle("Dossier 42 du tenant 2"));


		TenantUtils.setTenant("1");
		session.set(newCMISSessionAsUserInZeCollection(admin));
		Folder folder = (Folder) session.get().getObject(records.get().getFolder_A42().getId());
		assertThat(folder.getName()).isEqualTo("Dossier 42 du tenant 1");

		TenantUtils.setTenant("2");
		session.set(newCMISSessionAsUserInZeCollection(admin));
		folder = (Folder) session.get().getObject(records.get().getFolder_A42().getId());
		assertThat(folder.getName()).isEqualTo("Dossier 42 du tenant 2");

	}

	@Test
	public void givenRecordAsAuthOnALeafRecordThenCanCallGetChildrenAndGetParentsOnNodeLeadingToIt()
			throws Exception {

		forEachTenants(() -> {

			Record folder1Doc = getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(
					from(rm.get().document.schemaType()).where(rm.get().document.folder()).isEqualTo(records.get().folder_A05))).get(0);

			authorizationsServices.get().add(authorizationForUsers(users.get().robinIn(zeCollection))
					.on(folder1Doc).givingReadAccess(), users.get().adminIn(zeCollection));

			assertThat(getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection)
					.hasUserAccessToSomethingInPrincipalConcept(users.get().robinIn(zeCollection), records.get().getUnit10a().getWrappedRecord(), false, false))
					.isTrue();

			assertThat(getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection)
					.hasUserAccessToSomethingInPrincipalConcept(users.get().robinIn(zeCollection), records.get().getUnit10().getWrappedRecord(), false, false))
					.isTrue();


			waitForBatchProcess();

			session.set(newCMISSessionAsUserInZeCollection(robin));
			assertThatChildren(session.get().getRootFolder()).containsOnly("taxo_plan", "taxo_containers", "taxo_admUnits");
			assertThatChildren("taxo_admUnits").containsOnly("unitId_10");
			assertThatChildren("unitId_10").containsOnly("unitId_10a");
			assertThatChildren("unitId_10a").containsOnly(records.get().folder_A05);
			assertThatChildren(records.get().folder_A05).containsOnly(folder1Doc.getId());
		});
	}

	@Test
	public void whenUserDoRequestsOnAFolderThenAllActionsLogged()
			throws Exception {

		forEachTenants(() -> {
			clearLogs();

			as(gandalf).session.get().getObject(records.get().folder_A02);
			as(chuckNorris).session.get().getObjectByPath("/taxo_plan/categoryId_X/categoryId_X100/" + records.get().folder_C30);
			as(admin).session.get().getBinding().getObjectService()
					.getProperties(session.get().getRepositoryInfo().getId(), records.get().folder_A06, null, null);
			recordServices.get().flush();
			assertThatRecords(rm.get().searchEvents(ALL)).extracting("type", "username", "recordId").isEmpty();
			givenConfig(RMConfigs.LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS, true);

			as(gandalf).session.get().getObject(records.get().folder_A02);
			as(chuckNorris).session.get().getObjectByPath("/taxo_plan/categoryId_X/categoryId_X100/" + records.get().folder_C30);
			as(admin).session.get().getBinding().getObjectService()
					.getProperties(session.get().getRepositoryInfo().getId(), records.get().folder_A06, null, null);
			recordServices.get().flush();
			assertThatRecords(rm.get().searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
					tuple("view_folder", "gandalf", "A02"),
					tuple("view_folder", "chuck", "C30"),
					tuple("view_folder", "admin", "A06")
			);

			clearLogs();

			as(gandalf).session.get().getObject(records.get().folder_A02).updateProperties(asMap("title", "newTitle1"))
					.updateProperties(asMap("title", "newTitle2"));
			MutableProperties properties = new PropertiesImpl();

			as(chuckNorris).session.get().getObject(records.get().folder_C30).rename("newTitle3");
			((Folder) as(admin).session.get().getObject(records.get().folder_A05))
					.move(new ObjectIdImpl(records.get().unitId_10a), new ObjectIdImpl(records.get().unitId_20));
			String newFolderId = ((Folder) as(gandalf).session.get().getObject(records.get().folder_A07)).createFolder(
					asMap("title", "test", PropertyIds.OBJECT_TYPE_ID, "folder_default", "openingDate", Calendar.getInstance()))
					.getId();
			((Folder) as(chuckNorris).session.get().getObject(records.get().folder_A12)).deleteTree(true, UnfileObject.DELETE, true);
			recordServices.get().flush();

			List<String> documentsInA05 = getModelLayerFactory().newSearchServices().searchRecordIds(from(rm.get().document.schemaType())
					.where(rm.get().document.folder()).isEqualTo(records.get().folder_A05));

			assertThatRecords(rm.get().searchEvents(ALL)).extracting("type", "username", "recordId").contains(
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
		});
	}

	@Test
	public void whenUserDoRequestsOnADocumentThenAllActionsLogged()
			throws Exception {

		forEachTenants(() -> {
			clearLogs();

			String contentId = ((Folder) as(gandalf).session.get().getObject(records.get().document_B30)).getChildren().iterator().next().getId();

			as(chuckNorris).session.get()
					.getObjectByPath("/taxo_plan/categoryId_X/categoryId_X100/" + records.get().folder_A49 + "/" + records.get().document_A49);
			as(admin).session.get().getBinding().getObjectService().getProperties(zeCollection, records.get().document_A79, null, null);
			as(aliceWonderland).session.get().getObject(contentId);
			recordServices.get().flush();
			assertThatRecords(rm.get().searchEvents(ALL)).extracting("type", "username", "recordId").isEmpty();
			givenConfig(RMConfigs.LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS, true);

			contentId = ((Folder) as(gandalf).session.get().getObject(records.get().document_B30)).getChildren().iterator().next().getId();
			as(chuckNorris).session.get()
					.getObjectByPath("/taxo_plan/categoryId_X/categoryId_X100/" + records.get().folder_A49 + "/" + records.get().document_A49);
			as(admin).session.get().getBinding().getObjectService().getProperties(zeCollection, records.get().document_A79, null, null);
			as(aliceWonderland).session.get().getObject(contentId);
			recordServices.get().flush();
			assertThatRecords(rm.get().searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
					tuple("view_document", "gandalf", records.get().document_B30),
					tuple("view_document", "chuck", records.get().document_A49),
					tuple("view_document", "admin", records.get().document_A79),
					tuple("view_document", "alice", records.get().document_B30)
			);

			clearLogs();

			as(gandalf).session.get().getObject(records.get().document_B30).updateProperties(asMap("title", "newTitle1"))
					.updateProperties(asMap("title", "newTitle2"));
			MutableProperties properties = new PropertiesImpl();
			((Folder) as(chuckNorris).session.get().getObject(records.get().document_B33)).deleteTree(true, UnfileObject.DELETE, true);
			as(chuckNorris).session.get().getObject(records.get().document_A49).rename("newTitle3");
			((Folder) as(admin).session.get().getObject(records.get().document_A79))
					.move(new ObjectIdImpl(records.get().folder_A79), new ObjectIdImpl(records.get().folder_A80));
			as(chuckNorris).session.get().getObject(contentId).delete();
			String newDocumentId = ((Folder) as(gandalf).session.get().getObject(records.get().folder_A07))
					.createFolder(asMap("title", "test", PropertyIds.OBJECT_TYPE_ID, "document_default")).getId();

			Document contentA19 = (Document) ((Folder) as(chuckNorris).session.get().getObject(records.get().document_A19)).getChildren()
					.iterator().next();
			contentA19.checkOut();
			contentA19.checkIn(false, new HashMap<String, Object>(), null, null);

			contentA19 = (Document) ((Folder) as(gandalf).session.get().getObject(records.get().document_A19)).getChildren().iterator().next();
			contentA19.checkOut();
			contentA19.checkIn(false, new HashMap<String, Object>(), pdf2ContentStream(), null);

			recordServices.get().flush();
			assertThatRecords(rm.get().searchEvents(ALL)).extracting("type", "username", "recordId").containsOnly(
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
		});
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

		forEachTenants(() -> {
			for (String user : asList(admin, aliceWonderland, chuckNorris, gandalf)) {
				session.set(newCMISSessionAsUserInZeCollection(user));
				Folder folder = session.get().getRootFolder();

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

			TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
			recordServices.get()
					.add((RecordWrapper) tasks.newTask().setTitle("Ze task").set(RMTask.ADMINISTRATIVE_UNIT, records.get().unitId_11b));

			as(admin).assertThatChildrensOf(folder(records.get().unitId_11b)).containsOnly(
					tuple("B31", "folder_default", "Orange"),
					tuple("B01", "folder_default", "Abricot"),
					tuple("B05", "folder_default", "Fraise"),
					tuple("B09", "folder_default", "Melon"),
					tuple("B07", "folder_default", "Kiwi"),
					tuple("B03", "folder_default", "Citron")
			);
		});
	}

	@Test
	public void whenGetObjectByIdThenOnlyWorkWithSupportedTypes()
			throws Exception {

		forEachTenants(() -> {
			//		try {
			//			as(admin).session.get().getObject(records.get().containerId_bac06);
			//			fail("Exception expected");
			//		} catch (CmisRuntimeException e) {
			//			assertThat(e.getMessage()).isEqualTo("todo");
			//		}

			try {
				as(admin).session.get().getObject(records.get().list_04);
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unknown type: decommissioningList_default");
			}

			try {
				as(admin).session.get().getObject(records.get().documentTypeId_4);
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unknown type: ddvDocumentType_default");
			}

			try {
				as(admin).session.get().getObject(records.get().ruleId_3);
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unknown type: retentionRule_default");
			}

			try {
				as(admin).session.get().getObject(records.get().subdivId_1);
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unknown type: uniformSubdivision_default");
			}

			try {
				as(admin).session.get().getObject(records.get().getAlice().getId());
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unknown type: user_default");
			}

			try {
				as(admin).session.get().getObject(records.get().getLegends().getId());
				fail("Exception expected");
			} catch (CmisRuntimeException e) {
				assertThat(e.getMessage()).isEqualTo("Unknown type: group_default");
			}
		});
	}

	private Folder folder(String pathOrId) {
		if (pathOrId.startsWith("/")) {
			return (Folder) session.get().getObjectByPath(pathOrId);
		} else {
			return (Folder) session.get().getObject(pathOrId);
		}
	}

	private CMISMultinenancyAcceptanceTest as(String user) {
		session.set(newCMISSessionAsUserInCollection(user, zeCollection));
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

	private ListAssert<Object> assertThatChildren(String id) {
		Folder folder = (Folder) session.get().getObject(id);
		return assertThatChildren(folder);
	}

	private ListAssert<Object> assertThatChildren(Folder folder) {

		List<CmisObject> children = new ArrayList<>();
		for (CmisObject object : folder.getChildren()) {
			List<ObjectParentData> bindingParents = session.get().getBinding().getNavigationService().getObjectParents(
					session.get().getRepositoryInfo().getId(), object.getId(), null, false, NONE, null, true, null);
			assertThat(bindingParents).extracting("object.id").contains(folder.getId());
			children.add(object);
		}
		return assertThat(children).extracting("id");
	}

}
