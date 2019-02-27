package com.constellio.model.services.users;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_NoSuchUserLog;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

//AFTER : Rename UserFilesServices
public class UserPhotosServices {

	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH_mm_ss.SSS";

	private final String ZIP_LOG_FILE_RESOURCENAME = "UserFilesServices-zipLogFile";
	private final String ZIP_LOG_TEMP_FOLDER_RESOURCENAME = "UserFilesServices-zipLogTempFolder";
	private final String WRITE_LOG_FILE_TO_TEMP_FOLDER_RESOURCENAME = "UserFilesServices-writeLogFileToTempFolderOutputStream";
	private final String READ_LOG_FILE_RESOURCENAME = "UserFilesServices-readLogFileInputStream";

	private DataLayerFactory dataLayerFactory;

	private IOServices ioServices;

	private ConfigManager configManager;

	private ZipService zipService;

	public UserPhotosServices(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
		this.configManager = dataLayerFactory.getConfigManager();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.zipService = dataLayerFactory.getIOServicesFactory().newZipService();

	}

	public void changePhoto(InputStream inputStream, String username) {
		String configPath = getPhotoConfigPath(username);

		if (configManager.exist(configPath)) {
			String hash = configManager.getBinary(configPath).getHash();
			try {
				configManager.update(configPath, hash, inputStream);
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new ImpossibleRuntimeException(optimisticLockingConfiguration);
			}
		} else {
			configManager.add(configPath, inputStream);
		}

	}

	public StreamFactory<InputStream> getPhotoInputStream(String username) {
		String configPath = getPhotoConfigPath(username);
		if (configManager.exist(configPath)) {
			BinaryConfiguration binaryConfiguration = configManager.getBinary(configPath);
			return binaryConfiguration.getInputStreamFactory();
		} else {
			throw new UserPhotosServicesRuntimeException_UserHasNoPhoto(username);
		}
	}

	private String getPhotoConfigPath(String username) {
		return "/photos/" + username;
	}

	public boolean hasPhoto(String username) {
		String configPath = getPhotoConfigPath(username);

		return configManager.exist(configPath);
	}

	private String getLogFolderId(String username) {
		return "userlogs/" + username;
	}

	private String getLogId(String username, String log) {
		return "userlogs/" + username + "/" + log;
	}

	public void addLogFile(String username, InputStream inputStream) {
		String logName = TimeProvider.getLocalDateTime().toString(DATE_PATTERN);
		addLogFile(username, logName, inputStream);
	}

	public void addLogFile(String username, String logName, InputStream inputStream) {
		String path = getLogId(username, logName);
		getContentDao().add(path, inputStream);
	}

	public ContentDao getContentDao() {
		return dataLayerFactory.getContentsDao();
	}

	public File getPhotosFolder() {
		return ((FileSystemContentDao) dataLayerFactory.getContentsDao()).getFileOf("photos");
	}

	public File getUserLogsFolder() {
		return ((FileSystemContentDao) dataLayerFactory.getContentsDao()).getFileOf("userlogs");
	}

	public List<String> getUserLogs(String username) {
		String folderId = getLogFolderId(username);
		ContentDao contentDao = getContentDao();
		if (contentDao.isFolderExisting(folderId)) {
			List<String> logs = new ArrayList<>();
			for (String file : contentDao.getFolderContents(folderId)) {
				logs.add(file.replace(folderId + "/", ""));
			}
			return logs;
		} else {
			return Collections.emptyList();
		}
	}

	public InputStream newUserLogInputStream(String username, String log, String resourceName) {
		String logPath = getLogId(username, log);
		try {
			return getContentDao().getContentInputStream(logPath, resourceName);
		} catch (ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
			throw new UserPhotosServicesRuntimeException_NoSuchUserLog(username, log);
		}
	}

	StreamFactory<InputStream> getAllLogs(final String username) {
		return new StreamFactory<InputStream>() {
			@Override
			public InputStream create(String name)
					throws IOException {

				File zipFile = new File(ioServices.newTemporaryFolder(ZIP_LOG_FILE_RESOURCENAME), "logs.zip");
				File tempFolder = ioServices.newTemporaryFolder(ZIP_LOG_TEMP_FOLDER_RESOURCENAME);

				try {
					copyUserLogFilesInTempFolder(tempFolder, username);
					zipService.zip(zipFile, asList(tempFolder.listFiles()));
				} catch (ZipServiceException | RuntimeException e) {
					ioServices.deleteQuietly(zipFile);
					throw new RuntimeException(e);

				} finally {
					ioServices.deleteQuietly(tempFolder);
				}

				return ioServices.newBufferedFileInputStreamWithFileDeleteOnClose(zipFile, name);
			}
		};
	}

	private void copyUserLogFilesInTempFolder(File tempFolder, String username)
			throws IOException {
		for (String log : getUserLogs(username)) {
			File logFile = new File(tempFolder, log + ".zip");

			OutputStream out = ioServices
					.newBufferedFileOutputStream(logFile, WRITE_LOG_FILE_TO_TEMP_FOLDER_RESOURCENAME);

			InputStream in;
			try {
				in = newUserLogInputStream(username, log, READ_LOG_FILE_RESOURCENAME);
			} catch (RuntimeException e) {
				ioServices.closeQuietly(out);
				throw e;
			}
			ioServices.copyAndClose(in, out);

		}
	}

	public void deleteUserLog(String username, String log) {
		String logPath = getLogId(username, log);
		getContentDao().delete(asList(logPath));
	}
}