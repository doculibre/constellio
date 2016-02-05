package com.constellio.app.modules.rm.ui.components.folder.fields;

import java.util.List;

public interface CustomOptionFolderField extends CustomFolderField<String> {

	List<String> getOptions();

	void setOptions(List<String> options);

}
