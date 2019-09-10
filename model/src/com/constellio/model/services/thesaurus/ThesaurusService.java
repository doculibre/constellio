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

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.thesaurus.util.SkosUtil;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.constellio.model.services.thesaurus.util.SkosUtil.getToLowerCase;
import static com.constellio.model.services.thesaurus.util.SkosUtil.parseForSearch;

@SuppressWarnings("serial")
public class ThesaurusService implements Serializable {

	private static final int MIN_INPUT_LENGTH = 3;
	private static final int EXACT_PREF_RESULTS_NUMBER_FOR_NARROWING = 1;
	private static final int SUFFICIENT_RESULTS_NUMBER = 2;
	private static final int MAX_AUTOCOMPLETE_RESULTS = 5;
	public static final String DISAMBIGUATIONS = "disambiguations";
	public static final String SUGGESTIONS = "suggestions";
	public static final String DOMAINE_LABEL = "DOMAINE";

	private String rdfAbout;
	private String dcTitle;
	private String dcDescription;
	private String dcCreator;
	private Date dcDate;
	private Locale dcLanguage;

	private Set<String> deniedTerms;
	private Set<SkosConcept> topConcepts;
	private Map<String, SkosConcept> allConcepts;

	public ThesaurusService() {
		deniedTerms = new HashSet<>();
		topConcepts = new HashSet<>();
		allConcepts = new ConcurrentHashMap<>();
	}

	public void setDeniedTerms(List<String> deniedTerms) {
		this.deniedTerms = getToLowerCase(new HashSet<>(deniedTerms));
	}

	public Set<SkosConcept> getTopConcepts() {
		return topConcepts;
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

	public void setDcLanguage(Locale dcLanguage) {
		this.dcLanguage = dcLanguage;
	}

	public void addTopConcept(SkosConcept topConcept) {
		topConcept.getBroader().clear();
		this.topConcepts.add(topConcept);
		topConcept.setThesaurusService(this);
	}

	public boolean equalsRdfAbout(ThesaurusService obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (super.equals(obj)) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ThesaurusService other = (ThesaurusService) obj;
		if (rdfAbout == null) {
			if (other.rdfAbout != null) {
				return false;
			}
		} else if (!rdfAbout.equals(other.rdfAbout)) {
			return false;
		}
		return true;
	}

	public Set<SkosConcept> getPrefLabelsThatEqualsOrSpecify(String input, Locale locale) {

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if (StringUtils.isNotBlank(input) && !input.startsWith("*")) {
			String parsedInput = parseForSearch(input);
			// replace takes care of PatternSyntaxException on a input with a missing closing or opening parentheses
			parsedInput = StringUtils.replace(parsedInput, "(", "\\(");
			parsedInput = StringUtils.replace(parsedInput, ")", "\\)");
			Pattern p = Pattern.compile(parsedInput + ".*\\(.*\\)"); // search for "generalSearchedWord (specification)"

			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<ThesaurusLabel> thesaurusLabels = skosConcept.getPrefLabels();

				// for each label of given lang
				for (ThesaurusLabel thesaurusLabel : thesaurusLabels) {

					String parsedLabelValue = thesaurusLabel.getParsedForSearchValue(locale);
					if (parsedLabelValue != null && (parsedInput.equals(parsedLabelValue) || p.matcher(parsedLabelValue)
							.find())) {
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getPrefLabelsThatContains(String input, Locale locale) {

		String inputParsedForSearch = SkosUtil.parseForSearch(input);

		Set<SkosConcept> skosConcepts = new HashSet<>();

		if (StringUtils.isNotBlank(input)) {
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<ThesaurusLabel> thesaurusLabels = skosConcept.getPrefLabels();

				// for each label of given lang
				for (ThesaurusLabel thesaurusLabel : thesaurusLabels) {

					String parsedLabelValue = thesaurusLabel.getParsedForSearchValue(locale);
					if (parsedLabelValue != null && parsedLabelValue.contains(inputParsedForSearch)) {
						skosConcepts.add(skosConcept);
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getAltLabelsThatContains(String input, Locale locale) {
		String inputParsedForSearch = SkosUtil.parseForSearch(input);
		Set<SkosConcept> skosConcepts = new HashSet<>();

		if (StringUtils.isNotBlank(input)) {
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<String> labelValues = skosConcept.getAltLabelsParsedForSearch(locale);

				if (labelValues != null) {
					// for each label of given lang
					for (String labelValue : labelValues) {
						if (labelValue.contains(inputParsedForSearch)) {
							skosConcepts.add(skosConcept);
						}
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getAltLabelsThatEquals(String input, Locale locale) {


		Set<SkosConcept> skosConcepts = new HashSet<>();

		if (StringUtils.isNotBlank(input)) {
			String inputParsedForSearch = SkosUtil.parseForSearch(input);
			// for each concept
			for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
				SkosConcept skosConcept = skosConceptEntry.getValue();
				Set<String> labelValues = skosConcept.getAltLabelsParsedForSearch(locale);

				if (labelValues != null) {
					// for each label of given lang
					for (String labelValue : labelValues) {
						if (LangUtils.isEqual(inputParsedForSearch, labelValue)) {
							skosConcepts.add(skosConcept);
						}
					}
				}
			}
		}

		return skosConcepts;
	}

	public Set<SkosConcept> getAllLabelsThatContains(String input, Locale locale) {

		Set<SkosConcept> searchPrefLabel = getPrefLabelsThatContains(input, locale);
		Set<SkosConcept> searchAltLabel = getAltLabelsThatContains(input, locale);

		Set<SkosConcept> allLabels = new HashSet<>(searchPrefLabel);
		allLabels.addAll(searchAltLabel);

		return allLabels;
	}

	public List<String> matchThesaurusLabels(String text, Locale locale) {
		String normalisedTextForMatching = SkosUtil.normaliseTextForMatching(text);
		List<String> idsMatching = new ArrayList<>();
		boolean isDenied = false;

		for (SkosConcept skosConcept : allConcepts.values()) {
			isDenied = false;
			String prefLabel = skosConcept.getUppercasedPrefLabelWithoutParentheses(locale);

			for (String term : deniedTerms) {
				if (term.equalsIgnoreCase(prefLabel)) {
					isDenied = true;
					break;
				}
			}
			if (isDenied) {
				continue;
			}

			if (prefLabel != null && !Strings.isNullOrEmpty(prefLabel)) {
				String skosConceptId = SkosUtil.getSkosConceptId(skosConcept.getRdfAbout());

				int count = StringUtils.countMatches(normalisedTextForMatching, " " + prefLabel + " ");
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						idsMatching.add(skosConceptId);
					}
				}
			}
		}

		return idsMatching;
	}

	public List<SkosConcept> findRootDomain(String skosId) {
		SkosConcept skosConcept = getSkosConcept(skosId);
		List<SkosConcept> domainSkosConcept = new ArrayList<>();
		findDomainOfSkosConcept(skosConcept, DOMAINE_LABEL, domainSkosConcept);
		return domainSkosConcept;
	}

	public void findDomainOfSkosConcept(SkosConcept skosConcept, String frenchLabelParentRequirement,
										List<SkosConcept> domainSkosConcepts) {

		if (domainSkosConcepts == null) {
			throw new IllegalArgumentException("domainSkosConcepts cannot be null when calling this" +
											   " mehtod it is an out parameter.");
		}

		if (skosConcept.getBroader() == null || skosConcept.getBroader().size() == 0) {
			return;
		}

		for (SkosConcept skosConcept1 : skosConcept.getBroader()) {
			if (skosConcept1.getBroader() == null || skosConcept1.getBroader().size() == 0) {
				return;
			} else {
				boolean isFound = false;
				for (SkosConcept skosConcept2 : skosConcept1.getBroader()) {
					boolean meetParentLabelCriteria = false;
					if (frenchLabelParentRequirement != null && StringUtils.isNotBlank(frenchLabelParentRequirement)) {
						for (ThesaurusLabel thesaurusLabel : skosConcept2.getLabels()) {
							String frLabel = thesaurusLabel.getValue(new Locale("fr"));
							if (frenchLabelParentRequirement.equalsIgnoreCase(frLabel)) {
								meetParentLabelCriteria = true;
							}
						}
					} else {
						meetParentLabelCriteria = true;
					}
					if (skosConcept2.getBroader() == null || skosConcept2.getBroader().size() == 0 &&
															 meetParentLabelCriteria) {
						boolean found = false;
						for (SkosConcept skosConcept3 : domainSkosConcepts) {
							if (skosConcept1.getRdfAbout().equals(skosConcept3.getRdfAbout())) {
								found = true;
							}
						}
						if (!found) {
							domainSkosConcepts.add(skosConcept1);
						}
					} else {
						if (!isFound) {
							findDomainOfSkosConcept(skosConcept1, frenchLabelParentRequirement, domainSkosConcepts);
						}
					}
				}
			}
		}
	}

	public SkosConcept getSkosConcept(String id) {

		SkosConcept concept = allConcepts.get(id);
		if (concept != null) {
			return concept;
		}

		for (Map.Entry<String, SkosConcept> skosConceptEntry : allConcepts.entrySet()) {
			SkosConcept skosConcept = skosConceptEntry.getValue();
			String currentId = SkosUtil.getSkosConceptId(skosConcept.getRdfAbout());
			if (currentId.equals(id)) {
				return skosConcept;
			}
		}
		return null;
	}

	/**
	 * Get skos concepts for all languages available.
	 *
	 * @param input                      unparsed search term
	 * @param languageCodesAvailableList
	 * @return skos concept response
	 */
	public ResponseSkosConcept getSkosConcepts(String input, List<String> languageCodesAvailableList) {

		ResponseSkosConcept responseSkosConcept = new ResponseSkosConcept();
		Set<String> languageCodesAvailable = new HashSet<>(languageCodesAvailableList);

		for (final String languageCodeAvailable : languageCodesAvailable) {
			getSkosConceptForGivenLang(input, languageCodeAvailable, responseSkosConcept);
		}

		return responseSkosConcept;
	}

	/**
	 * Get skos concept for a given language.
	 *
	 * @param input                 unparsed search term
	 * @param languageCodeAvailable
	 * @return skos concept response
	 */
	public ResponseSkosConcept getSkosConcepts(String input, Language languageCodeAvailable) {

		ResponseSkosConcept responseSkosConcept = new ResponseSkosConcept();

		if (input != null && input.length() > MIN_INPUT_LENGTH) {
			getSkosConceptForGivenLang(input, languageCodeAvailable
					.getLocale().getLanguage(), responseSkosConcept);
		}

		return responseSkosConcept;
	}

	public void getSkosConceptForGivenLang(String input, String languageCodeAvailable,
										   ResponseSkosConcept responseSkosConcept) {
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

		String queryAdjusted = StringUtils.capitalize(input.toLowerCase());
		allLinks.remove(queryAdjusted);

		int max = 21;
		for (int i = 0; i < disambiguationConcepts.size(); i++) {
			SkosConcept disambiguationConcept = disambiguationConcepts.get(i);
			String prefLabel = disambiguationConcept.getPrefLabel(currentLanguage);
			prefLabel = StringUtils.capitalize(prefLabel.toLowerCase());
			String disambiguation = prefLabel;

			if (isNotExcludedByUser(disambiguation)) {
				localeDisambiguationsNL.add(disambiguation);
				allLinks.remove(disambiguation); // so disambiguations are not in suggestions
			}
		}
		for (int i = 0; i < max && i < allLinks.size(); i++) {
			String suggestion = allLinks.get(i);
			if (isNotExcludedByUser(suggestion)) {
				localeSuggestionsNL.add(suggestion);
			}
		}

		responseSkosConcept.getDisambiguations().put(currentLanguage, new ArrayList(localeDisambiguationsNL));
		responseSkosConcept.getSuggestions().put(currentLanguage, new ArrayList(localeSuggestionsNL));
	}

	private boolean isNotExcludedByUser(String term) {
		return !deniedTerms.contains(term.toLowerCase());
	}

	public List<String> suggestSimpleSearch(String input, Locale locale, int minInputLength, int maxResults,
											boolean useMostPopularQueriesAutocomplete,
											SearchEventServices searchEventServices) {

		// ordered Set to prioritize results found first (since last results are often found as last resort)
		List<String> suggestions = new ArrayList<>();

		if (StringUtils.isNotEmpty(input) && input.length() >= minInputLength) {

			// get related pref labels that contains input

			Set<SkosConcept> prefLabelSuggestions = getPrefLabelsThatContains(input, locale);

			for (SkosConcept suggestion : prefLabelSuggestions) {
				String localeSuggestion = suggestion.getPrefLabel(locale);

				if (isValidAutocompleteSuggestion(input, localeSuggestion)) {
					addToSuggestions(suggestions, localeSuggestion);
				}
			}

			// if not enough results, get related alt labels

			if (suggestions.size() <= maxResults) {
				Set<SkosConcept> altLabelSuggestions = getAltLabelsThatContains(input, locale);

				for (SkosConcept suggestion : altLabelSuggestions) {
					for (String localeSuggestion : suggestion.getAltLabels(locale)) {
						if (isValidAutocompleteSuggestion(input, localeSuggestion)) {
							addToSuggestions(suggestions, localeSuggestion);
						}
					}
				}
			}

			if (useMostPopularQueriesAutocomplete) {
				List<String> autocompleteSuggestions = searchEventServices
						.getMostPopularQueriesAutocomplete(input, MAX_AUTOCOMPLETE_RESULTS,
								deniedTerms.toArray(new String[deniedTerms.size()]));

				for (String suggestion : autocompleteSuggestions) {
					if (isValidAutocompleteSuggestion(input, suggestion)) {
						addToSuggestions(suggestions, suggestion);
					}
				}
			}

		}

		return suggestions;
	}

	private void addToSuggestions(List<String> suggestions, String suggestion) {
		if (suggestions.size() < MAX_AUTOCOMPLETE_RESULTS) {
			suggestions.add(StringUtils.capitalize(suggestion.toLowerCase()));
		}
	}

	/**
	 * Validates pertinence of autocomplete suggestion result.
	 *
	 * @param input
	 * @param suggestion
	 * @return true if matching and not in exclusions.
	 */
	private boolean isValidAutocompleteSuggestion(String input, String suggestion) {
		boolean valid = false;

		String normalizedInput = parseForSearch(input);
		String normalizedSuggestion = parseForSearch(suggestion);
		if (isNotExcludedByUser(suggestion) && normalizedSuggestion.startsWith(normalizedInput)) {
			valid = true;
		}

		return valid;
	}
}