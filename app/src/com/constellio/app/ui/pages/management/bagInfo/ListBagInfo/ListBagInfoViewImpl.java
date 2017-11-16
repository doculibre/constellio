package com.constellio.app.ui.pages.management.bagInfo.ListBagInfo;

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
import com.vaadin.ui.Table;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListBagInfoViewImpl extends BaseViewImpl implements ListBagInfoView {
    private ListBagInfoPresenter presenter;

    @Override
    protected String getTitle() {
        return super.getTitle();
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new ListBagInfoPresenter(this);
    }

    @Override
    protected Button.ClickListener getBackButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigateTo().previousView();
            }
        };
    }

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new BaseButton($("ListBagInfoViewImpl.addBagInfo")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                navigateTo().addBagInfo();
            }
        });
        return buttons;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        Container tableContainer = new RecordVOLazyContainer(presenter.getBagInfoDataProvider());
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
        Table table = new RecordVOTable("", tableContainer);
        setTableProperty(table, tableContainer.size());
        return table;
    }
}
