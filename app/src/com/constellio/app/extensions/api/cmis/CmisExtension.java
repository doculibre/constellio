package com.constellio.app.extensions.api.cmis;

import com.constellio.app.extensions.api.cmis.params.BuildAllowableActionsParams;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;
import com.constellio.app.extensions.api.cmis.params.CheckInParams;
import com.constellio.app.extensions.api.cmis.params.CheckOutParams;
import com.constellio.app.extensions.api.cmis.params.GetObjectParams;
import com.constellio.app.extensions.api.cmis.params.IsSchemaTypeSupportedParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class CmisExtension {

	public void buildCMISObjectFromConstellioRecord(BuildCmisObjectFromConstellioRecordParams params) {

	}

	public void buildConstellioRecordFromCmisObject(BuildConstellioRecordFromCmisObjectParams params) {

	}

	public void buildAllowableActions(BuildAllowableActionsParams params) {

	}

	public void onGetObject(GetObjectParams params) {

	}

	public void onCheckIn(CheckInParams params) {

	}

	public void onCheckOut(CheckOutParams params) {

	}
	//
	//	public void onCreateCMISFolder(CreateFolderParams params) {
	//
	//	}
	//
	//	public void onCreateCMISDocument(CreateDocumentParams params) {
	//
	//	}
	//
	//	public void onUpdateCMISFolder(UpdateFolderParams params) {
	//
	//	}
	//
	//	public void onUpdateCMISDocument(UpdateDocumentParams params) {
	//
	//	}
	//
	//	public void onDeleteContent(DeleteContentParams params) {
	//
	//	}
	public ExtensionBooleanResult isSchemaTypeSupported(IsSchemaTypeSupportedParams params) {
		return null;
	}

}
