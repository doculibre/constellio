package com.constellio.app.ui.util;

import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class FileIconUtils implements Serializable {

	private static Logger LOGGER = LoggerFactory.getLogger(FileIconUtils.class);

	private static final String ICON_EXTENSION = ".gif";
	private static final String IMAGES_DIR = "images";
	private static final String ICONS_DIR = IMAGES_DIR + "/icons/ext/mantis/";
	private static final String DEFAULT_VALUE = "document";

	private static final String DEFAULT_ICON_PATH = ICONS_DIR + DEFAULT_VALUE + ".gif";

	public static Resource getIcon(String fileName) {
		if (fileName.contains("/") && ThemeUtils.resourceExists(fileName)) {
			return new ThemeResource(fileName);
		} else {
			String iconPath = getIconPath(fileName);
			return new ThemeResource(iconPath);
		}
	}

	public static String getIconPath(String fileName) {
		String iconPath;
		if (fileName != null) {
			String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
			if (StringUtils.isBlank(extension)) {
				extension = fileName;
			}
			iconPath = ICONS_DIR + extension + ICON_EXTENSION;
			if (!ThemeUtils.resourceExists(iconPath)) {
				LOGGER.warn("Resource not found : '" + iconPath + "'");
				iconPath = DEFAULT_ICON_PATH;
			}
		} else {
			iconPath = DEFAULT_ICON_PATH;
		}
		return iconPath;
	}

	public static String getExtension(RecordVO recordVO) {
		String extension;
		String fileName = null;
		for (MetadataValueVO metadataValueVO : recordVO.getMetadataValues()) {
			Object value = metadataValueVO.getValue();
			if (value instanceof ContentVersionVO) {
				fileName = ((ContentVersionVO) value).getFileName();
				break;
			}
		}
		if (fileName != null) {
			String iconPath = getIconPath(fileName);
			if (DEFAULT_ICON_PATH.equals(iconPath)) {
				extension = DEFAULT_VALUE;
			} else {
				extension = FilenameUtils.getExtension(fileName);
			}
		} else {
			extension = getExtensionForRecordVO(recordVO);
		}
		return extension;
	}

	public static String getExtensionForRecordVO(RecordVO recordVO) {
		if (recordVO.getExtension() != null) {
			return recordVO.getExtension();
		} else {
			return null;
		}
	}

	public static Resource getIcon(RecordVO recordVO) {
		String fileName = null;
		for (MetadataValueVO metadataValueVO : recordVO.getMetadataValues()) {
			Object value = metadataValueVO.getValue();
			if (value instanceof ContentVersionVO) {
				fileName = ((ContentVersionVO) value).getFileName();
				break;
			}
		}
		if (fileName != null) {
			return getIcon(fileName);
		} else {
			return getIconForRecordVO(recordVO);
		}
	}

	public static Resource getIconForRecordId(String recordId) {
		return getIconForRecordId(recordId, false);
	}

	public static Resource getIconForRecordVO(RecordVO recordVO) {
		if (StringUtils.isBlank(recordVO.getResourceKey())) {
			return null;
		}
		if (recordVO.getResourceKey() != null) {
			return getIcon(recordVO.getResourceKey());
		} else {
			return null;
		}
	}

	public static Resource getIconForRecordId(String recordId, boolean expanded) {
		try {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			Record record = recordServices.getDocumentById(recordId);
			String collection = record.getCollection();
			String fileName = appLayerFactory.getExtensions().forCollection(collection).getIconForRecord(
					new GetIconPathParams(record, expanded));

			if (fileName != null) {
				return getIcon(fileName);
			} else {
				return null;
			}
		} catch (Throwable t) {
			LOGGER.warn("Error while retrieving icon for record id " + recordId, t);
			return null;
		}
	}
}
