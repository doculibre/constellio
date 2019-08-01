package com.constellio.app.ui.framework.components.viewers.document;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.data.io.ConversionManager;
import com.constellio.model.services.contents.ContentManager;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
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
import java.util.Map;

public class DocumentViewer extends CustomComponent {

	private static final Logger LOGGER = LogManager.getLogger(DocumentViewer.class);

	private static boolean useCache = false;

	private static Map<String, File> cache = new HashMap<>();

	private static ConversionManager conversionManager;

	private static File tempDir;

	static {
		if (useCache) {
			conversionManager = ConstellioFactories.getInstance().getDataLayerFactory().getConversionManager();
		}
	}

	public static String[] CONVERSION_EXTENSIONS = ArrayUtils.removeElements(ConversionManager.getSupportedExtensions(), new String[]{"pdf"});

	public static String[] SUPPORTED_EXTENSIONS = ArrayUtils.add(ConversionManager.getSupportedExtensions(), "pdf");

	private static final int DEFAULT_WIDTH = 750;

	private static final int DEFAULT_HEIGHT = 1000;
	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	private Resource contentResource;

	private File file;

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


				//				String iframeHTML = "<iframe src = \"./VAADIN/themes/constellio/viewerjs/index.html?/VIEWER/#../../../../" + contentURL + "\" width=\"" + width + "\" height=\"" + height + "\" allowfullscreen webkitallowfullscreen></iframe>";
				String iframeHTML = "<iframe src = \"./VAADIN/themes/constellio/pdfjs/web/viewer.html?file=../../../../../" + contentURL + "\" width=\"100%\" height=\"100%\" allowfullscreen webkitallowfullscreen></iframe>";
				Component compositionRoot = new Label(iframeHTML, ContentMode.HTML);
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

}