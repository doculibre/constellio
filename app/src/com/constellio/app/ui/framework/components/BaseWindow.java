package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Window;

public class BaseWindow extends Window {
	
	public static final int OVER_ADVANCED_SEARCH_FORM_Z_INDEX = 20001;
	
	private Integer zIndex = null;
	
	private float widthBeforeMinimize;
	private float heightBeforeMinimize;
	private Unit widthUnitsBeforeMinimize;
	private Unit heightUnitsBeforeMinimize;
	private int positionXBeforeMinimize;
	private int positionYBeforeMinimize;

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
		widthBeforeMinimize = getWidth();
		heightBeforeMinimize = getHeight();
		
		widthUnitsBeforeMinimize = getWidthUnits();
		heightUnitsBeforeMinimize = getHeightUnits();
		
		positionXBeforeMinimize = getPositionX();
		positionYBeforeMinimize = getPositionY();
		
		int browserWidth = Page.getCurrent().getBrowserWindowWidth();
		int browserHeight = Page.getCurrent().getBrowserWindowHeight();
		
		float minimizedWith = 150;
		float mimizedHeight = 90;
		int minimizedPositionX = (int) (browserWidth - minimizedWith);
		int minimizedPositionY = (int) (browserHeight - mimizedHeight);
		
		setWidth(minimizedWith + "px");
		setHeight(mimizedHeight + "px");
		setPositionX(minimizedPositionX);
		setPositionY(minimizedPositionY);
	}
	
	public void restoreMinimized() {
		setWidth(widthBeforeMinimize, widthUnitsBeforeMinimize);
		setHeight(heightBeforeMinimize, heightUnitsBeforeMinimize);
		setPositionX(positionXBeforeMinimize);
		setPositionY(positionYBeforeMinimize);
	} 
	
	private void init() {
	}

	public final Integer getZIndex() {
		return zIndex;
	}

	public final void setZIndex(Integer zIndex) {
		this.zIndex = zIndex;
	}

	@Override
	public void attach() {
		super.attach();
		if (zIndex != null) {
			executeZIndexAdjustJavascript(zIndex);
		}
	}
	
	public static void executeZIndexAdjustJavascript(int zIndex) {
		String jsVarName = "var_" + ((int) (Math.random() * 1000)) + "_" + System.currentTimeMillis();
		StringBuffer zIndexFixJS = new StringBuffer();
		zIndexFixJS.append("var " + jsVarName + " = document.getElementsByClassName('v-window');\n");
		zIndexFixJS.append("for (i = 0; i < " + jsVarName + ".length; i++) {\n");
	    zIndexFixJS.append("    " + jsVarName + "[i].style.zIndex=" + zIndex + ";\n");
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
