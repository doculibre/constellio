package com.constellio.app.ui.pages.management.plugin;

import com.constellio.app.services.extensions.plugins.PluginActivationFailureCause;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class PluginManagementViewImpl extends BaseViewImpl implements PluginManagementView {
	PluginManagementPresenter presenter;
	private BaseUploadField fileUpload;

	public PluginManagementViewImpl() {
		super();
		this.presenter = new PluginManagementPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("PluginManagementView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		Label restartMessage = new Label($("PluginManagementView.restartRequired"));
		restartMessage.addStyleName(ValoTheme.LABEL_COLORED);
		restartMessage.addStyleName(ValoTheme.LABEL_BOLD);

		Button restart = new BaseButton($("PluginManagementView.restart")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.restartRequested();
			}
		};

		HorizontalLayout restartPanel = new HorizontalLayout(restartMessage, restart);
		restartPanel.setSpacing(true);
		restartPanel.setVisible(presenter.isRestartMessageVisible());

		fileUpload = new BaseUploadField();
		fileUpload.setMultiValue(true);
		if (presenter.hasUpdatePermission()) {
			layout.addComponent(fileUpload);
		}

		Button uploadButton = new BaseButton($("PluginManagementView.upload")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (fileUpload.getValue() != null) {
					StringBuilder errors = new StringBuilder();
					List<TempFileUpload> uploads = (List<TempFileUpload>) fileUpload.getValue();
					for (TempFileUpload upload : uploads) {
						PluginActivationFailureCause cause = presenter.installPlugin(upload.getTempFile());
						if (cause != null) {
							buildErrorMessage(errors, upload, cause);
						}
					}
					String error = errors.toString();
					refreshPluginsList();
					if (StringUtils.isNotBlank(error)) {
						showErrorMessage(error);
					}
				}
			}

		};

		if (presenter.hasUpdatePermission()) {
			layout.addComponents(restartPanel, uploadButton);
		}

		Table table = createPluginsTable();

		layout.addComponent(table);

		return layout;
	}

	private void buildErrorMessage(StringBuilder errors, TempFileUpload upload, PluginActivationFailureCause cause) {
		errors.append(upload.getFileName());
		errors.append(" : ");
		errors.append(cause.toString());
		errors.append("<br/><br/>");
	}

	private Table createPluginsTable() {
		List<ConstellioPluginInfo> plugins = presenter.getAllPlugins();
		Table table = new BaseTable(getClass().getName(), $("PluginManagementView.plugins", plugins.size()));
		Container beanContainer = new BeanItemContainer<>(ConstellioPluginInfo.class);
		table.setContainerDataSource(beanContainer);

		table.addItems(plugins);
		table.addGeneratedColumn("enable", new ColumnGenerator() {
			@Override
			public Component generateCell(final Table source, final Object itemId, final Object columnId) {
				CheckBox enable = new CheckBox() {
					@Override
					public boolean isVisible() {
						return presenter.isEnableOrDisablePossible((ConstellioPluginInfo) itemId);
					}
				};
				enable.setValue(presenter.isEnabled((ConstellioPluginInfo) itemId));
				enable.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.enablePlugin((ConstellioPluginInfo) itemId, (Boolean) event.getProperty().getValue());
					}
				});
				return enable;
			}
		});
		table.setPageLength(Math.min(15, plugins.size()));
		table.setWidth("100%");
		//table.setColumnHeader("code", $("PluginManagementView.code"));
		table.setColumnHeader("title", $("PluginManagementView.title"));
		table.setColumnHeader("lastInstallDate", $("PluginManagementView.lastInstallDate"));
		table.setColumnHeader("pluginActivationFailureCause", $("PluginManagementView.pluginActivationFailureCause"));
		table.setColumnHeader("pluginStatus", $("PluginManagementView.pluginStatus"));
		//table.setColumnHeader("requiredConstellioVersion", $("PluginManagementView.requiredConstellioVersion"));
		table.setColumnHeader("version", $("PluginManagementView.version"));
		table.setColumnHeader("enable", "");
		table.setColumnWidth("enable", 40);
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				ConstellioPluginInfo constellioPluginInfo = (ConstellioPluginInfo) event.getItemId();
				if ("pluginActivationFailureCause".equals(event.getPropertyId())
					&& constellioPluginInfo.getPluginActivationFailureCause() != null) {
					Window window = new BaseWindow(
							$("PluginActivationFailureCause." + constellioPluginInfo.getPluginActivationFailureCause()
									.getCode()));
					window.center();
					window.setWidth("50%");
					VerticalLayout vLayout = new VerticalLayout();
					vLayout.setHeightUndefined();
					TextArea textArea = new TextArea();
					textArea.setSizeFull();
					String stackTrace = constellioPluginInfo.getStackTrace() != null ? constellioPluginInfo.getStackTrace() : "";
					textArea.setValue(stackTrace);
					vLayout.addComponent(textArea);
					window.setContent(vLayout);
					UI.getCurrent().addWindow(window);
				}
			}
		});
		table.setConverter("pluginActivationFailureCause", newConstellioPluginStatusFailureCauseConverter());
		table.setConverter("pluginStatus", newConstellioPluginStatusConverter());
		table.setVisibleColumns("title", "lastInstallDate", "pluginActivationFailureCause", "pluginStatus",
				/*"requiredConstellioVersion",*/ "version", "enable");
		return table;
	}

	//FIXME
	private void refreshPluginsList() {
		navigateTo().pluginManagement();
	}

	private Converter<String, ConstellioPluginStatus> newConstellioPluginStatusConverter() {
		return new Converter<String, ConstellioPluginStatus>() {
			@Override
			public ConstellioPluginStatus convertToModel(String value,
														 Class<? extends ConstellioPluginStatus> targetType,
														 Locale locale)
					throws ConversionException {
				return null;
			}

			@Override
			public String convertToPresentation(ConstellioPluginStatus value, Class<? extends String> targetType,
												Locale locale)
					throws ConversionException {
				return $("ConstellioPluginStatus." + value.getCode());
			}

			@Override
			public Class<ConstellioPluginStatus> getModelType() {
				return ConstellioPluginStatus.class;
			}

			@Override
			public Class<String> getPresentationType() {
				return String.class;
			}
		};
	}

	private Converter<String, PluginActivationFailureCause> newConstellioPluginStatusFailureCauseConverter() {
		return new Converter<String, PluginActivationFailureCause>() {
			@Override
			public PluginActivationFailureCause convertToModel(String value,
															   Class<? extends PluginActivationFailureCause> targetType,
															   Locale locale)
					throws ConversionException {
				return null;
			}

			@Override
			public String convertToPresentation(PluginActivationFailureCause value, Class<? extends String> targetType,
												Locale locale)
					throws ConversionException {
				if (value == null) {
					return "";
				} else {
					return $("PluginManagementView.openError");
				}
			}

			@Override
			public Class<PluginActivationFailureCause> getModelType() {
				return PluginActivationFailureCause.class;
			}

			@Override
			public Class<String> getPresentationType() {
				return String.class;
			}
		};
	}
}
