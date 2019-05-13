package com.constellio.model.services.records.cache2;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;

public class FileSystemRecordsValuesCacheDataStore {

	private HTreeMap<Integer, byte[]> intKeyMap;

	private HTreeMap<String, byte[]> stringKeyMap;

	public FileSystemRecordsValuesCacheDataStore(File file) {
		DB database = DBMaker.fileDB(file).make();
		intKeyMap = database.hashMap("intKeysDataStore")
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.create();

		stringKeyMap = database.hashMap("stringKeysDataStore")
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.create();
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
		return stringKeyMap.get(id);
	}

	public byte[] loadIntKey(int id) {
		return intKeyMap.get(id);
	}

}
