package com.constellio.app.ui.framework.components;

import com.constellio.app.api.extensions.params.BaseWindowParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Window;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseWindow extends Window {

	public static final String WINDOW_STYLE_NAME = "base-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";
	public static final String WINDOW_CONTENT_SCROLL_STYLE_NAME = WINDOW_STYLE_NAME + "-content-scroll";

	public static final int OVER_ADVANCED_SEARCH_FORM_Z_INDEX = 20001;

	private Integer zIndex = null;

	private Float opacity = null;

	private float widthBeforeMinimize;
	private float heightBeforeMinimize;
	private Unit widthUnitsBeforeMinimize;
	private Unit heightUnitsBeforeMinimize;
	private int positionXBeforeMinimize;
	private int positionYBeforeMinimize;
	private boolean modalBeforeMinimize;
	private boolean resizableBeforeMinimize;

	private boolean minimized;

	private final MouseEvents.ClickListener restoreMinimizedListener = new MouseEvents.ClickListener() {
		@Override
		public void click(MouseEvents.ClickEvent event) {
			restoreMinimized();
		}
	};

	public BaseWindow() {
		init();
	}

	public BaseWindow(String caption) {
		super(caption);
		init();
	}

	public BaseWindow(String caption, Component content) {
		super(caption, content);
		init();
	}

	public void minimize() {
		if (!minimized) {
			setModal(false);
			setResizable(false);
			addClickListener(restoreMinimizedListener);

			widthBeforeMinimize = getWidth();
			heightBeforeMinimize = getHeight();

			widthUnitsBeforeMinimize = getWidthUnits();
			heightUnitsBeforeMinimize = getHeightUnits();

			positionXBeforeMinimize = getPositionX();
			positionYBeforeMinimize = getPositionY();

			modalBeforeMinimize = isModal();
			resizableBeforeMinimize = isResizable();

			int browserWidth = Page.getCurrent().getBrowserWindowWidth();
			int browserHeight = Page.getCurrent().getBrowserWindowHeight();

			float minimizedWith = 281;
			float mimizedHeight = 36;
			int minimizedPositionX = (int) (browserWidth - minimizedWith) - 20;
			int minimizedPositionY = (int) (browserHeight - mimizedHeight);

			setWidth(minimizedWith + "px");
			setHeight(mimizedHeight + "px");
			setPositionX(minimizedPositionX);
			setPositionY(minimizedPositionY);
			minimized = true;
		}
	}

	public void restoreMinimized() {
		if (minimized) {
			setWidth(widthBeforeMinimize, widthUnitsBeforeMinimize);
			setHeight(heightBeforeMinimize, heightUnitsBeforeMinimize);
			setPositionX(positionXBeforeMinimize);
			setPositionY(positionYBeforeMinimize);
			setModal(modalBeforeMinimize);
			setResizable(resizableBeforeMinimize);
			removeClickListener(restoreMinimizedListener);
			minimized = false;
		}
	}

	public boolean isMinimized() {
		return minimized;
	}

	private void init() {
		addStyleName(WINDOW_STYLE_NAME);
		addStyleName(WINDOW_CONTENT_SCROLL_STYLE_NAME);
		if (isRightToLeft()) {
			addStyleName("right-to-left");
		}
	}

	@Override
	public void setContent(Component content) {
		if (content != null) {
			content.addStyleName(WINDOW_CONTENT_STYLE_NAME);
		}
		super.setContent(content);
	}

	public final Integer getZIndex() {
		return zIndex;
	}

	public final void setZIndex(Integer zIndex) {
		this.zIndex = zIndex;
	}

	public final Float getOpacity() {
		return opacity;
	}

	public final void setOpacity(Float opacity) {
		this.opacity = opacity;
	}

	@Override
	public void attach() {
		super.attach();

		BaseWindowParams params = new BaseWindowParams(this);
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		appLayerFactory.getExtensions().getSystemWideExtensions().decorateWindow(params);

		if (zIndex != null) {
			executeZIndexAdjustJavascript(zIndex);
		}
		if (opacity != null) {
			executeOpacityAdjustJavascript(opacity);
		}
	}

	public static void executeZIndexAdjustJavascript(int zIndex) {
		executeZIndexAdjustJavascript(zIndex, null);
	}

	public static void executeZIndexAdjustJavascript(int zIndex, String className) {
		if (className == null) {
			className = "v-window";
		}
		String jsVarName = "var_" + ((int) (Math.random() * 1000)) + "_" + System.currentTimeMillis();
		StringBuffer zIndexFixJS = new StringBuffer();
		zIndexFixJS.append("var " + jsVarName + " = document.getElementsByClassName('" + className + "');\n");
		zIndexFixJS.append("for (i = 0; i < " + jsVarName + ".length; i++) {\n");
		zIndexFixJS.append("    " + jsVarName + "[i].style.zIndex=" + zIndex + ";\n");
		zIndexFixJS.append("}");

		JavaScript.getCurrent().execute(zIndexFixJS.toString());
	}

	public static void executeOpacityAdjustJavascript(float opacity) {
		String jsVarName = "var_" + ((int) (Math.random() * 1000)) + "_" + System.currentTimeMillis();
		StringBuffer zIndexFixJS = new StringBuffer();
		zIndexFixJS.append("var " + jsVarName + " = document.getElementsByClassName('v-window-modalitycurtain');\n");
		zIndexFixJS.append("for (i = 0; i < " + jsVarName + ".length; i++) {\n");
		zIndexFixJS.append("    " + jsVarName + "[i].style.opacity=" + opacity + ";\n");
		zIndexFixJS.append("}");

		JavaScript.getCurrent().execute(zIndexFixJS.toString());
	}

	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

}
