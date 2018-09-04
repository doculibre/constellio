package com.constellio.app.modules.rm.extensions.app;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.api.extensions.ListSchemaExtention;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandParams;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandReturnParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;

public class RMListSchemaExtention extends ListSchemaExtention {

	@Override
	public List<ListSchemaExtraCommandReturnParams> getExtraCommands(
			final ListSchemaExtraCommandParams listSchemaExtraCommandParams) {
		List<ListSchemaExtraCommandReturnParams> listSchemaExtraCommandReturnParams = new ArrayList<>();
		SystemConfigurationsManager systemConfigurationsManager = listSchemaExtraCommandParams.getView().getConstellioFactories()
				.getAppLayerFactory().getModelLayerFactory()
				.getSystemConfigurationsManager();

		if (systemConfigurationsManager.getValue(RMConfigs.SHOW_FOLDER_UNICITY_AND_FOLDER_SUMMARY_CONFIG)) {
			if (listSchemaExtraCommandParams.getSchemaVO().getCode().startsWith(Folder.SCHEMA_TYPE)) {
				listSchemaExtraCommandReturnParams.add(new ListSchemaExtraCommandReturnParams(new MenuBar.Command() {
					@Override
					public void menuSelected(MenuBar.MenuItem selectedItem) {
						Map<String, String> parameters = new HashMap<>();
						parameters.put("schemaCode", listSchemaExtraCommandParams.getSchemaVO().getCode());
						String params = ParamUtils
								.addParams(NavigatorConfigurationService.FOLDER_UNIQUE_KEY_METADATA_CONFIGURATOR, parameters);
						listSchemaExtraCommandParams.getView().navigate().to(RMViews.class).folderSummaryConfig(params);
					}
				}, $("ListSchemaViewImpl.menu.resumeConfiguration"),
						new ThemeResource("images/icons/config/display-config-summary-column.png")));

				listSchemaExtraCommandReturnParams.add(new ListSchemaExtraCommandReturnParams(new MenuBar.Command() {
					@Override
					public void menuSelected(MenuBar.MenuItem selectedItem) {
						Map<String, String> parameters = new HashMap<>();
						parameters.put("schemaCode", listSchemaExtraCommandParams.getSchemaVO().getCode());
						String params = ParamUtils
								.addParams(NavigatorConfigurationService.FOLDER_UNIQUE_KEY_METADATA_CONFIGURATOR, parameters);
						listSchemaExtraCommandParams.getView().navigate().to(RMViews.class).folderUnicityConfigurator(params);
					}
				}, $("ListSchemaViewImpl.menu.uniquekeyConfiguration"),
						new ThemeResource("images/icons/config/display-config-unicity-column.png")));
			}
		}

		return listSchemaExtraCommandReturnParams;
	}
}
