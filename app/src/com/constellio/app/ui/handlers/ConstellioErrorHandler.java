package com.constellio.app.ui.handlers;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.containers.exception.ContainerException.ContainerException_ItemListChanged;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table.CacheUpdateException;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ConstellioErrorHandler extends DefaultErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioErrorHandler.class);

	@Override
	public void error(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		LOGGER.error(throwable.getMessage(), throwable);

		BaseViewImpl view = getCurrentView();
		if (view != null) {
			if((!(view instanceof HomeView)) && (throwable instanceof CacheUpdateException)
					&& throwable.getCause() != null
					&& throwable.getCause() instanceof ContainerException_ItemListChanged) {

				view.updateUI();
				getCurrentView().showMessage(i18n.$("ConstellioErrorHandler.tableElement"));
			} else if (ConstellioUI.getCurrent().isProductionMode()) {
				if (throwable instanceof EofException) {
					LOGGER.error("Connection killed", throwable);
				} else {
					view.navigateTo().home();
				}
			} else {
				view.removeAllComponents();

				String indent = "&nbsp;&nbsp;&nbsp;&nbsp;";
				String cause = "<b>An exception occured:</b><br/>";
				String[] stackFrames = ExceptionUtils.getStackFrames(throwable);
				boolean first = true;
				for (String stackFrame : stackFrames) {
					if (first) {
						cause += indent;
						first = false;
					} else {
						cause += indent + indent;
					}
					cause += stackFrame + "<br/>";
				}
				Label label = new Label(cause, ContentMode.HTML);
				label.setSizeFull();

				view.addComponent(label);
				doDefault(event);
			}
		}
	}

	private BaseViewImpl getCurrentView() {
		UI ui = UI.getCurrent();
		return ComponentTreeUtils.getFirstChild(ui, BaseViewImpl.class);
	}

}
