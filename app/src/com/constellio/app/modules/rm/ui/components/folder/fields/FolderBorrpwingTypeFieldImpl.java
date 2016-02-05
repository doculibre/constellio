package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;

public class FolderBorrpwingTypeFieldImpl extends EnumWithSmallCodeOptionGroup<BorrowingType>
		implements FolderBorrowingTypeField {

	public FolderBorrpwingTypeFieldImpl() {
		super(BorrowingType.class);
	}

	@Override
	public BorrowingType getFieldValue() {
		return (BorrowingType) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

}
