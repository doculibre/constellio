package com.constellio.app.modules.rm.util;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.Map;

public class RMNavigationUtils {

	public static void navigateToDisplayFolder(String id, Map<String, String> params,
											   AppLayerFactory appLayerFactory, String collection) {
		Navigation navigation = new Navigation();

		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);
		String containerId = null;
		String favGroupId = null;

		if (params != null) {
			containerId = params.get("containerId");
			favGroupId = params.get(RMViews.FAV_GROUP_ID_KEY);
		}

		if(favGroupId != null) {
			navigation.to(RMViews.class).displayFolderFromFavorites(id, favGroupId);
		} else if (containerId != null) {
			navigation.to(RMViews.class).displayFolderFromContainer(id, containerId);
		} else if (DecommissionNavUtil.areTypeAndSearchIdPresent(params)) {

			navigation.to(RMViews.class).displayFolderFromDecommission(id, DecommissionNavUtil.getHomeUri(appLayerFactory),
					false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToDisplayFolderWhileKeepingTraceOfPreviousView(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).displayFolder(id);
		}
	}

	public static void navigateToEditFolder(String id, Map<String, String> params,
											AppLayerFactory appLayerFactory,
											String collection) {
		boolean areTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		Navigation navigation = new Navigation();

		String favGroupId = null;

		if(params != null) {
			favGroupId = params.get(RMViews.FAV_GROUP_ID_KEY);
		}

		if(favGroupId != null) {
			navigation.to(RMViews.class).editFolderFromFavorites(id, favGroupId);
		} else if (areTypeAndSearchIdPresent) {
			navigation.to(RMViews.class).editFolderFromDecommission(id,
					DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToEditFolderWhileKeepingTraceOfPreviousView(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).editFolder(id);
		}
	}


	public static void navigateToDisplayDocument(String id, Map<String, String> params,
												 AppLayerFactory appLayerFactory, String collection) {
		String containerId = null;

		if (params != null) {
			containerId = params.get("containerId");
		}

		Navigation navigation = new Navigation();
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		String favGroupId = null;

		if(params != null) {
			favGroupId = params.get(RMViews.FAV_GROUP_ID_KEY);
		}

		if(favGroupId != null) {
			navigation.to(RMViews.class).displayDocumentFromFavorites(id, favGroupId);
		} else if (containerId != null) {
			navigation.to(RMViews.class).displayDocumentFromContainer(id, containerId);
		} else if (DecommissionNavUtil.areTypeAndSearchIdPresent(params)) {
			ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());
			navigation.to(RMViews.class).displayDocumentFromDecommission(id, configs.getConstellioUrl(),
					false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToDisplayDocumentWhileKeepingTraceOfPreviousView(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).displayDocument(id);
		}
	}

	public static void navigateToEditDocument(String id, Map<String, String> params,
											  AppLayerFactory appLayerFactory,
											  String collection) {

		Navigation navigation = new Navigation();
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
				.forModule(ConstellioRMModule.ID);

		boolean areTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);

		String favGroupId = null;
		if(params != null) {
			favGroupId = params.get(RMViews.FAV_GROUP_ID_KEY);
		}

		if(favGroupId != null) {
			navigation.to(RMViews.class).editDocumentFromFavorites(id, favGroupId);
		} else if (areTypeAndSearchIdPresent) {
			navigation.to(RMViews.class).editDocumentFromDecommission(id,
					DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (rmModuleExtensions.navigateToEditDocumentWhileKeepingTraceOfPreviousView(new NavigateToFromAPageParams(params, id))) {
		} else {
			navigation.to(RMViews.class).editDocument(id);
		}
	}
}
