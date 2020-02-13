package com.constellio.app.modules.rm.services.background;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AlertDocumentBorrowingPeriodBackgroundActionAcceptanceTest extends ConstellioTest {
	private LocalDateTime now = LocalDateTime.now();
	private RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	private RecordServices recordServices;

	@Mock
	private AlertDocumentBorrowingPeriodBackgroundAction action;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withAllTest(users));

		recordServices = getModelLayerFactory().newRecordServices();
		action = Mockito.spy(new AlertDocumentBorrowingPeriodBackgroundAction(getAppLayerFactory(), zeCollection));
	}

	@Test
	public void givenDocumentWithCheckoutPeriodOverWhenRunAlertDocumentBorrowingPeriodBackgroundActionThenSendEmail()
			throws RecordServicesException {
		Document document = records.getDocumentWithContent_A19().setBorrowed(true);
		Content content = document.getContent();
		content.checkOut(users.adminIn(zeCollection));
		document.setContent(content);

		recordServices.add(document);

		doReturn(now.plusDays(8)).when(action).getCurrentDateTime();
		action.run();

		verify(action, times(1)).sendEmail(any(Document.class));
	}

	@Test
	public void givenDocumentWithCheckoutPeriodNotOverWhenRunAlertDocumentBorrowingPeriodBackgroundActionThenEmailIsNotSent()
			throws RecordServicesException {
		Document document = records.getDocumentWithContent_A19().setBorrowed(true);
		Content content = document.getContent();
		content.checkOut(users.adminIn(zeCollection));
		document.setContent(content);

		recordServices.add(document);

		action.run();

		verify(action, never()).sendEmail(any(Document.class));
	}

	@Test
	public void givenNoCheckedOutDocumentWhenRunAlertDocumentBorrowingPeriodBackgroundActionThenEmailIsNotSent() {
		action.run();

		verify(action, never()).sendEmail(any(Document.class));
	}


}
