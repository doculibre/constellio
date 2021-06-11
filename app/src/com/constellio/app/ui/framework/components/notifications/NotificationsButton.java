package com.constellio.app.ui.framework.components.notifications;

import com.constellio.app.services.background.UpdateServerPingBackgroundAction.Notification;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class NotificationsButton extends WindowButton {
	public NotificationsButton() {
		super($("ConstellioHeader.notifications"),
				$("ConstellioHeader.notificationsPanelTitle"), new WindowConfiguration(true, true, "50%", null) {
					@Override
					public List<String> getAdditionalStyleNames() {
						return Collections.singletonList("header-notifications-window");
					}
				});

		setIcon(FontAwesome.BELL_O);
		setBadgeVisible(true);
		setBadgeCount(0);
		addStyleName("header-notifications-button");
		setEnabled(false);
	}

	@Override
	protected Component buildWindowContent() {
		NotificationsPanel notificationsPanel = newNotificationPanel();
		notificationsPanel = notificationsPanel != null ? notificationsPanel : new NotificationsPanel(null, null);

		notificationsPanel.addPanelWantsToCloseListener(args -> getWindow().close());

		Map<String, Notification> notifications = notificationsPanel.getNotifications();
		if (notifications != null && !notifications.isEmpty()) {
			int size = notifications.size();

			size = Math.min(size, 3);

			getWindow().setHeight(size * 50 + 110 + "px");
		}

		return notificationsPanel;
	}

	public abstract NotificationsPanel newNotificationPanel();
}
