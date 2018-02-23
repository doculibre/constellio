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
import com.constellio.model.services.thesaurus.util.SkosUtil;
import com.drew.lang.StringUtil;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.Locale;

@SuppressWarnings("serial")
public class ThesaurusService implements Serializable {

	private static final int EXACT_PREF_RESULTS_NUMBER_FOR_NARROWING = 1;
	private static final int SUFFICIENT_RESULTS_NUMBER = 2;
	public static final String DESAMBIUGATIONS = "disambiguations";
	public static final String SUGGESTIONS = "suggestions";

	private String rdfAbout;
	private String dcTitle;
	private String dcDescription;
	private String dcCreator;
	private Date dcDate;
	private Locale dcLanguage;

	private List<String> deniedTerms = new ArrayList<>();
	private Set<SkosConcept> topConcepts = new HashSet<SkosConcept>();
	private Map<String, SkosConcept> allConcepts = new ConcurrentHashMap<String, SkosConcept>();

	public ThesaurusService(){

	}

	public ThesaurusService(String dcTitle) {
		this.dcTitle = dcTitle;
	}

	public void setDeniedTerms(List<String> deniedTerms) {
		this.deniedTerms = deniedTerms;
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

	public Set<SkosConcept> getPrefLabelsThatEqualsOrSpecify(String input, Locale locale){

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

	public Set<SkosConcept> getPrefLabelsThatContains(String input, Locale locale){

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

	public Set<SkosConcept> getAltLabelsThatContains(String input, Locale locale){

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

	public Set<SkosConcept> getAltLabelsThatEquals(String input, Locale locale){

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if(StringUtils.isNotBlank(input)) {
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<String> labelValues = skosConcept.getAltLabels(locale);

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

	public Set<SkosConcept> getAllLabelsThatContains(String input, Locale locale){

		Set<SkosConcept> searchPrefLabel = getPrefLabelsThatContains(input, locale);
		Set<SkosConcept> searchAltLabel = getAltLabelsThatContains(input, locale);

		Set<SkosConcept> allLabels = new HashSet<>(searchPrefLabel);
		allLabels.addAll(searchAltLabel);

		return allLabels;
	}

	public String matchThesaurusLabel(String text, Locale locale) {
		SkosUtil.normaliseTextForMatching(text);

		for(String key : allConcepts.keySet()) {

			SkosConcept skosConcept = allConcepts.get(key);
			String prefLabel = skosConcept.getPrefLabelWithoutParentheses(locale);
			if(prefLabel != null && !Strings.isNullOrEmpty(prefLabel)) {

				int count = StringUtils.countMatches(allConcepts.get(key).getPrefLabel(locale)
						.toUpperCase(), text);
				if(count  > 0) {

				}
			}
		}

		return "";
	}



	public ResponseSkosConcept getSkosConcepts(String input, List<String> languageCodesAvailableList) {

			ResponseSkosConcept responseSkosConcept = new ResponseSkosConcept();
			Set<String> languageCodesAvailable = new HashSet<>(languageCodesAvailableList);

			for (final String languageCodeAvailable : languageCodesAvailable) {
				getSkosConceptForGivenLang(input, languageCodeAvailable, responseSkosConcept);
			}

		return responseSkosConcept;
	}

	public ResponseSkosConcept getSkosConcepts(String input, Language languageCodeAvailable) {

		ResponseSkosConcept responseSkosConcept = new ResponseSkosConcept();
		getSkosConceptForGivenLang(input, languageCodeAvailable.getLocale().getLanguage(), responseSkosConcept);

		return responseSkosConcept;
	}

	public void getSkosConceptForGivenLang(String input, String languageCodeAvailable, ResponseSkosConcept responseSkosConcept){
		final Locale currentLanguage = new Locale(languageCodeAvailable);
		Set<String> localeDisambiguationsNL = new LinkedHashSet<>();
		Set<String> localeSuggestionsNL = new LinkedHashSet<>();
		Set<SkosConcept> suggestedConcepts = new LinkedHashSet<>();

		// pref search (exact or specify)
		Set<SkosConcept> prefLabelsThatEqualsOrSpecifyTerm = getPrefLabelsThatEqualsOrSpecify(input, currentLanguage);
		List<SkosConcept> disambiguationConcepts = new ArrayList<SkosConcept>();
		// enough results
		if (prefLabelsThatEqualsOrSpecifyTerm.size() >= SUFFICIENT_RESULTS_NUMBER) {
			disambiguationConcepts.addAll(prefLabelsThatEqualsOrSpecifyTerm);
		}
		// not enough results
		if (localeSuggestionsNL.isEmpty()) {
			Set<SkosConcept> altLabelsThatEqualsTerm = getAltLabelsThatEquals(input, currentLanguage);
			if (!altLabelsThatEqualsTerm.isEmpty()) {
				SkosConcept firstAltLabelConcept = altLabelsThatEqualsTerm.iterator().next();
				suggestedConcepts.add(firstAltLabelConcept);
			}
		}

		// pref search (contains)
		Set<SkosConcept> prefLabelMatches = getPrefLabelsThatContains(input, currentLanguage);
		for (SkosConcept prefLabelMatch : prefLabelMatches) {
			String prefLabel = prefLabelMatch.getPrefLabel(currentLanguage);
			if (StringUtils.isNotBlank(prefLabel)) {
				prefLabel = StringUtils.capitalize(prefLabel.toLowerCase());
				suggestedConcepts.add(prefLabelMatch);
			}
		}

		// adds linked concepts

		List<String> allLinks = new ArrayList<String>();
		List<SkosConcept> linkConcepts = new ArrayList<SkosConcept>();

		// based on prefs if only one
		if (prefLabelsThatEqualsOrSpecifyTerm.size() == EXACT_PREF_RESULTS_NUMBER_FOR_NARROWING) {
			SkosConcept skosConcept = prefLabelsThatEqualsOrSpecifyTerm.iterator().next();
			Set<SkosConcept> narrowerConcepts = skosConcept.getNarrower();
			for (SkosConcept narrowerConcept : narrowerConcepts) {
				String prefLabel = narrowerConcept.getPrefLabel(currentLanguage);
				if (StringUtils.isNotBlank(prefLabel)) {
					linkConcepts.add(narrowerConcept);
				}
			}

			Set<SkosConcept> relatedConcepts = skosConcept.getRelated();
			for (SkosConcept relatedConcept : relatedConcepts) {
				String prefLabel = relatedConcept.getPrefLabel(currentLanguage);
				if (StringUtils.isNotBlank(prefLabel)) {
					linkConcepts.add(relatedConcept);
				}
			}

			Set<SkosConcept> broaderConcepts = skosConcept.getBroader();
			for (SkosConcept broaderConcept : broaderConcepts) {
				String prefLabel = broaderConcept.getPrefLabel(currentLanguage);
				if (StringUtils.isNotBlank(prefLabel)) {
					linkConcepts.add(broaderConcept);
				}
			}
		} else {
			for (SkosConcept suggestedConcept : suggestedConcepts) {
				if (!linkConcepts.contains(suggestedConcept)) {
					linkConcepts.add(suggestedConcept);
				}
			}
		}

		// sorts linked concepts

		Collections.sort(linkConcepts, new Comparator<SkosConcept>() {
			@Override
			public int compare(SkosConcept o1, SkosConcept o2) {
				int density1 = getDensity(o1);
				int density2 = getDensity(o2);
				int result = -(new Integer(density1).compareTo(new Integer(density2)));
				return result;
			}

			private int getDensity(SkosConcept skosConcept) {
				int broaderCount = 0;
				int narrowerCount = 0;
				int relatedCount = 0;

				Set<SkosConcept> narrowerConcepts = skosConcept.getNarrower();
				for (SkosConcept narrowerConcept : narrowerConcepts) {
					String prefLabel = narrowerConcept.getPrefLabel(currentLanguage);
					if (StringUtils.isNotBlank(prefLabel)) {
						broaderCount++;
					}
				}

				Set<SkosConcept> relatedConcepts = skosConcept.getRelated();
				for (SkosConcept relatedConcept : relatedConcepts) {
					String prefLabel = relatedConcept.getPrefLabel(currentLanguage);
					if (StringUtils.isNotBlank(prefLabel)) {
						narrowerCount++;
					}
				}

				Set<SkosConcept> broaderConcepts = skosConcept.getBroader();
				for (SkosConcept broaderConcept : broaderConcepts) {
					String prefLabel = broaderConcept.getPrefLabel(currentLanguage);
					if (StringUtils.isNotBlank(prefLabel)) {
						relatedCount++;
					}
				}
				return broaderCount + narrowerCount + relatedCount;
			}
		});

		for (SkosConcept linkConcept : linkConcepts) {
			String prefLabel = linkConcept.getPrefLabel(currentLanguage);
			prefLabel = StringUtils.capitalize(prefLabel.toLowerCase());
			allLinks.add(prefLabel);
		}

		int max = 21;
		for (int i = 0; i < disambiguationConcepts.size(); i++) {
			SkosConcept disambiguationConcept = disambiguationConcepts.get(i);
			String prefLabel = disambiguationConcept.getPrefLabel(currentLanguage);
			prefLabel = StringUtils.capitalize(prefLabel.toLowerCase());
			String disambiguation = prefLabel;
			localeDisambiguationsNL.add(disambiguation);
			allLinks.remove(disambiguation);
		}
		for (int i = 0; i < max && i < allLinks.size(); i++) {
			String suggestion = allLinks.get(i);
			localeSuggestionsNL.add(suggestion);
		}

		// custom user filter
		localeDisambiguationsNL.removeAll(deniedTerms);
		localeSuggestionsNL.removeAll(deniedTerms);

		responseSkosConcept.getDisambiguations().put(currentLanguage, new ArrayList(localeDisambiguationsNL));
		responseSkosConcept.getSuggestions().put(currentLanguage, new ArrayList(localeSuggestionsNL));
	}

//	public List<String> suggestSimpleSearch(String input, Locale locale) {
//
//		List<String> suggestions = new ArrayList<String>();
//
//
//		if (StringUtils.isNotEmpty(input) && input.length() >= 3) { // && !input.contains("*:*") && !collection.isOpenSearch()
//			try {
//				StatsServices statsServices = ConstellioSpringUtils.getStatsServices();
//
//				// get related pref labels that contains input
//
//				int maxResults = 5; // TODO maxResults = MAX_RESULTS_NUMBER_INIT; (+cte)
////				String analyzedInput = AnalyzerUtils.analyze(input);
//				List<String> analyzedSuggestions = new ArrayList<String>();
//				Set<SkosConcept> prefLabelSuggestions = getPrefLabelsThatContains(input, locale);
//
//					for (SkosConcept thesaurusSuggestion : prefLabelSuggestions) {
//						String prefLabel = thesaurusSuggestion.getPrefLabel(locale).toLowerCase();
////						String analyzedSuggestion = AnalyzerUtils.analyze(prefLabel);
//
//						if (isValidAutocompleteSuggestion(input, analyzedInput, prefLabel, analyzedSuggestion)
//								&& !isAlreadyInSuggestions(analyzedSuggestions, analyzedSuggestion)) {
//							maxResults--;
//							suggestions.add(prefLabel);
//							analyzedSuggestions.add(analyzedSuggestion);
//							if (maxResults == 0) {
//								break;
//							}
//						}
//					}
//
//					// if not enough results, get related alt labels
//					if (maxResults > 0) {
//						Set<SkosConcept> altLabelSuggestions = getAltLabelsThatContains(input, locale);
//						for (SkosConcept thesaurusSuggestion : altLabelSuggestions) {
//							for (String altLabel : thesaurusSuggestion.getAltLabels(locale)) {
//								altLabel = altLabel.toLowerCase();
//								String analyzedSuggestion = AnalyzerUtils.analyze(altLabel);
//								if (isValidAutocompleteSuggestion(input, analyzedInput, altLabel, analyzedSuggestion)
//										&& !analyzedSuggestions.contains(analyzedSuggestion)) {
//									maxResults--;
//									suggestions.add(altLabel);
//									analyzedSuggestions.add(analyzedSuggestion);
//									if (maxResults == 0) {
//										break;
//									}
//								}
//							}
//						}
//					}
//				// if not enough results, get related most popular queries
//				if (maxResults > 0) {
//					List<String> mostPopularQueriesSuggestions = statsServices.getMostPopularQueriesAutocomplete(input, maxResults, null);
//					for (String mostPopularQueriesSuggestion : mostPopularQueriesSuggestions) {
//						String analyzedSuggestion = AnalyzerUtils.analyze(mostPopularQueriesSuggestion);
//						if (isValidAutocompleteSuggestion(input, analyzedInput, mostPopularQueriesSuggestion, analyzedSuggestion) && !analyzedSuggestions.contains(analyzedSuggestion)) {
//							maxResults--;
//							suggestions.add(mostPopularQueriesSuggestion);
//							analyzedSuggestions.add(analyzedSuggestion);
//							if (maxResults == 0) {
//								break;
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//			}
//		}
//		return suggestions;
//	}

	private boolean isValidAutocompleteSuggestion(String input, String analyzedInput, String suggestion, String analyzedSuggestion) {
		boolean valid = false;

		if (!deniedTerms.contains(suggestion)) {
			if ((StringUtils.isNotBlank(analyzedInput) && analyzedSuggestion.startsWith(analyzedInput))
					|| (StringUtils.isNotBlank(input) && suggestion.startsWith(input))) {
				valid = true;
			}
		}
		return valid;
	}

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