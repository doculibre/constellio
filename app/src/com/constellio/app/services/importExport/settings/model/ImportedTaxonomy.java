package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportedTaxonomy {

    private String code;
    private Map<String, String> titles = new HashMap<>();
    private List<String> classifiedTypes = new ArrayList<>();
    private Boolean visibleOnHomePage = true;
    private List<String> userIds = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();


    public ImportedTaxonomy setCode(String code){
        this.code = code;
        return this;
    }

    public ImportedTaxonomy setTitles(Map<String, String> titles) {
        this.titles = titles;
        return this;
    }

    public Map<String, String> getTitles() {
        return titles;
    }

    public ImportedTaxonomy setClassifiedTypes(List classifiedTypes){
        this.classifiedTypes = classifiedTypes;
        return this;
    }

    public ImportedTaxonomy setVisibleOnHomePage(Boolean visibleOnHomePage) {
        this.visibleOnHomePage = visibleOnHomePage;
        return this;
    }

    public Boolean getVisibleOnHomePage() {
        return visibleOnHomePage;
    }

    public ImportedTaxonomy setUserIds(List<String> users) {
        this.userIds = users;
        return this;
    }

    public List<String> getUserIds(){
        return userIds;
    }

    public String getCode() {
        return code;
    }

    public ImportedTaxonomy setGroupIds(List<String> groups) {
        this.groupIds = groups;
        return this;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public List<String> getClassifiedTypes() {
        return classifiedTypes;
    }
}
