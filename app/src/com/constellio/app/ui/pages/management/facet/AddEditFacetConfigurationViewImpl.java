/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.facet;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;
import java.util.Map;

import org.vaadin.maddon.ListContainer;

import com.constellio.app.modules.rm.ui.components.facet.FacetFieldFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationPresenter.AvailableFacetFieldMetadata;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class AddEditFacetConfigurationViewImpl extends BaseViewImpl implements AddEditFacetConfigurationView {
	private AddEditFacetConfigurationPresenter presenter;
	private TabSheet tabSheet;
	private Table valuesList;
	private Tab valuesTab;
	private Tab profileTab;
	private String dataFieldCode;
	private EnumWithSmallCodeOptionGroup facetType;
	private boolean edit;
	ComboBox dataFieldCombo;

	public AddEditFacetConfigurationViewImpl() {
		presenter = new AddEditFacetConfigurationPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AddEditFacetConfigurationView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		tabSheet = new TabSheet();
		tabSheet.addStyleName("face-configuration");
		profileTab = tabSheet.addTab(newForm(), $("AddEditFacetConfiguration.tabs.profile"), null, 0);
		tabSheet.setSelectedTab(0);

		mainLayout.addComponents(tabSheet);
		return mainLayout;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		edit = !event.getParameters().isEmpty();
		presenter.forParams(event.getParameters(), edit);
	}

	private void addEmptyValue() {
		addItem("", "");
	}

	private Component createValuesTab() {
		String postfix = presenter.getTypePostfix();
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		Button addValue = new Button($("AddEditFacetConfiguration.values.new." + postfix));
		addValue.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				addEmptyValue();
			}
		});

		final Table table = new Table($("AddEditFacetConfiguration.tableTitle"));
		table.addContainerProperty("label", String.class, null);
		table.addContainerProperty("value", String.class, null);
		table.addContainerProperty("delete", Button.class, null);
		table.setColumnHeader("label", $("AddEditFacetConfiguration.values.label." + postfix));
		table.setColumnHeader("value", $("AddEditFacetConfiguration.values.value." + postfix));
		table.setColumnHeader("delete", "");
		table.setWidth("100%");
		table.setEditable(true);
		table.setPageLength(table.getItemIds().size());
		valuesList = table;

		List<Map<String, String>> values = presenter.getValueForFacet(dataFieldCode);

		for (Map<String, String> row : values) {
			for (String label : row.keySet()) {
				addItem(label, row.get(label));
			}
		}

		mainLayout.addComponent(addValue);
		mainLayout.addComponent(table);

		return mainLayout;
	}

	private void saveValues() {
		if (valuesList != null) {
			List<Integer> ids = (List<Integer>) valuesList.getItemIds();
			for (Integer id : ids) {
				Item item = valuesList.getItem(id);
				String label = (String) item.getItemProperty("label").getValue();
				String value = (String) item.getItemProperty("value").getValue();

				presenter.addValue(label, value, id);
			}
		}
	}

	private void refreshTable() {
		valuesList.removeAllItems();

		Map<Integer, Map<String, String>> values = presenter.getValues();

		for (Integer id : values.keySet()) {
			Map<String, String> value = values.get(id);
			for (final String label : value.keySet()) {
				valuesList.addItem(id);
				addItem(label, value.get(label), id);
			}
		}
	}

	private void addItem(String label, final String value, final int id) {
		Item row1 = valuesList.getItem(id);
		Button delete = new Button("Delete");
		delete.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				saveValues();
				presenter.removeValue(id);
				refreshTable();
			}
		});

		row1.getItemProperty("label").setValue(label);
		row1.getItemProperty("value").setValue(value);
		row1.getItemProperty("delete").setValue(delete);

		presenter.addValue(label, value, id);
	}

	private void addItem(String label, String value) {
		int id = (int) valuesList.addItem();
		addItem(label, value, id);
	}

	public void refreshValuesTab() {
		Component refreshedTab = createValuesTab();

		if (tabSheet != null) {
			if (valuesTab != null) {
				tabSheet.removeTab(valuesTab);
			}

			valuesTab = tabSheet.addTab(refreshedTab, $("AddEditFacetConfiguration.tabs." + presenter.getTypePostfix()));
		}
	}

	public void refreshProfileTab() {
		if (tabSheet != null) {
			if (profileTab != null) {
				tabSheet.removeTab(profileTab);
				profileTab = tabSheet.addTab(getRecordFormForVO(), $("AddEditFacetConfiguration.tabs.profile"), null, 0);
				tabSheet.setSelectedTab(0);
			}
		}
	}

	public void removeValuesTab() {
		if (tabSheet != null && valuesTab != null) {
			tabSheet.removeTab(valuesTab);
		}
	}

	private RecordForm getRecordFormForVO() {
		return new RecordForm(presenter.getRecordVO(), new FacetFieldFactory(dataFieldCombo, facetType)) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				saveValues();
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
	}

	private RecordForm newForm() {
		dataFieldCode = presenter.getRecordVO().get(Facet.FIELD_DATA_STORE_CODE);
		dataFieldCombo = new ComboBox($("AddEditFacetConfiguration.fieldDatastoreCode"));
		dataFieldCombo.setContainerDataSource(new ListContainer<>(String.class));
		dataFieldCombo.setValue(dataFieldCode);

		for (AvailableFacetFieldMetadata metadata : presenter.getAvailableDataStoreCodes()) {
			dataFieldCombo.addItem(metadata.getCode());
			dataFieldCombo.setItemCaption(metadata.getCode(), metadata.getLabel());
		}

		dataFieldCombo.setEnabled(presenter.isDataStoreCodeNeeded());
		dataFieldCombo.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataFieldCode = (String) event.getProperty().getValue();
				presenter.displayCorrectTab(dataFieldCode);
			}
		});

		facetType = new EnumWithSmallCodeOptionGroup<>(FacetType.class);
		facetType.setCaption($("AddEditFacetConfiguration.facetType"));
		facetType.setEnabled(!edit);
		facetType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				FacetType type = (FacetType) event.getProperty().getValue();
				presenter.setFacetType(type);
				if (!presenter.isDataStoreCodeNeeded()) {
					dataFieldCombo.setEnabled(false);
					if (!edit) {
						dataFieldCombo.setValue(null);
					}
				} else {
					dataFieldCombo.setEnabled(true);
				}
				presenter.typeChanged(dataFieldCode);
			}
		});

		return getRecordFormForVO();
	}

	public void displayInvalidQuery(List<Integer> invalids) {
		valuesList.setCellStyleGenerator(new FacetConfigurationValueCellStyleGenerator(invalids));
		valuesList.refreshRowCache();
		showErrorMessage($("AddEditFacetConfiguration.error.InvalidQuery"));
	}
}
