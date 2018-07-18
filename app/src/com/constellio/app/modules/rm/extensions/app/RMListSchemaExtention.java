package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.ListSchemaExtention;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandParams;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandReturnParams;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMListSchemaExtention extends ListSchemaExtention {

    @Override
    public List<ListSchemaExtraCommandReturnParams> getExtraCommands(final ListSchemaExtraCommandParams listSchemaExtraCommandParams) {
        List<ListSchemaExtraCommandReturnParams> listSchemaExtraCommandParams1 = new ArrayList<>();
        if(listSchemaExtraCommandParams.getSchemaVO().getCode().startsWith(Folder.SCHEMA_TYPE)) {
            listSchemaExtraCommandParams1.add(new ListSchemaExtraCommandReturnParams(new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("schemaCode", listSchemaExtraCommandParams.getSchemaVO().getCode());
                    String params = ParamUtils.addParams(NavigatorConfigurationService.FOLDER_UNIQUE_KEY_METADATA_CONFIGURATOR, parameters);
                    listSchemaExtraCommandParams.getView().navigate().to(RMViews.class).folderSummaryColumn(params);
                }
            }, $("ListSchemaViewImpl.menu.resumeConfiguration"), new ThemeResource("images/icons/config/display-config-summary-column.png")));

            listSchemaExtraCommandParams1.add(new ListSchemaExtraCommandReturnParams(new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("schemaCode", listSchemaExtraCommandParams.getSchemaVO().getCode());
                    String params = ParamUtils.addParams(NavigatorConfigurationService.FOLDER_UNIQUE_KEY_METADATA_CONFIGURATOR, parameters);
                    listSchemaExtraCommandParams.getView().navigate().to(RMViews.class).folderUnicityConfigurator(params);
                }
            }, $("ListSchemaViewImpl.menu.uniquekeyConfiguration"), new ThemeResource("images/icons/config/display-config-unicity-column.png")));
        }

        return listSchemaExtraCommandParams1;
    }
}
