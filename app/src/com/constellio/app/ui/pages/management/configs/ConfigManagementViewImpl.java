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
package com.constellio.app.ui.pages.management.configs;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ConfigManagementViewImpl extends BaseViewImpl implements
														   ConfigManagementView {

	public static final String CONFIG_ELEMENT_VALUE = "seleniumConfigValue";

	ConfigManagementPresenter presenter;
	private TabSheet tabsheet;

	public ConfigManagementViewImpl() {
		super();
		this.presenter = new ConfigManagementPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		this.tabsheet = new TabSheet();

		final SystemConfigurationGroupdataProvider dataProvider = presenter.systemConfigurationGroupDataProvider();
		for (String groupCode : dataProvider.getCodesList()) {
			List<SystemConfigurationVO> configs = dataProvider.getSystemConfigurationGroup(groupCode).getConfigs();
			GridLayout gridLayout = new GridLayout(2, configs.size() + 1);
			gridLayout.setSizeFull();
			gridLayout.setId(groupCode);
			gridLayout.setSpacing(true);

			for (int i = 0; i < configs.size(); i++) {
				SystemConfigurationVO currentConfigurationVO = configs.get(i);
				Label currentLabel = new Label(presenter.getLabel(groupCode, currentConfigurationVO.getCode()));
				currentLabel.setSizeFull();
				gridLayout.addComponent(currentLabel, 0, i);
				Field field = createField(currentConfigurationVO);
				field.setId(groupCode + i);
				field.addStyleName(CONFIG_ELEMENT_VALUE);
				field.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						Field field = (Field) event.getProperty();
						String id = field.getId();
						String groupCode = tabsheet.getSelectedTab().getId();
						String iString = StringUtils.substringAfter(id, groupCode);
						int i = Integer.valueOf(iString);
						Object value = field.getValue();
						if (value == null) {
							dataProvider.valueChange(groupCode, i, null);
						} else {
							dataProvider.valueChange(groupCode, i, value.toString());
						}

					}
				});
				field.setSizeFull();
				gridLayout.addComponent(field, 1, i);
			}

			tabsheet.addTab(gridLayout, presenter.getGroupLabel(groupCode));

		}
		layout.addComponent(tabsheet);

		Label separator = new Label("&nbsp;", ContentMode.HTML);
		layout.addComponent(separator);

		Button saveButton = new Button($("save"));
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Iterator<Component> iterator = tabsheet.iterator();
				while (iterator.hasNext()) {
					Component next = iterator.next();
					String groupCode = next.getId();
					presenter.saveButtonClicked(groupCode, dataProvider);
				}
			}
		});
		saveButton.addStyleName(BaseForm.SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		layout.addComponent(saveButton);
		layout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);

		return layout;
	}

	private Field createField(SystemConfigurationVO config) {
		SystemConfigurationType type = config.getType();
		if (type == SystemConfigurationType.STRING ||
				config.getType() == SystemConfigurationType.INTEGER) {
			TextField textField = new TextField();
			textField.setRequired(true);
			textField.setValue(config.getValue().toString());
			if (config.getType() == SystemConfigurationType.INTEGER) {
				textField.setConverter(Integer.class);
				textField.addValidator(
						new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator_withoutLimits"), null,
								null));
			}
			return textField;
		} else if (type == SystemConfigurationType.BOOLEAN) {
			CheckBox checkbox = new CheckBox();
			checkbox.setValue((Boolean) config.getValue());
			return checkbox;
		} else if (type == SystemConfigurationType.ENUM) {
			ComboBox combobox = new BaseComboBox();
			combobox.setNullSelectionAllowed(false);
			for (Enum value : config.getValues().getEnumConstants()) {
				combobox.addItem(value.name());
				combobox.setItemCaption(value.name(), $(value.getClass().getSimpleName() + "." + value.name()));
			}
			Enum value = (Enum) config.getValue();
			combobox.setValue(value.name());
			combobox.setRequired(true);
			return combobox;
		} else {
			throw new RuntimeException("Unsupported type " + type);
		}
	}

	@Override
	protected String getTitle() {
		return $("ConfigManagementView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}
}
