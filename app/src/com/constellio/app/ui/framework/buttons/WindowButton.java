package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class WindowButton extends BaseButton implements Button.ClickListener {

	public static final String STYLE_NAME = "window-button";
	public static final String WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	private String windowCaption;
	private final WindowConfiguration configuration;
	private BaseWindow window;
	private Integer zIndex;

	private List<CloseListener> closeListeners = new ArrayList<>();

	public WindowButton(String caption, String windowCaption, WindowConfiguration configuration) {
		super(caption);
		this.windowCaption = windowCaption;
		this.configuration = configuration;
		addStyleName(STYLE_NAME);
		addClickListener(this);
	}

	public WindowButton(String caption, String windowCaption) {
		this(null, caption, windowCaption);
	}

	public WindowButton(Resource icon, String caption, String windowCaption) {
		this(icon, caption, windowCaption, false, WindowConfiguration.modalDialog("50%", "50%"));
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
		if (iconOnly) {
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		}
		addClickListener(this);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (window == null || !UI.getCurrent().getWindows().contains(window)) {
			window = newWindow(getWindowCaption());
			window.setId(WINDOW_STYLE_NAME);
			window.addStyleName(WINDOW_STYLE_NAME);
			window.setModal(configuration.isModal());
			window.setResizable(configuration.isResizable());
			if (configuration.getWidth() != null) {
				window.setWidth(configuration.getWidth());
			}
			if (configuration.getHeight() != null) {
				window.setHeight(configuration.getHeight());
			}

			if (acceptWindowOpen(event)) {
				Component windowContent = buildWindowContent();
				if (windowContent != null) {
					windowContent.addStyleName(WINDOW_CONTENT_STYLE_NAME);
					if (!windowContent.getStyleName().contains("scroll")) {
						windowContent.addStyleName("auto-scroll");
					}
					if (windowContent instanceof BaseViewImpl) {
						((BaseViewImpl) windowContent).enter(null);
					}
					window.setContent(windowContent);
					if (zIndex != null) {
						window.setZIndex(zIndex);
					}

					for (CloseListener listener : closeListeners) {
						window.addCloseListener(listener);
					}

					UI.getCurrent().addWindow(window);
					this.afterOpenModal();
				} else {
					window.close();
				}
			}
		}
	}

	protected BaseWindow newWindow(String windowCaption) {
		return new BaseWindow(windowCaption);
	}

	protected boolean acceptWindowOpen(ClickEvent event) {
		return true;
	}

	public void afterOpenModal() {

	}

	public WindowConfiguration getConfiguration() {
		return configuration;
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

	public List<CloseListener> getCloseListeners() {
		return Collections.unmodifiableList(closeListeners);
	}

	public void addCloseListener(CloseListener listener) {
		this.closeListeners.add(listener);
	}

	public void removeCloseListener(CloseListener listener) {
		this.closeListeners.remove(listener);
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
