package com.constellio.data.dao.services.solr.serverFactories;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.ChildAtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.ZookeeperAtomicFileSystem;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.io.stream.CloudSolrStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.params.MapSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class CloudSolrServerFactory extends AbstractSolrServerFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudSolrServerFactory.class);

	private List<AtomicFileSystem> atomicFileSystems = new ArrayList<>();
	private List<SolrClient> solrClients = new ArrayList<>();

	private static final String CONFIGS = "/configs";
	private final String zkHost;
	private static final int defaultTimeout = 6000;

	public CloudSolrServerFactory(String zkHost) {
		super();
		this.zkHost = zkHost;
	}

	@Override
	public synchronized SolrClient newSolrServer(String core) {
		SolrClient solrClient = getSolrClient(core);
		solrClients.add(solrClient);
		return solrClient;
	}

	@Override
	public TupleStream newTupleStream(String core, Map<String, String> props) {
		try {
			return new CloudSolrStream(zkHost, core, new MapSolrParams(props));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void clear() {
		for (AtomicFileSystem atomicFileSystem : atomicFileSystems) {
			atomicFileSystem.close();
		}

		for (SolrClient solrClient : solrClients) {
			try {
				solrClient.close();
			} catch (IOException ioe) {
				LOGGER.error("Error while closing solr client", ioe);
			}
		}
	}

	@Override
	public synchronized AtomicFileSystem getConfigFileSystem(String core) {
		AtomicFileSystem configFileSystem = getAtomicFileSystem(core);
		atomicFileSystems.add(configFileSystem);
		return configFileSystem;
	}

	public synchronized AtomicFileSystem getConfigFileSystem() {
		AtomicFileSystem configFileSystem = getAtomicFileSystem("");
		atomicFileSystems.add(configFileSystem);
		return configFileSystem;
	}

	@Override
	public void reloadSolrServer(String core) {
		try {
			CollectionAdminRequest.Reload reload = CollectionAdminRequest.reloadCollection(core);
			CollectionAdminResponse response = reload.process(getAdminServer());
			if (!response.isSuccess()) {
				throw new RuntimeException("Core is not reloaded " + response.getErrorMessages());
			}
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	SolrClient getSolrClient(String core) {
		CloudSolrClient solrClient = new CloudSolrClient.Builder(singletonList(zkHost), Optional.empty()).build();
		solrClient.setSoTimeout(900_000);
		solrClient.setZkClientTimeout(60_000);
		solrClient.setZkConnectTimeout(60_000);
		solrClient.setDefaultCollection(core);
		return solrClient;
	}

	@Override
	AtomicFileSystem getAtomicFileSystem(String core) {
		String path = CONFIGS + "/" + core;
		if (core.isEmpty()) {
			path = CONFIGS;
		}

		return new ChildAtomicFileSystem(new ZookeeperAtomicFileSystem(zkHost, defaultTimeout), path, false);
	}

}
