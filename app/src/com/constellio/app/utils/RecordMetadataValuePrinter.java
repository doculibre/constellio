package com.constellio.app.utils;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RecordMetadataValuePrinter {

	String nullValue;

	ModelLayerFactory modelLayerFactory;

	public RecordMetadataValuePrinter(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public String convertForPrinting(Metadata metadata, Object metadataValue) {
		if (metadata.isMultivalue()) {
			List<Object> items = (List) metadataValue;
			StringBuilder stringBuilder = new StringBuilder("[");
			for (int i = 0; i < items.size(); i++) {
				if (i != 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(convertScalarForPrinting(metadata, items.get(i)));
			}
			stringBuilder.append("]");
			return stringBuilder.toString();
		} else {
			return convertScalarForPrinting(metadata, metadataValue);
		}

	}

	private String convertScalarForPrinting(Metadata metadata, Object metadataValue) {

		if (metadataValue == null) {
			return nullValue;

		} else if (metadata.getType() == MetadataValueType.REFERENCE) {
			String referenceId = (String) metadataValue;
			if (referenceId != null) {
				Record record = modelLayerFactory.newRecordServices().getDocumentById(referenceId);
				String code = record.get(Schemas.CODE);
				String title = record.get(Schemas.TITLE);
				if (code == null) {
					return title;
				} else {
					return code + "-" + title;
				}
			}

		} else if (metadata.getType() == MetadataValueType.CONTENT) {
			Content content = (Content) metadataValue;
			return content.getCurrentVersion().getFilename();

		} else if (metadata.getType() == MetadataValueType.ENUM) {
			EnumWithSmallCode enumWithSmallCode = (EnumWithSmallCode) metadataValue;
			return $(enumWithSmallCode.getClass().getSimpleName() + "." + enumWithSmallCode);
		}

		return metadataValue.toString();
	}

}
