/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.io.concurrent.filesystem;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.io.UncheckedIOException;

import com.constellio.data.io.concurrent.data.DataWithVersion;

public class AtomicFileSystemUtils {

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
		return sync("/", master, slave, excludePathRegex);
	}

	public static boolean sync( String path, AtomicFileSystem master, AtomicFileSystem slave, String... excludePathRegex) {
		if (matchWithExclusion(path, excludePathRegex))
			return true;

		if (master.isDirectory(path)){
			boolean isSynced = true;

			if (slave.exists(path) && !slave.isDirectory(path)){
				slave.delete(path, null);
				System.out.println("AtomicFileSystemUtils.sync() 1: " + path);
				isSynced = false;
			} else if (!slave.exists(path)){
				slave.mkdirs(path);
				System.out.println("AtomicFileSystemUtils.sync() 2: " + path);
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
				System.out.println("AtomicFileSystemUtils.sync() 3: " + path);
				remove(slave, remainPath);
			}

			return isSynced;

		} else {
			if (slave.isDirectory(path))
				throw new UncheckedIOException("TODO");

			DataWithVersion masterData = master.readData(path);
			byte[] masterBinaryData = masterData.getData();

			boolean isSynced;
			if (slave.exists(path)){
				DataWithVersion slaveData = slave.readData(path);
				isSynced = Arrays.equals(slaveData.getData(), masterBinaryData);
			} else {
				System.out.println("AtomicFileSystemUtils.sync() 4: " + path);
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
