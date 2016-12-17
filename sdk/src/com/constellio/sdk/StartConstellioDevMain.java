package com.constellio.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.records.extractions.RecordPopulateServices;

/**
 * Created by francisbaril on 2015-11-02.
 */
public class StartConstellioDevMain {

	private static File getWebContentDir() {
		File webContent = new FoldersLocator().getAppProjectWebContent();

		assertThat(webContent).exists().isDirectory();

		File webInf = new File(webContent, "WEB-INF");
		assertThat(webInf).exists().isDirectory();
		assertThat(new File(webInf, "web.xml")).exists();
		assertThat(new File(webInf, "sun-jaxws.xml")).exists();

		File cmis11 = new File(webInf, "cmis11");
		assertThat(cmis11).exists().isDirectory();
		assertThat(cmis11.listFiles()).isNotEmpty();
		return webContent;
	}

	public static void main(String argv[]) {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		Toggle.SIMULATE_CONNECTOR_DOWNLOAD_CONTENT.enable();

		AppLayerFactory factory = SDKScriptUtils.startApplicationWithBatchProcessesAndBackgroundThreads();

		ApplicationStarter.startApplication(false, getWebContentDir(), 7070);

		//ReindexingServices reindexingServices = factory.getModelLayerFactory().newReindexingServices();
		//reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

	}

}
