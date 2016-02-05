package com.constellio.data.io.concurrent.filesystem;

import java.io.Closeable;
import java.io.File;
import java.util.List;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.AtomicIOException;

/**
 * This class represents a file system in the Constellio environment. 
 * @author Majid
 *
 */
public interface AtomicFileSystem extends Closeable {
	public static final String ROOT_PATH = File.separator;

	/**
	 * Atomic read data at the
	 * @param path
	 * @return a DataWithVersion if a file exists in the given path.
	 * @throws {@link AtomicIOException} if no file exists in the given path.
	 */
	public DataWithVersion readData(String path);

	/**
	 * Write data to a specific path and return the a DataWithVersion with the updated version of the data. If the input version is null, the method does
	 * not check for optimistic locking. 
	 * @param path
	 * @param dataWithVersion
	 * @return An updated dataWithVersion with same data of the input but a new version
	 * @throws throws OptimisticLockingException if the version of file is different from the given input
	 */
	public DataWithVersion writeData(String path, DataWithVersion dataWithVersion);

	public void delete(String path, Object version);

	/**
	 * list all files in the given path
	 * @return list of all files in the given path 
	 */
	public List<String> list(String path);

	public boolean exists(String path);

	public boolean isDirectory(String path);

	public boolean mkdirs(String path);

	public void close();
}
