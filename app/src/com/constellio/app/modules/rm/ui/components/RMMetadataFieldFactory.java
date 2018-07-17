package com.constellio.app.modules.rm.ui.components;

import java.util.Locale;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;

public class RMMetadataFieldFactory extends MetadataFieldFactory {

	@Override
	public Field<?> build(MetadataVO metadata, Locale locale) {
		Field<?> field;
		String schemaTypeCode = metadata.getSchemaTypeCode();
		MetadataInputType inputType = metadata.getMetadataInputType();
		if (inputType == MetadataInputType.LOOKUP && schemaTypeCode.equals(Folder.SCHEMA_TYPE) && !metadata.isMultivalue()) {
			field = new LookupFolderField();
		} else {
			field = super.build(metadata, locale);
		}
		if (field instanceof LookupFolderField) {
			postBuild(field, metadata);
		}
		return field;
	}

}
