package com.constellio.app.ui.framework.components.menuBar;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseMenuBar extends MenuBar implements BrowserWindowResizeListener {

	private final boolean lazyLoading;

	private Map<MenuItem, String> rootItemNonResponsiveCaptions = new HashMap<>();

	private boolean desktopMode;

	public BaseMenuBar() {
		this(false, false);
	}

	public BaseMenuBar(boolean compact, boolean lazyLoading) {
		this.lazyLoading = lazyLoading;
		if (lazyLoading) {
			setId(UUID.randomUUID().toString());
		}
		if (isRightToLeft()) {
			addStyleName("menubar-rtl");
		}
		if (compact) {
			addStyleName("compact-menubar");
			addStyleName(ValoTheme.MENUBAR_BORDERLESS);
		}
		desktopMode = ResponsiveUtils.isDesktop();
	}

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
		computeResponsive();
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		super.detach();
	}

	public boolean isLazyLoading() {
		return lazyLoading;
	}

	@Override
	public MenuItem addItem(String caption, Resource icon, Command command) {
		MenuItem rootItem;
		if (lazyLoading) {
			rootItem = super.addItem(caption, icon, new Command() {
				@Override
				public void menuSelected(final MenuItem selectedItem) {
					if (selectedItem.getCommand() != null) {
						final String loadingNotificationId = UUID.randomUUID().toString();
						Notification loadingNotification = new Notification("<div id=\"" + loadingNotificationId + "\">" + $("loading") + "</div>");
						loadingNotification.setHtmlContentAllowed(true);
						loadingNotification.setDelayMsec(100000);
						loadingNotification.show(Page.getCurrent());

						final List<MenuItem> childrenBeforeLazyLoad = new ArrayList<>(selectedItem.getChildren());
						lazyLoadChildren(selectedItem);
						ConstellioUI.getCurrent().runAsync(new Runnable() {
							@Override
							public void run() {
								ConstellioUI.getCurrent().access(new Runnable() {
									@Override
									public void run() {
										for (MenuItem childBeforeLazyLoad : childrenBeforeLazyLoad) {
											selectedItem.removeChild(childBeforeLazyLoad);
										}
										selectedItem.setCommand(null);

										StringBuilder js = new StringBuilder();
										js.append("setTimeout(function(){");
										js.append("document.getElementById('" + getId() + "').firstChild.click();");
										String findVNotificationJS = "document.getElementById('" + loadingNotificationId + "').parentNode.parentNode.parentNode.parentNode";
										js.append(findVNotificationJS + ".parentNode.removeChild(" + findVNotificationJS + ");");
										js.append(" }, 10);");
										JavaScript.eval(js.toString());
									}
								});
							}

						}, 100, BaseMenuBar.this);
					}
				}
			});
			rootItem.addItem($("loading"), FontAwesome.SPINNER, null);
		} else {
			rootItem = super.addItem(caption, icon, command);
		}
		return rootItem;
	}

	private void computeResponsive() {
		boolean switchToMobile;
		boolean switchToDesktop;
		if (desktopMode && !ResponsiveUtils.isDesktop()) {
			switchToMobile = true;
			switchToDesktop = false;
		} else if (!desktopMode && ResponsiveUtils.isDesktop()) {
			switchToMobile = false;
			switchToDesktop = true;
		} else {
			switchToMobile = false;
			switchToDesktop = false;
		}
		if (switchToMobile || switchToDesktop) {
			for (MenuItem item : getItems()) {
				if (switchToMobile) {
					rootItemNonResponsiveCaptions.put(item, item.getText());
					item.setText("");
				} else {
					item.setText(rootItemNonResponsiveCaptions.get(item));
					rootItemNonResponsiveCaptions.put(item, null);
				}
			}
			desktopMode = ResponsiveUtils.isDesktop();
		}
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		computeResponsive();
	}

	protected void lazyLoadChildren(MenuItem rootItem) {
	}

}
