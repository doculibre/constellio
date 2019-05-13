package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.utils.Holder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache2.CacheRecordDTOUtils.CacheRecordDTOBytesArray;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.constellio.model.services.records.cache2.CacheRecordDTOUtils.convertDTOToByteArrays;

public abstract class ByteArrayRecordDTO implements RecordDTO, Map<String, Object>, Supplier<byte[]> {

	Holder<MetadataSchema> schemaHolder;
	long version;
	byte[] data;
	boolean summary;

	private ByteArrayRecordDTO(Holder<MetadataSchema> schemaHolder, long version, boolean summary,
							   byte[] data) {
		this.schemaHolder = schemaHolder;
		this.version = version;
		this.data = data;
		this.summary = summary;
	}

	public static ByteArrayRecordDTO create(ModelLayerFactory modelLayerFactory, RecordDTO dto) {
		String collection = (String) dto.getFields().get("collection_s");
		String schemaCode = (String) dto.getFields().get("schema_s");
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);

		//TODO Handle Holder
		Holder<MetadataSchema> schemaHolder = new Holder<>(schema);
		CacheRecordDTOBytesArray bytesArray = convertDTOToByteArrays(dto, schema);

		int intId = CacheRecordDTOUtils.toIntKey(dto.getId());

		if (intId == CacheRecordDTOUtils.KEY_IS_NOT_AN_INT) {
			SummaryCacheSingletons.dataStore.saveStringKey(dto.getId(), bytesArray.bytesToPersist);
			return new ByteArrayRecordDTOWithStringId(dto.getId(), schemaHolder, dto.getVersion(), dto.isSummary(), bytesArray.bytesToKeepInMemory);
		} else {
			SummaryCacheSingletons.dataStore.saveIntKey(intId, bytesArray.bytesToPersist);
			return new ByteArrayRecordDTOWithIntegerId(intId, schemaHolder, dto.getVersion(), dto.isSummary(), bytesArray.bytesToKeepInMemory);
		}

	}

	private static class ByteArrayRecordDTOWithIntegerId extends ByteArrayRecordDTO {

		int id;

		public ByteArrayRecordDTOWithIntegerId(
				int id, Holder<MetadataSchema> schemaHolder, long version, boolean summary, byte[] data) {
			super(schemaHolder, version, summary, data);
			this.id = id;
		}

		@Override
		public String getId() {
			return StringUtils.leftPad("" + id, 11, '0');
		}

		@Override
		public byte[] get() {
			return SummaryCacheSingletons.dataStore.loadIntKey(id);
		}
	}

	private static class ByteArrayRecordDTOWithStringId extends ByteArrayRecordDTO {

		String id;

		public ByteArrayRecordDTOWithStringId(
				String id, Holder<MetadataSchema> schemaHolder, long version, boolean summary, byte[] data) {
			super(schemaHolder, version, summary, data);
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public byte[] get() {
			return SummaryCacheSingletons.dataStore.loadStringKey(id);
		}
	}

	@Override
	public abstract String getId();

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public List<String> getLoadedFields() {
		return Collections.unmodifiableList(new ArrayList<>(keySet()));
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
	public boolean isSummary() {
		return summary;
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

	/**
	 * @return
	 */
	@Override
	public int size() {
		return CacheRecordDTOUtils.metadatasSize(data);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return CacheRecordDTOUtils.containsMetadata(data, schemaHolder.get(), (String) key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("containsValue is not supported on summary record cache");
	}

	@Override
	public Object get(Object key) {
		return CacheRecordDTOUtils.readMetadata(data, schemaHolder.get(), (String) key);
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
		return CacheRecordDTOUtils.getStoredMetadatas(data, schemaHolder.get());
	}

	@NotNull
	@Override
	public Collection<Object> values() {
		return CacheRecordDTOUtils.getStoredValues(data, schemaHolder.get());
	}

	@NotNull
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return CacheRecordDTOUtils.toEntrySet(data, schemaHolder.get());
	}

}
