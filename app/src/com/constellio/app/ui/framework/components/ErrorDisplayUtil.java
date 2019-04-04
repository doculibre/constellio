package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashSet;
import java.util.Set;

import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class ErrorDisplayUtil {
	public static void showErrorMessage(String message) {
		Notification notification = new Notification(message + "<br/><br/>" + $("clickToClose"), Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	public static void showBackendValidationException(ValidationErrors validationErrors) {
		Set<String> globalErrorMessages = new HashSet<String>();
		for (ValidationError validationError : validationErrors.getValidationErrors()) {
			String errorMessage = $(validationError);
			globalErrorMessages.add(errorMessage);
		}

		if (!globalErrorMessages.isEmpty()) {
			StringBuffer globalErrorMessagesSB = new StringBuffer();
			for (String globalErrorMessage : globalErrorMessages) {
				globalErrorMessage = $(globalErrorMessage);
				if (globalErrorMessagesSB.length() != 0) {
					globalErrorMessagesSB.append("<br />");
				}
				globalErrorMessagesSB.append(globalErrorMessage);
			}
			showErrorMessage(globalErrorMessagesSB.toString());
		}
	}
}
