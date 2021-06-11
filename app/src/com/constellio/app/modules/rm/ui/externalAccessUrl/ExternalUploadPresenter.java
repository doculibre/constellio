package com.constellio.app.modules.rm.ui.externalAccessUrl;

import com.constellio.app.modules.rm.services.EmailParsingServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderPresenter;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.ExternalUploadLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ExternalUploadPresenter extends SingleSchemaBasePresenter<ExternalUploadView> {

	private static Logger LOGGER = LoggerFactory.getLogger(ExternalUploadPresenter.class);

	RMSchemasRecordsServices rm;
	ExternalUploadLink externalUploadLink;
	Map<String, String> params;
	Folder folder;

	public ExternalUploadPresenter(ExternalUploadView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		boolean tokenMatch = externalUploadLink.getToken().equals(this.params.get("token"));
		boolean isExpired = externalUploadLink.getExpirationDate() != null && TimeProvider.getLocalDate().isAfter(externalUploadLink.getExpirationDate());
		return tokenMatch && !isExpired;
	}

	public void forParams(Map<String, String> params) {
		this.params = params;
		String id = params.get("id");
		Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);
		rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
		externalUploadLink = rm.wrapExternalUploadLink(record);
		folder = rm.getFolder(externalUploadLink.getAccessRecord());
	}

	public void uploadWindowClosed() {
	}

	public void contentVersionUploaded(List<ContentVersionVO> uploadedContentVOs) {
		Transaction contentVersionUploadedTransaction = new Transaction();

		try {
			Iterator<ContentVersionVO> iterator = uploadedContentVOs.iterator();

			if (iterator.hasNext()) {
				while (iterator.hasNext()) {
					contentVersionUploaded(iterator.next(), contentVersionUploadedTransaction);
				}

				if (contentVersionUploadedTransaction.getRecordCount() > 0) {
					appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(contentVersionUploadedTransaction);
					view.showMessage($("ExternalUploadViewImpl.fileSuccessfullyUploaded"));
				}
			}
		} catch (RecordServicesException.ValidationException e) {
			List<String> errorMessages = i18n.asListOfMessages(e.getErrors().getValidationErrors());
			for (String msg : errorMessages) {
				view.showErrorMessage(msg);
			}
			view.showErrorMessage("ExternalUploadViewImpl.errorWhileUploading");
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			view.showErrorMessage("ExternalUploadViewImpl.errorWhileUploading");
			LOGGER.error(e.getMessage(), e);
		} finally {
			view.clearUploadField();
		}
	}

	private void contentVersionUploaded(ContentVersionVO uploadedContentVO, Transaction transactionProvided)
			throws RecordServicesException {

		String fileName = uploadedContentVO.getFileName();

		uploadedContentVO.setMajorVersion(true);
		Document document;
		if (rm.isEmail(fileName)) {
			InputStreamProvider inputStreamProvider = uploadedContentVO.getInputStreamProvider();
			InputStream in = inputStreamProvider.getInputStream(DisplayFolderPresenter.class + ".contentVersionUploaded");
			document = new EmailParsingServices(rm).newEmail(fileName, in);
		} else {
			document = rm.newDocument();
		}
		document.setFolder(folder);
		document.setTitle(fileName);
		InputStream inputStream = null;
		ContentVersionDataSummary contentVersionDataSummary;
		try {
			inputStream = uploadedContentVO.getInputStreamProvider().getInputStream("SchemaPresenterUtils-VersionInputStream");
			UploadOptions options = new UploadOptions().setFileName(fileName);
			ContentManager.ContentVersionDataSummaryResponse uploadResponse = uploadContent(inputStream, options);
			contentVersionDataSummary = uploadResponse.getContentVersionDataSummary();
			document.setContent(appLayerFactory.getModelLayerFactory().getContentManager().createMajor(rm.getUser(externalUploadLink.getCreatedBy()), fileName, contentVersionDataSummary));

			if (transactionProvided == null) {
				Transaction transaction = new Transaction();
				transaction.add(document);
				transaction.setUser(getCurrentUser());
				appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(transaction);
			} else {
				transactionProvided.add(document);
				transactionProvided.setUser(getCurrentUser());
			}
		} finally {
			IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
			ioServices.closeQuietly(inputStream);
		}
	}

	public String getTitle() {
		return $("ExternalUploadViewImpl.title",
				SchemaCaptionUtils.getCaptionForRecord(folder.getWrappedRecord(), view.getSessionContext().getCurrentLocale(), true));
	}
}
