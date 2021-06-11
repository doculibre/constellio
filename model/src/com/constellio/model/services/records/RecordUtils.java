package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesCache;
import com.constellio.model.utils.DependencyUtils;
import com.constellio.model.utils.DependencyUtilsParams;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.data.dao.dto.records.RecordDTOMode.SUMMARY;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.entities.schemas.entries.DataEntryType.SEQUENCE;
import static java.util.Arrays.asList;

public class RecordUtils {

	public static final byte KEY_IS_NOT_AN_INT = 0;

	private static Logger LOGGER = LoggerFactory.getLogger(RecordUtils.class);

	private SchemaUtils schemaUtils;

	public RecordUtils() {
		schemaUtils = newSchemaUtils();
	}

	public static void removeMetadataValuesOn(List<Metadata> metadatas, Record record) {
		for (Metadata metadata : metadatas) {
			if (metadata.getDataEntry().getType() == MANUAL || metadata.getDataEntry().getType() == SEQUENCE) {
				record.set(metadata, null);
			}
		}
	}

	public static void migrateMetadataTypeAndMultivalueOn(MetadataList metadatas, Record record) {
		metadatas.stream()
				.filter(metadata -> metadata.getDataEntry().getType() == MANUAL)
				.forEach(metadata -> {
					if (record.getSchemaCode().equals(metadata.getSchemaCode())) {
						Metadata migrationMetadata = metadata.getTypeAndMultivalueMigrationMetadata();
						if (migrationMetadata != null) {
							if (Boolean.TRUE.equals(metadata.isMarkedForMigrationToMultivalue())) {
								record.set(migrationMetadata, Lists.newArrayList((Object) record.get(metadata)));
							} else {
								record.set(migrationMetadata, record.get(metadata));
							}

							record.set(metadata, null);
						}
					}
				});
	}

	public static boolean isZeroPaddingId(String id) {
		if (id != null && id.length() == 11) {
			try {
				return Long.parseLong(id) > 0;
			} catch (Exception e) {
				return false;
			}

		} else {
			return false;
		}
	}

	public static int estimateRecordSize(Record record) {
		if (!(record instanceof RecordImpl)) {
			return 0;
		}
		RecordDTO recordDTO = ((RecordImpl) record).getRecordDTO();

		if (recordDTO == null) {
			return 0;
		}

		return estimateRecordSize(recordDTO.getFields(), recordDTO.getCopyFields());
	}

	public static int estimateRecordSize(Map<String, Object> fields, Map<String, Object> copyFields) {
		long size = 0;

		if (copyFields != null) {
			for (Map.Entry<String, Object> entry : copyFields.entrySet()) {
				if (!EXCLUDED_METADATAS_FROM_RECORD_ESTIMATE.contains(entry.getKey()) && !entry.getKey().startsWith("id_")) {
					size += sizeOf(entry.getValue());
				}
			}

		}
		if (fields != null) {
			for (Map.Entry<String, Object> entry : fields.entrySet()) {
				if (!EXCLUDED_METADATAS_FROM_RECORD_ESTIMATE.contains(entry.getKey()) && !entry.getKey().startsWith("id_")) {
					size += sizeOf(entry.getValue());
				}
			}
		}
		return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
	}

	private static final List<String> EXCLUDED_METADATAS_FROM_RECORD_ESTIMATE = asList(
			Schemas.VERSION.getLocalCode(), Schemas.ESTIMATED_SIZE.getLocalCode(),
			Schemas.MIGRATION_DATA_VERSION.getLocalCode(), Schemas.IDENTIFIER.getLocalCode());

	public static int estimateRecordUpdateSize(Map<String, Object> modifiedFields, Map<String, Object> fields,
											   Map<String, Object> modifiedCopyFields,
											   Map<String, Object> copyFields) {

		int size = 0;

		if (modifiedCopyFields != null) {
			for (Map.Entry<String, Object> entry : modifiedCopyFields.entrySet()) {
				if (!EXCLUDED_METADATAS_FROM_RECORD_ESTIMATE.contains(entry.getKey()) && !entry.getKey().startsWith("id_")) {
					size += sizeOf(entry.getValue());

					Object originalValue = copyFields.get(entry.getKey());
					if (originalValue != null) {
						size -= sizeOf(originalValue);
					}
				}
			}
		}
		if (modifiedFields != null) {
			for (Map.Entry<String, Object> entry : modifiedFields.entrySet()) {
				if (!EXCLUDED_METADATAS_FROM_RECORD_ESTIMATE.contains(entry.getKey()) && !entry.getKey().startsWith("id_")) {
					size += sizeOf(entry.getValue());

					Object originalValue = fields.get(entry.getKey());
					if (originalValue != null) {
						size -= sizeOf(originalValue);
					}
				}
			}
		}
		return size;
	}

	/**
	 * This method is far, very far from being complete!
	 * Since the most important part of the data are strings
	 *
	 * @param object
	 * @return
	 */
	public static long sizeOf(Object object) {

		if (object == null) {
			return 0;

		} else if (object instanceof Integer) {
			return 8;

		} else if (object instanceof Float) {
			return 8;

		} else if (object instanceof Double) {
			return 8;

		} else if (object instanceof String) {
			return 16 + 2 * ((String) object).length();

		} else if (object instanceof List) {
			int size = 4;
			for (Object element : ((List) object)) {
				size += sizeOf(element);
			}
			return size;
		}

		return 0;
	}

	public Set<String> toIdSet(List<Record> records) {
		Set<String> idList = new HashSet<>();

		for (Record record : records) {
			idList.add(record.getId());
		}
		return idList;
	}

	public List<String> toIdList(List<Record> records) {
		List<String> idList = new ArrayList<>();

		for (Record record : records) {
			idList.add(record.getId());
		}
		return idList;
	}

	public static List<String> toWrappedRecordIdsList(List<? extends RecordWrapper> records) {
		List<String> idList = new ArrayList<>();

		for (RecordWrapper record : records) {
			idList.add(record.getId());
		}
		return idList;
	}

	public static Set<String> toWrappedRecordIdsSet(Collection<? extends RecordWrapper> records) {
		Set<String> idList = new HashSet<>();

		for (RecordWrapper record : records) {
			idList.add(record.getId());
		}
		return idList;
	}

	public static Set<String> toRecordIdsSet(List<Record> records) {
		Set<String> idList = new HashSet<>();

		for (Record record : records) {
			idList.add(record.getId());
		}
		return idList;
	}

	public Map<String, List<Record>> splitRecordsBySchemaTypes(List<Record> records) {
		KeyListMap<String, Record> recordsSplittedByTypes = new KeyListMap<>();

		SchemaUtils schemaUtils = new SchemaUtils();
		for (Record record : records) {
			String schemaType = schemaUtils.getSchemaTypeCode(record.getSchemaCode());
			recordsSplittedByTypes.add(schemaType, record);
		}
		return recordsSplittedByTypes.getNestedMap();
	}

	public Map<String, Record> toIdRecordMap(List<Record> records) {
		Map<String, Record> idRecordMap = new HashMap<>();

		for (Record record : records) {
			idRecordMap.put(record.getId(), record);
		}
		return idRecordMap;
	}

	public List<Record> sortRecordsOnDependencies(List<Record> unsortedRecords, MetadataSchemaTypes schemaTypes) {
		schemaUtils = new SchemaUtils();
		List<Record> recordsSortedOnDependencies = new ArrayList<>();

		KeyListMap<String, Record> keyListMap = new KeyListMap<>();

		for (Record record : unsortedRecords) {

			keyListMap.add(schemaUtils.getSchemaTypeCode(record.getSchemaCode()), record);
		}

		List<String> typesSortedByDependency = schemaTypes.getSchemaTypesCodesSortedByDependency();
		for (String schemaTypeCode : typesSortedByDependency) {
			MetadataSchemaType type = schemaTypes.getSchemaType(schemaTypeCode);
			List<Record> records = keyListMap.get(schemaTypeCode);
			if (records != null) {
				recordsSortedOnDependencies.addAll(sortRecordsOfType(type, records));
			}
		}

		return recordsSortedOnDependencies;
	}

	private List<Record> sortRecordsOfType(MetadataSchemaType schemaType, List<Record> unsortedRecords) {

		if (hasRecordDependingOnAnother(schemaType, unsortedRecords)) {

			List<Record> sortedRecords = new ArrayList<>();

			List<Metadata> referenceMetadatas = schemaType.getAllParentReferences();

			Map<String, Set<String>> dependencyMap = new HashMap<>();
			for (Record record : unsortedRecords) {
				String parentDependencyId = record.getNonNullValueIn(referenceMetadatas);
				dependencyMap.put(record.getId(), Collections.singleton(parentDependencyId));
			}
			List<String> sortedIds = new DependencyUtils<String>().sortByDependency(dependencyMap);
			Map<String, Record> idRecordMap = toIdRecordMap(unsortedRecords);
			for (String recordId : sortedIds) {
				sortedRecords.add(idRecordMap.get(recordId));
			}
			return sortedRecords;
		} else {
			Set<String> ids = new HashSet<>();
			List<Record> sortedRecordsById = new ArrayList<>();

			for (int i = unsortedRecords.size() - 1; i >= 0; i--) {
				if (!ids.contains(unsortedRecords.get(i).getId())) {
					ids.add(unsortedRecords.get(i).getId());
					sortedRecordsById.add(unsortedRecords.get(i));
				} else {
					LOGGER.error("Same record added twice in a collection");
				}
			}
			Collections.sort(sortedRecordsById, new Comparator<Record>() {
				@Override
				public int compare(Record o1, Record o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});

			return sortedRecordsById;
		}
	}

	public boolean hasRecordDependingOnAnother(MetadataSchemaType schemaType, List<Record> unsortedRecords) {

		if (unsortedRecords.isEmpty()) {
			return false;
		}

		List<Metadata> metadatas = new ArrayList<>();
		for (MetadataSchema schema : schemaType.getAllSchemas()) {
			for (Metadata metadata : schema.getMetadatas()) {
				if (metadata.getType() == MetadataValueType.REFERENCE && metadata.getAllowedReferences().isAllowed(schemaType)
					&& metadata.getInheritance() == null) {
					metadatas.add(metadata);
				}
			}
		}

		if (metadatas.isEmpty()) {
			return false;
		}

		List<String> ids = toIdList(unsortedRecords);

		for (Record unsortedRecord : unsortedRecords) {
			for (Metadata metadata : metadatas) {
				if (metadata.isMultivalue()) {
					for (String anId : unsortedRecord.<String>getList(metadata)) {
						if (ids.contains(anId)) {
							return true;
						}
					}

				} else {
					if (ids.contains(unsortedRecord.get(metadata))) {
						return true;
					}
				}
			}
		}

		return false;
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}

	public List<RecordDTO> toRecordDTOList(List<Record> records) {

		List<RecordDTO> recordDTOs = new ArrayList<>();

		for (Record record : records) {
			recordDTOs.add(((RecordImpl) record).getRecordDTO());
		}

		return recordDTOs;
	}

	public List<Record> newListWithoutDuplicates(List<Record> records) {
		List<Record> listWithoutDuplicates = new ArrayList<>();
		Set<String> ids = new HashSet<>();

		for (Record record : records) {
			if (!ids.contains(record.getId())) {
				ids.add(record.getId());
				listWithoutDuplicates.add(record);
			}
		}

		return listWithoutDuplicates;
	}

	public List<RecordWrapper> newWrappersListWithoutDuplicates(List<RecordWrapper> recordWrappers) {
		List<RecordWrapper> listWithoutDuplicates = new ArrayList<>();
		Set<String> ids = new HashSet<>();

		for (RecordWrapper recordWrapper : recordWrappers) {
			if (!ids.contains(recordWrapper.getId())) {
				ids.add(recordWrapper.getId());
				listWithoutDuplicates.add(recordWrapper);
			}
		}

		return listWithoutDuplicates;
	}

	public String getRecordsCollection(List<Record> records) {
		String collection = null;

		for (Record record : records) {
			if (collection == null) {
				collection = record.getCollection();
			} else if (collection.equals(record.getCollection())) {
				throw new RuntimeException("Records are in different collections");
			}
		}

		return collection;
	}

	public Record findRecordWithId(List<Record> records, String id) {
		for (Record record : records) {
			if (record.getId().equals(id)) {
				return record;
			}
		}
		return null;
	}

	public static List<Record> unwrap(List<? extends RecordWrapper> recordWrappers) {
		List<Record> records = new ArrayList<>();
		for (RecordWrapper recordWrapper : recordWrappers) {
			records.add(recordWrapper.getWrappedRecord());
		}
		return records;
	}

	public static void copyMetadatas(RecordWrapper source, RecordWrapper destination) {
		copyMetadatas(source.getWrappedRecord(), destination.getWrappedRecord(), source.getMetadataSchemaTypes());
	}

	private static List<String> excludedMetadatas = asList(Schemas.IDENTIFIER.getLocalCode(), Schemas.LEGACY_ID.getLocalCode());

	public static void copyMetadatas(Record source, Record destination, MetadataSchemaTypes types) {
		MetadataSchema sourceRecordSchema = types.getSchema(source.getSchemaCode());
		MetadataSchema destinationRecordSchema = types.getSchema(destination.getSchemaCode());

		for (Metadata sourceMetadata : sourceRecordSchema.getMetadatas()) {

			String sourceMetadataLocalCode = SchemaUtils.getMetadataLocalCodeWithoutPrefix(sourceMetadata);

			for (Metadata destinationMetadata : destinationRecordSchema.getMetadatas()) {
				String destMetadataLocalCode = SchemaUtils.getMetadataLocalCodeWithoutPrefix(destinationMetadata);
				if (sourceMetadataLocalCode.equals(destMetadataLocalCode)) {
					Object value = source.get(sourceMetadata);
					if (destinationMetadata.getDataEntry().getType() == DataEntryType.MANUAL
						&& destinationMetadata.getType() == sourceMetadata.getType()
						&& destinationMetadata.isMultivalue() == sourceMetadata.isMultivalue()
						&& !destinationMetadata.isSystemReserved()
						&& value != null
						&& !excludedMetadatas.contains(destinationMetadata.getLocalCode())) {

						destination.set(destinationMetadata, value);
					}
				}
			}
		}
	}

	public static void changeSchemaTypeAccordingToTypeLinkedSchema(Record record, MetadataSchemaTypes schemaTypes,
																   RecordProvider recordProvider) {
		MetadataSchema recordSchema = schemaTypes.getSchemaOf(record);

		for (Metadata metadata : recordSchema.getMetadatas()) {

			if (schemaTypes.isRecordTypeMetadata(metadata)) {
				changeSchemaTypeAccordingToTypeLinkedSchema(record, schemaTypes, recordProvider, metadata);
			}
		}
	}

	public static void changeSchemaTypeAccordingToTypeLinkedSchema(Record record, MetadataSchemaTypes schemaTypes,
																   RecordProvider recordProvider,
																   Metadata typeMetadata) {
		MetadataSchema recordSchema = schemaTypes.getSchemaOf(record);
		String newSchemaCode = getSchemaAccordingToTypeLinkedSchema(record, schemaTypes, recordProvider, typeMetadata);
		if (!record.getSchemaCode().equals(newSchemaCode)) {
			MetadataSchema newSchema = schemaTypes.getSchema(newSchemaCode);
			record.changeSchema(recordSchema, newSchema);
		}

	}

	public static String getSchemaAccordingToTypeLinkedSchema(Record record, MetadataSchemaTypes schemaTypes,
															  RecordProvider recordProvider, Metadata typeMetadata) {
		MetadataSchema recordSchema = schemaTypes.getSchemaOf(record);
		MetadataSchema referencedSchema = schemaTypes.getDefaultSchema(typeMetadata.getReferencedSchemaTypeCode());
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String typeId = record.get(typeMetadata);
		String customSchema = null;
		if (typeId != null) {

			Record typeRecord = recordProvider.getRecord(typeId);
			customSchema = typeRecord.get(referencedSchema.get("linkedSchema"));
		}

		if (customSchema != null && customSchema.contains("_")) {
			return customSchema;
		}

		return schemaTypeCode + "_" + (customSchema == null ? "default" : customSchema);
	}

	public static String removeZerosInId(String id) {
		int lastZero = -1;
		for (int i = 0; i < id.length(); i++) {
			if (id.charAt(i) == '0') {
				lastZero = i;
			} else {
				break;
			}
		}

		if (lastZero == -1 || lastZero == id.length() - 1) {
			return id;
		} else {
			return id.substring(lastZero + 1);
		}
	}

	public static List<Record> sortRecordByDependency(MetadataSchemaTypes types, List<Record> records) {

		Set<String> ids = new HashSet<>(new RecordUtils().toIdList(records));
		Map<String, Set<String>> dependencies = new HashMap<>();
		Map<String, Record> recordMap = new HashMap<>();

		boolean hasInterdependency = false;

		for (Record record : records) {
			MetadataSchema schema = types.getSchemaOf(record);
			Set<String> dependentIds = new HashSet<>();
			recordMap.put(record.getId(), record);
			for (Metadata metadata : schema.getMetadatas()) {
				if (metadata.getType() == MetadataValueType.REFERENCE) {
					if (metadata.isMultivalue()) {
						List<String> metadataIds = record.getList(metadata);
						dependentIds.addAll(metadataIds);
					} else {
						String metadataId = record.get(metadata);
						if (metadataId != null) {
							dependentIds.add(metadataId);
						}
					}
				}
			}

			for (String dependency : dependentIds) {
				hasInterdependency |= ids.contains(dependency);
			}

			dependencies.put(record.getId(), dependentIds);
		}

		if (hasInterdependency) {
			List<Record> sorted = new ArrayList<>();
			DependencyUtilsParams params = new DependencyUtilsParams().withToleratedCyclicDepencies()
					.sortUsingDefaultComparator();
			for (String recordId : new DependencyUtils<String>().sortByDependency(dependencies, params)) {
				sorted.add(recordMap.get(recordId));
			}
			return sorted;
		} else {
			return records;
		}

	}

	public static List<String> parentPaths(Record record) {
		List<String> paths = record.<String>getList(Schemas.PATH);
		List<String> parentPaths = new ArrayList<>();

		for (String path : paths) {
			String parentPath = StringUtils.substringBeforeLast(path, "/");
			if (!parentPath.isEmpty()) {
				parentPaths.add(parentPath);
			}
		}
		return parentPaths;
	}

	public static void invalidateTaxonomiesCache(List<Record> records, MetadataSchemaTypes types,
												 RecordProvider recordProvider,
												 TaxonomiesSearchServicesCache cache) {

		if (Toggle.NO_TAXONOMIES_CACHE_INVALIDATION.isEnabled()) {
			return;
		}


		Set<String> idsWithPossibleNewChildren = new HashSet<>();
		Set<String> idsWithPossibleRemovedChildren = new HashSet<>();
		for (Record record : records) {

			List<Metadata> metadatas = record.getModifiedMetadataList(types).stream().filter((m) -> {
				return m.isTaxonomyRelationship() || m.isChildOfRelationship();
			}).collect(Collectors.toList());

			for (Metadata metadata : metadatas) {
				for (String newReference : record.<String>getValues(metadata)) {
					idsWithPossibleNewChildren.addAll(getHierarchyIdsTo(newReference, types, recordProvider));
				}
			}

			if (record.isSaved() && !metadatas.isEmpty()) {
				Record originalRecord = record.getCopyOfOriginalRecord();
				for (Metadata metadata : metadatas) {
					for (String removedReference : originalRecord.<String>getValues(metadata)) {
						idsWithPossibleRemovedChildren.addAll(getHierarchyIdsTo(removedReference, types, recordProvider));
					}
				}
			}

			if (record.isModified(Schemas.LOGICALLY_DELETED_STATUS)) {
				if (Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS))) {
					idsWithPossibleRemovedChildren.addAll(getHierarchyIdsTo(record.getId(), types, recordProvider));
				} else {
					idsWithPossibleNewChildren.addAll(getHierarchyIdsTo(record.getId(), types, recordProvider));
				}
			}

			if (record.isModified(Schemas.VISIBLE_IN_TREES)) {
				if (Boolean.FALSE.equals(record.get(Schemas.VISIBLE_IN_TREES))) {
					idsWithPossibleRemovedChildren.addAll(getHierarchyIdsTo(record.getId(), types, recordProvider));
				} else {
					idsWithPossibleNewChildren.addAll(getHierarchyIdsTo(record.getId(), types, recordProvider));
				}
			}

			if (User.SCHEMA_TYPE.equals(record.getTypeCode())) {
				MetadataSchema userSchema = types.getSchema(User.DEFAULT_SCHEMA);

				if (record.isModified(userSchema.getMetadata(User.COLLECTION_READ_ACCESS))
					|| record.isModified(userSchema.getMetadata(User.COLLECTION_WRITE_ACCESS))
					|| record.isModified(userSchema.getMetadata(User.COLLECTION_DELETE_ACCESS))
					|| record.isModified(userSchema.getMetadata(User.GROUPS))) {
					cache.invalidateUser(record.<String>get(userSchema.getMetadata(User.USERNAME)));
				}
			}

			if (Group.SCHEMA_TYPE.equals(record.getTypeCode())) {
				MetadataSchema groupSchema = types.getSchema(Group.DEFAULT_SCHEMA);

				if (record.isModified(groupSchema.getMetadata(Group.PARENT))) {
					cache.invalidateAll();
				}
			}

		}
		for (
				String idWithPossibleNewChildren : idsWithPossibleNewChildren) {
			cache.invalidateWithoutChildren(idWithPossibleNewChildren);
		}
		for (
				String idWithPossibleNewChildren : idsWithPossibleRemovedChildren) {
			cache.invalidateWithChildren(idWithPossibleNewChildren);
		}

	}

	public static Set<String> getHierarchyIdsTo(Record record, ModelLayerFactory modelLayerFactory) {
		List<String> ids = getHierarchyIdsTo(record.getId(),
				modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection()),
				new RecordProvider(modelLayerFactory.newRecordServices()));

		return new HashSet<>(ids);
	}

	private static List<String> getHierarchyIdsTo(String newReference, MetadataSchemaTypes types,
												  RecordProvider recordProvider) {
		List<String> collectedIds = new ArrayList<>();
		return getHierarchyIdsTo(newReference, types, recordProvider, collectedIds);
	}

	private static List<String> getHierarchyIdsTo(String newReference, MetadataSchemaTypes types,
												  RecordProvider recordProvider, List<String> collectedIds) {
		List<String> ids = new ArrayList<>();

		Record record = recordProvider.getRecordSummary(newReference);

		if (record.isSaved()) {
			if (!collectedIds.contains(record.getId())) {
				ids.add(record.getId());
				collectedIds.add(record.getId());
			}

			for (Metadata metadata : types.getSchemaOf(record).getMetadatas()) {
				if (metadata.isTaxonomyRelationship() || metadata.isChildOfRelationship()) {
					for (String aReference : record.<String>getValues(metadata)) {
						if (!ids.contains(aReference)) {
							ids.addAll(getHierarchyIdsTo(aReference, types, recordProvider, collectedIds));
						}
					}
				}
			}
		}

		return ids;
	}

	public static <T> List<T> getNewAndRemovedValues(Record record, Metadata metadata) {

		List<T> values = new ArrayList<>();

		if (record.isSaved()) {

			if (record.isModified(metadata)) {
				Record originalRecord = record.getCopyOfOriginalRecord();
				if (metadata.isMultivalue()) {
					List<T> previousValues = originalRecord.getList(metadata);
					List<T> newValues = record.getList(metadata);

					ListComparisonResults<T> comparisonResults = LangUtils.compare(previousValues, newValues);
					values.addAll(comparisonResults.getNewItems());
					values.addAll(comparisonResults.getRemovedItems());

				} else {
					T previousValue = originalRecord.get(metadata);
					T newValue = record.get(metadata);
					if (previousValue != null) {
						values.add(previousValue);
					}
					if (newValue != null) {
						values.add(newValue);
					}
				}
			}
		} else {
			if (metadata.isMultivalue()) {
				values.addAll(record.<T>getList(metadata));

			} else {
				T newValue = record.get(metadata);
				if (newValue != null) {
					values.add(newValue);
				}
			}
		}

		return values;

	}

	public static Map<String, List<Record>> splitByCollection(List<Record> records) {
		KeyListMap<String, Record> splittedByCollection = new KeyListMap<>();
		for (Record record : records) {
			splittedByCollection.add(record.getCollection(), record);
		}
		return splittedByCollection.getNestedMap();
	}

	public static RecordDTO toPersistedSummaryRecordDTO(Record record, MetadataSchema schema) {

		RecordDTO dto = ((RecordImpl) record).getRecordDTO();

		Map<String, Object> fields = new HashMap<>();

		for (Metadata summaryMetadata : schema.getSummaryMetadatas()) {
			String summaryMetadataDataStoreCode = summaryMetadata.getDataStoreCode();
			fields.put(summaryMetadataDataStoreCode, dto.getFields().get(summaryMetadataDataStoreCode));
		}

		fields.put("collection_s", dto.getFields().get("collection_s"));
		fields.put("schema_s", dto.getFields().get("schema_s"));

		return new SolrRecordDTO(dto.getId(), dto.getVersion(), Collections.unmodifiableMap(fields), SUMMARY);

	}

	public static int toIntKey(Object key) {
		if (key instanceof Integer) {
			return ((Integer) key);
		}

		if (key instanceof Long) {
			return KEY_IS_NOT_AN_INT;
		}

		if (key instanceof String) {
			long value = LangUtils.tryParseLong((String) key, 0);

			if (((String) key).length() == 11 && value < Integer.MAX_VALUE) {
				return (int) value;
			} else {
				return KEY_IS_NOT_AN_INT;
			}
		}

		throw new ImpossibleRuntimeException("Invalid key : " + key);
	}

	public static List<Record> compareRecords(List<Record> records, List<Metadata> comparedMetadatas) {
		Record baseRecord = records.get(0);

		Iterator currentTestedRecordIterator = records.iterator();
		currentTestedRecordIterator.next();

		while (currentTestedRecordIterator.hasNext()) {
			Record currentRecord = (Record) currentTestedRecordIterator.next();
			for (Metadata metadata : comparedMetadatas) {
				if (!metadata.isSystemReserved()) {
					if (!Objects.equals(baseRecord.get(metadata), currentRecord.get(metadata))) {
						return records;
					}
				}
			}
		}

		return Collections.emptyList();
	}

	public static boolean isMetadataValueTypeMigrationSupported(MetadataValueType sourceType,
																MetadataValueType destType) {
		if (sourceType == destType) {
			throw new ImpossibleRuntimeException("Metadata cannot be migrated to the same type. " +
												 "If you want to keep the type the same, set the value to null.");
		}
		if (destType == null) {
			return true;
		}

		switch (sourceType) {
			case STRING:
			case TEXT:
				switch (destType) {
					case TEXT:
					case STRING:
						return true;
				}
			default:
				return false;
		}
	}

	public static boolean isMetadataMultivalueMigrationSupported(boolean sourceMultivalue,
																 Boolean destMutlivalue) {
		boolean destIsMultivalue = Boolean.TRUE.equals(destMutlivalue);

		if (sourceMultivalue && destIsMultivalue) {
			throw new ImpossibleRuntimeException("Metadata cannot be migrated to the same mutlivalue's value. " +
												 "If you want to keep the type the same, set the value to null.");
		}
		if (destMutlivalue == null) {
			return true;
		}
		if (sourceMultivalue) {
			return false;
		} else {
			return destIsMultivalue;
		}
	}
}
