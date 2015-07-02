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
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
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

	public static Resource getIcon(String fileName) {
		String iconPath;
		String defaultIconPath = ICONS_DIR + DEFAULT_VALUE + ICON_EXTENSION;
		if (fileName != null) {
			String extension = FilenameUtils.getExtension(fileName);
			if (StringUtils.isBlank(extension)) {
				extension = fileName;
			}
			iconPath = ICON_PATH_CACHE.get(extension);
			if (iconPath == null) {
				iconPath = ICONS_DIR + extension + ICON_EXTENSION;
				if (!ThemeUtils.resourceExists(iconPath)) {
					iconPath = defaultIconPath;
				}
				ICON_PATH_CACHE.put(extension, iconPath);
			}
		} else {
			iconPath = defaultIconPath;
		}
		return new ThemeResource(iconPath);
	}

	public static String getExtension(RecordVO recordVO) {
		String fileName = null;
		for (MetadataValueVO metadataValueVO : recordVO.getMetadataValues()) {
			Object value = metadataValueVO.getValue();
			if (value instanceof ContentVersionVO) {
				fileName = ((ContentVersionVO) value).getFileName();
				break;
			}
		}
		if (fileName != null) {
			return FilenameUtils.getExtension(fileName);
		} else {
			return getExtensionForRecordId(recordVO.getId());
		}
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
			return getFolderExtension(folder);
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

	public static Resource getIconForRecordId(String recordId) {
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
			return getFolderIcon(folder);
		}

		if (fileName != null) {
			return getIcon(fileName);
		} else {
			return null;
		}
	}
	
	private static String getFolderExtension(Folder folder) {
		String imgName;
		FolderStatus archivisticStatus = folder.getArchivisticStatus();
		if (archivisticStatus.isDestroyed()) {
			imgName = "dossier_gris";
		} else if (archivisticStatus.isDeposited()) {
			imgName = "dossier_mauve";
		} else if (archivisticStatus.isSemiActive()) {
			imgName = "dossier_orange";
		} else {
			imgName = "dossier";
		}
		return imgName;
	}
	
	private static Resource getFolderIcon(Folder folder) {
		String imgName = getFolderExtension(folder);
		String imgPath = IMAGES_DIR + "/commun/dossier/" + imgName + ".gif";
		return new ThemeResource(imgPath);
	}

}
