package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.LoadTest;

@LoadTest
public class BigVaultServerLoadTest extends ConstellioTest {

	BigVaultServer vaultServer;

	@Before
	public void setUp()
			throws Exception {
		DataLayerFactory daosFactory = (DataLayerFactory) getDataLayerFactory();
		vaultServer = daosFactory.getRecordsVaultServer();
	}

	@Test
	public void whenDeleting100000EmptyRecordsThenFast()
			throws Exception {

		List<String> ids = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			System.out.println(i);
			List<SolrInputDocument> documents = new ArrayList<>();
			for (int j = 0; j < 1000; j++) {
				String id = "id" + i + "_" + j;
				ids.add(id);
				SolrInputDocument inputDocument = new ConstellioSolrInputDocument();
				inputDocument.setField("id", id);
				inputDocument.setField("text_s", TestUtils.frenchPangram());
				documents.add(inputDocument);
			}
			vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(documents));
		}

		Date start = new Date();
		vaultServer.addAll(new BigVaultServerTransaction(NOW).setDeletedRecords(ids));
		System.out.println(new Date().getTime() - start.getTime() + "ms");

		assertThat(vaultServer.query(query("*:*")).getResults().getNumFound()).isEqualTo(0);
	}

	@Test
	public void whenUpdating100000EmptyRecordsThenFast()
			throws Exception {

		List<String> ids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
			List<SolrInputDocument> documents = new ArrayList<>();
			ids = new ArrayList<>();
			for (int j = 0; j < 10000; j++) {

				String id = "id" + i + "_" + j;
				ids.add(id);
				SolrInputDocument inputDocument = new ConstellioSolrInputDocument();
				inputDocument.setField("id", id);
				inputDocument.setField("type_l", "8");
				documents.add(inputDocument);
			}
			vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(documents));
		}

		List<SolrInputDocument> modifiedDocuments = new ArrayList<>();
		for (String id : ids) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.setField("id", id);
			doc.setField("_version_", 1);
			doc.setField("type_l", newMap("inc", "-2"));
			modifiedDocuments.add(doc);
		}

		Date start = new Date();
		vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(modifiedDocuments));
		System.out.println(new Date().getTime() - start.getTime() + "ms");

		assertThat(vaultServer.query(query("type_l:6")).getResults().getNumFound()).isEqualTo(10000);
	}

	@Test
	public void testOptimisticLockingWithVersion1()
			throws Exception {

		SolrInputDocument solrInputDocument = new ConstellioSolrInputDocument();
		solrInputDocument.setField("id", "chuck");
		//solrInputDocument.setField("text_s", "norris");
		vaultServer.addAll(new BigVaultServerTransaction(NOW).setNewDocuments(asList(solrInputDocument)));

		solrInputDocument = new ConstellioSolrInputDocument();
		solrInputDocument.setField("id", "chuck");
		solrInputDocument.setField("_version_", 1);
		//solrInputDocument.setField("text_s", "norris");
		vaultServer.addAll(new BigVaultServerTransaction(NOW).setUpdatedDocuments(asList(solrInputDocument)));

	}

	private SolrParams query(String q) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", q);
		return params;
	}

	private Map<String, String> newMap(String key, String value) {
		Map<String, String> map = new HashMap<>();
		map.put(key, value);
		return map;
	}
}
