package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;

public class FolderCopyStatusEnteredFieldImpl extends EnumWithSmallCodeOptionGroup<CopyType>
		implements FolderCopyStatusEnteredField {

	public FolderCopyStatusEnteredFieldImpl() {
		super(CopyType.class);
	}

	@Override
	public CopyType getFieldValue() {
		return (CopyType) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

}
