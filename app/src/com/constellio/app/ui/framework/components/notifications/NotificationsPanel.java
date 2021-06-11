package com.constellio.app.ui.framework.components.notifications;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.app.services.background.UpdateServerPingBackgroundAction.Notification;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.icons.DefaultIconService;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.notifications.NotificationsPanel.PanelWantsToCloseArgs.PanelWantsToCloseListener;
import com.constellio.app.ui.framework.components.notifications.NotificationsPanel.PanelWantsToCloseArgs.PanelWantsToCloseObservable;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static com.constellio.app.ui.i18n.i18n.$;

public class NotificationsPanel extends CustomComponent {
	private final PanelWantsToCloseObservable panelWantsToCloseObservable;
	private final Map<String, Notification> notifications;
	private final Consumer<Collection<String>> notificationsViewedCallback;

	private final IconService iconService;

	public NotificationsPanel(Map<String, Notification> notifications,
							  Consumer<Collection<String>> notificationsViewedCallback) {
		this.notifications = notifications != null ? notifications : Collections.emptyMap();

		this.notificationsViewedCallback = notificationsViewedCallback != null ? notificationsViewedCallback : keys -> {
		};
		panelWantsToCloseObservable = new PanelWantsToCloseObservable();
		iconService = getIconService();
		setSizeFull();
	}

	@Override
	public void attach() {
		super.attach();
		setCompositionRoot(buildMainComponent());
	}

	private Component buildMainComponent() {
		final VerticalLayout layout = new VerticalLayout();

		layout.setSizeFull();
		layout.addStyleName("header-selection-panel");

		final Component notificationsLayout = buildNotificationsLayout(notifications, notificationsViewedCallback);
		final Component buttonsLayout = buildButtonsLayout(notifications, notificationsViewedCallback);
		layout.addComponents(notificationsLayout, buttonsLayout);

		layout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
		layout.setExpandRatio(notificationsLayout, 1.0f);

		return layout;
	}

	private Component buildNotificationsLayout(Map<String, Notification> notifications,
											   Consumer<Collection<String>> notificationsViewedCallback) {
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.addStyleName("notifications-panel");

		final VerticalLayout notificationsLayout = new VerticalLayout();

		notifications.forEach((key, notification) -> {

			final HorizontalLayout notificationComponent = new I18NHorizontalLayout();
			notificationComponent.addStyleName("notification");
			notificationComponent.setSizeFull();

			final Component icon = buildIcon(notification);
			final Component notificationCaption = buildNotification(notification);
			final Component notificationViewedButton = buildNotificationViewedButton(key, keySet -> {
				notificationsViewedCallback.accept(keySet);
				notificationsLayout.removeComponent(notificationComponent);
			});

			notificationComponent.addComponents(icon, notificationCaption, notificationViewedButton);
			notificationComponent.setExpandRatio(notificationCaption, 1.0f);
			notificationComponent.setComponentAlignment(icon, Alignment.MIDDLE_CENTER);
			notificationComponent.setComponentAlignment(notificationViewedButton, Alignment.MIDDLE_CENTER);

			notificationsLayout.addComponent(notificationComponent);
		});

		panel.setContent(notificationsLayout);

		return panel;
	}

	private Component buildIcon(Notification notification) {
		String iconName = notification.getIconName();
		Label icon = new Label("");
		icon.addStyleName("notification-icon");

		if (iconName != null) {
			icon.setIcon(iconService.getIconByName(iconName));
		}

		icon.setWidth("30px");

		return icon;
	}

	private Component buildNotification(Notification notification) {
		String caption = notification.getCaption();

		Component notificationCaption = new Label(caption);
		notificationCaption.setSizeFull();
		notificationCaption.addStyleName("notification-caption");

		Component notificationComponent;
		if (notification.hasActionToExecuteOnClick()) {
			final Runnable actionToExecuteOnClick = notification.getActionToExecuteOnClick();

			final Panel notificationClickablePanel = new Panel(notificationCaption);
			notificationClickablePanel.addClickListener(clickEvent -> actionToExecuteOnClick.run());

			notificationClickablePanel.setSizeFull();
			notificationClickablePanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

			notificationClickablePanel.addStyleName("notification-with-action");

			notificationComponent = notificationClickablePanel;
		} else {
			notificationComponent = notificationCaption;
		}

		return notificationComponent;
	}

	private Component buildNotificationViewedButton(String key,
													Consumer<Collection<String>> notificationsViewedCallback) {
		Button button = new BaseButton("", iconService.getIconByName(iconService.getIconName(FontAwesome.EYE)), true) {
			@Override
			protected void buttonClick(ClickEvent event) {
				notificationsViewedCallback.accept(Collections.singletonList(key));
			}
		};

		button.addStyleName(ValoTheme.BUTTON_LINK);
		button.setWidth("30px");
		button.setDescription($("ConstellioHeader.notification.markAsViewed"));

		return button;
	}

	private Component buildButtonsLayout(Map<String, Notification> notifications,
										 Consumer<Collection<String>> notificationsViewedCallback) {
		Button markAsViewedButton = new BaseButton($("ConstellioHeader.notification.markAllAsViewed")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				notificationsViewedCallback.accept(notifications.keySet());
				panelWantsToCloseObservable.fire(new PanelWantsToCloseArgs(NotificationsPanel.this));
			}
		};
		markAsViewedButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		Button closeButton = new BaseButton($("ConstellioHeader.close")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				panelWantsToCloseObservable.fire(new PanelWantsToCloseArgs(NotificationsPanel.this));
			}
		};

		I18NHorizontalLayout buttonsLayout = new I18NHorizontalLayout();
		buttonsLayout.addStyleName("notifications-panel-button-layout");

		buttonsLayout.setHeight("50px");
		buttonsLayout.addComponents(markAsViewedButton, closeButton);
		buttonsLayout.setSpacing(true);

		return buttonsLayout;
	}

	public void addPanelWantsToCloseListener(PanelWantsToCloseListener listener) {
		panelWantsToCloseObservable.addListener(listener);
	}

	public void removePanelWantsToCloseListener(PanelWantsToCloseListener listener) {
		panelWantsToCloseObservable.removeListener(listener);
	}

	public IconService getIconService() {
		return new DefaultIconService(ConstellioFactories.getInstance().getAppLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}

	public Map<String, Notification> getNotifications() {
		return notifications;
	}

	public static class PanelWantsToCloseArgs extends EventArgs<NotificationsPanel> {
		public PanelWantsToCloseArgs(NotificationsPanel sender) {
			super(sender);
		}

		public interface PanelWantsToCloseListener extends EventListener<PanelWantsToCloseArgs> {
		}

		public static class PanelWantsToCloseObservable extends EventObservable<PanelWantsToCloseArgs> {
		}
	}
}
