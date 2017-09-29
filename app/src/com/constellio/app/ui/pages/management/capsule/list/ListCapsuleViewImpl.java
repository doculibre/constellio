package com.constellio.app.ui.pages.management.capsule.list;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.labels.ListLabelViewImpl.TYPE_TABLE;

public class ListCapsuleViewImpl extends BaseViewImpl implements ListCapsuleView {
    private ListCapsulePresenter presenter;

    @Override
    protected String getTitle() {
        return $("ListCapsuleViewImpl.title");
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new ListCapsulePresenter(this);
    }

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new BaseButton($("ListCapsuleViewImpl.addCapsule")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                navigateTo().addEditCapsule(null);
            }
        });
        return buttons;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        Container tableContainer = new RecordVOLazyContainer(presenter.getCapsuleDataProvider());
        ButtonsContainer buttonTableContainer = new ButtonsContainer(tableContainer, "buttons");
        buttonTableContainer.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new DisplayButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        presenter.displayButtonClicked(presenter.getRecordsWithIndex(itemId));
                    }
                };
            }
        });
        buttonTableContainer.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new EditButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        presenter.editButtonClicked(presenter.getRecordsWithIndex(itemId));
                    }
                };
            }
        });
        buttonTableContainer.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new DeleteButton() {

                    @Override
                    protected void confirmButtonClick(ConfirmDialog dialog) {
                        presenter.deleteButtonClicked(presenter.getRecordsWithIndex(itemId));
                    }
                };
            }
        });

        tableContainer = buttonTableContainer;
        Table table = new RecordVOTable($("ListCapsuleViewImpl.title"), tableContainer);
        setTableProperty(table, tableContainer.size());
        return table;
    }


}
