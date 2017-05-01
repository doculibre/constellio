package com.constellio.app.ui.pages.management.schemas.display.report;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.ReportVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.ReportToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ReportDisplayConfigPresenter extends BasePresenter<ReportConfigurationView>{

	private Map<String, String> parameters;

	private boolean isAddMode = true;

	public ReportDisplayConfigPresenter(ReportConfigurationView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public MetadataVODataProvider getDataProvider() {
		return new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection, getSchemaTypeCode()){
			@Override
			public List<MetadataVO> buildList() {
				Set<String> metadataLocalCodeSet = new HashSet<>();
				List<MetadataVO> schemaVOs = new ArrayList<>();
				MetadataSchemaType type = schemaType(getSchemaTypeCode());
				if (type != null) {
					for(MetadataSchema schema:type.getAllSchemas()){
						for (Metadata meta : schema.getMetadatas()) {
							if ((!meta.isSystemReserved() || isSystemReservedAllowedInReport(meta)) && metadataLocalCodeSet.add(meta.getLocalCode())) {
								MetadataVO metadataVO = voBuilder.build(meta, view.getSessionContext());
								if(AllowedMetadataUtil.isAllowedMetadata(metadataVO)){
									schemaVOs.add(metadataVO);
								}
							}
						}
					}
				}
				return schemaVOs;
			}
		};
	}

	public boolean isSystemReservedAllowedInReport(Metadata meta) {
		List<String> allowedMetadatas = Arrays.asList(Schemas.IDENTIFIER.getLocalCode());
		return allowedMetadatas.contains(meta.getLocalCode());
	}

	private String getSelectedReport() {
		return view.getSelectedReport();
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public void saveButtonClicked(List<MetadataVO> metadataVOs) {
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		String reportTile = getSelectedReport();
		String schemaTypeCode = getSchemaTypeCode();
		Report report = reportServices.getReport(schemaTypeCode, reportTile);
		if(report == null){
			MetadataSchema reportSchema = schemaType(Report.SCHEMA_TYPE).getDefaultSchema();
			Record newReportRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(reportSchema);
			report = new Report(newReportRecord, types());
			report.setTitle(reportTile);
			report.setSchemaTypeCode(schemaTypeCode);
		}
		report.setColumnsCount(metadataVOs.size());
		report.setLinesCount(1);
		List<ReportedMetadata> reportedMetadataList = buildReportedMetadataList(metadataVOs);
		report.setReportedMetadata(reportedMetadataList);
		reportServices.saveReport(getCurrentUser(), report);

		view.navigate().to().listSchemaTypes();
	}

	private List<ReportedMetadata> buildReportedMetadataList(List<MetadataVO> metadataVOs) {
		List<ReportedMetadata> returnList = new ArrayList<>();
		for(int i = 0; i < metadataVOs.size(); i++){
			MetadataVO metadataVO = metadataVOs.get(i);
			returnList.add(new ReportedMetadata(metadataVO.getCode(), i));
		}
		return returnList;
	}

	public void cancelButtonClicked() {
		view.navigate().to().listSchemaTypes();
	}

	private MetadataSchemasManager getMetadataSchemasManager() {
		return modelLayerFactory.getMetadataSchemasManager();
	}

	public String getSchemaTypeCode() {
		return parameters.get("schemaTypeCode");
	}

	public List<ReportVO> getReports() {
		List<ReportVO> returnList = new ArrayList<>();
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<Report> reports = reportServices.getReports(getSchemaTypeCode());
		ReportToVOBuilder builder = new ReportToVOBuilder();
		for(Report report: reports){
			returnList.add(builder.build(report));
		}
		return returnList;
	}

	public List<MetadataVO> getReportMetadatas() {
		List<MetadataVO> returnMetadataVOs = new ArrayList<>();
		if(StringUtils.isBlank(getSelectedReport())){
			return returnMetadataVOs;
		}
		ReportServices reportServices = new ReportServices(modelLayerFactory, view.getCollection());
		Report report = reportServices.getReport(getSchemaTypeCode(), getSelectedReport());
		if(report == null){
			return returnMetadataVOs;
		}
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		for(ReportedMetadata reportedMetadata : report.getReportedMetadata()){
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(reportedMetadata.getMetadataCode());
			returnMetadataVOs.add(builder.build(metadata, view.getSessionContext()));

		}
		return returnMetadataVOs;
	}

	public String getSchemaName(String metadataCode) {
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		int index = metadataCode.lastIndexOf("_");
		return metadataSchemasManager.getSchemaTypes(collection).getSchema(metadataCode.substring(0, index))
				.getLabel(language);
	}

	public boolean isAddMode() {
		return isAddMode;
	}

	public void setAddMode(boolean addMode) {
		isAddMode = addMode;
	}
}
