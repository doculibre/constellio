package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;

public class RecordFieldFactoryPostBuildExtensionParams {

	private MetadataFieldFactory metadataFieldFactory;
	private Field<?> field;
	private RecordVO recordVO;
	private MetadataVO metadataVO;

	public RecordFieldFactoryPostBuildExtensionParams(MetadataFieldFactory metadataFieldFactory, Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		this.metadataFieldFactory = metadataFieldFactory;
		this.field = field;
		this.recordVO = recordVO;
		this.metadataVO = metadataVO;
	}

	public MetadataFieldFactory getMetadataFieldFactory() {
		return metadataFieldFactory;
	}

	public Field<?> getField() {
		return field;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public MetadataVO getMetadataVO() {
		return metadataVO;
	}

}
