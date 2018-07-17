package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.Locale;

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
	
	public final Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		return build(recordVO, metadataVO, null);
	}
	
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		return metadataFieldFactory.build(metadataVO, locale);
	}
	
	protected void postBuild(Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		metadataFieldFactory.postBuild(field, metadataVO);
	}

}
