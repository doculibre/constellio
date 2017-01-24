package com.constellio.app.ui.framework.buttons;

import java.io.Serializable;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public abstract class WindowButton extends Button implements Button.ClickListener {
	
	public static final String STYLE_NAME = "window-button";
	public static final String WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	private String windowCaption;
	private final WindowConfiguration configuration;
	private BaseWindow window;
	private Integer zIndex;

	public WindowButton(String caption, String windowCaption, WindowConfiguration configuration) {
		super(caption);
		this.windowCaption = windowCaption;
		this.configuration = configuration;
		addStyleName(STYLE_NAME);
		addClickListener(this);
	}

	public WindowButton(String caption, String windowCaption) {
		this(caption, windowCaption, WindowConfiguration.modalDialog("50%", "50%"));
	}

	public WindowButton(Resource icon, String caption, boolean iconOnly, WindowConfiguration configuration) {
		this(icon, caption, caption, iconOnly, configuration);
	}

	public WindowButton(
			Resource icon, String caption, String windowCaption, boolean iconOnly, WindowConfiguration configuration) {
		super(caption, icon);
		this.configuration = configuration;
		this.windowCaption = windowCaption;
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		if (iconOnly) {
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		}
		addClickListener(this);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		window = new BaseWindow(getWindowCaption());
		window.setId(WINDOW_STYLE_NAME);
		window.addStyleName(WINDOW_STYLE_NAME);
		window.setModal(configuration.isModal());
		window.setResizable(configuration.isResizable());
		window.setWidth(configuration.getWidth());
		window.setHeight(configuration.getHeight());
		Component windowContent = buildWindowContent();
		windowContent.addStyleName(WINDOW_CONTENT_STYLE_NAME);
		if (windowContent instanceof BaseViewImpl) {
			((BaseViewImpl) windowContent).enter(null);
		}
		window.setContent(windowContent);
		if (zIndex != null) {
			window.setZIndex(zIndex);
		}
		UI.getCurrent().addWindow(window);
	}

	protected String getWindowCaption() {
		return windowCaption;
	}

	public final Window getWindow() {
		return window;
	}

	public final Integer getZIndex() {
		return zIndex;
	}

	public final void setZIndex(Integer zIndex) {
		this.zIndex = zIndex;
	}

	public void setWindowCaption(String caption) {
		windowCaption = caption;
	}

	protected abstract Component buildWindowContent();

	public static class WindowConfiguration implements Serializable {
		private final boolean modal;
		private final boolean resizable;
		private final String width;
		private final String height;

		public static WindowConfiguration modalDialog(String width, String height) {
			return new WindowConfiguration(true, false, width, height);
		}

		public WindowConfiguration(boolean modal, boolean resizable, String width, String height) {
			this.modal = modal;
			this.resizable = resizable;
			this.width = width;
			this.height = height;
		}

		public boolean isModal() {
			return modal;
		}

		public boolean isResizable() {
			return resizable;
		}

		public String getWidth() {
			return width;
		}

		public String getHeight() {
			return height;
		}
	}

}
