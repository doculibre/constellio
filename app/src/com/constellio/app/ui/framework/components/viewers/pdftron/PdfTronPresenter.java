package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.pdftron.AnnotationLockManager;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditAnnotationWithoutLock;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.constellio.model.services.pdftron.PdfTronXMLService;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class PdfTronPresenter implements CopyAnnotationsOfOtherVersionPresenter {

	private AppLayerFactory appLayerFactory;
	private ContentManager contentManager;
	private ContentDao contentDao;
	private String recordId;
	private ContentVersionVO contentVersionVO;
	private PdfTronViewer pdfTronViewer;
	private SchemasRecordsServices schemasRecordsServices;
	private boolean doesCurrentUserHaveAnnotationLock = false;
	private Record record;
	private PdfTronXMLService pdfTronParser;
	private String xmlCurrentAnnotations;
	private MetadataSchemasManager metadataSchemasManager;
	private IOServices ioServices;
	private String metadataCode;
	private String pageRandomId;
	private boolean doesCurrentPageHaveLock;
	private AnnotationLockManager annotationLockManager;
	private User currentUser;

	public PdfTronPresenter(PdfTronViewer pdfTronViewer, String recordId, String metadataCode,
							ContentVersionVO contentVersion) {
		this.appLayerFactory = pdfTronViewer.getAppLayerFactory();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.contentVersionVO = contentVersion;
		this.pdfTronViewer = pdfTronViewer;
		this.recordId = recordId;
		this.schemasRecordsServices = new SchemasRecordsServices(pdfTronViewer.getCurrentSessionContext().getCurrentCollection(),
				appLayerFactory.getModelLayerFactory());
		this.record = this.schemasRecordsServices.get(recordId);
		this.pdfTronParser = new PdfTronXMLService();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.metadataCode = metadataCode;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.pageRandomId = UUID.randomUUID().toString();
		this.annotationLockManager = appLayerFactory.getModelLayerFactory().getAnnotationLockManager();
		this.currentUser = appLayerFactory.getModelLayerFactory().newUserServices()
				.getUserInCollection(getUserVO().getUsername(),
						pdfTronViewer.getCurrentSessionContext().getCurrentCollection());
		initialize();
	}

	private void initialize() {
		this.doesCurrentUserHaveAnnotationLock = doesUserHaveLock();
		this.doesCurrentPageHaveLock = false;

		try {
			if (hasContentAnnotation()) {
				xmlCurrentAnnotations = getContentAnnotationFromVault();
			} else {
				xmlCurrentAnnotations = null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean doesCurrentPageHaveLock() {
		return doesCurrentPageHaveLock;
	}

	private boolean hasContentAnnotation() {
		return this.contentManager.hasContentAnnotation(contentVersionVO.getHash(), recordId, contentVersionVO.getVersion());
	}

	public String getUserIdThatHaveAnnotationLock() {
		return this.annotationLockManager.getUserIdOfLock(this.contentVersionVO.getHash(), recordId, contentVersionVO.getVersion());
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public String getHash() {
		return contentVersionVO.getHash();
	}

	public String getRecordId() {
		return recordId;
	}

	public String getVersion() {
		return contentVersionVO.getVersion();
	}

	private UserVO getUserVO() {
		return pdfTronViewer.getCurrentSessionContext().getCurrentUser();
	}

	public void saveAnnotation(String annotation) throws PdfTronXMLException_IOExeption {
		InputStream inputStreamForVault = null;

		try {
			inputStreamForVault = IOUtils.toInputStream(annotation, (String) null);
			contentDao.add(contentVersionVO.getHash() + ".annotation."
						   + recordId + "." + contentVersionVO.getVersion(),
					inputStreamForVault);
		} catch (IOException e) {
			throw new PdfTronXMLException_IOExeption(e);
		} finally {
			ioServices.closeQuietly(inputStreamForVault);
		}
	}

	public String getUserName(String userId) {
		User user = schemasRecordsServices.getUser(userId);

		return user.getFirstName() + " " + user.getLastName();
	}

	public String getSignatureImageData() {
		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		UserCredential userCredentials = userServices.getUser(currentUser.getUsername());
		return getImageData(userCredentials.getElectronicSignature());
	}

	public String getInitialsImageData() {
		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		UserCredential userCredentials = userServices.getUser(currentUser.getUsername());
		return getImageData(userCredentials.getElectronicInitials());
	}

	private String getImageData(Content content) {
		if (content != null) {
			ContentVersion version = content.getCurrentVersion();
			InputStream inputStream =
					contentManager.getContentInputStream(version.getHash(), "getImageData");
			try {
				byte[] data = new byte[inputStream.available()];
				inputStream.read(data);
				String base64str = DatatypeConverter.printBase64Binary(data);

				StringBuilder sb = new StringBuilder();
				sb.append("data:");
				sb.append(version.getMimetype());
				sb.append(";base64,");
				sb.append(base64str);
				return sb.toString();
			} catch (IOException e) {
				log.warn(MessageUtils.toMessage(e));
			} finally {
				ioServices.closeQuietly(inputStream);
			}
		}
		return "";
	}

	public boolean hasWriteAccessToDocument() {
		return getCurrentUser().hasWriteAccess().on(record);
	}

	public boolean hasEditAllAnnotation() {
		return getCurrentUser().has(CorePermissions.EDIT_ALL_ANNOTATION).on(record);
	}

	public boolean doesUserHaveLock() {
		String userIdOfLock = annotationLockManager.getUserIdOfLock(contentVersionVO.getHash(), recordId, contentVersionVO.getVersion());
		return userIdOfLock != null && userIdOfLock.equals(getUserVO().getId());
	}

	public boolean obtainAnnotationLock() {
		boolean isLockObtained = annotationLockManager.obtainLock(contentVersionVO.getHash(), recordId, contentVersionVO.getVersion(), getUserVO().getId(), pageRandomId);
		this.doesCurrentUserHaveAnnotationLock = isLockObtained;
		this.doesCurrentPageHaveLock = isLockObtained;
		return doesCurrentPageHaveLock;
	}

	public void releaseAnnotationLockIfPagehasIt() {
		if (!doesCurrentPageHaveLock) {
			return;
		}

		annotationLockManager.releaseLock(contentVersionVO.getHash(), recordId, contentVersionVO.getVersion(), getCurrentUser().getId(), this.pageRandomId);
		doesCurrentUserHaveAnnotationLock = false;
		doesCurrentPageHaveLock = false;
	}


	public String getContentAnnotationFromVault() throws IOException {
		InputStream contentAnnotationInputStream = null;
		try {
			contentAnnotationInputStream = contentManager.getContentAnnotationInputStream(contentVersionVO.getHash(),
					recordId, contentVersionVO.getVersion(), PdfTronPresenter.class.getSimpleName() + "getAnnotationsFromVault");

			return IOUtils.toString(contentAnnotationInputStream, "UTF-8");
		} finally {
			ioServices.closeQuietly(contentAnnotationInputStream);
		}
	}

	public void handleNewXml(String newXml, boolean userHasRightToEditOtherUserAnnotation, String userId)
			throws PdfTronXMLException_CannotEditOtherUsersAnnoations, PdfTronXMLException_XMLParsingException, PdfTronXMLException_IOExeption,
				   PdfTronXMLException_CannotEditAnnotationWithoutLock {
		String currenttAnnotations = xmlCurrentAnnotations;

		if (doesCurrentPageHaveLock) {
			String xmlToSave = pdfTronParser.processNewXML(currenttAnnotations, newXml, userHasRightToEditOtherUserAnnotation, userId);

			if (xmlToSave != null) {
				saveAnnotation(xmlToSave);
				xmlCurrentAnnotations = xmlToSave;
			}
		} else {
			throw new PdfTronXMLException_CannotEditAnnotationWithoutLock();
		}
	}

	@Override
	public List<ContentVersionVO> getAvailableVersion() {
		record = schemasRecordsServices.get(recordId);

		MetadataSchema metadataSchema = metadataSchemasManager.getSchemaOf(record);
		Metadata contentMetadata = metadataSchema.getMetadata(metadataCode);

		Object contentValueAsObj = record.get(contentMetadata);

		Content content;
		if (contentValueAsObj instanceof Content) {
			content = (Content) contentValueAsObj;
		} else {
			throw new ImpossibleRuntimeException("Not implemented because no use case for now. (multi val)");
		}

		String currentVersion = contentVersionVO.getVersion();

		List<ContentVersionVO> listContentVersionVO = new ArrayList<>();

		ContentVersionToVOBuilder contentVersionToVOBuilder = new ContentVersionToVOBuilder(appLayerFactory.getModelLayerFactory());

		List<ContentVersion> historyVersionsWithCurrent = new ArrayList<>(content.getHistoryVersions());
		historyVersionsWithCurrent.add(0, content.getCurrentVersion());

		for (ContentVersion contentVersion : historyVersionsWithCurrent) {
			if (!contentVersion.getVersion().equals(currentVersion)
				&& contentManager.hasContentAnnotation(contentVersion.getHash(), recordId, contentVersion.getVersion())) {
				listContentVersionVO.add(contentVersionToVOBuilder.build(content, contentVersion));
			}
		}

		return listContentVersionVO;
	}

	@Override
	public void addAnnotation(ContentVersionVO contentVersionOfAnnotationToCopy)
			throws PdfTronXMLException_IOExeption, PdfTronXMLException_XMLParsingException {
		InputStream annotationInputStream = contentManager.getContentAnnotationInputStream(contentVersionOfAnnotationToCopy.getHash(), recordId, contentVersionOfAnnotationToCopy.getVersion(), PdfTronPresenter.class.getSimpleName() + "addAnnotationToVersion");

		try {
			if (doesCurrentPageHaveLock) {
				if (!contentManager.hasContentAnnotation(contentVersionVO.getHash(), recordId, contentVersionVO.getVersion())) {
					contentDao.add(contentVersionVO.getHash() + ".annotation."
								   + recordId + "." + contentVersionVO.getVersion(),
							annotationInputStream);
					try {
						xmlCurrentAnnotations = getContentAnnotationFromVault();
					} catch (IOException e) {
						throw new PdfTronXMLException_IOExeption(e);
					}
				} else {
					String xmlToSave;
					try {
						xmlToSave = pdfTronParser.mergeTwoAnnotationFile(xmlCurrentAnnotations, IOUtils.toString(annotationInputStream, "UTF-8"));
					} catch (IOException e) {
						throw new PdfTronXMLException_IOExeption(e);
					}

					if (xmlToSave != null) {
						saveAnnotation(xmlToSave);
						xmlCurrentAnnotations = xmlToSave;
					}
				}
			}
		} finally {
			ioServices.closeQuietly(annotationInputStream);
		}
	}

}
