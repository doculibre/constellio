package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.ui.components.converters.FolderIdToContextCaptionConverter;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class LookupFolderField extends LookupRecordField {

	public LookupFolderField() {
		this(false);
	}

	public LookupFolderField(boolean writeAccess) {
		super(Folder.SCHEMA_TYPE, writeAccess);
		setItemConverter(new FolderIdToContextCaptionConverter());
	}

}
