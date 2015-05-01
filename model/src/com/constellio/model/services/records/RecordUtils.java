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
package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.utils.DependencyUtils;

public class RecordUtils {

	private SchemaUtils schemaUtils;

	public RecordUtils() {
		schemaUtils = newSchemaUtils();
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
		List<Record> sortedRecords = new ArrayList<>();

		List<Metadata> referenceMetadatas = schemaType.getAllParentReferences();

		Map<String, Set<String>> dependencyMap = new HashMap<>();
		for (Record record : unsortedRecords) {
			String parentDependencyId = record.getNonNullValueIn(referenceMetadatas);
			dependencyMap.put(record.getId(), Collections.singleton(parentDependencyId));
		}
		List<String> sortedIds = new DependencyUtils<String>().sortByDependency(dependencyMap, null);
		Map<String, Record> idRecordMap = toIdRecordMap(unsortedRecords);
		for (String recordId : sortedIds) {
			sortedRecords.add(idRecordMap.get(recordId));
		}
		return sortedRecords;
	}

	public List<String> getModifiedMetadatasDataStoreCodes(List<Record> records) {
		Set<String> modifiedMetadatasCodes = new HashSet<>();
		for (Record record : records) {
			RecordImpl recordImpl = (RecordImpl) record;
			Map<String, Object> modifiedValues = recordImpl.getModifiedValues();
			modifiedMetadatasCodes.addAll(modifiedValues.keySet());
		}

		return new ArrayList<>(modifiedMetadatasCodes);
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
}
