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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.services.solr.serverFactories.HttpSolrServerFactory;
import com.constellio.sdk.tests.ConstellioTest;

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
