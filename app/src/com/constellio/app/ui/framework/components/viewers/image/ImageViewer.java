package com.constellio.app.ui.framework.components.viewers.image;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

@JavaScript({"theme://jquery/jquery-2.1.4.min.js", "theme://iviewer/jquery-ui.min.js", "theme://iviewer/jquery.mousewheel.min.js", "theme://iviewer/jquery.iviewer.min.js"})
@StyleSheet("theme://iviewer/jquery.iviewer.css")
public class ImageViewer extends CustomComponent {

	private String javascriptToExecute;

	public static String[] SUPPORTED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "tif", "tiff"};
	private static String[] NEED_CONVERSION_EXTENSIONS = {"tif", "tiff"};

	private static final int DEFAULT_WIDTH = 800;

	private static final int DEFAULT_HEIGHT = 1000;
	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	private File file;

	public ImageViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
	}

	public ImageViewer(File file) {
		this.file = file;
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

			String id = getConnectorId();
			String divId = id + "-viewer";

			Resource contentResource;
			InputStream in;
			if (recordVO != null) {
				String version = contentVersionVO.getVersion();
				String filename = contentVersionVO.getFileName();
				if (Arrays.asList(NEED_CONVERSION_EXTENSIONS).contains(recordVO.getExtension()) ||
					ConstellioResourceHandler.isContentOversized(recordVO.getId(), metadataCode, version)) {
					if (ConstellioResourceHandler.hasContentJpegConversion(recordVO.getId(), metadataCode, version)) {
						contentResource = ConstellioResourceHandler.createConvertedResource(recordVO.getId(), metadataCode, version, filename);
					} else {
						setVisible(false);
						return;
					}
				} else {
					contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, filename);
				}
				in = contentVersionVO.getInputStreamProvider().getInputStream(getClass().getSimpleName());
			} else if (file != null) {
				contentResource = ConstellioResourceHandler.createResource(file);
				in = new FileInputStream(file);
			} else {
				contentResource = null;
				in = null;
			}

			if (contentResource != null) {
				BufferedImage bufferedImage;
				try {
					bufferedImage = ImageIO.read(in);
				} finally {
					IOUtils.closeQuietly(in);
				}

				int imageWidth = bufferedImage.getWidth();
				int imageHeight = bufferedImage.getHeight();

				float heightWidthRatio = (float) imageHeight / imageWidth;
				float heightF = (float) heightWidthRatio * height;

				String widthStr = "" + width + widthUnits;
				String heightStr = "" + heightF + heightUnits;

				ResourceReference contentResourceReference = ResourceReference.create(contentResource, this, "ImageViewer.file");
				String contentURL = contentResourceReference.getURL();
				
//				String divHTML = "<div id=\"" + divId + "\" class=\"viewer\" style=\"position:relative; width:" + width + "px; height:"+ height + "px;\"></div>";

				StringBuffer js = new StringBuffer();
				js.append("var $ = jQuery;");
				js.append("\n");
				js.append("$(document).ready(function() {");
				js.append("\n");
				js.append("    var iv1 = $('#" + divId + "').iviewer({");
				js.append("\n");
				js.append("        src: '" + contentURL + "',");
				js.append("\n");
				js.append("        zoom_min: '1'");
				js.append("\n");
				js.append("    });");
				js.append("\n");
				js.append("    $('#" + divId + "').bind('ivieweronfinishload', function(ev, src) { $('#" + divId + ">img').css('top', '0'); });");
				// $('#viewer').bind('ivieweronfinishload', function(ev, src) { /* handle this */ })
				js.append("\n");
				js.append("});");
				js.append("\n");
				String divHTML = "<div id=\"" + divId + "\" class=\"viewer\" style=\"position:relative; width:100%; height:" + height + "px;\"></div>";

				Component compositionRoot = new Label(divHTML, ContentMode.HTML);
				compositionRoot.setWidth(widthStr);
				compositionRoot.setHeight(heightStr);
				setCompositionRoot(compositionRoot);
				
				javascriptToExecute = "setTimeout(function() {" + js.toString() + "}, 1)";
				show();

			}
		} catch (Throwable t) {
			// FIXME
			t.printStackTrace();
			setVisible(false);
		}
	}

	public void show() {
		com.vaadin.ui.JavaScript javascript = com.vaadin.ui.JavaScript.getCurrent();
		javascript.execute(javascriptToExecute);
	}

	public static boolean isSupported(String fileName) {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		return Arrays.asList(SUPPORTED_EXTENSIONS).contains(extension);
	}

}
