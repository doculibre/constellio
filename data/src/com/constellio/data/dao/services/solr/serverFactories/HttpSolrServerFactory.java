package com.constellio.data.dao.services.solr.serverFactories;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.AtomicLocalFileSystem;
import com.constellio.data.io.concurrent.filesystem.ChildAtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.VersioningAtomicFileSystem;

public class HttpSolrServerFactory extends AbstractSolrServerFactory {
	private List<AtomicFileSystem> atomicFileSystems = new ArrayList<>();
	private List<SolrClient> solrClients = new ArrayList<>();
	private final String url;
	private IOServicesFactory ioServicesFactory;

	public HttpSolrServerFactory(String url, IOServicesFactory ioServicesFactory) {
		super();

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		this.url = url;
		this.ioServicesFactory = ioServicesFactory;
	}

	@Override
	public synchronized SolrClient newSolrServer(String core) {
		SolrClient solrClient = getSolrClient(core);
		solrClients.add(solrClient);
		return solrClient;
	}

	@Override
	public synchronized void clear() {
		for (AtomicFileSystem atomicFileSystem: atomicFileSystems)
			atomicFileSystem.close();
		for (SolrClient solrClient: solrClients)
			solrClient.shutdown();
	}

	@Override
	public synchronized VersioningAtomicFileSystem getConfigFileSystem(String core) {
		try {
			URL urlToSolrServer = new URL(url);
			String host = urlToSolrServer.getHost();
			if (host.equals("localhost") || host.equals("127.0.0.1")){
				VersioningAtomicFileSystem fileSystem = getAtomicFileSystem(core);
				atomicFileSystems.add(fileSystem);
				return fileSystem;
			}
			throw new UnsupportedOperationException("Not implemented yet");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private String getRootFolder(String core) throws SolrServerException, IOException {
		if (core.isEmpty()){
			return getSolrServerLocation().getAbsolutePath();
		} else {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.QT, "/admin/cores");
			QueryResponse response;
			try {
				response = getAdminServer().query(params);
			} catch (RemoteSolrException e) {	
				//This is a bug in Solr that sometime throw an exception if send a request to the server every time.
				//https://issues.apache.org/jira/browse/SOLR-7785
				try {
					Thread.sleep(1000);
					response = getAdminServer().query(params);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			}
			SimpleOrderedMap<SimpleOrderedMap<String>> status = (SimpleOrderedMap<SimpleOrderedMap<String>>) response.getResponse().get("status");
			SimpleOrderedMap<String> coreInfo = status.get(core);

			String instanceDir = coreInfo.get("instanceDir");
			String baseDir = instanceDir + "conf";
			return baseDir;
		}

	}

	@Override
	public synchronized AtomicFileSystem getConfigFileSystem() {
		AtomicFileSystem atomicFileSystem = getAtomicFileSystem("");
		atomicFileSystems.add(atomicFileSystem);
		return atomicFileSystem;
	}

	private File getSolrServerLocation()
			throws SolrServerException, IOException {
		CoreAdminRequest coreAdminRequest = new CoreAdminRequest();
		coreAdminRequest.setAction(CoreAdminAction.STATUS);
		CoreAdminResponse process = coreAdminRequest.process(getAdminServer());
		NamedList<NamedList<Object>> coreStatus = process.getCoreStatus();

		File parent = null;
		for (Entry<String, NamedList<Object>> aCoreStatus: coreStatus){
			File coreConfigFld = new File(aCoreStatus.getValue().get("instanceDir").toString());
			File solrFld = coreConfigFld.getParentFile();
			if (parent == null)
				parent = solrFld;
			else if (!parent.equals(solrFld))
				throw new UnsupportedOperationException("TODO ?!");
		}
		return parent;
	}

	@Override
	protected VersioningAtomicFileSystem getAtomicFileSystem(String core) {
		try {
			return new VersioningAtomicFileSystem(new ChildAtomicFileSystem(
					new AtomicLocalFileSystem(ioServicesFactory.newHashingService()), getRootFolder(core)));
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void reloadSolrServer(String core) {
		try {
			CoreAdminRequest.reloadCore(core, getAdminServer());
		} catch (SolrServerException | RemoteSolrException | IOException e) {
			throw new ServerReloadException(e);
		}
	}

	@Override
	protected SolrClient getSolrClient(String core) {
		return new HttpSolrClient(url + "/" + core);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isHealthy(String name) {
		CoreAdminResponse status;
		try {
			status = CoreAdminRequest.getStatus(name, getAdminServer());
			Map<String, Exception> failedCores =
					 (Map<String, Exception>) status.getResponse().get("initFailures");
			return failedCores == null || failedCores.get(name) == null;
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
