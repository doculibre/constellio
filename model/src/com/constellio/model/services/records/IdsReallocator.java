package com.constellio.model.services.records;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class IdsReallocator {

	List<boolean[]> allocatedIds = new ArrayList<>();

	SearchServices searchServices;

	Map<String, TypeWithIdsToReallocate> typesWithIdsToReallocateMap = new HashMap<>();

	private IdsReallocator(SearchServices searchServices) {
		this.searchServices = searchServices;


	}

	private boolean get(int intId) {
		int tenMillion = intId / 10_000_000;

		if (allocatedIds.size() - 1 < tenMillion) {
			return false;
		} else {
			return allocatedIds.get(tenMillion)[intId % 10_000_000];
		}
	}

	private void set(int intId, boolean value) {

		int tenMillion = intId / 10_000_000;

		while (allocatedIds.size() <= tenMillion) {
			allocatedIds.add(new boolean[10_000_000]);
		}

		allocatedIds.get(tenMillion)[intId % 10_000_000] = value;
	}

	private void reallocate() {
		int idx = 1;


		for (TypeWithIdsToReallocate typeWithIdsToReallocate : typesWithIdsToReallocateMap.values()) {
			Map<String, String> ids = new HashMap<>();
			for (String id : typeWithIdsToReallocate.idsToReallocateToSequential) {
				while (get(idx)) {
					idx++;
				}

				ids.put(id, StringUtils.leftPad("" + idx, 11, '0'));
				idx++;

			}

			for (String id : typeWithIdsToReallocate.sequentialIdsToReallocateToUUID) {
				ids.put(id, UUIDV1Generator.newRandomId());
			}

			typeWithIdsToReallocate.oldAndNewIdMapping = ids;

		}
	}

	private static List<String> typesWithUUIDs = Arrays.asList(Event.SCHEMA_TYPE, SearchEvent.SCHEMA_TYPE, "savedSearch");

	public static List<TypeWithIdsToReallocate> reallocateScanningSolr(ModelLayerFactory modelLayerFactory) {
		IdsReallocator reallocator = new IdsReallocator(modelLayerFactory.newSearchServices());

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			for (MetadataSchemaType schemaType : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes()) {

				Iterator<String> idIterator = modelLayerFactory.newSearchServices().recordsIdsIterator(new LogicalSearchQuery(from(schemaType).returnAll()));

				while (idIterator.hasNext()) {
					String id = idIterator.next();
					int intId = (int) LangUtils.tryParseLong(id, 0);

					String key = schemaType.getCollection() + "-" + schemaType.getCode();
					TypeWithIdsToReallocate typeWithIdsToReallocate = reallocator.typesWithIdsToReallocateMap.get(key);
					if (typeWithIdsToReallocate == null) {
						typeWithIdsToReallocate = new TypeWithIdsToReallocate();
						typeWithIdsToReallocate.schemaType = schemaType;
						reallocator.typesWithIdsToReallocateMap.put(key, typeWithIdsToReallocate);
					}

					if (typesWithUUIDs.contains(schemaType.getCode())) {
						if (intId != 0) {
							typeWithIdsToReallocate.sequentialIdsToReallocateToUUID.add(id);
							reallocator.set(intId, true);
						}

					} else {
						if (intId == 0) {
							typeWithIdsToReallocate.idsToReallocateToSequential.add(id);
						} else {
							reallocator.set(intId, true);
						}

					}

				}


			}
		}

		reallocator.reallocate();

		return new ArrayList<>(reallocator.typesWithIdsToReallocateMap.values());
	}

	public static class TypeWithIdsToReallocate {

		private MetadataSchemaType schemaType;

		private List<String> sequentialIdsToReallocateToUUID = new ArrayList<>();

		private List<String> idsToReallocateToSequential = new ArrayList<>();

		private Map<String, String> oldAndNewIdMapping = new HashMap<>();

		public MetadataSchemaType getSchemaType() {
			return schemaType;
		}

		public List<String> getSequentialIdsToReallocateToUUID() {
			return sequentialIdsToReallocateToUUID;
		}

		public List<String> getIdsToReallocateToSequential() {
			return idsToReallocateToSequential;
		}

		public Map<String, String> getOldAndNewIdMapping() {
			return oldAndNewIdMapping;
		}
	}

}
