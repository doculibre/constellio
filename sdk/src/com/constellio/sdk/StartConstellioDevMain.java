package com.constellio.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordCachesServices;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.sdk.tests.TestPagesComponentsExtensions;

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

		AppLayerFactory factory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();
		new RecordCachesServices(factory.getModelLayerFactory()).loadCachesIn("zeCollection");
		new RecordCachesServices(factory.getModelLayerFactory()).loadCachesIn("anotherCollection");

		User admin = factory.getModelLayerFactory().newUserServices().getUserInCollection("admin", "collection");

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRows(20);
		TaxonomiesSearchServices services = factory.getModelLayerFactory().newTaxonomiesSearchService();
		print(services.getLinkableRootConceptResponse(admin, "collection", "taxoTitulaire", "folder",
				options.setStartRow(0)));

		print(services.getLinkableRootConceptResponse(admin, "collection", "taxoTitulaire", "folder",
				options.setStartRow(20)));

		print(services.getLinkableRootConceptResponse(admin, "collection", "taxoTitulaire", "folder",
				options.setStartRow(40)));

		ApplicationStarter.startApplication(false, getWebContentDir(), 7070);



	}

	private static void print(LinkableTaxonomySearchResponse response) {
		System.out.println("");
		System.out.println("=========================================");
		System.out.println("Size of list : " + response.getRecords().size());
		System.out.println("Numfound : " + response.getNumFound());
		System.out.println("");
		for(TaxonomySearchRecord record : response.getRecords()) {
			System.out.println(record.getRecord().getIdTitle());
		}

	}

}
