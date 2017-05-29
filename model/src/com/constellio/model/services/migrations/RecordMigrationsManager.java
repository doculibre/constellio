package com.constellio.model.services.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom2.Document;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.RecordMigrationsManager.SchemaTypesRecordMigration;
import com.constellio.model.services.migrations.RecordMigrationsManagerRuntimeException.RecordMigrationsManagerRuntimeException_ScriptNotRegistered;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;

public class RecordMigrationsManager implements StatefulService,
												OneXMLConfigPerCollectionManagerListener<SchemaTypesRecordMigration> {

	private static final String SCHEMAS_CONFIG_PATH = "/recordMigrations.xml";

	Map<String, Map<String, RecordMigrationScript>> scripts = new HashMap<>();

	SearchServices searchServices;
	MetadataSchemasManager metadataSchemasManager;
	BatchProcessesManager batchProcessesManager;
	OneXMLConfigPerCollectionManager<SchemaTypesRecordMigration> oneXMLConfigPerCollectionManager;
	CollectionsListManager collectionsListManager;
	ConstellioCacheManager cacheManager;

	public RecordMigrationsManager(ModelLayerFactory modelLayerFactory) {
		batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		collectionsListManager = modelLayerFactory.getCollectionsListManager();
		cacheManager = modelLayerFactory.getDataLayerFactory().getSettingsCacheManager();
		searchServices = modelLayerFactory.newSearchServices();
		
		ConstellioCache cache = cacheManager.getCache(getClass().getName());
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(
				modelLayerFactory.getDataLayerFactory().getConfigManager(), modelLayerFactory.getCollectionsListManager(),
				SCHEMAS_CONFIG_PATH, new RecordMigrationReader(), this, new NewSchemaTypesRecordMigrationAlteration(), cache);
	}

	public void initialize() {

	}

	@Override
	public void close() {
	}

	public void markScriptAsFinished(String collection, final String schemaType, final int version) {
		oneXMLConfigPerCollectionManager.updateXML(collection, new SchemaTypesRecordMigrationAlteration(collection) {
			@Override
			protected void alter(SchemaTypesRecordMigration schemaTypesRecordMigration) {

				for (SchemaTypeRecordMigration migration : schemaTypesRecordMigration
						.getSchemaType(schemaType).migrationScripts) {
					if (migration.dataVersion == version) {
						migration.finished = true;
					}
				}

			}
		});
	}

	public synchronized void checkScriptsToFinish() {
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			SchemaTypesRecordMigration schemaTypesRecordMigration = oneXMLConfigPerCollectionManager.get(collection);

			Map<String, RecordMigrationScript> collectionMigrationScripts = scripts.get(collection);
			if (collectionMigrationScripts != null) {
				for (Entry<String, SchemaTypeRecordMigrations> entry : schemaTypesRecordMigration.schemaTypesRecordMigration
						.entrySet()) {
					MetadataSchemaType schemaType = types.getSchemaType(entry.getKey());
					for (SchemaTypeRecordMigration recordMigration : entry.getValue().migrationScripts) {
						if (!recordMigration.finished) {
							boolean allRecordMigrationScriptsRegistered = true;
							List<RecordMigrationScript> recordMigrationScript = new ArrayList<>();
							for (String finishedScript : recordMigration.migrationScripts) {
								RecordMigrationScript script = collectionMigrationScripts.get(finishedScript);
								allRecordMigrationScriptsRegistered &= script != null;
								recordMigrationScript.add(script);
							}

							if (allRecordMigrationScriptsRegistered && !searchServices.hasResults(from(schemaType)
									.where(Schemas.MIGRATION_DATA_VERSION).isLessThan(recordMigration.dataVersion)
									.orWhere(Schemas.MIGRATION_DATA_VERSION).isNull())) {

								for (RecordMigrationScript script : recordMigrationScript) {
									script.afterLastMigratedRecord();
								}
								markScriptAsFinished(collection, schemaType.getCode(), recordMigration.dataVersion);
							}
						}
					}
				}
			}
		}
	}

	public boolean isFinished(String collection, RecordMigrationScript script) {
		SchemaTypesRecordMigration schemaTypesRecordMigration = oneXMLConfigPerCollectionManager.get(collection);
		SchemaTypeRecordMigrations migrations = schemaTypesRecordMigration.getSchemaType(script.getSchemaType());
		for (SchemaTypeRecordMigration aScript : migrations.migrationScripts) {
			if (aScript.migrationScripts.contains(script.getId())) {
				return aScript.finished;
			}
		}
		throw new RecordMigrationsManagerRuntimeException_ScriptNotRegistered(script.getId(), script.getSchemaType(), collection);
	}

	public Set<String> registerReturningTypesWithNewScripts(String collection,
			final List<RecordMigrationScript> recordMigrationScripts, boolean isMasterNode) {
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, new NewSchemaTypesRecordMigrationAlteration());

		Map<String, RecordMigrationScript> collectionMigrationScripts = scripts.get(collection);
		if (collectionMigrationScripts == null) {
			collectionMigrationScripts = new HashMap<>();
			scripts.put(collection, collectionMigrationScripts);
		}

		for (RecordMigrationScript script : recordMigrationScripts) {
			collectionMigrationScripts.put(script.getId(), script);
		}

		final Set<String> schemaTypes = new HashSet<>();

		if (isMasterNode) {
			oneXMLConfigPerCollectionManager.updateXML(collection, new SchemaTypesRecordMigrationAlteration(collection) {
				@Override
				protected void alter(SchemaTypesRecordMigration schemaTypesRecordMigration) {

					Set<String> newScripts = schemaTypesRecordMigration
							.registerNewScriptsReturningTheirTypes(recordMigrationScripts);
					schemaTypes.addAll(newScripts);

				}
			});
		}

		return schemaTypes;
	}

	private static class NewSchemaTypesRecordMigrationAlteration implements DocumentAlteration {

		@Override
		public void alter(Document document) {
			new RecordMigrationWriter().writeEmpty(document);
		}
	}

	private static abstract class SchemaTypesRecordMigrationAlteration implements DocumentAlteration {

		String collection;

		public SchemaTypesRecordMigrationAlteration(String collection) {
			this.collection = collection;
		}

		@Override
		public void alter(Document document) {
			SchemaTypesRecordMigration schemaTypesRecordMigration = new RecordMigrationReader().read(collection, document);
			alter(schemaTypesRecordMigration);
			new RecordMigrationWriter().write(document, schemaTypesRecordMigration);
		}

		protected abstract void alter(SchemaTypesRecordMigration schemaTypesRecordMigration);
	}

	public RequiredRecordMigrations getRecordMigrationsFor(Record record) {
		Map<String, RecordMigrationScript> collectionMigrationScripts = scripts.get(record.getCollection());
		SchemaTypesRecordMigration typesRecordMigration = oneXMLConfigPerCollectionManager.get(record.getCollection());
		if (typesRecordMigration != null && collectionMigrationScripts != null) {
			SchemaTypeRecordMigrations migrations = typesRecordMigration.getSchemaType(record.getTypeCode());
			if (migrations != null) {
				int currentDataVersion = migrations.getCurrentDataVersion();
				int recordDataVersion = (int) record.getDataMigrationVersion();

				if (currentDataVersion == recordDataVersion) {
					return new RequiredRecordMigrations(currentDataVersion, new ArrayList<RecordMigrationScript>());

				} else {
					List<RecordMigrationScript> returnedScripts = new ArrayList<>();

					for (SchemaTypeRecordMigration script : migrations.migrationScripts) {
						if (script.dataVersion > recordDataVersion) {
							for (String migrationScript : script.migrationScripts) {

								returnedScripts.add(collectionMigrationScripts.get(migrationScript));
							}
						}

					}

					return new RequiredRecordMigrations(currentDataVersion, returnedScripts);
				}
			} else {
				return new RequiredRecordMigrations(0, new ArrayList<RecordMigrationScript>());
			}
		} else {
			return new RequiredRecordMigrations(0, new ArrayList<RecordMigrationScript>());
		}
	}

	public long getCurrentDataVersion(String collection, String schemaType) {
		SchemaTypesRecordMigration typesRecordMigration = oneXMLConfigPerCollectionManager.get(collection);
		if (typesRecordMigration != null) {
			SchemaTypeRecordMigrations migrations = typesRecordMigration.getSchemaType(schemaType);
			if (migrations != null) {
				int currentDataVersion = migrations.getCurrentDataVersion();
				return currentDataVersion;
			}
		}
		return 0;
	}

	@Override
	public void onValueModified(String collection, SchemaTypesRecordMigration newValue) {

	}

	public static class SchemaTypesRecordMigration implements Serializable {

		Map<String, SchemaTypeRecordMigrations> schemaTypesRecordMigration = new HashMap<>();

		public SchemaTypesRecordMigration() {
		}

		public SchemaTypesRecordMigration(
				Map<String, SchemaTypeRecordMigrations> schemaTypesRecordMigration) {
			this.schemaTypesRecordMigration = schemaTypesRecordMigration;
		}

		public Set<String> registerNewScriptsReturningTheirTypes(List<RecordMigrationScript> recordMigrationScripts) {

			Set<String> typesWithNewScripts = new HashSet<>();

			KeyListMap<String, RecordMigrationScript> schemaTypes = splitBySchemaType(recordMigrationScripts);
			for (Entry<String, List<RecordMigrationScript>> entry : schemaTypes.getMapEntries()) {
				SchemaTypeRecordMigrations schemaTypeRecordMigration = schemaTypesRecordMigration.get(entry.getKey());

				if (schemaTypeRecordMigration == null) {
					schemaTypeRecordMigration = new SchemaTypeRecordMigrations();
					schemaTypesRecordMigration.put(entry.getKey(), schemaTypeRecordMigration);
				}

				List<String> newScripts = schemaTypeRecordMigration.toIdsOfNewScripts(entry.getValue());
				if (!newScripts.isEmpty()) {
					schemaTypeRecordMigration.register(newScripts);
					typesWithNewScripts.add(entry.getKey());
				}

			}

			return typesWithNewScripts;
		}

		private KeyListMap<String, RecordMigrationScript> splitBySchemaType(List<RecordMigrationScript> recordMigrationScripts) {
			KeyListMap<String, RecordMigrationScript> scriptKeyListMap = new KeyListMap<>();

			for (RecordMigrationScript script : recordMigrationScripts) {
				scriptKeyListMap.add(script.getSchemaType(), script);
			}

			return scriptKeyListMap;
		}

		public SchemaTypeRecordMigrations getSchemaType(String schemaType) {
			SchemaTypeRecordMigrations schemaTypeRecordMigrations = schemaTypesRecordMigration.get(schemaType);

			if (schemaTypeRecordMigrations == null) {
				return new SchemaTypeRecordMigrations();
			} else {
				return schemaTypeRecordMigrations;
			}
		}
	}

	public static class SchemaTypeRecordMigrations implements Serializable {

		Set<String> allMigrationScripts = new HashSet<>();
		List<SchemaTypeRecordMigration> migrationScripts = new ArrayList<>();

		public SchemaTypeRecordMigrations() {
		}

		public SchemaTypeRecordMigrations(List<SchemaTypeRecordMigration> migrationScripts) {
			this.allMigrationScripts = new HashSet<>();
			this.migrationScripts = migrationScripts;
			for (SchemaTypeRecordMigration migrationScript : migrationScripts) {
				this.allMigrationScripts.addAll(migrationScript.migrationScripts);
			}
		}

		public List<String> toIdsOfNewScripts(List<RecordMigrationScript> scripts) {

			List<String> ids = new ArrayList<>();
			for (RecordMigrationScript script : scripts) {
				String id = script.getId();
				if (!allMigrationScripts.contains(id)) {
					ids.add(id);
				}
			}

			return ids;
		}

		public void register(List<String> scripts) {
			allMigrationScripts.addAll(scripts);

			int nextDataVersion = getCurrentDataVersion() + 1;
			migrationScripts.add(new SchemaTypeRecordMigration(nextDataVersion, scripts, false));
		}

		public int getCurrentDataVersion() {
			if (migrationScripts.isEmpty()) {
				return 0;
			} else {
				return migrationScripts.get(migrationScripts.size() - 1).dataVersion;
			}
		}
	}

	public static class SchemaTypeRecordMigration implements Serializable {
		int dataVersion;
		List<String> migrationScripts;
		boolean finished;

		public SchemaTypeRecordMigration(int dataVersion, List<String> migrationScripts, boolean finished) {
			this.dataVersion = dataVersion;
			this.migrationScripts = migrationScripts;
			this.finished = finished;
		}
	}

}
