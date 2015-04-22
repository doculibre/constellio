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
package com.constellio.model.services.schemas;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.NoSuchConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLWriter;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;

public class MetadataSchemasManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<MetadataSchemaTypes> {

	private static final String SCHEMAS_CONFIG_PATH = "/schemas.xml";
	private final DataStoreTypesFactory typesFactory;
	private final TaxonomiesManager taxonomiesManager;
	private final ConfigManager configManager;
	private final CollectionsListManager collectionsListManager;
	KeyListMap<String, MetadataSchemasManagerListener> listeners = new KeyListMap<String, MetadataSchemasManagerListener>();
	private OneXMLConfigPerCollectionManager<MetadataSchemaTypes> oneXmlConfigPerCollectionManager;

	public MetadataSchemasManager(ConfigManager configManager, DataStoreTypesFactory typesFactory,
			TaxonomiesManager taxonomiesManager, CollectionsListManager collectionsListManager) {
		this.configManager = configManager;
		this.typesFactory = typesFactory;
		this.taxonomiesManager = taxonomiesManager;
		this.collectionsListManager = collectionsListManager;
	}

	@Override
	public void initialize() {
		this.oneXmlConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				SCHEMAS_CONFIG_PATH, xmlConfigReader(), this);
	}

	public void createCollectionSchemas(final String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				new MetadataSchemaXMLWriter().writeEmptyDocument(collection, document);
			}
		};
		oneXmlConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	private XMLConfigReader<MetadataSchemaTypes> xmlConfigReader() {
		return new XMLConfigReader<MetadataSchemaTypes>() {

			@Override
			public MetadataSchemaTypes read(String collection, Document document) {
				MetadataSchemaTypesBuilder typesBuilder = new MetadataSchemaXMLReader().read(collection, document, typesFactory,
						taxonomiesManager);
				return typesBuilder.build(typesFactory, taxonomiesManager);
			}
		};
	}

	public MetadataSchemaTypes getSchemaTypes(String collection) {
		return oneXmlConfigPerCollectionManager.get(collection);
	}

	public List<MetadataSchemaTypes> getAllCollectionsSchemaTypes() {
		List<MetadataSchemaTypes> types = new ArrayList<>();
		for (String collection : collectionsListManager.getCollections()) {
			types.add(getSchemaTypes(collection));
		}
		return types;
	}

	public MetadataSchemaTypesBuilder modify(String collection) {
		return MetadataSchemaTypesBuilder.modify(getSchemaTypes(collection));
	}

	public MetadataSchemaTypes saveUpdateSchemaTypes(MetadataSchemaTypesBuilder schemaTypesBuilder)
			throws OptimistickLocking {
		MetadataSchemaTypes schemaTypes = schemaTypesBuilder.build(typesFactory, taxonomiesManager);

		Document document = new MetadataSchemaXMLWriter().write(schemaTypes);
		saveSchemaTypesDocument(schemaTypesBuilder, document);

		return schemaTypes;
	}

	private void saveSchemaTypesDocument(MetadataSchemaTypesBuilder schemaTypesBuilder, Document document) {
		try {
			String collection = schemaTypesBuilder.getCollection();
			oneXmlConfigPerCollectionManager.update(collection, "" + schemaTypesBuilder.getVersion(), document);
		} catch (OptimisticLockingConfiguration | NoSuchConfiguration e) {
			throw new MetadataSchemasManagerRuntimeException.CannotUpdateDocument(document.toString(), e);
		}
	}

	public void registerListener(String path, ConfigEventListener configEventListener) {
		configManager.registerListener(path, configEventListener);
	}

	public void registerListener(String collection, MetadataSchemasManagerListener metadataSchemasManagerListener) {
		listeners.add(collection, metadataSchemasManagerListener);
	}

	@Override
	public void onValueModified(String collection, MetadataSchemaTypes newValue) {
		xmlConfigReader();

		for (MetadataSchemasManagerListener listener : listeners.get(collection)) {
			listener.onCollectionSchemasModified(collection);
		}
	}

	@Override
	public void close() {

	}
}
