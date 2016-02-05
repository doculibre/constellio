package com.constellio.model.services.reports;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServicesRuntimeException;

public class ReportServices {
	private final MetadataSchema reportSchema;
	private final String collection;
	SearchServices searchServices;
	RecordServices recordServices;
	private ModelLayerFactory modelLayerFactory;

	public ReportServices(ModelLayerFactory modelLayerFactory, String collection) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		MetadataSchemaTypes schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		reportSchema = schemaTypes.getSchema(Report.DEFAULT_SCHEMA);
	}

	public void saveReport(User user, Report report) {
		String reportUsername = report.getUsername();
		if (user.getUsername().equals(reportUsername)) {
			addUpdate(report);
		} else {
			if (report.getUsername() == null || hasReportManagementPermission(user)) {
				addUpdate(report);
			} else {
				throw new CouldNotModifySomeoneElseReportRuntimeException(report.getUsername() + "!=" + user.getUsername());
			}
		}
	}

	private boolean hasReportManagementPermission(User user) {
		//TODO
		return true;
	}

	public List<Report> getUserReports(User user, String schemaTypeCode) {
		Metadata schemaTypeCodeMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
		Metadata userMetaData = reportSchema.getMetadata(Report.USERNAME);
		LogicalSearchQuery query = new LogicalSearchQuery();
		String username = null;
		if (user != null) {
			username = user.getUsername();
		}
		query.setCondition(
				from(reportSchema).where(schemaTypeCodeMetadata).isEqualTo(schemaTypeCode)
						.andWhere(userMetaData).isEqualTo(username));
		List<Record> results = searchServices.search(query);
		return wrapReports(results);
	}

	private List<Report> wrapReports(List<Record> records) {
		List<Report> reports = new ArrayList<>();
		for (Record record : records) {
			reports.add(new Report(record, modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)));
		}
		return reports;
	}

	public Report getReport(String schemaTypeCode, String reportTitle) {
		return getUserReport(null, schemaTypeCode, reportTitle);
	}

	public Report getUserReport(String username, String schemaTypeCode, String reportTitle) {
		Metadata schemaTypeCodeMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
		Metadata userMetadata = reportSchema.getMetadata(Report.USERNAME);
		Metadata titleMetaData = Schemas.TITLE;
		LogicalSearchQuery query = new LogicalSearchQuery();
		if (username != null) {
			query.setCondition(from(reportSchema)
					.where(schemaTypeCodeMetadata).isEqualTo(schemaTypeCode)
					.andWhere(userMetadata).isEqualTo(username)
					.andWhere(titleMetaData).isEqualTo(reportTitle));
		} else {
			query.setCondition(from(reportSchema)
					.where(schemaTypeCodeMetadata).isEqualTo(schemaTypeCode)
					.andWhere(userMetadata).isNull()
					.andWhere(titleMetaData).isEqualTo(reportTitle));
		}

		long count = searchServices.getResultsCount(query);
		if (count == 0) {
			if (username == null) {
				return null;
			} else {
				query.setCondition(from(reportSchema)
						.where(schemaTypeCodeMetadata).isEqualTo(schemaTypeCode)
						.andWhere(userMetadata).isNull()
						.andWhere(titleMetaData).isEqualTo(reportTitle));
				if (searchServices.getResultsCount(query) == 0) {
					return null;
				}
			}
		}
		List<Record> results = searchServices.search(query);
		return new Report(results.get(0), modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public List<Report> getReports(String schemaTypeCode) {
		Metadata schemaTypeCodeMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
		Metadata userMetadata = reportSchema.getMetadata(Report.USERNAME);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(reportSchema)
				.where(schemaTypeCodeMetadata).isEqualTo(schemaTypeCode)
				.andWhere(userMetadata).isNull());
		long count = searchServices.getResultsCount(query);
		if (count == 0) {
			return new ArrayList<>();
		} else {
			List<Record> results = searchServices.search(query);
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			return warpReports(results, types);
		}
	}

	private List<Report> warpReports(List<Record> results, MetadataSchemaTypes types) {
		List<Report> returnList = new ArrayList<>();
		for (Record record : results) {
			returnList.add(new Report(record, types));
		}
		return returnList;
	}

	public void addUpdate(Report report) {
		Transaction transaction = new Transaction();
		transaction.add(report);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new UserServicesRuntimeException.UserServicesRuntimeException_CannotExcuteTransaction(e);
		}
	}

	public List<String> getUserReportTitles(User user, String schemaTypeCode) {
		List<String> returnList = new ArrayList<>();
		Metadata schemaTypeCodeMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
		Metadata userMetadata = reportSchema.getMetadata(Report.USERNAME);
		Metadata titleMetaData = reportSchema.getMetadata(Report.TITLE);
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition withUserNameOrWithNull;
		if (user != null) {
			withUserNameOrWithNull = where(userMetadata).isEqualTo(user.getUsername()).orWhere(userMetadata).isNull();
		} else {
			withUserNameOrWithNull = where(userMetadata).isNull();
		}
		query.setCondition(
				from(reportSchema).where(withUserNameOrWithNull).andWhere(schemaTypeCodeMetadata).isEqualTo(schemaTypeCode));

		long count = searchServices.getResultsCount(query);
		if (count == 0) {
			return returnList;
		}
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitle());
		List<Record> results = searchServices.search(query);
		if (results == null || results.isEmpty()) {
			return returnList;
		}
		for (Record record : results) {
			String reportTitle = record.get(titleMetaData);
			returnList.add(reportTitle);
		}
		return returnList;
	}

	private class CouldNotModifySomeoneElseReportRuntimeException extends RuntimeException {
		public CouldNotModifySomeoneElseReportRuntimeException(String message) {
			super(message);
		}
	}
}
