package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.PrintableReportVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.ReportToVOBuilder;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;

import java.util.*;

import static java.util.Arrays.asList;

class ReportTabButtonPresenter  {
    private List<RecordVO> recordVOList;
    private boolean removeExcelTab = false, removePrintableTab = false;
    private ReportTabButton view;
    List<PrintableReportListPossibleType> generalSchemaList;
    List<MetadataSchemaVO> specificsSchemaList;
    List<RecordVO> reportList;

    public ReportTabButtonPresenter(ReportTabButton view) {
        recordVOList = new ArrayList<>();
        this.view = view;
        initLists();
    }

    public void setRecordVoList(RecordVO... recordVOS) {
        if (recordVOS.length > 0) {
            recordVOList.addAll(asList(recordVOS));
        }
    }

    public List<RecordVO> getRecordVOList() {
        return recordVOList;
    }

    public List<String> getRecordVOIdFilteredList(MetadataSchemaVO schemaVO){
        List<String> ids = new ArrayList<>();
        for(RecordVO recordVO : recordVOList) {
            if(recordVO.getSchema().equals(schemaVO)) {
                ids.add(recordVO.getId());
            }
        }
        return ids;
    }

    public void addRecordToVoList(RecordVO recordVO) {
        recordVOList.add(recordVO);
    }

    public boolean isNeedToRemovePDFTab(){
        return removePrintableTab;
    }

    public void setNeedToRemoveExcelTab(boolean needToRemove) {
        this.removeExcelTab = needToRemove;
    }

    public boolean isNeedToRemoveExcelTab() {
        return removeExcelTab;
    }

    public List<PrintableReportListPossibleType> getAllGeneralSchema(){
        List<PrintableReportListPossibleType> currentPossibleGeneralSchema = new ArrayList<>();
        for(RecordVO recordVO : recordVOList ) {
            PrintableReportListPossibleType currentGeneralSchema = PrintableReportListPossibleType.getValueFromSchemaType(recordVO.getSchema().getTypeCode());
            if(!currentPossibleGeneralSchema.contains(currentGeneralSchema)) {
                currentPossibleGeneralSchema.add(currentGeneralSchema);
            }
        }
        return currentPossibleGeneralSchema;
    }

    public List<MetadataSchemaVO> getAllCustomSchema(PrintableReportListPossibleType generalSchema){
        List<MetadataSchemaVO> currentPossibleCustomSchema = new ArrayList<>();
        for(RecordVO recordVO : recordVOList ) {
            MetadataSchemaVO currentCustomSchema = recordVO.getSchema();
            if(!currentPossibleCustomSchema.contains(currentCustomSchema) && PrintableReportListPossibleType.getValueFromSchemaType(currentCustomSchema.getTypeCode()).equals(generalSchema)) {
                currentPossibleCustomSchema.add(currentCustomSchema);
            }
        }
        return currentPossibleCustomSchema;
    }

    public List<RecordVO> getAllAvailableReport(MetadataSchemaVO currentCustomSchema) {
        List<RecordVO> currentAvailableReport = new ArrayList<>();
        for(RecordVO recordVO : reportList) {
            if(recordVO.<String>get(PrintableReport.RECORD_SCHEMA).equals(currentCustomSchema.getCode())) {
                currentAvailableReport.add(recordVO);
            }
        }
        return currentAvailableReport;
    }

    public Content getReportContent(RecordVO recordVO) {
        Record record = this.view.getFactory().getModelLayerFactory().newRecordServices().getDocumentById(recordVO.getId());
        MetadataSchema schema = this.view.getFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(this.view.getCollection()).getSchema(PrintableReport.SCHEMA_NAME);
        return record.get(schema.getMetadata(PrintableReport.JASPERFILE));
    }

    private void initLists(){
        generalSchemaList = getGeneralListFromRecordVoList();
        specificsSchemaList = getSpecificsSchemaListFromRecordVOList();
        reportList = getAllReport();
    }

    private List<RecordVO> filterPrintableReportForCurrentCondition(PrintableReportListPossibleType selectedGeneralSchema, String selectedCustomSchema) {
        List<RecordVO> currentRecordVO = reportList;
        currentRecordVO = filterGeneralSchemaList(currentRecordVO, selectedGeneralSchema);
        currentRecordVO = filterCustomSchemaList(currentRecordVO, selectedCustomSchema);
        return currentRecordVO;
    }

    private List<RecordVO> filterGeneralSchemaList(List<RecordVO> recordVOList, PrintableReportListPossibleType selectedGeneralSchema) {
        List<RecordVO> filteredVOs = new ArrayList<>();
        for(RecordVO record : recordVOList) {
            if(record.getSchema().getTypeCode().equals(selectedGeneralSchema.getSchemaType())){
                filteredVOs.add(record);
            }
        }
        return filteredVOs;
    }

    private List<RecordVO> filterCustomSchemaList(List<RecordVO> recordVOList, String selectedCustomSchema) {
        List<RecordVO> filteredVOs = new ArrayList<>();
        for(RecordVO record : recordVOList) {
            if(record.getSchema().getCode().equals(selectedCustomSchema)){
                filteredVOs.add(record);
            }
        }
        return filteredVOs;
    }

    private List<PrintableReportListPossibleType> getGeneralListFromRecordVoList(){
        List<PrintableReportListPossibleType> generalList = new ArrayList<>();
        for(RecordVO recordvo: this.recordVOList) {
            PrintableReportListPossibleType type = PrintableReportListPossibleType.getValueFromSchemaType(recordvo.getSchema().getTypeCode());
            if(!generalList.contains(type)) {
                generalList.add(type);
            }
        }
        return generalList;
    }

    private List<MetadataSchemaVO> getSpecificsSchemaListFromRecordVOList() {
        List<MetadataSchemaVO> specificList = new ArrayList<>();
        for(RecordVO recordvo: this.recordVOList) {
            MetadataSchemaVO metadataSchemaVO = recordvo.getSchema();
            if(!specificList.contains(metadataSchemaVO)) {
                specificList.add(metadataSchemaVO);
            }
        }
        return specificList;
    }

    private List<RecordVO> getAllReport(){
        List<RecordVO> printableReportVOS = new ArrayList<>();
        RecordToVOBuilder builder = new RecordToVOBuilder();
        SearchServices searchServices = view.getFactory().getModelLayerFactory().newSearchServices();
        MetadataSchema reportSchema = view.getFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(view.getCollection()).getSchema(PrintableReport.SCHEMA_NAME);
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getCollection(), view.getFactory());
        List<PrintableReport> allPrintableReport = rm.wrapPrintableReports(searchServices.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators.from(reportSchema).returnAll())));
        for(PrintableReport currentReport: allPrintableReport) {
            MetadataSchemaVO metadataSchemaVO = new MetadataSchemaToVOBuilder().build(currentReport.getSchema(), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
            printableReportVOS.add(builder.build(currentReport.getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, metadataSchemaVO, view.getSessionContext()));
        }
        if(printableReportVOS.isEmpty()) {
            this.removePrintableTab = true;
        }
        return printableReportVOS;
    }
}
