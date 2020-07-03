package com.constellio.app.modules.rm.extensions;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.params.GetCaptionForRecordParams;

public class RMRecordCaptionExtension extends RecordExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMRecordCaptionExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public String getCaptionForRecord(GetCaptionForRecordParams params) {
		return SchemaCaptionUtils.getCaptionForRecord(params.getRecord(), params.getLocale(), true);
	}
}
