package com.constellio.app.modules.rm.ui.components.folder;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

/**
 * Implemented:
 *
 * Folder.TYPE
 * Folder.ADMINISTRATIVE_UNIT : String
 * Folder.FILING_SPACE
 * Folder.CATEGORY_ENTERED
 * Folder.RETENTION_RULE
 * Folder.COPY_STATUS_ENTERED
 * Folder.ACTUAL_TRANSFER_DATE
 * Folder.ACTUAL_DEPOSIT_DATE
 * Folder.ACTUAL_DESTRUCTION_DATE
 *
 * Always invisible:
 * Folder.CONTAINER
 *
 * Folder.FOLDER_DEFAULT_MEDIA_TYPE should have an enum-backed CHECKBOXES input type
 *
 * @author Vincent
 */
public interface FolderForm extends Serializable {

	void reload();
	
	void commit();

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	CustomFolderField<?> getCustomField(String metadataCode);

}
