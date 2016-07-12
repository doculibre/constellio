package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportedTaxonomy {

    private String code;
    private Map<String, String> titles = new HashMap<>();
    private List<String> classifiedTypes = new ArrayList<>();
    private boolean visibleOnHomePage = true;
    private List<String> users = new ArrayList<>();
    private List<String> userGroups = new ArrayList<>();


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

    public ImportedTaxonomy setVisibleOnHomePage(boolean visibleOnHomePage) {
        this.visibleOnHomePage = visibleOnHomePage;
        return this;
    }

    public boolean isVisibleOnHomePage(){
        return visibleOnHomePage;
    }

    public ImportedTaxonomy setUsers(List<String> users) {
        this.users = users;
        return this;
    }

    public List<String> getUsers(){
        return users;
    }

    public String getCode() {
        return code;
    }

    public ImportedTaxonomy setUserGroups(List<String> groups) {
        this.userGroups = groups;
        return this;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public List<String> getClassifiedTypes() {
        return classifiedTypes;
    }
}
