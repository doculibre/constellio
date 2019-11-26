package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.SchemasRecordsServices;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class PdfTronPresenter {

	private AppLayerFactory appLayerFactory;
	private ContentManager contentManager;
	private ContentDao contentDao;
	private DocumentVO documentVO;
	private PdfTronViewer pdfTronViewer;
	private SchemasRecordsServices schemasRecordsServices;
	private boolean doesCurrentUserHaveAnnotationLock = false;

	public PdfTronPresenter(PdfTronViewer pdfTronViewer, DocumentVO documentVO) {
		this.appLayerFactory = pdfTronViewer.getAppLayerFactory();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.documentVO = documentVO;
		this.pdfTronViewer = pdfTronViewer;
		this.schemasRecordsServices = new SchemasRecordsServices(pdfTronViewer.getCurrentSessionContext().getCurrentCollection(),
				appLayerFactory.getModelLayerFactory());
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
		contentDao.add(documentVO.getContent().getHash() + ".annotation." + documentVO.getId() + ":" + documentVO.getContent().getVersion(), IOUtils.toInputStream(annotation, (String) null));
	}

	public String getAnnotations() throws IOException {
		String hash = documentVO.getContent().getHash();
		InputStream annotations = contentManager.getContentAnnotationInputStream(hash, documentVO.getId(), documentVO.getContent().getVersion(), getClass().getSimpleName() + hash + ".PdfTronPresenter");

		return IOUtils.toString(annotations, (String) null);
	}

	public String getUserName(String userId) {
		User user = schemasRecordsServices.getUser(userId);

		return user.getFirstName() + " " + user.getLastName();
	}

	public boolean userHasWrtteAccessToDocument() {
		return getCurrentUser().hasWriteAccess().on(documentVO.getRecord());
	}

	public String getCurrentAnnotationLockUser() {
		return contentManager.getUserHavingAnnotationLock(documentVO.getContent().getHash(), documentVO.getId(), documentVO.getContent().getVersion());
	}

	public boolean obtainAnnotationLock() {
		boolean isLockObtained = contentManager.obtainAnnotationLock(documentVO.getContent().getHash(), documentVO.getId(), documentVO.getContent().getVersion(), getUserVO().getId());
		doesCurrentUserHaveAnnotationLock = isLockObtained;
		return isLockObtained;
	}

	public void releaseAnnotationLockIfUserhasIt() {
		if (!doesCurrentUserHaveAnnotationLock) {
			return;
		}

		contentManager.releaseAnnotationLock(documentVO.getContent().getHash(), documentVO.getId(), documentVO.getContent().getVersion());
		doesCurrentUserHaveAnnotationLock = false;
	}
}
