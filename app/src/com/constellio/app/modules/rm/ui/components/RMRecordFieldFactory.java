package com.constellio.app.modules.rm.ui.components;

import java.util.Locale;

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
		return super.build(recordVO, metadataVO, locale);
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
