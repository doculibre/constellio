package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.ListSchemaExtention;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandParams;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandReturnParams;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.vaadin.ui.MenuBar;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMListSchemaExtention extends ListSchemaExtention {

    @Override
    public List<ListSchemaExtraCommandReturnParams> getExtraCommands(final ListSchemaExtraCommandParams listSchemaExtraCommandParams) {
        List<ListSchemaExtraCommandReturnParams> listSchemaExtraCommandParams1 = new ArrayList<>();
        if(listSchemaExtraCommandParams.getSchemaVO().getCode().startsWith(Folder.SCHEMA_TYPE)) {
            listSchemaExtraCommandParams1.add(new ListSchemaExtraCommandReturnParams(new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    listSchemaExtraCommandParams.getView().navigate().to(RMViews.class);
                }
            }, $("ListSchemaViewImpl.menu.resumeConfiguration")));
        }

        return listSchemaExtraCommandParams1;
    }
}
