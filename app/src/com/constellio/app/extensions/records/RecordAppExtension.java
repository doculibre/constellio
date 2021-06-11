package com.constellio.app.extensions.records;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetDynamicFieldMetadatasParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.extensions.records.params.GetRecordsToSaveInSameTransactionAsParentRecordParams;
import com.constellio.app.extensions.records.params.IsMetadataSpecialCaseToNotBeShownParams;
import com.constellio.app.extensions.records.params.IsMetadataVisibleInRecordFormParams;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.vaadin.server.Resource;

import java.util.Collections;
import java.util.List;

public class RecordAppExtension {

	public List<MetadataValueVO> addSyntheticMetadataValues(AddSyntheticMetadataValuesParams params) {
		return null;
	}

	public List<Record> getRecordsToSaveInSameTransactionAsParentRecord(
			GetRecordsToSaveInSameTransactionAsParentRecordParams params) {
		return null;
	}

	public void buildRecordVO(BuildRecordVOParams params) {
	}

	public String getIconPathForRecord(GetIconPathParams params) {
		return null;
	}

	public String getIconPathForRecordVO(GetIconPathParams params) {
		return null;
	}

	public Resource getThumbnailResourceForRecordVO(GetIconPathParams params) {
		return null;
	}

	public List<String> getThumbnailStylesForRecordVO(GetIconPathParams params) {
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

	public ExtensionBooleanResult isMetadataSpecialCaseToNotBeShown(IsMetadataSpecialCaseToNotBeShownParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<String> getDynamicFieldMetadatas(GetDynamicFieldMetadatasParams params) {
		return Collections.emptyList();
	}

	public Resource getIconFromContent(GetIconPathParams params) {
		return null;
	}

}
