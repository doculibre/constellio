package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.model.entities.EnumWithSmallCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FolderDisposalTypeFieldImpl extends EnumWithSmallCodeOptionGroup<DisposalType>
		implements FolderDisposalTypeField {

	public FolderDisposalTypeFieldImpl() {
		super(DisposalType.class);
		setNullSelectionAllowed(true);
	}

	@Override
	public DisposalType getFieldValue() {
		return (DisposalType) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

	@Override
	public void setOptions(List<EnumWithSmallCode> enumConstants) {
		List<EnumWithSmallCode> adjustedOptions = new ArrayList<>(enumConstants);
		for (Iterator<EnumWithSmallCode> it = adjustedOptions.iterator(); it.hasNext(); ) {
			EnumWithSmallCode disposalType = it.next();
			if (disposalType.equals(DisposalType.SORT)) {
				it.remove();
			}
		}
		super.setOptions(adjustedOptions);
	}

}
