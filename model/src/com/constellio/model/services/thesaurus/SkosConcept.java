/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 * <p>
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.constellio.model.services.thesaurus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class SkosConcept implements Serializable {

	public static final String PREF_LABEL = "prefLabel";

	private String rdfAbout;

	private String skosNotes;

	private ThesaurusService thesaurusService;

	private Set<SkosConcept> broader = new HashSet<SkosConcept>();

	private Set<SkosConcept> narrower = new HashSet<SkosConcept>();

	private Set<SkosConcept> related = new HashSet<SkosConcept>();

	private Set<ThesaurusLabel> labels = new HashSet<ThesaurusLabel>();

	private Set<SkosConceptAltLabel> altLabels = new HashSet<SkosConceptAltLabel>();

	public String getRdfAbout() {
		return rdfAbout;
	}


	public void setRdfAbout(String rdfAbout) {
		this.rdfAbout = rdfAbout;
	}

	public String getSkosNotes() {
		return skosNotes;
	}

	public void setSkosNotes(String skosNotes) {
		this.skosNotes = skosNotes;
	}

	public ThesaurusService getThesaurusService() {
		return thesaurusService;
	}

	public void setThesaurusService(ThesaurusService thesaurusService) {
		this.thesaurusService = thesaurusService;
	}

	public Set<SkosConcept> getBroader() {
		return broader;
	}

	public void setBroader(Set<SkosConcept> broader) {
		this.broader = broader;
	}

	public void addBroader(SkosConcept skosConcept) {
		this.broader.add(skosConcept);
	}

	public Set<SkosConcept> getNarrower() {
		return narrower;
	}

	public void setNarrower(Set<SkosConcept> narrower) {
		this.narrower = narrower;
	}

	public void addNarrower(SkosConcept skosConcept) {
		this.narrower.add(skosConcept);
	}

	public Set<SkosConcept> getRelated() {
		return related;
	}

	public void setRelated(Set<SkosConcept> related) {
		this.related = related;
	}

	public void addRelated(SkosConcept skosConcept) {
		this.related.add(skosConcept);
	}

	public Set<ThesaurusLabel> getLabels() {
		return this.labels;
	}

	protected void setLabels(Set<ThesaurusLabel> labels) {
		this.labels = labels;
	}

	public Set<ThesaurusLabel> getPrefLabels() {
		return getLabels();
	}

	public String getPrefLabel(Locale locale) {
		return getLabel(PREF_LABEL, locale, false);
	}

	Map<String, String> prefLabelWithoutParenthesesCache = new HashMap<>();

	public String getPrefLabelWithoutParentheses(Locale locale) {

		String prefLabelWithoutParentheses = prefLabelWithoutParenthesesCache.get(locale.getLanguage());

		if (prefLabelWithoutParentheses == null) {
			prefLabelWithoutParentheses = getLabel(PREF_LABEL, locale, true);
			prefLabelWithoutParenthesesCache.put(locale.getLanguage(), prefLabelWithoutParentheses);
		}
		return prefLabelWithoutParentheses;
	}

	Map<String, String> uppercasedPrefLabelWithoutParenthesesCache = new HashMap<>();

	public String getUppercasedPrefLabelWithoutParentheses(Locale locale) {

		String prefLabelWithoutParentheses = uppercasedPrefLabelWithoutParenthesesCache.get(locale.getLanguage());

		if (prefLabelWithoutParentheses == null) {
			prefLabelWithoutParentheses = getPrefLabelWithoutParentheses(locale);
			if (prefLabelWithoutParentheses != null) {
				prefLabelWithoutParentheses = prefLabelWithoutParentheses.toUpperCase();
			}
			uppercasedPrefLabelWithoutParenthesesCache.put(locale.getLanguage(), prefLabelWithoutParentheses);
		}
		return prefLabelWithoutParentheses;
	}

	private String getLabel(String prefLabel, Locale locale, boolean removeParentheses) {
		String labelStr;
		ThesaurusLabel matchingLabel = null;
		for (ThesaurusLabel label : getLabels()) {
			if (label.getKey().equals(prefLabel)) {
				matchingLabel = label;
				break;
			}
		}
		if (matchingLabel != null) {
			labelStr = matchingLabel.getValue(new Locale(locale.getLanguage()));
		} else {
			labelStr = null;
		}
		if (labelStr != null && removeParentheses) {
			labelStr = labelStr.replaceAll("[(][^)]*[)]", "").trim();
		}
		return labelStr;
	}

	public void setPrefLabel(String value, Locale locale) {
		setLabel(PREF_LABEL, value, locale);
	}

	private void setLabel(String prefLabel, String value, Locale locale) {
		ThesaurusLabel matchingLabel = null;
		for (ThesaurusLabel label : getLabels()) {
			if (label.getKey().equals(prefLabel)) {
				matchingLabel = label;
				break;
			}
		}
		if (matchingLabel == null) {
			matchingLabel = new ThesaurusLabel();
			matchingLabel.setKey(prefLabel);
			this.getLabels().add(matchingLabel);
		}
		matchingLabel.setValue(value, new Locale(locale.getLanguage()));
	}

	public Set<SkosConceptAltLabel> getAltLabels() {
		return altLabels;
	}

	public void setAltLabels(Set<SkosConceptAltLabel> altLabels) {
		this.altLabels = altLabels;
	}

	public Set<String> getAltLabels(Locale locale) {
		SkosConceptAltLabel match = null;
		for (SkosConceptAltLabel altLabel : altLabels) {
			if (altLabel.getLocale().equals(locale)) {
				match = altLabel;
				break;
			}
		}
		if (match == null) {
			match = new SkosConceptAltLabel();
			match.setLocale(locale);
			match.setSkosConcept(this);
			altLabels.add(match);
		}
		return match.getValues();
	}

	public Set<String> getAltLabelsParsedForSearch(Locale locale) {
		SkosConceptAltLabel match = null;
		for (SkosConceptAltLabel altLabel : altLabels) {
			if (altLabel.getLocale().equals(locale)) {
				match = altLabel;
				break;
			}
		}
		if (match == null) {
			match = new SkosConceptAltLabel();
			match.setLocale(locale);
			match.setSkosConcept(this);
			altLabels.add(match);
		}
		return match.getValuesParsedForSearch();
	}

	public Set<String> getParsedForSearchAltLabels(Locale locale) {
		SkosConceptAltLabel match = null;
		for (SkosConceptAltLabel altLabel : altLabels) {
			if (altLabel.getLocale().equals(locale)) {
				match = altLabel;
				break;
			}
		}
		if (match == null) {
			match = new SkosConceptAltLabel();
			match.setLocale(locale);
			match.setSkosConcept(this);
			altLabels.add(match);
		}
		return match.getValuesParsedForSearch();
	}

	public void addAltLabel(Locale locale, String value) {
		SkosConceptAltLabel match = null;
		for (SkosConceptAltLabel altLabel : altLabels) {
			if (altLabel.getLocale().equals(locale)) {
				match = altLabel;
				break;
			}
		}
		if (match == null) {
			match = new SkosConceptAltLabel();
			match.setLocale(locale);
			match.setSkosConcept(this);
			altLabels.add(match);
		}
		match.addValue(value);
	}

	// @Override
	// public int hashCode() {
	// final int prime = 31;
	// int result = 1;
	// result = prime * result + ((rdfAbout == null) ? 0 : rdfAbout.hashCode());
	// return result;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (super.equals(obj))
	// return true;
	// if (getClass() != obj.getClass())
	// return false;
	// SkosConcept other = (SkosConcept) obj;
	// if (rdfAbout == null) {
	// if (other.rdfAbout != null)
	// return false;
	// } else if (!rdfAbout.equals(other.rdfAbout))
	// return false;
	// return true;
	// }

}
