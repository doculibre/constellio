package com.constellio.data.dao.services.bigVault.solr;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolrServersTest extends ConstellioTest {

	@Mock DataLayerExtensions extensions;
	@Mock DataLayerConfiguration configurations;
	@Mock SolrClient aCoreFirstSolrServerInstance;
	@Mock SolrClient aCoreSecondSolrServerInstance;
	@Mock SolrClient anotherCoreFirstSolrServerInstance;
	@Mock SolrClient anotherCoreSecondSolrServerInstance;
	@Mock SolrServerFactory solrServerFactory;
	@Mock BigVaultLogger bigVaultLogger;

	private SolrServers solrServers;

	private String aCore = aString();
	private String anOtherCore = aString();

	@Test
	public void givenNoSolrServerInstanciatedWhenGettingSolrServerThenCreateInstanceAndReuseIt() {
		BigVaultServer solrServer = solrServers.getSolrServer(aCore);
		BigVaultServer otherCallSolrServer = solrServers.getSolrServer(aCore);
		assertThat(solrServer.getNestedSolrServer()).isSameAs(aCoreFirstSolrServerInstance);
		assertThat(otherCallSolrServer.getNestedSolrServer()).isSameAs(aCoreFirstSolrServerInstance);

	}

	@Test
	public void givenSolrServerInstanciatedClosedWhenGettingSolrServerThenCreateOtherInstanceAndReuseIt() {
		solrServers.getSolrServer(aCore);
		solrServers.close();

		BigVaultServer solrServer = solrServers.getSolrServer(aCore);
		BigVaultServer otherCallSolrServer = solrServers.getSolrServer(aCore);
		assertThat(solrServer.getNestedSolrServer()).isSameAs(aCoreSecondSolrServerInstance);
		assertThat(otherCallSolrServer.getNestedSolrServer()).isSameAs(aCoreSecondSolrServerInstance);
	}

	@Before
	public void setUp() {
		when(solrServerFactory.newSolrServer(aCore)).thenReturn(aCoreFirstSolrServerInstance).thenReturn(
				aCoreSecondSolrServerInstance);
		when(solrServerFactory.newSolrServer(anOtherCore)).thenReturn(anotherCoreFirstSolrServerInstance).thenReturn(
				anotherCoreSecondSolrServerInstance);

		solrServers = new SolrServers(solrServerFactory, bigVaultLogger, extensions, configurations);
	}

	@Test
	public void whenClosingThenClearSolrFactory() throws IOException {
		solrServers.getSolrServer(aCore);
		solrServers.getSolrServer(anOtherCore);
		verify(aCoreFirstSolrServerInstance, never()).close();
		verify(anotherCoreFirstSolrServerInstance, never()).close();

		solrServers.close();

		verify(solrServerFactory, times(1)).clear();
	}

	@Test
	public void whenGettingSolrServerWithDifferentCoreNamesThenReturnDifferentInstanceForEach() {
		BigVaultServer solrServer = solrServers.getSolrServer(aCore);
		BigVaultServer otherSolrServer = solrServers.getSolrServer(anOtherCore);
		assertThat(solrServer.getNestedSolrServer()).isSameAs(aCoreFirstSolrServerInstance);
		assertThat(otherSolrServer.getNestedSolrServer()).isSameAs(anotherCoreFirstSolrServerInstance);
	}

}
