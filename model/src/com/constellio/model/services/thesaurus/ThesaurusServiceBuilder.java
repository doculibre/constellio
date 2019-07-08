package com.constellio.model.services.thesaurus;

import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThesaurusServiceBuilder {

	private static final Logger LOGGER = Logger.getLogger(ThesaurusServiceBuilder.class.getName());

	private static final String SKOS_CONCEPT_SCHEME = "//skos:ConceptScheme";
	private static final String SKOS_HAS_TOP_CONCEPT_XPATH = "skos:hasTopConcept";
	private static final String DC_TITLE_XPATH = "dc:title";
	private static final String DC_DESCRIPTION_XPATH = "dc:description";
	private static final String DC_CREATOR_XPATH = "dc:creator";
	private static final String DC_DATE_XPATH = "dc:date";
	private static final String DC_LANGUAGE_XPATH = "dc:language";
	private static final String SKOS_CONCEPT_XPATH = "//skos:Concept";
	private static final String RDF_ABOUT_ATTR_XPATH = "@rdf:about";
	private static final String SKOS_PREF_LABEL_XPATH = "skos:prefLabel";
	private static final String SKOS_NOTES_XPATH = "skos:notes";
	private static final String SKOS_BROADER_XPATH = "skos:broader";
	private static final String SKOS_ALT_LABEL_XPATH = "skos:altLabel";
	private static final String SKOS_RELATED_XPATH = "skos:related";
	private static final String SKOS_NARROWER_XPATH = "skos:narrower";

	private static final Namespace rdfNs = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

	private static ThesaurusService thesaurus;

	@SuppressWarnings("unchecked")
	public static ThesaurusService getThesaurus(InputStream skosFileStream) throws ThesaurusInvalidFileFormat {
		thesaurus = new ThesaurusService();
		// thesaurus.setSourceFileLocation();
		Map<String, SkosConcept> parsedConcepts = new HashMap<String, SkosConcept>();
		SAXBuilder builder;
		try {
			builder = new SAXBuilder();
			builder.setValidation(false);
			Document skosJdom = builder.build(skosFileStream);

			SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
			collectNamespaces(skosJdom, namespaceContext);

			// Parsing thesaurus tag
			JDOMXPath currentXPath;

			// Concept scheme
			currentXPath = new JDOMXPath(SKOS_CONCEPT_SCHEME);
			currentXPath.setNamespaceContext(namespaceContext);
			Element conceptSchemeElement = (Element) currentXPath.selectSingleNode(skosJdom);

			processRDFAbout(thesaurus, namespaceContext, conceptSchemeElement);
			processTitle(thesaurus, namespaceContext, conceptSchemeElement);
			processDescription(thesaurus, namespaceContext, conceptSchemeElement);
			processCreator(thesaurus, namespaceContext, conceptSchemeElement);
			processCreationDate(thesaurus, namespaceContext, conceptSchemeElement);
			processLanguage(thesaurus, namespaceContext, conceptSchemeElement);

			LOGGER.finest("First pass of skos:Concept tags (collecting labels)");
			currentXPath = new JDOMXPath(SKOS_CONCEPT_XPATH);
			currentXPath.setNamespaceContext(namespaceContext);
			List<Element> skosConceptElements = currentXPath.selectNodes(skosJdom);

			processSKOSConceptsFirstPass(parsedConcepts, namespaceContext, skosConceptElements);

			LOGGER.finest("Second pass of skos:Concept tags (identifying relationships)");

			processSKOSConceptsSecondPass(parsedConcepts, namespaceContext, skosConceptElements);

			LOGGER.finest("Validating skos:hasTopConcept tags");
			// Validate top concepts
			processTopConcepts(parsedConcepts, skosJdom, namespaceContext);

			LOGGER.finest("Third pass of skos:Concept tags (validating relationships)");
			processSKOSConceptsRelationships(parsedConcepts, namespaceContext, skosConceptElements);

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to create mew thesaurus service", e);
		} finally {
			IOUtils.closeQuietly(skosFileStream);
		}

		return thesaurus;
	}

	@SuppressWarnings("unchecked")
	private static void collectNamespaces(Document doc, SimpleNamespaceContext namespaceContext) {
		List<String> collectedNamespaces = new ArrayList<String>();
		//		processChildren(doc.getRootElement(), collectedNamespaces, namespaceContext);

		Element rootElement = doc.getRootElement();
		Namespace currentNamespace = (Namespace) rootElement.getNamespace();
		String nsUri = (currentNamespace.getURI());
		if (!exist(collectedNamespaces, nsUri)) {
			collectedNamespaces.add(nsUri.trim());
			namespaceContext.addNamespace(currentNamespace.getPrefix(), currentNamespace.getURI());
		}
		List<Namespace> additionalNs = (List<Namespace>) rootElement.getAdditionalNamespaces();
		if (!additionalNs.isEmpty()) {
			copyNsList(additionalNs, collectedNamespaces, namespaceContext);
		}
	}

	//	@SuppressWarnings("unchecked")
	//	private static void processChildren(Element element, List<String> collectedNamespaces,
	//			SimpleNamespaceContext namespaceContext) {
	//		Namespace currentNamespace = (Namespace) element.getNamespace();
	//		String nsUri = (currentNamespace.getURI());
	//		if (!exist(collectedNamespaces, nsUri)) {
	//			collectedNamespaces.add(nsUri.trim());
	//			namespaceContext.addNamespace(currentNamespace.getPrefix(), currentNamespace.getURI());
	//		}
	//		List<Namespace> additionalNs = element.getAdditionalNamespaces();
	//		if (!additionalNs.isEmpty()) {
	//			copyNsList(additionalNs, collectedNamespaces, namespaceContext);
	//		}
	//		if (element.getChildren().size() > 0) {
	//			List<Element> elementChildren = element.getChildren();
	//			for (int i = 0; i < elementChildren.size(); i++) {
	//				processChildren(elementChildren.get(i), collectedNamespaces, namespaceContext);
	//			}
	//		}
	//	}

	private static boolean exist(List<String> collectedNamespaces, String nsUri) {
		if (collectedNamespaces.isEmpty()) {
			return false;
		}
		for (int i = 0; i < collectedNamespaces.size(); i++) {
			if ((collectedNamespaces.get(i)).equals(nsUri)) {
				return true;
			}
		}
		return false;
	}

	private static void copyNsList(List<Namespace> additionalNs, List<String> collectedNamespaces,
								   SimpleNamespaceContext namespaceContext) {
		for (int i = 0; i < additionalNs.size(); i++) {
			Namespace ns = additionalNs.get(i);
			namespaceContext.addNamespace(ns.getPrefix(), ns.getURI());
			collectedNamespaces.add(ns.getURI().trim());
		}
	}

	private static Attribute getAttributeFromXPath(String attr_xpath, SimpleNamespaceContext namespaceContext,
												   Element conceptSchemeElement) throws JaxenException {
		JDOMXPath currentXPath;
		currentXPath = new JDOMXPath(attr_xpath);
		currentXPath.setNamespaceContext(namespaceContext);
		return (Attribute) currentXPath.selectSingleNode(conceptSchemeElement);
	}

	private static Element getElementFromXPath(String elem_xpath, SimpleNamespaceContext namespaceContext,
											   Element conceptSchemeElement) throws JaxenException {
		JDOMXPath currentXPath;
		currentXPath = new JDOMXPath(elem_xpath);
		currentXPath.setNamespaceContext(namespaceContext);
		return (Element) currentXPath.selectSingleNode(conceptSchemeElement);
	}

	@SuppressWarnings("unchecked")
	private static List<Element> getNodesFromXPath(String nodes_xpath, SimpleNamespaceContext namespaceContext,
												   Element conceptSchemeElement) throws JaxenException {
		JDOMXPath currentXPath;
		currentXPath = new JDOMXPath(nodes_xpath);
		currentXPath.setNamespaceContext(namespaceContext);
		return currentXPath.selectNodes(conceptSchemeElement);
	}

	private static void processRDFAbout(ThesaurusService thesaurus, SimpleNamespaceContext namespaceContext,
										Element conceptSchemeElement) throws JaxenException {

		Attribute thesaurusRdfAboutAttribute = getAttributeFromXPath(RDF_ABOUT_ATTR_XPATH, namespaceContext,
				conceptSchemeElement);
		if (thesaurusRdfAboutAttribute != null) {
			String thesaurusRdfAbout = thesaurusRdfAboutAttribute.getValue();
			thesaurus.setRdfAbout(thesaurusRdfAbout);
		}
	}

	private static void processTitle(ThesaurusService thesaurus, SimpleNamespaceContext namespaceContext,
									 Element conceptSchemeElement) throws JaxenException {

		Element titleElement = getElementFromXPath(DC_TITLE_XPATH, namespaceContext, conceptSchemeElement);
		if (titleElement != null) {
			String dcTitle = titleElement.getValue();
			dcTitle = StringEscapeUtils.unescapeXml(dcTitle);
			thesaurus.setDcTitle(dcTitle);
		}
	}

	private static void processDescription(ThesaurusService thesaurus, SimpleNamespaceContext namespaceContext,
										   Element conceptSchemeElement) throws JaxenException {

		Element descriptionElement = getElementFromXPath(DC_DESCRIPTION_XPATH, namespaceContext, conceptSchemeElement);
		if (descriptionElement != null) {
			String dcDescription = descriptionElement.getValue();
			dcDescription = StringEscapeUtils.unescapeXml(dcDescription);
			thesaurus.setDcDescription(dcDescription);
		}
	}

	private static void processCreator(ThesaurusService thesaurus, SimpleNamespaceContext namespaceContext,
									   Element conceptSchemeElement) throws JaxenException {

		Element creatorElement = getElementFromXPath(DC_CREATOR_XPATH, namespaceContext, conceptSchemeElement);
		if (creatorElement != null) {
			String dcCreator = creatorElement.getValue();
			dcCreator = StringEscapeUtils.unescapeXml(dcCreator);
			thesaurus.setDcCreator(dcCreator);
		}
	}

	private static void processCreationDate(ThesaurusService thesaurus, SimpleNamespaceContext namespaceContext,
											Element conceptSchemeElement) throws JaxenException, ParseException {

		Element dateElement = getElementFromXPath(DC_DATE_XPATH, namespaceContext, conceptSchemeElement);
		if (dateElement != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dcDate = sdf.parse(dateElement.getValue());
			thesaurus.setDcDate(dcDate);
		}
	}

	private static void processLanguage(ThesaurusService thesaurus, SimpleNamespaceContext namespaceContext,
										Element conceptSchemeElement) throws JaxenException {

		Element languageElement = getElementFromXPath(DC_LANGUAGE_XPATH, namespaceContext, conceptSchemeElement);
		if (languageElement != null) {
			Locale dcLanguage = new Locale(languageElement.getValue());
			thesaurus.setDcLanguage(dcLanguage);
		}
	}

	private static void processSKOSConceptsFirstPass(Map<String, SkosConcept> parsedConcepts,
													 SimpleNamespaceContext namespaceContext,
													 List<Element> skosConceptElements) throws JaxenException {

		for (Element skosConceptElement : skosConceptElements) {
			Attribute rdfAboutAttribute = getAttributeFromXPath(RDF_ABOUT_ATTR_XPATH, namespaceContext,
					skosConceptElement);
			// récupération de l'ID
			String rdfAbout = rdfAboutAttribute.getValue();
			LOGGER.finest("Collecting labels for " + rdfAbout);

			SkosConcept skosConcept = new SkosConcept();
			skosConcept.setThesaurusService(thesaurus);
			parsedConcepts.put(rdfAbout, skosConcept);
			skosConcept.setRdfAbout(rdfAbout);

			// recupération du descripteur de la langue du thesaurus et les
			// équivalent sont dans une autre langue
			List<Element> prefLabelElements = getNodesFromXPath(SKOS_PREF_LABEL_XPATH, namespaceContext,
					skosConceptElement);

			for (Element prefLabelElement : prefLabelElements) {
				String lang = prefLabelElement.getAttributeValue("lang", Namespace.XML_NAMESPACE);
				Locale prefLabelLocale = lang != null ? new Locale(lang) : Locale.ENGLISH;
				String prefLabel = prefLabelElement.getValue();
				prefLabel = StringEscapeUtils.unescapeXml(prefLabel);
				skosConcept.setPrefLabel(prefLabel, prefLabelLocale);
			}

			// Notes
			Element skosNotesElement = getElementFromXPath(SKOS_NOTES_XPATH, namespaceContext, skosConceptElement);
			if (skosNotesElement != null) {
				String skosNotes = skosNotesElement.getTextTrim();
				skosNotes = StringEscapeUtils.unescapeXml(skosNotes);
				skosConcept.setSkosNotes(skosNotes);
			}

			// Rejected forms
			List<Element> altLabelElements = getNodesFromXPath(SKOS_ALT_LABEL_XPATH, namespaceContext,
					skosConceptElement);
			for (Element altLabelElement : altLabelElements) {
				String lang = altLabelElement.getAttributeValue("lang", Namespace.XML_NAMESPACE);
				Locale altLabelLocale = lang != null ? new Locale(lang) : Locale.ENGLISH;
				String altLabel = altLabelElement.getValue();
				altLabel = StringEscapeUtils.unescapeXml(altLabel);
				skosConcept.addAltLabel(altLabelLocale, altLabel);
			}
		}
		addParsedConcepts(thesaurus, parsedConcepts);
	}

	private static void processSKOSConceptsSecondPass(Map<String, SkosConcept> parsedConcepts,
													  SimpleNamespaceContext namespaceContext,
													  List<Element> skosConceptElements) throws JaxenException {
		for (Element skosConceptElement : skosConceptElements) {
			Attribute rdfAboutAttribute = getAttributeFromXPath(RDF_ABOUT_ATTR_XPATH, namespaceContext,
					skosConceptElement);
			// recupération de l'ID
			String rdfAbout = rdfAboutAttribute.getValue();
			LOGGER.finest("Processing relationships for " + rdfAbout);
			SkosConcept skosConcept = parsedConcepts.get(rdfAbout);

			// recupération des termes génériques rdf:resource
			List<Element> broaderElements = getNodesFromXPath(SKOS_BROADER_XPATH, namespaceContext, skosConceptElement);

			for (Element broaderElement : broaderElements) {
				String resource = broaderElement.getAttributeValue("resource", rdfNs);
				SkosConcept broader = parsedConcepts.get(resource);
				if (broader != null) {
					skosConcept.addBroader(broader);
					broader.addNarrower(skosConcept);
				} else {
					StringBuffer errorMsg = new StringBuffer();
					errorMsg.append("Concept ");
					errorMsg.append(rdfAbout);
					errorMsg.append(" has a missing broader concept :");
					errorMsg.append(resource);
					LOGGER.severe(errorMsg.toString());
					// throw new RuntimeException(errorMsg.toString());
				}
			}
			// No broader element, therefore this is a top concept
			if (skosConcept.getBroader().isEmpty()) {
				thesaurus.addTopConcept(skosConcept);
			}

			// Associées
			List<Element> relatedElements = getNodesFromXPath(SKOS_RELATED_XPATH, namespaceContext, skosConceptElement);

			for (Element relatedElement : relatedElements) {
				String resource = relatedElement.getAttributeValue("resource", rdfNs);
				SkosConcept related = parsedConcepts.get(resource);
				skosConcept.addRelated(related);
			}
		}
	}

	private static void addParsedConcepts(ThesaurusService thesaurus, Map<String, SkosConcept> parsedConcepts) {
		thesaurus.setAllConcepts(parsedConcepts);
	}

	@SuppressWarnings("unchecked")
	private static void processTopConcepts(Map<String, SkosConcept> parsedConcepts, Document skosJdom,
										   SimpleNamespaceContext namespaceContext) throws JaxenException {
		JDOMXPath currentXPath;
		currentXPath = new JDOMXPath(SKOS_HAS_TOP_CONCEPT_XPATH);
		currentXPath.setNamespaceContext(namespaceContext);
		List<Element> hasTopConceptElements = currentXPath.selectNodes(skosJdom);

		for (Element hasTopConceptElement : hasTopConceptElements) {
			String resource = hasTopConceptElement.getAttributeValue("resource", rdfNs);
			SkosConcept topConcept = parsedConcepts.get(resource);
			if (!topConcept.getBroader().isEmpty()) {
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append("Top concept ");
				errorMsg.append(resource);
				errorMsg.append(" has broader concept(s) (");
				for (Iterator<SkosConcept> it = topConcept.getBroader().iterator(); it.hasNext(); ) {
					SkosConcept broaderConcept = it.next();
					errorMsg.append(broaderConcept.getRdfAbout());
					if (it.hasNext()) {
						errorMsg.append(", ");
					}
				}
				errorMsg.append(")");
				throw new RuntimeException(errorMsg.toString());
			} else {
				LOGGER.finest(resource + " is a valid top concept");
			}
		}
	}

	private static void processSKOSConceptsRelationships(Map<String, SkosConcept> parsedConcepts,
														 SimpleNamespaceContext namespaceContext,
														 List<Element> skosConceptElements) throws JaxenException {
		for (Element skosConceptElement : skosConceptElements) {
			Attribute rdfAboutAttribute = getAttributeFromXPath(RDF_ABOUT_ATTR_XPATH, namespaceContext,
					skosConceptElement);

			// recupération de l'ID
			String rdfAbout = rdfAboutAttribute.getValue();
			SkosConcept skosConcept = parsedConcepts.get(rdfAbout);

			// Récupération des termes spécifiques
			// recupération des termes spécifiques avec rdf:resource
			List<Element> narrowerElements = getNodesFromXPath(SKOS_NARROWER_XPATH, namespaceContext,
					skosConceptElement);

			for (Element narrowerElement : narrowerElements) {
				String resource = narrowerElement.getAttributeValue("resource", rdfNs);
				SkosConcept narrower = parsedConcepts.get(resource);
				if (narrower != null) {
					if (narrower.getBroader().isEmpty()) {
						StringBuffer errorMsg = new StringBuffer();
						errorMsg.append("Le concept ");
						errorMsg.append(rdfAbout);
						errorMsg.append(" possède un concept spécifique (narrower) (");
						errorMsg.append(narrower.getRdfAbout());
						errorMsg.append(") qui ne possède pas un concept générique (broader) correspondant.");
						LOGGER.severe(errorMsg.toString());
						// errorMessages.add(errorMsg.toString());
						// throw new RuntimeException(errorMsg.toString());
					} else if (!narrower.getBroader().contains(skosConcept)) {
						StringBuffer errorMsg = new StringBuffer();
						errorMsg.append("Le concept ");
						errorMsg.append(rdfAbout);
						errorMsg.append(" possède au moins un concept spécifique (narrower) (");
						errorMsg.append(narrower.getRdfAbout());
						errorMsg.append(") qui ne possède pas un concept générique (broader) correspondant. (");
						for (Iterator<SkosConcept> it = narrower.getBroader().iterator(); it.hasNext(); ) {
							SkosConcept narrowerConcept = it.next();
							errorMsg.append(narrowerConcept.getRdfAbout());
							if (it.hasNext()) {
								errorMsg.append(", ");
							}
						}
						errorMsg.append(")");
						LOGGER.severe(errorMsg.toString());
						// errorMessages.add(errorMsg.toString());
						// throw new RuntimeException(errorMsg.toString());
					} else {
						LOGGER.finest(resource + " has a valid broader/narrower relationship");
					}
				} else {
					StringBuffer errorMsg = new StringBuffer();
					errorMsg.append("Il manque un concept spécifique (narrower) au concept générique : ");
					errorMsg.append(rdfAbout);
					LOGGER.severe(errorMsg.toString());
					// errorMessages.add(errorMsg.toString());
					// throw new RuntimeException(errorMsg.toString());
				}
			}
		}
	}
}
