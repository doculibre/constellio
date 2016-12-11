package com.constellio.app.extensions.api.cmis;

import com.constellio.app.extensions.api.cmis.params.AllowableActionsParams;
import com.constellio.app.extensions.api.cmis.params.CheckInParams;
import com.constellio.app.extensions.api.cmis.params.CheckOutParams;
import com.constellio.app.extensions.api.cmis.params.CreateDocumentParams;
import com.constellio.app.extensions.api.cmis.params.CreateFolderParams;
import com.constellio.app.extensions.api.cmis.params.DeleteContentParams;
import com.constellio.app.extensions.api.cmis.params.DeleteTreeParams;
import com.constellio.app.extensions.api.cmis.params.GetObjectParams;
import com.constellio.app.extensions.api.cmis.params.UpdateDocumentParams;
import com.constellio.app.extensions.api.cmis.params.UpdateFolderParams;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;

public class CmisExtension {

	public void buildCMISObjectFromConstellioRecord(BuildCmisObjectFromConstellioRecordParams params) {

	}

	public void buildConstellioRecordFromCmisObject(BuildConstellioRecordFromCmisObjectParams params) {

	}

	public void buildAllowableActions(AllowableActionsParams params) {

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

	public void onDeleteTree(DeleteTreeParams params) {

	}
	//
	//	public void onDeleteContent(DeleteContentParams params) {
	//
	//	}

}
