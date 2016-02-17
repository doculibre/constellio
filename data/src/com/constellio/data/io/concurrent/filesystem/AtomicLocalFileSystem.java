package com.constellio.data.io.concurrent.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.AtomicIOException;
import com.constellio.data.io.concurrent.exception.OptimisticLockingException;
import com.constellio.data.io.concurrent.exception.UnsupportedPathException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;

class LocalFileSystemLock implements DistributedLock{
	RandomAccessFile randomAccessFile;
	FileChannel channel;
	FileLock lock;
	File file;
	
	public LocalFileSystemLock(File file){
		this.file = file;
	}
	
	public void lock(){
		try {
			prepare();
			lock = channel.lock();
		} catch (IOException e) {
			throw new DistributedLock.LockingException("Cannot create the lock", e);
		}
	}

	private void prepare()
			throws FileNotFoundException {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		randomAccessFile = new RandomAccessFile(file, "rw");
		channel = randomAccessFile.getChannel();
	}
	
	public void release() throws IOException{
		lock.release();
		channel.close();
		randomAccessFile.close();
	}
	
	@Override
	public boolean tryLock(long timeout, TimeUnit unit) {
		try {
			prepare();
			lock = channel.tryLock();
			return lock != null;
		} catch (IOException e) {
			throw new DistributedLock.LockingException("Cannot create the lock", e);
		}
	}

	@Override
	public void unlock()
			throws LockingException {
		try {
			lock.release();
			channel.close();
			randomAccessFile.close();
		} catch (IOException e) {
			throw new DistributedLock.LockingException("Cannot close the lock", e);
		}
	}
}


/**
 * A {@link AtomicFileSystem} that works on the local fileSystem
 * @author doculibre
 *
 */
public class AtomicLocalFileSystem extends AbstractAtomicFileSystem{
	private HashingService hashingService;

	public AtomicLocalFileSystem(HashingService hashingService) {
		this.hashingService = hashingService;
	}

	@Override
	public synchronized DataWithVersion readData(String path) {
		try {
			File aFile = makeFile(path);
			byte[] content = null;
			String version = null;
			if (!aFile.isDirectory()) {
				content = FileUtils.readFileToByteArray(aFile);
				version = hashingService.getHashFromBytes(content);
			}

			return new DataWithVersion(content, version);
		} catch (FileNotFoundException e) {
			throw new com.constellio.data.io.concurrent.exception.FileNotFoundException(e);
		} catch (IOException | HashingServiceException e) {
			throw new AtomicIOException(e);
		}
	}

	private File makeFile(String path) {
		return new File(path);
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
			throw new AtomicIOException(e);
		}
	}

	private String checkPath(String path, Object version) {
		if (version != null) {
			DataWithVersion readData = readData(path);
			if (!readData.getVersion().equals(version))
				throw new OptimisticLockingException();
		}

		if (!path.contains(File.separator) || path.startsWith("//"))
			throw new UnsupportedPathException(
					"All paths should contain " + File.separator + " and should not start with //: " + path);
		return path;
	}

	@Override
	public synchronized void delete(String path, Object version) {
		path = checkPath(path, version);
		File aFile = makeFile(path);
		if (aFile.isFile()) {
			aFile.delete();
		} else {
			try {
				FileUtils.deleteDirectory(aFile);
			} catch (IOException e) {
				throw new AtomicIOException(e);
			}
		}
	}

	@Override
	public synchronized List<String> list(String path) {
		List<String> files = new ArrayList<>();
		File aDir = makeFile(path);
		if (!aDir.exists() || aDir.isFile())
			return null;
		for (File file : aDir.listFiles()) {
			files.add(file.getAbsolutePath());
		}
		return files;
	}

	@Override
	public synchronized boolean exists(String path) {
		return makeFile(path).exists();
	}

	@Override
	public synchronized boolean isDirectory(String path) {
		path = checkPath(path, null);
		return makeFile(path).isDirectory();
	}

	@Override
	public synchronized boolean mkdirs(String path) {
		path = checkPath(path, null);
		return makeFile(path).mkdirs();
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public DistributedLock getLock(String path) {
		path = checkPath(path, null);
		LocalFileSystemLock localFileSystemLock = new LocalFileSystemLock(makeFile(path));
		
		return localFileSystemLock;
	}

}
