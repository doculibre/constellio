package com.constellio.data.dao.services.contents;

import com.constellio.data.dao.services.contents.ContentDao.DaoFileConsumer;
import com.constellio.data.dao.services.contents.ContentDao.DaoFileFunction;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.Optional;

@AllArgsConstructor
public class DaoFile {

	@Getter
	String id;

	@Getter
	String name;

	long length;

	long lastModified;

	ContentDao contentDao;

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

}
