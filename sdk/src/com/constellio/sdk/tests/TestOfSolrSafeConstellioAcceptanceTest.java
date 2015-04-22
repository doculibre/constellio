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
package com.constellio.sdk.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.DataWrapper;
import com.constellio.data.io.concurrent.data.StringView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

public class TestOfSolrSafeConstellioAcceptanceTest extends SolrSafeConstellioAcceptanceTest {

	@Test
	@Ignore
	// TODO Majid: Fails on CI server
	public void whenChangingSolrConfiguraitonInTestThenOtherTestsAreNotAffectedFirst()
			throws OptimisticLockingConfiguration {
		String synontyFilePath = "/conf/synonyms.txt";
		String flag = "This content should not be in this file.";
		for (BigVaultServer server : getConstellioFactories().getDataLayerFactory().getSolrServers().getServers()) {
			AtomicFileSystem solrFileSystem = server.getSolrFileSystem();
			DataWithVersion text = solrFileSystem.readData(synontyFilePath);
			DataWrapper<String> dataView = text.getView(new StringView());
			assertThat(dataView.getData()).isNotEqualTo(flag);
			dataView.setData(flag);
			solrFileSystem.writeData(synontyFilePath, text);

		}
	}

	@Test
	@Ignore
	// TODO Majid: Fails on CI server
	public void whenChangingSolrConfiguraitonInTestThenOtherTestsAreNotAffectedSecond()
			throws OptimisticLockingConfiguration {
		String synontyFilePath = "/conf/synonyms.txt";
		String flag = "This content should not be in this file.";
		for (BigVaultServer server : getConstellioFactories().getDataLayerFactory().getSolrServers().getServers()) {
			AtomicFileSystem solrFileSystem = server.getSolrFileSystem();
			DataWithVersion text = solrFileSystem.readData(synontyFilePath);
			DataWrapper<String> dataView = text.getView(new StringView());
			assertThat(dataView.getData()).isNotEqualTo(flag);
			dataView.setData(flag);
			solrFileSystem.writeData(synontyFilePath, text);
		}
	}

}
