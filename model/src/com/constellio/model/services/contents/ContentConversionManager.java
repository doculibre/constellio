/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.contents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ContentConversionManager implements AutoCloseable {
	private final ContentManager contentManager;
	private final IOServices ioServices;

	private ConversionManager conversionManager;
	private File workingFolder;

	public ContentConversionManager(ModelLayerFactory modelLayerFactory) {
		contentManager = modelLayerFactory.getContentManager();
		ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
	}

	public Content replaceContentByPDFA(Content content) {
		ContentVersion current = content.getCurrentVersion();
		ContentVersionDataSummary summary = convertAndUpload(current);
		ContentVersion pdfVersion = createVersion(current, summary);
		return ContentImpl.create(content.getId(), pdfVersion, content.getHistoryVersions());
	}

	public void convertContentToPDFA(User user, Content content) {
		String pdfFilename = changeExtension(content.getCurrentVersion().getFilename());

		ContentVersion current = content.getCurrentVersion();
		ContentVersionDataSummary summary = convertAndUpload(current);

		content.updateContentWithName(user, summary, true, pdfFilename);
	}

	@Override
	public void close() {
		if (conversionManager != null) {
			conversionManager.close();
			ioServices.deleteDirectoryWithoutExpectableIOException(workingFolder);
		}
	}

	private ContentVersionDataSummary convertAndUpload(ContentVersion current) {
		InputStream original = contentManager.getContentInputStream(current.getHash(), "ContentConversionManager-original");
		InputStream converted = null;

		File convertedFile = null;

		try {
			convertedFile = conversionManager().convertToPDF(original, current.getFilename());
			converted = ioServices.newFileInputStream(convertedFile, "ContentConversionManager-converted");
			return contentManager.upload(converted);
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
				current.getModifiedBy(), current.getLastModificationDateTime());
	}

	private String changeExtension(String filename) {
		int start = filename.lastIndexOf('.');
		if (start < 0) {
			return filename + ".pdf";
		} else {
			return filename.substring(0, start) + ".pdf";
		}
	}

	private ConversionManager conversionManager() {
		if (conversionManager == null) {
			workingFolder = ioServices.newTemporaryFolder("ContentConversionManager");
			conversionManager = new ConversionManager(ioServices, 1, workingFolder);
		}
		return conversionManager;
	}
}
