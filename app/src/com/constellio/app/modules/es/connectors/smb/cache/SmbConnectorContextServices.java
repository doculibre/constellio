package com.constellio.app.modules.es.connectors.smb.cache;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.utils.ImpossibleRuntimeException;

import java.io.*;

public class SmbConnectorContextServices {

	private static final String URLS_TEMP_FILE_WRITING_RESOURCE = "SmbConnectorContextServices-UrlsTempFileWriting";
	private static final String URLS_TEMP_FILE_INPUTSTREAM_RESOURCE = "SmbConnectorContextServices-UrlsTempFileInputStream";
	private static final String URLS_TEMP_FILE_OUTPUTSTREAM_RESOURCE = "SmbConnectorContextServices-UrlsTempFileOutputStream";
	private static final String URLS_CONFIG_INPUTSTREAM_RESOURCE = "SmbConnectorContextServices-UrlsConfigInputStream";

	ESSchemasRecordsServices es;

	public SmbConnectorContextServices(ESSchemasRecordsServices es) {
		this.es = es;
	}

	public void save(SmbConnectorContext context) {
		save(context, false);
	}

	private void save(SmbConnectorContext context, boolean add) {
		File tempFile = es.getIOServices().newTemporaryFile(URLS_TEMP_FILE_WRITING_RESOURCE);
		InputStream tempFileInputStream = null;
		try {
			ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
			saveTo(context, tempFile);

			tempFileInputStream = es.getIOServices().newBufferedFileInputStream(tempFile, URLS_TEMP_FILE_INPUTSTREAM_RESOURCE);
			String path = "/connectors/smb/" + context.getConnectorId() + "/fetchedUrls.txt";
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

	private void saveTo(SmbConnectorContext context, File file) {
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

	public SmbConnectorContext createContext(String connectorId) {
		SmbConnectorContext connectorSmbContext = new SmbConnectorContext(connectorId);
		save(connectorSmbContext, true);
		return connectorSmbContext;
	}

	public SmbConnectorContext loadContext(String connectorId) {
		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		String path = "/connectors/smb/" + connectorId + "/fetchedUrls.txt";
		BinaryConfiguration binaryConfiguration = configManager.getBinary(path);
		ObjectInputStream binaryConfigurationInputStream = null;

		try {
			binaryConfigurationInputStream = new ObjectInputStream(new BufferedInputStream(
					binaryConfiguration.getInputStreamFactory().create(URLS_CONFIG_INPUTSTREAM_RESOURCE)));
			return (SmbConnectorContext) binaryConfigurationInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);

		} finally {
			es.getIOServices().closeQuietly(binaryConfigurationInputStream);
		}
	}

	public void deleteContext(String connectorId) {
		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		String path = "/connectors/smb/" + connectorId + "/fetchedUrls.txt";
		configManager.delete(path);
	}
}
