package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.ui.components.RMRecordFieldFactory;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;

public class RMAdministrativeUnitRecordFieldFactoryExtension extends RecordFieldFactoryExtension {

	@Override
	public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams params) {
		RecordFieldFactory result;
		RecordVO recordVO = params.getRecordVO();
		String schemaType = recordVO.getSchema().getTypeCode();
		if (AdministrativeUnit.SCHEMA_TYPE.equals(schemaType)) {
			result = new RMRecordFieldFactory();
		} else {
			result = null;
		}
		return result;
	}

}
