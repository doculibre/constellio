package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;

public class ListSchemaExtraCommandParams {
	MetadataSchemaVO schemaVO;
	BaseViewImpl view;

	public ListSchemaExtraCommandParams(MetadataSchemaVO schemaVO, BaseViewImpl view) {
		this.schemaVO = schemaVO;
		this.view = view;
	}

	public MetadataSchemaVO getSchemaVO() {
		return schemaVO;
	}

	public BaseViewImpl getView() {
		return view;
	}
}
