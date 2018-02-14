/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.constellio.model.services.thesaurus;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class ThesaurusService implements Serializable {

	private String rdfAbout;
	private String dcTitle;
	private String dcDescription;
	private String dcCreator;
	private Date dcDate;
	private Locale dcLanguage;
	private String collection;
	private String sourceFileName;

	private Set<SkosConcept> topConcepts = new HashSet<SkosConcept>();
	private Map<String, SkosConcept> allConcepts = new ConcurrentHashMap<String, SkosConcept>();

	public ThesaurusService(){

	}

	public ThesaurusService(String dcTitle) {
		this.dcTitle = dcTitle;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public void setRecordCollection(String recordCollection) {
		this.collection = recordCollection;
	}

	public Map<String, SkosConcept> getAllConcepts() {
		return allConcepts;
	}

	public void setAllConcepts(Map<String, SkosConcept> allConcepts) {
		this.allConcepts = allConcepts;
	}

	public String getRdfAbout() {
		return rdfAbout;
	}

	public void setRdfAbout(String rdfAbout) {
		this.rdfAbout = rdfAbout;
	}

	public String getDcTitle() {
		return dcTitle;
	}

	public void setDcTitle(String dcTitle) {
		this.dcTitle = dcTitle;
	}

	public String getDcDescription() {
		return dcDescription;
	}

	public void setDcDescription(String dcDescription) {
		this.dcDescription = dcDescription;
	}

	public String getDcCreator() {
		return dcCreator;
	}

	public void setDcCreator(String dcCreator) {
		this.dcCreator = dcCreator;
	}

	public Date getDcDate() {
		return dcDate;
	}

	public void setDcDate(Date dcDate) {
		this.dcDate = dcDate;
	}

	public Locale getDcLanguage() {
		return dcLanguage;
	}

	public void setDcLanguage(Locale dcLanguage) {
		this.dcLanguage = dcLanguage;
	}

	public Set<SkosConcept> getTopConcepts() {
		return topConcepts;
	}

	public void setTopConcepts(Set<SkosConcept> topConcepts) {
		this.topConcepts = topConcepts;
	}

	public void addTopConcept(SkosConcept topConcept) {
		topConcept.getBroader().clear();
		this.topConcepts.add(topConcept);
		topConcept.setThesaurus(this);
	}

	public boolean equalsRdfAbout(ThesaurusService obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (super.equals(obj))
			return true;
		if (getClass() != obj.getClass())
			return false;
		ThesaurusService other = (ThesaurusService) obj;
		if (rdfAbout == null) {
			if (other.rdfAbout != null)
				return false;
		} else if (!rdfAbout.equals(other.rdfAbout))
			return false;
		return true;
	}

	public List<SkosConcept> searchPrefLabel(String input){

		List<SkosConcept> skosConcepts = new ArrayList<>();

		for(Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()){
			SkosConcept skosConcept = skosConceptEntry.getValue();
			Set<ThesaurusLabel> thesaurusLabels = skosConcept.getPrefLabels();

			for(ThesaurusLabel thesaurusLabel : thesaurusLabels){
				// one thesaurus label per lang code possible (and multiple lang codes possible)
				for(String prefValue : thesaurusLabel.getValues().values()){ // TODO check if only iterate through values OK
					if(StringUtils.containsIgnoreCase(input, prefValue)){
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public List<SkosConcept> searchAltLabel(String input){

		List<SkosConcept> skosConcepts = new ArrayList<>();

		for(Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()){
			SkosConcept skosConcept = skosConceptEntry.getValue();
			Set<SkosConceptAltLabel> thesaurusLabels = skosConcept.getAltLabels();

			for(SkosConceptAltLabel thesaurusLabel : thesaurusLabels){
				// one thesaurus label per lang code possible (and multiple lang codes possible)
				for(String prefValue : thesaurusLabel.getValues()){ // TODO check if only iterate through values OK
					if(StringUtils.containsIgnoreCase(input, prefValue)){
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

//	public List<SkosConcept> searchRelatedLabel(String rdfAbout){
//
//		List<SkosConcept> skosConcepts = new ArrayList<>();
//
//		for(Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()){
//			SkosConcept skosConcept = skosConceptEntry.getValue();
//			Set<SkosConcept> thesaurusLabels = skosConcept.getRelated();
//
//			for(SkosConcept thesaurusLabel : thesaurusLabels)
//
//				for(ThesaurusLabel : thesaurusLabel.getPrefLabels()) {
//				}
//
//				// one thesaurus label per lang code possible (and multiple lang codes possible)
//				for(String prefValue : thesaurusLabel.getValues()){ // TODO check if only iterate through values OK
//					if(StringUtils.containsIgnoreCase(input, prefValue)){
//						skosConcepts.add(skosConcept);
//					}
//				}
//			}
//		}
//
//		return skosConcepts;
//	}

	public List<SkosConcept> searchAllLabels(String input){

		List<SkosConcept> searchPrefLabel = searchPrefLabel(input);
		List<SkosConcept> searchAltLabel = searchAltLabel(input);

		List<SkosConcept> allLabels = new ArrayList<>(searchPrefLabel);
		allLabels.addAll(searchAltLabel);

		// TODO add suggestions

		return allLabels;
	}

}
