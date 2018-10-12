package com.constellio.app.modules.rm.util;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.Map;

public class RMNavUtil {
	public static void navigateToDisplayFolderAreTypeAndSearchIdPresent(String id, Map<String, String> params,
			AppLayerFactory appLayerFactory, String collection) {
		Navigation navigation = new Navigation();

		boolean areSearchTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);

		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		if (areSearchTypeAndSearchIdPresent) {
			navigation.to(RMViews.class)
					.displayFolderFromDecommission(id, DecommissionNavUtil.getHomeUri(appLayerFactory),
							false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToDisplayFolderFromAPage(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).displayFolder(id);
		}
	}

	public static void navigateToDisplayFolderAreSearchTypeSearchIdOrContainerPresent(String id, Map<String, String> params,
			AppLayerFactory appLayerFactory, String collection) {
		Navigation navigation = new Navigation();

		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);
		String containerId = params.get("containerId");

		if (containerId != null) {
			navigation.to(RMViews.class).displayFolderFromContainer(id, containerId);
		} else if (DecommissionNavUtil.areTypeAndSearchIdPresent(params)) {

			navigation.to(RMViews.class).displayFolderFromDecommission(id, DecommissionNavUtil.getHomeUri(appLayerFactory),
					false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToDisplayFolderFromAPage(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).displayFolder(id);
		}
	}

	public static void navigateToDisplayDocumentAreTypeAndSearchIdPresent(String id, Map<String, String> params,
			AppLayerFactory appLayerFactory, String collection) {
		Navigation navigation = new Navigation();

		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		boolean areSearchTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);

		if (areSearchTypeAndSearchIdPresent) {
			navigation.to(RMViews.class)
					.displayDocumentFromDecommission(id, DecommissionNavUtil.getHomeUri(appLayerFactory),
							false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToDisplayDocumentFromAPage(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).displayDocument(id);
		}
	}

	public static void navigateToDisplayDocumentAreSearchTypeAndSearchIdOrContainerPresent(String id, Map<String, String> params,
			AppLayerFactory appLayerFactory, String collection) {
		String containerId = params.get("containerId");

		Navigation navigation = new Navigation();
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		if (containerId != null) {
			navigation.to(RMViews.class).displayDocumentFromContainer(id, containerId);
		} else if (DecommissionNavUtil.areTypeAndSearchIdPresent(params)) {
			ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());
			navigation.to(RMViews.class).displayDocumentFromDecommission(id, configs.getConstellioUrl(),
					false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToDisplayDocumentFromAPage(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).displayDocument(id);
		}
	}

	public static void navigateToEditDocumentAreTypeAndSearchPresent(String id, Map<String, String> params,
			AppLayerFactory appLayerFactory, String collection) {

		Navigation navigation = new Navigation();
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		boolean areTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);

		if (areTypeAndSearchIdPresent) {
			navigation.to(RMViews.class).editDocumentFromDecommission(id,
					DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToEditDocumentFromAPage(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).editDocument(id);
		}
	}
}
