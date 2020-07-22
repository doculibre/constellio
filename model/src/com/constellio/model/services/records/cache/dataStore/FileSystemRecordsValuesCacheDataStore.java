package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices.RecordIdVersion;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.tika.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDateTime;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FileSystemRecordsValuesCacheDataStore {

	private static final int VERSION = 2;

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemRecordsValuesCacheDataStore.class);

	private boolean recreated;

	private DB onDiskFileSystemCacheDatabase;

	private DB onDiskRebootMemoryCacheDatabase;

	private final Map<Integer, byte[]> tempIntKeyMap = Collections.synchronizedMap(new LRUMap<>(40_000));

	private BTreeMap<Integer, byte[]> onDiskFileSystemCacheIntKeyMap;
	private BTreeMap<String, byte[]> onDiskFileSystemCacheStringKeyMap;

	//Used for faster memory cache rebuild when Constellio is restarting
	private BTreeMap<Integer, byte[]> onDiskRebootMemoryCacheIntKeyMap;

	private File filesystemCacheFile;
	private File rebootMemoryCacheFile;
	private File localCacheConfigs;

	private boolean busy = false;

	public FileSystemRecordsValuesCacheDataStore(File filesystemCacheFile, File rebootMemoryCacheFile,
												 File localCacheConfigs) {
		this.filesystemCacheFile = filesystemCacheFile;
		this.rebootMemoryCacheFile = rebootMemoryCacheFile;
		this.localCacheConfigs = localCacheConfigs;

		//Possibly enable mmap for faster loading
		open(Toggle.USE_MMAP_WITHMAP_DB_FOR_LOADING.isEnabled());
		onDiskFileSystemCacheStringKeyMap.clear();
	}

	public File getLocalCacheConfigs() {
		return localCacheConfigs;
	}

	private void open(boolean useMmap) {
		if (Toggle.USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE.isEnabled()) {

			recreated = !filesystemCacheFile.exists();
			Maker onDiskFileSystemCacheDatabaseMaker = DBMaker.fileDB(filesystemCacheFile);
			Maker onDiskRebootMemoryCacheDatabaseMaker = DBMaker.fileDB(rebootMemoryCacheFile);
			if (useMmap) {
				LOGGER.info("Opening MapDB with MMAP support");
				onDiskFileSystemCacheDatabaseMaker.fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable();
				onDiskFileSystemCacheDatabaseMaker.allocateStartSize(500 * 1024 * 1024).allocateIncrement(500 * 1024 * 1024);

				onDiskRebootMemoryCacheDatabaseMaker.fileMmapEnableIfSupported().fileMmapPreclearDisable().cleanerHackEnable();
				onDiskRebootMemoryCacheDatabaseMaker.allocateStartSize(50 * 1024 * 1024).allocateIncrement(50 * 1024 * 1024);
			} else {
				LOGGER.info("Opening MapDB without MMAP support");
				onDiskFileSystemCacheDatabaseMaker.fileChannelEnable();
				onDiskRebootMemoryCacheDatabaseMaker.fileChannelEnable();

			}
			onDiskFileSystemCacheDatabaseMaker.checksumHeaderBypass();
			onDiskFileSystemCacheDatabaseMaker.closeOnJvmShutdownWeakReference();

			onDiskRebootMemoryCacheDatabaseMaker.checksumHeaderBypass();
			onDiskRebootMemoryCacheDatabaseMaker.closeOnJvmShutdownWeakReference();

			this.onDiskFileSystemCacheDatabase = onDiskFileSystemCacheDatabaseMaker.fileLockDisable().make();
			this.onDiskRebootMemoryCacheDatabase = onDiskRebootMemoryCacheDatabaseMaker.fileLockDisable().make();


		} else {
			Maker dbMaker = DBMaker.memoryDB();
			this.onDiskFileSystemCacheDatabase = dbMaker.make();
			this.onDiskRebootMemoryCacheDatabase = dbMaker.make();
		}

		onDiskFileSystemCacheIntKeyMap = onDiskFileSystemCacheDatabase.treeMap("intKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();

		onDiskRebootMemoryCacheIntKeyMap = onDiskRebootMemoryCacheDatabase.treeMap("intKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();

		recreated |= onDiskFileSystemCacheIntKeyMap.isEmpty();
		if (recreated) {
			byte[] bytes = new byte[1];
			bytes[0] = VERSION;
			onDiskFileSystemCacheIntKeyMap.put(0, bytes);
		} else {
			byte[] version = onDiskFileSystemCacheIntKeyMap.get(0);
			//Will fail if previous map is from previous war with different structure
			recreated = version == null || version.length == 0 || version[0] != VERSION;
			if (recreated) {
				onDiskFileSystemCacheIntKeyMap.clear();

				byte[] bytes = new byte[1];
				bytes[0] = VERSION;
				onDiskFileSystemCacheIntKeyMap.put(0, bytes);
			}
		}

		onDiskFileSystemCacheStringKeyMap = onDiskFileSystemCacheDatabase.treeMap("stringKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();
	}

	public boolean isRecreated() {
		return recreated;
	}

	public void saveStringKey(String id, byte[] bytes) {
		ensureNotBusy();
		onDiskFileSystemCacheStringKeyMap.put(id, bytes);
	}

	public void saveIntKeyPersistedAndMemoryData(int id, byte[] persistedData, ByteArrayRecordDTO memoryRecordDTO) {
		Stats.compilerFor("FileSystemRecordsValuesCacheDataStore:savePersisted").log(() -> {
			ensureNotBusy();
			synchronized (tempIntKeyMap) {
				tempIntKeyMap.put(id, persistedData);
			}

			onDiskFileSystemCacheIntKeyMap.put(id, persistedData);
		});

		Stats.compilerFor("FileSystemRecordsValuesCacheDataStore:saveMemoryCopy").log(() -> {
			ensureNotBusy();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = null;
			try {
				objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeLong(memoryRecordDTO.getVersion());
				objectOutputStream.writeShort(memoryRecordDTO.getTenantId());
				objectOutputStream.writeByte(memoryRecordDTO.getCollectionId());
				objectOutputStream.writeShort(memoryRecordDTO.getTypeId());
				objectOutputStream.writeShort(memoryRecordDTO.getSchemaId());
				objectOutputStream.write(memoryRecordDTO.getData());

			} catch (IOException e) {
				throw new RuntimeException(e);

			} finally {
				IOUtils.closeQuietly(objectOutputStream);
			}

			onDiskRebootMemoryCacheIntKeyMap.put(id, byteArrayOutputStream.toByteArray());
		});
	}

	public void removeStringKey(String id) {
		ensureNotBusy();
		onDiskFileSystemCacheStringKeyMap.remove(id);
	}

	public void removeIntKey(int id) {
		Stats.compilerFor("FileSystemRecordsValuesCacheDataStore:remove").log(() -> {
			ensureNotBusy();
			synchronized (tempIntKeyMap) {
				tempIntKeyMap.remove(id);
			}
			onDiskFileSystemCacheIntKeyMap.remove(id);
			onDiskRebootMemoryCacheIntKeyMap.remove(id);
		});
	}

	public byte[] loadStringKey(String id) {
		ensureNotBusy();
		byte[] bytes = onDiskFileSystemCacheStringKeyMap.get(id);
		if (bytes == null) {
			throw new IllegalStateException("Record '" + id + "' has no stored bytes");
		}
		return bytes;
	}

	public byte[] loadIntKeyPersistedData(int id) {
		ensureNotBusy();
		return Stats.compilerFor("FileSystemRecordsValuesCacheDataStore:get").log(() -> {
			byte[] persistedDataInMemory = null;
			synchronized (tempIntKeyMap) {
				persistedDataInMemory = tempIntKeyMap.get(id);
			}

			if (persistedDataInMemory != null) {
				return persistedDataInMemory;
			}

			byte[] bytes = onDiskFileSystemCacheIntKeyMap.get(id);
			if (bytes == null) {
				throw new IllegalStateException("Record '" + id + "' has no stored bytes");
			}
			synchronized (tempIntKeyMap) {
				tempIntKeyMap.put(id, bytes);
			}
			return bytes;
		});
	}

	public ByteArrayRecordDTO loadRecordDTOIfVersion(int id, long expectedVersion,
													 MetadataSchemasManager schemasManager,
													 MetadataSchemaType schemaType) {
		ensureNotBusy();
		byte[] bytes = onDiskRebootMemoryCacheIntKeyMap.get(id);
		if (bytes == null) {
			return null;
		}

		return toByteArrayRecordDTO(id, expectedVersion, schemasManager, schemaType, bytes);
	}

	@Nullable
	private ByteArrayRecordDTO toByteArrayRecordDTO(int id, long expectedVersion, MetadataSchemasManager schemasManager,
													MetadataSchemaType schemaType, byte[] memoryBytes) {
		ensureNotBusy();
		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(new ByteArrayInputStream(memoryBytes));
			long version = objectInputStream.readLong();
			if (expectedVersion != 0 && expectedVersion != version) {
				return null;
			}
			short tenantId = objectInputStream.readShort();
			byte collectionId = objectInputStream.readByte();
			short typeId = objectInputStream.readShort();

			if (schemaType.getCollectionInfo().getCollectionId() != collectionId || schemaType.getId() != typeId) {
				return null;
			}
			short schemaId = objectInputStream.readShort();

			byte[] memoryData = new byte[memoryBytes.length - Long.BYTES
										 - Short.BYTES - Byte.BYTES - Short.BYTES - Short.BYTES];
			objectInputStream.read(memoryData);

			return new ByteArrayRecordDTOWithIntegerId(id, schemasManager, version, true, tenantId,
					schemaType.getCollection(), schemaType.getCollectionInfo().getCollectionId(),
					schemaType.getCode(), schemaType.getId(), schemaType.getSchema(schemaId).getCode(), schemaId,
					memoryData, -1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(objectInputStream);
		}
	}

	public void close() {
		onDiskFileSystemCacheIntKeyMap.close();
		onDiskFileSystemCacheStringKeyMap.close();
		onDiskRebootMemoryCacheIntKeyMap.close();

		onDiskFileSystemCacheIntKeyMap = null;
		onDiskFileSystemCacheStringKeyMap = null;
		onDiskRebootMemoryCacheIntKeyMap = null;
	}

	public void closeThenReopenWithoutMmap() {
		if (Toggle.USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE.isEnabled()) {
			//Otherwise, the case is safely in memory, no need to restart it

			LOGGER.info("closeThenReopenWithoutMmap");
			busy = true;

			try {
				Thread.sleep(10_000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			close();
			open(false);

			busy = false;
		}
	}

	private void ensureNotBusy() {
		while (busy) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void clearAll() {
		ensureNotBusy();
		synchronized (tempIntKeyMap) {
			tempIntKeyMap.clear();
		}
		onDiskFileSystemCacheIntKeyMap.close();
		onDiskFileSystemCacheStringKeyMap.close();
		onDiskRebootMemoryCacheIntKeyMap.close();
	}

	public List<RecordIdVersion> retrieveIdVersionForRecordOfType(MetadataSchemasManager schemasManager,
																  MetadataSchemaType schemaType,
																  LocalDateTime modifiedOnBefore,
																  Set<RecordId> except) {
		ensureNotBusy();
		List<RecordIdVersion> idVersions = new ArrayList<>();

		Iterator<Entry<Integer, byte[]>> entryIterator = onDiskRebootMemoryCacheIntKeyMap.entryIterator();
		while (entryIterator.hasNext()) {
			Entry<Integer, byte[]> entry = entryIterator.next();
			int id = entry.getKey();
			RecordId recordId = RecordId.id(id);
			if (!except.contains(recordId) && id != 0) {
				ByteArrayRecordDTO recordDTO = toByteArrayRecordDTO(id, 0, schemasManager, schemaType, entry.getValue());

				if (recordDTO != null) {
					LocalDateTime modifiedOn = (LocalDateTime) recordDTO.get(Schemas.MODIFIED_ON.getDataStoreCode());
					if (modifiedOn == null || !modifiedOn.isAfter(modifiedOnBefore)) {
						idVersions.add(new RecordIdVersion(recordId, recordDTO.getVersion()));
					}
				}
			}
		}


		return idVersions;
	}
}
