package com.constellio.app.modules.rm.ui.components.folder;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

import java.io.Serializable;

/**
 * Implemented:
 * <p>
 * Folder.TYPE
 * Folder.ADMINISTRATIVE_UNIT : String
 * Folder.FILING_SPACE
 * Folder.CATEGORY_ENTERED
 * Folder.RETENTION_RULE
 * Folder.COPY_STATUS_ENTERED
 * Folder.ACTUAL_TRANSFER_DATE
 * Folder.ACTUAL_DEPOSIT_DATE
 * Folder.ACTUAL_DESTRUCTION_DATE
 * <p>
 * Always invisible:
 * Folder.CONTAINER
 * <p>
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

	void setFieldVisible(String metadataCode, boolean visible);

}
