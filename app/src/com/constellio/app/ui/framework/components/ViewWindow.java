package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;

public class ViewWindow extends BaseWindow {

	public static final String WINDOW_STYLE_NAME = "view-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	private BaseViewImpl view;
	
	public ViewWindow(BaseViewImpl view) {
		addStyleName(WINDOW_STYLE_NAME);
		setHeight("95%");
		setWidth("95%");
		setResizable(true);
		setModal(false);
		center();
		
		setContent(view);
	}

	@Override
	public void setContent(Component content) {
		if (content != null) {
			content.addStyleName(WINDOW_CONTENT_STYLE_NAME);
			
			BaseViewImpl view = (BaseViewImpl) content;
			this.view = view;
			
			view.enter(null);
			//			String viewHeight = getViewHeight();
			//			if (viewHeight != null) {
			//				view.setHeight(viewHeight);
			//			}
		}
		super.setContent(content);
	}

	public BaseViewImpl getView() {
		return view;
	}

	protected String getViewHeight() {
		int browserWindowHeight = Page.getCurrent().getBrowserWindowHeight();
		int viewHeight = browserWindowHeight + 230;
		return viewHeight + "px";
	}

}
