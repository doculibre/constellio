package com.constellio.app.modules.es.connectors.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class ConnectorHttpContextServices {

	private static final String URLS_TEMP_FILE_READING_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileReading";
	private static final String URLS_TEMP_FILE_WRITING_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileWriting";
	private static final String URLS_TEMP_FILE_INPUTSTREAM_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileInputStream";
	private static final String URLS_TEMP_FILE_OUTPUTSTREAM_RESOURCE = "ConnectorHttpContextServices-UrlsTempFileOutputStream";
	private static final String URLS_CONFIG_INPUTSTREAM_RESOURCE = "ConnectorHttpContextServices-UrlsConfigInputStream";

	private static final String WRITE_URLS_TO_FILE_RESOURCE = "InMemoryFetchedUrlsList-WriteUrlsToFile";

	ESSchemasRecordsServices es;

	public ConnectorHttpContextServices(ESSchemasRecordsServices es) {
		this.es = es;
	}

	public void save(ConnectorHttpContext context) {
		save(context, false);
	}

	private void save(ConnectorHttpContext context, boolean add) {
		File tempFile = es.getIOServices().newTemporaryFile(URLS_TEMP_FILE_WRITING_RESOURCE);
		InputStream tempFileInputStream = null;
		try {
			ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
			saveTo(context, tempFile);

			tempFileInputStream = es.getIOServices().newBufferedFileInputStream(tempFile, URLS_TEMP_FILE_INPUTSTREAM_RESOURCE);
			String path = "/connectors/http/" + context.getConnectorId() + "/fetchedUrls.txt";
			if (add) {
				configManager.add(path, tempFileInputStream);
			} else {
				String hash = configManager.getBinary(path).getHash();
				configManager.update(path, hash, tempFileInputStream);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (OptimisticLockingConfiguration e) {
			throw new ImpossibleRuntimeException(e);
		} finally {
			es.getIOServices().closeQuietly(tempFileInputStream);
			es.getIOServices().deleteQuietly(tempFile);
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
		save(connectorHttpContext, true);
		return connectorHttpContext;
	}

	public ConnectorHttpContext loadContext(String connectorId) {

		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		String path = "/connectors/http/" + connectorId + "/fetchedUrls.txt";
		BinaryConfiguration binaryConfiguration = configManager.getBinary(path);
		ObjectInputStream binaryConfigurationInputStream = null;

		try {
			binaryConfigurationInputStream = new ObjectInputStream(new BufferedInputStream(
					binaryConfiguration.getInputStreamFactory().create(URLS_CONFIG_INPUTSTREAM_RESOURCE)));
			return (ConnectorHttpContext) binaryConfigurationInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);

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
		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		String path = "/connectors/http/" + connectorId + "/fetchedUrls.txt";
		configManager.delete(path);
	}
}
