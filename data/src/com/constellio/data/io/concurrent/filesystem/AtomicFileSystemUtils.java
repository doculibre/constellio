package com.constellio.data.io.concurrent.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.UnsupportedPathException;

public class AtomicFileSystemUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(AtomicFileSystemUtils.class);
	/**
	 * This function is not thread safe.
	 * @param master
	 * @param slave
	 * @return
	 */
	public static boolean sync(AtomicFileSystem master, AtomicFileSystem slave){
		return sync(master, slave, (String[])null);
	}

	public static boolean sync(AtomicFileSystem master, AtomicFileSystem slave, String... excludePathRegex){
		return sync(File.separator, master, slave, excludePathRegex);
	}

	public static boolean sync( String path, AtomicFileSystem master, AtomicFileSystem slave, String... excludePathRegex) {
		if (matchWithExclusion(path, excludePathRegex))
			return true;
		
		if (!master.exists(path))
			throw new UnsupportedPathException("The path does not exist!");

		if (master.isDirectory(path)){
			boolean isSynced = true;

			if (slave.exists(path) && !slave.isDirectory(path)){
				slave.delete(path, null);
				isSynced = false;
			} else if (!slave.exists(path)){
				slave.mkdirs(path);
				isSynced = false;
			}

			List<String> slaveSubPathes = slave.list(path);
			for (String subPath: master.list(path)){
				isSynced &= sync(subPath, master, slave, excludePathRegex);
				slaveSubPathes.remove(subPath);
			}

			for (String remainPath: slaveSubPathes){
				if (matchWithExclusion(remainPath, excludePathRegex))
					continue;
				isSynced = false;
				remove(slave, remainPath);
			}

			return isSynced;

		} else {
			boolean isSynced;
			if (slave.isDirectory(path)){
				LOGGER.info("delete a directory in the slave:"  + path);
				slave.delete(path, null);
			}

			DataWithVersion masterData = master.readData(path);
			byte[] masterBinaryData = masterData.getData();

			if (slave.exists(path)){
				DataWithVersion slaveData = slave.readData(path);
				isSynced = Arrays.equals(slaveData.getData(), masterBinaryData);
			} else {
				isSynced = false;
			}

			if (!isSynced){
				slave.writeData(path, new DataWithVersion(masterBinaryData, null));
			}

			return isSynced;
		}
	}

	private static boolean matchWithExclusion(String path, String... excludePathRegex) {
		if (excludePathRegex != null)
			for (String exclude: excludePathRegex){
				if (path.matches(exclude))
					return true;
			}
		return false;
	}

	private static void remove(AtomicFileSystem slave, String remainPath) {
		if (slave.isDirectory(remainPath)){
			for (String path: slave.list(remainPath)){
				remove(slave, path);
			}
		}

		slave.delete(remainPath, null);
	}


}
