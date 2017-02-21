package com.constellio.app.modules.rm.reports.model.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.data.io.streamFactories.StreamFactory;

/**
 * Created by Charles Blanchette on 2017-02-20.
 */
public class ArborescenceEmplacementReportModel {

    private boolean detailed = false;

    private boolean isByAdministrativeUnit = false;

    private StreamFactory<InputStream> headerLogo;

    private List<ArborescenceEmplacementReportModel_Category> rootCategories = new ArrayList<>();

    List<ArborescenceEmplacementReportModel_Category> categories = new ArrayList<ArborescenceEmplacementReportModel_Category>();

    Map<AdministrativeUnit, List<ArborescenceEmplacementReportModel_Category>> adminUnitRulesMap = new HashMap();

    public StreamFactory<InputStream> getHeaderLogo() {
        return headerLogo;
    }

    public ArborescenceEmplacementReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
        this.headerLogo = headerLogo;
        return this;
    }

    public List<ArborescenceEmplacementReportModel_Category> getRootCategories() {
        return rootCategories;
    }

    public ArborescenceEmplacementReportModel setRootCategories(List<ArborescenceEmplacementReportModel_Category> rootCategories) {
        this.rootCategories = rootCategories;
        return this;
    }

    public void setCategoriesByAdministrativeUnit(
            Map<AdministrativeUnit, List<ArborescenceEmplacementReportModel_Category>> rulesByAdministrativeUnit) {
        this.adminUnitRulesMap = rulesByAdministrativeUnit;
    }

    public Map<AdministrativeUnit, List<ArborescenceEmplacementReportModel_Category>> getCategoriesByAdministrativeUnitMap() {
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
        return $("ArborescenceEmplacementReport.Title");
    }

    public static class ArborescenceEmplacementReportModel_Category {

        private String code;

        private String label;

        private String description;

        private List<ArborescenceEmplacementReportModel_Category> categories = new ArrayList<>();

        private List<String> keywords = new ArrayList<>();

        private List<String> rententionRules = new ArrayList<>();

        public String getCode() {
            return code;
        }

        public ArborescenceEmplacementReportModel_Category setCode(String code) {
            this.code = code;
            return this;
        }

        public String getLabel() {
            return label;
        }

        public ArborescenceEmplacementReportModel_Category setLabel(String label) {
            this.label = label;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public ArborescenceEmplacementReportModel_Category setDescription(String description) {
            this.description = description;
            return this;
        }

        public List<ArborescenceEmplacementReportModel_Category> getCategories() {
            return categories;
        }

        public ArborescenceEmplacementReportModel_Category setCategories(List<ArborescenceEmplacementReportModel_Category> categories) {
            this.categories = categories;
            return this;
        }

        public ArborescenceEmplacementReportModel_Category setKeywords(List<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public ArborescenceEmplacementReportModel_Category setRetentionRules(List<String> rententionRules) {
            this.rententionRules = rententionRules;
            return this;
        }

        public List<String> getRetentionRules() {
            return rententionRules;
        }
    }

}
