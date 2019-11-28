package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.constellio.model.services.pdftron.PdfTronXMLService;
import com.constellio.model.services.records.SchemasRecordsServices;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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
	private PdfTronXMLService pdfTronParser;
	private String xmlCurrentAnnotations;
	private IOServices ioServices;


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
		this.pdfTronParser = new PdfTronXMLService();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();

		initialize();
	}

	private void initialize() {
		String currentAnnotationLockUser = getCurrentAnnotationLockUser();

		try {
			if (hasContentAnnotation()) {
				xmlCurrentAnnotations = getContentAnnotationFromVault();
			} else {
				xmlCurrentAnnotations = null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		doesCurrentUserHaveAnnotationLock = currentAnnotationLockUser != null && currentAnnotationLockUser
				.equals(pdfTronViewer.getCurrentSessionContext().getCurrentUser().getId());
	}

	private boolean hasContentAnnotation() {
		return this.contentManager.hasContentAnnotation(contentVersion.getHash(), recordId, contentVersion.getVersion());
	}


	public boolean doesCurrentUserHaveAnnotationLock() {
		return doesCurrentUserHaveAnnotationLock;
	}

	public User getCurrentUser() {
		return appLayerFactory.getModelLayerFactory().newUserServices()
				.getUserInCollection(getUserVO().getUsername(),
						pdfTronViewer.getCurrentSessionContext().getCurrentCollection());
	}

	private UserVO getUserVO() {
		return pdfTronViewer.getCurrentSessionContext().getCurrentUser();
	}

	public void saveAnnotation(String annotation) throws PdfTronXMLException_IOExeption {
		try {
			contentDao.add(contentVersion.getHash() + ".annotation."
						   + recordId + "." + contentVersion.getVersion(),
					IOUtils.toInputStream(annotation, (String) null));
		} catch (IOException e) {
			throw new PdfTronXMLException_IOExeption(e);
		}
	}

	public String getUserHavingAnnotationLock() {
		String hash = contentVersion.getHash();

		return contentManager.getUserHavingAnnotationLock(hash, recordId, contentVersion.getVersion());
	}

	public String getUserName(String userId) {
		User user = schemasRecordsServices.getUser(userId);

		return user.getFirstName() + " " + user.getLastName();
	}

	public boolean hasWrtteAccessToDocument() {
		return getCurrentUser().hasWriteAccess().on(record);
	}

	public boolean hasEditAllAnnotation() {
		return getCurrentUser().has(CorePermissions.EDIT_ALL_ANNOTATION).on(record);
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


	public String getContentAnnotationFromVault() throws IOException {
		InputStream contentAnnotationInputStream = null;
		try {
			contentAnnotationInputStream = contentManager.getContentAnnotationInputStream(contentVersion.getHash(),
					recordId, contentVersion.getVersion(), PdfTronPresenter.class.getSimpleName() + "getAnnotationsFromVault");

			return IOUtils.toString(contentAnnotationInputStream, "UTF-8");
		} finally {
			ioServices.closeQuietly(contentAnnotationInputStream);
		}
	}

	public void handleNewXml(String newXml, boolean userHasRightToEditOtherUserAnnotation, String userId)
			throws PdfTronXMLException_CannotEditOtherUsersAnnoations, PdfTronXMLException_XMLParsingException, PdfTronXMLException_IOExeption {
		String currenttAnnotations = xmlCurrentAnnotations;

		// Will throw if something is wrong.
		String xmlToSave = pdfTronParser.processNewXML(currenttAnnotations, newXml, userHasRightToEditOtherUserAnnotation, userId);

		if (xmlToSave != null) {
			saveAnnotation(xmlToSave);
			xmlCurrentAnnotations = xmlToSave;
			System.out.println(xmlToSave);
		}
	}
}
