/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.reports.model.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SearchResultReportPresenter {
	static int LIMIT = 10000;
	static int BATCH_SIZE = 100;
	private final List<String> selectedRecords;
	private final String schemaTypeCode;
	private final String collection;
	private final String username;
	private final String reportTitle;
	private final LogicalSearchQuery searchQuery;
	private final ModelLayerFactory modelLayerFactory;

	public SearchResultReportPresenter(ModelLayerFactory modelLayerFactory, List<String> selectedRecords, String schemaType,
			String collection, String username,
			String reportTitle, LogicalSearchQuery searchQuery) {
		this.selectedRecords = selectedRecords;
		this.schemaTypeCode = schemaType;
		this.collection = collection;
		this.username = username;
		this.reportTitle = reportTitle;
		this.searchQuery = searchQuery;
		this.modelLayerFactory = modelLayerFactory;
	}

	public SearchResultReportModel buildModel(ModelLayerFactory modelLayerFactory) {
		SearchResultReportModel resultReportModel = new SearchResultReportModel();

		List<Metadata> orderedEnabledReportedMetadataList = getEnabledReportedMetadataList(modelLayerFactory);

		for (Metadata metadata : orderedEnabledReportedMetadataList) {
			resultReportModel.addTitle(metadata.getLabel());
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
				.setReturnedMetadatas(new ReturnedMetadatasFilter(orderedEnabledReportedMetadataList));
		return searchServices.query(newSearchQuery).getRecords();
	}

	private List<Record> getAllSchemaTypeRecords(int index, ModelLayerFactory modelLayerFactory,
			List<Metadata> orderedEnabledReportedMetadataList) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ReturnedMetadatasFilter returnMetadata = new ReturnedMetadatasFilter(orderedEnabledReportedMetadataList);
		LogicalSearchQuery newSearchQuery = searchQuery
				.setReturnedMetadatas(new ReturnedMetadatasFilter(orderedEnabledReportedMetadataList)).setStartRow(index)
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
				Record record = modelLayerFactory.newRecordServices().getDocumentById(referenceId);
				String code = record.get(Schemas.CODE);
				String title = record.get(Schemas.TITLE);
				if (code == null) {
					return title;
				} else {
					return code + "-" + title;
				}
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