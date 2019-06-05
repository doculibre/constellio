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
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileIconUtils implements Serializable {

	private static Logger LOGGER = LoggerFactory.getLogger(FileIconUtils.class);

	private static final String ICON_EXTENSION = ".gif";
	private static final String IMAGES_DIR = "images";
	private static final String ICONS_DIR = IMAGES_DIR + "/icons/ext/mantis/";
	public static final String DEFAULT_VALUE = "document";

	private static final String DEFAULT_ICON_PATH = ICONS_DIR + DEFAULT_VALUE + ".gif";

	private static final Map<String, String> ICON_PATHS = new HashMap<>();

	public static Resource getIcon(String fileName) {
		if (fileName.contains("/") && ThemeUtils.resourceExists(fileName)) {
			return new ThemeResource(fileName);
		} else {
			String iconPath = getIconPath(fileName);
			return new ThemeResource(iconPath);
		}
	}
	
	public static boolean isDefaultIconPath(String iconPath) {
		return DEFAULT_ICON_PATH.equals(iconPath);
	}

	public static String getIconPath(String fileName) {
		String iconPath;
		if (fileName != null) {
			String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
			if (StringUtils.isBlank(extension)) {
				extension = fileName;
			}
			iconPath = ICON_PATHS.get(extension);
			if (iconPath == null) {
				iconPath = ICONS_DIR + extension + ICON_EXTENSION;
				if (!ThemeUtils.resourceExists(iconPath)) {
					LOGGER.warn("Resource not found : '" + iconPath + "'");
					iconPath = DEFAULT_ICON_PATH;
				}
				ICON_PATHS.put(extension, iconPath);
			}
		} else {
			iconPath = DEFAULT_ICON_PATH;
		}
		return iconPath;
	}

	public static String getExtension(RecordVO recordVO) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		String collection = recordVO.getSchema().getCollection();
		String extension = appLayerFactory.getExtensions().forCollection(collection).getExtensionForRecordVO(new GetIconPathParams(recordVO, false));
		if (extension == null) {
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
					extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
				}
			} else {
				extension = getExtensionForRecordVO(recordVO);
			}
		}
		return extension;
	}

	public static String getExtensionForRecordVO(RecordVO recordVO) {
		if (recordVO.getExtension() != null) {
			return recordVO.getExtension().toLowerCase();
		} else {
			return null;
		}
	}

	public static Resource getIcon(RecordVO recordVO) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		String collection = recordVO.getSchema().getCollection();
		Resource resource = appLayerFactory.getExtensions().forCollection(collection).getIconFromContent(new GetIconPathParams(recordVO, false));
		if (resource != null) {
			return resource;
		} else {
			return getIconForRecordVO(recordVO);
		}
	}

	@Deprecated
	public static Resource getIconForRecordId(String recordId) {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		try {
			return getIconForRecordId(recordServices.getDocumentById(recordId), false);
		} catch (Exception e) {
			return null;
		}
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

	public static Resource getIconForRecordId(Record record, boolean expanded) {
		try {
			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			String collection = record.getCollection();
			String fileName = appLayerFactory.getExtensions().forCollection(collection).getIconForRecord(
					new GetIconPathParams(record, expanded));

			if (fileName != null) {
				return getIcon(fileName);
			} else {
				return null;
			}
		} catch (Throwable t) {
			LOGGER.warn("Error while retrieving icon for record id " + record.getId(), t);
			return null;
		}
	}

	public static String getIconPathForMimeType(String mimeType)
			throws MimeTypeException {
		MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
		MimeType currentMimeType = allTypes.forName(mimeType);
		return currentMimeType.getExtension();
	}
}
