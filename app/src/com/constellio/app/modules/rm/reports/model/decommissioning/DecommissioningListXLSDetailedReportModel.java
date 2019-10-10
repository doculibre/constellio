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

	private List<Object> includedFolderTitles;
	private List<List<Object>> includedFolders;

	private List<Object> excludedFolderTitles;
	private List<List<Object>> excludedFolders;

	private List<Object> undefinedFolderTitles;
	private List<List<Object>> undefinedFolders;

	private boolean useDecommissionningListWithSelectedFolders = false;

	public DecommissioningListXLSDetailedReportModel() {
		headerTitles = new ArrayList<>();
		headerInfos = new ArrayList<>();
		commentTitles = new ArrayList<>();
		comments = new ArrayList<>();
		validationTitles = new ArrayList<>();
		validations = new ArrayList<>();
		includedFolderTitles = new ArrayList<>();
		includedFolders = new ArrayList<>();
		excludedFolderTitles = new ArrayList<>();
		excludedFolders = new ArrayList<>();
		undefinedFolderTitles = new ArrayList<>();
		undefinedFolders = new ArrayList<>();
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
	public String getIncludedFolderSheetName() {
		return $("DecommissioningListDetailedReport.includedFolderSheetName");
	}
	public String getExcludedFolderSheetName() { return $("DecommissioningListDetailedReport.excludedFolderSheetName"); }
	public String getUndefinedFolderSheetName() { return $("DecommissioningListDetailedReport.undefinedFolderSheetName"); }

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

	public void addIncludedFolderTitle(Object title) { includedFolderTitles.add(title); }
	public List<Object> getIncludedFolderTitles() { return includedFolderTitles; }
	public void addIncludedFolder(List<Object> line) { includedFolders.add(line); }
	public List<List<Object>> getIncludedFolders() { return includedFolders; }

	public void addExcludedFolderTitle(Object title) { excludedFolderTitles.add(title); }
	public List<Object> getExcludedFolderTitles() { return excludedFolderTitles; }
	public void addExcludedFolder(List<Object> line) { excludedFolders.add(line); }
	public List<List<Object>> getExcludedFolders() { return excludedFolders; }

	public void addUndefinedFolderTitle(Object title) { undefinedFolderTitles.add(title); }
	public List<Object> getUndefinedFolderTitles() { return undefinedFolderTitles; }
	public void addUndefinedFolder(List<Object> line) { undefinedFolders.add(line); }
	public List<List<Object>> getUndefinedFolders() { return undefinedFolders; }

	public void setUseDecommissionningListWithSelectedFolders(boolean value) { useDecommissionningListWithSelectedFolders = value; }
	public boolean getUseDecommissionningListWithSelectedFolders() { return useDecommissionningListWithSelectedFolders; }
}
