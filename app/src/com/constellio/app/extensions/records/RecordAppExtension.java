package com.constellio.app.extensions.records;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.extensions.records.params.IsMetadataVisibleInRecordFormParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class RecordAppExtension {

	public void buildRecordVO(BuildRecordVOParams params) {

	}

	public String getIconPathForRecord(GetIconPathParams params) {
		return null;
	}

	public void notifyFolderDeleted(FolderDeletionEvent event) {
	}

	public ExtensionBooleanResult isMetadataVisibleInRecordForm(IsMetadataVisibleInRecordFormParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
