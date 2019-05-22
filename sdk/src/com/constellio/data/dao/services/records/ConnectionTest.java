package com.constellio.data.dao.services.records;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.atomicSet;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionTest {

	private CloudSolrClient server, otherServer;

	/**
	 * -- NOT RUN BY INTEGRATION SERVER --
	 * This test is for troubleshooting purposes only
	 */
	public void test()
			throws IOException, SolrServerException {

		server = new CloudSolrClient("192.168.1.100:2381");
		server.setDefaultCollection("records");

		otherServer = new CloudSolrClient("192.168.1.100:2381");
		otherServer.setDefaultCollection("records");

		server.deleteByQuery("*:*");
		server.commit();
		assertThat(query("id:test1")).isEmpty();

		SolrInputDocument doc1 = new SolrInputDocument();
		doc1.setField("id", "test1");
		doc1.setField("title_s", "title1");
		doc1.setField("_version_", "-1");
		doc1.setField("keywords_ss", Arrays.asList("keyword1", "keyword2"));
		server.add(doc1);
		SolrInputDocument doc2 = new SolrInputDocument();
		doc2.setField("id", "test2");
		doc2.setField("title_s", "title2");
		doc2.setField("_version_", "-1");
		doc2.setField("keywords_ss", Arrays.asList("keyword1", "keyword2"));
		server.add(doc2);
		server.commit();

		List<SolrDocument> docs = query("id:test*");

		assertThat(docs).hasSize(2);
		assertThat(getDoc1().getFieldValue("id")).isEqualTo("test1");
		assertThat(getDoc1().getFieldValue("title_s")).isEqualTo("title1");
		assertThat(getDoc1().getFieldValue("keywords_ss")).isEqualTo(Arrays.asList("keyword1", "keyword2"));
		assertThat((Long) getDoc1().getFieldValue("_version_")).isGreaterThan(1);
		Long version = (Long) getDoc1().getFieldValue("_version_");

		SolrInputDocument updateDocument1v = new SolrInputDocument();
		updateDocument1v.setField("id", "test1");
		updateDocument1v.setField("bob_s", atomicSet("z"));
		updateDocument1v.setField("_version_", version);

		SolrInputDocument updateDocument2v = new SolrInputDocument();
		updateDocument2v.setField("id", "test2");
		updateDocument2v.setField("bob_s", atomicSet("z"));
		updateDocument2v.setField("_version_", 42);

		SolrInputDocument updateDocument1 = new SolrInputDocument();
		updateDocument1.setField("id", "test1");
		updateDocument1.setField("title_s", atomicSet("theNewTitle"));

		SolrInputDocument updateDocument2 = new SolrInputDocument();
		updateDocument2.setField("id", "test2");
		updateDocument2.setField("title_s", atomicSet("theNewTitle"));

		tryAdd(Arrays.asList(updateDocument1v, updateDocument2v), Arrays.asList(updateDocument1, updateDocument2));
		//server.softCommit();
		otherServer.commit();
		server.rollback();
		server.commit();
		assertThat(getDoc1().getFieldValue("title_s")).isEqualTo("title1");
		assertThat(getDoc2().getFieldValue("title_s")).isEqualTo("title2");

	}

	private SolrDocument getDoc1()
			throws SolrServerException, IOException {
		return query("id:test1").get(0);
	}

	private SolrDocument getDoc2()
			throws SolrServerException, IOException {
		return query("id:test2").get(0);
	}

	private void tryAdd(List<SolrInputDocument> optimisticLockingValidations, List<SolrInputDocument> changes) {
		try {
			server.add(optimisticLockingValidations);
			server.add(changes);
		} catch (Exception e) {

		}
	}

	private List<SolrDocument> query(String q)
			throws SolrServerException, IOException {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("q", q);
		return server.query(solrParams).getResults();
	}

}
