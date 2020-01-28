package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.utils.dev.Toggle;
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

	private boolean recreated;

	private DB onDiskDatabase;

	private BTreeMap<Integer, byte[]> intKeyMap;

	private BTreeMap<String, byte[]> stringKeyMap;

	private File file;

	private boolean busy = false;

	public FileSystemRecordsValuesCacheDataStore(File file) {
		this.file = file;

		//Possibly enable mmap for faster loading
		open(file, Toggle.USE_MMAP_WITHMAP_DB_FOR_LOADING.isEnabled());
		intKeyMap.clear();
		stringKeyMap.clear();
	}

	private void open(File file, boolean useMmap) {
		if (Toggle.USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE.isEnabled()) {

			//	try {
			//recreated = !file.exists();
			Maker dbMaker = DBMaker.fileDB(file);
			if (useMmap) {
				LOGGER.info("Opening MapDB with MMAP support");
				dbMaker.fileMmapEnableIfSupported().fileMmapPreclearDisable();
				dbMaker.allocateStartSize(500 * 1024 * 1024).allocateIncrement(500 * 1024 * 1024);
			} else {
				LOGGER.info("Opening MapDB without MMAP support");
			}
			dbMaker.checksumHeaderBypass();
			dbMaker.closeOnJvmShutdownWeakReference();
			this.onDiskDatabase = dbMaker.fileLockDisable().make();

		} else {
			Maker dbMaker = DBMaker.memoryDB();
			this.onDiskDatabase = dbMaker.make();
		}


		intKeyMap = onDiskDatabase.treeMap("intKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.INTEGER)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();
		//		recreated = intKeyMap.isEmpty();
		//		if (recreated) {
		//			byte[] bytes = new byte[1];
		//			bytes[0] = VERSION;
		//			intKeyMap.put(0, bytes);
		//		} else {
		//			byte[] version = intKeyMap.get(0);
		//			//Will fail if previous map is from previous war with different structure
		//			recreated = version == null || version.length == 0 || version[0] != VERSION;
		//			if (recreated) {
		//				intKeyMap.clear();
		//
		//				byte[] bytes = new byte[1];
		//				bytes[0] = VERSION;
		//				intKeyMap.put(0, bytes);
		//			}
		//		}


		stringKeyMap = onDiskDatabase.treeMap("stringKeysDataStore")
				.valuesOutsideNodesEnable()
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.BYTE_ARRAY)
				.createOrOpen();
	}

	public void saveStringKey(String id, byte[] bytes) {
		ensureNotBusy();
		stringKeyMap.put(id, bytes);
	}

	public void saveIntKey(int id, byte[] bytes) {
		ensureNotBusy();
		intKeyMap.put(id, bytes);
	}

	public void removeStringKey(String id) {
		ensureNotBusy();
		stringKeyMap.remove(id);
	}

	public void removeIntKey(int id) {
		ensureNotBusy();
		intKeyMap.remove(id);
	}

	public byte[] loadStringKey(String id) {
		ensureNotBusy();
		byte[] bytes = stringKeyMap.get(id);
		if (bytes == null) {
			throw new IllegalStateException("Record '" + id + "' has no stored bytes");
		}
		return bytes;
	}

	public byte[] loadIntKey(int id) {
		ensureNotBusy();
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

		intKeyMap = null;
		stringKeyMap = null;
		onDiskDatabase = null;
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
			open(file, false);

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
		intKeyMap.clear();
		stringKeyMap.clear();
		intKeyMap.clear();
		stringKeyMap.clear();
	}


}
