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
import com.constellio.model.entities.records.Record;
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

    public void addRecordToVoList(RecordVO recordVO) {
        recordVOList.add(recordVO);
    }

    public boolean isNeedToRemovePDFTab(){
        return removePrintableTab;
    }

    public boolean isNeedToRemoveExcelTab() {
        return removeExcelTab;
    }

    private void initLists(){
        generalSchemaList = getGeneralListFromRecordVoList();
        specificsSchemaList = getSpecificsSchemaListFromRecordVOList();
        reportList = getAllReport();
    }

    private List<RecordVO> filterPrintableReportForCurrentCondition() {
        List<RecordVO> currentRecordVO
    }

    protected void defaultElementSelectedValueChangeListener(Property.ValueChangeEvent event) {
        selectedReporType = ((PrintableReportListPossibleType) event.getProperty().getValue());
        if (fillSchemaCombobox(customElementSelected) == null) {
            customElementSelected.setVisible(false);
            customElementSelected.setEnabled(false);
            Iterator<MetadataSchemaVO> setIterator = occurence.getAllCustomMetadataSchemaOccurence().keySet().iterator();
            do {
                selectedSchemaType = setIterator.next().getCode();
            } while (setIterator.hasNext() && !selectedSchemaType.contains(selectedReporType.getSchemaType()));

        } else {
            customElementSelected.setVisible(true);
            customElementSelected.setEnabled(true);
            selectedSchemaType = null;
            reportComboBox.setValue(null);

        }
        if (selectedSchemaType != null && selectedReporType != null) {
            reportComboBox = fillTemplateComboBox(reportComboBox);
        }
        final MetadataSchemaVO firstCustomItem =  ((List<MetadataSchemaVO>)customElementSelected.getItemIds()).get(0);
        customElementSelectedValueChangeListener(new ReportTabButton.customValueChangeEvent((firstCustomItem)));
    }

    protected void customElementSelectedValueChangeListener(Property.ValueChangeEvent event){
        if (event.getProperty() != null && event.getProperty().getValue() != null) {
            selectedSchemaType = ((MetadataSchemaVO) event.getProperty().getValue()).getCode();
            if (selectedSchemaType != null && selectedReporType != null) {
                reportComboBox = fillTemplateComboBox(reportComboBox);
            }
        }
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
        MetadataSchemaType reportType = view.getFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(view.getCollection()).getSchemaType(PrintableReport.SCHEMA_TYPE);
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getCollection(), view.getFactory());
        List<PrintableReport> allPrintableReport = rm.wrapPrintableReports(searchServices.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators.from(reportType).returnAll())));
        for(PrintableReport currentReport: allPrintableReport) {
            MetadataSchemaVO metadataSchemaVO = new MetadataSchemaToVOBuilder().build(currentReport.getSchema(), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
            printableReportVOS.add(builder.build(currentReport.getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, metadataSchemaVO, view.getSessionContext()));
        }
        return printableReportVOS;
    }

    protected class MetadataSchemaCounter {
        Map<PrintableReportListPossibleType, Integer> defaultMetadataOccurence;
        Map<MetadataSchemaVO, Integer> customMetadataOccurence;

        public MetadataSchemaCounter() {
            defaultMetadataOccurence = new HashMap<>();
            customMetadataOccurence = new HashMap<>();
        }

        void addDefaultSchema(PrintableReportListPossibleType metadataSchemaVO) {
            defaultMetadataOccurence.put(metadataSchemaVO, 1);
        }

        void addCustomSchema(MetadataSchemaVO metadataSchemaVO) {
            customMetadataOccurence.put(metadataSchemaVO, 1);
        }

        void incrementDefaultSchema(PrintableReportListPossibleType metadataSchemaVO) {
            this.defaultMetadataOccurence.put(metadataSchemaVO, getDefaultMetadataSchemaOccurence(metadataSchemaVO) + 1);
        }

        void incrementCustomSchema(MetadataSchemaVO metadataSchemaVO) {
            this.customMetadataOccurence.put(metadataSchemaVO, getCustomMetadataSchemaOccurence(metadataSchemaVO) + 1);
        }

        int getDefaultMetadataSchemaOccurence(PrintableReportListPossibleType metadataSchemaVO) {
            return this.defaultMetadataOccurence.get(metadataSchemaVO);
        }

        int getCustomMetadataSchemaOccurence(MetadataSchemaVO metadataSchemaVO) {
            return this.customMetadataOccurence.get(metadataSchemaVO);
        }

        Map<PrintableReportListPossibleType, Integer> getAllDefaultMetadataSchemaOccurence() {
            return this.defaultMetadataOccurence;
        }

        Map<MetadataSchemaVO, Integer> getAllCustomMetadataSchemaOccurence() {
            return this.customMetadataOccurence;
        }

        void addOrIncrementDefaultSchema(PrintableReportListPossibleType metadataSchemaVO) {
            if (this.doesDefaultMetadataSchemaContains(metadataSchemaVO)) {
                this.incrementDefaultSchema(metadataSchemaVO);
            } else {
                this.addDefaultSchema(metadataSchemaVO);
            }
        }

        void addOrIncrementCustomSchema(MetadataSchemaVO metadataSchemaVO) {
            if (this.doesCustomMetadataSchemaContains(metadataSchemaVO)) {
                this.incrementCustomSchema(metadataSchemaVO);
            } else {
                this.addCustomSchema(metadataSchemaVO);
            }
        }

        boolean doesDefaultMetadataSchemaContains(PrintableReportListPossibleType metadataSchemaVO) {
            return this.defaultMetadataOccurence.keySet().contains(metadataSchemaVO);
        }

        boolean doesCustomMetadataSchemaContains(MetadataSchemaVO metadataSchemaVO) {
            return this.customMetadataOccurence.keySet().contains(metadataSchemaVO);
        }

        int getNumberOfCustomSchemaOccurence() {
            return this.customMetadataOccurence.size();
        }

        int getNumberOfDefaultSchemaOccurence() {
            return this.defaultMetadataOccurence.size();
        }
    }

    protected static class customValueChangeEvent implements Property.ValueChangeEvent {
        private Object value;
        public customValueChangeEvent(Object value) {
            this.value = value;
        }
        @Override
        public Property getProperty() {
            final Object finalValue = this.value;
            return new Property() {
                @Override
                public Object getValue() {
                    return finalValue;
                }

                @Override
                public void setValue(Object newValue) throws ReadOnlyException {

                }

                @Override
                public Class getType() {
                    return null;
                }

                @Override
                public boolean isReadOnly() {
                    return false;
                }

                @Override
                public void setReadOnly(boolean newStatus) {

                }
            };
        }
    }
}
