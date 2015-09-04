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
package com.constellio.app.ui.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordDataTreeNode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public class FileIconUtils implements Serializable {

	private static final String ICON_EXTENSION = ".gif";
	private static final String IMAGES_DIR = "images";
	private static final String ICONS_DIR = IMAGES_DIR + "/icons/ext/mantis/";
	private static final String DEFAULT_VALUE = "document";

	private static final Map<String, String> ICON_PATH_CACHE = new HashMap<String, String>();
	
	private static final String DEFAULT_ICON_PATH = ICONS_DIR + DEFAULT_VALUE + ".gif";

	public static Resource getIcon(String fileName) {
		String iconPath = getIconPath(fileName);
		return new ThemeResource(iconPath);
	}
	
	private static String getIconPath(String fileName) {
		String iconPath;
		if (fileName != null) {
			String extension = FilenameUtils.getExtension(fileName);
			if (StringUtils.isBlank(extension)) {
				extension = fileName;
			}
			iconPath = ICON_PATH_CACHE.get(extension);
			if (iconPath == null) {
				iconPath = ICONS_DIR + extension + ICON_EXTENSION;
				if (!ThemeUtils.resourceExists(iconPath)) {
					iconPath = DEFAULT_ICON_PATH;
				}
				ICON_PATH_CACHE.put(extension, iconPath);
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
			extension = getExtensionForRecordId(recordVO.getId());
		}
		return extension;
	}

	public static String getExtensionForRecordId(String recordId) {
		String fileName = null;

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		Record record = recordServices.getDocumentById(recordId);
		String collection = record.getCollection();
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		// FIXME Remove references to RM module
		if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			Document document = new Document(record, metadataSchemaTypes);
			Content content = document.getContent();
			if (content != null && content.getCurrentVersion() != null) {
				fileName = content.getCurrentVersion().getFilename();
			} else {
				return DEFAULT_VALUE;
			}
		} else if (schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			Folder folder = new Folder(record, metadataSchemaTypes);
			return getFolderExtension(folder, false);
		} else if (schemaTypeCode.equals(Task.SCHEMA_TYPE)) {
			return "task";
		}

		if (fileName != null) {
			return FilenameUtils.getExtension(fileName);
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
			return getIconForRecordId(recordVO.getId());
		}
	}

	public static Resource getIconForRecordId(String collection, RecordDataTreeNode node) {
		String fileName = null;

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		String schemaTypeCode = node.getSchemaType();
		// FIXME Remove references to RM module
		if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			//			Document document = new Document(record, metadataSchemaTypes);
			//			Content content = document.getContent();
			//			if (content != null && content.getCurrentVersion() != null) {
			//				fileName = content.getCurrentVersion().getFilename();
			//			} else {
			return getIcon((String) null);
			//			}
		} else if (schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			//MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			return getDefaultFolderIcon();
		} else if (schemaTypeCode.equals(ContainerRecord.SCHEMA_TYPE)) {
			return getDefaultContainerIcon();
		} else if (schemaTypeCode.equals(Task.SCHEMA_TYPE)) {
			return getDefaultTaskIcon();
		}

		if (fileName != null) {
			return getIcon(fileName);
		} else {
			return null;
		}

	}

	public static Resource getIconForRecordId(String recordId) {
		return getIconForRecordId(recordId, false);
	}

	public static Resource getIconForRecordId(String recordId, boolean expanded) {
		if (StringUtils.isBlank(recordId)) {
			return null;
		}
		String fileName = null;

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		Record record = recordServices.getDocumentById(recordId);
		String collection = record.getCollection();
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		// FIXME Remove references to RM module
		if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			Document document = new Document(record, metadataSchemaTypes);
			Content content = document.getContent();
			if (content != null && content.getCurrentVersion() != null) {
				fileName = content.getCurrentVersion().getFilename();
			} else {
				return getIcon((String) null);
			}
		} else if (schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			Folder folder = new Folder(record, metadataSchemaTypes);
			return getFolderIcon(folder, expanded);
		} else if (schemaTypeCode.equals(ContainerRecord.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			ContainerRecord container = new ContainerRecord(record, metadataSchemaTypes);
			return getContainerIcon(container, expanded);
		} else if (schemaTypeCode.equals(Task.SCHEMA_TYPE)) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			Task task = new Task(record, metadataSchemaTypes);
			return getTaskIcon(task, expanded);
		}

		if (fileName != null) {
			return getIcon(fileName);
		} else {
			return null;
		}
	}

	private static String getFolderExtension(Folder folder, boolean expanded) {
		String imgName;
		FolderStatus archivisticStatus = folder.getArchivisticStatus();
		if (archivisticStatus.isDestroyed()) {
			imgName = expanded ? "folder_open_grey" : "folder_grey";
		} else if (archivisticStatus.isDeposited()) {
			imgName = expanded ? "folder_open_purple" : "folder_purple";
		} else if (archivisticStatus.isSemiActive()) {
			imgName = expanded ? "folder_open_orange" : "folder_orange";
		} else {
			imgName = expanded ? "folder_open" : "folder";
		}
		return imgName;
	}

	private static Resource getFolderIcon(Folder folder, boolean expanded) {
		String imgName = getFolderExtension(folder, expanded);
		String imgPath = IMAGES_DIR + "/icons/folder/" + imgName + ".png";
		return new ThemeResource(imgPath);
	}

	private static Resource getDefaultFolderIcon() {
		String imgPath = IMAGES_DIR + "/icons/folder/folder.png";
		return new ThemeResource(imgPath);
	}

	private static String getTaskExtension(Task task, boolean expanded) {
		return "task";
	}

	private static Resource getTaskIcon(Task task, boolean expanded) {
		String imgName = getTaskExtension(task, expanded);
		String imgPath = IMAGES_DIR + "/icons/task/" + imgName + ".png";
		return new ThemeResource(imgPath);
	}

	private static Resource getDefaultTaskIcon() {
		String imgPath = IMAGES_DIR + "/icons/task/task.png";
		return new ThemeResource(imgPath);
	}

	private static String getContainerExtension(ContainerRecord container, boolean expanded) {
		return "box";
	}

	private static Resource getContainerIcon(ContainerRecord container, boolean expanded) {
		String imgName = getContainerExtension(container, expanded);
		String imgPath = IMAGES_DIR + "/icons/container/" + imgName + ".png";
		return new ThemeResource(imgPath);
	}

	private static Resource getDefaultContainerIcon() {
		String imgPath = IMAGES_DIR + "/icons/container/box.png";
		return new ThemeResource(imgPath);
	}

}
