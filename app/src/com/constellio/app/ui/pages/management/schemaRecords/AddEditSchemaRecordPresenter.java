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
package com.constellio.app.ui.pages.management.schemaRecords;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.FieldOverridePresenter;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.SchemaUtils;

@SuppressWarnings("serial")
public class AddEditSchemaRecordPresenter extends SingleSchemaBasePresenter<AddEditSchemaRecordView>
		implements FieldOverridePresenter {
	public static final String FOLDER_TYPE_LINKED_SCHEMA = FolderType.DEFAULT_SCHEMA + "_" + FolderType.LINKED_SCHEMA;
	public static final String DOCUMENT_TYPE_LINKED_SCHEMA = DocumentType.DEFAULT_SCHEMA + "_" + DocumentType.LINKED_SCHEMA;
	public static final String CONTAINER_TYPE_LINKED_SCHEMA =
			ContainerRecordType.DEFAULT_SCHEMA + "_" + ContainerRecordType.LINKED_SCHEMA;
	public static final String STORAGE_SPACE_LINKED_SCHEMA =
			StorageSpaceType.DEFAULT_SCHEMA + "_" + StorageSpaceType.LINKED_SCHEMA;

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditSchemaRecordPresenter.class);

	public AddEditSchemaRecordPresenter(AddEditSchemaRecordView view) {
		super(view);
	}

	public void forSchema(String schemaCode) {
		setSchemaCode(schemaCode);
	}

	public RecordVO getRecordVO(String id) {
		if (StringUtils.isNotBlank(id)) {
			return presenterService().getRecordVO(id, VIEW_MODE.FORM);
		} else {
			return new RecordToVOBuilder().build(newRecord(), VIEW_MODE.FORM);
		}
	}

	public void saveButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		try {
			Record record = toRecord(recordVO);
			addOrUpdate(record);
			view.navigateTo().listSchemaRecords(schemaCode);
		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigateTo().listSchemaRecords(schemaCode);
	}

	@Override
	public OverrideMode getOverride(String metadataCode) {
		switch (metadataCode) {
		case FOLDER_TYPE_LINKED_SCHEMA:
		case DOCUMENT_TYPE_LINKED_SCHEMA:
		case CONTAINER_TYPE_LINKED_SCHEMA:
		case STORAGE_SPACE_LINKED_SCHEMA:
			return OverrideMode.DROPDOWN;
		default:
			return OverrideMode.NONE;
		}
	}

	@Override
	public List<Choice> getChoices(String metadataCode) {
		return getSchemaChoices(getLinkedSchemaType(metadataCode));
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);
	}

	private List<Choice> getSchemaChoices(String schemaTypeCode) {
		MetadataSchemaType type = types().getSchemaType(schemaTypeCode);
		List<Choice> result = new ArrayList<>();
		for (MetadataSchema schema : type.getCustomSchemas()) {
			result.add(new Choice(schema.getCode(), schema.getLabel()));
		}
		return result;
	}

	private String getLinkedSchemaType(String metadataCode) {
		switch (metadataCode) {
		case FOLDER_TYPE_LINKED_SCHEMA:
			return Folder.SCHEMA_TYPE;
		case DOCUMENT_TYPE_LINKED_SCHEMA:
			return Document.SCHEMA_TYPE;
		case CONTAINER_TYPE_LINKED_SCHEMA:
			return ContainerRecord.SCHEMA_TYPE;
		case STORAGE_SPACE_LINKED_SCHEMA:
			return StorageSpace.SCHEMA_TYPE;
		}
		return null;
	}
}
