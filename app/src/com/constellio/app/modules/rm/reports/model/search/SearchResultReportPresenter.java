package com.constellio.app.modules.rm.reports.model.search;

import com.constellio.app.modules.rm.reports.model.excel.BaseExcelReportPresenter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public class SearchResultReportPresenter extends BaseExcelReportPresenter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchResultReportPresenter.class);
	static int LIMIT = 10000;
	static int BATCH_SIZE = 100;
	private final List<String> selectedRecords;
	private final String schemaTypeCode;
	private final String collection;
	private final String username;
	private final String reportTitle;
	private final LogicalSearchQuery searchQuery;
	private final User userInCollection;

	public SearchResultReportPresenter(AppLayerFactory appLayerFactory, List<String> selectedRecords, String schemaType,
									   String collection, String username,
									   String reportTitle, LogicalSearchQuery searchQuery, Locale locale) {
		super(appLayerFactory, locale, collection);
		this.selectedRecords = selectedRecords;
		this.schemaTypeCode = schemaType;
		this.collection = collection;
		this.username = username;
		this.reportTitle = reportTitle;
		this.searchQuery = searchQuery;
		userInCollection = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
	}

	public SearchResultReportModel buildModel(ModelLayerFactory modelLayerFactory) {
		SearchResultReportModel resultReportModel = new SearchResultReportModel();

		List<Metadata> orderedEnabledReportedMetadataList = getEnabledReportedMetadataList(modelLayerFactory);

		for (Metadata metadata : orderedEnabledReportedMetadataList) {
			resultReportModel.addTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
		}
		Iterator<Record> recordsIterator;
		if (searchQuery != null) {
			recordsIterator = modelLayerFactory.newSearchServices().recordsIteratorKeepingOrder(searchQuery, 200);
		}
		//TODO DO Not use searchQuery
		else if (selectedRecords == null || selectedRecords.isEmpty()) {
			ArrayList<Record> recordsList = new ArrayList<>();
			int index = 0;
			boolean allRecordsAdded = false;
			while (!allRecordsAdded && recordsList.size() < LIMIT) {
				List<Record> currentRecords = getAllSchemaTypeRecords(index, modelLayerFactory,
						orderedEnabledReportedMetadataList);
				recordsList.addAll(currentRecords);
				index += currentRecords.size();
				allRecordsAdded = (currentRecords.size() == 0) ? true : false;
			}
			recordsIterator = recordsList.iterator();
		} else {
			recordsIterator = getAllSelectedRecordsFromIndex(modelLayerFactory, orderedEnabledReportedMetadataList).iterator();
		}
		while (recordsIterator.hasNext()) {
			resultReportModel.addLine(getRecordLine(recordsIterator.next(), orderedEnabledReportedMetadataList));
		}
		return resultReportModel;
	}

	private List<Record> getAllSelectedRecordsFromIndex(ModelLayerFactory modelLayerFactory,
														List<Metadata> orderedEnabledReportedMetadataList) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		LogicalSearchQuery newSearchQuery = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(asList(schemaTypeCode), collection).where(Schemas.IDENTIFIER)
						.isIn(selectedRecords)).filteredWithUser(userInCollection)
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList));
		//		LogicalSearchCondition newCondition = searchQuery.getCondition().andWhere(Schemas.IDENTIFIER).isIn(selectedRecords);
		//		LogicalSearchQuery newSearchQuery = searchQuery.setCondition(newCondition)
		//				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList));
		return searchServices.query(newSearchQuery).getRecords();
	}

	private List<Record> getAllSchemaTypeRecords(int index, ModelLayerFactory modelLayerFactory,
												 List<Metadata> orderedEnabledReportedMetadataList) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ReturnedMetadatasFilter returnMetadata = ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList);
		LogicalSearchQuery newSearchQuery = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(asList(schemaTypeCode), collection).returnAll()).filteredWithUser(
						userInCollection)
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList))
				.setStartRow(index)
				.setNumberOfRows(BATCH_SIZE);
		//		LogicalSearchQuery newSearchQuery = searchQuery
		//				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(orderedEnabledReportedMetadataList))
		//				.setStartRow(index)
		//				.setNumberOfRows(BATCH_SIZE);
		return searchServices.query(new LogicalSearchQuery(newSearchQuery).setReturnedMetadatas(returnMetadata)).getRecords();
	}

	protected List<Object> getRecordLine(Record record, List<Metadata> orderedEnabledReportedMetadataList) {
		List<Object> returnList = new ArrayList<>();
		for (Metadata metadata : orderedEnabledReportedMetadataList) {
			Object metadataValue = record.get(metadata, locale);

			if (metadataValue == null ||
				metadataValue instanceof Collection && ((Collection) metadataValue).isEmpty() ||
				!userInCollection.hasAccessToMetadata(metadata, record)) {
				returnList.add(null);
			} else {
				Metadata metadataOfRecordSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
						.getSchemaOf(record).getMetadata(metadata.getLocalCode());
				returnList.add(getConvertedValue(metadataOfRecordSchema, metadataValue));
			}
		}
		return returnList;
	}

	protected Object getConvertedValue(Metadata metadata, Object metadataValue) {
		if (metadata.isMultivalue()) {
			List<Object> items = (List) metadataValue;
			List<Object> convertedValue = new ArrayList<>();
			for (Object item : items) {
				convertedValue.add(getConvertedScalarValue(metadata, item));
			}
			if (convertedValue.isEmpty()) {
				return "";
			}
			return convertedValue;
		} else {
			return getConvertedScalarValue(metadata, metadataValue);
		}

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
			throw new UnsupportedReportException();
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
		MetadataList allMetadata = schemaType.getAllMetadatas().onlyAccessibleGloballyBy(userInCollection);

		for (ReportedMetadata reportedMetadata : reportedMetadataList) {
			boolean found = false;
			for (Metadata metadata : allMetadata) {
				if (metadata.getLocalCode().equals(reportedMetadata.getMetadataLocaleCode())) {
					//					if (metadata.isEnabled()) {
					returnList.add(metadata);
					//					}
					found = true;
					break;
				}
			}
			if (!found) {
				LOGGER.warn("Could not find reported metadata: " + reportedMetadata.getMetadataLocaleCode());
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