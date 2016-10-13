package com.constellio.data.dao.services.solr;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class QueuedSolrClientAcceptanceTest extends ConstellioTest {

	static Map<String, Object> incBy1 = new HashMap<>();

	static {
		incBy1.put("inc", "1.0");
	}

	@Test
	public void whenPushingLotsOfDocumentsUsingMultipleThreadsThenAllSaved()
			throws Exception {

		Map<String, Object> map = new HashMap<>();
		map.put("inc", "1.0");

		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		QueuedSolrClient client = QueuedSolrClient.createAndStart(solrClient, 100, 5);

		for (int i = 1; i <= 10000; i++) {
			System.out.println("Adding - " + i);
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", "doc" + i);
			solrInputDocument.setField("value_s", "test" + i);
			solrInputDocument.setField("count_d", "0.0");
			client.addAsync(solrInputDocument);
		}

		for (int i = 1; i <= 10000; i++) {
			System.out.println("Updating - " + i);
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", "doc" + i);
			solrInputDocument.setField("_version_", "1");
			solrInputDocument.setField("count_d", incBy1);
			client.addAsync(solrInputDocument);

			solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", "doc" + i);
			solrInputDocument.setField("_version_", "1");
			solrInputDocument.setField("count_d", incBy1);
			client.addAsync(solrInputDocument);
		}

		client.close();

		for (int i = 1; i <= 10000; i++) {
			System.out.println("Validating - " + i);
			SolrDocument solrDocument = solrClient.query(byId("doc" + i)).getResults().get(0);
			assertThat(solrDocument.getFieldValue("count_d")).isEqualTo(2.0);
			assertThat(solrDocument.getFieldValue("value_s")).isEqualTo("test" + i);
		}

	}

	private SolrParams byId(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);
		return params;
	}
}
