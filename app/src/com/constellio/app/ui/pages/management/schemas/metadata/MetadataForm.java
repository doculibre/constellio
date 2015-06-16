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
package com.constellio.app.ui.pages.management.schemas.metadata;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.vaadin.ui.Field;

public abstract class MetadataForm extends BaseForm<FormMetadataVO> {

	public MetadataForm(FormMetadataVO viewObject, Serializable objectWithMemberFields, Field... fields) {
		super(viewObject, objectWithMemberFields, fields);
	}

	public MetadataForm(FormMetadataVO viewObject, List fieldsAndPropertyIds) {
		super(viewObject, fieldsAndPropertyIds);
	}

	public void reload() {
	}

	public void commit() {
	}
}
