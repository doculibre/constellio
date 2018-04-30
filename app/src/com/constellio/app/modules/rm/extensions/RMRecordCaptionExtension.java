package com.constellio.app.modules.rm.extensions;

import java.util.Locale;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.extensions.behaviors.RecordExtension;

public class RMRecordCaptionExtension extends RecordExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMRecordCaptionExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public String getCaptionForRecord(Record record, Locale locale) {
		return SchemaCaptionUtils.getCaptionForRecord(record, locale);
	}
}
