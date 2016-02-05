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
