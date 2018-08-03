package com.constellio.data.dao.services.bigVault.solr.serverFactories;

import com.constellio.data.dao.services.solr.serverFactories.HttpSolrServerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpSolrServerFactoryTest extends ConstellioTest {

	String coreName = "core";
	String anUrl = "http/test/url";
	private HttpSolrServerFactory factory;

	@Before
	public void setUp()
			throws Exception {
		factory = new HttpSolrServerFactory(anUrl, null);
	}

	@Test
	public void whenGettingNewSolrServerThenSolrServerCreatedForCoreUrl() {
		SolrClient returnedSolrServer = factory.newSolrServer(coreName);
		assertEquals(anUrl + "/" + coreName, ((HttpSolrClient) returnedSolrServer).getBaseURL());
	}

}
