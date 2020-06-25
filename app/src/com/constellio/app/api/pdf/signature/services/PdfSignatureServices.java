package com.constellio.app.api.pdf.signature.services;

import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotCreateTempFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadKeystoreFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignatureFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignedFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSourceFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSaveNewVersionException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSignDocumentException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_NothingToSignException;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.ExternalAccessUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.pdf.signature.CreateVisibleSignature;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PdfSignatureServices {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;

	public PdfSignatureServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.contentManager = modelLayerFactory.getContentManager();
	}

	public void signAndCertify(Record record, Metadata metadata, User user, List<PdfSignatureAnnotation> signatures)
			throws PdfSignatureException {
		signAndCertify(record, metadata, user, signatures, null);
	}

	@SuppressWarnings("rawtypes")
	public void signAndCertify(Record record, Metadata metadata, User user,
							   List<PdfSignatureAnnotation> signatureAnnotations,
							   String base64PdfContent)
			throws PdfSignatureException {
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();

		List<File> tempFiles = new ArrayList<>();
		try {
			String tempDocToSignFilename = UUID.randomUUID() + ".pdf";
			File docToSignFile;
			if (base64PdfContent != null) {
				docToSignFile = createTempFileFromBase64(tempDocToSignFilename, base64PdfContent);
			} else {
				Content content = record.get(metadata);
				if (content != null) {
					ContentVersion contentVersion = content.getCurrentVersion();
					String extension = FilenameUtils.getExtension(contentVersion.getFilename()).toLowerCase();
					String hash = contentVersion.getHash();

					InputStream pdfInputStream;
					if ("pdf".equals(extension)) {
						pdfInputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
					} else {
						pdfInputStream = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
					}
					try {
						docToSignFile = createTempFileFromInputStream(tempDocToSignFilename, pdfInputStream);
					} catch (ContentManagerRuntimeException_NoSuchContent e) {
						throw new PdfSignatureException_CannotReadSourceFileException();
					} finally {
						ioServices.closeQuietly(pdfInputStream);
					}
				} else {
					throw new PdfSignatureException_CannotReadSourceFileException();
				}
			}

			if (docToSignFile == null) {
				throw new PdfSignatureException_CannotReadSourceFileException();
			}

			File keystoreFile;
			String keystorePass;
			StreamFactory keystore = appLayerFactory.getModelLayerFactory()
					.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE);
			if (keystore != null) {
				keystoreFile = createTempKeystoreFile("keystore");
				keystorePass = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE_PASSWORD);
				tempFiles.add(keystoreFile);
			} else {
				FoldersLocator foldersLocator = new FoldersLocator();
				keystoreFile = foldersLocator.getKeystoreFile();
				File constellioPropertiesFile = foldersLocator.getConstellioProperties();
				if (constellioPropertiesFile.exists()) {
					keystorePass = PropertyFileUtils.loadKeyValues(constellioPropertiesFile).get("server.keystorePassword");
				} else {
					keystorePass = null;
				}
			}
			if (keystoreFile == null || !keystoreFile.exists() || keystorePass == null) {
				throw new PdfSignatureException_CannotReadKeystoreFileException();
			}

			if (signatureAnnotations.size() < 1) {
				throw new PdfSignatureException_NothingToSignException();
			}
			Collections.sort(signatureAnnotations);

			List<PdfSignatureAnnotation> signedAnnotations = new ArrayList<>();
			List<PdfSignatureAnnotation> notSignedAnnotations = new ArrayList<>();

			for (PdfSignatureAnnotation signature : signatureAnnotations) {
				if (signature.isInitials()) {
					notSignedAnnotations.add(signature);
				} else {
					signedAnnotations.add(signature);
				} 
			}

			if (!notSignedAnnotations.isEmpty()) {
				PDDocument doc = null;
				try {
					doc = PDDocument.load(docToSignFile);

					for (int i = 0; i < notSignedAnnotations.size(); i++) {
						PdfSignatureAnnotation notSignedAnnotation = notSignedAnnotations.get(i);

						int pageNumber = notSignedAnnotation.getPage();
						//Retrieving the page
						PDPage page = doc.getPage(pageNumber);

						File initialsFile = createTempFileFromBase64("not_signed", notSignedAnnotation.getImageData());
						if (initialsFile == null) {
							throw new PdfSignatureException_CannotReadSignatureFileException();
						} else {
							tempFiles.add(initialsFile);
						}
					   
						//Creating PDImageXObject object
						PDImageXObject pdImage = PDImageXObject.createFromFile(initialsFile.getAbsolutePath(), doc);
					   
						//Creating the PDPageContentStream object
						try (PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
							//Drawing the image in the PDF document
							Rectangle imagePosition = notSignedAnnotation.getPosition();
							float adjustedY = (float) (imagePosition.getY() - imagePosition.getHeight());
							imagePosition.setLocation((int) imagePosition.getX(), (int) adjustedY);
							contents.drawImage(pdImage, (float) imagePosition.getX(), (float) imagePosition.getY(), (float) imagePosition.getWidth(), (float) imagePosition.getHeight());
						}
					}

					File docWithInitialsFile = fileService.newTemporaryFile(docToSignFile.getName() + "_with_images.pdf");
					tempFiles.add(docWithInitialsFile);
					//Saving the document
					doc.save(docWithInitialsFile);
					docToSignFile = docWithInitialsFile;
				} catch (IOException e) {
					throw new PdfSignatureException_CannotSignDocumentException(e);
				} finally {
					ioServices.closeQuietly(doc);
				}
			}

			File signedDocument = null;
			for (PdfSignatureAnnotation signature : signedAnnotations) {
				File signatureFile = createTempFileFromBase64("signature", signature.getImageData());
				if (signatureFile == null) {
					throw new PdfSignatureException_CannotReadSignatureFileException();
				} else {
					tempFiles.add(signatureFile);
				}
				try {
					String keystorePath = keystoreFile.getAbsolutePath();
					String docToSignFilePath = docToSignFile.getAbsolutePath();
					String signaturePath = signatureFile.getAbsolutePath();
					signedDocument = CreateVisibleSignature.signDocument(keystorePath, keystorePass, docToSignFilePath, signaturePath, signature);
					tempFiles.add(docToSignFile);
					docToSignFile = signedDocument;
				} catch (Exception e) {
					throw new PdfSignatureException_CannotSignDocumentException(e);
				}
			}

			uploadNewVersion(signedDocument, record, metadata, user);
		} finally {
			for (File tempFile : tempFiles) {
				fileService.deleteQuietly(tempFile);
			}
		}
	}

	private void uploadNewVersion(File signedPdf, Record record, Metadata metadata, User user)
			throws PdfSignatureException {
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		
		Content content = record.get(metadata);
		ContentVersion currentVersion = content.getCurrentVersion();

		User uploadUser;
		if (user instanceof ExternalAccessUser) {
			ExternalAccessUser externalAccessUser = (ExternalAccessUser) user;
			ExternalAccessUrl externalAccessUrl = externalAccessUser.getExternalAccessUrl();

			String collection = externalAccessUrl.getCollection();

			// FIXME Determine how to deal with external access users
			String uploadUserId = externalAccessUrl.getCreatedBy();
			if (uploadUserId == null) {
				uploadUser = userServices.getUserInCollection(User.ADMIN, collection);
			} else {
				Record uploadUserRecord = recordServices.getDocumentById(uploadUserId);
				if (uploadUserRecord != null) {
					MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
					Roles collectionRoles = modelLayerFactory.getRolesManager().getCollectionRoles(collection);
					uploadUser = new User(uploadUserRecord, types, collectionRoles);
				} else {
					uploadUser = null;
				}
			}
		} else {
			uploadUser = user;
		}
		
		String oldFilename = currentVersion.getFilename();
		String substring = oldFilename.substring(0, oldFilename.lastIndexOf('.'));
		String newFilename = substring + ".pdf";

		ContentVersionDataSummary version;
		try {
			InputStream signedStream = new FileInputStream(signedPdf);
			version = contentManager.upload(signedStream, new ContentManager.UploadOptions(newFilename)).getContentVersionDataSummary();
			contentManager.createMajor(uploadUser, newFilename, version);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadSignedFileException(e);
		}

		content.updateContentWithName(uploadUser, version, true, newFilename);

		try {
			recordServices.update(record, uploadUser);
		} catch (RecordServicesException e) {
			throw new PdfSignatureException_CannotSaveNewVersionException(e);
		}

		//		ConstellioUI.getCurrent().updateContent();
	}

	@SuppressWarnings("rawtypes")
	private File createTempKeystoreFile(String filename) throws PdfSignatureException {
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

		return tempFile;
	}

	/**
	 * FIXME PdfTron specific code
	 *
	 * @param filename
	 * @param fileAsBase64Str
	 * @return
	 * @throws PdfSignatureException
	 */
	private File createTempFileFromBase64(String filename, String fileAsBase64Str) throws PdfSignatureException {
		if (StringUtils.isBlank(fileAsBase64Str)) {
			return null;
		}

		String[] parts = fileAsBase64Str.split(",");
		if (parts.length != 2) {
			return null;
		}
		String imageExtension = StringUtils.substringAfter(StringUtils.substringBefore(fileAsBase64Str, ";"), "image/");
		String encodedText;
		try {
			encodedText = new String(parts[1].getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		byte[] data = Base64.getDecoder().decode(encodedText);
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		File tempFile = fileService.newTemporaryFile(filename, "." + imageExtension);

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		try (OutputStream outputStream = ioServices.newFileOutputStream(tempFile, getClass().getSimpleName() + ".createTempFileFromBase64")) {
			outputStream.write(data);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile;
	}

	private File createTempFileFromInputStream(String filename, InputStream inputStream)
			throws PdfSignatureException {
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		File tempFile = fileService.newTemporaryFile(filename);

		try (OutputStream outputStream = ioServices.newFileOutputStream(tempFile, getClass().getSimpleName() + ".createTempFileFromInputStream")) {
			ioServices.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile;
	}

}
