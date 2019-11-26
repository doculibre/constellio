package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.SchemasRecordsServices;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class PdfTronPresenter {

	private AppLayerFactory appLayerFactory;
	private ContentManager contentManager;
	private ContentDao contentDao;
	private String recordId;
	private ContentVersionVO contentVersion;
	private PdfTronViewer pdfTronViewer;
	private SchemasRecordsServices schemasRecordsServices;
	private boolean doesCurrentUserHaveAnnotationLock = false;
	private Record record;

	public PdfTronPresenter(PdfTronViewer pdfTronViewer, String recordId, ContentVersionVO contentVersion) {
		this.appLayerFactory = pdfTronViewer.getAppLayerFactory();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.contentVersion = contentVersion;
		this.pdfTronViewer = pdfTronViewer;
		this.recordId = recordId;
		this.schemasRecordsServices = new SchemasRecordsServices(pdfTronViewer.getCurrentSessionContext().getCurrentCollection(),
				appLayerFactory.getModelLayerFactory());
		this.record = this.schemasRecordsServices.get(recordId);

		initialize();
	}

	private void initialize() {
		String currentAnnotationLockUser = getCurrentAnnotationLockUser();

		doesCurrentUserHaveAnnotationLock = currentAnnotationLockUser != null && currentAnnotationLockUser
				.equals(pdfTronViewer.getCurrentSessionContext().getCurrentUser().getId());
	}

	public boolean doesCurrentUserHaveAnnotationLock() {
		return doesCurrentUserHaveAnnotationLock;
	}

	public User getCurrentUser() {
		return schemasRecordsServices.getUser(getUserVO().getId());
	}

	private UserVO getUserVO() {
		return pdfTronViewer.getCurrentSessionContext().getCurrentUser();
	}

	public void saveAnnotation(String annotation) throws IOException {
		contentDao.add(contentVersion.getHash() + ".annotation." + recordId + "." + contentVersion.getVersion(), IOUtils.toInputStream(annotation, (String) null));
	}

	public String getAnnotations() throws IOException {
		String hash = contentVersion.getHash();

		return contentManager.getUserHavingAnnotationLock(hash, recordId, contentVersion.getVersion());
	}

	public String getUserName(String userId) {
		User user = schemasRecordsServices.getUser(userId);

		return user.getFirstName() + " " + user.getLastName();
	}

	public boolean userHasWrtteAccessToDocument() {
		return getCurrentUser().hasWriteAccess().on(record);
	}

	public String getCurrentAnnotationLockUser() {
		return contentManager.getUserHavingAnnotationLock(contentVersion.getHash(), recordId, contentVersion.getVersion());
	}

	public boolean obtainAnnotationLock() {
		boolean isLockObtained = contentManager.obtainAnnotationLock(contentVersion.getHash(), recordId, contentVersion.getVersion(), getUserVO().getId());
		doesCurrentUserHaveAnnotationLock = isLockObtained;
		return isLockObtained;
	}

	public void releaseAnnotationLockIfUserhasIt() {
		if (!doesCurrentUserHaveAnnotationLock) {
			return;
		}

		contentManager.releaseAnnotationLock(contentVersion.getHash(), recordId, contentVersion.getVersion());
		doesCurrentUserHaveAnnotationLock = false;
	}
}
