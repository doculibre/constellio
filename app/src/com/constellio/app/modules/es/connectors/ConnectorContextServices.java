package com.constellio.app.modules.es.connectors;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.ConfigurationAlreadyExists;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class ConnectorContextServices {
	private static final String CONTEXT_TEMP_FILE_WRITING_RESOURCE = "ConnectorContextServices-ContextTempFileWriting";
	private static final String CONTEXT_TEMP_FILE_INPUT_STREAM_RESOURCE = "ConnectorContextServices-ContextTempFileInputStream";
	private static final String CONTEXT_TEMP_FILE_OUTPUT_STREAM_RESOURCE = "ConnectorContextServices-ContextTempFileOutputStream";
	private static final String CONTEXT_CONFIG_INPUT_STREAM_RESOURCE = "ConnectorContextServices-ContextConfigInputStream";

	final String connectorType;
	ESSchemasRecordsServices es;

	public ConnectorContextServices(ESSchemasRecordsServices es, String connectorType) {
		this.es = es;
		this.connectorType = connectorType;
	}

	public void save(String connectorId, Object context) {
		save(connectorId, context, false);
	}

	private void save(String connectorId, Object context, boolean add) {
		File tempFile = es.getIOServices().newTemporaryFile(CONTEXT_TEMP_FILE_WRITING_RESOURCE);
		InputStream tempFileInputStream = null;
		try {
			ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
			saveTo(context, tempFile);

			tempFileInputStream = es.getIOServices()
					.newBufferedFileInputStream(tempFile, CONTEXT_TEMP_FILE_INPUT_STREAM_RESOURCE);
			String path = "/connectors/" + connectorType + "/" + connectorId + "/context.txt";
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

	private void saveTo(Object context, File file) {
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new BufferedOutputStream(
					es.getIOServices().newBufferedFileOutputStream(file, CONTEXT_TEMP_FILE_OUTPUT_STREAM_RESOURCE)));

			outputStream.writeObject(context);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			es.getIOServices().closeQuietly(outputStream);
		}
	}

	public Object createContext(String connectorId, Object context) {
		save(connectorId, context, true);
		return context;
	}

	public Object forceCreateContext(String connectorId, Object context) {
		try {
			return createContext(connectorId, context);

		} catch (ConfigurationAlreadyExists e) {
			deleteContext(connectorId);
			return createContext(connectorId, context);
		}
	}



	public Object loadContext(String connectorId) {
		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		String path = "/connectors/" + connectorType + "/" + connectorId + "/context.txt";
		BinaryConfiguration binaryConfiguration = configManager.getBinary(path);
		ObjectInputStream binaryConfigurationInputStream = null;

		try {
			binaryConfigurationInputStream = new ObjectInputStream(new BufferedInputStream(
					binaryConfiguration.getInputStreamFactory().create(CONTEXT_CONFIG_INPUT_STREAM_RESOURCE)));
			return binaryConfigurationInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);

		} finally {
			es.getIOServices().closeQuietly(binaryConfigurationInputStream);
		}
	}

	public void deleteContext(String connectorId) {
		ConfigManager configManager = es.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		String path = "/connectors/" + connectorType + "/" + connectorId + "/context.txt";
		configManager.delete(path);
	}

}
