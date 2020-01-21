package com.constellio.app.modules.rm.ui.components.folder;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;

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

	Field getField(String field);

	void setFieldVisible(String metadataCode, boolean visible);

	List<Field<?>> getFields();

	Field getExtraField(String key);

	void addExtraFieldToForm(Field component, int index, String key);
}
