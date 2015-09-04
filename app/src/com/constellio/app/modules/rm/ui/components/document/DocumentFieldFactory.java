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
package com.constellio.app.modules.rm.ui.components.document;

import static com.constellio.app.modules.rm.wrappers.Document.*;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.ui.components.RMMetadataFieldFactory;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeFieldLookupImpl;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.ui.entities.MetadataVO;
import com.vaadin.ui.Field;

public class DocumentFieldFactory extends RMMetadataFieldFactory {

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		String metadataCode = metadata.getCode();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		if (TYPE.equals(metadataCode) || TYPE.equals(metadataCodeWithoutPrefix)) {
			field = new DocumentTypeFieldLookupImpl();
		} else if (CONTENT.equals(metadataCode) || CONTENT.equals(metadataCodeWithoutPrefix)) {	
			field = new DocumentContentFieldImpl();
		} else {
			field = super.build(metadata);
		}
		if (field instanceof CustomDocumentField) {
			postBuild(field, metadata);
		}
		return field;
	}

	@Override
	protected void postBuild(Field<?> field, MetadataVO metadata) {
		super.postBuild(field, metadata);

		String schemaCode = metadata.getSchema().getCode();
		if (Email.SCHEMA.equals(schemaCode)) {
			List<String> readOnlyMetadataCodes = Arrays.asList(Email.EMAIL_ATTACHMENTS_LIST, Email.EMAIL_BCC_TO,
					Email.EMAIL_CC_TO, Email.EMAIL_CONTENT, Email.EMAIL_FROM, Email.EMAIL_IN_NAME_OF, Email.EMAIL_OBJECT,
					Email.EMAIL_RECEIVED_ON, Email.EMAIL_RECEIVED_ON, Email.EMAIL_SENT_ON, Email.EMAIL_TO);
			String metadataCode = metadata.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (readOnlyMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				field.setEnabled(false);
			}
		}
	}

}
