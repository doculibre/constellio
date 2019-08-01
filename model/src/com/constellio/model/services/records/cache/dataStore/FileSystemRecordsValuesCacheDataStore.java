package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileSystemRecordsValuesCacheDataStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemRecordsValuesCacheDataStore.class);

	private DB onDiskDatabase;

	private BTreeMap<Integer, byte[]> intKeyMap;

	private BTreeMap<String, byte[]> stringKeyMap;

	public FileSystemRecordsValuesCacheDataStore(File file) {
		if (!file.exists() || file.delete()) {

			if (new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
				Maker dbMaker = DBMaker.fileDB(file);

				if (Toggle.USE_MMAP_WITHMAP_DB.isEnabled()) {
					dbMaker.fileMmapEnableIfSupported().fileMmapPreclearDisable();
					dbMaker.allocateStartSize(500 * 1024 * 1024).allocateIncrement(500 * 1024 * 1024);
				}
				this.onDiskDatabase = dbMaker.fileLockDisable().make();
			} else {
				Maker dbMaker = DBMaker.memoryDB();
				this.onDiskDatabase = dbMaker.make();
			}


		} else {
			throw new IllegalStateException("File delete failed. Only one instance per file can be active. " +
											"To Create a new one close the other instance before instanciating " +
											"the second one.");
		}

		intKeyMap = onDiskDatabase.treeMap("intKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.create();

		stringKeyMap = onDiskDatabase.treeMap("stringKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.create();

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

	public void saveStringKey(String id, byte[] bytes) {
		stringKeyMap.put(id, bytes);
	}

	public void saveIntKey(int id, byte[] bytes) {
		intKeyMap.put(id, bytes);
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

	public byte[] loadIntKey(int id) {
		byte[] bytes = intKeyMap.get(id);
		if (bytes == null) {
			throw new IllegalStateException("Record '" + id + "' has no stored bytes");
		}
		return bytes;
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
}
