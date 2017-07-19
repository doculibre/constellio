package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.ui.entities.PrintableReportVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListPrintableReportViewImpl extends BaseViewImpl implements ListPrintableReportView{

    public static final String TYPE_TABLE = "types";

    private static final PrintableReportListPossibleType[] SCHEMA_INDEX_ARRAY = new PrintableReportListPossibleType[] {PrintableReportListPossibleType.FOLDER, PrintableReportListPossibleType.DOCUMENT, PrintableReportListPossibleType.TASK};

    private List<PrintableReportVO> printableReportVOS;
    private TabSheet tabSheet;
    private PrintableReportListPossibleType currentSchema;
    private ListPrintableReportPresenter presenter = new ListPrintableReportPresenter(this);
    private VerticalLayout mainLayout;
    private Button addLabelButton, downloadTemplateButton;
    private GetXmlButtonV2 getXmlButtonV2;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        this.currentSchema = PrintableReportListPossibleType.FOLDER;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        mainLayout = new VerticalLayout();

        tabSheet = new TabSheet();
        tabSheet.addTab(createFolderTable(), $("PrintableReport.tabs.folderTitle"));
        tabSheet.addTab(createDocumentTable(), $("PrintableReport.tabs.documentTitle"));
        tabSheet.addTab(createTaskTable(), $("PrintableReport.tabs.taskTitle"));

        tabSheet.addSelectedTabChangeListener(new TabsChangeListener());
        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    private Table createFolderTable() {
        Container container = new RecordVOLazyContainer(presenter.getPrintableReportFolderDataProvider());
        ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container,  "buttons");
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportEditButton(itemId, currentSchema);
            }
        });
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportDisplayButton(itemId, currentSchema);
            }
        });

        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportDeleteButton(itemId, currentSchema);
            }
        });
        container = buttonsContainerForFolder;
        Table tableFolder = new RecordVOTable($("ListSchemaTypeView.tableTitle"), buttonsContainerForFolder);
        tableFolder = setTableProperty(tableFolder, container.size());
        return tableFolder;
    }

    private Table createDocumentTable() {
        Container container = new RecordVOLazyContainer(presenter.getPrintableReportDocumentDataProvider());
        ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container,  "buttons");
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportEditButton(itemId, currentSchema);
            }
        });
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportDisplayButton(itemId, currentSchema);
            }
        });

        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportDeleteButton(itemId, currentSchema);
            }
        });
        container = buttonsContainerForFolder;
        Table tableFolder = new RecordVOTable($("ListSchemaTypeView.tableTitle"), buttonsContainerForFolder);
        tableFolder = setTableProperty(tableFolder, container.size());
        return tableFolder;
    }

    private Table createTaskTable() {
        Container container = new RecordVOLazyContainer(presenter.getPrintableReportTaskDataProvider());
        ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container,  "buttons");
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportEditButton(itemId, currentSchema);
            }
        });
        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportDisplayButton(itemId, currentSchema);
            }
        });

        buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new PrintableReportDeleteButton(itemId, currentSchema);
            }
        });
        container = buttonsContainerForFolder;
        Table tableFolder = new RecordVOTable($("ListSchemaTypeView.tableTitle"), buttonsContainerForFolder);
        tableFolder = setTableProperty(tableFolder, container.size());
        return tableFolder;
    }

    private Table setTableProperty(Table table, int maxSize) {
        table.setSizeFull();
        table.setPageLength(Math.min(15, maxSize));
        table.setColumnHeader("buttons", "");
        table.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
        table.setColumnExpandRatio("caption", 1);
        table.addStyleName(TYPE_TABLE);
        return table;
    }

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
        List<Button> actionMenuButtons = new ArrayList<Button>();

        addLabelButton = new AddButton($("PrintableReport.menu.addNewReport")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.addLabelButtonClicked();
            }
        };

        getXmlButtonV2 = new GetXmlButtonV2($("DisplayLabelViewImpl.menu.getXMLButton"), $("DisplayLabelViewImpl.menu.getXMLButton"), getConstellioFactories().getAppLayerFactory(), getCollection(), this, currentSchema);

        downloadTemplateButton = new Button($("DisplayLabelViewImpl.menu.getTemplate"));
        StreamResource zip = presenter.createResource();
        FileDownloader fileDownloader = new FileDownloader(zip);
        fileDownloader.extend(downloadTemplateButton);



        actionMenuButtons.add(addLabelButton);
        actionMenuButtons.add(getXmlButtonV2);
        actionMenuButtons.add(downloadTemplateButton);
        return actionMenuButtons;
    }

    @Override
    protected String getTitle() {
        return $("PrintableReport.title");
    }

    private class PrintableReportEditButton extends EditButton {
        private String itemId;
        private PrintableReportListPossibleType currentSchema;

        public PrintableReportEditButton(Object itemId, PrintableReportListPossibleType currentSchema) {
            RecordVO item = presenter.getRecordsWithIndex(currentSchema, itemId + "");
            if(item != null) {
                this.itemId = item.getId();
                this.currentSchema = currentSchema;
            }
        }

        @Override
        protected void buttonClick(ClickEvent event) {
            presenter.editButtonClicked(itemId, currentSchema);
        }

        @Override
        public boolean isVisible() {
            RecordVO ret = presenter.getRecordsWithIndex(currentSchema, itemId);
            return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
        }
    }

    private class PrintableReportDisplayButton extends DisplayButton {
        private String itemId;
        private PrintableReportListPossibleType currentSchema;

        public PrintableReportDisplayButton(Object itemId, PrintableReportListPossibleType currentSchema) {
            RecordVO item = presenter.getRecordsWithIndex(currentSchema, itemId + "");
            if(item != null) {
                this.itemId = item.getId();
                this.currentSchema = currentSchema;
            }
        }

        @Override
        protected void buttonClick(ClickEvent event) {
            presenter.displayButtonClicked(itemId, currentSchema);
        }
    }

    private class PrintableReportDeleteButton extends DeleteButton {
        private String itemId;
        private PrintableReportListPossibleType currentSchema;

        public PrintableReportDeleteButton(Object itemId, PrintableReportListPossibleType currentSchema) {
            RecordVO item = presenter.getRecordsWithIndex(currentSchema, itemId + "");
            if(item != null) {
                this.itemId = item.getId();
                this.currentSchema = currentSchema;
            }
        }

        @Override
        protected void confirmButtonClick(ConfirmDialog dialog) {
            presenter.removeRecord(itemId, currentSchema);
        }

        @Override
        public boolean isVisible() {
            RecordVO ret = presenter.getRecordsWithIndex(currentSchema, itemId);
            return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
        }
    }

    private class TabsChangeListener implements TabSheet.SelectedTabChangeListener {

        @Override
        public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
            TabSheet eventSource = (TabSheet) event.getSource();
            currentSchema = SCHEMA_INDEX_ARRAY[tabSheet.getTabPosition(eventSource.getTab(eventSource.getSelectedTab()))];
            ListPrintableReportViewImpl.this.getXmlButtonV2.setCurrentSchema(currentSchema);
        }
    }
}
