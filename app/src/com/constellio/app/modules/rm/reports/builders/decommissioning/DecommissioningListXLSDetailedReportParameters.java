package com.constellio.app.modules.rm.reports.builders.decommissioning;

import com.constellio.model.entities.records.wrappers.User;

public class DecommissioningListXLSDetailedReportParameters {
	private String decommissioningListId;
	private String schemaType;
	private String collection;
	private String includedFolderReportTitle;
	private String excludedFolderReportTitle;
	private String undefinedFolderReportTitle;
	private String username;

	public DecommissioningListXLSDetailedReportParameters(String decommissioningListId, String schemaType,
														  String collection, User user, String reportTitle) {
		this(decommissioningListId, schemaType, collection, user, reportTitle, reportTitle, reportTitle);
	}

	public DecommissioningListXLSDetailedReportParameters(String decommissioningListId, String schemaType,
														  String collection, User user ,
														  String includedFolderReportTitle,
														  String excludedFolderReportTitle,
														  String undefinedFolderReportTitle) {
		this.decommissioningListId = decommissioningListId;
		this.schemaType = schemaType;
		this.collection = collection;
		this.includedFolderReportTitle = includedFolderReportTitle;
		this.excludedFolderReportTitle = excludedFolderReportTitle;
		this.undefinedFolderReportTitle = undefinedFolderReportTitle;

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

	public String getIncludedFolderReportTitle() {
		return includedFolderReportTitle;
	}

	public String getExcludedFolderReportTitle() {
		return excludedFolderReportTitle;
	}

	public String getUndefinedFolderReportTitle() {
		return undefinedFolderReportTitle;
	}

	public String getUsername() {
		return username;
	}
}
