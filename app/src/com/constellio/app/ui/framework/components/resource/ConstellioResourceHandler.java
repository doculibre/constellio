package com.constellio.app.ui.framework.components.resource;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioVaadinServlet;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.HttpSessionContext;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImageUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.util.FileTypeResolver;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class ConstellioResourceHandler implements RequestHandler {

	public enum ResourceType {
		NORMAL, PREVIEW, THUMBNAIL, JPEG_CONVERSION, ANNOTATION;
	}

	private static final long serialVersionUID = 1L;
	private static final String PATH = UUID.randomUUID().toString();

	@Override
	public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
			throws IOException {
		String sep = "/" + PATH + "/";
		String requestURI = ((VaadinServletRequest) request).getRequestURI();
		if (requestURI.indexOf(sep) != -1) {
			String params = StringUtils.substringAfter(requestURI, sep);

			Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
			String recordId = paramsMap.get("recordId");
			String metadataCode = paramsMap.get("metadataCode");
			String version = paramsMap.get("version");
			String preview = paramsMap.get("preview");
			String thumbnail = paramsMap.get("thumbnail");
			String jpegConversion = paramsMap.get("jpegConversion");
			String annotation = paramsMap.get("annotation");
			String contentId = paramsMap.get("contentId");
			String fileId = paramsMap.get("file");
			String hashParam = paramsMap.get("hash");
			String filenameParam = paramsMap.get("z-filename");

			String filename;
			InputStream in = null;
			if (recordId != null || hashParam != null) {
				ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
				ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
				UserServices userServices = modelLayerFactory.newUserServices();
				RecordServices recordServices = modelLayerFactory.newRecordServices();
				ContentManager contentManager = modelLayerFactory.getContentManager();
				IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
				MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

				if (hashParam != null) {
					filename = filenameParam;
					in = contentManager.getContentInputStream(hashParam, getClass().getSimpleName() + ".handleRequest");
				} else {
					VaadinSession vaadinSession = VaadinSession.getCurrent();
					UserVO userVO = (UserVO) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_USER_ATTRIBUTE);
					String collection = (String) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE);

					MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
					User user = userServices.getUserInCollection(userVO.getUsername(), collection);
					Record record = recordServices.getDocumentById(recordId);
					if ((!types.getSchemaType(record.getTypeCode()).hasSecurity() || "workflowModel".equals(record.getTypeCode()))
						|| user.hasReadAccess().on(record)) {
						String schemaCode = record.getSchemaCode();
						Metadata metadata = types.getMetadata(schemaCode + "_" + metadataCode);

						Object metadataValue = record.get(metadata);

						if (metadataValue instanceof List && contentId != null && ((List) metadataValue).size() > 0) {
							List listValue = (List) metadataValue;
							if (listValue.get(0) instanceof Content) {
								for (Content currentContent : (List<Content>) listValue) {
									if (contentId.equals(currentContent.getId())) {
										metadataValue = currentContent;
									}
								}
							}

						}

						if (metadataValue instanceof Content) {
							Content content = (Content) metadataValue;
							ContentVersion contentVersion = content.getVersion(version);

							String hash = contentVersion.getHash();
							filename = contentVersion.getFilename();
							if ("true".equals(preview)) {
								if (contentManager.hasContentPreview(hash)) {
									in = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + ".handleRequest");
								} else {
									in = null;
								}
							} else if ("true".equals(thumbnail)) {
								if (contentManager.hasContentThumbnail(hash)) {
									String extension = FilenameUtils.getExtension(filename);
									filename = StringUtils.substringBeforeLast(filename, extension) + "jpg";
									in = contentManager.getContentThumbnailInputStream(hash, getClass().getSimpleName() + ".handleRequest");
								} else {
									in = null;
								}
							} else if ("true".equals(jpegConversion)) {
								if (contentManager.hasContentJpegConversion(hash)) {
									String extension = FilenameUtils.getExtension(filename);
									filename = StringUtils.substringBeforeLast(filename, extension) + "jpg";
									in = contentManager.getContentJpegConversionInputStream(hash, getClass().getSimpleName() + ".handleRequest");
								} else {
									in = null;
								}
							} else if ("true".equals(annotation)) {
								if (contentManager.hasContentAnnotation(hash, recordId, version)) {
									in = contentManager.getContentAnnotationInputStream(hash, recordId, version, getClass().getSimpleName() + ".handleRequest");
								} else {
									in = null;
								}
							} else {
								in = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".handleRequest");
							}
						} else if (metadataValue instanceof String) {
							filename = paramsMap.get("z-filename");
							String stringValue = (String) metadataValue;
							in = ioServices.newByteInputStream(stringValue.getBytes(), getClass().getSimpleName() + ".handleRequest");
						} else {
							filename = null;
							in = null;
						}
					} else {
						filename = null;
						in = null;
					}
				}
			} else if (fileId != null) {
				String filePath = getFilePathMappedToId(fileId);

				if (filePath != null) {
					File file = new File(filePath);
					if (file.exists()) {

						ensureCanAccessFile(file);

						filename = file.getName();
						in = new FileInputStream(file);
					} else {
						filename = null;
						in = null;
					}
				} else {
					filename = null;
					in = null;
				}
			} else {
				filename = null;
				in = null;
			}

			if (in != null) {
				String mimeType = FileTypeResolver.getMIMEType(filename);
				response.setContentType(mimeType);

				OutputStream out = null;
				try {
					out = response.getOutputStream();
					IOUtils.copy(in, out);
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
			return true;
		}
		return false;
	}


	private static String getFilePathMappedToId(String fileId) {
		String filePath;

		HttpSessionContext sessionContext = new HttpSessionContext(ConstellioVaadinServlet.getCurrentHttpServletRequest());
		if (sessionContext != null) {
			Object potentialFilePath = sessionContext.getAttribute(fileId);

			if (potentialFilePath instanceof String) {
				filePath = (String) potentialFilePath;
			} else {
				filePath = null;
			}

			if (potentialFilePath != null) {
				sessionContext.setAttribute(fileId, null);
			}
		} else {
			filePath = null;
		}

		return filePath;
	}

	private static String setFilePathMappedToId(String filePath) {
		String fileId = UUID.randomUUID().toString();

		HttpSessionContext sessionContext = new HttpSessionContext(ConstellioVaadinServlet.getCurrentHttpServletRequest());
		sessionContext.setAttribute(fileId, filePath);

		return fileId;
	}

	private static void ensureCanAccessFile(File file) {
		/*
			TODO
			Gestion des folderLocations où on accepte pas d'url (webinf) Voir avec francis, comme identifier et authorizer les emplacement
			Tout authorize et refuser ou tout refuser et auhorizer
		 */

		FoldersLocator foldersLocator = new FoldersLocator();
		File webInfFolder = foldersLocator.getConstellioWebinfFolder();
		IOServicesFactory ioServicesFactory = ConstellioFactories.getInstance().getDataLayerFactory().getIOServicesFactory();

		String fileAbsolutePath = file.getAbsolutePath();

		Optional<String> optionalNotAllowLocation = Arrays.asList(webInfFolder.getAbsolutePath()).stream()
				.filter(Objects::nonNull)
				.filter(fileAbsolutePath::contains)
				.findFirst();

		if (optionalNotAllowLocation.isPresent()) {
			throw new RuntimeException("Access to Resource at " + optionalNotAllowLocation.get() + " is not allowed");
		}
	}

	public static Resource createResource(String recordId, String metadataCode, String version, String filename) {
		return createResource(recordId, metadataCode, version, filename, ResourceType.NORMAL);
	}

	public static Resource createPreviewResource(String recordId, String metadataCode, String version,
												 String filename) {
		return createResource(recordId, metadataCode, version, filename, ResourceType.PREVIEW);
	}

	public static Resource createThumbnailResource(String recordId, String metadataCode, String version,
												   String filename) {
		return createResource(recordId, metadataCode, version, filename, ResourceType.THUMBNAIL);
	}

	public static Resource createConvertedResource(String recordId, String metadataCode, String version,
												   String filename) {
		return createResource(recordId, metadataCode, version, filename, ResourceType.JPEG_CONVERSION);
	}

	public static Resource createAnnotationResource(String recordId, String metadataCode, String version,
													String fileName, String contentId) {
		return createResource(recordId, metadataCode, version, fileName, ResourceType.ANNOTATION, false, contentId);
	}

	private static Resource createResource(String recordId, String metadataCode, String version, String filename,
										   ResourceType resourceType) {
		return createResource(recordId, metadataCode, version, filename, resourceType, false);
	}

	public static Resource createResource(String recordId, String metadataCode, String version, String filename,
										  ResourceType resourceType, boolean useBrowserCache) {
		return createResource(recordId, metadataCode, version, filename, resourceType, useBrowserCache, null);
	}

	public static Resource createResource(String recordId, String metadataCode, String version, String filename,
										  ResourceType resourceType, boolean useBrowserCache, String contentId) {
		boolean jpg = resourceType == ResourceType.THUMBNAIL || resourceType == ResourceType.JPEG_CONVERSION;
		Map<String, String> params = new LinkedHashMap<>();
		params.put("recordId", recordId);
		params.put("metadataCode", metadataCode);
		params.put("preview", "" + (resourceType == ResourceType.PREVIEW));
		params.put("thumbnail", "" + (resourceType == ResourceType.THUMBNAIL));
		params.put("jpegConversion", "" + (resourceType == ResourceType.JPEG_CONVERSION));
		params.put("annotation", "" + (resourceType == ResourceType.ANNOTATION));
		if (contentId != null) {
			params.put("contentId", contentId);
		}
		params.put("version", version);
		if (jpg) {
			String extension = FilenameUtils.getExtension(filename);
			filename = StringUtils.substringBeforeLast(filename, extension) + "jpg";
		}
		params.put("z-filename", filename);
		if (!useBrowserCache) {
			Random random = new Random();
			params.put("cacheRandomizer", String.valueOf(random.nextLong()));
		}
		String resourcePath = ParamUtils.addParams(PATH, params);
		return new ExternalResource(resourcePath);
	}

	public static Resource createResource(File file) {
		String fileId = setFilePathMappedToId(file.getAbsolutePath());

		Map<String, String> params = new LinkedHashMap<>();
		params.put("file", fileId);
		String resourcePath = ParamUtils.addParams(PATH, params);
		return new ExternalResource(resourcePath);
	}

	public static Resource createResource(String hash, String filename) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("hash", hash);
		params.put("z-filename", filename);
		String resourcePath = ParamUtils.addParams(PATH, params);
		return new ExternalResource(resourcePath);
	}

	public static boolean hasContentPreview(String recordId, String metadataCode, String version) {
		Content content = getContent(recordId, metadataCode);
		if (content != null) {
			ContentVersion contentVersion = content.getVersion(version);
			String hash = contentVersion.getHash();
			return ConstellioFactories.getInstance().getModelLayerFactory().getContentManager()
					.hasContentPreview(hash);
		}
		return false;
	}

	public static boolean hasContentThumbnail(String recordId, String metadataCode, String version) {
		Content content = getContent(recordId, metadataCode);
		if (content != null) {
			ContentVersion contentVersion = content.getVersion(version);
			String hash = contentVersion.getHash();
			return ConstellioFactories.getInstance().getModelLayerFactory().getContentManager()
					.hasContentThumbnail(hash);
		}
		return false;
	}

	public static boolean hasContentJpegConversion(String recordId, String metadataCode, String version) {
		Content content = getContent(recordId, metadataCode);
		if (content != null) {
			ContentVersion contentVersion = content.getVersion(version);
			String hash = contentVersion.getHash();
			return ConstellioFactories.getInstance().getModelLayerFactory().getContentManager()
					.hasContentJpegConversion(hash);
		}
		return false;
	}

	public static boolean isContentOversized(String recordId, String metadataCode, String version) {
		Content content = getContent(recordId, metadataCode);
		if (content != null) {
			ContentVersion contentVersion = content.getVersion(version);
			String hash = contentVersion.getHash();
			try {
				ParsedContent parsedContent = ConstellioFactories.getInstance().getModelLayerFactory().getContentManager()
						.getParsedContent(hash);
				return ImageUtils.isImageOversized(Double.parseDouble((String) parsedContent.getProperties().get(org.apache.tika.metadata.Metadata.IMAGE_LENGTH.getName())));
			} catch (Throwable t) {
				return false;
			}
		}
		return false;
	}

	private static Content getContent(String recordId, String metadataCode) {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

		VaadinSession vaadinSession = VaadinSession.getCurrent();
		UserVO userVO = (UserVO) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_USER_ATTRIBUTE);
		String collection = (String) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE);

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		User user = userServices.getUserInCollection(userVO.getUsername(), collection);
		Record record = recordServices.realtimeGetRecordSummaryById(recordId);

		if (user.hasReadAccess().on(record)) {
			String schemaCode = record.getSchemaCode();
			Metadata metadata = types.getMetadata(schemaCode + "_" + metadataCode);
			if (metadata.isStoredInSummaryCache()) {
				return record.get(metadata);
			} else {
				record = recordServices.realtimeGetRecordById(recordId);
				return record.get(metadata);
			}

		}
		return null;
	}

}