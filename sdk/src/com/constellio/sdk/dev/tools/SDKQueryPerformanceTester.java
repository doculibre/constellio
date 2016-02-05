package com.constellio.sdk.dev.tools;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.utils.scripts.SearchPresenterPerformanceTester;
import com.constellio.sdk.SDKScriptUtils;

/**
 * Created by dakota on 11/15/15.
 */
public class SDKQueryPerformanceTester {

	public static void main(String[] args)
			throws Exception {

		String collection = "zeCollection";
		String username = "admin";
		int nbOfThreads = 1;
		int nbOfExecute = 50;
		String search = "*";
		AppLayerFactory appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();
		//QueryPerformanceTester.runTest(appLayerFactory, collection, username, nbOfThreads, nbOfExecute);
		SearchPresenterPerformanceTester.runTest(appLayerFactory, collection, username, nbOfThreads, nbOfExecute, search);

	}
}
