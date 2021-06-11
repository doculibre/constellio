package com.constellio.data.dao.services.contents;

import com.constellio.data.dao.services.contents.ContentDao.DaoFileConsumer;
import com.constellio.data.dao.services.contents.ContentDao.DaoFileFunction;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import lombok.Getter;

import java.io.File;
import java.util.Optional;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class DaoFile {


	@Getter
	String id;

	@Getter
	String name;

	long length;

	long lastModified;

	boolean isDirectory;

	Boolean cachedExistStatus;

	@Getter
	ContentDao contentDao;

	public DaoFile(String id, String name, long length, long lastModified, boolean isDirectory,
				   ContentDao contentDao) {
		this.id = id;
		this.name = name;
		this.length = length;
		this.lastModified = lastModified;
		this.isDirectory = isDirectory;
		this.contentDao = contentDao;
	}

	public DaoFile(String id, String name, long length, long lastModified, boolean isDirectory,
				   ContentDao contentDao, Boolean cachedExistStatus) {
		this.id = id;
		this.name = name;
		this.length = length;
		this.lastModified = lastModified;
		this.isDirectory = isDirectory;
		this.contentDao = contentDao;
		this.cachedExistStatus = cachedExistStatus;
	}

	public long lastModifed() {
		return lastModified;
	}

	public long length() {
		return length;
	}

	public void readonlyConsume(DaoFileConsumer consumer) throws ContentDaoException_NoSuchContent {
		contentDao.readonlyConsume(id, consumer);
	}

	public void readonlyConsumeIfExists(DaoFileConsumer consumer) {
		contentDao.readonlyConsumeIfExists(id, consumer);
	}

	@Deprecated
	public File toFile() {
		return contentDao.getFileOf(id);
	}

	public void delete() {
		contentDao.delete(asList(id));
	}

	public boolean exists() {
		if (cachedExistStatus == null) {
			return cachedExistStatus = contentDao.isDocumentExisting(id);
		} else {
			return cachedExistStatus;
		}
	}

	public <T> T readonlyFunction(DaoFileFunction<T> function) throws ContentDaoException_NoSuchContent {
		return contentDao.readonlyFunction(id, function);
	}

	public <T> Optional<T> optionalReadonlyFunction(DaoFileFunction<T> function) {
		try {
			return Optional.of(contentDao.readonlyFunction(id, function));

		} catch (ContentDaoException_NoSuchContent ignored) {
			return Optional.empty();
		}
	}

	public boolean isDirectory() {
		return isDirectory;
	}
}
