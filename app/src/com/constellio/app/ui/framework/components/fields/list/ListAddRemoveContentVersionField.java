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
package com.constellio.app.ui.framework.components.fields.list;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("unchecked")
public class ListAddRemoveContentVersionField extends ListAddRemoveField<Serializable, ContentVersionUploadField> {

	@Override
	protected ContentVersionUploadField newAddEditField() {
		ContentVersionUploadField contentUploadField = new ContentVersionUploadField();
		contentUploadField.setMultiValue(true);
		return contentUploadField;
	}

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected boolean isEditPossible() {
		return false;
	}

	@Override
	protected void addValue(Serializable value) {
		if (value instanceof List) {
			List<ContentVersionVO> contentVersionVOs = (List<ContentVersionVO>) value;
			for (ContentVersionVO contentVersionVO : contentVersionVOs) {
				super.addValue(contentVersionVO);
			}
		}
	}

}
