package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.ui.entities.*;
import com.constellio.app.ui.framework.components.TitlePanel;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.getLanguage;

public class ListTemporaryRecordViewImpl extends BaseViewImpl implements ListTemporaryRecordView{

    private ListTemporaryRecordPresenter presenter;

    private Map<String, String> tabs = new HashMap<>();

    public ListTemporaryRecordViewImpl() {presenter = new ListTemporaryRecordPresenter(this); }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addStyleName("batch-processes");
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        initTabWithDefaultValues();
        TabSheet tabSheet = new TabSheet();
        for(Map.Entry<String, String> currentTabs : tabs.entrySet()) {
            RecordVODataProvider provider = presenter.getDataProviderFromType(currentTabs.getKey());
            if(provider.size() > 0 ) {
                tabSheet.addTab(buildTable(provider), currentTabs.getValue());
            }
        }
        if(tabSheet.getComponentCount() > 0) {
            mainLayout.addComponent(tabSheet);
        } else {
            mainLayout.addComponent(new TitlePanel($("ListTemporaryRecordViewImpl.noTemporaryReportAvailable")));
        }
        return mainLayout;
    }

    private BaseTable buildTable(RecordVODataProvider provider) {
        RecordVOTable importTable = new RecordVOTable(provider) {
            @Override
            protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
                return super.buildMetadataComponent(metadataValue, recordVO);
            }
        };
        importTable.setWidth("98%");
        importTable.setCellStyleGenerator(newImportStyleGenerator());

        return importTable;
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

    private void initTabWithDefaultValues(){
        MetadataSchemasManager manager = getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager();
        for(MetadataSchema schema : manager.getSchemaTypes(getCollection()).getSchemaType(TemporaryRecord.SCHEMA_TYPE).getCustomSchemas()) {
            tabs.put(schema.getCode(), schema.getLabel(getLanguage()));
        }
    }
}
