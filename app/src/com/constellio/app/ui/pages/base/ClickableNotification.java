package com.constellio.app.ui.pages.base;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

/**
 * Created by Constellio on 2017-05-23.
 */
public class ClickableNotification extends Window {

    public static ClickableNotification show(final UI ui, final String windowCaption, final String message) {
        final ClickableNotification notification = new ClickableNotification();
        notification.setCaptionAsHtml(true);
        notification.setCaption(windowCaption);
        notification.setContent(buildNotificationContent(message));
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
