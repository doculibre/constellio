package com.constellio.model.services.contents;

import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContentConversionManager implements AutoCloseable {
	private final ContentManager contentManager;
	private final IOServices ioServices;

	private ConversionManager conversionManager;
	private File workingFolder;

	public ContentConversionManager(ModelLayerFactory modelLayerFactory) {
		contentManager = modelLayerFactory.getContentManager();
		ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		conversionManager = modelLayerFactory.getDataLayerFactory().getConversionManager();
		workingFolder = ioServices.newTemporaryFolder("ContentConversionManager");
	}

	public Content replaceContentByPDF(Content content, boolean convertToPdfA) {
		ContentVersion current = content.getCurrentVersion();
		ContentVersionDataSummary summary = convertAndUpload(current, convertToPdfA);
		ContentVersion pdfVersion = createVersion(current, summary);
		return ContentImpl.create(content.getId(), pdfVersion, content.getHistoryVersions());
	}

	public void convertContentToPDF(User user, Content content, boolean pdfA) {
		String pdfFilename = changeExtension(content.getCurrentVersion().getFilename());

		ContentVersion current = content.getCurrentVersion();
		ContentVersionDataSummary summary = convertAndUpload(current, pdfA);

		content.updateContentWithName(user, summary, true, pdfFilename);
	}

	@Override
	public void close() {
		ioServices.deleteDirectoryWithoutExpectableIOException(workingFolder);
	}

	private ContentVersionDataSummary convertAndUpload(ContentVersion current, boolean pdfA) {
		InputStream original = contentManager.getContentInputStream(current.getHash(), "ContentConversionManager-original");
		InputStream converted = null;

		File convertedFile = null;

		try {
			convertedFile = conversionManager.convertToPDF(original, current.getFilename(), workingFolder, pdfA);
			converted = ioServices.newFileInputStream(convertedFile, "ContentConversionManager-converted");
			return contentManager.upload(converted, current.getFilename()).getContentVersionDataSummary();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.closeQuietly(converted);
			ioServices.deleteQuietly(convertedFile);
		}
	}

	private ContentVersion createVersion(ContentVersion current, ContentVersionDataSummary summary) {
		String pdfFilename = changeExtension(current.getFilename());
		return new ContentVersion(summary, pdfFilename, current.getVersion(),
				current.getModifiedBy(), current.getLastModificationDateTime(), null);
	}

	private String changeExtension(String filename) {
		int start = filename.lastIndexOf('.');
		if (start < 0) {
			return filename + ".pdf";
		} else {
			return filename.substring(0, start) + ".pdf";
		}
	}
}
