package com.constellio.app.ui.framework.components.viewers.image;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.vaadin.annotations.JavaScript;
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

@JavaScript({"theme://jquery/jquery-2.1.4.min.js", "theme://tiff.js/tiff.min.js"})
public class TiffImageViewer extends CustomComponent {

	private String javascriptToExecute;

	public static String[] SUPPORTED_EXTENSIONS = {"tif", "tiff"};

	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 1000;

	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	private File file;

	public TiffImageViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
		init();
	}

	public TiffImageViewer(File file) {
		this.file = file;
		init();
	}

	private void init() {
		setVisible(computeVisibility());
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
				contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, filename);
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

				ResourceReference contentResourceReference = ResourceReference.create(contentResource, this, "TiffImageViewer.file");
				String contentURL = contentResourceReference.getURL();

				//			String divHTML = "<div id=\"" + divId + "\" class=\"viewer\" style=\"position:relative; width:" + width + "px; height:"+ height + "px;\"></div>";

				StringBuffer js = new StringBuffer();
				js.append("var $ = jQuery;");
				js.append("\n");
				js.append("$(document).ready(function() {");
				js.append("\n");
				js.append("    console.info('Test');");
				js.append("\n");
				js.append("    Tiff.initialize({TOTAL_MEMORY: 16777216 * 10});");
				js.append("\n");
				js.append("    var xhr = new XMLHttpRequest();");
				js.append("\n");
				js.append("    xhr.open('GET', '" + contentURL + "');");
				js.append("\n");
				js.append("    xhr.responseType = 'arraybuffer';");
				js.append("\n");
				js.append("    xhr.onload = function (e) {");
				js.append("\n");
				js.append("        var buffer = xhr.response;");
				js.append("\n");
				js.append("        var tiff = new Tiff({buffer: buffer});");
				js.append("\n");
				js.append("        for (var i = 0, len = tiff.countDirectory(); i < len; ++i) {");
				js.append("\n");
				js.append("          tiff.setDirectory(i);");
				js.append("\n");
				js.append("          var canvas = tiff.toCanvas();");
				js.append("\n");
				js.append("          canvas.setAttribute('style', 'width:100%');");
				//				js.append("\n");
				js.append("          $('#" + divId + "').append(canvas);");
				js.append("\n");
				js.append("          var spacer = document.createElement(\"div\");");
				js.append("\n");
				js.append("          spacer.setAttribute('style', 'height:10px; background-color: #fafafa;');");
				js.append("\n");
				js.append("          $('#" + divId + "').append(spacer);");
				js.append("\n");
				js.append("        }");
				js.append("\n");
				js.append("    };");
				js.append("\n");
				js.append("    xhr.send();");
				js.append("\n");
				js.append("});");
				String divHTML = "<div id=\"" + divId + "\" class=\"tiffviewer\" style=\"position:relative; width:100%; height:" + heightStr + ";\"></div>";

				Component compositionRoot = new Label(divHTML, ContentMode.HTML);
				compositionRoot.setWidth(widthStr);
				compositionRoot.setHeight(heightStr);
				setCompositionRoot(compositionRoot);

				setWidth("100%");

				javascriptToExecute = "setTimeout(function() {" + js.toString() + "}, 1)";
				show();
			}
		} catch (Throwable t) {
			// FIXME
			t.printStackTrace();
			setVisible(false);
		}
	}

	private boolean computeVisibility() {
		boolean visible;

		if (recordVO != null) {
			visible = true;
		} else if (file != null) {
			visible = true;
		} else {
			visible = false;
		}

		return visible;
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
