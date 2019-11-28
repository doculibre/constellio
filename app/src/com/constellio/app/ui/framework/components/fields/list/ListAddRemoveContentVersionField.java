package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unchecked")
public class ListAddRemoveContentVersionField extends ListAddRemoveField<Serializable, ContentVersionUploadField> {

	private String recordId;
	private String metadata;

	public ListAddRemoveContentVersionField(String recordId, String metatadata) {
		this.recordId = recordId;
		this.metadata = metatadata;
	}

	@Override
	protected ContentVersionUploadField newAddEditField() {
		ContentVersionUploadField contentUploadField = new ContentVersionUploadField(this.recordId, this.metadata);
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
