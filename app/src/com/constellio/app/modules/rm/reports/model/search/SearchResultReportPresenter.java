package com.constellio.app.modules.rm.reports.model.search;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultReportPresenter {
	static int LIMIT = 10000;
	static int BATCH_SIZE = 100;
	private final List<String> selectedRecords;
	private final String schemaTypeCode;
	private final String collection;
	private final String username;
	private final String reportTitle;
	private final LogicalSearchQuery searchQuery;
	private final AppLayerFactory appLayerFactory;
	private final Locale locale;

	public SearchResultReportPresenter(AppLayerFactory appLayerFactory, List<String> selectedRecords, String schemaType,
									   String collection, String username,
									   String reportTitle, LogicalSearchQuery searchQuery, Locale locale) {
		this.selectedRecords = selectedRecords;
		this.schemaTypeCode = schemaType;
		this.collection = collection;
		this.username = username;
		this.reportTitle = reportTitle;
		this.searchQuery = searchQuery;
		this.appLayerFactory = appLayerFactory;
		this.locale = locale;
	}

	public SearchResultReportModel buildModel(ModelLayerFactory modelLayerFactory) {
		SearchResultReportModel resultReportModel = new SearchResultReportModel();

		List<Metadata> orderedEnabledReportedMetadataList = getEnabledReportedMetadataList(modelLayerFactory);

		for (Metadata metadata : orderedEnabledReportedMetadataList) {
			resultReportModel.addTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
		}
		List<Record> records;
		if (selectedRecords == null || selectedRecords.isEmpty()) {
			records = new ArrayList<>();
			int index = 0;
			boolean allRecordsAdded = false;
			while (!allRecordsAdded && records.size() < LIMIT) {
				List<Record> currentRecords = getAllSchemaTypeRecords(index, modelLayerFactory,
						orderedEnabledReportedMetadataList);
				records.addAll(currentRecords);
				index += currentRecords.size();
				allRecordsAdded = (currentRecords.size() == 0) ? true : false;
			}
		} else {
			records = getAllSelectedRecordsFromIndex(modelLayerFactory, orderedEnabledReportedMetadataList);
		}
		for (Record record : records) {
			resultReportModel.addLine(getRecordLine(record, orderedEnabledReportedMetadataList));
		}
		return resultReportModel;
	}

	private List<Record> getAllSelectedRecordsFromIndex(ModelLayerFactory modelLayerFactory,
			List<Metadata> orderedEnabledReportedMetadataList) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchCondition newCondition = searchQuery.getCondition().andWhere(Schemas.IDENTIFIER).isIn(selectedRecords);
		LogicalSearchQuery newSearchQuery = searchQuery.setCondition(newCondition)
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList));
		return searchServices.query(newSearchQuery).getRecords();
	}

	private List<Record> getAllSchemaTypeRecords(int index, ModelLayerFactory modelLayerFactory,
			List<Metadata> orderedEnabledReportedMetadataList) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ReturnedMetadatasFilter returnMetadata = ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList);
		LogicalSearchQuery newSearchQuery = searchQuery
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList))
				.setStartRow(index)
				.setNumberOfRows(BATCH_SIZE);
		return searchServices.query(new LogicalSearchQuery(newSearchQuery).setReturnedMetadatas(returnMetadata)).getRecords();
	}

	private List<Object> getRecordLine(Record record, List<Metadata> orderedEnabledReportedMetadataList) {
		List<Object> returnList = new ArrayList<>();
		for (Metadata metadata : orderedEnabledReportedMetadataList) {
			Object metadataValue = record.get(metadata);
			if (metadataValue == null) {
				returnList.add(null);
			} else {
				returnList.add(getConvertedValue(metadata, metadataValue));
			}
		}
		return returnList;
	}

	private Object getConvertedValue(Metadata metadata, Object metadataValue) {
		if (metadata.isMultivalue()) {
			List<Object> items = (List) metadataValue;
			List<Object> convertedValue = new ArrayList<>();
			for (Object item : items) {
				convertedValue.add(getConvertedScalarValue(metadata, item));
			}
			return convertedValue;
		} else {
			return getConvertedScalarValue(metadata, metadataValue);
		}

	}

	private Object getConvertedScalarValue(Metadata metadata, Object metadataValue) {

		if (metadata.getType() == MetadataValueType.REFERENCE) {
			String referenceId = (String) metadataValue;
			if (referenceId != null) {
				Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(referenceId);
				String code = record.get(Schemas.CODE);
				String title = record.get(Schemas.TITLE);
				if (code == null || !metadata.isDefaultRequirement()) {
					return title;
				} else {
					return code + "-" + title;
				}
			}
		}
		else if(metadata.getType() == MetadataValueType.BOOLEAN) {
			return metadataValue.equals(true)? $("yes"):$("no");
		}
		else if(metadata.getType() == MetadataValueType.TEXT) {
			SchemasDisplayManager schemasManager = appLayerFactory.getMetadataSchemasDisplayManager();
			MetadataDisplayConfig config = schemasManager.getMetadata(collection, metadata.getCode());
			if(config.getInputType().equals(MetadataInputType.RICHTEXT)) {
				String result = metadataValue.toString().replaceAll("<br>", "\n");
				result = result.toString().replaceAll("<li>", "\n");
				result = result.toString().replaceAll("\\<[^>]*>","");
				return result;
			}
		}

		return metadataValue;
	}

	private List<ReportedMetadata> getReportedMetadataList(ReportServices reportServices) {
		Report report = reportServices.getUserReport(username, schemaTypeCode, reportTitle);
		if (report == null) {
			report = reportServices.getReport(schemaTypeCode, reportTitle);
		}
		if (report == null) {
			String username = null;
			if (this.username != null) {
				username = this.username;
			}
			throw new NoSuchReportRuntimeException(username, schemaTypeCode, reportTitle);
		}
		if (report.getLinesCount() != 1) {
			throw new UnsupportedReport();
		}
		return report.getReportedMetadata();
	}

	private List<Metadata> getEnabledReportedMetadataList(ModelLayerFactory modelLayerFactory) {
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>(getReportedMetadataList(reportServices));
		orderByPosition(reportedMetadataList);
		List<Metadata> returnList = new ArrayList<>();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
		MetadataList allMetadata = schemaType.getAllMetadatas();

		for (ReportedMetadata reportedMetadata : reportedMetadataList) {
			boolean found = false;
			for (Metadata metadata : allMetadata) {
				if (metadata.getLocalCode().equals(reportedMetadata.getMetadataLocaleCode())) {
					if (metadata.isEnabled()) {
						returnList.add(metadata);
					}
					found = true;
					break;
				}
			}
			if (!found) {
				throw new InExistingReportedMetadataRuntimeException(reportedMetadata, schemaTypeCode);
			}
		}

		return returnList;
	}

	private void orderByPosition(List<ReportedMetadata> reportedMetadataList) {
		Collections.sort(reportedMetadataList, new Comparator<ReportedMetadata>() {
			@Override
			public int compare(ReportedMetadata o1, ReportedMetadata o2) {
				return o1.getXPosition() - o2.getXPosition();
			}
		});
	}

}