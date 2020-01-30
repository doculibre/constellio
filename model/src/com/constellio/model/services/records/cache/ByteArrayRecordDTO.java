package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemaProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.constellio.data.dao.dto.records.RecordDTOMode.SUMMARY;

public abstract class ByteArrayRecordDTO implements Map<String, Object>, RecordDTO, Supplier<byte[]> {

	MetadataSchemaProvider schemaProvider;
	long version;
	boolean summary;
	short tenantId;
	byte collectionId;
	String collectionCode;
	short schemaId;
	String schemaCode;
	short typeId;
	String typeCode;
	byte[] data;

	private ByteArrayRecordDTO(MetadataSchemaProvider schemaProvider, long version, boolean summary,
							   short tenantId, String collectionCode, byte collectionId, String typeCode,
							   short typeId,
							   String schemacode, short schemaId, byte[] data) {
		this.schemaProvider = schemaProvider;
		this.version = version;
		this.data = data;
		this.tenantId = tenantId;
		this.collectionCode = collectionCode;
		this.collectionId = collectionId;
		this.typeCode = typeCode;
		this.typeId = typeId;
		this.schemaCode = schemacode;
		this.schemaId = schemaId;
		this.summary = summary;
	}

	public byte getCollectionId() {
		return collectionId;
	}

	public short getTypeId() {
		return typeId;
	}

	public short getSchemaId() {
		return schemaId;
	}

	public static class ByteArrayRecordDTOWithIntegerId extends ByteArrayRecordDTO {

		int id;

		public ByteArrayRecordDTOWithIntegerId(
				int id, MetadataSchemaProvider schemaProvider, long version, boolean summary,
				short tenantId, String collectionCode, byte collectionId, String typeCode, short typeId,
				String schemaCode, short schemaId, byte[] data) {
			super(schemaProvider, version, summary, tenantId, collectionCode, collectionId, typeCode, typeId,
					schemaCode, schemaId, data);
			this.id = id;
		}

		@Override
		public String getId() {
			return RecordUtils.toStringId(id);
		}

		@Override
		public long heapMemoryConsumption() {
			return 0;
		}

		@Override
		public long offHeapMemoryConsumption() {
			return data.length;
		}

		@Override
		public byte[] get() {
			return SummaryCacheSingletons.dataStore.get(tenantId).loadIntKey(id);
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder();

			sb.append("ByteArrayRecordDTO{");
			sb.append("id=" + getId());
			sb.append(", version=" + version);
			sb.append(", collection=" + getCollection());
			sb.append(", schema=" + getSchemaCode());
			sb.append(", data=" + Arrays.toString(data));
			sb.append("'}'");

			return sb.toString();
		}

		public int getIntId() {
			return id;
		}
	}

	public static class ByteArrayRecordDTOWithStringId extends ByteArrayRecordDTO {

		String id;

		public ByteArrayRecordDTOWithStringId(
				String id, MetadataSchemaProvider schemaProvider, long version, boolean summary,
				short instanceId, String collectionCode, byte collectionId, String typeCode, short typeId,
				String schemaCode, short schemaId, byte[] data) {
			super(schemaProvider, version, summary, instanceId, collectionCode, collectionId, typeCode, typeId,
					schemaCode, schemaId, data);
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public long heapMemoryConsumption() {
			return LangUtils.sizeOf(id) + data.length;
		}

		@Override
		public long offHeapMemoryConsumption() {
			return 0;
		}

		@Override
		public byte[] get() {
			return SummaryCacheSingletons.dataStore.get(tenantId).loadStringKey(id);
		}
	}

	@Override
	public abstract String getId();

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public Map<String, Object> getFields() {
		return this;
	}

	@Override
	public Map<String, Object> getCopyFields() {
		return Collections.emptyMap();
	}

	@Override
	public RecordDTOMode getLoadingMode() {
		return summary ? SUMMARY : RecordDTOMode.FULLY_LOADED;
	}

	@Override
	public RecordDTO createCopyWithDelta(RecordDeltaDTO recordDeltaDTO) {
		throw new UnsupportedOperationException("createCopyWithDelta is not supported on summary record cache");
	}

	@Override
	public RecordDTO withVersion(long version) {
		throw new UnsupportedOperationException("withVersion is not supported on summary record cache");
	}

	@Override
	public RecordDTO createCopyOnlyKeeping(Set<String> metadatasDataStoreCodes) {
		throw new UnsupportedOperationException("createCopyOnlyKeeping is not supported on summary record cache");
	}

	@Override
	public int size() {
		return CacheRecordDTOUtils.metadatasWithValueCount(data);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		MetadataSchema schema = schemaProvider.get(collectionId, typeId, schemaId);
		return CacheRecordDTOUtils.containsMetadata(data, schema, (String) key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("containsValue is not supported on summary record cache");
	}

	@Override
	public Object get(Object key) {
		if ("collection_s".equals(key)) {
			return collectionCode;
		}

		if ("schema_s".equals(key)) {
			return schemaCode;
		}

		MetadataSchema schema = schemaProvider.get(collectionId, typeId, schemaId);
		return CacheRecordDTOUtils.readMetadata(getId(), data, schema, (String) key, this);
	}

	@Nullable
	@Override
	public Object put(String key, Object value) {
		throw new UnsupportedOperationException("put is not supported on summary record cache");
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException("remove is not supported on summary record cache");
	}

	@Override
	public void putAll(@NotNull Map<? extends String, ?> m) {
		throw new UnsupportedOperationException("putAll is not supported on summary record cache");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("clear is not supported on summary record cache");
	}

	@NotNull
	@Override
	public Set<String> keySet() {
		MetadataSchema schema = schemaProvider.get(collectionId, typeId, schemaId);
		Set<String> keys = CacheRecordDTOUtils.getStoredMetadatas(data, schema);
		keys.add("collection_s");
		keys.add("schema_s");
		return keys;
	}

	@NotNull
	@Override
	public Collection<Object> values() {
		MetadataSchema schema = schemaProvider.get(collectionId, typeId, schemaId);
		Set<Object> values = CacheRecordDTOUtils.getStoredValues(getId(), data, schema, this);
		values.add(getCollection());
		values.add(getSchemaCode());
		return values;
	}

	@NotNull
	@Override
	public Set<Entry<String, Object>> entrySet() {

		MetadataSchema schema = schemaProvider.get(collectionId, typeId, schemaId);
		Set<Entry<String, Object>> entries = CacheRecordDTOUtils.toEntrySet(getId(), data, schema, this);

		entries.add(new SimpleEntry("collection_s", getCollection()));
		entries.add(new SimpleEntry("schema_s", getSchemaCode()));

		try {


			return entries;

		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("ByteArrayRecordDTO{");
		sb.append("id=" + getId());
		sb.append(", version=" + version);
		sb.append(", collection=" + getCollection());
		sb.append(", schema=" + getSchemaCode());
		sb.append(", data=" + Arrays.toString(data));
		sb.append("'}'");

		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ByteArrayRecordDTO)) {
			return false;
		}
		ByteArrayRecordDTO that = (ByteArrayRecordDTO) o;
		return version == that.version &&
			   summary == that.summary &&
			   collectionId == that.collectionId &&
			   schemaId == that.schemaId &&
			   typeId == that.typeId &&
			   Arrays.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(version, summary, collectionId, schemaId, typeId);
		result = 31 * result + Arrays.hashCode(data);
		return result;
	}
}
