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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.ConcurrencyIOException;
import com.constellio.data.io.concurrent.exception.OptimisticLockingException;
import com.constellio.data.io.concurrent.exception.UnsupportedPathException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;

/**
 * A {@link AtomicFileSystem} that works on the local fileSystem
 * @author doculibre
 *
 */
public class AtomicLocalFileSystem implements AtomicFileSystem{
	private File baseFld;
	private HashingService hashingService;
	
	public AtomicLocalFileSystem(File baseFld, HashingService hashingService) {
		this.baseFld = baseFld;
		baseFld.mkdirs();
		this.hashingService = hashingService;
	}

	@Override
	public synchronized DataWithVersion readData(String path) {
		try {
			byte[] content = FileUtils.readFileToByteArray(makeFile(path));
			String version = hashingService.getHashFromBytes(content);
			
			return new DataWithVersion(content, version);
		} catch (IOException | HashingServiceException e) {
			throw new ConcurrencyIOException(e);
		}
	}

	private File makeFile(String path) {
		return new File(baseFld, path);
	}

	@Override
	public synchronized DataWithVersion writeData(String path, DataWithVersion dataWithVersion) {
		checkPath(path, dataWithVersion.getVersion());
		try {
			byte[] bytes = dataWithVersion.getData();
			FileUtils.writeByteArrayToFile(makeFile(path), bytes);
			String newVersion = hashingService.getHashFromBytes(bytes);
			return new DataWithVersion(bytes, newVersion);
		} catch (IOException | HashingServiceException e) {
			throw new ConcurrencyIOException(e);
		}
	}

	private String checkPath(String path, Object version) {
		if (version != null){
			DataWithVersion readData = readData(path);
			if (!readData.getVersion().equals(version))
				throw new OptimisticLockingException();
		}
		
		if (!path.startsWith("/") || path.startsWith("//"))
			throw new UnsupportedPathException("All pathes should start with only one '/': " + path);
		return path.substring(1);
	}

	@Override
	public synchronized void delete(String path, Object version) {
		path = checkPath(path, version);
		File aFile = makeFile(path);
		if (aFile.isFile()){
			aFile.delete();
		} else {
			try {
				FileUtils.deleteDirectory(aFile);
			} catch (IOException e) {
				throw new ConcurrencyIOException(e);
			}
		}
		
		baseFld.mkdir();	//in case the root of file system is deleted.
	}

	@Override
	public synchronized List<String> list(String path) {
		List<String> files = new ArrayList<>();
		File aDir = makeFile(path);
		if (!aDir.exists() || aDir.isFile())
			return null;
		for (File file: aDir.listFiles()){
			files.add(file.getAbsolutePath().replace(baseFld.getAbsolutePath(), "").replace("//", "/").replace('\\', '/'));
		}
		return files;
	}

	@Override
	public synchronized boolean exists(String path) {
		return makeFile(path).exists();
	}

	@Override
	public boolean isDirectory(String path) {
		path = checkPath(path, null);
		return makeFile(path).isDirectory();
	}

	@Override
	public boolean mkdirs(String path) {
		path = checkPath(path, null);
		return makeFile(path).mkdir();
	}
	
	public File getBaseFld() {
		return baseFld;
	}

}
