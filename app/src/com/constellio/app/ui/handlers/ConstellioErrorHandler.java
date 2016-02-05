package com.constellio.app.ui.handlers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ConstellioErrorHandler extends DefaultErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioErrorHandler.class);

	@Override
	public void error(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		LOGGER.error(throwable.getMessage(), throwable);
		
		UI ui = UI.getCurrent();
		BaseViewImpl view = ComponentTreeUtils.getFirstChild(ui, BaseViewImpl.class);
		if (view != null) {
			if (ConstellioUI.getCurrent().isProductionMode()) {
				view.navigateTo().home();
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

}
