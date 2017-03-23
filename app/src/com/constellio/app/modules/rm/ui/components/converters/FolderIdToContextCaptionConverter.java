package com.constellio.app.modules.rm.ui.components.converters;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;

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
			if (folder != null) {
				StringBuffer sb = new StringBuffer();
				Folder currentFolder = folder;
				while (currentFolder != null) {
					if (sb.length() > 0) {
						sb.insert(0, DELIM);
					}
					String currentFolderCaption = SchemaCaptionUtils.getCaptionForRecordId(currentFolder.getId());
					sb.insert(0, currentFolderCaption);
					
					String parentFolderId = currentFolder.getParentFolder();
					if (parentFolderId != null) {
						currentFolder = rmSchemasRecordsServices.getFolder(parentFolderId);
					} else {
						currentFolder = null;
					}
				}
				caption = sb.toString();
			} else {
				caption = "";
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
