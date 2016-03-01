package com.constellio.data.io.concurrent.filesystem;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.DataWrapper;
import com.constellio.data.io.concurrent.exception.AtomicIOException;

/**
 * Create a file system that is limited to a sub path in another file system.
 *
 * @author Majid Laali
 *
 */
public class ChildAtomicFileSystem extends BaseAtomicFileSystemDecorator {
	private String basePath;

	public ChildAtomicFileSystem(AtomicFileSystem toBeDecorated, String basePath) {
		this(toBeDecorated, basePath, true);
	}

	public ChildAtomicFileSystem(AtomicFileSystem toBeDecorated, String basePath, boolean forceCreate) {
		super(toBeDecorated);

		/*if (!basePath.startsWith("/"))
			throw new IllegalArgumentException("The path should start with '/' but it does not: " + basePath);*/

		if (basePath.endsWith("//"))
			throw new IllegalArgumentException("The path should not end with '//' but it does: " + basePath);

		if (basePath.endsWith("/"))
			basePath.substring(0, basePath.length() - 1);

		this.basePath = basePath;
		if (!toBeDecorated.exists(basePath)) {
			if (forceCreate)
				toBeDecorated.mkdirs(basePath);
			else
				throw new AtomicIOException("The base folder does not exist: " + basePath);
		}
	}

	@Override
	public DataWithVersion readData(String path) {
		return super.readData(basePath + path);
	}

	@Override
	public DataWithVersion writeData(String path, DataWithVersion dataWithVersion) {
		return super.writeData(basePath + path, dataWithVersion);
	}

	@Override
	public void delete(String path, Object version) {
		super.delete(basePath + path, version);
		if (path.equals(ROOT_PATH)) {    //if the user delete root directory
			super.mkdirs(basePath);    //create a root directory again.
		}
	}

	@Override
	public List<String> list(String path) {
		List<String> absolutePathList = super.list(basePath + path);
		List<String> pathList = null;
		if (absolutePathList != null) {
			pathList = new ArrayList<>();
			for (String anAbsolutePath : absolutePathList) {
				pathList.add(anAbsolutePath.substring(basePath.length()));
			}
		}
		return pathList;
	}

	@Override
	public boolean exists(String path) {
		return super.exists(basePath + path);
	}

	@Override
	public boolean isDirectory(String path) {
		return super.isDirectory(basePath + path);
	}

	@Override
	public boolean mkdirs(String path) {
		return super.mkdirs(basePath + path);
	}
	
	@Override
	public <T, R extends DataWrapper<T>> R writeData(String path, R dataWrapper) {
		return super.writeData(basePath + path, dataWrapper);
	}
	
	@Override
	public <T, R extends DataWrapper<T>> R readData(String path, R dataWrapper) {
		return super.readData(basePath + path, dataWrapper);
	}

	@Override
	public DistributedLock getLock(String path) {
		return super.getLock(basePath + path);
	}
}
