package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.ui.pages.extrabehavior.ProvideSecurityWithNoUrlParamSupport;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class ViewWindow extends BaseWindow {

	public static final String WINDOW_STYLE_NAME = "view-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	private BaseViewImpl view;
	private ViewWindowPresenter viewWindowPresenter;

	public ViewWindow(BaseViewImpl view) throws UserDoesNotHaveAccessException {
		viewWindowPresenter = new ViewWindowPresenter(this);

		addStyleName(WINDOW_STYLE_NAME);
		setHeight("95%");
		setWidth("95%");
		setResizable(true);
		setModal(false);
		center();

		setView(view);
	}

	private void throwMissingProvideSecurityWithNoUrlParamSupportIntereface(Component view) {
		throw new IllegalArgumentException(view.getClass().getSimpleName() + " interface is missing.");
	}

	public void showErrorMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	@Override
	public void setContent(Component content) {
		if (content != null) {
			content.addStyleName(WINDOW_CONTENT_STYLE_NAME);
			
			BaseViewImpl view;
			if (content instanceof BaseViewImpl) {
				view = (BaseViewImpl) content;
			} else {
				DragAndDropWrapper dragAndDropWrapper = (DragAndDropWrapper) content;
				view = (BaseViewImpl) dragAndDropWrapper.getDropHandler();
			}
			this.view = view;
			
			view.enter(null);
			//			String viewHeight = getViewHeight();
			//			if (viewHeight != null) {
			//				view.setHeight(viewHeight);
			//			}
		}
		super.setContent(content);
	}

	public void setView(BaseViewImpl baseView) throws UserDoesNotHaveAccessException {
		if (baseView != null) {
			boolean isSecurityCheckPossible = (baseView instanceof ProvideSecurityWithNoUrlParamSupport);

			if (throwWhenSecurityCheckIsNotPossible() && !isSecurityCheckPossible) {
				throwMissingProvideSecurityWithNoUrlParamSupportIntereface(baseView);
				return;
			}

			viewWindowPresenter.hasPageAccess(baseView);
		}
		
		if (baseView instanceof DropHandler) {
			DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(baseView) {
				@Override
				public void setDropHandler(DropHandler dropHandler) {
					if (ResponsiveUtils.isFileDropSupported()) {
						super.setDropHandler(dropHandler);
					}
				}
			};
			dragAndDropWrapper.setSizeFull();
			dragAndDropWrapper.setDropHandler((DropHandler) baseView);		
			setContent(dragAndDropWrapper);
		} else {
			setContent(baseView);
		}
	}

	public BaseViewImpl getView() {
		return view;
	}

	public boolean throwWhenSecurityCheckIsNotPossible() {
		return true;
	}

	protected String getViewHeight() {
		int browserWindowHeight = Page.getCurrent().getBrowserWindowHeight();
		int viewHeight = browserWindowHeight + 230;
		return viewHeight + "px";
	}
}
