package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.MainLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.SingleComponentContainer;

public class PagesComponentsExtensionParams {

	ConstellioHeader header;
	Component mainMenu;
	Layout footer;
	MainLayout mainLayout;
	SingleComponentContainer contentViewWrapper;
	Layout contentFooterWrapperLayout;

	public PagesComponentsExtensionParams(ConstellioHeader header, Component mainMenu, Layout footer, MainLayout mainLayout,
			SingleComponentContainer contentViewWrapper, Layout contentFooterWrapperLayout) {
		this.header = header;
		this.mainMenu = mainMenu;
		this.footer = footer;
		this.mainLayout = mainLayout;
		this.contentViewWrapper = contentViewWrapper;
		this.contentFooterWrapperLayout = contentFooterWrapperLayout;
	}

	public ConstellioHeader getHeader() {
		return header;
	}

	public Component getMainMenu() {
		return mainMenu;
	}

	public Layout getFooter() {
		return footer;
	}

	public MainLayout getMainLayout() {
		return mainLayout;
	}

	public SingleComponentContainer getContentViewWrapper() {
		return contentViewWrapper;
	}

	public Layout getContentFooterWrapperLayout() {
		return contentFooterWrapperLayout;
	}
}
