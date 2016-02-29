package com.constellio.app.ui.framework.components.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;

public class UpdateContentVersionPresenter implements Serializable {

	private static final String STREAM_NAME = "UpdateContentVersionPresenter-InputStream";

	private static Logger LOGGER = LoggerFactory.getLogger(UpdateContentVersionPresenter.class);

	private UpdateContentVersionWindow window;

	private RecordVO recordVO;

	private MetadataVO metadataVO;

	private SchemaPresenterUtils presenterUtils;

	private transient ModelLayerFactory modelLayerFactory;

	private transient ContentManager contentManager;

	public UpdateContentVersionPresenter(UpdateContentVersionWindow window, RecordVO recordVO, MetadataVO metadataVO) {
		this.window = window;
		this.recordVO = recordVO;
		this.metadataVO = metadataVO;

		initTransientObjects();

		String schemaCode = recordVO.getSchema().getCode();
		ConstellioFactories constellioFactories = window.getConstellioFactories();
		SessionContext sessionContext = window.getSessionContext();
		this.presenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);
	}

	public void windowAttached(boolean checkingIn) {
		if (validateSavePossible()) {
			if (!checkingIn && isCurrentUserBorrower()) {
				window.addMajorMinorSameOptions();
			} else {
				window.addMajorMinorOptions();
			}
			boolean uploadFieldVisible = !checkingIn;
			window.setUploadFieldVisible(uploadFieldVisible);
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		modelLayerFactory = window.getConstellioFactories().getModelLayerFactory();
		contentManager = modelLayerFactory.getContentManager();
	}

	private Content getContent() {
		Record record = presenterUtils.getRecord(recordVO.getId());
		Metadata contentMetadata = presenterUtils.getMetadata(metadataVO.getCode());
		return record.get(contentMetadata);
	}

	private boolean isCurrentUserBorrower() {
		User currentUser = presenterUtils.getCurrentUser();
		Content content = getContent();
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	private boolean isContentCheckedOut() {
		Content content = getContent();
		return content != null && content.getCheckoutUserId() != null;
	}

	private boolean validateSavePossible() {
		boolean uploadPossible;
		if (isContentCheckedOut() && !isCurrentUserBorrower()) {
			uploadPossible = false;
			window.setFormVisible(false);

			Record record = presenterUtils.getRecord(recordVO.getId());
			Metadata contentMetadata = presenterUtils.getMetadata(metadataVO.getCode());
			Content content = record.get(contentMetadata);
			String checkoutUserId = content.getCheckoutUserId();
			String userCaption = SchemaCaptionUtils.getCaptionForRecordId(checkoutUserId);
			window.showErrorMessage("UpdateContentVersionWindow.borrowed", userCaption);
		} else {
			uploadPossible = true;
		}
		return uploadPossible;
	}

	public void contentVersionSaved(ContentVersionVO newVersionVO, Boolean majorVersion) {
		if (validateSavePossible()) {
			InputStream inputStream;
			if (newVersionVO != null) {
				inputStream = newVersionVO.getInputStreamProvider().getInputStream(STREAM_NAME);
			} else {
				inputStream = null;
			}
			boolean contentUploaded = newVersionVO != null && inputStream != null;

			Record record = presenterUtils.getRecord(recordVO.getId());
			Metadata contentMetadata = presenterUtils.getMetadata(metadataVO.getCode());
			User currentUser = presenterUtils.getCurrentUser();
			Content content = record.get(contentMetadata);
			InputStreamProvider inputStreamProvider;

			boolean sameVersion = isContentCheckedOut() && majorVersion == null;
			boolean newMajorVersion = Boolean.TRUE.equals(majorVersion);
			boolean newMinorVersion = Boolean.FALSE.equals(majorVersion);
			boolean checkingIn = isCurrentUserBorrower() && newMajorVersion || newMinorVersion;

			if (contentUploaded) {
				String fileName = newVersionVO.getFileName();

				inputStreamProvider = newVersionVO.getInputStreamProvider();

				if (!sameVersion) {
					newVersionVO.setMajorVersion(majorVersion);
				}
				recordVO.set(metadataVO, newVersionVO);

				boolean newContent;
				if (content == null) {
					newContent = true;
					record = presenterUtils.toRecord(recordVO);
					content = record.get(contentMetadata);
				} else {
					newContent = false;
				}
				newVersionVO.setContentId(content.getId());

				ContentVersionDataSummary newVersionDataSummary = contentManager.upload(inputStream, fileName);
				if (newMajorVersion) {
					contentManager.createMajor(currentUser, fileName, newVersionDataSummary);
				} else if (newMinorVersion) {
					contentManager.createMinor(currentUser, fileName, newVersionDataSummary);
				}

				if (isContentCheckedOut()) {
					if (checkingIn) {
						content.checkInWithModificationAndName(newVersionDataSummary, newMajorVersion, fileName);
					} else {
						content.updateCheckedOutContentWithName(newVersionDataSummary, fileName);
					}
					modelLayerFactory.newLoggingServices().returnRecord(record, currentUser);
				} else if (!newContent) {
					content.updateContentWithName(currentUser, newVersionDataSummary, newMajorVersion, fileName);
				}
			} else {
				inputStreamProvider = null;
				if (newMajorVersion) {
					content.finalizeVersion();
				} else if (newMinorVersion) {
					content.checkIn();
					modelLayerFactory.newLoggingServices().returnRecord(record, currentUser);
				} else {
					// TODO Throw appropriate exception
					throw new RuntimeException("A new version must be specified if no new content is uploaded");
				}
			}

			try {
				presenterUtils.addOrUpdate(record);
				if (newVersionVO != null) {
					newVersionVO.setVersion(content.getCurrentVersion().getVersion());
					newVersionVO.setHash(content.getCurrentVersion().getHash());
				}
				if (inputStreamProvider != null) {
					inputStreamProvider.deleteTemp();
				}
				window.close();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				window.showErrorMessage("UpdateContentVersionWindow.errorWhileUploading");
			}
		}
	}

}
