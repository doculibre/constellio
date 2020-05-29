package com.constellio.app.api.pdf.pdfjs.services;

import com.constellio.app.api.pdf.pdfjs.servlets.CertifyPdfJSSignaturesServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.GetPdfJSAnnotationsConfigServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.GetPdfJSAnnotationsServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.GetPdfJSSignatureServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.SavePdfJSAnnotationsServlet;
import com.constellio.app.api.pdf.pdfjs.servlets.SavePdfJSSignatureServlet;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException;
import com.constellio.app.api.pdf.signature.services.PdfSignatureServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.ExternalAccessUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.pdf.pdfjs.signature.PdfJSAnnotations;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Slf4j
public class PdfJSServices {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;
	private ContentDao contentDao;
	private PdfSignatureServices pdfSignatureServices;

	public PdfJSServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.contentManager = modelLayerFactory.getContentManager();
		this.contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();
		this.pdfSignatureServices = new PdfSignatureServices(this.appLayerFactory);
	}

	public String getExternalViewerUrl(Record record, Metadata metadata, User user, Locale locale, String serviceKey,
							   String token, boolean includeConstellioUrl) {
		return getViewerUrl(record, metadata, user, locale, null, null, serviceKey, token, includeConstellioUrl);
	}

	public String getInternalViewerUrl(Record record, Metadata metadata, User user, Locale locale, String contentPathPrefix,
							   String contentPreviewPath, String serviceKey, String token) {
		return getViewerUrl(record, metadata, user, locale, contentPathPrefix, contentPreviewPath, serviceKey, token, false);
	}

	private String getViewerUrl(Record record, Metadata metadata, User user, Locale locale, String contentPathPrefix,
							   String contentPreviewPath, String serviceKey, String token, boolean includeConstellioUrl) {
		UserServices userServices = modelLayerFactory.newUserServices();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		String username = user.getUsername();
		if (serviceKey == null && !(user instanceof ExternalAccessUser)) {
			UserCredential userCredentials = userServices.getUserCredential(username);
			serviceKey = userServices.giveNewServiceToken(userCredentials);
		}
		if (token == null && !(user instanceof ExternalAccessUser)) {
			token = userServices.generateToken(username);
		}

		String constellioUrl = systemConfigurationsManager.getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
		if (StringUtils.endsWith(constellioUrl, "/")) {
			constellioUrl = StringUtils.substringBeforeLast(constellioUrl, "/");
		}
		boolean disableSignature = !isSignaturePossible(record, metadata, user);

		String metadataCode = metadata.getCode();
		if (contentPathPrefix == null) {
			contentPathPrefix = "../../../../../";
		}
		if (StringUtils.endsWith(contentPathPrefix, "/")) {
			contentPathPrefix = StringUtils.substringBeforeLast(contentPathPrefix, "/");
		}
		if (contentPreviewPath == null && record != null) {
			StringBuilder contentPreviewParams = new StringBuilder();
			contentPreviewParams.append("recordId=" + record.getId());
			contentPreviewParams.append("&metadataCode=" + metadataCode);
			contentPreviewParams.append("&preview=true");
			contentPreviewParams.append("&serviceKey=" + serviceKey);
			contentPreviewParams.append("&token=" + token);
			if (user instanceof ExternalAccessUser) {
				ExternalAccessUser externalAccessUser = (ExternalAccessUser) user;
				contentPreviewParams.append("&accessId=" + externalAccessUser.getExternalAccessUrl().getId());
			}
			
			Content content = record.get(metadata);
			String filename = content.getCurrentVersion().getFilename();
			if (!StringUtils.endsWith(filename, ".pdf")) {
				filename += ".pdf";
			}
			contentPreviewParams.append("&z-filename=" + filename);
			contentPreviewPath = contentPathPrefix + "/getRecordContent?" + contentPreviewParams;
		}

		StringBuilder viewerParams = new StringBuilder();
		viewerParams.append("locale=" + getPdfJSLocaleCode(locale.getLanguage()));
		viewerParams.append("&disableSignature=" + disableSignature);
		if (!disableSignature) {
			String configParams = getCallbackParams(record, metadata, user, locale.getLanguage(), serviceKey, token);
			String configPath;
			if (includeConstellioUrl) {
				configPath = constellioUrl;
			} else {
				configPath = contentPathPrefix;
			}
			configPath += GetPdfJSAnnotationsConfigServlet.PATH + "?" + configParams;
			try {
				configPath = URLEncoder.encode(configPath, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			viewerParams.append("&annotationsConfig=" + configPath);
		}
		//		viewerParams.append("&serviceKey=" + serviceKey);
		//		viewerParams.append("&token=" + token);
		try {
			viewerParams.append("&file=" + URLEncoder.encode(contentPreviewPath, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		StringBuilder pdfJSViewerUrl = new StringBuilder();
		if (includeConstellioUrl) {
			pdfJSViewerUrl.append(constellioUrl + "/");
		}
		pdfJSViewerUrl.append("VAADIN/themes/constellio/pdfjs/web/viewer.html?");
		pdfJSViewerUrl.append(viewerParams);
		return pdfJSViewerUrl.toString();
	}

	private String getCallbackParams(Record record, Metadata metadata, User user, String localeCode, String serviceKey,
									 String token) {
		StringBuilder params = new StringBuilder();
		params.append("locale=" + getPdfJSLocaleCode(localeCode));
		if (serviceKey != null) {
			params.append("&serviceKey=" + serviceKey);
		}
		if (token != null) {
			params.append("&token=" + token);
		}
		if (record != null) {
			params.append("&recordId=" + record.getId());
			params.append("&metadataCode=" + metadata.getCode());
		}
		if (user instanceof ExternalAccessUser) {
			ExternalAccessUser externalAccessUser = (ExternalAccessUser) user;
			params.append("&accessId=" + externalAccessUser.getExternalAccessUrl().getId());
		}
		return params.toString();
	}

	private static String getPdfJSLocaleCode(String language) {
		String pdfJSLocaleCode;
		if ("en".equalsIgnoreCase(language)) {
			pdfJSLocaleCode = "en-US";
		} else {
			pdfJSLocaleCode = language;
		}
		return pdfJSLocaleCode;
	}

	@SuppressWarnings("rawtypes")
	public boolean isSignaturePossible(Record record, Metadata metadata, User user) {
		boolean signaturePossible;
		if (record != null && user.hasWriteAccess().on(record)) {
			StreamFactory keystore = appLayerFactory.getModelLayerFactory()
					.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE);
			signaturePossible = keystore != null;
		} else {
			signaturePossible = false;
		}
		return signaturePossible;
	}

	public String getSignatureBase64Url(User user, boolean initials) {
		String signatureBase64Url;

		UserServices userServices = modelLayerFactory.newUserServices();
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();

		UserCredential userCredential = userServices.getUserCredential(user.getUsername());
		Content signatureContent = initials ? userCredential.getElectronicInitials() : userCredential.getElectronicSignature();
		if (signatureContent != null) {
			ContentVersion signatureContentVersion = signatureContent.getCurrentVersion();
			String hash = signatureContentVersion.getHash();
			String signatureExtension = FilenameUtils.getExtension(signatureContentVersion.getFilename()).toLowerCase();
			try (InputStream in = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".getSignatureBase64")) {
				byte[] imageBytes = ioServices.readBytes(in);
				String urlFirstPart = "data:image/" + signatureExtension +";base64,";
				signatureBase64Url = urlFirstPart + new String(Base64.getEncoder().encodeToString(imageBytes));
			} catch (ContentManagerRuntimeException_NoSuchContent e) {
				log.warn("No signature for user " + user.getUsername(), e);
				signatureBase64Url = null;
			} catch (IOException e) {
				log.warn("Problem getting signature for user " + user.getUsername(), e);
				signatureBase64Url = null;
			}
		} else {
			signatureBase64Url = null;
		}
		return signatureBase64Url;
	}

	public void saveSignatureBase64Url(User user, String signatureBase64Url, boolean initials)
			throws IOException, RecordServicesException {
		if (StringUtils.isNotBlank(signatureBase64Url)) {
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			UserServices userServices = modelLayerFactory.newUserServices();
			IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();

			String filename = initials ? "initials.png" : "signature.png";

			String[] parts = signatureBase64Url.split(",");
			if (parts.length != 2) {
				throw new RuntimeException("Invalid Base64: " + signatureBase64Url);
			}

			String encodedText;
			try {
				encodedText = new String(parts[1].getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			byte[] data = Base64.getDecoder().decode(encodedText);
			try (InputStream in = ioServices.newBufferedByteArrayInputStream(data, getClass().getSimpleName() + ".saveSignatureBase64")) {
				ContentVersionDataSummaryResponse uploadResponse = contentManager.upload(in, filename);

				UserCredential userCredential = userServices.getUserCredential(user.getUsername());
				Content signatureContent = initials ? userCredential.getElectronicInitials() : userCredential.getElectronicSignature();
				if (signatureContent == null) {
					signatureContent = contentManager.createMajor(user, filename, uploadResponse.getContentVersionDataSummary());
					if (initials) {
						userCredential.setElectronicInitials(signatureContent);
					} else {
						userCredential.setElectronicSignature(signatureContent);
					}
				}
				recordServices.update(userCredential, user);
			}
		} else {
			removeSignature(user, initials);
		}
	}

	public void removeSignature(User user, boolean initials)
			throws IOException, RecordServicesException {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential userCredential = userServices.getUserCredential(user.getUsername());
		if (initials) {
			userCredential.setElectronicInitials(null);
		} else {
			userCredential.setElectronicSignature(null);
		}
		recordServices.update(userCredential, user);
	}

	public PdfJSAnnotations getAnnotations(Record record, Metadata metadata, User user) throws IOException {
		PdfJSAnnotations result;

		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();

		Content content = record.get(metadata);
		ContentVersion contentVersion = content.getCurrentVersion();
		String hash = contentVersion.getHash();
		String id = record.getId();
		String version = contentVersion.getVersion();
		if (contentManager.hasContentPDFJSAnnotation(hash, id, version)) {
			try (InputStream in = contentManager.getContentPDFJSAnnotationInputStream(hash, id, version, getClass().getSimpleName() + ".getAnnotations")) {
				String annotationsJson = ioServices.readStreamToString(in);
				JSONObject annotationsJsonObject = new JSONObject(annotationsJson);
				result = new PdfJSAnnotations(annotationsJsonObject);
			}
		} else {
			result = null;
		}
		return result;
	}

	public void saveAnnotations(Record record, Metadata metadata, User user, PdfJSAnnotations annotations)
			throws IOException {
		Content content = record.get(metadata);
		ContentVersion contentVersion = content.getCurrentVersion();
		String hash = contentVersion.getHash();
		String id = record.getId();
		String version = contentVersion.getVersion();

		String newAnnotationsVersion;
		PdfJSAnnotations existingAnnotations = getAnnotations(record, metadata, user);
		if (existingAnnotations != null) {
			String existingVersion = existingAnnotations.getVersion();
			//			if (!existingVersion.equals(annotations.getVersion())) {
			//				// TODO Handle better
			//				throw new RuntimeException("Optimistic locking exception!");
			//			} else {
			//				newAnnotationsVersion = getNextVersionNumber(existingVersion, false);
			//			}
			newAnnotationsVersion = getNextVersionNumber(existingVersion, false);
		} else {
			newAnnotationsVersion = "1.0";
		}
		annotations.setVersion(newAnnotationsVersion);

		String jsonString = annotations.getJSONObject().toString(4);
		try (InputStream jsonInputStream = IOUtils.toInputStream(jsonString, "UTF-8")) {
			String filename = hash + ".annotation.pdfjs." + id + "." + version;
			contentDao.add(filename, jsonInputStream);
		}
	}

	public void signAndCertifyPdf(Record record, Metadata metadata, User user, PdfJSAnnotations annotations)
			throws PdfSignatureException, InvalidPasswordException, IOException {
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();

		Content content = record.get(metadata);
		ContentVersion contentVersion = content.getCurrentVersion();
		String extension = FilenameUtils.getExtension(contentVersion.getFilename()).toLowerCase();
		String hash = contentVersion.getHash();

		InputStream pdfInputStream;
		if ("pdf".equals(extension)) {
			pdfInputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertifyPdf");
		} else {
			pdfInputStream = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + ".signAndCertifyPdf");
		}
		PDDocument pdDocument = null;
		try {
			pdDocument = PDDocument.load(pdfInputStream);
			List<PdfSignatureAnnotation> signatureAnnotations = annotations.getSignatureAnnotations(pdDocument);
			pdfSignatureServices.signAndCertify(record, metadata, user, signatureAnnotations);

			String annotationsVersion = annotations.getVersion();
			String newAnnotationsVersion = getNextVersionNumber(annotationsVersion, true);
			annotations.setVersion(newAnnotationsVersion);
			saveAnnotations(record, metadata, user, annotations);

			if (user instanceof ExternalAccessUser) {
				ExternalAccessUser externalUser = (ExternalAccessUser) user;
				ExternalAccessUrl externalAccess = externalUser.getExternalAccessUrl();
				externalAccess.setStatus(ExternalAccessUrlStatus.TO_CLOSE);

				RecordServices recordServices = modelLayerFactory.newRecordServices();
				recordServices.update(externalAccess);
			}
		} catch (RecordServicesException e) {
			log.error("Unable to close external access for " + user.getUsername(), e);
		} finally {
			ioServices.closeQuietly(pdfInputStream);
			ioServices.closeQuietly(pdDocument);
		}
	}

	public String getAnnotationsConfig(Record record, Metadata metadata, User user, String localeCode,
									   String serviceKey, String token) {
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		String constellioUrl = systemConfigurationsManager.getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
		String prefix = constellioUrl;
		if (StringUtils.endsWith(prefix, "/")) {
			prefix = StringUtils.substringBeforeLast(prefix, "/");
		}

		String params = getCallbackParams(record, metadata, user, localeCode, serviceKey, token);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("certifyServiceUrl", prefix + CertifyPdfJSSignaturesServlet.PATH + "?" + params);
		jsonObject.put("getAnnotationsServiceUrl", prefix + GetPdfJSAnnotationsServlet.PATH + "?" + params);
		jsonObject.put("saveAnnotationsServiceUrl", prefix + SavePdfJSAnnotationsServlet.PATH + "?" + params);
		if (!(user instanceof ExternalAccessUser)) {
			jsonObject.put("getSignatureServiceUrl", prefix + GetPdfJSSignatureServlet.PATH + "?" + params);
			jsonObject.put("saveSignatureServiceUrl", prefix + SavePdfJSSignatureServlet.PATH + "?" + params);
		}
		return jsonObject.toString(4);
	}

	private static String getNextVersionNumber(String version, boolean major) {
		String nextVersionNumber;
		if (StringUtils.isBlank(version)) {
			nextVersionNumber = "1.0";
		} else if (major) {
			int numberToIncrement = Integer.parseInt(StringUtils.substringBefore(version, "."));
			nextVersionNumber = (numberToIncrement + 1) + ".0";
		} else {
			int numberToIncrement = Integer.parseInt(StringUtils.substringAfterLast(version, "."));
			nextVersionNumber = StringUtils.substringBeforeLast(version, ".") + "." + (numberToIncrement + 1);
		}
		return nextVersionNumber;
	}

}
