package com.constellio.app.ui.framework.components.viewers.html;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.JavaScript;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

public class BaseHtmlFrame extends BrowserFrame {
	private static final Logger LOGGER = LogManager.getLogger(BaseHtmlFrame.class);
	private static final String STREAM_NAME = "BaseHtmlFrame.InputStream";

	private static final int DEFAULT_WIDTH = 750;
	private static final int DEFAULT_HEIGHT = 1000;

	private boolean sandboxed;
	private Resource htmlContentResource;

	public BaseHtmlFrame(String htmlContent) {
		this(htmlContent, true);
	}

	public BaseHtmlFrame(String htmlContent, boolean sandboxed) {
		this.sandboxed = sandboxed;
		setId(UUID.randomUUID().toString());

		if (this.sandboxed) {
			JavaScript.getCurrent().execute("document.getElementById('" + getId() + "').childNodes[0].setAttribute('sandbox','allow-same-origin allow-forms')");
		}

		htmlContentResource = new StreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return new AutoCloseInputStream(new ByteArrayInputStream(htmlContent.getBytes()));
			}
		}, $(STREAM_NAME) + ".html");
		setSource(htmlContentResource);
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
			setWidth(widthStr);
			setWidth(heightStr);
		} catch (Throwable t) {
			LOGGER.error(ExceptionUtils.getStackTrace(t));
			setVisible(false);
		}
	}
}
