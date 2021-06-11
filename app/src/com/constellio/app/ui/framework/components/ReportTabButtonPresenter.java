package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

class ReportTabButtonPresenter {
	private List<RecordVO> recordVOList;
	private boolean removeExcelTab = false, removeWordTab = false, removePrintableTab = false,
			removeXlsxTab = false, removeHtmlTab = false;
	private ReportTabButton view;
	List<PrintableReportListPossibleType> generalSchemaList;
	List<MetadataSchemaVO> specificsSchemaList;
	List<RecordVO> reportList;
	private LogicalSearchQuery logicalSearchQuery;

	public ReportTabButtonPresenter(ReportTabButton view) {
		recordVOList = new ArrayList<>();
		this.view = view;
		initLists();
	}

	public void setRecordVoList(RecordVO... recordVOS) {
		recordVOList.clear();
		if (recordVOS.length > 0) {
			recordVOList.addAll(asList(recordVOS));
		}
	}

	public List<RecordVO> getRecordVOList() {
		return recordVOList;
	}

	public List<String> getRecordVOIdFilteredList(MetadataSchemaVO schemaVO) {
		List<String> ids = new ArrayList<>();
		boolean defaultSchema = schemaVO.getCode().endsWith("_default");
		for (RecordVO recordVO : recordVOList) {
			if (recordVO.getSchema().equals(schemaVO) ||
				(defaultSchema && recordVO.getSchema().getTypeCode().equals(schemaVO.getTypeCode()))) {
				ids.add(recordVO.getId());
			}
		}
		return ids;
	}

	public void addRecordToVoList(RecordVO recordVO) {
		recordVOList.add(recordVO);
	}

	public boolean isNeedToRemovePDFTab() {
		return removePrintableTab;
	}

	public boolean isNeedToRemoveWordTab() {
		return removeWordTab;
	}

	public boolean isNeedToRemoveXlsxTab() {
		return removeXlsxTab;
	}

	public boolean isNeedToRemoveHtmlTab() {
		return removeHtmlTab;
	}

	public void setNeedToRemoveExcelTab(boolean needToRemove) {
		this.removeExcelTab = needToRemove;
	}

	public boolean isNeedToRemoveExcelTab() {
		return removeExcelTab;
	}

	public List<PrintableReportListPossibleType> getAllGeneralSchema() {
		Set<PrintableReportListPossibleType> currentPossibleGeneralSchema = new HashSet<>();
		LogicalSearchQuery query = view.getLogicalSearchQuery(null);
		if (query != null) {
			SPEQueryResponse response = view.getFactory().getModelLayerFactory().newSearchServices().query(query.setNumberOfRows(0).addFieldFacet(Schemas.SCHEMA.getDataStoreCode()));
			List<FacetValue> fieldFacetValues = response.getFieldFacetValues(Schemas.SCHEMA.getDataStoreCode());
			for (FacetValue schema : fieldFacetValues) {
				if (schema.getQuantity() > 0) {
					currentPossibleGeneralSchema.add(PrintableReportListPossibleType.getValueFromSchemaType(SchemaUtils.getSchemaTypeCode(schema.getValue())));
				}
			}
		} else {
			for (RecordVO recordVO : recordVOList) {
				PrintableReportListPossibleType currentGeneralSchema = PrintableReportListPossibleType.getValueFromSchemaType(recordVO.getSchema().getTypeCode());
				currentPossibleGeneralSchema.add(currentGeneralSchema);
			}
		}

		return new ArrayList<>(currentPossibleGeneralSchema);
	}

	public List<MetadataSchemaVO> getAllCustomSchema(PrintableReportListPossibleType generalSchema) {
		Set<MetadataSchemaVO> currentPossibleCustomSchema = new HashSet<>();
		MetadataSchemaToVOBuilder metadataSchemaToVOBuilder = new MetadataSchemaToVOBuilder();
		MetadataSchemaTypes schemaTypes = view.getFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(view.getCollection());
		LogicalSearchQuery query = view.getLogicalSearchQuery(null);
		if (query != null) {
			SPEQueryResponse response = view.getFactory().getModelLayerFactory().newSearchServices().query(query.setNumberOfRows(0).addFieldFacet(Schemas.SCHEMA.getDataStoreCode()));
			List<FacetValue> fieldFacetValues = response.getFieldFacetValues(Schemas.SCHEMA.getDataStoreCode());
			for (FacetValue schema : fieldFacetValues) {
				if (schema.getQuantity() > 0 && schema.getValue().startsWith(generalSchema.getSchemaType() + "_")) {
					currentPossibleCustomSchema.add(metadataSchemaToVOBuilder.build(schemaTypes.getSchema(schema.getValue()), RecordVO.VIEW_MODE.DISPLAY));
				}
			}
		} else {
			for (RecordVO recordVO : recordVOList) {
				MetadataSchemaVO currentCustomSchema = recordVO.getSchema();
				if (PrintableReportListPossibleType.getValueFromSchemaType(currentCustomSchema.getTypeCode()).equals(generalSchema)) {
					currentPossibleCustomSchema.add(currentCustomSchema);
				}
			}
		}
		return new ArrayList<>(currentPossibleCustomSchema);
	}

	public List<RecordVO> getAllAvailableReport(MetadataSchemaVO currentCustomSchema) {
		List<RecordVO> currentAvailableReport = new ArrayList<>();
		for (RecordVO recordVO : reportList) {
			String recordType = recordVO.get(PrintableReport.RECORD_TYPE);
			String recordSchema = recordVO.get(PrintableReport.RECORD_SCHEMA);
			if (recordType.equals(currentCustomSchema.getTypeCode()) &&
				(recordSchema == null || recordSchema.equals(currentCustomSchema.getCode()))) {
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

	private void initLists() {
		generalSchemaList = getGeneralListFromRecordVoList();
		specificsSchemaList = getSpecificsSchemaListFromRecordVOList();
		reportList = getAllReport();
	}

	private List<RecordVO> filterPrintableReportForCurrentCondition(
			PrintableReportListPossibleType selectedGeneralSchema, String selectedCustomSchema) {
		List<RecordVO> currentRecordVO = reportList;
		currentRecordVO = filterGeneralSchemaList(currentRecordVO, selectedGeneralSchema);
		currentRecordVO = filterCustomSchemaList(currentRecordVO, selectedCustomSchema);
		return currentRecordVO;
	}

	private List<RecordVO> filterGeneralSchemaList(List<RecordVO> recordVOList,
												   PrintableReportListPossibleType selectedGeneralSchema) {
		List<RecordVO> filteredVOs = new ArrayList<>();
		for (RecordVO record : recordVOList) {
			if (record.getSchema().getTypeCode().equals(selectedGeneralSchema.getSchemaType())) {
				filteredVOs.add(record);
			}
		}
		return filteredVOs;
	}

	private List<RecordVO> filterCustomSchemaList(List<RecordVO> recordVOList, String selectedCustomSchema) {
		List<RecordVO> filteredVOs = new ArrayList<>();
		for (RecordVO record : recordVOList) {
			if (record.getSchema().getCode().equals(selectedCustomSchema)) {
				filteredVOs.add(record);
			}
		}
		return filteredVOs;
	}

	private List<PrintableReportListPossibleType> getGeneralListFromRecordVoList() {
		List<PrintableReportListPossibleType> generalList = new ArrayList<>();
		for (RecordVO recordvo : this.recordVOList) {
			PrintableReportListPossibleType type = PrintableReportListPossibleType.getValueFromSchemaType(recordvo.getSchema().getTypeCode());
			if (!generalList.contains(type)) {
				generalList.add(type);
			}
		}
		return generalList;
	}

	private List<MetadataSchemaVO> getSpecificsSchemaListFromRecordVOList() {
		List<MetadataSchemaVO> specificList = new ArrayList<>();
		for (RecordVO recordvo : this.recordVOList) {
			MetadataSchemaVO metadataSchemaVO = recordvo.getSchema();
			if (!specificList.contains(metadataSchemaVO)) {
				specificList.add(metadataSchemaVO);
			}
		}
		return specificList;
	}

	private List<RecordVO> getAllReport() {
		List<RecordVO> printableReportVOS = new ArrayList<>();
		RecordToVOBuilder builder = new RecordToVOBuilder();
		SearchServices searchServices = view.getFactory().getModelLayerFactory().newSearchServices();
		MetadataSchemaType printableSchemaType = view.getFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(view.getCollection()).getSchemaType(Printable.SCHEMA_TYPE);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getCollection(), view.getFactory());
		List<PrintableReport> allPrintableReport = rm.wrapPrintableReports(searchServices.cachedSearch(new LogicalSearchQuery(
				LogicalSearchQueryOperators.from(printableSchemaType).where(Schemas.SCHEMA).isEqualTo(PrintableReport.SCHEMA_NAME))));
		for (PrintableReport currentReport : allPrintableReport) {
			if (!currentReport.isDisabled()) {
				MetadataSchemaVO metadataSchemaVO = new MetadataSchemaToVOBuilder().build(currentReport.getSchema(), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
				printableReportVOS.add(builder.build(currentReport.getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, metadataSchemaVO, view.getSessionContext()));
			}
		}
		if (printableReportVOS.isEmpty()) {
			this.removePrintableTab = true;
		}
		return printableReportVOS;
	}

	public String getLabelForSchemaType(String schemaType) {
		SessionContext sessionContext = view.getSessionContext();
		return view.getFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(view.getCollection()).getSchemaType(schemaType).getLabel(Language.withLocale(sessionContext.getCurrentLocale()));
	}
}
