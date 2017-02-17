package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.LabelVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListLabelViewImpl extends BaseViewImpl implements AddEditLabelView {
    private List<LabelVO> listLabel;
    private TabSheet tabSheet;
    private AddEditLabelPresenter presenter;
    private Component folderDisplay, containerDisplay;
    private VerticalLayout mainLayout;
    private ReportUtils ru;
    private Button addLabelButton;
    private String currentSchema;
    final private GetXMLButton getXMLButton = new GetXMLButton($("DisplayLabelViewImpl.menu.getXMLButton"), $("DisplayLabelViewImpl.menu.getXMLButton"), getConstellioFactories().getAppLayerFactory(), getSessionContext().getCurrentCollection(), this);
    public static final String TYPE_TABLE = "types";

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        mainLayout = new VerticalLayout();
        presenter = new AddEditLabelPresenter(this);
        folderDisplay = new CustomComponent();
        containerDisplay = new CustomComponent();
        this.currentSchema = Folder.SCHEMA_TYPE;

//        final RecordVOLazyContainer FolderLabel = new RecordVOLazyContainer(presenter.getLabelFolderDataProvider());
//        final RecordVOTable FolderLabelTable = new RecordVOTable($("DisplayLabelViewImpl.tabs.table.header"), FolderLabel);
//        FolderLabelTable.setWidth("100%");
//
//        final RecordVOLazyContainer ContainerLabel = new RecordVOLazyContainer(presenter.getLabelContainerDataProvider());
//        RecordVOTable ContainerLabelTable = new RecordVOTable($("DisplayLabelViewImpl.tabs.table.header"), ContainerLabel);
//        ContainerLabelTable.setWidth("100%");

//        folderDisplay.setWidth("100%");
//        containerDisplay.setWidth("100%");
        final SchemaTypeVODataProvider dataProvider = presenter.getDataProvider();
        ru = new ReportUtils(getSessionContext().getCurrentCollection(), getConstellioFactories().getAppLayerFactory(), getSessionContext().getCurrentUser().getUsername());
        Container folderContainer = new RecordVOLazyContainer(presenter.getLabelFolderDataProvider());
        Container conteneurContainer = new RecordVOLazyContainer(presenter.getLabelContainerDataProvider());
        ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(folderContainer, "buttons");
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new EditButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        presenter.editButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), Folder.SCHEMA_TYPE);
                    }

                    @Override
                    public boolean isVisible() {
                        RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
                        return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
                    }
                };
            }
        });

        ButtonsContainer buttonsContainerForContainer = new ButtonsContainer(conteneurContainer, "buttons");
        buttonsContainerForContainer.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new EditButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        presenter.editButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), ContainerRecord.SCHEMA_TYPE);
                    }

                    @Override
                    public boolean isVisible() {
                        RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
                        return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
                    }
                };
            }
        });

        buttonsContainerForContainer.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new DisplayButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        presenter.displayButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), ContainerRecord.SCHEMA_TYPE);
                    }
                };
            }
        });

        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new DisplayButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        presenter.displayButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), Folder.SCHEMA_TYPE);
                    }
                };
            }
        });

        buttonsContainerForContainer.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new DeleteButton() {
                    @Override
                    protected void confirmButtonClick(ConfirmDialog dialog) {
                        presenter.removeRecord(itemId + "", ListLabelViewImpl.this.currentSchema);
                    }

                    @Override
                    public boolean isVisible() {
                        RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
                        return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
                    }
                };
            }
        });

        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new DeleteButton() {
                    @Override
                    protected void confirmButtonClick(ConfirmDialog dialog) {
                        presenter.removeRecord(itemId + "", ListLabelViewImpl.this.currentSchema);
                    }

                    @Override
                    public boolean isVisible() {
                        RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
                        return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
                    }
                };
            }
        });

        folderContainer = buttonsContainerForFolder;
        conteneurContainer = buttonsContainerForContainer;

        final Table tableFolder = new RecordVOTable($("ListSchemaTypeView.tableTitle"), folderContainer);
        tableFolder.setSizeFull();
        tableFolder.setPageLength(Math.min(15, folderContainer.size()));
        tableFolder.setColumnHeader("buttons", "");
        tableFolder.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
        tableFolder.setColumnExpandRatio("caption", 1);
        tableFolder.addStyleName(TYPE_TABLE);

        Table tableContainer = new RecordVOTable($("ListSchemaTypeView.tableTitle"), conteneurContainer);
        tableContainer.setSizeFull();
        tableContainer.setPageLength(Math.min(15, conteneurContainer.size()));
        tableContainer.setColumnHeader("buttons", "");
        tableContainer.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
        tableContainer.setColumnExpandRatio("caption", 1);
        tableContainer.addStyleName(TYPE_TABLE);


        tabSheet = new TabSheet();
        tabSheet.addTab(tableFolder, $("DisplayLabelViewImpl.tabs.folder"));
        tabSheet.addTab(tableContainer, $("DisplayLabelViewImpl.tabs.container"));
        System.out.println(tabSheet.getTabIndex());
        tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                getXMLButton.setCurrentSchema(tabSheet.getSelectedTab().equals(tableFolder) ? Folder.SCHEMA_TYPE : ContainerRecord.SCHEMA_TYPE);
                ListLabelViewImpl.this.currentSchema = getXMLButton.getCurrentSchema();
                System.out.println(ListLabelViewImpl.this.currentSchema);
            }
        });


        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
        List<Button> actionMenuButtons = new ArrayList<Button>();

        addLabelButton = new AddButton($("DisplayLabelViewImpl.menu.addLabelButton")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.addLabelButtonClicked();
            }
        };


        actionMenuButtons.add(addLabelButton);
        actionMenuButtons.add(getXMLButton);
        return actionMenuButtons;
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {

    }

    @Override
    public void setLabels(List<LabelVO> list) {
        this.listLabel = list;
    }

    @Override
    public void addLabels(LabelVO... items) {
        this.listLabel.addAll(Arrays.asList(items));
    }

    @Override
    protected String getTitle() {
        return $("LabelViewImpl.title");
    }
}
