package com.constellio.app.modules.rm.reports.model.decommissioning;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.constellio.data.io.streamFactories.StreamFactory;

public class DecommissioningListReportModel {

	private StreamFactory<InputStream> headerLogo;
	//	private List<Folder> folders = new ArrayList<>();
	private List<DecommissioningListReportModel_Folder> folders = new ArrayList<>();
	private String decommissioningListTitle = "";
	private String decommissioningListType = "";
	private String decommissioningListAdministrativeUnitCodeAndTitle = "";

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public DecommissioningListReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public String getTitle() {
//		String title = "DecommissioningListReport.Title";
//		return $(title);
		return getDecommissioningListType();
	}

	public List<DecommissioningListReportModel_Folder> getFolders() {
		return folders;
	}

	public void setFolders(List<DecommissioningListReportModel_Folder> folders) {
		this.folders = folders;
	}

	public String getDecommissioningListTitle() {
		return decommissioningListTitle;
	}

	public void setDecommissioningListTitle(String decommissioningListTitle) {
		this.decommissioningListTitle = decommissioningListTitle;
	}

	public String getDecommissioningListType() {
		return decommissioningListType;
	}

	public void setDecommissioningListType(String decommissioningListType) {
		this.decommissioningListType = decommissioningListType;
	}

	public String getDecommissioningListAdministrativeUnitCodeAndTitle() {
		return decommissioningListAdministrativeUnitCodeAndTitle;
	}

	public void setDecommissioningListAdministrativeUnitCodeAndTitle(String decommissioningListAdministrativeUnitCodeAndTitle) {
		this.decommissioningListAdministrativeUnitCodeAndTitle = decommissioningListAdministrativeUnitCodeAndTitle;
	}

	public static class DecommissioningListReportModel_Folder {

		String id;

		String title;

		String retentionRule;

		String category;

		String containerRecord;

		public DecommissioningListReportModel_Folder(String id, String title, String retentionRule, String category, String containerRecord) {
			this.id = id;
			this.title = title;
			this.retentionRule = retentionRule;
			this.category = category;
			this.containerRecord = containerRecord;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getRetentionRule() {
			return retentionRule;
		}

		public void setRetentionRule(String retentionRule) {
			this.retentionRule = retentionRule;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getContainerRecord(){ return containerRecord; }

        public void setContainerRecord(String containerRecord){ this.containerRecord = containerRecord; }
	}
}
