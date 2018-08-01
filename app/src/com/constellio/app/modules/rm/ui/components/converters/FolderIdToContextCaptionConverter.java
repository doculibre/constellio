package com.constellio.app.modules.rm.ui.components.converters;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;

public class FolderIdToContextCaptionConverter implements Converter<String, String> {

	public static final String DELIM = " | ";

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public FolderIdToContextCaptionConverter() {
		super();
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
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String caption;
		if (StringUtils.isNotBlank(value)) {
			Folder folder = rmSchemasRecordsServices.getFolder(value);
			caption = folder.get(Schemas.CAPTION);
			if (caption == null) {
				caption = folder.getTitle();
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

	public Resource getIcon(String recordId) {
		return FileIconUtils.getIconForRecordId(recordId);
	}

}
