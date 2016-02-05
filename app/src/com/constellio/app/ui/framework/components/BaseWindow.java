package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Window;

public class BaseWindow extends Window {
	
	public static final int OVER_ADVANCED_SEARCH_FORM_Z_INDEX = 20001;
	
	private Integer zIndex = null;

	public BaseWindow() {
	}

	public BaseWindow(String caption) {
		super(caption);
	}

	public BaseWindow(String caption, Component content) {
		super(caption, content);
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
