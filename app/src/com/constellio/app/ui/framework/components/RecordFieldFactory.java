package com.constellio.app.ui.framework.components;

import java.io.Serializable;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;

public class RecordFieldFactory implements Serializable {
	
	private MetadataFieldFactory metadataFieldFactory;
	
	public RecordFieldFactory() {
		this(new MetadataFieldFactory());
	}

	public RecordFieldFactory(MetadataFieldFactory metadataFieldFactory) {
		this.metadataFieldFactory = metadataFieldFactory;
	}
	
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		return metadataFieldFactory.build(metadataVO);
	}
	
	public void postBuild(Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		metadataFieldFactory.postBuild(field, metadataVO);
	}

}
