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

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.Language;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.util.NamedList;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class ThesaurusService implements Serializable {

	private static final int SUFFICIENT_RESULTS_NUMBER = 2;
	private static final Language[] languagesAvailable = {Language.French, Language.English};
	public static final String DESAMBIUGATIONS = "disambiguations";
	public static final String SUGGESTIONS = "suggestions";

	private String rdfAbout;
	private String dcTitle;
	private String dcDescription;
	private String dcCreator;
	private Date dcDate;
	private Locale dcLanguage;
	private String collection;
	private Locale locale;
	private String sourceFileName;

	private List<String> deniedWords = new ArrayList<>();
	private Set<SkosConcept> topConcepts = new HashSet<SkosConcept>();
	private Map<String, SkosConcept> allConcepts = new ConcurrentHashMap<String, SkosConcept>();

	public ThesaurusService(){
		locale = new Locale("fr"); // TODO define according to current sys. locale!
	}

	public ThesaurusService(String dcTitle) {
		this.dcTitle = dcTitle;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setDeniedWords(List<String> deniedWords) {
		this.deniedWords = deniedWords;
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
		topConcept.setThesaurusService(this);
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

	public Set<SkosConcept> getPrefLabelsThatEqualsOrSpecify(String input){

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if(StringUtils.isNotBlank(input)) {
			String parsedInput = parseForSearch(input);
			Pattern p = Pattern.compile(parsedInput + ".*\\(.*\\)"); // search for "generalSearchedWord (specification)"

			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<ThesaurusLabel> thesaurusLabels = skosConcept.getPrefLabels();

				// for each label of given lang
				for (ThesaurusLabel thesaurusLabel : thesaurusLabels) {

					String parsedLabelValue = parseForSearch(thesaurusLabel.getValue(locale));

					if (parsedInput.equals(parsedLabelValue) || p.matcher(parsedLabelValue).find()) {
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getPrefLabelsThatContains(String input){

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if(StringUtils.isNotBlank(input)) {
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<ThesaurusLabel> thesaurusLabels = skosConcept.getPrefLabels();

				// for each label of given lang
				for (ThesaurusLabel thesaurusLabel : thesaurusLabels) {

					String labelValue = thesaurusLabel.getValue(locale);

					if (containsWithParsing(labelValue, input)) {
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getAltLabelsThatContains(String input){

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if(StringUtils.isNotBlank(input)) {
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<String> labelValues = skosConcept.getAltLabels(locale);

				// for each label of given lang
				for (String labelValue : labelValues) {
					if (containsWithParsing(labelValue, input)) {
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getAltLabelsThatEquals(String input){

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if(StringUtils.isNotBlank(input)) {
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<String> labelValues = skosConcept.getAltLabels(locale); // TODO if not working, try getAltLabels() without parameters

				// for each label of given lang
				for (String labelValue : labelValues) {
					if (equalsWithParsing(input, labelValue)) {
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getAllLabelsThatContains(String input){

		Set<SkosConcept> searchPrefLabel = getPrefLabelsThatContains(input);
		Set<SkosConcept> searchAltLabel = getAltLabelsThatContains(input);

		Set<SkosConcept> allLabels = new HashSet<>(searchPrefLabel);
		allLabels.addAll(searchAltLabel);

		// TODO add suggestions

		return allLabels;
	}

	public NamedList getSkosConcepts(String input) {
		NamedList namedList = new NamedList();
		namedList.add(DESAMBIUGATIONS, "tata");
		namedList.add(DESAMBIUGATIONS, "tttt");
		namedList.add(SUGGESTIONS, "jojo");
		namedList.add(SUGGESTIONS, "jjjj");

		return namedList;
	}
//
//	public NamedList getSkosConcepts(String input) {
//
//			// prepares output structures
//			NamedList skosConceptsNL = new NamedList();
//			NamedList disambiguationsNL = new NamedList();
//			NamedList suggestionsNL = new NamedList();
//			skosConceptsNL.add("disambiguations", disambiguationsNL);
//			skosConceptsNL.add("suggestions", suggestionsNL);
//
//			for (final Language language : languagesAvailable) {
//				NamedList localeDisambiguationsNL = new NamedList();
//				NamedList localeSuggestionsNL = new NamedList();
//
//				Set<String> suggestions = new HashSet<String>();
//				List<SkosConcept> suggestedConcepts = new ArrayList<SkosConcept>();
//
//				// pref search (exact or specify)
//				Set<SkosConcept> prefLabelsThatEqualsOrSpecifyTerm = getPrefLabelsThatEqualsOrSpecify(input);
//				List<SkosConcept> disambiguationConcepts = new ArrayList<SkosConcept>();
//				// enough results
//				if (prefLabelsThatEqualsOrSpecifyTerm.size() >= SUFFICIENT_RESULTS_NUMBER) {
//					disambiguationConcepts.addAll(prefLabelsThatEqualsOrSpecifyTerm);
//				}
//				// not enough results
//				if (localeSuggestionsNL.size() == 0) { // TODO replace by isEmpty when real collection
//					Set<SkosConcept> altLabelsThatEqualsTerm = getAltLabelsThatEquals(input);
//					if (!altLabelsThatEqualsTerm.isEmpty()) {
//						SkosConcept firstAltLabelConcept = altLabelsThatEqualsTerm.iterator().next();
//						suggestedConcepts.add(firstAltLabelConcept);
//					}
//				}
//
//				// pref search (contains)
//				Set<SkosConcept> prefLabelMatches = getPrefLabelsThatContains(input);
//				for (SkosConcept prefLabelMatch : prefLabelMatches) {
//					String prefLabel = prefLabelMatch.getPrefLabel(language.getLocale());
//					if (StringUtils.isNotBlank(prefLabel)) {
//						prefLabel = StringUtils.capitalize(prefLabel.toLowerCase());
//						if (!suggestions.contains(prefLabel)) { // TODO unicity possible... NamedList => Set? + TODO unused suggestions... will never contains any value!!!
//							suggestedConcepts.add(prefLabelMatch);
//						}
//					}
//				}
//
//				// adds linked concepts
//
//				List<String> allLinks = new ArrayList<String>();
//				List<SkosConcept> linkConcepts = new ArrayList<SkosConcept>();
//
//				if (prefLabelsThatEqualsOrSpecifyTerm.size() == 1) {
//					SkosConcept skosConcept = prefLabelsThatEqualsOrSpecifyTerm.iterator().next();
//					Set<SkosConcept> narrowerConcepts = skosConcept.getNarrower();
//					for (SkosConcept narrowerConcept : narrowerConcepts) {
//						String prefLabel = narrowerConcept.getPrefLabel(locale);
//						if (StringUtils.isNotBlank(prefLabel)) {
//							linkConcepts.add(narrowerConcept);
//						}
//					}
//
//					Set<SkosConcept> relatedConcepts = skosConcept.getRelated();
//					for (SkosConcept relatedConcept : relatedConcepts) {
//						String prefLabel = relatedConcept.getPrefLabel(locale);
//						if (StringUtils.isNotBlank(prefLabel)) {
//							linkConcepts.add(relatedConcept);
//						}
//					}
//
//					Set<SkosConcept> broaderConcepts = skosConcept.getBroader();
//					for (SkosConcept broaderConcept : broaderConcepts) {
//						String prefLabel = broaderConcept.getPrefLabel(locale);
//						if (StringUtils.isNotBlank(prefLabel)) {
//							linkConcepts.add(broaderConcept);
//						}
//					}
//				} else {
//					for (SkosConcept suggestedConcept : suggestedConcepts) {
//						if (!linkConcepts.contains(suggestedConcept)) {
//							linkConcepts.add(suggestedConcept);
//						}
//					}
//				}
//
//				// sorts linked concepts
//
//				Collections.sort(linkConcepts, new Comparator<SkosConcept>() {
//					@Override
//					public int compare(SkosConcept o1, SkosConcept o2) {
//						int density1 = getDensity(o1);
//						int density2 = getDensity(o2);
//						int result = -(new Integer(density1).compareTo(new Integer(density2)));
//						return result;
//					}
//
//					private int getDensity(SkosConcept skosConcept) {
//						int broaderCount = 0;
//						int narrowerCount = 0;
//						int relatedCount = 0;
//
//						Set<SkosConcept> narrowerConcepts = skosConcept.getNarrower();
//						for (SkosConcept narrowerConcept : narrowerConcepts) {
//							String prefLabel = narrowerConcept.getPrefLabel(locale);
//							if (StringUtils.isNotBlank(prefLabel)) {
//								broaderCount++;
//							}
//						}
//
//						Set<SkosConcept> relatedConcepts = skosConcept.getRelated();
//						for (SkosConcept relatedConcept : relatedConcepts) {
//							String prefLabel = relatedConcept.getPrefLabel(locale);
//							if (StringUtils.isNotBlank(prefLabel)) {
//								narrowerCount++;
//							}
//						}
//
//						Set<SkosConcept> broaderConcepts = skosConcept.getBroader();
//						for (SkosConcept broaderConcept : broaderConcepts) {
//							String prefLabel = broaderConcept.getPrefLabel(locale);
//							if (StringUtils.isNotBlank(prefLabel)) {
//								relatedCount++;
//							}
//						}
//						return broaderCount + narrowerCount + relatedCount;
//					}
//				});
//
//				for (SkosConcept linkConcept : linkConcepts) {
//					String prefLabel = linkConcept.getPrefLabel(locale);
//					prefLabel = StringUtils.capitalize(prefLabel.toLowerCase()); // TODO why do .toLowerCase right before .capitalize?
//					allLinks.add(prefLabel);
//				}
//
////				String queryAdjusted = StringUtils.capitalize(query.toLowerCase());
////				allLinks.remove(queryAdjusted); // TODO voir ce que fait cette ligne...
//
//				int max = 21;
//				for (int i = 0; i < disambiguationConcepts.size(); i++) {
//					SkosConcept disambiguationConcept = disambiguationConcepts.get(i);
//					String prefLabel = disambiguationConcept.getPrefLabel(locale);
//					prefLabel = StringUtils.capitalize(prefLabel.toLowerCase());
//					String disambiguation = prefLabel;
//					localeDisambiguationsNL.add("label", disambiguation);
//					allLinks.remove(disambiguation);
//				}
//				for (int i = 0; i < max && i < allLinks.size(); i++) {
//					String suggestion = allLinks.get(i);
//					localeSuggestionsNL.add("label", suggestion);
//				}
//				//
//				// Set<String> suggestionsLabels = new TreeSet<String>();
//				// for (SkosConcept skosConcept : getSuggestions(query,
//				// collection, locale)) {
//				// suggestionsLabels.add(skosConcept.getPrefLabel(locale));
//				// }
//				// for (String suggestionsLabel : suggestionsLabels) {
//				// suggestionsNL.add("label", suggestionsLabel);
//				// }
//
//				disambiguationsNL.add(locale.getLanguage(), localeDisambiguationsNL);
//				suggestionsNL.add(locale.getLanguage(), localeSuggestionsNL);
//			}
//		// end
////		response.getValues().add("skosConcepts", skosConceptsNL);
//
//		return skosConceptsNL;
//	}

	// UTILS

	/**
	 * Compares two strings after parsing.
	 * @param s1
	 * @param s2
	 * @return true if parsed strings are equal
	 */
	private boolean equalsWithParsing(String s1, String s2){
		return parseForSearch(s1).equals(parseForSearch(s2));
	}

	/**
	 * Check if string contains another after parsing.
	 * @param container
	 * @param content
	 * @return true if parsed strings are equal
	 */
	private boolean containsWithParsing(String container, String content){
		return parseForSearch(container).contains(parseForSearch(content));
	}

	/**
	 * Remove accents, trim whitespaces and standardize case.
	 * @param input
	 * @return the parsed input
	 */
	private String parseForSearch(String input) {
		return AccentApostropheCleaner.removeAccents(input.trim().toLowerCase());
	}
}