package com.constellio.model.services.records;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.utils.DependencyUtils;
import com.constellio.model.utils.DependencyUtilsParams;

public class RecordUtils {

	private SchemaUtils schemaUtils;

	public RecordUtils() {
		schemaUtils = newSchemaUtils();
	}

	public static long estimateRecordSize(Record record) {
		if (record == null) {
			return 0;
		}
		RecordDTO recordDTO = ((RecordImpl) record).getRecordDTO();

		long size = 0;

		if (recordDTO != null) {
			if (recordDTO.getCopyFields() != null) {
				for (Object object : recordDTO.getCopyFields().values()) {
					size += sizeOf(object);
				}

			}
			if (recordDTO.getFields() != null) {
				for (Object object : recordDTO.getFields().values()) {
					size += sizeOf(object);
				}
			}
		}
		return size;
	}

	/**
	 * This method is far, very far from being complete!
	 * Since the most important part of the data are strings
	 * @param object
	 * @return
	 */
	private static long sizeOf(Object object) {

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

	public List<String> toWrappedRecordIdsList(List<? extends RecordWrapper> records) {
		List<String> idList = new ArrayList<>();

		for (RecordWrapper record : records) {
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

		List<String> typesSortedByDependency = schemaTypes.getSchemaTypesSortedByDependency();
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
				//for (int i = 0; i < unsortedRecords.size(); i++) {
				if (!ids.contains(unsortedRecords.get(i).getId())) {
					ids.add(unsortedRecords.get(i).getId());
					sortedRecordsById.add(unsortedRecords.get(i));
				} else {
					System.out.println("Same record added twice in a collection");
				}
			}
			//
			Collections.sort(sortedRecordsById, new Comparator<Record>() {
				@Override
				public int compare(Record o1, Record o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});
			//
			//			Set<String> ids = new HashSet<>();
			//			Iterator<Record> recordIterator = sortedRecordsById.iterator();
			//			while(recordIterator.hasNext()) {
			//				Record record = recordIterator.next();
			//				if (ids.contains(record.getId())) {
			//					recordIterator.remove();
			//				} else {
			//
			//				}
			//			}
			//
			//			List<Record> sortedRecords = new ArrayList<>();
			//
			//			List<Metadata> referenceMetadatas = schemaType.getAllParentReferences();
			//
			//			Map<String, Set<String>> dependencyMap = new HashMap<>();
			//			for (Record record : unsortedRecords) {
			//				String parentDependencyId = record.getNonNullValueIn(referenceMetadatas);
			//				dependencyMap.put(record.getId(), Collections.singleton(parentDependencyId));
			//			}
			//			List<String> sortedIds = new DependencyUtils<String>().sortByDependency(dependencyMap);
			//			Map<String, Record> idRecordMap = toIdRecordMap(unsortedRecords);
			//			for (String recordId : sortedIds) {
			//				sortedRecords.add(idRecordMap.get(recordId));
			//			}
			//
			//			List<String> idsOfSortedByIds = toIdList(sortedRecordsById);
			//			List<String> idsOfSorted = toIdList(sortedRecords);
			//
			//			if (!idsOfSortedByIds.equals(idsOfSorted)) {
			//				System.out.println("bobo!");
			//			}

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
		MetadataSchema recordSchema = schemaTypes.getSchema(record.getSchemaCode());

		for (Metadata metadata : recordSchema.getMetadatas()) {

			if (schemaTypes.isRecordTypeMetadata(metadata)) {
				changeSchemaTypeAccordingToTypeLinkedSchema(record, schemaTypes, recordProvider, metadata);
			}
		}
	}

	public static void changeSchemaTypeAccordingToTypeLinkedSchema(Record record, MetadataSchemaTypes schemaTypes,
			RecordProvider recordProvider, Metadata typeMetadata) {
		MetadataSchema recordSchema = schemaTypes.getSchema(record.getSchemaCode());
		String newSchemaCode = getSchemaAccordingToTypeLinkedSchema(record, schemaTypes, recordProvider, typeMetadata);
		if (!record.getSchemaCode().equals(newSchemaCode)) {
			MetadataSchema newSchema = schemaTypes.getSchema(newSchemaCode);
			record.changeSchema(recordSchema, newSchema);
		}

	}

	public static String getSchemaAccordingToTypeLinkedSchema(Record record, MetadataSchemaTypes schemaTypes,
			RecordProvider recordProvider, Metadata typeMetadata) {
		MetadataSchema recordSchema = schemaTypes.getSchema(record.getSchemaCode());
		MetadataSchema referencedSchema = schemaTypes.getDefaultSchema(typeMetadata.getReferencedSchemaType());
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
			MetadataSchema schema = types.getSchema(record.getSchemaCode());
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
}
