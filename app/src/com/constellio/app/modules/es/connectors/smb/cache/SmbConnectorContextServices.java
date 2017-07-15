package com.constellio.app.modules.es.connectors.smb.cache;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.conf.FoldersLocator;
import org.apache.commons.io.FileUtils;

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

	private synchronized void save(SmbConnectorContext context, boolean add) {
		File tempFile = es.getIOServices().newTemporaryFile(URLS_TEMP_FILE_WRITING_RESOURCE);
		InputStream tempFileInputStream = null;
		try {
			File workFolder = new FoldersLocator().getWorkFolder();
			File connectorWorkFolder = new File(workFolder, context.getConnectorId());
			connectorWorkFolder.mkdirs();
			saveTo(context, tempFile);
			tempFileInputStream = es.getIOServices().newBufferedFileInputStream(tempFile, URLS_TEMP_FILE_INPUTSTREAM_RESOURCE);
			File fetchedUrls = new File(connectorWorkFolder, "fetchedUrls.txt");
			FileUtils.copyInputStreamToFile(tempFileInputStream, fetchedUrls);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
		File workFolder = new FoldersLocator().getWorkFolder();
		File connectorWorkFolder = new File(workFolder, connectorId);
		File fetchedUrls = new File(connectorWorkFolder, "fetchedUrls.txt");

		ObjectInputStream binaryConfigurationInputStream = null;

		try (FileInputStream is = FileUtils.openInputStream(fetchedUrls)) {
			binaryConfigurationInputStream = new ObjectInputStream(new BufferedInputStream(is));
			return (SmbConnectorContext) binaryConfigurationInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			es.getIOServices().closeQuietly(binaryConfigurationInputStream);
		}
	}

	public void deleteContext(String connectorId) {
		File workFolder = new FoldersLocator().getWorkFolder();
		File connectorWorkFolder = new File(workFolder, connectorId);
		File fetchedUrls = new File(connectorWorkFolder, "fetchedUrls.txt");
		FileUtils.deleteQuietly(fetchedUrls);
	}
}
