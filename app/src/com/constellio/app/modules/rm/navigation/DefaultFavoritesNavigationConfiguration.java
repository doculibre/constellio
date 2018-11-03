package com.constellio.app.modules.rm.navigation;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.rm.ui.pages.home.DefaultFavoritesTable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.home.HomeView;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DefaultFavoritesNavigationConfiguration {
	public static void configureNavigation(NavigationConfig config) {
		configureHomeFragments(config);
	}

	private static void configureHomeFragments(NavigationConfig config) {
		config.add(HomeView.TABS, new PageItem.RecordTabSheet($("defaultFavorites")) {
			@Override
			public List<RecordVODataProvider> getDataProviders(AppLayerFactory appLayerFactory,
															   SessionContext sessionContext) {
				return new DefaultFavoritesTable(appLayerFactory, sessionContext).getDataProviders();
			}
		});
	}
}
