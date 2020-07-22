package com.constellio.app.modules.rm.ui.components.facet;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationPresenter;
import com.constellio.app.ui.pages.management.facet.fields.ValuesLabelFieldImpl;
import com.constellio.model.entities.records.wrappers.Facet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;

import java.util.Locale;

public class FacetFieldFactory extends MetadataFieldFactory {

	private ComboBox dataStoreCode;
	private OptionGroup facetType;
	private boolean dataStoreCodeSupportingLabelValues;
	private AddEditFacetConfigurationPresenter presenter;

	public FacetFieldFactory(ComboBox dataStoreCode, OptionGroup facetType,
							 AddEditFacetConfigurationPresenter presenter) {
		this.dataStoreCode = dataStoreCode;
		this.facetType = facetType;
		this.dataStoreCodeSupportingLabelValues = presenter.isDataStoreCodeSupportingLabelValues(presenter.getDataFieldCode());
		this.presenter = presenter;
	}

	@Override
	public Field<?> build(MetadataVO metadata, String recordId, Locale locale) {
		Field<?> field;
		String metadataCode = metadata.getCode();
		if (metadataCode.endsWith(Facet.FIELD_DATA_STORE_CODE)) {
			field = dataStoreCode;
		} else if (metadataCode.endsWith(Facet.ORDER)) {
			field = null;
		} else if (metadataCode.endsWith(Facet.PAGES)) {
			field = null;
		} else if (metadataCode.endsWith(Facet.FACET_TYPE)) {
			field = facetType;
		} else if ((metadataCode.endsWith(Facet.FIELD_VALUES_LABEL) && dataStoreCodeSupportingLabelValues) || metadataCode
				.endsWith(Facet.LIST_QUERIES)) {
			field = new ValuesLabelFieldImpl(presenter);
		} else {
			field = super.build(metadata, recordId, locale);
		}
		return field;
	}
}
