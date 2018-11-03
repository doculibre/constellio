package com.constellio.app.modules.rm.navigation;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.PageItem.CustomItem;
import com.constellio.app.modules.rm.ui.pages.home.DefaultFavoritesTable;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.home.HomeView;
import com.vaadin.ui.Component;

import java.io.Serializable;

import static com.constellio.app.ui.i18n.i18n.$;

public class DefaultFavoritesNavigationConfiguration implements Serializable {
	public static void configureNavigation(NavigationConfig config) {
		configureHomeFragments(config);
	}

	private static void configureHomeFragments(NavigationConfig config) {
		config.add(HomeView.TABS, new CustomItem($("defaultFavorites")) {
			@Override
			public Component buildCustomComponent(ConstellioFactories factories, SessionContext context) {
				return new DefaultFavoritesTable(factories.getAppLayerFactory(), context).builtCustomSheet();
			}
		});
	}
}
