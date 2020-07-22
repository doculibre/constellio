package com.constellio.app.ui.framework.components.viewers.email;

import com.constellio.app.api.extensions.params.ParseEmailMessageParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.viewers.html.BaseHtmlFrame;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.ConversionManager;
import com.constellio.model.services.emails.EmailServices;
import com.vaadin.server.Page;
import com.vaadin.ui.CustomComponent;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class EmailViewer extends CustomComponent {

	public static String[] SUPPORTED_EXTENSIONS = {"eml", "msg"};

	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 1000;

	private RecordVO recordVO;

	private String metadataCode;

	private ContentVersionVO contentVersionVO;

	private File file;

	public EmailViewer(RecordVO recordVO, String metadataCode, ContentVersionVO contentVersionVO) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		this.contentVersionVO = contentVersionVO;
		init();
	}

	public EmailViewer(File file) {
		this.file = file;
		init();
	}

	private void init() {
	}

	@SuppressWarnings("deprecation")
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

			InputStream in = getInputStream();
			String filename = getFilename();
			if (in != null) {
				AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

				ParseEmailMessageParams parseEmailMessageParams = new ParseEmailMessageParams(in, filename);
				MimeMessage mimeMessage;
				try {
					mimeMessage = appLayerFactory.getExtensions().getSystemWideExtensions().parseEmailMessage(parseEmailMessageParams);
				} finally {
					IOUtils.closeQuietly(in);
				}
				if (mimeMessage == null) {
					EmailServices emailServices = new EmailServices();
					in = getInputStream();
					try {
						mimeMessage = emailServices.parseMimeMessage(in);
					} finally {
						IOUtils.closeQuietly(in);
					}
				}
				if (mimeMessage != null) {
					String htmlContent = getHtmlTextFromMessage(mimeMessage);
					if (htmlContent != null) {
						BaseHtmlFrame compositionRoot = new BaseHtmlFrame(htmlContent);
						compositionRoot.setWidth(widthStr);
						compositionRoot.setHeight(heightStr);
						setCompositionRoot(compositionRoot);
					} else {
						setVisible(false);
					}
				} else {
					setVisible(false);
				}
				//				setWidth("100%");
			}
		} catch (Throwable t) {
			// FIXME
			t.printStackTrace();
			setVisible(false);
		}
	}

	private String getHtmlTextFromMessage(Message message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = "<pre>" + message.getContent().toString() + "</pre>";
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getHtmlTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private String getHtmlTextFromMimeMultipart(
			MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + "<pre>" + bodyPart.getContent() + "</pre>";
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				result = result + "\n" + org.jsoup.Jsoup.parse(html).html();
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getHtmlTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}

	private InputStream getInputStream() {
		InputStream in;
		if (recordVO != null) {
			in = contentVersionVO.getInputStreamProvider().getInputStream(getClass().getSimpleName());
		} else if (file != null) {
			try {
				in = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			in = null;
		}
		return in;
	}

	private String getFilename() {
		String filename;
		if (recordVO != null) {
			filename = contentVersionVO.getFileName();
		} else if (file != null) {
			filename = file.getName();
		} else {
			filename = null;
		}
		return filename;
	}

	public static boolean isSupported(String fileName) {
		boolean supported;
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if (Arrays.asList("eml", "msg").contains(extension)) {
			DataLayerFactory dataLayerFactory = ConstellioFactories.getInstance().getDataLayerFactory();
			ConversionManager conversionManager = dataLayerFactory.getConversionManager();

			List<String> conversionSupportedExtensions = Arrays.asList(conversionManager.getAllSupportedExtensions());
			supported = !conversionSupportedExtensions.contains(extension); // Otherwise, PDF Conversion will be used.
		} else {
			supported = false;
		}
		return supported;
	}

}
