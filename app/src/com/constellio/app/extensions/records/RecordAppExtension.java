package com.constellio.app.extensions.records;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetDynamicFieldMetadatasParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.extensions.records.params.IsMetadataVisibleInRecordFormParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.vaadin.server.Resource;

import java.util.Collections;
import java.util.List;

public class RecordAppExtension {

	public void buildRecordVO(BuildRecordVOParams params) {
	}

	public String getIconPathForRecord(GetIconPathParams params) {
		return null;
	}

	public String getIconPathForRecordVO(GetIconPathParams params) {
		return null;
	}

	public String getExtensionForRecordVO(GetIconPathParams params) {
		return null;
	}

	public void notifyFolderDeleted(FolderDeletionEvent event) {
	}

	public ExtensionBooleanResult isMetadataVisibleInRecordForm(IsMetadataVisibleInRecordFormParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<String> getDynamicFieldMetadatas(GetDynamicFieldMetadatasParams params) {
		return Collections.emptyList();
	}

	public Resource getIconFromContent(GetIconPathParams params) {
		return null;
	}

}
