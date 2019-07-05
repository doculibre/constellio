package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentView;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Constellio on 2016-12-08.
 */
public class DocumentActionsPresenterUtilsAcceptanceTest extends ConstellioTest {
	private static final String TEST_ID = "CartEmlServiceAcceptanceTest-inputStreams";
	@Mock
	DisplayDocumentView view;
	@Mock
	RecordVO recordVO;
	Users users = new Users();
	RecordServices recordServices;
	UserServices userServices;
	ESSchemasRecordsServices es;
	Cart cart;
	String title1 = "Chevreuil.odt";
	String title2 = "Grenouille.odt";
	private IOServices ioServices;
	private ContentManager contentManager;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	RolesManager rolesManager;

	Content content1_title1, content1_title2, content2_title1, content2_title2;
	File content1File, content2File;
	Document document11WithContent1HavingTitle1, document12WithContent1HavingTitle2, document21WithContent2HavingTitle1,
			document22WithContent2HavingTitle2, documentWithoutContent;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);
		ConstellioFactories constellioFactories = getConstellioFactories();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(constellioFactories);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
		contentManager = getModelLayerFactory().getContentManager();
		ioServices = getDataLayerFactory().getIOServicesFactory().newIOServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		rolesManager = getAppLayerFactory().getModelLayerFactory().getRolesManager();

		initTestData();
	}

	@Test
	public void whenDeleteVersionThenEventIsCreated()
			throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(document11WithContent1HavingTitle1.setContent(content1_title1).setTitle("11"));
		recordServices.execute(transaction);


		DocumentVO documentVo = new DocumentToVOBuilder(getModelLayerFactory()).build(document11WithContent1HavingTitle1.getWrappedRecord(),
				RecordVO.VIEW_MODE.FORM, FakeSessionContext.adminInCollection(zeCollection));

		DocumentActionsPresenterUtils presenterUtils = spy(new DocumentActionsPresenterUtils(view));
		doReturn(true).when(presenterUtils).isDeleteContentVersionPossible(any(ContentVersionVO.class));
		doReturn(null).when(presenterUtils).buildContentVersionVO(any(Content.class));

		presenterUtils.setRecordVO(documentVo);
		presenterUtils.deleteContentVersionButtonClicked(documentVo.getContent());


		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> recordList = searchServices.search(new LogicalSearchQuery().setCondition(from(rm.eventSchemaType()).returnAll()));
		assertThat(recordList).hasSize(1);
		assertThat(rm.wrapEvent(recordList.get(0)).getRecordVersion()).isEqualTo(content1_title1.getCurrentVersion().getVersion());
	}

	@Test
	public void whenDeleteVersionPossibleWithRolePermissionAndDeleteAccess() throws RecordServicesException {
		Role manager = rolesManager.getRole(zeCollection, RMRoles.MANAGER);
		List<String> permissions = new ArrayList<>(manager.getOperationPermissions());
		if (!permissions.contains(CorePermissions.DELETE_CONTENT_VERSION)) {
			permissions.add(CorePermissions.DELETE_CONTENT_VERSION);
		}
		rolesManager.updateRole(manager.withPermissions(withoutDuplicates(permissions)));
		manager = rolesManager.getRole(zeCollection, RMRoles.MANAGER);

		User sasquatch = users.sasquatchIn(zeCollection);
		sasquatch.setCollectionDeleteAccess(true);
		recordServices.update(sasquatch.setUserRoles(manager.getCode()));


		Transaction transaction = new Transaction();
		transaction.add(document21WithContent2HavingTitle1.setContent(content2_title1).setTitle("21"));
		recordServices.execute(transaction);


		DocumentVO documentVo = new DocumentToVOBuilder(getModelLayerFactory()).build(document21WithContent2HavingTitle1.getWrappedRecord(),
				RecordVO.VIEW_MODE.FORM, FakeSessionContext.sasquatchInCollection(zeCollection));

		DocumentActionsPresenterUtils presenterUtils = spy(new DocumentActionsPresenterUtils(view));
		doReturn(sasquatch).when(presenterUtils).getCurrentUser();
		presenterUtils.setRecordVO(documentVo);
		assertThat(presenterUtils.isDeleteContentVersionPossible()).isTrue();
	}

	@Test
	public void whenDeleteContentVersionImpossibleWithoutRolePermission() throws RecordServicesException {
		Role manager = rolesManager.getRole(zeCollection, RMRoles.MANAGER);
		List<String> permissions = new ArrayList<>(manager.getOperationPermissions());
		if (permissions.contains(CorePermissions.DELETE_CONTENT_VERSION)) {
			permissions.remove(CorePermissions.DELETE_CONTENT_VERSION);
		}
		rolesManager.updateRole(manager.withPermissions(withoutDuplicates(permissions)));
		manager = rolesManager.getRole(zeCollection, RMRoles.MANAGER);

		User sasquatch = users.sasquatchIn(zeCollection);
		sasquatch.setCollectionDeleteAccess(true);
		recordServices.update(sasquatch.setUserRoles(manager.getCode()));


		Transaction transaction = new Transaction();
		transaction.add(document21WithContent2HavingTitle1.setContent(content2_title1).setTitle("21"));
		recordServices.execute(transaction);


		DocumentVO documentVo = new DocumentToVOBuilder(getModelLayerFactory()).build(document21WithContent2HavingTitle1.getWrappedRecord(),
				RecordVO.VIEW_MODE.FORM, FakeSessionContext.sasquatchInCollection(zeCollection));

		DocumentActionsPresenterUtils presenterUtils = spy(new DocumentActionsPresenterUtils(view));
		doReturn(sasquatch).when(presenterUtils).getCurrentUser();
		presenterUtils.setRecordVO(documentVo);
		assertThat(presenterUtils.isDeleteContentVersionPossible()).isFalse();
	}

	@Test
	public void whenDeleteContentVersionImpossibleWithoutNoDeleteAccess() throws RecordServicesException {
		Role manager = rolesManager.getRole(zeCollection, RMRoles.MANAGER);
		List<String> permissions = new ArrayList<>(manager.getOperationPermissions());
		if (!permissions.contains(CorePermissions.DELETE_CONTENT_VERSION)) {
			permissions.add(CorePermissions.DELETE_CONTENT_VERSION);
		}
		rolesManager.updateRole(manager.withPermissions(withoutDuplicates(permissions)));
		manager = rolesManager.getRole(zeCollection, RMRoles.MANAGER);

		User sasquatch = users.sasquatchIn(zeCollection);
		sasquatch.setCollectionDeleteAccess(false);
		recordServices.update(sasquatch.setUserRoles(manager.getCode()));


		Transaction transaction = new Transaction();
		transaction.add(document21WithContent2HavingTitle1.setContent(content2_title1).setTitle("21"));
		recordServices.execute(transaction);


		DocumentVO documentVo = new DocumentToVOBuilder(getModelLayerFactory()).build(document21WithContent2HavingTitle1.getWrappedRecord(),
				RecordVO.VIEW_MODE.FORM, FakeSessionContext.sasquatchInCollection(zeCollection));

		DocumentActionsPresenterUtils presenterUtils = spy(new DocumentActionsPresenterUtils(view));
		doReturn(sasquatch).when(presenterUtils).getCurrentUser();
		presenterUtils.setRecordVO(documentVo);
		assertThat(presenterUtils.isDeleteContentVersionPossible()).isFalse();
	}

	private void initTestData()
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		content1_title1 = createContent(title1, title1);
		content1_title2 = createContent(title1, title2);
		content2_title1 = createContent(title2, title1);
		content2_title2 = createContent(title2, title2);
		File folder = newTempFolder();
		content1File = createFileFromContent(content1_title1, folder.getPath() + "/1");
		content2File = createFileFromContent(content2_title2, folder.getPath() + "/2");

		document11WithContent1HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1)
				.setFolder(records.getFolder_A01().getId());
		transaction.add(document11WithContent1HavingTitle1.setContent(content1_title1).setTitle("11"));

		document12WithContent1HavingTitle2 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document12WithContent1HavingTitle2.setContent(content1_title2).setTitle("12"));

		document21WithContent2HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document21WithContent2HavingTitle1.setContent(content2_title1).setTitle("21"));

		document22WithContent2HavingTitle2 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document22WithContent2HavingTitle2.setContent(content2_title2).setTitle("22"));

		documentWithoutContent = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(documentWithoutContent.setTitle("withoutContent"));

		cart = rm.getOrCreateUserCart(users.aliceIn(zeCollection));
		List<Record> documents = asList(document11WithContent1HavingTitle1.getWrappedRecord(),
				document12WithContent1HavingTitle2.getWrappedRecord(),
				document21WithContent2HavingTitle1.getWrappedRecord(),
				document22WithContent2HavingTitle2.getWrappedRecord(),
				documentWithoutContent.getWrappedRecord());
		addDocumentsToCart(cart, documents);
		transaction.add(cart);
		recordServices.execute(transaction);
		recordServices.execute(new Transaction(documents));
	}

	private void addDocumentsToCart(Cart cart, List<Record> documents) {
		for (Record record : documents) {
			rm.wrapDocument(record).addFavorite(cart.getId());
		}
	}

	private Content createContent(String resource, String title) {
		User user = users.adminIn(zeCollection);
		ContentVersionDataSummary version01 = upload("Minor_" + resource);
		Content content = contentManager.createMinor(user, title, version01);
		ContentVersionDataSummary version10 = upload("Major_" + resource);
		content.updateContent(user, version10, true);
		return content;
	}

	private File createFileFromContent(Content content, String filePath) {
		InputStream inputStream = null;
		try {
			inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), TEST_ID);
			FileUtils.copyInputStreamToFile(inputStream, new File(filePath));
			return new File(filePath);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private ContentVersionDataSummary upload(String resource) {
		InputStream inputStream = DemoTestRecords.class.getResourceAsStream("RMTestRecords_" + resource);
		return contentManager.upload(inputStream);
	}
}
