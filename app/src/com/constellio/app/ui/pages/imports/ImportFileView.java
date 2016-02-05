package com.constellio.app.ui.pages.imports;

import java.io.File;
import java.util.List;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ImportFileView extends BaseView, AdminViewGroup {

	void setTotal(int newTotal);

	void setProgress(int nFinishedRecords);

	void showImportCompleteMessage();

	void setExampleFile(File exampleExcelFile);

	List<String> getSelectedCollections();

}
