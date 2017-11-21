package com.constellio.app.ui.pages.exclusion;

import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.elevations.EditElevationView;
import com.constellio.app.ui.pages.elevations.EditElevationViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.Elevations;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class DeleteExclusionsImpl extends BaseViewImpl implements DeleteExclusionsView {

    DeleteExclusionsView deleteExclusionsView;

    BaseTable baseTable;
    ButtonsContainer buttonsContainer;
    IndexedContainer indexedContainer;

    public DeleteExclusionsImpl(DeleteExclusionsView view) {
        deleteExclusionsView = deleteExclusionsView;
    }

    protected boolean hasPageAccess(String params, User user) {
        return false;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();

        baseTable.addItem();

        return verticalLayout;
    }
}
