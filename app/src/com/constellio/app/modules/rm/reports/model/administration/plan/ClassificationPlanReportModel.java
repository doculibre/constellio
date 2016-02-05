package com.constellio.app.modules.rm.reports.model.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.data.io.streamFactories.StreamFactory;

public class ClassificationPlanReportModel {

	private boolean detailed = false;

	private boolean isByAdministrativeUnit = false;

	private StreamFactory<InputStream> headerLogo;

	private List<ClassificationPlanReportModel_Category> rootCategories = new ArrayList<>();

	List<ClassificationPlanReportModel_Category> categories = new ArrayList<ClassificationPlanReportModel_Category>();

	Map<AdministrativeUnit, List<ClassificationPlanReportModel_Category>> adminUnitRulesMap = new HashMap();

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public ClassificationPlanReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public List<ClassificationPlanReportModel_Category> getRootCategories() {
		return rootCategories;
	}

	public ClassificationPlanReportModel setRootCategories(List<ClassificationPlanReportModel_Category> rootCategories) {
		this.rootCategories = rootCategories;
		return this;
	}

	public void setCategoriesByAdministrativeUnit(
			Map<AdministrativeUnit, List<ClassificationPlanReportModel_Category>> rulesByAdministrativeUnit) {
		this.adminUnitRulesMap = rulesByAdministrativeUnit;
	}

	public Map<AdministrativeUnit, List<ClassificationPlanReportModel_Category>> getCategoriesByAdministrativeUnitMap() {
		return adminUnitRulesMap;
	}

	public boolean isDetailed() {
		return detailed;
	}

	public void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}

	public boolean isByAdministrativeUnit() {
		return isByAdministrativeUnit;
	}

	public void setByAdministrativeUnit(boolean isByAdministrativeUnit) {
		this.isByAdministrativeUnit = isByAdministrativeUnit;
	}

	public String getTitle() {
		String title;
		if (isByAdministrativeUnit()) {
			title = "ClassificationPlanByAdministrativeUnitReport.Title";
		} else if (isDetailed()) {
			title = "ClassificationPlanDetailedReport.Title";
		} else {
			title = "ClassificationPlanReport.Title";
		}
		return $(title);
	}

	public static class ClassificationPlanReportModel_Category {

		private String code;

		private String label;

		private String description;

		private List<ClassificationPlanReportModel_Category> categories = new ArrayList<>();

		private List<String> keywords = new ArrayList<>();

		private List<String> rententionRules = new ArrayList<>();

		public String getCode() {
			return code;
		}

		public ClassificationPlanReportModel_Category setCode(String code) {
			this.code = code;
			return this;
		}

		public String getLabel() {
			return label;
		}

		public ClassificationPlanReportModel_Category setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public ClassificationPlanReportModel_Category setDescription(String description) {
			this.description = description;
			return this;
		}

		public List<ClassificationPlanReportModel_Category> getCategories() {
			return categories;
		}

		public ClassificationPlanReportModel_Category setCategories(List<ClassificationPlanReportModel_Category> categories) {
			this.categories = categories;
			return this;
		}

		public ClassificationPlanReportModel_Category setKeywords(List<String> keywords) {
			this.keywords = keywords;
			return this;
		}

		public List<String> getKeywords() {
			return keywords;
		}

		public ClassificationPlanReportModel_Category setRetentionRules(List<String> rententionRules) {
			this.rententionRules = rententionRules;
			return this;
		}

		public List<String> getRetentionRules() {
			return rententionRules;
		}
	}

}
