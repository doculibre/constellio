/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.dao.services.bigVault.solr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.solr.client.solrj.SolrClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.sdk.tests.ConstellioTest;

public class SolrServersTest extends ConstellioTest {

	@Mock DataLayerExtensions extensions;
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

		solrServers = new SolrServers(solrServerFactory, bigVaultLogger, extensions);
	}

	@Test
	public void whenClosingThenShutdownAllSolrServers() {
		solrServers.getSolrServer(aCore);
		solrServers.getSolrServer(anOtherCore);
		verify(aCoreFirstSolrServerInstance, never()).shutdown();
		verify(anotherCoreFirstSolrServerInstance, never()).shutdown();

		solrServers.close();

		verify(aCoreFirstSolrServerInstance, times(1)).shutdown();
		verify(anotherCoreFirstSolrServerInstance, times(1)).shutdown();
	}

	@Test
	public void whenGettingSolrServerWithDifferentCoreNamesThenReturnDifferentInstanceForEach() {
		BigVaultServer solrServer = solrServers.getSolrServer(aCore);
		BigVaultServer otherSolrServer = solrServers.getSolrServer(anOtherCore);
		assertThat(solrServer.getNestedSolrServer()).isSameAs(aCoreFirstSolrServerInstance);
		assertThat(otherSolrServer.getNestedSolrServer()).isSameAs(anotherCoreFirstSolrServerInstance);
	}

}
