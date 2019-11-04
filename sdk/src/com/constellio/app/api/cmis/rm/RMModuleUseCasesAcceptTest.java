package com.constellio.app.api.cmis.rm;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static java.util.Calendar.MARCH;
import static java.util.Calendar.NOVEMBER;
import static org.apache.chemistry.opencmis.commons.enums.VersioningState.MAJOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class RMModuleUseCasesAcceptTest extends ConstellioTest {

	Session cmisSession;
	RMTestRecords records = new RMTestRecords(zeCollection);

	Users users = new Users();
	UserServices userServices;
	String chuckNorrisKey = "chuckNorris-key";
	String chuckNorrisToken;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		authenticationService.changePassword(chuckNorris, "1qaz2wsx");

		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);

		userServices.addUpdateUserCredential(
				userServices.getUserCredential(chuckNorris).setServiceKey(chuckNorrisKey).setSystemAdminEnabled());
		chuckNorrisToken = userServices.generateToken(chuckNorris);
		userServices.addUserToCollection(users.chuckNorris(), zeCollection);
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorrisKey, chuckNorrisToken).onCollection(zeCollection)
				.build();
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());
	}

	//@Test
	//This is a problem in OpenCMIS, the "category" is silently filtered
	//And the server will throw an exception that "category" is required
	//The solution is to fill the field "categoryEntered"
	public void whenCreateFolderUsingAutomaticMetadataThenException() {
		//		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();

		try {
			Map<String, Object> folderProperties = new HashMap<>();
			folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
			folderProperties.put("title", "My folder");
			folderProperties.put("category", records.categoryId_X100);
			folderProperties.put("retentionRuleEntered", records.ruleId_1);
			folderProperties.put("administrativeUnitEntered", records.unitId_10a);
			folderProperties.put("openingDate", new GregorianCalendar(2013, MARCH, 27));
			cmisSession.createFolder(folderProperties, new ObjectIdImpl(records.unitId_10a));
			fail("Exception expected");
		} catch (Exception e) {
			assertThat(e).hasMessage("Metadata 'category' is readonly.");
		}

	}

	@Test
	public void createAFolderInACategory()
			throws Exception {
		//		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();

		Folder categoryZe42 = (Folder) cmisSession.getObject(records.categoryId_ZE42);
		Map<String, Object> folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		folderProperties.put("title", "My folder");
		folderProperties.put("retentionRuleEntered", records.ruleId_1);
		folderProperties.put("administrativeUnitEntered", records.unitId_10a);
		folderProperties.put("openingDate", new GregorianCalendar(2013, MARCH, 27));
		Folder newFolder = categoryZe42.createFolder(folderProperties);

		//
		//-- Validate new folder
		assertThat(newFolder.<String>getPropertyValue("title")).isEqualTo("My folder");
		assertThat(newFolder.<String>getPropertyValue("categoryEntered")).isEqualTo(records.categoryId_ZE42);
		assertThat(newFolder.<String>getPropertyValue("retentionRuleEntered")).isEqualTo(records.ruleId_1);
		assertThat(newFolder.<String>getPropertyValue("copyStatusEntered")).isNull();
		assertThat(newFolder.<String>getPropertyValue("copyStatus")).isEqualTo("P");
		assertThat(newFolder.<String>getPropertyValue("administrativeUnitEntered")).isEqualTo(records.unitId_10a);
		assertThat(newFolder.<GregorianCalendar>getPropertyValue("openingDate")).is(sameDayThan(new LocalDate(2013, 3, 27)));
	}

	@Test
	public void createACategoriesAndFoldersInside()
			throws Exception {
		//		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();
		ObjectId planTaxonomyObjectId = new ObjectIdImpl(cmisSession.getObject("taxo_plan").getId());
		//
		//-- Create a root category
		Map<String, Object> rootCategoryProperties = new HashMap<>();
		rootCategoryProperties.put(PropertyIds.OBJECT_TYPE_ID, "category_default");
		rootCategoryProperties.put("code", "A");
		rootCategoryProperties.put("title", "My root category");
		rootCategoryProperties.put("description", "The description of the root category");
		ObjectId newRootCategoryId = cmisSession.createFolder(rootCategoryProperties, planTaxonomyObjectId);

		Map<String, Object> childCategoryProperties = new HashMap<>();
		childCategoryProperties.put(PropertyIds.OBJECT_TYPE_ID, "category_default");
		childCategoryProperties.put("code", "A100");
		childCategoryProperties.put("title", "My child category");
		childCategoryProperties.put("description", "The description of the child category");
		childCategoryProperties.put("parent", newRootCategoryId.getId());
		childCategoryProperties.put("retentionRules", Arrays.asList(records.ruleId_1, records.ruleId_2));
		ObjectId newChildCategoryId = cmisSession.createFolder(childCategoryProperties, newRootCategoryId);

		//
		//-- Create a folder in the child category
		Map<String, Object> folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		folderProperties.put("title", "My folder");
		folderProperties.put("categoryEntered", newChildCategoryId.getId());
		folderProperties.put("retentionRuleEntered", records.ruleId_1);
		folderProperties.put("administrativeUnitEntered", records.unitId_10a);
		folderProperties.put("openingDate", new GregorianCalendar(2013, MARCH, 27));
		ObjectId newFolderId = cmisSession.createFolder(folderProperties, new ObjectIdImpl(records.unitId_10a));

		//
		//-- Validate new root category
		Folder newRootCategory = (Folder) cmisSession.getObject(newRootCategoryId);
		assertThat(newRootCategory.<String>getPropertyValue("code")).isEqualTo("A");
		assertThat(newRootCategory.<String>getPropertyValue("title")).isEqualTo("My root category");
		assertThat(newRootCategory.<String>getPropertyValue("description"))
				.isEqualTo("The description of the root category");
		assertThat(newRootCategory.<String>getPropertyValue("parent")).isNull();

		//
		//-- Validate new child category
		Folder newChildCategory = (Folder) cmisSession.getObject(newChildCategoryId);
		assertThat(newChildCategory.<String>getPropertyValue("code")).isEqualTo("A100");
		assertThat(newChildCategory.<String>getPropertyValue("title")).isEqualTo("My child category");
		assertThat(newChildCategory.<String>getPropertyValue("description"))
				.isEqualTo("The description of the child category");
		assertThat(newChildCategory.<String>getPropertyValue("parent")).isEqualTo(newRootCategoryId.getId());
		assertThat(newChildCategory.<List<String>>getPropertyValue("retentionRules"))
				.isEqualTo(Arrays.asList(records.ruleId_1, records.ruleId_2));

		//
		//-- Validate new folder
		Folder folder = (Folder) cmisSession.getObject(newFolderId);
		assertThat(folder.<String>getPropertyValue("title")).isEqualTo("My folder");
		assertThat(folder.<String>getPropertyValue("categoryEntered")).isEqualTo(newChildCategoryId.getId());
		assertThat(folder.<String>getPropertyValue("retentionRuleEntered")).isEqualTo(records.ruleId_1);
		assertThat(folder.<String>getPropertyValue("copyStatusEntered")).isNull();
		assertThat(folder.<String>getPropertyValue("copyStatus")).isEqualTo("P");
		assertThat(folder.<String>getPropertyValue("administrativeUnitEntered")).isEqualTo(records.unitId_10a);
		assertThat(folder.<GregorianCalendar>getPropertyValue("openingDate")).is(sameDayThan(new LocalDate(2013, 3, 27)));

	}

	@Test
	public void createAFolderAndADocumentThenModifyThem()
			throws Exception {

		givenTimeIs(new LocalDate(2014, 11, 4));

		//		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();

		//
		//-- Create a folder
		Map<String, Object> folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		folderProperties.put("title", "My folder");
		folderProperties.put("categoryEntered", records.categoryId_X100);
		folderProperties.put("retentionRuleEntered", records.ruleId_1);
		folderProperties.put("administrativeUnitEntered", records.unitId_10a);
		folderProperties.put("openingDate", new GregorianCalendar(2013, MARCH, 27));
		folderProperties.put("formCreatedOn", new GregorianCalendar(2015, NOVEMBER, 4));
		folderProperties.put("formModifiedOn", new GregorianCalendar(2016, NOVEMBER, 4));
		ObjectId newFolderId = cmisSession.createFolder(folderProperties, new ObjectIdImpl(records.unitId_10a));

		//
		//-- Create a document
		Map<String, Object> documentProperties = new HashMap<>();
		documentProperties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");
		documentProperties.put("title", "My document");
		documentProperties.put("folder", newFolderId.getId());
		ObjectId newDocumentId = cmisSession.createFolder(documentProperties, newFolderId);

		//
		//-- Add a content to the new document
		Map<String, Object> contentProperties = new HashMap<>();
		contentProperties.put("metadata", "content");
		contentProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		ContentStream contentStream = new ContentStreamImpl(
				"My document.pdf",
				BigInteger.valueOf(getTestResourceFile("test.pdf").length()),
				"application/pdf",
				getTestResourceInputStream("test.pdf"));
		ObjectId documentContentId = cmisSession.createDocument(contentProperties, newDocumentId, contentStream, MAJOR);

		//
		//-- Validate new folder
		Folder folder = (Folder) cmisSession.getObject(newFolderId);
		assertThat(folder.<String>getPropertyValue("title")).isEqualTo("My folder");
		assertThat(folder.<String>getPropertyValue("categoryEntered")).isEqualTo(records.categoryId_X100);
		assertThat(folder.<String>getPropertyValue("retentionRuleEntered")).isEqualTo(records.ruleId_1);
		assertThat(folder.<String>getPropertyValue("copyStatusEntered")).isNull();
		assertThat(folder.<String>getPropertyValue("copyStatus")).isEqualTo("P");
		assertThat(folder.<String>getPropertyValue("administrativeUnitEntered")).isEqualTo(records.unitId_10a);
		assertThat(folder.<GregorianCalendar>getPropertyValue("openingDate")).is(sameDayThan(new LocalDate(2013, 3, 27)));
		assertThat(folder.<GregorianCalendar>getPropertyValue("formCreatedOn")).is(sameDayThan(new LocalDate(2015, 11, 4)));
		assertThat(folder.getCreationDate()).is(sameDayThan(new LocalDate(2015, 11, 4)));
		assertThat(folder.<GregorianCalendar>getPropertyValue("formModifiedOn")).is(sameDayThan(new LocalDate(2016, 11, 4)));
		assertThat(folder.getLastModificationDate()).is(sameDayThan(new LocalDate(2016, 11, 4)));
		//
		//-- Validate new document
		Folder document = (Folder) cmisSession.getObject(newDocumentId);
		assertThat(document.<String>getPropertyValue("title")).isEqualTo("My document");
		assertThat(document.<String>getPropertyValue("folder")).isEqualTo(newFolderId.getId());
		assertThat(document.<String>getPropertyValue("category")).isEqualTo(records.categoryId_X100);
		assertThat(document.<String>getPropertyValue("retentionRule")).isEqualTo(records.ruleId_1);
		assertThat(document.<String>getPropertyValue("administrativeUnit")).isEqualTo(records.unitId_10a);
		assertThat(document.<GregorianCalendar>getPropertyValue("formCreatedOn")).is(sameDayThan(new LocalDate(2014, 11, 4)));
		assertThat(document.getCreationDate()).is(sameDayThan(new LocalDate(2014, 11, 4)));
		assertThat(document.<GregorianCalendar>getPropertyValue("formModifiedOn")).is(sameDayThan(new LocalDate(2014, 11, 4)));
		assertThat(document.getLastModificationDate()).is(sameDayThan(new LocalDate(2014, 11, 4)));

		//
		//-- Validate document's content
		Document content = (Document) cmisSession.getObject(documentContentId);
		assertThat(content.getContentStreamFileName()).isEqualTo("My document.pdf");

		//
		//-- Modify the folder
		folderProperties = new HashMap<>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "folder_default");
		folderProperties.put("categoryEntered", records.categoryId_X110);
		folderProperties.put("retentionRuleEntered", records.ruleId_2);
		folderProperties.put("copyStatusEntered", "P");
		folder.updateProperties(folderProperties);

		//
		//-- Wait until the document has been reindexed
		waitForBatchProcess();

		//
		//-- Validate new folder
		folder = (Folder) cmisSession.getObject(newFolderId);
		assertThat(folder.<String>getPropertyValue("title")).isEqualTo("My folder");
		assertThat(folder.<String>getPropertyValue("categoryEntered")).isEqualTo(records.categoryId_X110);
		assertThat(folder.<String>getPropertyValue("retentionRuleEntered")).isEqualTo(records.ruleId_2);
		assertThat(folder.<String>getPropertyValue("copyStatusEntered")).isEqualTo("P");
		assertThat(folder.<String>getPropertyValue("copyStatus")).isEqualTo("P");
		assertThat(folder.<String>getPropertyValue("administrativeUnitEntered")).isEqualTo(records.unitId_10a);
		assertThat(folder.<GregorianCalendar>getPropertyValue("openingDate")).is(sameDayThan(new LocalDate(2013, 3, 27)));

		//
		//-- Validate new document
		document = (Folder) cmisSession.getObject(newDocumentId);
		assertThat(document.<String>getPropertyValue("title")).isEqualTo("My document");
		assertThat(document.<String>getPropertyValue("folder")).isEqualTo(newFolderId.getId());
		assertThat(document.<String>getPropertyValue("category")).isEqualTo(records.categoryId_X110);
		assertThat(document.<String>getPropertyValue("retentionRule")).isEqualTo(records.ruleId_2);
		assertThat(document.<String>getPropertyValue("administrativeUnit")).isEqualTo(records.unitId_10a);

	}

	private Condition<? super Object> sameDayThan(final LocalDate localDate) {
		return new Condition<Object>() {
			@Override
			public boolean matches(Object value) {
				Calendar calendar = (Calendar) value;
				assertThat(calendar.get(Calendar.YEAR)).isEqualTo(localDate.getYear());
				assertThat(calendar.get(Calendar.MONTH) + 1).isEqualTo(localDate.getMonthOfYear());
				assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(localDate.getDayOfMonth());
				return true;
			}
		};
	}

}
