package com.constellio.app.ui.framework.components;

import java.io.Serializable;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;

public class RecordFieldFactory implements Serializable {
	
	private MetadataFieldFactory metadataFieldFactory;
	private boolean isViewOnly;

	public RecordFieldFactory() {
		this(false);
	}

	public RecordFieldFactory(boolean isViewOnly) {
		this(new MetadataFieldFactory(isViewOnly));
		this.isViewOnly = isViewOnly;
	}


	public RecordFieldFactory(MetadataFieldFactory metadataFieldFactory) {
		this.metadataFieldFactory = metadataFieldFactory;
	}
	
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		return metadataFieldFactory.build(metadataVO);
	}
	
	protected void postBuild(Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		metadataFieldFactory.postBuild(field, metadataVO);
	}

}
