package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.Locale;

import com.constellio.app.api.extensions.params.MetadataFieldExtensionParams;
import com.constellio.app.api.extensions.params.RecordFieldFactoryPostBuildExtensionParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
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

	public MetadataFieldFactory getMetadataFieldFactory() {
		return metadataFieldFactory;
	}

	//Do not call as super when overwriting function with Locale
	public final Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		return build(recordVO, metadataVO, null);
	}

	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		String collection = recordVO.getSchema().getCollection();
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		AppLayerCollectionExtensions collectionExtensions = constellioFactories.getAppLayerFactory().getExtensions().forCollection(collection);
		Field field = collectionExtensions.getMetadataField(new MetadataFieldExtensionParams(metadataVO,recordVO,locale));
		if(field != null) {
			return field;
		} else {
			return metadataFieldFactory.build(metadataVO, locale);
		}
	}

	protected void postBuild(Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		metadataFieldFactory.postBuild(field, metadataVO);
		callPostBuildExtensions(field, recordVO, metadataVO);
	}

	protected void callPostBuildExtensions(Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		String collection = recordVO.getSchema().getCollection();
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		RecordFieldFactoryPostBuildExtensionParams params = new RecordFieldFactoryPostBuildExtensionParams(metadataFieldFactory, field, recordVO, metadataVO);
		AppLayerCollectionExtensions collectionExtensions = constellioFactories.getAppLayerFactory().getExtensions().forCollection(collection);
		collectionExtensions.postRecordFieldFactoryBuild(params);
	}

}
