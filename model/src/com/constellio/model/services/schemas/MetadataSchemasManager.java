package com.constellio.model.services.schemas;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.NoSuchConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException.MetadataSchemasManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.impacts.SchemaTypesAlterationImpact;
import com.constellio.model.services.schemas.impacts.SchemaTypesAlterationImpactsCalculator;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader1;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader2;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLReader3;
import com.constellio.model.services.schemas.xml.MetadataSchemaXMLWriter3;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.schemas.xml.MetadataSchemaXMLWriter3.FORMAT_ATTRIBUTE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class MetadataSchemasManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<MetadataSchemaTypes>, MetadataSchemaProvider {

	public static final String SCHEMAS_CONFIG_PATH = "/schemas.xml";
	private final DataStoreTypesFactory typesFactory;
	private final TaxonomiesManager taxonomiesManager;
	private final ConfigManager configManager;
	private final CollectionsListManager collectionsListManager;
	List<MetadataSchemasManagerListener> listeners = new ArrayList<MetadataSchemasManagerListener>();
	private OneXMLConfigPerCollectionManager<MetadataSchemaTypes> oneXmlConfigPerCollectionManager;
	private BatchProcessesManager batchProcessesManager;
	private SearchServices searchServices;
	private ModelLayerFactory modelLayerFactory;
	private Delayed<ConstellioModulesManager> modulesManagerDelayed;
	private ConstellioCacheManager cacheManager;

	public MetadataSchemasManager(ModelLayerFactory modelLayerFactory,
								  Delayed<ConstellioModulesManager> modulesManagerDelayed) {
		this.configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
		this.typesFactory = modelLayerFactory.getDataLayerFactory().newTypesFactory();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.modulesManagerDelayed = modulesManagerDelayed;
		this.modelLayerFactory = modelLayerFactory;
		this.cacheManager = modelLayerFactory.getDataLayerFactory().getLocalCacheManager();
	}

	@Override
	public void initialize() {
		oneXmlConfigPerCollectionManager();
	}

	/**
	 * This cache saves a lot of time during test execution, but has not benefit during normal runtime
	 * It is static because it is shared by all tests.
	 * It is disabled by default and enabled in tests
	 */
	public static boolean cacheEnabled = false;
	private static Map<String, MetadataSchemaTypes> typesCache = new HashMap<>();

	OneXMLConfigPerCollectionManager<MetadataSchemaTypes> newOneXMLManager(ConfigManager configManager,
																		   CollectionsListManager collectionsListManager) {
		ConstellioCache cache = cacheManager.getCache(MetadataSchemasManager.class.getName());
		return new OneXMLConfigPerCollectionManager<MetadataSchemaTypes>(configManager,
				collectionsListManager, SCHEMAS_CONFIG_PATH, xmlConfigReader(), this, cache) {

			@Override
			protected MetadataSchemaTypes parse(String collection, XMLConfiguration xmlConfiguration) {
				if (cacheEnabled) {
					if (typesCache.get(collection + xmlConfiguration.getRealHash()) != null) {
						return typesCache.get(collection + xmlConfiguration.getRealHash());
					}
					MetadataSchemaTypes types = super.parse(collection, xmlConfiguration);
					//typesCache.put(collection + xmlConfiguration.getRealHash(), types);
					return types;

				} else {
					return super.parse(collection, xmlConfiguration);
				}
			}
		};
	}

	public static Set<String> getCacheKeys() {
		return typesCache.keySet();
	}

	public static void keepOnlyCacheKeys(Set<String> keys) {
		for (String key : new HashSet<>(typesCache.keySet())) {
			if (!keys.contains(key)) {
				typesCache.remove(key);
			}
		}
	}

	public void createCollectionSchemas(final CollectionInfo collectionInfo) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				new MetadataSchemaXMLWriter3().writeEmptyDocument(collectionInfo, document);
			}
		};
		oneXmlConfigPerCollectionManager.createCollectionFile(collectionInfo.getCode(), createConfigAlteration);
	}

	private XMLConfigReader<MetadataSchemaTypes> xmlConfigReader() {
		return new XMLConfigReader<MetadataSchemaTypes>() {

			@Override
			public MetadataSchemaTypes read(String collection, Document document) {

				Element rootElement = document.getRootElement();
				String formatVersion = rootElement == null ? null : rootElement.getAttributeValue(FORMAT_ATTRIBUTE);
				final CollectionInfo collectionInfo = collectionsListManager.getCollectionInfo(collection);
				MetadataSchemaTypesBuilder typesBuilder;
				if (formatVersion == null) {
					typesBuilder = new MetadataSchemaXMLReader1(getClassProvider())
							.read(collectionInfo, document, typesFactory, modelLayerFactory);

				} else if (MetadataSchemaXMLReader2.FORMAT_VERSION.equals(formatVersion)) {
					typesBuilder = new MetadataSchemaXMLReader2(getClassProvider())
							.read(collectionInfo, document, typesFactory, modelLayerFactory);

				} else if (MetadataSchemaXMLReader3.FORMAT_VERSION.equals(formatVersion)) {
					typesBuilder = new MetadataSchemaXMLReader3(getClassProvider())
							.read(collectionInfo, document, typesFactory, modelLayerFactory);
				} else {
					throw new ImpossibleRuntimeException("Invalid format version '" + formatVersion + "'");
				}

				MetadataSchemaTypes builtTypes = typesBuilder.build(typesFactory, modelLayerFactory);

				return builtTypes;
			}
		};
	}

	public MetadataSchema getSchemaOf(Record record) {
		return getSchemaTypes(record).getSchema(record.getSchemaCode());
	}

	public MetadataSchemaType getSchemaTypeOf(Record record) {
		return getSchemaTypes(record).getSchemaType(record.getTypeCode());
	}

	public List<MetadataSchemaType> getSchemaTypes(CollectionObject collectionObject, List<String> schemaTypeCodes) {
		return getSchemaTypes(collectionObject.getCollection(), schemaTypeCodes);
	}

	public List<MetadataSchemaType> getSchemaTypes(String collection, List<String> schemaTypeCodes) {
		MetadataSchemaTypes allTypes = getSchemaTypes(collection);
		List<MetadataSchemaType> types = new ArrayList<>();
		for (String schemaTypeCode : schemaTypeCodes) {
			types.add(allTypes.getSchemaType(schemaTypeCode));
		}
		return Collections.unmodifiableList(types);
	}

	public MetadataSchemaTypes getSchemaTypes(CollectionObject collectionObject) {
		return getSchemaTypes(collectionObject.getCollection());
	}

	public MetadataSchemaTypes getSchemaTypes(byte collectionId) {
		String collectionCode = collectionsListManager.getCollectionCode(collectionId);
		return getSchemaTypes(collectionCode);
	}

	public MetadataSchemaTypes getSchemaTypes(String collection) {
		MetadataSchemaTypes types = oneXmlConfigPerCollectionManager().get(collection);
		if (types == null) {
			throw new MetadataSchemasManagerRuntimeException_NoSuchCollection(collection);
		}
		return types;
	}

	private OneXMLConfigPerCollectionManager<MetadataSchemaTypes> oneXmlConfigPerCollectionManager() {
		if (oneXmlConfigPerCollectionManager == null) {
			this.oneXmlConfigPerCollectionManager = newOneXMLManager(configManager, collectionsListManager);
		}
		return oneXmlConfigPerCollectionManager;
	}

	public List<MetadataSchemaTypes> getAllCollectionsSchemaTypes() {
		List<MetadataSchemaTypes> types = new ArrayList<>();
		for (String collection : collectionsListManager.getCollections()) {
			types.add(getSchemaTypes(collection));
		}
		return types;
	}

	public MetadataSchemaTypesBuilder modify(String collection) {
		return MetadataSchemaTypesBuilder.modify(getSchemaTypes(collection), getClassProvider());
	}

	private ClassProvider getClassProvider() {
		final DefaultClassProvider defaultClassProvider = new DefaultClassProvider();
		return new ClassProvider() {

			@Override
			public <T> Class<T> loadClass(String name)
					throws ClassNotFoundException {
				try {
					return defaultClassProvider.loadClass(name);

				} catch (Throwable e) {
					return modulesManagerDelayed.get().getModuleClass(name);
				}

			}
		};
	}

	public void modify(String collection, MetadataSchemaTypesAlteration alteration) {

		MetadataSchemaTypesBuilder builder = modify(collection);
		alteration.alter(builder);

		try {
			saveUpdateSchemaTypes(builder);
		} catch (OptimisticLocking optimistickLocking) {
			modify(collection, alteration);
		}

	}

	public void deleteSchemaTypes(final List<MetadataSchemaType> typesToDelete) {

		if (!typesToDelete.isEmpty()) {
			modify(typesToDelete.get(0).getCollection(), new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {

					for (MetadataSchemaType type : typesToDelete) {
						types.deleteSchemaType(type, searchServices);
					}
				}
			});
		}
	}

	public void deleteCustomSchemas(final List<MetadataSchema> schemasToDelete) {

		if (!schemasToDelete.isEmpty()) {
			modify(schemasToDelete.get(0).getCollection(), new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {

					for (MetadataSchema schema : schemasToDelete) {
						types.getSchemaType(schema.getCode().split("_")[0]).deleteSchema(schema, searchServices);
					}
				}
			});
		}
	}

	public MetadataSchemaTypes saveUpdateSchemaTypes(MetadataSchemaTypesBuilder schemaTypesBuilder)
			throws OptimisticLocking {
		MetadataSchemaTypes schemaTypes = schemaTypesBuilder.build(typesFactory, modelLayerFactory);

		Document document = new MetadataSchemaXMLWriter3().write(schemaTypes);
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
			LogicalSearchCondition condition = getBatchProcessCondition(impact, collection);
			if (searchServices.hasResults(condition)) {
				batchProcesses.add(batchProcessesManager
						.addBatchProcessInStandby(condition, impact.getAction(), "reindex.schemasAlteration"));
			}
		}
		return batchProcesses;
	}

	private LogicalSearchCondition getBatchProcessCondition(SchemaTypesAlterationImpact impact, String collection) {
		MetadataSchemaType type = getSchemaTypes(collection).getSchemaType(impact.getSchemaType());
		return from(type).returnAll();
	}

	private List<SchemaTypesAlterationImpact> calculateImpactsOf(MetadataSchemaTypesBuilder schemaTypesBuilder) {
		return new SchemaTypesAlterationImpactsCalculator().calculatePotentialImpacts(schemaTypesBuilder);
	}

	private void saveSchemaTypesDocument(MetadataSchemaTypesBuilder schemaTypesBuilder, Document document)
			throws OptimisticLocking {
		try {
			String collection = schemaTypesBuilder.getCollection();
			oneXmlConfigPerCollectionManager.update(collection, "" + schemaTypesBuilder.getVersion(), document);

		} catch (NoSuchConfiguration e) {
			throw new MetadataSchemasManagerRuntimeException.CannotUpdateDocument(document.toString(), e);

		} catch (OptimisticLockingConfiguration e) {
			throw new MetadataSchemasManagerException.OptimisticLocking(e);

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


	@Override
	public MetadataSchema get(byte collectionId, short typeId, short schemaId) {
		return getSchemaTypes(collectionId).getSchemaType(typeId).getSchema(schemaId);
	}


	@Override
	public MetadataSchemaType get(byte collectionId, short typeId) {
		return getSchemaTypes(collectionId).getSchemaType(typeId);
	}
}
