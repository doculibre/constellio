package com.constellio.app.services.signature;

import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotCreateTempFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadKeystoreFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignatureFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignedFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSourceFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSaveNewVersionException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSignDocumentException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_NothingToSignException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.pdf.pdtron.AnnotationLockManager;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_CannotEditAnnotationWithoutLock;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLService;
import com.constellio.model.services.pdf.signature.CreateVisibleSignature;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PdfDocumentCertifyService {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;
	private ContentDao contentDao;
	private String recordId;
	private ContentVersion contentVersion;
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
	private RMSchemasRecordsServices rm;

	public PdfDocumentCertifyService(AppLayerFactory appLayerFactory, String collection, String recordId,
									 String metadataCode,
									 ContentVersion contentVersion, User user) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.collection = collection;
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.contentVersion = contentVersion;
		this.recordId = recordId;
		this.schemasRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		this.record = this.schemasRecordsServices.get(recordId);
		this.pdfTronParser = new PdfTronXMLService();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.metadataCode = metadataCode;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.pageRandomId = UUID.randomUUID().toString();
		this.annotationLockManager = appLayerFactory.getModelLayerFactory().getAnnotationLockManager();
		this.currentUser = appLayerFactory.getModelLayerFactory().newUserServices()
				.getUserInCollection(user.getUsername(), collection);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);

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

	private boolean hasContentAnnotation() {
		return this.contentManager.hasContentAnnotation(contentVersion.getHash(), recordId, contentVersion.getVersion());
	}

	public String getUserIdThatHaveAnnotationLock() {
		return this.annotationLockManager.getUserIdOfLock(this.contentVersion.getHash(), recordId, contentVersion.getVersion());
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public String getHash() {
		return contentVersion.getHash();
	}

	public String getRecordId() {
		return recordId;
	}

	public String getVersion() {
		return contentVersion.getVersion();
	}

	public void saveAnnotation(String annotation) throws PdfTronXMLException_IOExeption {
		InputStream inputStreamForVault = null;

		try {
			inputStreamForVault = IOUtils.toInputStream(annotation, (String) null);
			contentDao.add(contentVersion.getHash() + ".annotation."
						   + recordId + "." + contentVersion.getVersion(),
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

	public boolean hasWriteAccessToDocument() {
		return getCurrentUser().hasWriteAccess().on(record);
	}

	public boolean hasEditAllAnnotation() {
		return getCurrentUser().has(CorePermissions.EDIT_ALL_ANNOTATION).on(record);
	}

	public boolean doesUserHaveLock() {
		String userIdOfLock = annotationLockManager.getUserIdOfLock(contentVersion.getHash(), recordId, contentVersion.getVersion());
		return userIdOfLock != null && userIdOfLock.equals(this.currentUser.getId());
	}

	public boolean obtainAnnotationLock() {
		boolean isLockObtained = annotationLockManager.obtainLock(contentVersion.getHash(), recordId, contentVersion.getVersion(), this.currentUser.getId(), pageRandomId);
		this.doesCurrentUserHaveAnnotationLock = isLockObtained;
		this.doesCurrentPageHaveLock = isLockObtained;
		return doesCurrentPageHaveLock;
	}

	public void releaseAnnotationLockIfPagehasIt() {
		if (!doesCurrentPageHaveLock) {
			return;
		}

		annotationLockManager.releaseLock(contentVersion.getHash(), recordId, contentVersion.getVersion(), getCurrentUser().getId(), this.pageRandomId);
		doesCurrentUserHaveAnnotationLock = false;
		doesCurrentPageHaveLock = false;
	}


	public String getContentAnnotationFromVault() throws IOException {
		InputStream contentAnnotationInputStream = null;
		try {
			contentAnnotationInputStream = contentManager.getContentAnnotationInputStream(contentVersion.getHash(),
					recordId, contentVersion.getVersion(), PdfDocumentCertifyService.class.getSimpleName() + "getAnnotationsFromVault");

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

		String currentVersion = contentVersion.getVersion();

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

	private boolean canEditContent() {
		Document document = rm.getDocument(recordId);
		Content content = document.getContent();
		return content != null &&
			   (content.getCheckoutUserId() == null || content.getCheckoutUserId().equals(getCurrentUser().getId()));
	}

	public void certifyAndSign(String fileAsStr, List<PdfSignatureAnnotation> signatures)
			throws PdfSignatureException {

		String filePath = createTempFileFromBase64("docToSign.pdf", fileAsStr);
		if (StringUtils.isBlank(filePath)) {
			throw new PdfSignatureException_CannotReadSourceFileException();
		}

		String keystorePath = createTempKeystoreFile("keystore");
		String keystorePass = modelLayerFactory
				.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE_PASSWORD);

		if (signatures.size() < 1) {
			throw new PdfSignatureException_NothingToSignException();
		}
		Collections.sort(signatures);

		File signedDocument = null;
		for (PdfSignatureAnnotation signature : signatures) {
			String signaturePath = createTempFileFromBase64("signature", signature.getImageData());
			if (StringUtils.isBlank(signaturePath)) {
				throw new PdfSignatureException_CannotReadSignatureFileException();
			}

			try {
				signedDocument = CreateVisibleSignature.signDocument(keystorePath, keystorePass, filePath, signaturePath, signature);
				filePath = signedDocument.getPath();
			} catch (Exception e) {
				throw new PdfSignatureException_CannotSignDocumentException(e);
			}
		}

		uploadNewVersion(signedDocument);

	}

	private void uploadNewVersion(File signedPdf) throws PdfSignatureException {
		String oldFilename = contentVersion.getFilename();
		String substring = oldFilename.substring(0, oldFilename.lastIndexOf('.'));
		String newFilename = substring + ".pdf";

		ContentVersionDataSummary version;
		try {
			InputStream signedStream = new FileInputStream(signedPdf);
			version = contentManager.upload(signedStream, new ContentManager.UploadOptions(newFilename)).getContentVersionDataSummary();
			contentManager.createMajor(getCurrentUser(), newFilename, version);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadSignedFileException(e);
		}

		Document document = rm.getDocument(recordId);
		document.getContent().updateContentWithName(getCurrentUser(), version, true, newFilename);

		try {
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			recordServices.update(document);
		} catch (RecordServicesException e) {
			throw new PdfSignatureException_CannotSaveNewVersionException(e);
		}
	}

	private String createTempKeystoreFile(String filename) throws PdfSignatureException {
		FileService fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		File tempFile = fileService.newTemporaryFile(filename);

		StreamFactory keystore = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE);
		if (keystore == null) {
			throw new PdfSignatureException_CannotReadKeystoreFileException();
		}

		byte[] data;
		try {
			InputStream inputStream = (InputStream) keystore.create("keystore-stream");
			data = new byte[inputStream.available()];
			inputStream.read(data);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadKeystoreFileException(e);
		}

		try {
			OutputStream outputStream = new FileOutputStream(tempFile);
			outputStream.write(data);
			outputStream.close();
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

	private String createTempFileFromBase64(String filename, String fileAsBase64Str) throws PdfSignatureException {
		if (StringUtils.isBlank(fileAsBase64Str)) {
			return null;
		}

		String[] parts = fileAsBase64Str.split(",");
		if (parts.length != 2) {
			return null;
		}

		byte[] data = Base64.getDecoder().decode(parts[1]);
		FileService fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		File tempFile = fileService.newTemporaryFile(filename);

		try {
			OutputStream outputStream = new FileOutputStream(tempFile);
			outputStream.write(data);
			outputStream.close();
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

}
