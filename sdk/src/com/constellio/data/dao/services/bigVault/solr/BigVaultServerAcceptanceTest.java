package com.constellio.data.dao.services.bigVault.solr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.sdk.tests.ConstellioTest;

public class BigVaultServerAcceptanceTest extends ConstellioTest {

	@Test
	public void givenSolrServerWhenAskForConfigMangerThenSolrConfigFileCanAccessable() {
		//when
		AtomicFileSystem configManger = getDataLayerFactory().getRecordsVaultServer().getSolrFileSystem();
		//Then
		assertThat(configManger.exists("/solrconfig.xml")).isTrue();
	}
}
