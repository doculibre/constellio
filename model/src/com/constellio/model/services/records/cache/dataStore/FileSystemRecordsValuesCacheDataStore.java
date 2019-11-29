package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices.RecordIdVersion;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class FileSystemRecordsValuesCacheDataStore {

	private static final int VERSION = 2;

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemRecordsValuesCacheDataStore.class);

	private boolean recreated;

	private DB onDiskDatabase;

	private BTreeMap<Integer, byte[]> intKeyMap;

	private BTreeMap<String, byte[]> stringKeyMap;

	private File file;

	public FileSystemRecordsValuesCacheDataStore(File file) {
		this.file = file;


		if (Toggle.USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE.isEnabled()) {

			//	try {
			recreated = !file.exists();
			Maker dbMaker = DBMaker.fileDB(file);
			if (Toggle.USE_MMAP_WITHMAP_DB.isEnabled()) {
				dbMaker.fileMmapEnableIfSupported().fileMmapPreclearDisable();
				dbMaker.allocateStartSize(500 * 1024 * 1024).allocateIncrement(500 * 1024 * 1024);
			}
			dbMaker.checksumHeaderBypass();
			dbMaker.closeOnJvmShutdownWeakReference();
			this.onDiskDatabase = dbMaker.fileLockDisable().make();

			//			} catch(Throwable t) {
			//				file.delete();
			//				recreated = true;
			//				t.printStackTrace();
			//			}

			//			if (recreated) {
			////				Maker dbMaker = DBMaker.fileDB(file);
			////
			////				if (Toggle.USE_MMAP_WITHMAP_DB.isEnabled()) {
			////					dbMaker.fileMmapEnableIfSupported().fileMmapPreclearDisable();
			////					dbMaker.allocateStartSize(500 * 1024 * 1024).allocateIncrement(500 * 1024 * 1024);
			////				}
			//				this.onDiskDatabase = dbMaker.fileLockDisable().make();
			//			} else {
			//				throw new IllegalStateException("File delete failed. Only one instance per file can be active. " +
			//												"To Create a new one close the other instance before instanciating " +
			//												"the second one.");
			//			}
		} else {
			Maker dbMaker = DBMaker.memoryDB();
			this.onDiskDatabase = dbMaker.make();
		}


		intKeyMap = onDiskDatabase.treeMap("intKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();
		recreated = intKeyMap.isEmpty();
		if (recreated) {
			byte[] bytes = new byte[1];
			bytes[0] = VERSION;
			intKeyMap.put(0, bytes);
		} else {
			byte[] version = intKeyMap.get(0);
			//Will fail if previous map is from previous war with different structure
			recreated = version == null || version.length == 0 || version[0] != VERSION;
			if (recreated) {
				intKeyMap.clear();

				byte[] bytes = new byte[1];
				bytes[0] = VERSION;
				intKeyMap.put(0, bytes);
			}
		}


		stringKeyMap = onDiskDatabase.treeMap("stringKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();
		stringKeyMap.clear();
		DB onMEmoryDatabase = DBMaker.memoryDirectDB().make();

		//
		//		intKeyMapMemoryBuffer = onMEmoryDatabase.hashMap("intKeysDataStoreBuffer")
		//				.keySerializer(Serializer.INTEGER)
		//				.valueSerializer(Serializer.BYTE_ARRAY)
		//				.expireStoreSize(25 * 1024 * 1024)
		//				//.expireAfterGet()
		//				.expireAfterUpdate()
		//				.expireOverflow(intKeyMap)
		//				.create();
		//
		//		stringKeyMapMemoryBuffer = onMEmoryDatabase.hashMap("stringKeysDataStoreBuffer")
		//				.keySerializer(Serializer.STRING)
		//				.valueSerializer(Serializer.BYTE_ARRAY)
		//				.expireStoreSize(5 * 1024 * 1024)
		//				.expireAfterGet()
		//				.expireAfterCreate()
		//				.expireOverflow(stringKeyMap)
		//				.create();


	}

	public boolean isRecreated() {
		return recreated;
	}

	public void saveStringKey(String id, byte[] bytes) {
		stringKeyMap.put(id, bytes);
	}

	public void saveIntKeyPersistedAndMemoryData(int id, byte[] persistedData, ByteArrayRecordDTO memoryRecordDTO) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeInt(persistedData.length);
			objectOutputStream.writeLong(memoryRecordDTO.getVersion());
			objectOutputStream.write(persistedData);
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

		intKeyMap.put(id, byteArrayOutputStream.toByteArray());
	}

	public void removeStringKey(String id) {
		stringKeyMap.remove(id);
	}

	public void removeIntKey(int id) {
		intKeyMap.remove(id);
	}

	public byte[] loadStringKey(String id) {
		byte[] bytes = stringKeyMap.get(id);
		if (bytes == null) {
			throw new IllegalStateException("Record '" + id + "' has no stored bytes");
		}
		return bytes;
	}

	public byte[] loadIntKeyPersistedData(int id) {
		byte[] bytes = intKeyMap.get(id);
		if (bytes == null) {
			throw new IllegalStateException("Record '" + id + "' has no stored bytes");
		}

		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
			int dataLength = objectInputStream.readInt();
			objectInputStream.skipBytes(Long.BYTES);
			byte[] returnedBytes = new byte[dataLength];
			objectInputStream.read(returnedBytes);
			return returnedBytes;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(objectInputStream);
		}

	}

	public ByteArrayRecordDTO loadRecordDTOIfVersion(int id, long expectedVersion,
													 MetadataSchemasManager schemasManager,
													 MetadataSchemaType schemaType) {
		byte[] bytes = intKeyMap.get(id);
		if (bytes == null) {
			return null;
		}

		return toByteArrayRecordDTO(id, expectedVersion, schemasManager, schemaType, bytes);
	}

	@Nullable
	private ByteArrayRecordDTO toByteArrayRecordDTO(int id, long expectedVersion, MetadataSchemasManager schemasManager,
													MetadataSchemaType schemaType, byte[] bytes) {
		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
			int persitedDataLength = objectInputStream.readInt();
			long version = objectInputStream.readLong();
			if (expectedVersion != 0 && expectedVersion != version) {
				return null;
			}
			objectInputStream.skipBytes(persitedDataLength);
			short tenantId = objectInputStream.readShort();
			byte collectionId = objectInputStream.readByte();
			short typeId = objectInputStream.readShort();

			if (schemaType.getCollectionInfo().getCollectionId() != collectionId || schemaType.getId() != typeId) {
				return null;
			}
			short schemaId = objectInputStream.readShort();

			byte[] memoryData = new byte[bytes.length - Integer.BYTES - Long.BYTES - persitedDataLength
										 - Short.BYTES - Byte.BYTES - Short.BYTES - Short.BYTES];
			objectInputStream.read(memoryData);

			return new ByteArrayRecordDTOWithIntegerId(id, schemasManager, version, true, tenantId,
					schemaType.getCollection(), schemaType.getCollectionInfo().getCollectionId(),
					schemaType.getCode(), schemaType.getId(), schemaType.getSchema(schemaId).getCode(), schemaId,
					memoryData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(objectInputStream);
		}
	}

	public void close() {
		intKeyMap.close();
		stringKeyMap.close();
		onDiskDatabase.close();
	}

	public void clearAll() {
		intKeyMap.clear();
		stringKeyMap.clear();
		intKeyMap.clear();
		stringKeyMap.clear();
	}

	public List<RecordIdVersion> retrieveIdVersionForRecordOfType(MetadataSchemasManager schemasManager,
																  MetadataSchemaType schemaType,
																  LocalDateTime modifiedOnBefore,
																  Set<RecordId> except) {
		List<RecordIdVersion> idVersions = new ArrayList<>();

		Iterator<Entry<Integer, byte[]>> entryIterator = intKeyMap.entryIterator();
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
