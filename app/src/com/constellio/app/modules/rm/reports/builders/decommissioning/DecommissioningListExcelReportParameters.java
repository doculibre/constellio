package com.constellio.app.modules.rm.reports.builders.decommissioning;

import com.constellio.model.entities.records.wrappers.User;

public class DecommissioningListExcelReportParameters {
	private String decommissioningListId;
	private String schemaType;
	private String collection;
	private String reportTitle;
	private String username;

	public DecommissioningListExcelReportParameters(String decommissioningListId, String schemaType,
													String collection, String reportTitle, User user) {
		this.decommissioningListId = decommissioningListId;
		this.schemaType = schemaType;
		this.collection = collection;
		this.reportTitle = reportTitle;

		if (user != null) {
			this.username = user.getUsername();
		} else {
			username = null;
		}
	}

	public String getDecommissioningListId() {
		return decommissioningListId;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public String getCollection() {
		return collection;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public String getUsername() {
		return username;
	}
}
