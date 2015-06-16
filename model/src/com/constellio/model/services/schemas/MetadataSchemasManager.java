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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.impacts.SchemaTypesAlterationImpact;
import com.constellio.model.services.schemas.impacts.SchemaTypesAlterationImpactsCalculator;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLWriter;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
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
	List<MetadataSchemasManagerListener> listeners = new ArrayList<MetadataSchemasManagerListener>();
	private OneXMLConfigPerCollectionManager<MetadataSchemaTypes> oneXmlConfigPerCollectionManager;
	private BatchProcessesManager batchProcessesManager;
	private SearchServices searchServices;

	public MetadataSchemasManager(ConfigManager configManager, DataStoreTypesFactory typesFactory,
			TaxonomiesManager taxonomiesManager, CollectionsListManager collectionsListManager,
			BatchProcessesManager batchProcessesManager, SearchServices searchServices) {
		this.configManager = configManager;
		this.typesFactory = typesFactory;
		this.taxonomiesManager = taxonomiesManager;
		this.collectionsListManager = collectionsListManager;
		this.batchProcessesManager = batchProcessesManager;
		this.searchServices = searchServices;
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

	public void modify(String collection, MetadataSchemaTypesAlteration alteration) {

		MetadataSchemaTypesBuilder builder = modify(collection);
		alteration.alter(builder);

		try {
			saveUpdateSchemaTypes(builder);
		} catch (OptimistickLocking optimistickLocking) {
			modify(collection, alteration);
		}

	}

	public MetadataSchemaTypes saveUpdateSchemaTypes(MetadataSchemaTypesBuilder schemaTypesBuilder)
			throws OptimistickLocking {
		MetadataSchemaTypes schemaTypes = schemaTypesBuilder.build(typesFactory, taxonomiesManager);

		Document document = new MetadataSchemaXMLWriter().write(schemaTypes);
		List<SchemaTypesAlterationImpact> impacts = calculateImpactsOf(schemaTypesBuilder);
		List<BatchProcess> batchProcesses = prepareBatchProcesses(impacts, schemaTypesBuilder.getCollection());

		try {
			saveSchemaTypesDocument(schemaTypesBuilder, document);
			batchProcessesManager.markAsPending(batchProcesses);
		} catch (Throwable t) {
			batchProcessesManager.cancelStandByBatchProcesses(batchProcesses);
			throw t;
		}

		return schemaTypes;
	}

	private List<BatchProcess> prepareBatchProcesses(List<SchemaTypesAlterationImpact> impacts, String collection) {
		List<BatchProcess> batchProcesses = new ArrayList<>();
		for (SchemaTypesAlterationImpact impact : impacts) {
			List<String> ids = getRecordIds(impact, collection);
			if (!ids.isEmpty()) {
				batchProcesses.add(batchProcessesManager.add(ids, collection, impact.getAction()));
			}
		}
		return batchProcesses;
	}

	private List<String> getRecordIds(SchemaTypesAlterationImpact impact, String collection) {
		MetadataSchemaType type = getSchemaTypes(collection).getSchemaType(impact.getSchemaType());
		return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(from(type).returnAll()));
	}

	private List<SchemaTypesAlterationImpact> calculateImpactsOf(MetadataSchemaTypesBuilder schemaTypesBuilder) {
		return new SchemaTypesAlterationImpactsCalculator().calculatePotentialImpacts(schemaTypesBuilder);
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

	public void registerListener(MetadataSchemasManagerListener metadataSchemasManagerListener) {
		listeners.add(metadataSchemasManagerListener);
	}

	@Override
	public void onValueModified(String collection, MetadataSchemaTypes newValue) {
		xmlConfigReader();

		for (MetadataSchemasManagerListener listener : listeners) {
			listener.onCollectionSchemasModified(collection);
		}
	}

	@Override
	public void close() {

	}

}
