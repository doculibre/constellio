package com.constellio.data.io.concurrent.filesystem;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.DataWrapper;

/**
 * This file system keeps at most two versions of each file: 
 * 1- Initial state of a file. Whenever a file is read or updated for only the first time, 
 * 		a copy of the initial content of the file is stored in another file. 
 * 		This version of the file will be kept forever, unless it is manually deleted.
 * 2- Latest version of a file before the file is recovered. Whenever a recover request is sent to 
 * 		the file system, before removing the content of the file with the initial content of the file, 
 * 		a snapshot of the latest version of the file is store in a temporary file. 
 * 		Note that this file will be updated after each recover request.
 * 
 * @author Majid Laali
 *
 */
public class VersioningAtomicFileSystem extends BaseAtomicFileSystemDecorator{
	public static final String BACK_FILE_FLAG = ".__BACKUP__.";
	public static final String LOCK_FILE_PATH = "/_locknode_/lock-";
	public static final String TEMP_FILE_FLAG = ".__TEMP__.";
	private boolean hideBackupFiles;

	public VersioningAtomicFileSystem(AtomicFileSystem toBeDecorated, boolean hideBackupFiles) {
		super(toBeDecorated);
		this.hideBackupFiles = hideBackupFiles;
	}

	public VersioningAtomicFileSystem(AtomicFileSystem toBeDecorated) {
		this(toBeDecorated, true);
	}

	@Override
	public DataWithVersion readData(String path) {
		makeBackup(path);
		DataWithVersion backupContent = super.readData(path);
		if (backupContent != null)
			return backupContent;
		return super.readData(path);
	}

	private synchronized void makeBackup(String path) {
		String backupPath = getBackupPath(path);
		if (super.exists(path) && !super.exists(backupPath)){
			DistributedLock lock = super.getLock(LOCK_FILE_PATH);
			lock.lock();
			if (!exists(backupPath)){	//This is the first time the backup file is created.
				DataWithVersion content = super.readData(path);
				super.writeData(backupPath, new DataWithVersion(content.getData(), null));
			}
			lock.unlock();
		}
	}
	
	@Override
	public DataWithVersion writeData(String path, DataWithVersion dataWithVersion) {
		makeBackup(path);
		return super.writeData(path, dataWithVersion);
	}

	@Override
	public List<String> list(String path) {
		List<String> allFiles = super.list(path);
		
		List<String> pruned = new ArrayList<>();

		for (String file: allFiles)
			if (!LOCK_FILE_PATH.contains(file) && !(isHideBackupFiles() && file.contains(BACK_FILE_FLAG)))
				pruned.add(file);
		return pruned;
	}

	private String getBackupPath(String path){

		return String.format("%s%s", path, BACK_FILE_FLAG);
	}

	public boolean forceToRecover(String path) {
		String recoverFile = getBackupPath(path);
		if (!super.exists(recoverFile))
			return false;
		
		super.writeData(getTemporaryFilePathFor(path)
				, new DataWithVersion(super.readData(path).getData(), null));	//set version to null so it forces the write operation without concurrency exceptions.
		DataWithVersion backup = super.readData(recoverFile);
		
		super.writeData(path, new DataWithVersion(backup.getData(), null));
		return true;
	}

	private String getTemporaryFilePathFor(String path) {
		return String.format("%s%s", path, TEMP_FILE_FLAG);
	}

	public void setHideBackupFiles(boolean hideBackupFiles) {
		this.hideBackupFiles = hideBackupFiles;
	}

	public boolean isHideBackupFiles() {
		return hideBackupFiles;
	}

	public void forceToRecoverAll() {
		recoverADirectory("/");
	}
	
	private void recoverADirectory(String path){
		if (isDirectory(path)){
			List<String> subFile = super.list(path);
			for (String aFile: subFile){
				recoverADirectory(aFile);
			}
		} else {
			if (path.contains(BACK_FILE_FLAG)){
				String pathToOriginalFile = path.substring(0, path.indexOf(BACK_FILE_FLAG));
				DataWithVersion backup = super.readData(path);
				super.writeData(pathToOriginalFile, new DataWithVersion(backup.getData(), null));
			}
		}
		
	}
	
	@Override
	public <T, R extends DataWrapper<T>> R readData(String path, R dataWrapper) {
		return readData(path).getView(dataWrapper);
	}

	@Override
	public <T, R extends DataWrapper<T>> R writeData(String path, R dataWrapper) {
		DataWithVersion newData = writeData(path, dataWrapper.toDataWithVersion());
		dataWrapper.initWithDataWithVersion(newData);
		return dataWrapper;
	}

	public DataWithVersion getContentBeforeRecovering(String path) {
		String temporaryFile = getTemporaryFilePathFor(path);
		if (super.exists(temporaryFile))
			return super.readData(temporaryFile);
		return null;
	}
	
	public void cleanup(String path){
		if (isDirectory(path)){
			for (String aPath: super.list(path)){
				cleanup(aPath);
			}
		} 
		if (path.contains(BACK_FILE_FLAG) || path.contains(TEMP_FILE_FLAG))
			super.delete(path, null);
	}
	
	@Override
	public void delete(String path, Object version) {
		makeBackup(path);
		super.delete(path, version);
	}
}
