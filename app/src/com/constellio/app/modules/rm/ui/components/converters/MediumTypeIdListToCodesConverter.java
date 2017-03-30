package com.constellio.app.modules.rm.ui.components.converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.data.util.converter.Converter;

public class MediumTypeIdListToCodesConverter implements Converter<String, List<String>> {

	private String separator;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public MediumTypeIdListToCodesConverter() {
		this(", ");
	}
	public MediumTypeIdListToCodesConverter(String separator) {
		this.separator = separator;
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public List<String> convertToModel(String value, Class<? extends List<String>> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		List<String> listValue;
		if (value != null) {
			listValue = new ArrayList<>();
			String[] tokens = StringUtils.split(value, separator);
			for (String token : tokens) {
				listValue.add(token);
			}
		} else {
			listValue = null;
		}
		return listValue;
	}

	@Override
	public String convertToPresentation(List<String> value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String presentation;
		if (value != null) {
			StringBuffer sb = new StringBuffer();
			for (String recordId : value) {
				if (sb.length() > 0) {
					sb.append(separator);
				}
				MediumType mediumType = rmSchemasRecordsServices.getMediumType(recordId);
				String caption = mediumType.getCode();
				sb.append(caption);
			}
			presentation = sb.toString();
		} else {
			presentation = null;
		}
		return presentation;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getModelType() {
		return List.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
