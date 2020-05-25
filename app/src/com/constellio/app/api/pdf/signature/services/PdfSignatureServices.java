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
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.pdf.signature.CreateVisibleSignature;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

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

	public void signAndCertify(Record record, Metadata metadata, User user, List<PdfSignatureAnnotation> signatures,
							   String base64PdfContent)
			throws PdfSignatureException {

		String tempFilename = "docToSign.pdf";
		String docToSignFilePath;
		if (base64PdfContent != null) {
			docToSignFilePath = createTempFileFromBase64(tempFilename, base64PdfContent);
		} else {
			Content content = record.get(metadata);
			if (content != null) {
				String hash = content.getCurrentVersion().getHash();
				try (InputStream inputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertify")) {
					docToSignFilePath = createTempFileFromInputStream(tempFilename, inputStream);
				} catch (ContentManagerRuntimeException_NoSuchContent e) {
					throw new PdfSignatureException_CannotReadSourceFileException();
				} catch (IOException e) {
					throw new PdfSignatureException_CannotReadSourceFileException();
				}
			} else {
				throw new PdfSignatureException_CannotReadSourceFileException();
			}
		}

		if (StringUtils.isBlank(docToSignFilePath)) {
			throw new PdfSignatureException_CannotReadSourceFileException();
		}

		String keystorePath = createTempKeystoreFile("keystore");
		String keystorePass = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE_PASSWORD);

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
				signedDocument = CreateVisibleSignature.signDocument(keystorePath, keystorePass, docToSignFilePath, signaturePath, signature);
				docToSignFilePath = signedDocument.getPath();
			} catch (Exception e) {
				throw new PdfSignatureException_CannotSignDocumentException(e);
			}
		}

		uploadNewVersion(signedDocument, record, metadata, user);
	}

	private void uploadNewVersion(File signedPdf, Record record, Metadata metadata, User user)
			throws PdfSignatureException {
		Content content = record.get(metadata);
		ContentVersion currentVersion = content.getCurrentVersion();

		String oldFilename = currentVersion.getFilename();
		String substring = oldFilename.substring(0, oldFilename.lastIndexOf('.'));
		String newFilename = substring + ".pdf";

		ContentVersionDataSummary version;
		try {
			InputStream signedStream = new FileInputStream(signedPdf);
			version = contentManager.upload(signedStream, new ContentManager.UploadOptions(newFilename)).getContentVersionDataSummary();
			contentManager.createMajor(user, newFilename, version);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadSignedFileException(e);
		}

		content.updateContentWithName(user, version, true, newFilename);

		try {
			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			recordServices.update(record, user);
		} catch (RecordServicesException e) {
			throw new PdfSignatureException_CannotSaveNewVersionException(e);
		}

		//		ConstellioUI.getCurrent().updateContent();
	}

	@SuppressWarnings("rawtypes")
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

	/**
	 * FIXME PdfTron specific code
	 *
	 * @param filename
	 * @param fileAsBase64Str
	 * @return
	 * @throws PdfSignatureException
	 */
	private String createTempFileFromBase64(String filename, String fileAsBase64Str) throws PdfSignatureException {
		if (StringUtils.isBlank(fileAsBase64Str)) {
			return null;
		}

		String[] parts = fileAsBase64Str.split(",");
		if (parts.length != 2) {
			return null;
		}

		byte[] data = Base64.getDecoder().decode(parts[1]);
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		File tempFile = fileService.newTemporaryFile(filename);

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		try (OutputStream outputStream = ioServices.newFileOutputStream(tempFile, getClass().getSimpleName() + ".createTempFileFromBase64")) {
			outputStream.write(data);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

	private String createTempFileFromInputStream(String filename, InputStream inputStream)
			throws PdfSignatureException {
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		File tempFile = fileService.newTemporaryFile(filename);

		try (OutputStream outputStream = ioServices.newFileOutputStream(tempFile, getClass().getSimpleName() + ".createTempFileFromInputStream")) {
			ioServices.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

}
