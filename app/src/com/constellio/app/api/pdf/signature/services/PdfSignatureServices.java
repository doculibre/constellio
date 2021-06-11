package com.constellio.app.api.pdf.signature.services;

import static com.constellio.app.ui.i18n.i18n.$;

import java.awt.Rectangle;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.metadata.PDFAIdentificationValidation;
import org.apache.pdfbox.preflight.metadata.XpacketParsingException;
import org.apache.pdfbox.util.Matrix;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.joda.time.LocalDateTime;

import com.constellio.app.api.pdf.signature.config.ESignatureConfigs;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotCreateTempFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadKeystoreFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignatureFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignedFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSourceFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSaveNewVersionException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSignDocumentException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_NothingToSignException;
import com.constellio.app.modules.rm.services.SignatureExternalAccessServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.PropertyFileUtils;
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
import com.constellio.model.services.pdf.PdfAnnotation;
import com.constellio.model.services.pdf.signature.CreateSignatureBase;
import com.constellio.model.services.pdf.signature.CreateVisibleSignature;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;
import org.joda.time.LocalDateTime;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfSignatureServices {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;
	private ConversionManager conversionManager;

	public PdfSignatureServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.contentManager = modelLayerFactory.getContentManager();
		this.conversionManager = modelLayerFactory.getDataLayerFactory().getConversionManager();
	}

	public void signAndCertify(Record record, Metadata metadata, User user, List<PdfAnnotation> annotations,
							   String localeCode)
			throws PdfSignatureException {
		signAndCertify(record, metadata, user, annotations, localeCode, null);
	}

	@SuppressWarnings("rawtypes")
	public void signAndCertify(Record record, Metadata metadata, User user,
							   List<PdfAnnotation> annotations, String localeCode,
							   String base64PdfContent)
			throws PdfSignatureException {
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		ESignatureConfigs eSignatureConfigs = new ESignatureConfigs(modelLayerFactory.getSystemConfigurationsManager());
		SignatureExternalAccessServices signatureServices =
				new SignatureExternalAccessServices(record.getCollection(), appLayerFactory);

		PDDocument doc = null;

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
					if ("pdf".equals(extension) /*&& !eSignatureConfigs.isConvertToPdfAWhenSigning()*/) {
						pdfInputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
						File pdfTempFile;
						try {
							pdfTempFile = createTempFileFromInputStream(tempDocToSignFilename, pdfInputStream);
							tempFiles.add(pdfTempFile);
						} catch (ContentManagerRuntimeException_NoSuchContent e) {
							throw new PdfSignatureException_CannotReadSourceFileException();
						} finally {
							ioServices.closeQuietly(pdfInputStream);
						}
						if (!eSignatureConfigs.isConvertToPdfAWhenSigning()) {
							docToSignFile = pdfTempFile;
						} else {
							try {
								doc = PDDocument.load(pdfTempFile);
								if (isPdfA(doc)) {
									// Already a PDFA
									docToSignFile = pdfTempFile;
								} else {
									if (doc.getSignatureDictionaries().isEmpty()) {
										ioServices.closeQuietly(doc);
										doc = null;
										docToSignFile = null;
										
										int attempts = 3;
										while (attempts > 0) {
											attempts--;
											// No signature in document, we can convert it to PDFA
											InputStream contentInputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
											try {
												File tempFolder = ioServices.newTemporaryFolder(getClass().getSimpleName() + ".signAndCertify.pdfAConversionFolder");
												tempFiles.add(tempFolder);
												docToSignFile = conversionManager.convertToPDF(contentInputStream, contentVersion.getFilename(), tempFolder, true);
												break;
											} catch (RuntimeException e) {
												if (attempts == 0) {
													throw new PdfSignatureException_CannotReadSourceFileException();
												}
											} finally {
												ioServices.closeQuietly(contentInputStream);
											}
										}
									} else {
										// The document already contains a signature, it's too late to convert it to PDFA
										docToSignFile = pdfTempFile;
									}
								}
							} catch (IOException ioe) {
								throw new PdfSignatureException_CannotReadSourceFileException();
							}
						}
					} else if (eSignatureConfigs.isConvertToPdfAWhenSigning()) {
						InputStream contentInputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
						File tempFolder = ioServices.newTemporaryFolder(getClass().getSimpleName() + ".signAndCertify.pdfAConversionFolder");
						tempFiles.add(tempFolder);
						try {
							docToSignFile = conversionManager.convertToPDF(contentInputStream, contentVersion.getFilename(), tempFolder, true);
						} catch (RuntimeException e) {
							throw new PdfSignatureException_CannotReadSourceFileException();
						} finally {
							ioServices.closeQuietly(contentInputStream);
						}
					} else {
						pdfInputStream = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
						try {
							docToSignFile = createTempFileFromInputStream(tempDocToSignFilename, pdfInputStream);
						} catch (ContentManagerRuntimeException_NoSuchContent e) {
							throw new PdfSignatureException_CannotReadSourceFileException();
						} finally {
							ioServices.closeQuietly(pdfInputStream);
						}
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
			StreamFactory keystore = eSignatureConfigs.getKeystore();
			if (keystore != null) {
				keystoreFile = createTempKeystoreFile("keystore");
				keystorePass = eSignatureConfigs.getKeystorePass();
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

			if (annotations.size() < 1) {
				throw new PdfSignatureException_NothingToSignException();
			}
			Collections.sort(annotations);

			List<PdfAnnotation> signedAnnotations = new ArrayList<>();
			List<PdfAnnotation> notSignedAnnotations = new ArrayList<>();

			try {
				if (doc == null) {
					doc = PDDocument.load(docToSignFile);
				}

				int mdpPermission = CreateSignatureBase.getMDPPermission(doc);
				boolean addSignaturePossible = mdpPermission != 1;
				boolean addAnnotationsPossible = mdpPermission == 0 || mdpPermission == 3;
				for (PdfAnnotation annotation : annotations) {
					if (annotation.isSignature()) {
						if (!addSignaturePossible) {
							throw new PdfSignatureException_CannotSignDocumentException("cannotAddSignature", new RuntimeException());
						} else {
							signedAnnotations.add(annotation);
						}
					} else if (addAnnotationsPossible) {
						notSignedAnnotations.add(annotation);
					} else {
						throw new PdfSignatureException_CannotSignDocumentException("cannotAddAnnotation", new RuntimeException());
					}
				}

				if (!notSignedAnnotations.isEmpty()) {
					Set<COSDictionary> objectsToWrite = new HashSet<>();
					for (int i = 0; i < notSignedAnnotations.size(); i++) {
						PdfAnnotation notSignedAnnotation = notSignedAnnotations.get(i);

						int pageNumber = notSignedAnnotation.getPage();
						//Retrieving the page
						PDPage page = doc.getPage(pageNumber);

						File initialsFile = createTempFileFromBase64("not_signed", notSignedAnnotation.getImageData());
						if (initialsFile == null) {
							throw new PdfSignatureException_CannotReadSignatureFileException();
						} else {
							tempFiles.add(initialsFile);
						}

						Rectangle imagePosition = notSignedAnnotation.getPosition();
						float adjustedY = (float) (imagePosition.getY() - imagePosition.getHeight());
						imagePosition.setLocation((int) imagePosition.getX(), (int) adjustedY);
						float x = (float) imagePosition.getX();
						float y = (float) imagePosition.getY();
						float width = (float) imagePosition.getWidth();
						float height = (float) imagePosition.getHeight();

						//Creating PDImageXObject object
						PDImageXObject pdImage = PDImageXObject.createFromFile(initialsFile.getAbsolutePath(), doc);
						//							pdImage.setWidth((int) width);
						//							pdImage.setHeight((int) height);

						PDAppearanceStream appearanceStream = new PDAppearanceStream(doc);
						appearanceStream.setBBox(new PDRectangle(1, 1));
						appearanceStream.setResources(new PDResources());

						//Creating the PDPageContentStream object
						try (PDPageContentStream contents = new PDPageContentStream(doc, appearanceStream)) {
							//Drawing the image in the PDF document
							//								contents.drawImage(pdImage, (float) imagePosition.getX(), (float) imagePosition.getY(), (float) imagePosition.getWidth(), (float) imagePosition.getHeight());
							contents.drawImage(pdImage, new Matrix());
						}

						PDAppearanceDictionary appearance = new PDAppearanceDictionary();
						appearance.setNormalAppearance(appearanceStream);

						PDAnnotationRubberStamp stamp = new PDAnnotationRubberStamp();
						stamp.setLocked(true);
						stamp.setLockedContents(true);
						stamp.setPrinted(true);
						stamp.setReadOnly(true);
						stamp.setAppearance(appearance);
						stamp.setIntent("StampImage");
						stamp.setRectangle(new PDRectangle(x, y, width, height));

						page.getAnnotations().add(stamp);
						if (!objectsToWrite.contains(page.getCOSObject())) {
							objectsToWrite.add(page.getCOSObject());
						}
					}

					File docWithInitialsFile = fileService.newTemporaryFile(docToSignFile.getName() + "_with_images.pdf");
					tempFiles.add(docWithInitialsFile);
					//Saving the document
					try (FileOutputStream fos = new FileOutputStream(docWithInitialsFile)) {
						doc.saveIncremental(fos, objectsToWrite);
					}

					docToSignFile = docWithInitialsFile;
				}
				ioServices.closeQuietly(doc);

				File newDocumentVersionFile;
				if (!signedAnnotations.isEmpty()) {
					newDocumentVersionFile = null;

					String keystorePath = keystoreFile.getAbsolutePath();
					String docToSignFilePath = docToSignFile.getAbsolutePath();
					String location = user.getLastIPAddress();
					boolean externalSignature = user instanceof ExternalAccessUser;

					for (PdfAnnotation signature : signedAnnotations) {
						File signatureFile = createTempFileFromBase64("signature", signature.getImageData());
						if (signatureFile == null) {
							throw new PdfSignatureException_CannotReadSignatureFileException();
						} else {
							tempFiles.add(signatureFile);
						}
						try {
							String signaturePath = signatureFile.getAbsolutePath();
							String username = externalSignature ? signature.getUsername() : formatInternalUsername(signature.getUsername(), user);
							String reason = $("pdf.signatureReason", username, LocalDateTime.now().toString(DateFormatUtils.getDateTimeFormat()), location);
							newDocumentVersionFile = CreateVisibleSignature.signDocument(keystorePath, keystorePass, docToSignFilePath, signaturePath, signature, location, reason, externalSignature);
							tempFiles.add(docToSignFile);
							docToSignFile = newDocumentVersionFile;
						} catch (Exception e) {
							throw new PdfSignatureException_CannotSignDocumentException(e);
						}
					}
				} else {
					newDocumentVersionFile = docToSignFile;
				}

				uploadNewVersion(newDocumentVersionFile, record, metadata, user);
				signatureServices.sendSignatureNotifications(record, user, localeCode);
			} catch (IOException e) {
				throw new PdfSignatureException_CannotSignDocumentException(e);
			}
		} finally {
			ioServices.closeQuietly(doc);
			for (File tempFile : tempFiles) {
				fileService.deleteQuietly(tempFile);
			}
		}
	}

	private String formatInternalUsername(String username, User user) {
		if (StringUtils.isBlank(user.getEmail())) {
			return username;
		}

		String prefix = username.substring(0, username.lastIndexOf(')'));
		return prefix + " - " + user.getEmail() + ")";
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
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadSignedFileException(e);
		}

		content.updateContentWithName(uploadUser, version, true, newFilename);
		record.set(metadata, content);

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
		ESignatureConfigs eSignatureConfigs = new ESignatureConfigs(modelLayerFactory.getSystemConfigurationsManager());

		File tempFile = fileService.newTemporaryFile(filename);

		StreamFactory keystore = eSignatureConfigs.getKeystore();
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

	private static boolean isPdfA(PDDocument doc) throws IOException {
		boolean pdfA;
		try (InputStream is = getXpacket(doc)) {
			DomXmpParser builder = new DomXmpParser();
			XMPMetadata metadata = builder.parse(is);
			PDFAIdentificationValidation validation = new PDFAIdentificationValidation();
			List<ValidationError> result = validation.validatePDFAIdentifer(metadata);
			pdfA = result.isEmpty();
		} catch (XpacketParsingException | XmpParsingException e) {
			pdfA = false;
		}
		return pdfA;
	}

	/**
	 * Return the xpacket from the dictionary's stream
	 */
	private static InputStream getXpacket(PDDocument document)
			throws IOException, XpacketParsingException
	{
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
		if (metadata == null)
		{
			COSBase metaObject = catalog.getCOSObject().getDictionaryObject(COSName.METADATA);
			if (!(metaObject instanceof COSStream))
			{
				// the Metadata object isn't a stream
				ValidationError error = new ValidationError(
						PreflightConstants.ERROR_METADATA_FORMAT, "Metadata is not a stream");
				throw new XpacketParsingException("Failed while retrieving xpacket", error);
			}
			// missing Metadata Key in catalog
			ValidationError error = new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
					"Missing Metadata Key in catalog");
			throw new XpacketParsingException("Failed while retrieving xpacket", error);
		}

		// no filter key
		if (metadata.getFilters() != null)
		{
			// should not be defined
			ValidationError error = new ValidationError(
					PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER,
					"Filter specified in metadata dictionary");
			throw new XpacketParsingException("Failed while retrieving xpacket", error);
		}

		return metadata.exportXMPMetadata();
	}

}
