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
package com.constellio.data.dao.services.solr.serverFactories;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.AtomicLocalFileSystem;

public class HttpSolrServerFactory implements SolrServerFactory {

	private final String url;
	private IOServicesFactory ioServicesFactory;
	private SolrClient adminServer;

	public HttpSolrServerFactory(String url, IOServicesFactory ioServicesFactory) {
		super();
		
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		this.url = url;
		this.ioServicesFactory = ioServicesFactory;
		adminServer = newSolrServer("");
	}

	@Override
	public SolrClient newSolrServer(String core) {
		return new HttpSolrClient(url + "/" + core);
	}

	@Override
	public void clear() {
	}

	@Override
	public AtomicFileSystem getConfigFileSystem(String core) {
		try {
			URL urlToSolrServer = new URL(url);
			String host = urlToSolrServer.getHost();
			if (host.equals("localhost") || host.equals("127.0.0.1")){
				File configFolder = getRootFolder(core);
				AtomicFileSystem fileSystem = new AtomicLocalFileSystem(configFolder, ioServicesFactory.newHashingService());
				return fileSystem;
			}
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@SuppressWarnings("unchecked")
	private File getRootFolder(String core) throws SolrServerException, IOException {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set(CommonParams.QT, "/admin/cores");
		QueryResponse response = adminServer.query(params);
		SimpleOrderedMap<SimpleOrderedMap<String>> status = (SimpleOrderedMap<SimpleOrderedMap<String>>) response.getResponse().get("status");
		SimpleOrderedMap<String> coreInfo = status.get(core);
		
		String dataDir = coreInfo.get("dataDir");
		String instanceDir = coreInfo.get("instanceDir");
		String baseDir = dataDir.substring(0, dataDir.indexOf(instanceDir) + instanceDir.length());
		
		return new File(baseDir);
	}

	@Override
	public SolrClient getAdminServer() {
		return adminServer;
	}

	@Override
	public AtomicFileSystem getConfigFileSystem() {
		CoreAdminRequest coreAdminRequest = new CoreAdminRequest();
		coreAdminRequest.setAction(CoreAdminAction.STATUS);
		try {
			CoreAdminResponse process = coreAdminRequest.process(adminServer);
			NamedList<NamedList<Object>> coreStatus = process.getCoreStatus();
			
			File parent = null;
			for (Entry<String, NamedList<Object>> aCoreStatus: coreStatus){
				File coreConfigFld = new File(aCoreStatus.getValue().get("instanceDir").toString());
				File solrFld = coreConfigFld.getParentFile().getParentFile();
				if (parent == null)
					parent = solrFld;
				else if (!parent.equals(solrFld))
					throw new UnsupportedOperationException("TODO ?!");
			}
			
			return new AtomicLocalFileSystem(parent, ioServicesFactory.newHashingService());
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
