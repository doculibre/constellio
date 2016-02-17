package com.constellio.data.io.concurrent.filesystem;

import java.util.List;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.DataWrapper;

public class BaseAtomicFileSystemDecorator implements AtomicFileSystem{
	private AtomicFileSystem toBeDecorated;
	
	public BaseAtomicFileSystemDecorator(AtomicFileSystem toBeDecorated) {
		this.toBeDecorated = toBeDecorated;
	}

	@Override
	public DataWithVersion readData(String path) {
		return toBeDecorated.readData(path);
	}

	@Override
	public DataWithVersion writeData(String path, DataWithVersion dataWithVersion) {
		return toBeDecorated.writeData(path, dataWithVersion);
	}

	@Override
	public void delete(String path, Object version) {
		toBeDecorated.delete(path, version);
	}

	@Override
	public List<String> list(String path) {
		return toBeDecorated.list(path);
	}

	@Override
	public boolean exists(String path) {
		return toBeDecorated.exists(path);
	}

	@Override
	public boolean isDirectory(String path) {
		return toBeDecorated.isDirectory(path);
	}

	@Override
	public boolean mkdirs(String path) {
		return toBeDecorated.mkdirs(path);
	}

	@Override
	public void close() {
		toBeDecorated.close();
	}

	@Override
	public <T, R extends DataWrapper<T>> R readData(String path, R dataWrapper) {
		return toBeDecorated.readData(path, dataWrapper);
	}

	@Override
	public <T, R extends DataWrapper<T>> R writeData(String path, R dataWrapper) {
		return toBeDecorated.writeData(path, dataWrapper);
	}

	public AtomicFileSystem getDecoratedFileSystem(){
		return toBeDecorated;
	}

	@Override
	public DistributedLock getLock(String path) {
		return toBeDecorated.getLock(path);
	}

}
