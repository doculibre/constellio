package com.constellio.app.modules.es.connectors.http;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectorHttpContextServices {

	private static final String URLS_TEMP_FILE_READING_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileReading";
	private static final String URLS_TEMP_FILE_WRITING_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileWriting";
	private static final String URLS_TEMP_FILE_INPUTSTREAM_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileInputStream";
	private static final String URLS_TEMP_FILE_OUTPUTSTREAM_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileOutputStream";
	private static final String URLS_CONFIG_INPUTSTREAM_RESOURCE = "ConnectorHttpContextServices-UrlsConfigInputStream";

	private static final String WRITE_URLS_TO_FILE_RESOURCE = "InMemoryFetchedUrlsList-WriteUrlsToFile";

	ESSchemasRecordsServices es;

	public static final Set<String> dirtyContexts = (Set) Collections.synchronizedSet(new HashSet<>());

	public ConnectorHttpContextServices(ESSchemasRecordsServices es) {
		this.es = es;
	}

	public void save(ConnectorHttpContext context) {

		if (dirtyContexts.contains(context.connectorId)) {
			dirtyContexts.remove(context.connectorId);
			File tempFile = es.getIOServices().newTemporaryFile(URLS_TEMP_FILE_WRITING_RESOURCE);
			InputStream tempFileInputStream = null;
			try {
				ContentDao contentDao = es.getModelLayerFactory().getDataLayerFactory().getContentsDao();
				saveTo(context, tempFile);

				String vaultFilePath = "connectors/" + context.getConnectorId() + "/fetchedUrls.txt";

				tempFileInputStream = es.getIOServices()
						.newBufferedFileInputStream(tempFile, URLS_TEMP_FILE_INPUTSTREAM_RESOURCE);
				//String path = "/connectors/http/" + context.getConnectorId() + "/fetchedUrls.txt";
				contentDao.add(vaultFilePath, tempFileInputStream);
				//			if (add) {
				//				configManager.add(path, tempFileInputStream);
				//			} else {
				//				String hash = configManager.getBinary(path).getHash();
				//				configManager.update(path, hash, tempFileInputStream);
				//			}

			} catch (IOException e) {
				throw new RuntimeException(e);

			} finally {
				es.getIOServices().closeQuietly(tempFileInputStream);
				es.getIOServices().deleteQuietly(tempFile);
			}
		}
	}

	private void saveTo(ConnectorHttpContext context, File file) {
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new BufferedOutputStream(
					es.getIOServices().newBufferedFileOutputStream(file, URLS_TEMP_FILE_OUTPUTSTREAM_RESOURCE)));

			outputStream.writeObject(context);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			es.getIOServices().closeQuietly(outputStream);
		}
	}

	public ConnectorHttpContext createContext(String connectorId) {

		ConnectorHttpContext connectorHttpContext = new ConnectorHttpContext(connectorId);
		ConnectorHttpContextServices.dirtyContexts.add(connectorId);
		save(connectorHttpContext);
		return connectorHttpContext;
	}

	public ConnectorHttpContext loadContext(String connectorId) {

		ContentDao contentDao = es.getModelLayerFactory().getDataLayerFactory().getContentsDao();

		String vaultFilePath = "connectors/" + connectorId + "/fetchedUrls.txt";

		ObjectInputStream binaryConfigurationInputStream = null;
		try {
			InputStream contextInputStream = contentDao.getContentInputStream(vaultFilePath, URLS_CONFIG_INPUTSTREAM_RESOURCE);
			binaryConfigurationInputStream = null;
			binaryConfigurationInputStream = new ObjectInputStream(new BufferedInputStream(contextInputStream));
			return (ConnectorHttpContext) binaryConfigurationInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			this.deleteContext(connectorId);
			throw new RuntimeException(e);
		} catch (ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
			throw new RuntimeException(contentDaoException_noSuchContent);
		} finally {
			es.getIOServices().closeQuietly(binaryConfigurationInputStream);

		}

	}

	private ConnectorHttpContext loadFrom(String connectorId, File file) {
		List<String> lines;
		try {
			lines = FileUtils.readLines(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ConnectorHttpContext fetchedUrlsList = new ConnectorHttpContext(connectorId);
		for (String line : lines) {
			fetchedUrlsList.markAsFetched(line);
		}
		return fetchedUrlsList;
	}

	public void deleteContext(String connectorId) {
		ContentDao contentDao = es.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		String vaultFilePath = "connectors/" + connectorId;
		if (contentDao.isFolderExisting(vaultFilePath)) {
			contentDao.deleteFolder(vaultFilePath);
		}
	}
}
