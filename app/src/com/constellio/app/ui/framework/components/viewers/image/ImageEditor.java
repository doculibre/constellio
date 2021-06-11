package com.constellio.app.ui.framework.components.viewers.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseRequestHandler;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.pdf.pdfjs.PdfJSAnnotations;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

public class ImageEditor extends CustomComponent implements ViewChangeListener {

	private static final String RESOURCE_KEY_PREFIX = "ImageEditor.file.";

	public static String[] SUPPORTED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 1000;
	private static final String URL_PREFIX = "../../../../";

	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	private File file;
	
	private String imageEditorResourceKey;

	private ImageEditorSaveRequestHandler saveRequestHandler;
	
	public ImageEditor(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
		init();
	}

	public ImageEditor(File file) {
		this.file = file;
		init();
	}

	private void init() {
		imageEditorResourceKey = RESOURCE_KEY_PREFIX + UUID.randomUUID().toString();
		ensureRequestHandler();
	}

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		ConstellioUI.getCurrent().getNavigator().removeViewChangeListener(this);
		ConstellioUI.getCurrent().removeRequestHandler(saveRequestHandler);
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void attach() {
		super.attach();

		try {
			String saveButtonCallbackURL = saveRequestHandler.getCallbackURL();
			
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

			Resource contentResource;
			String filename;
			InputStream in;
			if (recordVO != null) {
				String version = contentVersionVO.getVersion();
				filename = contentVersionVO.getFileName();
				contentResource = ConstellioResourceHandler.createResource(recordVO.getId(), metadataCode, version, filename);
				in = contentVersionVO.getInputStreamProvider().getInputStream(getClass().getSimpleName());
			} else if (file != null) {
				filename = file.getName();
				contentResource = ConstellioResourceHandler.createResource(file);
				in = new FileInputStream(file);
			} else {
				filename = null;
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

				ResourceReference contentResourceReference = ResourceReference.create(contentResource, this, "ImageEditor.file");
				String contentURL = URL_PREFIX + contentResourceReference.getURL();
				
				StringBuilder params = new StringBuilder("?");
				params.append("locale=" + getLocale().getLanguage());
				params.append("&");
				params.append("path=" + contentURL);
				params.append("&");
				params.append("name=" + filename);
				params.append("&");
				params.append("saveButtonCallbackURL=" + saveButtonCallbackURL);
				params.append("&");
				params.append("imageEditorResourceKey=" + imageEditorResourceKey);
				
				String editorURL = "./VAADIN/themes/constellio/tui.image-editor/index.html" + params;
				
				String iframeHTML = "<iframe src = \"" + editorURL + "\" width=\"100%\" height=\"100%\" style=\"border:0\" allowfullscreen webkitallowfullscreen></iframe>";
				Component compositionRoot = new Label(iframeHTML, ContentMode.HTML);
				compositionRoot.setWidth(widthStr);
				compositionRoot.setHeight(heightStr);
				setCompositionRoot(compositionRoot);

				setWidth("100%");
			}
		} catch (Throwable t) {
			// FIXME
			t.printStackTrace();
			setVisible(false);
		}
	}

	public static boolean isSupported(String fileName) {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		return Arrays.asList(SUPPORTED_EXTENSIONS).contains(extension);
	}

	private void ensureRequestHandler() {
		saveRequestHandler = new ImageEditorSaveRequestHandler(imageEditorResourceKey, ConstellioUI.getCurrent());
		ConstellioUI.getCurrent().addRequestHandler(saveRequestHandler);
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(this);
	}

	protected void onSave(String imageDataURL, ConstellioUI uiId) {
	}

	public class ImageEditorSaveRequestHandler extends BaseRequestHandler {

		private ConstellioUI constellioUI;
		private String imageEditorResourceKey;

		public ImageEditorSaveRequestHandler(String imageEditorFileResourceKey, ConstellioUI constellioUI) {
			super(ImageEditorSaveRequestHandler.class.getName());
			this.imageEditorResourceKey = imageEditorFileResourceKey;
			this.constellioUI = constellioUI;
		}

		public String getCallbackURL() {
			StringBuilder sb = new StringBuilder();
			sb.append(URL_PREFIX);
			sb.append(getPath());
			return sb.toString();
		}

		private String readRequestInputStream(VaadinRequest request) throws IOException {
			String result;
			try (InputStream in = request.getInputStream()) {
				result = IOUtils.toString(in, "UTF-8");
			}
			return result;
		}

		@Override
		protected boolean handleRequest(String requestURI, Map<String, String> paramsMap, User user,
										VaadinSession session, VaadinRequest request, VaadinResponse response)
				throws IOException {
			boolean handled;

			String jsonString = readRequestInputStream(request);
			if (StringUtils.isNotBlank(jsonString)) {
				JSONObject jsonObject = new JSONObject(jsonString);
				String key = jsonObject.getString("imageEditorResourceKey");
				if (imageEditorResourceKey.equals(key)) {
					String imageDataURL = jsonObject.getString("imageDataURL");
					onSave(imageDataURL, constellioUI);
					handled = true;
				} else {
					handled = false;
				}
			} else {
				handled = false;
			}
			String key = request.getParameter("imageEditorResourceKey");
			if (imageEditorResourceKey.equals(key)) {
				String imageDataURL = request.getParameter("imageDataURL");
				onSave(imageDataURL, constellioUI);
				handled = true;
			} else {
				handled = false;
			}
			return handled;
		}

	}

}
