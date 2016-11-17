package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.vaadin.ui.Field;

public class RMRecordFieldFactory extends RecordFieldFactory {

	public RMRecordFieldFactory() {
		super(new RMMetadataFieldFactory());
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		return super.build(recordVO, metadataVO);
	}

//	@Override
//	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
//		Field<?> field;
//		String schemaTypeCode = metadataVO.getSchemaTypeCode();
//		MetadataInputType inputType = metadataVO.getMetadataInputType();
//		if (inputType == MetadataInputType.LOOKUP && schemaTypeCode.equals(Folder.SCHEMA_TYPE) && !metadataVO.isMultivalue()) {
//			field = new LookupFolderField();
//		} else {
//			field = super.build(recordVO, metadataVO);
//		}
//		if (field instanceof LookupFolderField) {
//			postBuild(field, recordVO, metadataVO);
//		}
//		return field;
//	}
	
}
