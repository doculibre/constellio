package com.constellio.model.services.records;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class IdsReallocationUtils {

	List<boolean[]> allocatedIds = new ArrayList<>();

	SearchServices searchServices;

	Map<String, TypeWithIdsToReallocate> typesWithIdsToReallocateMap = new HashMap<>();

	private IdsReallocationUtils(SearchServices searchServices) {
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

	private TypeWithIdsToReallocate get(MetadataSchemaType schemaType) {
		String key = schemaType.getCollection() + "-" + schemaType.getCode();
		TypeWithIdsToReallocate typeWithIdsToReallocate = typesWithIdsToReallocateMap.get(key);
		if (typeWithIdsToReallocate == null) {
			typeWithIdsToReallocate = new TypeWithIdsToReallocate();
			typeWithIdsToReallocate.schemaType = schemaType;
			typesWithIdsToReallocateMap.put(key, typeWithIdsToReallocate);
		}
		return typeWithIdsToReallocate;
	}

	public static List<TypeWithIdsToReallocate> reallocateScanningSolr(ModelLayerFactory modelLayerFactory) {
		IdsReallocationUtils reallocator = new IdsReallocationUtils(modelLayerFactory.newSearchServices());

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			for (MetadataSchemaType schemaType : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes()) {

				if (!schemaType.getCode().equals(Collection.SCHEMA_TYPE)) {
					Iterator<String> idIterator = modelLayerFactory.newSearchServices().recordsIdsIterator(new LogicalSearchQuery(from(schemaType).returnAll()));

					while (idIterator.hasNext()) {
						String id = idIterator.next();

						if (!id.endsWith("ZZ")) {
							int intId = (int) LangUtils.tryParseLong(id, 0);

							if (typesWithUUIDs.contains(schemaType.getCode())) {
								if (intId != 0) {
									reallocator.get(schemaType).sequentialIdsToReallocateToUUID.add(id);
									reallocator.set(intId, true);
								}

							} else {
								if (intId == 0) {
									reallocator.get(schemaType).idsToReallocateToSequential.add(id);
								} else {
									reallocator.set(intId, true);
								}

							}
						}
					}
				}

			}
		}

		reallocator.reallocate();

		return new ArrayList<>(reallocator.typesWithIdsToReallocateMap.values());
	}

	private static final String HEADER = "Collection,Schema type,Current id,New id";

	public static void writeCSVFile(List<TypeWithIdsToReallocate> types, File file) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(file);
		try {
			if (types.isEmpty()) {
				writer.append("No ids to reallocate");

			} else {
				writer.append(HEADER + "\n");

				for (TypeWithIdsToReallocate type : types) {

					List<String> reallocatedIds = new ArrayList<>(type.getIdsToReallocateToSequential());
					Collections.sort(reallocatedIds);

					for (String id : reallocatedIds) {
						String newId = type.getOldAndNewIdMapping().get(id);
						writer.append(type.getSchemaType().getCollection() + "," + type.getSchemaType().getCode()
									  + "," + id + "," + newId + "\n");
					}

				}

			}

		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			IOUtils.closeQuietly(writer);
		}

	}

	public static List<TypeWithIdsToReallocate> readCSVFile(ModelLayerFactory modelLayerFactory, File file)
			throws FileNotFoundException {
		Map<String, TypeWithIdsToReallocate> typeWithIdsToReallocates = new HashMap<>();

		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		try {
			List<String> lines = FileUtils.readLines(file, "UTF-8");

			if (!lines.get(0).equals(HEADER)) {
				return new ArrayList<>();
			}

			for (int i = 1; i < lines.size(); i++) {
				String[] splittedLines = lines.get(i).split(",");

				String key = splittedLines[0] + "-" + splittedLines[1];
				TypeWithIdsToReallocate type = typeWithIdsToReallocates.get(key);

				if (type == null) {
					type = new TypeWithIdsToReallocate();
					type.schemaType = schemasManager.getSchemaTypes(splittedLines[0]).getSchemaType(splittedLines[1]);
					typeWithIdsToReallocates.put(key, type);
				}

				type.idsToReallocateToSequential.add(splittedLines[2]);
				type.oldAndNewIdMapping.put(splittedLines[2], splittedLines[3]);

			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new ArrayList<>(typeWithIdsToReallocates.values());
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
