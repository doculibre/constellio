package com.constellio.data.dao.services.solr.serverFactories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.ChildAtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.VersioningAtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.ZookeeperAtomicFileSystem;

public class CloudSolrServerFactory extends AbstractSolrServerFactory {
	private List<AtomicFileSystem> atomicFileSystems = new ArrayList<>();
	private List<SolrClient> solrClients = new ArrayList<>();

	private static final String CONFIGS = "/configs";
	private final String zkHost;
	private static final int defaultTimeout = 10000;

	public CloudSolrServerFactory(String zkHost) {
		super();
		this.zkHost = zkHost;
	}

	@Override
	public synchronized SolrClient newSolrServer(String core) {
		SolrClient solrClient = getSolrClient(core);
		((CloudSolrClient)solrClient).setDefaultCollection(core);
		solrClients.add(solrClient);
		return solrClient;
	}

	@Override
	public synchronized void clear() {
		for (AtomicFileSystem atomicFileSystem : atomicFileSystems) {
			atomicFileSystem.close();
		}

		for (SolrClient solrClient : solrClients)
			solrClient.shutdown();
	}

	@Override
	public synchronized VersioningAtomicFileSystem getConfigFileSystem(String core) {
		VersioningAtomicFileSystem configFileSystem = getAtomicFileSystem(core);
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
			CollectionAdminRequest.Reload reload = new CollectionAdminRequest.Reload();
			reload.setCollectionName(core);
			CollectionAdminResponse response = reload.process(getAdminServer());
			if (!response.isSuccess())
				throw new SolrServerException("Core is not reloaded " + response.getErrorMessages());
		} catch (SolrServerException | RemoteSolrException | IOException e) {
			throw new ServerReloadException(e);
		}
	}

	@Override
	public SolrClient getSolrClient(String core) {
		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		return solrClient;
	}

	@Override
	VersioningAtomicFileSystem getAtomicFileSystem(String core) {
		String path = CONFIGS + "/" + core;
		if (core.isEmpty())
			path = CONFIGS;

		return new VersioningAtomicFileSystem(new ChildAtomicFileSystem(
				new ZookeeperAtomicFileSystem(zkHost, defaultTimeout), path, false));
	}

	@Override
	public boolean isHealthy(String name) {
		try {
			CloudSolrClient adminServer = (CloudSolrClient)getAdminServer();
			List<String> coreNames = getCollectionCoreNames(adminServer, name);
			for (String coreName: coreNames){
				CoreAdminResponse status = CoreAdminRequest.getStatus(coreName, adminServer);

				@SuppressWarnings("unchecked")
				Map<String, Exception> failedCores =(Map<String, Exception>) status.getResponse().get("initFailures");
				if (failedCores != null && failedCores.get(coreName) != null)
					return false;
			}
			return true;
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<String> getCollectionCoreNames(CloudSolrClient server, String name){

	    ZkStateReader reader = server.getZkStateReader();
	    Collection<Slice> slices = reader.getClusterState().getSlices(name);
	    List<String> coreNames = new ArrayList<>();
	    for (Slice slice: slices) {
	        for(Replica replica:slice.getReplicas()) {
	            coreNames.add(replica.getStr("core"));
	        }
	    }
	    return coreNames;
	}

}
