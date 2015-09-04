/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.document;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class DisplayDocumentPresenterAcceptTest extends ConstellioTest {

	@Mock DisplayDocumentView displayDocumentView;
	@Mock ConstellioNavigator navigator;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemasRecordsServices;
	DisplayDocumentPresenter presenter;
	SessionContext sessionContext;
	RecordServices recordServices;
	LocalDateTime now = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime().plusDays(1);

	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(rmRecords)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withEvents()
		);

		schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
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

		presenter = new DisplayDocumentPresenter(displayDocumentView);
	}

	@Test
	public void givenDocumentWithContentWhenCreatePDFAThenOk()
			throws Exception {

		Content initialContent = rmRecords.getDocumentWithContent_A19().getContent();
		String initialHash = initialContent.getCurrentVersion().getHash();
		String initialOlderVersionHash = initialContent.getHistoryVersions().get(0).getHash();
		assertThat(rmRecords.getDocumentWithContent_A19().getContent().getHistoryVersions()).hasSize(1);

		presenter.forParams(rmRecords.document_A19);
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

		presenter.forParams(docId);
		assertThat(presenter.presenterUtils.getCreatePDFAState().isVisible()).isFalse();
	}

	@Test
	public void givenNoCheckoutDocumentThenAlertButtonIsNotVisible()
			throws Exception {

		presenter.forParams(rmRecords.document_A19);
		assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isFalse();
	}

	@Test
	public void givenCheckoutDocumentAndCurrentBorrowerThenAlertButtonIsNotVisible()
			throws Exception {

		presenter.forParams(rmRecords.document_A19);
		presenter.checkOutButtonClicked();
		assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isFalse();
	}

	@Test
	public void givenCheckoutDocumentAndAnotherUserThenAlertButtonIsVisible()
			throws Exception {

		presenter.forParams(rmRecords.document_A19);
		presenter.checkOutButtonClicked();

		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayDocumentPresenter(displayDocumentView);
		presenter.forParams(rmRecords.document_A19);

		assertThat(presenter.presenterUtils.getAlertWhenAvailableButtonState().isVisible()).isTrue();
	}

	@Test
	public void whenAlertWhenAvailableThenOk()
			throws Exception {

		presenter.forParams(rmRecords.document_A19);
		presenter.checkOutButtonClicked();

		presenter.forParams(rmRecords.document_A19);
		presenter.alertWhenAvailableClicked();

		Document document = rmRecords.getDocumentWithContent_A19();
		assertThat(document.getAlertUsersWhenAvailable()).hasSize(1);
		assertThat(document.getAlertUsersWhenAvailable().get(0)).isEqualTo(rmRecords.getChuckNorris().getId());
	}

	@Test
	public void givenSomeUsersToAlertWhenAlertWhenAvailableClickedManyTimeThenAlertOnceToEachUser()
			throws Exception {

		presenter.forParams(rmRecords.document_A19);
		presenter.checkOutButtonClicked();

		presenter.forParams(rmRecords.document_A19);
		presenter.alertWhenAvailableClicked();
		presenter.forParams(rmRecords.document_A19);
		presenter.alertWhenAvailableClicked();

		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(displayDocumentView.getSessionContext()).thenReturn(sessionContext);
		presenter = new DisplayDocumentPresenter(displayDocumentView);
		presenter.forParams(rmRecords.document_A19);

		presenter.alertWhenAvailableClicked();
		presenter.forParams(rmRecords.document_A19);
		presenter.alertWhenAvailableClicked();

		Document document = rmRecords.getDocumentWithContent_A19();
		assertThat(document.getAlertUsersWhenAvailable()).hasSize(2);
		assertThat(document.getAlertUsersWhenAvailable()).containsOnly(rmRecords.getChuckNorris().getId(),
				rmRecords.getBob_userInAC().getId());
	}

	@Test
	public void givenUserToAlertWhenReturnDocumentThenEmailToSendIsCreated()
			throws Exception {

		givenTimeIs(now);
		presenter.forParams(rmRecords.document_A19);
		presenter.checkOutButtonClicked();

		presenter.forParams(rmRecords.document_A19);
		presenter.alertWhenAvailableClicked();

		givenTimeIs(shishOClock);
		presenter.forParams(rmRecords.document_A19);
		Content content = rmRecords.getDocumentWithContent_A19().getContent().checkIn();
		Document document = rmRecords.getDocumentWithContent_A19().setContent(content);
		recordServices.update(document.getWrappedRecord());
		recordServices.flush();

		Metadata subjectMetadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata(EmailToSend.DEFAULT_SCHEMA + "_" + EmailToSend.SUBJECT);
		Metadata sendOnMetadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata(EmailToSend.DEFAULT_SCHEMA + "_" + EmailToSend.SEND_ON);
		User chuck = rmRecords.getChuckNorris();
		Document documentWithContentA19 = rmRecords.getDocumentWithContent_A19();
		LogicalSearchCondition condition = from(getSchemaTypes().getSchemaType(EmailToSend.SCHEMA_TYPE))
				.where(subjectMetadata).isContainingText(documentWithContentA19.getTitle())
				.andWhere(sendOnMetadata).is(shishOClock);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		System.out.println(query.getQuery());
		List<Record> emailToSendRecords = searchServices.search(query);

		assertThat(emailToSendRecords).hasSize(1);
		EmailToSend emailToSend = new EmailToSend(emailToSendRecords.get(0), getSchemaTypes());
		assertThat(emailToSend.getTo()).hasSize(1);
		assertThat(emailToSend.getTo().get(0).getName()).isEqualTo(chuck.getTitle());
		assertThat(emailToSend.getTo().get(0).getEmail()).isEqualTo(chuck.getEmail());
		assertThat(emailToSend.getSubject()).isEqualTo("Alerte lorsque le document est disponible " + documentWithContentA19
				.getTitle());
		assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
		assertThat(emailToSend.getError()).isNull();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0);
		assertThat(emailToSend.getParameters()).hasSize(2);
		assertThat(emailToSend.getParameters().get(0)).isEqualTo("returnDate" + EmailToSend.PARAMETER_SEPARATOR + shishOClock);
		assertThat(emailToSend.getParameters().get(1))
				.isEqualTo("title" + EmailToSend.PARAMETER_SEPARATOR + documentWithContentA19.getTitle());
	}

	//
	private MetadataSchemaTypes getSchemaTypes() {
		return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
	}

}
