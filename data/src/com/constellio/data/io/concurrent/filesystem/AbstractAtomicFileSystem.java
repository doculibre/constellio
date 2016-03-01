package com.constellio.data.io.concurrent.filesystem;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.DataWrapper;

public abstract class AbstractAtomicFileSystem implements AtomicFileSystem{
	private static int openedFileSystem;
	
	public AbstractAtomicFileSystem() {
		++openedFileSystem;
	}

	@Override
	public <T, R extends DataWrapper<T>> R readData(String path, R dataWrapper) {
		readData(path).getView(dataWrapper);
		return dataWrapper;
	}
	
	@Override
	public <T, R extends DataWrapper<T>> R writeData(String path, R dataWrapper) {
		DataWithVersion writeData = writeData(path, dataWrapper.toDataWithVersion());
		dataWrapper.initWithDataWithVersion(writeData);
		return dataWrapper;
	}
	
	@Override
	public void close() {
		--openedFileSystem;
	}
	
	public static int getOpenedFileSystem() {
		return openedFileSystem;
	}
}
