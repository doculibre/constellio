package com.constellio.app.ui.framework.components.viewers.document;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.modules.rm.extensions.RMDownloadContentVersionLinkExtension;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.contents.ContentManager;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

public class DocumentViewer extends CustomComponent {

	private static final Logger LOGGER = LogManager.getLogger(DocumentViewer.class);
	
	private static boolean useCache = false;
	
	private static Map<String, File> cache = new HashMap<>();
	
	private static ConversionManager conversionManager;
	
	static {
		if (useCache) {
			IOServices ioServices = ConstellioFactories.getInstance().getIoServicesFactory().newIOServices();
			File tempDir = ioServices.newTemporaryFolder(RMDownloadContentVersionLinkExtension.class + ".tempDir");
			conversionManager = new ConversionManager(ioServices, 1, tempDir);
		}
	}
	
	public static String[] CONVERSION_EXTENSIONS = { "doc", "docx" };
	
	public static String[] SUPPORTED_EXTENSIONS = { "doc", "docx", "pdf", "odt" };
	
	private static final int DEFAULT_WIDTH = 750;
	
	private static final int DEFAULT_HEIGHT = 600;
	
	private RecordVO recordVO;
	
	private String metadataCode;
	
	private ContentVersionVO contentVersionVO;
	
	private File file;

	public DocumentViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
	}
	
	public DocumentViewer(File file) {
		this.file = file;
	}

	@Override
	public void attach() {
		super.attach();
		
		try {
			int width = (int) getWidth();
			int height = (int) getHeight();
			
			if (width <= 0 || height <= 0) {
				width = DEFAULT_WIDTH;
				height = DEFAULT_HEIGHT;
			}
			
			int maxWidth = Page.getCurrent().getBrowserWindowWidth();
			if (width > maxWidth) {
				width = maxWidth;
			}
			
			Resource contentResource;
			if (recordVO != null) {
				String version = contentVersionVO.getVersion();
				String filename = contentVersionVO.getFileName();
				String extension = FilenameUtils.getExtension(filename);

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
								documentViewerFile = conversionManager.convertToPDF(in, filename);
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
							contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, filename + ".pdf", true);
						} else {
							contentResource = null;
						}
					} else {
						contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, filename, false);
					}
				}
			} else if (file != null) {
				contentResource = ConstellioResourceHandler.createResource(file);
			} else {
				contentResource = null;
			}
			
			if (contentResource != null) {
				ResourceReference contentResourceReference = ResourceReference.create(contentResource, this, "ViewerJS.file");
				String contentURL = contentResourceReference.getURL();
				
				String iframeHTML = "<iframe src = \"./VAADIN/themes/constellio/viewerjs/index.html?/VIEWER/#../../../../" + contentURL + "\" width=\"" + width + "\" height=\"" + height + "\" allowfullscreen webkitallowfullscreen></iframe>";
				setCompositionRoot(new Label(iframeHTML, ContentMode.HTML));
			}
		} catch (Throwable t) {
			LOGGER.error(ExceptionUtils.getStackTrace(t));
			setVisible(false);
		}
	}
	
	public static boolean isSupported(String filename) {
		String extension = FilenameUtils.getExtension(filename);
		return Arrays.asList(SUPPORTED_EXTENSIONS).contains(extension);
	}

}
