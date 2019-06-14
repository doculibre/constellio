package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListExcelReportPresenter extends BasePresenter<ListExcelReportView> {

	private MetadataSchema reportSchema;
	private MetadataSchemaToVOBuilder schemaVOBuilder;
	private Map<String, RecordVODataProvider> recordVODataProviderMap;

	public ListExcelReportPresenter(ListExcelReportView view) {
		super(view);
		reportSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(view.getCollection()).getSchema(Report.DEFAULT_SCHEMA);
		recordVODataProviderMap = new HashMap<>();
		initTransientObjects();
	}

	private void initTransientObjects() {
		schemaVOBuilder = new MetadataSchemaToVOBuilder();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_EXCEL_REPORT).globally();
	}

	public Map<String, String> initPossibleTab() {
		return this.initPossibleTab(view.getSessionContext().getCurrentLocale());
	}

	public Map<String, String> initPossibleTab(Locale locale) {
		Map<String, String> map = new HashMap<>();

		//get All metadata schema
		List<MetadataSchemaType> allMetadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes();
		for (MetadataSchemaType schemaType : allMetadataSchemaTypes) {
			if (isMetadataSchemaTypesSearchable(schemaType)) {
				map.put(schemaType.getCode(), schemaType.getLabel(Language.withLocale(locale)));
			}
		}

		map = sortByValue(map);
		return map;
	}

	public RecordVODataProvider getDataProviderForSchemaType(final String schemaType) {
		if (!this.recordVODataProviderMap.containsKey(schemaType)) {
			final MetadataSchemaVO reportVo = schemaVOBuilder.build(
					reportSchema,
					RecordVO.VIEW_MODE.TABLE,
					view.getSessionContext());
			this.recordVODataProviderMap.put(schemaType, new RecordVODataProvider(reportVo, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
				@Override
				public LogicalSearchQuery getQuery() {
					Metadata schemaMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
					LogicalSearchQuery query = new LogicalSearchQuery();
					query.setCondition(from(reportSchema)
							.where(schemaMetadata).isEqualTo(schemaType));

					return query;
				}
			});
		}
		return this.recordVODataProviderMap.get(schemaType);
	}

	protected void editButtonClicked(String item, String schema) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schema);
		paramsMap.put("id", item);
		String params = ParamUtils.addParams(NavigatorConfigurationService.REPORT_DISPLAY_FORM, paramsMap);
		view.navigate().to().reportDisplayForm(params);
	}

	protected void removeRecord(String item, String schema) {
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		UserServices userServices = modelLayerFactory.newUserServices();
		reportServices.deleteReport(userServices.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection), reportServices.getReportById(item));
		view.navigate().to().manageExcelReport();
	}

	protected void displayButtonClicked(String item, String schema) {
		view.navigate().to().displayExcelReport(item);
	}

	private boolean isMetadataSchemaTypesSearchable(MetadataSchemaType types) {
		return schemasDisplayManager().getType(collection, types.getCode()).isAdvancedSearch();
	}

	//copy paste from stack overflow. credit: https://stackoverflow.com/a/2581754/5784924
	public static <K extends String, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list =
				new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getKey()).compareTo(o2.getKey());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
