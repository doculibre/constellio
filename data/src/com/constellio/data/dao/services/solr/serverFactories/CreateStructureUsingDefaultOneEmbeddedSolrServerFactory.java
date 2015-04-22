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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import com.constellio.data.dao.services.solr.FileSystemSolrManagerException;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.services.facades.FileService;

public class CreateStructureUsingDefaultOneEmbeddedSolrServerFactory implements SolrServerFactory {

	private static final String DEFAULT_CORE = "default";

	File structureFolder;

	File defaultStructure;

	FileService fileService;

	CoreContainer coreContainer;

	public CreateStructureUsingDefaultOneEmbeddedSolrServerFactory(FileService fileService, File structureFolder,
			File defaultStructure) {
		super();
		this.fileService = fileService;
		this.structureFolder = structureFolder;
		this.defaultStructure = defaultStructure;
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

		File schemaFile = new File(confFolder, "schema.xml");
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
			throw new CreateStructureUsingDefaultOneEmbeddedSolrServerFactoryRuntimeException.CannotCreateSolrServer(e);
		}
	}

	private CoreContainer getLoadedCoreContainer()
			throws IOException {
		synchronized (this) {
			if (coreContainer == null) {

				validateStructure(defaultStructure);

				fileService.copyDirectory(defaultStructure, structureFolder);

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
	public AtomicFileSystem getConfigFileSystem(String core) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SolrClient getAdminServer() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public AtomicFileSystem getConfigFileSystem() {
		throw new UnsupportedOperationException("TODO");
	}

}
