package com.constellio.app.modules.rm.ui.pages.email;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class AddEmailAttachmentsToFolderPresenter extends SingleSchemaBasePresenter<AddEmailAttachmentsToFolderView> {

	private String userDocumentId;

	private transient RecordServices recordServices;

	private transient ContentManager contentManager;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public AddEmailAttachmentsToFolderPresenter(AddEmailAttachmentsToFolderView view) {
		super(view, Document.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		recordServices = modelLayerFactory.newRecordServices();
		contentManager = modelLayerFactory.getContentManager();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		this.userDocumentId = paramsMap.get("userDocumentId");
	}

	@SuppressWarnings("unchecked")
	void saveButtonClicked() {
		String folderId = view.getFolderId();
		if (folderId == null) {
			view.showErrorMessage($("AddEmailAttachmentsToFolderView.folderRequired"));
		} else {
			boolean noExceptionDisplayed = true;

			Folder folder = rmSchemasRecordsServices.getFolder(folderId);
			Record emailRecord = recordServices.getDocumentById(userDocumentId);
			MetadataSchemaTypes types = types();

			UserDocument userDocument = new UserDocument(emailRecord, types);
			String filename = userDocument.getContent().getCurrentVersion().getFilename();
			ContentVersion emailContentVersion = userDocument.getContent().getCurrentVersion();
			InputStream messageInputStream = contentManager.getContentInputStream(emailContentVersion.getHash(),
					"AddEmailAttachmentsToFolderPresenter.saveButtonClicked");
			try {
				Map<String, Object> parsedEmail = rmSchemasRecordsServices.parseEmail(filename, messageInputStream);
				Map<String, InputStream> emailAttachments = (Map<String, InputStream>) parsedEmail
						.get(RMSchemasRecordsServices.EMAIL_ATTACHMENTS);
				for (Entry<String, InputStream> emailAttachment : emailAttachments.entrySet()) {
					String attachmentFilename = emailAttachment.getKey();
					InputStream attachmentIn = emailAttachment.getValue();
					try {
						Document attachmentDocument = rmSchemasRecordsServices.newDocument();
						attachmentDocument.setTitle(attachmentFilename);
						ContentVersionDataSummary attachmentSummary = contentManager
								.upload(attachmentIn, true, true, attachmentFilename);
						Content attachmentContent = contentManager
								.createMajor(getCurrentUser(), attachmentFilename, attachmentSummary);
						attachmentDocument.setContent(attachmentContent);
						attachmentDocument.setFolder(folder);
						recordServices.add(attachmentDocument.getWrappedRecord());
					} catch (RecordServicesException e) {
						view.showErrorMessage(MessageUtils.toMessage(e));
						noExceptionDisplayed = false;
					} finally {
						IOUtils.closeQuietly(attachmentIn);
					}
				}
			} finally {
				IOUtils.closeQuietly(messageInputStream);
			}

			if (noExceptionDisplayed) {
				delete(userDocument.getWrappedRecord(), true);
				view.navigateTo().displayFolder(folderId);
			}
		}
	}

	public void cancelButtonClicked() {
		view.navigate().to(RMViews.class).listUserDocuments();
	}
}
