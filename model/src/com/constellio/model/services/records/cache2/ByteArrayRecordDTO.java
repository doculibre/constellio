package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.utils.Holder;
import com.constellio.model.entities.schemas.MetadataSchema;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.records.cache2.CacheRecordDTOUtils.convertDTOToByteArray;

public class ByteArrayRecordDTO implements RecordDTO, Map<String, Object> {

	Holder<MetadataSchema> schemaHolder;
	Object id;
	long version;
	byte[] data;

	public ByteArrayRecordDTO(Holder<MetadataSchema> schemaHolder, RecordDTO dto) {
		this.schemaHolder = schemaHolder;
		this.id = dto.getId();
		this.version = dto.getVersion();
		this.data = convertDTOToByteArray(dto, schemaHolder.get());
	}

	@Override
	public String getId() {
		if (id instanceof Integer) {
			return StringUtils.leftPad("" + ((Integer) id).intValue(), 11, '0');
		} else {
			return (String) id;
		}
	}

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
		return CacheRecordDTOUtils.metadatasSize(data, schemaHolder.get());
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
