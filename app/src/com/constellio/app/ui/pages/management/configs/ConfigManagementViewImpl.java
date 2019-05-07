package com.constellio.app.ui.pages.management.configs;

import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseMouseOverIcon;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BasePasswordField;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConfigManagementViewImpl extends BaseViewImpl implements ConfigManagementView {

	public static final String CONFIG_ELEMENT_VALUE = "seleniumConfigValue";

	ConfigManagementPresenter presenter;
	private TabSheet tabsheet;

	SystemConfigurationGroupdataProvider dataProvider;

	public ConfigManagementViewImpl() {
		super();
		this.presenter = new ConfigManagementPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	@Override
	public void setDataProvider(SystemConfigurationGroupdataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		this.tabsheet = new TabSheet();
		tabsheet.addStyleName("config-management");

		for (String groupCode : dataProvider.getCodesList()) {
			List<SystemConfigurationVO> configs = dataProvider.getSystemConfigurationGroup(groupCode).getConfigs();
			if (!configs.isEmpty()) {
				VerticalLayout groupLayout = new VerticalLayout();
				groupLayout.addStyleName("config-group");
				groupLayout.setSizeFull();
				groupLayout.setSpacing(true);
				groupLayout.setId(groupCode);

				for (int i = 0; i < configs.size(); i++) {
					SystemConfigurationVO currentConfigurationVO = configs.get(i);
					String fieldCaption = presenter.getLabel(groupCode, currentConfigurationVO.getCode());
					Field<?> field = createField(currentConfigurationVO);
					field.setId(groupCode + i);
					field.addStyleName(CONFIG_ELEMENT_VALUE);
					field.addValueChangeListener(new ValueChangeListener() {
						@Override
						public void valueChange(ValueChangeEvent event) {
							Field<?> field = (Field<?>) event.getProperty();
							String id = field.getId();
							String groupCode = tabsheet.getSelectedTab().getId();
							String iString = StringUtils.substringAfter(id, groupCode);
							if (StringUtils.isNotBlank(iString)) {
								int i = Integer.valueOf(iString);
								Object value = field.getValue();
								if (value == null) {
									dataProvider.valueChange(groupCode, i, null);
								} else {
									dataProvider.valueChange(groupCode, i, value);
								}
							}
						}
					});
					field.setSizeFull();

					OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
						@Override
						public void onEnterKeyPressed() {
							presenter.saveButtonClicked();
						}
					};
					if (field instanceof TextField) {
						onEnterHandler.installOn((TextField) field);
					} else if (field instanceof DateField) {
						onEnterHandler.installOn((DateField) field);
					} else if (field instanceof ComboBox) {
						onEnterHandler.installOn((ComboBox) field);
					}

					field.setCaption(fieldCaption);
					if (field instanceof AbstractComponent) {
						((AbstractComponent) field).setCaptionAsHtml(true);
					}

					HorizontalLayout currentConfigLayout = wrapFieldWithDocumentation(currentConfigurationVO, groupCode, field);
					groupLayout.addComponent(currentConfigLayout);
				}

				tabsheet.addTab(groupLayout, presenter.getGroupLabel(groupCode));
			}
		}
		layout.addComponent(tabsheet);

		Label separator = new Label("&nbsp;", ContentMode.HTML);
		layout.addComponent(separator);

		Button saveButton = new Button($("save"));
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.saveButtonClicked();
			}
		});
		saveButton.addStyleName(BaseForm.SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		layout.addComponent(saveButton);
		layout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);

		return layout;
	}

	private HorizontalLayout wrapFieldWithDocumentation(
			SystemConfigurationVO configVO, String groupCode, Field<?> field) {
		I18NHorizontalLayout layout = new I18NHorizontalLayout();
		String descriptionKey = "SystemConfigurationGroup." + groupCode + "." + configVO.getCode() + ".description";
		String configDescription = $(descriptionKey);
		BaseMouseOverIcon baseMouseOverIcon = new BaseMouseOverIcon(new ThemeResource("images/icons/information2.png"), configDescription);
		if(StringUtils.isBlank(configDescription) || configDescription.equals(descriptionKey)) {
			baseMouseOverIcon.setVisible(false);
		}
		layout.setSizeFull();
//		field.setWidth(null);
		layout.addComponents(field, baseMouseOverIcon);
		layout.setExpandRatio(field, 1);
//		layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
//		layout.setComponentAlignment(baseMouseOverIcon, Alignment.MIDDLE_LEFT);
//		layout.setExpandRatio(baseMouseOverIcon, 1.0f);
		return layout;
	}

	private Field<?> createField(SystemConfigurationVO config) {
		SystemConfigurationType type = config.getType();
		if (type == SystemConfigurationType.STRING ||
			config.getType() == SystemConfigurationType.INTEGER) {
			AbstractField<String> textField;
			if (config.isHiddenValue()) {
				textField = new BasePasswordField();
			} else if (config.getValue() != null && config.getValue().toString().contains("\n")) {
				textField = new BaseTextArea();
			} else {
				textField = new BaseTextField();
			}
			textField.setRequired(true);

			if (config.getType() == SystemConfigurationType.INTEGER) {
				textField.setConverter(Integer.class);
				textField.setValue(config.getValue().toString());
				textField.addValidator(
						new IntegerRangeValidator($("com.vaadin.data.validator.IntegerRangeValidator_withoutLimits"), null,
								null));
			} else {
				Object value = config.getValue();
				textField.setValue(value == null ? "" : value.toString());
			}
			return textField;
		} else if (type == SystemConfigurationType.BOOLEAN) {
			CheckBox checkbox = new CheckBox();
			checkbox.setValue((Boolean) config.getValue());
			return checkbox;
		} else if (type == SystemConfigurationType.ENUM) {
			ComboBox combobox = new BaseComboBox();
			combobox.setNullSelectionAllowed(false);
			for (Enum<?> value : config.getValues().getEnumConstants()) {
				combobox.addItem(value.name());
				combobox.setItemCaption(value.name(), $(value.getClass().getSimpleName() + "." + value.name()));
			}
			Enum<?> value = (Enum<?>) config.getValue();
			combobox.setValue(value.name());
			combobox.setRequired(true);
			return combobox;
		} else if (type == SystemConfigurationType.BINARY) {
			BaseUploadField uploadField = new BaseUploadField();
			return uploadField;
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

	@Override
	protected String getGuideUrl() {
		return null;//"http://documentation.constellio.com/pages/viewpage.action?pageId=2326848";
	}
}
