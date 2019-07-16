package com.constellio.app.modules.rm.reports.model.excel;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class BaseExcelReportPresenter {

	protected Locale locale;
	protected AppLayerFactory appLayerFactory;
	protected String collection;

	public BaseExcelReportPresenter(AppLayerFactory appLayerFactory, Locale locale, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.locale = locale;
		this.collection = collection;
	}

	protected List<Object> getRecordLine(Record record, List<Metadata> orderedEnabledReportedMetadataList) {
		List<Object> returnList = new ArrayList<>();
		for (Metadata metadata : orderedEnabledReportedMetadataList) {
			Object metadataValue = record.get(metadata, locale);
			if (metadataValue == null) {
				returnList.add(null);
			} else {
				returnList.add(getConvertedValue(metadata, metadataValue));
			}
		}
		return returnList;
	}

	protected Object getConvertedValue(Metadata metadata, Object metadataValue) {
		if (metadata.isMultivalue()) {
			List<Object> items = (List) metadataValue;
			List<Object> convertedValue = new ArrayList<>();
			for (Object item : items) {
				convertedValue.add(getConvertedScalarValue(metadata, item));
			}
			if (convertedValue.isEmpty()) {
				return "";
			}
			return convertedValue;
		} else {
			return getConvertedScalarValue(metadata, metadataValue);
		}

	}

	protected Object getConvertedScalarValue(Metadata metadata, Object metadataValue) {

		if (metadata.getType() == MetadataValueType.REFERENCE) {
			String referenceId = (String) metadataValue;
			if (referenceId != null) {
				Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(referenceId);
				String code = record.get(Schemas.CODE);
				String title = record.get(Schemas.TITLE);
				if (code == null || !metadata.isDefaultRequirement()) {
					return title;
				} else {
					return code + "-" + title;
				}
			}
		} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
			if (metadataValue == null) {
				return $("no");
			}
			return metadataValue.equals(true) ? $("yes") : $("no");
		} else if (metadata.getType() == MetadataValueType.TEXT || metadata.getType() == MetadataValueType.STRING) {
			SchemasDisplayManager schemasManager = appLayerFactory.getMetadataSchemasDisplayManager();
			MetadataDisplayConfig config = schemasManager.getMetadata(collection, metadata.getCode());
			String result = metadataValue.toString().replaceAll("<br>", "\n");
			result = result.replace("&nbsp;", "");
			result = result.replaceAll("<li>", "\n");
			if (config.getInputType().equals(MetadataInputType.RICHTEXT)) {
				result = result.replaceAll("\\<[^>]*>", "");
			}
			return result;
		} else if (metadata.getType() == MetadataValueType.ENUM) {
			EnumWithSmallCodeToCaptionConverter captionConverter =
					new EnumWithSmallCodeToCaptionConverter((Class<? extends EnumWithSmallCode>) metadataValue.getClass());
			return captionConverter.convertToPresentation(((EnumWithSmallCode) metadataValue).getCode(), String.class, locale);
		}

		return metadataValue;
	}
}
