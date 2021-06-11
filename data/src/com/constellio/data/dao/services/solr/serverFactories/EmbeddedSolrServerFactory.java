package com.constellio.data.dao.services.solr.serverFactories;

import com.constellio.data.dao.services.solr.FileSystemSolrManagerException;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.io.SolrClientCache;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.core.CoreContainer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class EmbeddedSolrServerFactory implements SolrServerFactory {

	private static final String DEFAULT_CORE = "default";

	File structureFolder;

	CoreContainer coreContainer;

	public EmbeddedSolrServerFactory(File structureFolder) {
		super();
		this.structureFolder = structureFolder;
	}

	private static void validateStructure(File structureFolder) {
		if (!structureFolder.exists()) {
			throw FileSystemSolrManagerException.noSuchFolder(structureFolder);
		}

		File defaultCore = new File(structureFolder, DEFAULT_CORE);
		validateCoreFolder(defaultCore);
	}

	private static void validateCoreFolder(File coreFolder) {
		if (!coreFolder.exists()) {
			throw FileSystemSolrManagerException.noSuchFolder(coreFolder);
		}

		File confFolder = new File(coreFolder, "conf");

		File solrConfigFile = new File(confFolder, "solrconfig.xml");
		if (!solrConfigFile.exists()) {
			throw FileSystemSolrManagerException.noSuchSolrConfig(confFolder);
		}

		File schemaFile = new File(confFolder, "managed-schema");
		if (!schemaFile.exists()) {
			throw FileSystemSolrManagerException.noSuchSchema(confFolder);
		}
	}

	@Override
	public SolrClient newSolrServer(String coreName) {
		try {
			CoreContainer loadedCoreContainer = getLoadedCoreContainer();
			return new EmbeddedSolrServer(loadedCoreContainer, coreName);

		} catch (IOException e) {
			throw new EmbeddedSolrServerFactoryRuntimeException.CannotCreateSolrServer(e);
		}
	}

	@Override
	public TupleStream newTupleStream(String core, Map<String, String> props) {
		throw new UnsupportedOperationException("unsupported");
	}

	private CoreContainer getLoadedCoreContainer()
			throws IOException {
		synchronized (this) {
			if (coreContainer == null) {

				validateStructure(structureFolder);

				coreContainer = new CoreContainer(structureFolder.getAbsolutePath());
				coreContainer.load();
			}
		}
		return coreContainer;
	}

	@Override
	public void clear() {
		if (coreContainer != null) {
			coreContainer.shutdown();
			coreContainer = null;
		}
	}

	@Override
	public SolrClientCache getSolrClientCache() {
		throw new UnsupportedOperationException("Unsupported");
	}

	@Override
	public AtomicFileSystem getConfigFileSystem(String core) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public AtomicFileSystem getConfigFileSystem() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void reloadSolrServer(String core) {
		throw new UnsupportedOperationException("TODO");
	}
}
