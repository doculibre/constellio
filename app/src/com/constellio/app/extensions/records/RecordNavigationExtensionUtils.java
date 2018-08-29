package com.constellio.app.extensions.records;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

public class RecordNavigationExtensionUtils {
	public static void showMessage(String message) {
		Notification notification = new Notification(message, Notification.Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}
}
