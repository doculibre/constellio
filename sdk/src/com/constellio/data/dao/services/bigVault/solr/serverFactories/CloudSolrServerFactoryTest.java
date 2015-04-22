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
package com.constellio.data.dao.services.bigVault.solr.serverFactories;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.services.solr.serverFactories.CloudSolrServerFactory;
import com.constellio.sdk.tests.ConstellioTest;

public class CloudSolrServerFactoryTest extends ConstellioTest {

	String coreName = "core";
	String zkBase = "1.2.3.4:5";
	private CloudSolrServerFactory factory;

	@Before
	public void setUp()
			throws Exception {
		factory = new CloudSolrServerFactory(zkBase);
	}

	@After
	public void tearDown()
			throws Exception {
		factory.clear();
	}

	@Test
	public void whenGettingNewSolrServerThenSolrServerCreatedForCoreUrl() {
		CloudSolrClient returnedSolrServer = (CloudSolrClient) factory.newSolrServer(coreName);

		assertEquals(coreName, returnedSolrServer.getDefaultCollection());

		returnedSolrServer.shutdown();
	}

}
