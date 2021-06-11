package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.modules.rm.ui.components.facet.FacetFieldFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationPresenter.AvailableFacetFieldMetadata;
import com.constellio.app.ui.pages.management.facet.fields.FacetConfigurationForm;
import com.constellio.app.ui.pages.management.facet.fields.FacetConfigurationFormImpl;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import org.vaadin.maddon.ListContainer;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditFacetConfigurationViewImpl extends BaseViewImpl implements AddEditFacetConfigurationView {
	private AddEditFacetConfigurationPresenter presenter;
	private String dataFieldCode;
	private EnumWithSmallCodeOptionGroup facetType;
	private boolean edit;
	ComboBox dataFieldCombo;
	private FacetConfigurationFormImpl form;

	public AddEditFacetConfigurationViewImpl() {
		presenter = new AddEditFacetConfigurationPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AddEditFacetConfigurationView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		form = newForm();
		return form;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		edit = !event.getParameters().isEmpty();
		presenter.forParams(event.getParameters(), edit);
	}

	@Override
	public FacetConfigurationForm getForm() {
		return form;
	}

	private FacetConfigurationFormImpl newForm() {
		dataFieldCode = presenter.getRecordVO().get(Facet.FIELD_DATA_STORE_CODE);
		dataFieldCombo = new ComboBox($("AddEditFacetConfiguration.fieldDatastoreCode"));
		dataFieldCombo.setContainerDataSource(new ListContainer<>(String.class));
		dataFieldCombo.setValue(dataFieldCode);

		for (AvailableFacetFieldMetadata metadata : presenter.getAvailableDataStoreCodes()) {
			dataFieldCombo.addItem(metadata.getCode());
			dataFieldCombo.setItemCaption(metadata.getCode(), metadata.getLabel());
		}

		dataFieldCombo.setRequired(presenter.isDataStoreCodeNeeded());
		dataFieldCombo.setEnabled(presenter.isDataStoreCodeNeeded());
		dataFieldCombo.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataFieldCode = (String) event.getProperty().getValue();
				if (dataFieldCode != null && !dataFieldCode.equals(presenter.getDataFieldCode())) {
					presenter.reloadForm(dataFieldCode);
				}
			}
		});

		facetType = new EnumWithSmallCodeOptionGroup<>(FacetType.class);
		facetType.setCaption($("AddEditFacetConfiguration.facetType"));
		facetType.setEnabled(!edit);
		facetType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				FacetType type = (FacetType) event.getProperty().getValue();
				if (presenter.getFacetType() != null && !presenter.getFacetType().equals(facetType.getValue())) {
					presenter.setFacetType(type);
					if (!presenter.isDataStoreCodeNeeded()) {
						dataFieldCombo.setEnabled(false);
						if (!edit) {
							dataFieldCombo.setValue(null);
						}
					} else {
						dataFieldCombo.setEnabled(true);
					}
					presenter.reloadForm(dataFieldCode);
				}
			}
		});

		form = new FacetConfigurationFormImpl(presenter.getRecordVO(),
				new FacetFieldFactory(dataFieldCombo, facetType, presenter), getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				if (presenter.getFacetType() == FacetType.FIELD) {
					if (getCustomField(Facet.FIELD_VALUES_LABEL) != null) {
						getCustomField(Facet.FIELD_VALUES_LABEL).saveValues();
					}
				} else if (presenter.getFacetType() == FacetType.QUERY) {
					if (getCustomField(Facet.LIST_QUERIES) != null) {
						getCustomField(Facet.LIST_QUERIES).saveValues();
					}
				}
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}

			@Override
			public void reload() {
				replaceComponent(this, newForm());
			}

			@Override
			public void commit() {
				for (Field<?> field : fieldGroup.getFields()) {
					try {
						field.commit();
					} catch (SourceException | InvalidValueException e) {
					}
				}
			}
		};

		return form;
	}

	public void displayInvalidQuery(List<Integer> invalids) {
		Table valuesList = getForm().getCustomField(Facet.LIST_QUERIES).getValueListTable();
		valuesList.setCellStyleGenerator(new FacetConfigurationValueCellStyleGenerator(invalids));
		valuesList.refreshRowCache();
		showErrorMessage($("AddEditFacetConfiguration.error.InvalidQuery"));
	}
}
