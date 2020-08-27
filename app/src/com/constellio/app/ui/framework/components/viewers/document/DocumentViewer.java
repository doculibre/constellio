package com.constellio.app.ui.framework.components.viewers.document;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.io.ConversionManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DocumentViewer extends CustomComponent {

	private static final Logger LOGGER = LogManager.getLogger(DocumentViewer.class);

	private static boolean useCache = false;

	private static Map<String, File> cache = new HashMap<>();

	private static ConversionManager conversionManager;

	private static File tempDir;

	static {
		conversionManager = ConstellioFactories.getInstance().getDataLayerFactory().getConversionManager();
	}

	public static String[] CONVERSION_EXTENSIONS = ArrayUtils.removeElements(conversionManager.getPreviewSupportedExtensions(), new String[]{"pdf"});

	public static String[] SUPPORTED_EXTENSIONS = ArrayUtils.add(conversionManager.getAllSupportedExtensions(), "pdf");

	private static final int DEFAULT_WIDTH = 750;

	private static final int DEFAULT_HEIGHT = 1000;
	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	private Resource contentResource;

	private File file;

	private Component compositionRoot;

	public DocumentViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
		init();
	}

	public DocumentViewer(File file) {
		this.file = file;
		init();
	}

	private void init() {
		resolveContentResource();
	}

	private void resolveContentResource() {
		try {
			if (recordVO != null) {
				String version = contentVersionVO.getVersion();
				String fileName = contentVersionVO.getFileName();
				String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));

				File documentViewerFile;
				if (useCache) {
					if (Arrays.asList(CONVERSION_EXTENSIONS).contains(extension)) {
						String hash = contentVersionVO.getHash();
						documentViewerFile = cache.get(hash);
						if (documentViewerFile == null) {
							ContentManager contentManager = ConstellioFactories.getInstance().getModelLayerFactory().getContentManager();
							InputStream in = null;
							try {
								in = contentManager.getContentInputStream(hash, getClass() + ".documentConversionFile");
								documentViewerFile = conversionManager.convertToPDF(in, fileName, tempDir);
								cache.put(hash, documentViewerFile);
							} finally {
								IOUtils.closeQuietly(in);
							}
						}
					} else {
						documentViewerFile = null;
					}
				} else {
					documentViewerFile = null;
				}
				if (documentViewerFile != null) {
					contentResource = ConstellioResourceHandler.createResource(documentViewerFile);
				} else {
					boolean preview = Arrays.asList(CONVERSION_EXTENSIONS).contains(extension);
					if (preview) {
						if (ConstellioResourceHandler.hasContentPreview(recordVO.getId(), metadataCode, version)) {
							contentResource = ConstellioResourceHandler.createPreviewResource(recordVO.getId(), metadataCode, version, fileName + ".pdf");
						} else {
							contentResource = null;
						}
					} else {
						contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, fileName);
					}
				}
			} else if (file != null) {
				contentResource = ConstellioResourceHandler.createResource(file);
			} else {
				contentResource = null;
			}
		} catch (Throwable t) {
			LOGGER.error(ExceptionUtils.getStackTrace(t));
			setVisible(false);
		}
	}

	@Override
	public void attach() {
		super.attach();

		try {
			int width = (int) getWidth();
			int height = (int) getHeight();
			Unit widthUnits = getWidthUnits();
			Unit heightUnits = getHeightUnits();

			if (width <= 0) {
				width = DEFAULT_WIDTH;
				widthUnits = Unit.PIXELS;
			}
			if (height <= 0) {
				height = DEFAULT_HEIGHT;
				heightUnits = Unit.PIXELS;
			}

			int maxWidth = Page.getCurrent().getBrowserWindowWidth();
			if (width > maxWidth) {
				width = maxWidth;
			}
			String widthStr = "" + width + widthUnits;
			String heightStr = "" + height + heightUnits;

			if (contentResource != null) {
				ResourceReference contentResourceReference = ResourceReference.create(contentResource, this, "DocumentViewer.file");
				String contentURL = contentResourceReference.getURL();

				Locale locale = getLocale();
				String contentPathPrefix;
				if (VaadinService.getCurrentRequest() != null) {
					String contextPath = VaadinService.getCurrentRequest().getContextPath();
					if (!StringUtils.endsWith(contextPath, "/")) {
						contextPath += "/";
					}
					contentPathPrefix = contextPath;
				} else {
					contentPathPrefix = "../../../../../";
				}

				ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
				AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
				ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
				UserServices userServices = modelLayerFactory.newUserServices();
				PresenterService presenterService = new PresenterService(modelLayerFactory);
				PdfJSServices pdfJSServices = new PdfJSServices(appLayerFactory);
				SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();

				User user = presenterService.getCurrentUser(sessionContext);
				String username = user.getUsername();
				UserCredential userCredentials = userServices.getUserCredential(username);
				String serviceKey = userCredentials.getServiceKey();
				if (serviceKey == null) {
					serviceKey = userServices.giveNewServiceKey(username);
				}
				String tokenAttributeName = "document_viewer_token";
				String token = ConstellioUI.getCurrent().getAttribute(tokenAttributeName);
				if (token == null || !userServices.isAuthenticated(serviceKey, token)) {
					token = userServices.generateToken(username);
					ConstellioUI.getCurrent().setAttribute(tokenAttributeName, token);
					final String finalToken = token;
					// Token only valid while current UI is alive
					ConstellioUI.getCurrent().addDetachListener(new DetachListener() {
						@Override
						public void detach(DetachEvent event) {
							ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
							ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
							UserServices userServices = modelLayerFactory.newUserServices();
							userServices.removeToken(finalToken);
						}
					});
				}

				String viewerUrl;
				if (recordVO != null) {
					Record record = presenterService.getRecord(recordVO.getId());
					MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record);
					Metadata metadata = metadataSchema.get(metadataCode);
					viewerUrl = pdfJSServices.getInternalViewerUrl(record, metadata, user, locale, contentPathPrefix, contentPathPrefix + contentURL, serviceKey, token);
				} else {
					viewerUrl = pdfJSServices.getInternalViewerUrl(null, null, user, locale, contentPathPrefix, contentPathPrefix + contentURL, serviceKey, token);
				}

				String iframeHTML = "<iframe src = \"" + viewerUrl + "\" width=\"100%\" height=\"100%\" allowfullscreen webkitallowfullscreen></iframe>";
				compositionRoot = new Label(iframeHTML, ContentMode.HTML);
				compositionRoot.setWidth(widthStr);
				compositionRoot.setHeight(heightStr);
				setCompositionRoot(compositionRoot);
			} else {
				setVisible(false);
			}
		} catch (Throwable t) {
			LOGGER.error(ExceptionUtils.getStackTrace(t));
			setVisible(false);
		}
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && contentResource != null;
	}

	public static boolean isSupported(String fileName) {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		return Arrays.asList(SUPPORTED_EXTENSIONS).contains(extension);
	}

	@Override
	public void setHeight(float height, Unit unit) {
		super.setHeight(height, unit);
		if (compositionRoot != null) {
			compositionRoot.setHeight(height, unit);
		}
	}

	@Override
	public void setWidth(float width, Unit unit) {
		super.setWidth(width, unit);
		if (compositionRoot != null) {
			compositionRoot.setWidth(width, unit);
		}
	}

}