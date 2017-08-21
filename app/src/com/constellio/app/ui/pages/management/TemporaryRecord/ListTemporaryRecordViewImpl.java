package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListTemporaryRecordViewImpl extends BaseViewImpl implements ListTemporaryRecordView{

    private ListTemporaryRecordPresenter presenter;

    public ListTemporaryRecordViewImpl() {presenter = new ListTemporaryRecordPresenter(this); }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addStyleName("batch-processes");
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        TabSheet tabSheet = new TabSheet();
        tabSheet.addTab(newImportTable(), $("ListImportExportView.importTable"));
        tabSheet.addTab(newExportTable(), $("ListImportExportView.exportTable"));
        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    private BaseTable newImportTable() {
        RecordVOTable importTable = new RecordVOTable(presenter.getImportDataProvider()) {
            @Override
            protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
                return super.buildMetadataComponent(metadataValue, recordVO);
            }
        };
        importTable.setWidth("98%");
        importTable.setCellStyleGenerator(newImportStyleGenerator());

        return importTable;
    }

    private BaseTable newExportTable() {
        RecordVOTable exportTable = new RecordVOTable(presenter.getExportDataProvider()) {
            @Override
            protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
                if(metadataValue.getMetadata().getLocalCode().equals(TemporaryRecord.CONTENT) && metadataValue.getValue() != null) {
                    ContentVersionVO content = metadataValue.getValue();
                    return new DownloadContentVersionLink(content, content.getFileName());
                } else {
                    return super.buildMetadataComponent(metadataValue, recordVO);
                }
            }
        };
        exportTable.setWidth("98%");

        return exportTable;
    }

    private Table.CellStyleGenerator newImportStyleGenerator() {
        return new Table.CellStyleGenerator() {

            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {
                try {
                    RecordVOItem item = (RecordVOItem) source.getItem(itemId);
                    RecordVO record = item.getRecord();
                    MetadataVO errors = record.getMetadata(ImportAudit.ERRORS);
                    if(errors != null && !((List)record.getMetadataValue(errors).getValue()).isEmpty()) {
                        return "error";
                    }
                } catch (Exception e) {

                }
                return null;
            }
        };
    }
}
