package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class GroupIdToCaptionConverter implements Converter<String, String> {

	private boolean showInactiveNotice = true;

	public GroupIdToCaptionConverter setShowInactiveNotice(boolean showInactiveNotice) {
		this.showInactiveNotice = showInactiveNotice;
		return this;
	}

	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {

		String caption;
		if (StringUtils.isNotBlank(value)) {
			ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
			Record record = modelLayerFactory.newRecordServices().getDocumentById(value);
			caption = SchemaCaptionUtils.getCaptionForRecord(record, locale, true);

			SchemasRecordsServices schemas = new SchemasRecordsServices(record.getCollection(), modelLayerFactory);
			Group group = schemas.wrapGroup(record);
			if (!schemas.isGroupActive(group)) {
				caption += " " + $("inactiveGroupNotice");
			}
		} else {
			caption = "";
		}

		return caption;
	}

	@Override
	public Class<String> getModelType() {
		return String.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
