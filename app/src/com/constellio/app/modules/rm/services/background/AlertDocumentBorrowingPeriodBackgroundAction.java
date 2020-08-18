package com.constellio.app.modules.rm.services.background;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.contents.ContentFactory.checkedOut;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AlertDocumentBorrowingPeriodBackgroundAction implements Runnable {
	private static final String DOCUMENT_TITLE_PARAMETER = "title";
	private static final String DISPLAY_DOCUMENT_PARAMETER = "displayDocument";

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private String collection;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private RMConfigs rmConfigs;
	private ConstellioEIMConfigs eimConfigs;


	public AlertDocumentBorrowingPeriodBackgroundAction(AppLayerFactory appLayerFactory, String collection) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.rmConfigs = new RMConfigs(appLayerFactory);
		this.eimConfigs = appLayerFactory.getModelLayerFactory().getSystemConfigs();
	}

	@Override
	public void run() {
		int documentBorrowingDurationDays = rmConfigs.getDocumentBorrowingDurationDays();
		if (documentBorrowingDurationDays != -1) {
			LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(rmSchemasRecordsServices.document.schemaType())
					.where(rmSchemasRecordsServices.documentContent()).is(checkedOut()).andWhere(rmSchemasRecordsServices.document.isCheckoutAlertSent()).isFalseOrNull()
					.andWhere(Schemas.COLLECTION).isEqualTo(collection));

			SearchResponseIterator<Record> borrowedDocumentsIterator = searchServices.recordsIterator(query, 1000);
			while (borrowedDocumentsIterator.hasNext()) {
				Document document = rmSchemasRecordsServices.wrapDocument(borrowedDocumentsIterator.next());

				LocalDateTime contentCheckedOutDate = document.getContentCheckedOutDate();
				if (isCheckoutPeriodOver(contentCheckedOutDate, documentBorrowingDurationDays)) {
					sendEmail(document);
					try {
						recordServices.update(document.setCheckoutAlertSent(true));
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	protected void sendEmail(Document document) {
		User userToSendMessageTo = rmSchemasRecordsServices.getUser(document.getContentCheckedOutBy());
		String email = userToSendMessageTo.getEmail();
		if (!StringUtils.isBlank(email)) {
			EmailAddress emailAddress = new EmailAddress(userToSendMessageTo.getTitle(), email);
			EmailToSend emailToSend = prepareEmailToSend(emailAddress, document);
			try {
				recordServices.add(emailToSend);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private EmailToSend prepareEmailToSend(EmailAddress emailAddress, Document document) {
		Language mainLanguage = modelLayerFactory.getCollectionsListManager().getCollectionInfo(document.getCollection()).getMainSystemLanguage();

		EmailToSend emailToSend = rmSchemasRecordsServices.newEmailToSend().setTryingCount(0d)
				.setTo(emailAddress)
				.setSendOn(TimeProvider.getLocalDateTime());
		prepareTaskParameters(emailToSend, document);

		if (mainLanguage == Language.French) {
			emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_BORROWING_PERIOD_ENDED_V2);
		} else {
			emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_BORROWING_PERIOD_ENDED_V2_EN);
		}

		return emailToSend;
	}

	private boolean isCheckoutPeriodOver(LocalDateTime contentCheckedOutDate, int documentBorrowingDurationDays) {
		return contentCheckedOutDate.plusDays(documentBorrowingDurationDays).isBefore(getCurrentDateTime());
	}

	protected LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now();
	}

	private void prepareTaskParameters(EmailToSend emailToSend, Document document) {
		List<String> parameters = new ArrayList<>();
		parameters.add(DOCUMENT_TITLE_PARAMETER + ":" + document.getTitle());
		String constellioURL = eimConfigs.getConstellioUrl();
		parameters.add(DISPLAY_DOCUMENT_PARAMETER + ":" + constellioURL + "#!" + RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + document.getId());
		emailToSend.setParameters(parameters);
	}

}
