package com.constellio.app.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.model.services.records.RecordServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.records.SystemCheckManagerRuntimeException.SystemCheckManagerRuntimeException_AlreadyRunning;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

public class SystemCheckManager implements StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemCheckManager.class);

	private boolean systemCheckResultsRunning;
	private SystemCheckResults lastSystemCheckResults;

	private AppLayerFactory appLayerFactory;
	private CollectionsListManager collectionsListManager;
	private MetadataSchemasManager schemasManager;
	private SearchServices searchServices;
	private UserServices userServices;
	private RecordServices recordServices;

	static final String CHECKED_REFERENCES_METRIC = "core.checkedReferences";
	static final String BROKEN_REFERENCES_METRIC = "core.brokenReferences";

	public SystemCheckManager(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.collectionsListManager = appLayerFactory.getModelLayerFactory().getCollectionsListManager();
		this.schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public synchronized void startSystemCheck(final boolean repair) {
		lastSystemCheckResults = new SystemCheckResults(TimeProvider.getLocalDateTime());
		if (systemCheckResultsRunning) {
			throw new SystemCheckManagerRuntimeException_AlreadyRunning();
		}

		systemCheckResultsRunning = true;
		new Thread() {
			@Override
			public void run() {
				try {
					runSystemCheck(repair);

				} finally {
					systemCheckResultsRunning = false;
				}
			}
		}.start();

	}

	public SystemCheckResults getLastSystemCheckResults() {
		return lastSystemCheckResults;
	}

	public boolean isSystemCheckResultsRunning() {
		return systemCheckResultsRunning;
	}

	SystemCheckResults runSystemCheck(boolean repair) {
		lastSystemCheckResults = new SystemCheckResults(TimeProvider.getLocalDateTime());
		Map<String, String> ids = findIdsAndTypes();

		Language language = Language.withCode(appLayerFactory.getModelLayerFactory().getConfiguration().getMainDataLanguage());
		SystemCheckResultsBuilder builder = new SystemCheckResultsBuilder(language, appLayerFactory, lastSystemCheckResults);

		for (String collection : collectionsListManager.getCollections()) {
			for (MetadataSchemaType type : schemasManager.getSchemaTypes(collection).getSchemaTypes()) {
				List<Metadata> references = type.getAllMetadatas().onlyWithType(REFERENCE);
				LogicalSearchQuery query = new LogicalSearchQuery(from(type).returnAll());
				Iterator<Record> allRecords = searchServices.recordsIterator(query, 10000);
				while (allRecords.hasNext()) {
					Record record = allRecords.next();
					boolean recordsRepaired = findBrokenLinksInRecord(ids, references, record, repair, builder);
					try {
						recordServices.validateRecord(record);
					} catch(RecordServicesException.ValidationException e) {
						builder.addNewValidationError(e);
					}
					if (recordsRepaired) {

						try {
							Transaction transaction = new Transaction();
							record.markAsModified(Schemas.TITLE);
							transaction.getRecordUpdateOptions().setFullRewrite(true);
							transaction.getRecordUpdateOptions().setUpdateModificationInfos(false);
							transaction.add(record);

							if (transaction.getModifiedRecords().size() >= 1) {

								recordServices.execute(transaction);

								lastSystemCheckResults.repairedRecords.add(record.getId());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			}
		}

		for (String collection : appLayerFactory.getModelLayerFactory().getCollectionsListManager()
				.getCollectionsExcludingSystem()) {
			CollectionSystemCheckParams params = new CollectionSystemCheckParams(collection, builder, repair);
			appLayerFactory.getExtensions().forCollection(collection).checkCollection(params);
		}

		return getLastSystemCheckResults();
	}

	private boolean findBrokenLinksInRecord(Map<String, String> ids, List<Metadata> references,
			Record record, boolean repair, SystemCheckResultsBuilder builder) {

		boolean recordRepaired = false;
		for (Metadata reference : references) {
			if (reference.isMultivalue()) {
				List<String> values = record.getList(reference);
				List<String> modifiedValues = new ArrayList<>();
				for (String value : values) {
					builder.incrementMetric(CHECKED_REFERENCES_METRIC);
					if (!ids.containsKey(value)) {
						builder.addBrokenLink(record.getId(), value, reference);
					} else {
						modifiedValues.add(value);
					}
				}

				if (repair && reference.getDataEntry().getType() == MANUAL && values.size() != modifiedValues.size()) {
					record.set(reference, modifiedValues);
					recordRepaired = true;
				}

			} else {
				String value = record.get(reference);
				if (value != null) {
					builder.incrementMetric(CHECKED_REFERENCES_METRIC);

					if (!ids.containsKey(value)) {
						builder.addBrokenLink(record.getId(), value, reference);

						if (repair && reference.getDataEntry().getType() == MANUAL) {
							String modifiedValue = null;
							//							if (reference.isSameLocalCodeIn(Schemas.CREATED_BY.getLocalCode(),
							//									Schemas.MODIFIED_BY.getLocalCode(), Folder.FORM_CREATED_BY, Folder.FORM_MODIFIED_BY)) {
							//								modifiedValue = userServices.getUserInCollection(User.ADMIN, record.getCollection()).getId();
							//							}
							record.set(reference, modifiedValue);
							recordRepaired = true;
						}

					}
				}

			}
		}

		return recordRepaired;
	}

	private Map<String, String> findIdsAndTypes() {
		Map<String, String> idsAndTypes = new HashMap<>();

		for (String collection : collectionsListManager.getCollections()) {
			LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(collection).returnAll());
			Iterator<Record> allRecords = searchServices.recordsIterator(query, 10000);
			while (allRecords.hasNext()) {
				Record record = allRecords.next();
				String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
				idsAndTypes.put(record.getId(), schemaType);
			}
		}
		return idsAndTypes;

	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}
}
