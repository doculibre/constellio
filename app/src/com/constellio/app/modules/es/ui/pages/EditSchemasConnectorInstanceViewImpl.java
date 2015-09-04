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
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class EditSchemasConnectorInstanceViewImpl extends BaseViewImpl implements EditSchemasConnectorInstanceView {

	private VerticalLayout mainLayout;
	Table table;
	private RecordVO recordVO;
	private Component addConnectorInstanceComponent;
	private Map<String, List<MetadataVO>> metadataVOs;
	Map<Integer, ComboBox> comboBoxes = new HashMap<>();
	Map<String, String> fields;
	Map<Integer, MetadataVO> settedFields;
	Map<Integer, String> properties = new HashMap<>();

	private EditSchemasConnectorInstancePresenter presenter;

	public EditSchemasConnectorInstanceViewImpl() {
		presenter = new EditSchemasConnectorInstancePresenter(this);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
		fields = presenter.getDefaultAvailableProperties();
		metadataVOs = presenter.getMetadataVOs();

		int i = 0;
		for (String property : fields.keySet()) {
			properties.put(i++, property);
		}

		settedFields = presenter.getSettedField(properties);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		table = buildTable();

		BaseButton saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.saveButtonClicked(properties, settedFields);
			}
		};
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		};
		cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.addComponents(saveButton, cancelButton);
		horizontalLayout.setComponentAlignment(saveButton, Alignment.TOP_CENTER);

		mainLayout.addComponents(table, horizontalLayout);
		return mainLayout;
	}

	private Table buildTable() {
		Table table = new Table();
		table.addContainerProperty("Label", Label.class, null);
		table.addContainerProperty("ComboBox", ComboBox.class, null);
		table.addContainerProperty("Button", Button.class, null);
		table.setColumnHeader("Label", "");
		table.setColumnHeader("ComboBox", "");
		table.setColumnHeader("Button", "");

		int i = 0;
		for (Entry<String, String> field : fields.entrySet()) {
			Label label = new Label(field.getKey());

			ComboBox comboBox = new ComboBox();
			comboBoxes.put(i, comboBox);
			if (metadataVOs.containsKey(field.getValue())) {
				for (MetadataVO metadataVO : metadataVOs.get(field.getValue())) {
					comboBox.addItem(metadataVO);
					comboBox.setItemCaption(metadataVO, presenter.getLocalCode(metadataVO.getCode()));
				}
			}
			final int index = i;
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					MetadataVO metadataVO = (MetadataVO) event.getProperty().getValue();
					settedFields.put(index, metadataVO);
				}
			});
			comboBox.select(settedFields.get(i));

			Button button = buildCreateMetadata(field.getValue(), i);

			table.addItem(new Object[] { label, comboBox, button }, i++);
		}
		table.setPageLength(fields.size());

		return table;
	}

	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

	private Button buildCreateMetadata(final String className, final int index) {
		return new WindowButton($("EditSchemasConnectorInstanceView.createMetadata"),
				$("EditSchemasConnectorInstanceView.createMetadata")) {
			@Override
			protected Component buildWindowContent() {

				final TextField localMetadata = new TextField();
				localMetadata.setCaption($("EditSchemasConnectorInstanceView.localMetadata"));
				localMetadata.setRequired(true);
				localMetadata.setNullRepresentation("");
				localMetadata.setId("localMetadata");
				localMetadata.addStyleName("localMetadata");
				localMetadata.addValidator(new Validator() {
					@Override
					public void validate(Object value)
							throws InvalidValueException {
						String pattern = "([a-zA-Z0-9])+";
						String valueStr = (String) value;
						if (!valueStr.matches(pattern)) {
							throw new InvalidValueException($("EditSchemasConnectorInstanceView.invalidCode"));
						}

					}
				});

				final TextField label = new TextField();
				label.setCaption($("EditSchemasConnectorInstanceView.label"));
				label.setRequired(true);
				label.setNullRepresentation("");
				label.setId("label");
				label.addStyleName("label");
				label.addValidator(new Validator() {
					@Override
					public void validate(Object value)
							throws InvalidValueException {
						String valueStr = (String) value;
						if (StringUtils.isBlank(valueStr)) {
							throw new InvalidValueException($("EditSchemasConnectorInstanceView.error.invalidLabel"));
						}

					}
				});

				BaseButton createMetadataButton = new BaseButton($("EditSchemasConnectorInstanceView.createMetadata")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if(localMetadata.getValue().isEmpty() || label.getValue().isEmpty()) {
							showErrorMessage($("EditSchemasConnectorInstanceView.error.validation"));
						} else {
							presenter.createMetadata(className, localMetadata.getValue(), label.getValue(), index);
							getWindow().close();
						}
					}
				};
				createMetadataButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(createMetadataButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addComponents(localMetadata, label, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
	}

	@Override
	public void setMetadataVOs(Map<String, List<MetadataVO>> metadataVOs) {
		this.metadataVOs = metadataVOs;
		for (Entry<Integer, ComboBox> comboBox : comboBoxes.entrySet()) {
			MetadataVO metadataVO = (MetadataVO) comboBox.getValue().getValue();
			settedFields.put(comboBox.getKey(), metadataVO);
		}
		refreshTable();
	}

	@Override
	public void setComboBoxValue(int index, MetadataVO metadataVO) {
		settedFields.put(index, metadataVO);
		refreshTable();
	}

	private void refreshTable() {
		Table newTable = buildTable();
		mainLayout.replaceComponent(table, newTable);
		table = newTable;
	}
}
