package com.constellio.app.extensions.records;

import java.util.Collections;
import java.util.List;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetDynamicFieldMetadatasParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.extensions.records.params.IsMetadataVisibleInRecordFormParams;
import com.constellio.app.extensions.records.params.isMetadataSpecialCaseOfNotBeingInFormParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.vaadin.server.Resource;

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

	public ExtensionBooleanResult isMetadataSpecialCaseToNotBeShown(isMetadataSpecialCaseOfNotBeingInFormParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<String> getDynamicFieldMetadatas(GetDynamicFieldMetadatasParams params) {
		return Collections.emptyList();
	}

	public Resource getIconFromContent(GetIconPathParams params) {
		return null;
	}

}
