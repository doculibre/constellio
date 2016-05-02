package com.constellio.app.modules.rm.extensions;

import org.apache.commons.io.FilenameUtils;

import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

public class RMRecordAppExtension extends RecordAppExtension {
	
	private static final String IMAGES_DIR = "images";

	private final String collection;
	private final MetadataSchemasManager manager;

	public RMRecordAppExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		manager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	@Override
	public void buildRecordVO(BuildRecordVOParams params) {
		String resourceKey = null;
		String extension = null;
		RecordVO recordVO = params.getBuiltRecordVO();

		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			ContentVersionVO contentVersion = recordVO.getMetadataValue(recordVO.getMetadata(Document.CONTENT)).getValue();
			if (contentVersion != null) {
				resourceKey = contentVersion.getFileName();
				extension = FilenameUtils.getExtension(resourceKey);
			} else {
				resourceKey = null;
				extension = "document";
			}
			setNiceTitle(recordVO, params.getRecord(), schemaTypeCode, schemaCode, Document.DESCRIPTION);
		} else if (schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			resourceKey = getFolderIconPath(recordVO, false);
			extension = getFolderExtension(recordVO, false);
			setNiceTitle(recordVO, params.getRecord(), schemaTypeCode, schemaCode, Folder.DESCRIPTION);
		} else if (schemaTypeCode.equals(ContainerRecord.SCHEMA_TYPE)) {
			resourceKey = getContainerIconPath();
			setNiceTitle(recordVO, params.getRecord(), schemaTypeCode, schemaCode, ContainerRecord.DESCRIPTION);
		}
		if (resourceKey != null) {
			recordVO.setResourceKey(resourceKey);
		}
		if (extension != null) {
			recordVO.setExtension(extension);
		}
	}

	private void setNiceTitle(RecordVO recordVO, Record record, String schemaTypeCode, String schemaCode, String metadataCode) {
		Metadata metadata = types().getSchemaType(schemaTypeCode).getSchema(schemaCode).getMetadata(metadataCode);
		String niceTitle = record.get(metadata);
		if (niceTitle != null) {
			recordVO.setNiceTitle(niceTitle);
		}
	}

	@Override
	public String getIconPathForRecord(GetIconPathParams params) {
		String fileName = null;
		Record record = params.getRecord();
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			Document document = new Document(record, types());
			Content content = document.getContent();
			if (content != null && content.getCurrentVersion() != null) {
				fileName = content.getCurrentVersion().getFilename();
			} else {
				fileName = "document";
			}
		} else if (schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			Folder folder = new Folder(record, types());
			fileName = getFolderIconPath(folder, params.isExpanded());
		} else if (schemaTypeCode.equals(ContainerRecord.SCHEMA_TYPE)) {
			fileName = getContainerIconPath();
		}
		return fileName != null ? fileName : null;
	}

	private String getContainerIconPath() {
		return IMAGES_DIR + "/icons/container/box.png";
	}

	private String getFolderIconPath(Folder folder, boolean expanded) {
		String imgName = getFolderExtension(folder, expanded);
		return IMAGES_DIR + "/icons/folder/" + imgName + ".png";
	}

	private String getFolderIconPath(RecordVO recordVO, boolean expanded) {
		String imgName = getFolderExtension(recordVO, expanded);
		return IMAGES_DIR + "/icons/folder/" + imgName + ".png";
	}

	private String getFolderExtension(RecordVO recordVO, boolean expanded) {
		FolderStatus archivisticStatus = recordVO.getMetadataValue(recordVO.getMetadata(Folder.ARCHIVISTIC_STATUS)).getValue();
		return getArchivisticStatusFilename(expanded, archivisticStatus);
	}

	private String getFolderExtension(Folder folder, boolean expanded) {
		FolderStatus archivisticStatus = folder.getArchivisticStatus();
		return getArchivisticStatusFilename(expanded, archivisticStatus);
	}

	private String getArchivisticStatusFilename(boolean expanded, FolderStatus archivisticStatus) {
		String imgName;
		if (archivisticStatus != null) {
			if (archivisticStatus.isDestroyed()) {
				imgName = expanded ? "folder_open_grey" : "folder_grey";
			} else if (archivisticStatus.isDeposited()) {
				imgName = expanded ? "folder_open_purple" : "folder_purple";
			} else if (archivisticStatus.isSemiActive()) {
				imgName = expanded ? "folder_open_orange" : "folder_orange";
			} else {
				imgName = expanded ? "folder_open" : "folder";
			}
		} else {
			imgName = expanded ? "folder_open" : "folder";
		}
		return imgName;
	}

	private MetadataSchemaTypes types() {
		return manager.getSchemaTypes(collection);
	}

}
