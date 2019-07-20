package com.constellio.model.services.records.cache2;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileSystemRecordsValuesCacheDataStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemRecordsValuesCacheDataStore.class);

	private DB onDiskDatabase;

	private HTreeMap<Integer, byte[]> intKeyMap;

	private HTreeMap<Integer, byte[]> intKeyMapMemoryBuffer;

	private HTreeMap<String, byte[]> stringKeyMap;

	private HTreeMap<String, byte[]> stringKeyMapMemoryBuffer;

	public FileSystemRecordsValuesCacheDataStore(File file) {
		if (!file.exists() || file.delete()) {
			this.onDiskDatabase = DBMaker.fileDB(file).fileLockDisable().make();
		} else {
			throw new IllegalStateException("File delete failed. Only one instance per file can be active. " +
											"To Create a new one close the other instance before instanciating " +
											"the second one.");
		}

		intKeyMap = onDiskDatabase.hashMap("intKeysDataStore")
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.create();

		stringKeyMap = onDiskDatabase.hashMap("stringKeysDataStore")
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.create();

		DB onMEmoryDatabase = DBMaker.memoryDB().make();


		intKeyMapMemoryBuffer = onMEmoryDatabase.hashMap("intKeysDataStoreBuffer")
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.expireMaxSize(10000)
				.expireOverflow(intKeyMap)
				.create();

		stringKeyMapMemoryBuffer = onMEmoryDatabase.hashMap("stringKeysDataStoreBuffer")
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.expireMaxSize(25)
				.expireOverflow(stringKeyMap)
				.create();


	}

	public void saveStringKey(String id, byte[] bytes) {
		stringKeyMapMemoryBuffer.put(id, bytes);
	}

	public void saveIntKey(int id, byte[] bytes) {
		intKeyMapMemoryBuffer.put(id, bytes);
	}

	public void removeStringKey(String id) {
		stringKeyMapMemoryBuffer.remove(id);
	}

	public void removeIntKey(int id) {
		intKeyMapMemoryBuffer.remove(id);
	}

	public byte[] loadStringKey(String id) {
		byte[] bytes = stringKeyMapMemoryBuffer.get(id);
		if (bytes == null) {
			throw new IllegalStateException("Record '" + id + "' has no stored bytes");
		}
		return bytes;
	}

	public byte[] loadIntKey(int id) {
		byte[] bytes = intKeyMapMemoryBuffer.get(id);
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
		intKeyMapMemoryBuffer.clear();
		stringKeyMapMemoryBuffer.clear();
	}
}
