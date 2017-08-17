package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.ead;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EADArchdesc {

	// DID
	
	private String didUnitid;
	
	private String didUnittitle;
	
	private Map<String, String> didUnitDates = new LinkedHashMap<String, String>(); 
	 
	private List<String> didLangmaterials = new ArrayList<String>();
	
	private List<String> didAbstracts = new ArrayList<String>();
	
	private String didOriginationCorpname;
	
	private List<String> didNotePs = new ArrayList<String>();
	
	
	private List<String> fileplanPs = new ArrayList<String>();
	
	private List<String> altformavailPs = new ArrayList<String>();
	
	private List<List<String>> relatedmaterialLists = new ArrayList<List<String>>();
	
	private List<String> controlAccessSubjects = new ArrayList<String>();
	
	private String accessRestrictLegalStatus;

	public String getDidUnitid() {
		return didUnitid;
	}

	public void setDidUnitid(String didUnitid) {
		this.didUnitid = didUnitid;
	}

	public String getDidUnittitle() {
		return didUnittitle;
	}

	public void setDidUnittitle(String didUnittitle) {
		this.didUnittitle = didUnittitle;
	}

	public Map<String, String> getDidUnitDates() {
		return didUnitDates;
	}

	public void setDidUnitDates(Map<String, String> didUnitDates) {
		this.didUnitDates = didUnitDates;
	}

	public List<String> getDidLangmaterials() {
		return didLangmaterials;
	}

	public void setDidLangmaterials(List<String> didLangmaterials) {
		this.didLangmaterials = didLangmaterials;
	}

	public List<String> getDidAbstracts() {
		return didAbstracts;
	}

	public void setDidAbstract(List<String> didAbstracts) {
		this.didAbstracts = didAbstracts;
	}

	public String getDidOriginationCorpname() {
		return didOriginationCorpname;
	}

	public void setDidOriginationCorpname(String didOriginationCorpname) {
		this.didOriginationCorpname = didOriginationCorpname;
	}

	public List<String> getDidNotePs() {
		return didNotePs;
	}

	public void setDidNotePs(List<String> didNotePs) {
		this.didNotePs = didNotePs;
	}

	public List<String> getFileplanPs() {
		return fileplanPs;
	}

	public void setFileplanPs(List<String> fileplanPs) {
		this.fileplanPs = fileplanPs;
	}

	public List<String> getAltformavailPs() {
		return altformavailPs;
	}

	public void setAltformavailPs(List<String> altformavailPs) {
		this.altformavailPs = altformavailPs;
	}

	public List<List<String>> getRelatedmaterialLists() {
		return relatedmaterialLists;
	}

	public void setRelatedmaterialLists(List<List<String>> relatedmaterialLists) {
		this.relatedmaterialLists = relatedmaterialLists;
	}

	public List<String> getControlAccessSubjects() {
		return controlAccessSubjects;
	}

	public void setControlAccessSubjects(List<String> controlAccessSubjects) {
		this.controlAccessSubjects = controlAccessSubjects;
	}

	public String getAccessRestrictLegalStatus() {
		return accessRestrictLegalStatus;
	}

	public void setAccessRestrictLegalStatus(String accessRestrictLegalStatus) {
		this.accessRestrictLegalStatus = accessRestrictLegalStatus;
	}
	
}
