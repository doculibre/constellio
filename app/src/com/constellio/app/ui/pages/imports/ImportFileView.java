package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

import java.io.File;
import java.util.List;

public interface ImportFileView extends BaseView, AdminViewGroup {

	void setTotal(int newTotal);

	void setProgress(int nFinishedRecords);

	void showImportCompleteMessage();

	void setExampleFile(File exampleFile);

	List<String> getSelectedCollections();

	ImportFileMode getImportFileMode();

	boolean isAllowingReferencesToNonExistingUsers();

	boolean isMergeExistingRecordWithSameLegacyId();
}
