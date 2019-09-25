package com.constellio.app.modules.rm.reports.model.decommissioning;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListXLSDetailedReportModel {
	private List<Object> headerTitles;
	private List<Object> headerInfos;

	private List<Object> commentTitles;
	private List<List<Object>> comments;

	private List<Object> validationTitles;
	private List<List<Object>> validations;

	private List<Object> folderTitles;
	private List<List<Object>> folders;

	private List<Object> exclusionTitles;
	private List<List<Object>> exclusions;

	public DecommissioningListXLSDetailedReportModel() {
		headerTitles = new ArrayList<>();
		headerInfos = new ArrayList<>();
		commentTitles = new ArrayList<>();
		comments = new ArrayList<>();
		validationTitles = new ArrayList<>();
		validations = new ArrayList<>();
		folderTitles = new ArrayList<>();
		folders = new ArrayList<>();
		exclusionTitles = new ArrayList<>();
		exclusions = new ArrayList<>();
	}

	public String getHeaderSheetName() {
		return $("DecommissioningListDetailedReport.idSheetName");
	}
	public String getCommentSheetName() {
		return $("DecommissioningListDetailedReport.commentSheetName");
	}
	public String getValidationSheetName() {
		return $("DecommissioningListDetailedReport.validationSheetName");
	}
	public String getFolderSheetName() {
		return $("DecommissioningListDetailedReport.includeSheetName");
	}
	public String getExclusionSheetName() { return $("DecommissioningListDetailedReport.excludeSheetName"); }

	public void addHeaderTitle(Object title) { headerTitles.add(title); }
	public List<Object> getHeaderTitles() { return headerTitles; }
	public void setHeader(List<Object> infos) { headerInfos = infos; }
	public List<Object> getHeaderInfos() { return headerInfos; }

	public void addCommentTitle(Object title) { commentTitles.add(title); }
	public List<Object> getCommentTitles() { return commentTitles; }
	public void addComment(List<Object> line) { comments.add(line); }
	public List<List<Object>> getComments() { return comments; }

	public void addValidationTitle(Object title) { validationTitles.add(title); }
	public List<Object> getValidationTitles() { return validationTitles; }
	public void addValidation(List<Object> line) { validations.add(line); }
	public List<List<Object>> getValidations() { return validations; }

	public void addFolderTitle(Object title) { folderTitles.add(title); }
	public List<Object> getFolderTitles() { return folderTitles; }
	public void addFolder(List<Object> line) { folders.add(line); }
	public List<List<Object>> getFolders() { return folders; }

	public void addExclusionTitle(Object title) { exclusionTitles.add(title); }
	public List<Object> getExclusionTitles() { return exclusionTitles; }
	public void addExclusion(List<Object> line) { exclusions.add(line); }
	public List<List<Object>> getExclusions() { return exclusions; }
}
