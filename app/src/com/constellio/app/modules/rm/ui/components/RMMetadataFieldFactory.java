/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;

public class RMMetadataFieldFactory extends MetadataFieldFactory {

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		String schemaTypeCode = metadata.getSchemaTypeCode();
		MetadataInputType inputType = metadata.getMetadataInputType();
		if (inputType == MetadataInputType.LOOKUP && schemaTypeCode.equals(Folder.SCHEMA_TYPE)) {
			field = new LookupFolderField();
		} else {
			field = super.build(metadata);
		}
		if (field instanceof LookupFolderField) {
			postBuild(field, metadata);
		}
		return field;
	}

}
