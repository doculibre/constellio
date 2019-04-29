package com.constellio.app.modules.rm.ui.components;

import java.util.Locale;

import com.constellio.app.modules.rm.ui.components.administrativeUnit.ListAddRemoveUserFunctionField;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.vaadin.ui.Field;

public class RMRecordFieldFactory extends RecordFieldFactory {

	public RMRecordFieldFactory() {
		super(new RMMetadataFieldFactory());
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		Field<?> field;
		if (metadataVO.codeMatches(AdministrativeUnit.FUNCTIONS)) {
			field = new ListAddRemoveUserFunctionField(recordVO);
		} else if (metadataVO.codeMatches(AdministrativeUnit.FUNCTIONS_USERS)) {
			field = null;
		} else {
			field = super.build(recordVO, metadataVO, locale);
		}
		if (field instanceof ListAddRemoveUserFunctionField) {
			postBuild(field, recordVO, metadataVO);
		}
		return field;
	}

}
