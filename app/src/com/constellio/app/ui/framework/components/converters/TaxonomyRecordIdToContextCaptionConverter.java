package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.vaadin.server.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;

public class TaxonomyRecordIdToContextCaptionConverter extends RecordIdToCaptionConverter {

	public static final String DELIM = " | ";

	private transient RecordServices recordServices;

	private transient TaxonomiesManager taxonomiesManager;

	public TaxonomyRecordIdToContextCaptionConverter() {
		super();
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String caption;
		if (StringUtils.isNotBlank(value)) {
			Record record = recordServices.getDocumentById(value);
			if (record != null) {
				Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
				if (taxonomy != null && taxonomy.isShowParentsInSearchResults()) {
					StringBuffer sb = new StringBuffer();
					Record currentRecord = record;
					while (currentRecord != null) {
						if (sb.length() > 0) {
							sb.insert(0, DELIM);
						}
						String currentRecordCaption = SchemaCaptionUtils.getCaptionForRecord(currentRecord, locale, true);
						sb.insert(0, currentRecordCaption);

						String parentRecordId = currentRecord.getParentId();
						if (parentRecordId != null) {
							currentRecord = recordServices.getDocumentById(parentRecordId);
						} else {
							currentRecord = null;
						}
					}
					caption = sb.toString();
				} else {
					caption = super.convertToPresentation(value, targetType, locale);
				}
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
