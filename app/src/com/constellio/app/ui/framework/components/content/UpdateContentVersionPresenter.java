package com.constellio.app.ui.framework.components.content;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
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
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class UpdateContentVersionPresenter implements Serializable {

	private static final String STREAM_NAME = "UpdateContentVersionPresenter-InputStream";

	private static Logger LOGGER = LoggerFactory.getLogger(UpdateContentVersionPresenter.class);

	private UpdateContentVersionWindow window;

	private Map<RecordVO, MetadataVO> records;

	private transient ModelLayerFactory modelLayerFactory;

	private transient ContentManager contentManager;

	ConstellioFactories constellioFactories;

	SessionContext sessionContext;

	private int successCount;

	public UpdateContentVersionPresenter(UpdateContentVersionWindow window, Map<RecordVO, MetadataVO> records) {
		this.window = window;
		this.records = records;

		initTransientObjects();

		constellioFactories = window.getConstellioFactories();
		sessionContext = window.getSessionContext();
	}

	public void windowAttached(boolean checkingIn) {
		Iterator<RecordVO> iterator = records.keySet().iterator();
		while (iterator.hasNext()) {
			RecordVO recordVO = iterator.next();
			if (validateSavePossible(recordVO)) {
				if (!checkingIn && isCurrentUserBorrower(recordVO)) {
					window.addMajorMinorSameOptions();
				} else {
					window.addMajorMinorOptions();
				}
				boolean uploadFieldVisible = !checkingIn;
				window.setUploadFieldVisible(uploadFieldVisible);
			}
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

	private Content getContent(RecordVO recordVO) {
		Record record = getPresenterUtils(recordVO).getRecord(recordVO.getId());
		Metadata contentMetadata = getPresenterUtils(recordVO).getMetadata(records.get(recordVO).getCode());
		return record.get(contentMetadata);
	}

	private boolean isCurrentUserBorrower(RecordVO recordVO) {
		User currentUser = getPresenterUtils(recordVO).getCurrentUser();
		Content content = getContent(recordVO);
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	private boolean isContentCheckedOut(RecordVO recordVO) {
		Content content = getContent(recordVO);
		return content != null && content.getCheckoutUserId() != null;
	}

	private boolean canReturnForOther(RecordVO recordVO) {
		User currentUser = getPresenterUtils(recordVO).getCurrentUser();
		return currentUser.has(RMPermissionsTo.RETURN_OTHER_USERS_DOCUMENTS).on(recordVO.getRecord());
	}

	private boolean validateSavePossible(RecordVO recordVO) {
		if (!isContentCheckedOut(recordVO) || isCurrentUserBorrower(recordVO)) {
			return true;
		}

		Record record = getPresenterUtils(recordVO).getRecord(recordVO.getId());
		Metadata contentMetadata = getPresenterUtils(recordVO).getMetadata(records.get(recordVO).getCode());
		Content content = record.get(contentMetadata);
		String checkoutUserId = content.getCheckoutUserId();
		String userCaption = SchemaCaptionUtils.getCaptionForRecordId(checkoutUserId);
		window.showErrorMessage("UpdateContentVersionWindow.borrowed", userCaption);

		if (canReturnForOther(recordVO)) {
			return true;
		}

		window.setFormVisible(false);

		return false;
	}

	public void contentVersionSaved(ContentVersionVO newVersionVO, Boolean majorVersion) {
		successCount = 0;
		Iterator<RecordVO> iterator = records.keySet().iterator();
		RecordUpdateOptions updateOptions = new RecordUpdateOptions();
		while (iterator.hasNext()) {
			RecordVO recordVO = iterator.next();
			if (validateSavePossible(recordVO)) {
				InputStream inputStream;
				if (newVersionVO != null) {
					inputStream = newVersionVO.getInputStreamProvider().getInputStream(STREAM_NAME);
				} else {
					inputStream = null;
				}
				boolean contentUploaded = newVersionVO != null && inputStream != null;

				Record record = getPresenterUtils(recordVO).getRecord(recordVO.getId());
				Metadata contentMetadata = getPresenterUtils(recordVO).getMetadata(records.get(recordVO).getCode());
				User currentUser = getPresenterUtils(recordVO).getCurrentUser();
				Content content = record.get(contentMetadata);
				InputStreamProvider inputStreamProvider;

				boolean sameVersion = isContentCheckedOut(recordVO) && majorVersion == null;
				boolean newMajorVersion = Boolean.TRUE.equals(majorVersion);
				boolean newMinorVersion = Boolean.FALSE.equals(majorVersion);
				boolean checkingIn = isCurrentUserBorrower(recordVO);

				if (contentUploaded) {
					String fileName = newVersionVO.getFileName();

					inputStreamProvider = newVersionVO.getInputStreamProvider();

					if (!sameVersion) {
						newVersionVO.setMajorVersion(majorVersion);
					}
					recordVO.set(records.get(recordVO), newVersionVO);

					try {
						boolean newContent;
						if (content == null) {
							newContent = true;
							record = getPresenterUtils(recordVO).toRecord(recordVO);
							content = record.get(contentMetadata);
						} else {
							newContent = false;
						}
						newVersionVO.setContentId(content.getId());

						UploadOptions options = new UploadOptions().setFileName(fileName);
						ContentManager.ContentVersionDataSummaryResponse uploadResponse = getPresenterUtils(recordVO)
								.uploadContent(inputStream, options);
						ContentVersionDataSummary newVersionDataSummary = uploadResponse.getContentVersionDataSummary();
						if (newMajorVersion) {
							contentManager.createMajor(currentUser, fileName, newVersionDataSummary);
						} else if (newMinorVersion) {
							contentManager.createMinor(currentUser, fileName, newVersionDataSummary);
						}

						if (isContentCheckedOut(recordVO)) {
							if (checkingIn) {
								if (sameVersion) {
									content.checkInWithModificationAndNameInSameVersion(newVersionDataSummary, fileName);
								} else {
									content.checkInWithModificationAndName(newVersionDataSummary, newMajorVersion, fileName);
								}
							} else {
								content.updateCheckedOutContentWithName(newVersionDataSummary, fileName);
							}
							modelLayerFactory.newLoggingServices().returnRecord(record, currentUser);
						} else if (!newContent) {
							content.updateContentWithName(currentUser, newVersionDataSummary, newMajorVersion, fileName);
						}
					} catch (final IcapException e) {
						window.showErrorMessage(e.getMessage());
						return;
					}
				} else {
					inputStreamProvider = null;
					if (newMajorVersion) {
						content.checkIn();
						if (!wasMajorVersion(content)) {
							content.finalizeVersion();
						} else {
							updateOptions.setOverwriteModificationDateAndUser(false);
						}
					} else if (newMinorVersion) {
						content.checkIn();
						if (!wasMinorVersion(content)) {
							content.updateMinorVersion();
						} else {
							updateOptions.setOverwriteModificationDateAndUser(false);
						}
					} else {
						content.checkIn();
					}
					modelLayerFactory.newLoggingServices().returnRecord(record, currentUser);
				}

				try {
					getPresenterUtils(recordVO).addOrUpdate(record, updateOptions);
					modelLayerFactory.newLoggingServices().uploadDocument(record, currentUser);
					if (newVersionVO != null) {
						newVersionVO.setVersion(content.getCurrentVersion().getVersion());
						newVersionVO.setHash(content.getCurrentVersion().getHash());
					}
					if (inputStreamProvider != null) {
						inputStreamProvider.deleteTemp();
					}
					successCount++;
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					window.showErrorMessage("UpdateContentVersionWindow.errorWhileUploading");
				}
			}
		}

		window.showMessage($("DocumentActionsComponent.checkedInDocuments", successCount, records.size()));
	}

	private boolean wasMajorVersion(Content content) {
		return content != null && content.getCurrentVersion().isMajor();
	}

	private boolean wasMinorVersion(Content content) {
		return content != null && !content.getCurrentVersion().isMajor();
	}

	public SchemaPresenterUtils getPresenterUtils(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		return new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);
	}

}
