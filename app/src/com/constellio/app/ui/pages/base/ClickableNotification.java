package com.constellio.app.ui.pages.base;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.Collection;

/**
 * Created by Constellio on 2017-05-23.
 */
public class ClickableNotification extends Window {

	public static ClickableNotification show(final UI ui, final String windowCaption, final String message) {
		final ClickableNotification notification = new ClickableNotification();
		notification.setCaptionAsHtml(true);
		notification.setCaption(windowCaption);
		StringBuilder stringBuilder = new StringBuilder();
		appendOtherClickableWindows(stringBuilder, ui);
		stringBuilder.append(message);
		notification.setContent(buildNotificationContent(stringBuilder.toString()));
		//TODO ADD LISTENER TO CLOSE WINDOW WHEN CLICK OUTSIDE BUT NOT WHEN CLICK HYPERLINK
		//        notification.addBlurListener(new FieldEvents.BlurListener() {
		//
		//            @Override
		//            public void blur(FieldEvents.BlurEvent event) {
		//                notification.close();
		//
		//            }
		//        });

		notification.show(ui, true);
		return notification;
	}

	public static void show(UI ui, String windowCaption, VerticalLayout verticalLayout) {
		ClickableNotification notification = new ClickableNotification();
		notification.setCaptionAsHtml(true);
		notification.setCaption(windowCaption);
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		notification.setContent(verticalLayout);

		notification.show(ui, true);
	}

	private static void appendOtherClickableWindows(StringBuilder stringBuilder, UI ui) {
		Collection<Window> windows = ui.getWindows();
		for (Window window : windows) {
			if (window != null && window instanceof ClickableNotification) {
				try {
					ClickableNotification notification = (ClickableNotification) window;
					VerticalLayout mainLayout = (VerticalLayout) notification.getContent();
					Label label = (Label) mainLayout.getComponent(0);
					stringBuilder.append(label.getValue());
					stringBuilder.append("<br><br>");
					ui.removeWindow(notification);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Component buildNotificationContent(String message) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		Label label = new Label(message, ContentMode.HTML);
		mainLayout.addComponent(label);
		return mainLayout;
	}

	public final void show(final UI ui, final boolean modal) {
		center();
		//        focus();
		setModal(modal);
		ui.addWindow(this);
	}
}
