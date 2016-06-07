package com.constellio.app.modules.rm.ui.pages.document;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class DisplayDocumentPresenterAcceptTest extends ConstellioTest {

	Users users = new Users();
	@Mock DisplayDocumentView displayDocumentView;
	@Mock CoreViews navigator;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemasRecordsServices;
	DisplayDocumentPresenter presenter;
	SessionContext sessionContext;
	@Mock UIContext uiContext;
	RecordServices recordServices;
	LocalDateTime now = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

		schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		when(displayDocumentView.getCollection()).thenReturn(zeCollection);
		when(displayDocumentView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(displayDocumentView.navigateTo()).thenReturn(navigator);
		when(displayDocumentView.getUIContext()).thenReturn(uiContext);

		presenter = new DisplayDocumentPresenter(displayDocumentView);
	}

	@Test
	public void givenDocumentWithContentWhenCreatePDFAThenOk()
			throws Exception {

		Content initialContent = rmRecords.getDocumentWithContent_A19().getContent();
		String initialHash = initialContent.getCurrentVersion().getHash();
		String initialOlderVersionHash = initialContent.getHistoryVersions().get(0).getHash();
		assertThat(rmRecords.getDocumentWithContent_A19().getContent().getHistoryVersions()).hasSize(1);

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		assertThat(presenter.presenterUtils.getCreatePDFAState().isVisible()).isTrue();

		presenter.createPDFAButtonClicked();

		Content modifiedContent = rmRecords.getDocumentWithContent_A19().getContent();

		assertThat(modifiedContent.getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
		assertThat(modifiedContent.getCurrentVersion().getHash())
				.isNotEqualTo(initialHash)
				.isNotEqualTo(initialOlderVersionHash);
		assertThat(modifiedContent.getCurrentVersion().getFilename())
				.isEqualTo("Chevreuil.pdf");

		assertThat(modifiedContent.getHistoryVersions()).hasSize(2);
		assertThat(modifiedContent.getHistoryVersions().get(0).getMimetype())
				.isEqualTo("application/vnd.oasis.opendocument.text");
		assertThat(modifiedContent.getHistoryVersions().get(0).getHash())
				.isEqualTo(initialOlderVersionHash);
		assertThat(modifiedContent.getHistoryVersions().get(0).getFilename())
				.isEqualTo("Chevreuil.odt");
		assertThat(modifiedContent.getHistoryVersions().get(1).getMimetype())
				.isEqualTo("application/vnd.oasis.opendocument.text");
		assertThat(modifiedContent.getHistoryVersions().get(1).getHash())
				.isEqualTo(initialHash);
		assertThat(modifiedContent.getHistoryVersions().get(1).getFilename())
				.isEqualTo("Chevreuil.odt");
	}

	@Test
	public void givenDocumentWithoutContentWhenCreatePDFAThenItIsNotVisible()
			throws Exception {

		String docId = "docNoContent";
		String docTitle = "Document Without Content";
		Document document = schemasRecordsServices.newDocumentWithId(docId);
		document.setFolder(rmRecords.folder_C30);
		document.setTitle(docTitle);
		recordServices.add(document);

		Map<String, String> params = new HashMap<>();
		params.put("id", docId);
		presenter.forParams(ParamUtils.addParams("", params));
		assertThat(presenter.presenterUtils.getCreatePDFAState().isVisible()).isFalse();
	}

	@Test
	public void givenCheckedOutDocumentWhenCreatePDFAThenItIsNotVisible()
			throws Exception {

		assertThat(rmRecords.getDocumentWithContent_A19().getContent().getHistoryVersions()).hasSize(1);

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.checkOutButtonClicked();
		assertThat(presenter.presenterUtils.getCreatePDFAState().isVisible()).isFalse();
	}

	@Test
	public void givenNoCheckoutDocumentThenAlertButtonIsNotVisible()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isFalse();
	}

	@Test
	public void givenCheckoutDocumentAndCurrentBorrowerThenAlertButtonIsNotVisible()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.checkOutButtonClicked();
		assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isFalse();
	}

	@Test
	public void givenCheckoutDocumentAndAnotherUserThenAlertButtonIsVisible()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.checkOutButtonClicked();

		connectAsBob();
		presenter.forParams(ParamUtils.addParams("", params));

		assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isTrue();
	}

	@Test
	public void whenAlertWhenAvailableThenOk()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.checkOutButtonClicked();
		presenter.alertWhenAvailableClicked();

		connectAsBob();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailableClicked();

		connectAsAlice();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailableClicked();

		Document document = rmRecords.getDocumentWithContent_A19();
		assertThat(document.getAlertUsersWhenAvailable()).containsOnly(
				rmRecords.getBob_userInAC().getId(),
				rmRecords.getAlice().getId());
	}

	@Test
	public void givenSomeUsersToAlertWhenAlertWhenAvailableClickedManyTimeThenAlertOnceToEachUser()
			throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.checkOutButtonClicked();

		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailableClicked();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailableClicked();

		connectAsBob();
		presenter.forParams(ParamUtils.addParams("", params));

		presenter.alertWhenAvailableClicked();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailableClicked();

		Document document = rmRecords.getDocumentWithContent_A19();
		assertThat(document.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getBob_userInAC().getId());
	}

	private void connectAsBob() {
		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayDocumentPresenter(displayDocumentView);
	}

	private void connectAsAlice() {
		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayDocumentPresenter(displayDocumentView);
	}

	@Test
	public void givenUserToAlertWhenReturnDocumentThenEmailToSendIsCreated()
			throws Exception {

		givenTimeIs(now);
		
		Map<String, String> params = new HashMap<>();
		params.put("id", rmRecords.document_A19);
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.checkOutButtonClicked();

		givenTimeIs(shishOClock);
		connectAsBob();
		presenter.forParams(ParamUtils.addParams("", params));
		presenter.alertWhenAvailableClicked();

		presenter.forParams(ParamUtils.addParams("", params));
		Content content = rmRecords.getDocumentWithContent_A19().getContent().checkIn();
		Document document = rmRecords.getDocumentWithContent_A19().setContent(content);
		recordServices.update(document.getWrappedRecord());
		recordServices.flush();

		Document documentWithContentA19 = rmRecords.getDocumentWithContent_A19();
		LogicalSearchCondition condition = from(getSchemaTypes().getSchemaType(EmailToSend.SCHEMA_TYPE)).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> emailToSendRecords = searchServices.search(query);

		assertThat(emailToSendRecords).hasSize(1);
		EmailToSend emailToSend = new EmailToSend(emailToSendRecords.get(0), getSchemaTypes());
		assertThat(emailToSend.getTo()).hasSize(1);
		assertThat(emailToSend.getTo().get(0).getName()).isEqualTo(users.bobIn(zeCollection).getTitle());
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(users.bobIn(zeCollection).getEmail());
		assertThat(emailToSend.getSubject()).isEqualTo("Alerte lorsque le document est disponible " + documentWithContentA19
				.getTitle());
		assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		assertThat(emailToSend.getError()).isNull();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0);
		assertThat(emailToSend.getParameters()).hasSize(2);
		assertThat(emailToSend.getParameters().get(0)).isEqualTo("returnDate" + EmailToSend.PARAMETER_SEPARATOR + shishOClock);
		assertThat(emailToSend.getParameters().get(1))
				.isEqualTo("title" + EmailToSend.PARAMETER_SEPARATOR + documentWithContentA19.getTitle());

		assertThat(rmRecords.getDocumentWithContent_A19().getAlertUsersWhenAvailable()).isEmpty();
	}

	//
	private MetadataSchemaTypes getSchemaTypes() {
		return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
	}

}
