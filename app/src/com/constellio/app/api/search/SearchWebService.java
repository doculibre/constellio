package com.constellio.app.api.search;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.framework.components.converters.CollectionCodeToLabelConverter;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.thesaurus.ResponseSkosConcept;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.base.Objects;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.Schemas.CAPTION;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.records.GetRecordOptions.RETURNING_SUMMARY;
import static com.constellio.model.services.records.GetRecordOptions.WARN_IF_DOES_NOT_EXIST;
import static com.constellio.model.services.search.SearchServices.addParamsForFreeTextSearch;
import static com.constellio.model.services.search.SearchServices.addParamsForHighlight;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

public class SearchWebService extends AbstractSearchServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchWebService.class);

	@Override
	protected void doGet(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ModifiableSolrParams solrParams = getModifiableSolrParams(req.getQueryString());
		boolean searchingInEvents = "true".equals(req.getParameter(SEARCH_EVENTS));
		String thesaurusValue = req.getParameter(THESAURUS_VALUE);
		String core = req.getParameter(CORE);

		solrParams.add("fq", "-type_s:index");

		solrParams.set("rows", StringUtils.defaultString(solrParams.get("rows"), "" + 10));
		solrParams.set("start", StringUtils.defaultString(solrParams.get("start"), "" + 0));

		String[] strings = solrParams.getParams("fq");

		String collection = "";

		ResponseSkosConcept responseSkosConcept = null;

		for (String param : strings) {
			if (param.startsWith("collection_s")) {
				collection = param.replace("collection_s:", "");
				collection = collection.replace("\"", "");
				break;
			}
		}

		String freeText = solrParams.get("freeText");
		solrParams.remove("freeText");
		String facetWithLabels = solrParams.get("facet.withLabels");
		solrParams.remove("facet.withLabels");

		String[] collectionsPlaceHolderParam = solrParams.getParams("collection");

		List<String> collectionFromPlaceHolder = new ArrayList<>();

		if (collectionsPlaceHolderParam != null && StringUtils.isNotBlank(collection)) {
			throw new RuntimeException("You cannot use the collection placeholder and also provide an fq " +
									   "containing a collection_s condition");
		}


		if (collectionsPlaceHolderParam != null) {
			for (int i = 0; i < collectionsPlaceHolderParam.length; i++) {
				collectionFromPlaceHolder.add(collectionsPlaceHolderParam[i]);
				collectionsPlaceHolderParam[i] = Schemas.COLLECTION.getDataStoreCode() + ":" + collectionsPlaceHolderParam[i];
			}

			solrParams.add("fq", join(collectionsPlaceHolderParam, " OR "));
			solrParams.remove("collection");
		}

		List<String> userCollections = user.getCollections();
		List<String> queryCollections = StringUtils.isNotBlank(collection) ? asList(collection) : collectionFromPlaceHolder.size() > 0 ? collectionFromPlaceHolder : userCollections;
		if (solrParams.get("q") == null) {
			adjustForFreeText(queryCollections, freeText, solrParams);
		}

		List<String[]> extraMetadatas = removeExtraReferencedMetadatas(solrParams);

		QueryResponse queryResponse;
		if (!Strings.isNullOrEmpty(thesaurusValue) && searchingInEvents) {
			throw new RuntimeException("You cannot search event and have a thesaurusValue");
		} else if (searchingInEvents) {
			if (!user.isSystemAdmin()) {
				throw new RuntimeException("You need system admin privileges");
			}

			queryResponse = getEventQueryResponse(solrParams);

		} else {
			SchemasRecordsServices schemasRecordsServices = null;
			ArrayList<String> paramList = new ArrayList<>();
			SearchEvent searchEvent = null;

			if (!Strings.isNullOrEmpty(collection)) {
				schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory());
				searchEvent = schemasRecordsServices.newSearchEvent();
				searchEvent.setClickCount(0);

				for (String paramName : solrParams.getParameterNames()) {
					if (!paramName.equals("qf") && !paramName.equals("pf")
						&& !paramName.equals("fl")) {
						if (paramName.equals("q")) {
							String query = StringUtils.stripAccents(solrParams.get(paramName).toLowerCase());

							searchEvent.setQuery(query);
							searchEvent.setOriginalQuery(query);
						} else {
							String[] values = solrParams.getParams(paramName);

							if (values.length == 1) {
								paramList.add(paramName + "=" + values[0]);
							} else if (values.length > 1) {
								StringBuilder valuesAsOneStringBuilder = new StringBuilder();
								for (String value : values) {
									valuesAsOneStringBuilder.append(paramName).append("=").append(value).append(";");
								}
								paramList.add(valuesAsOneStringBuilder.toString());
							}

						}
					}
				}
			}

			if (StringUtils.isBlank(solrParams.get(CommonParams.DF))) {
				solrParams.add(CommonParams.DF, TITLE.getDataStoreCode());
			}

			queryResponse = getQueryResponse(core, solrParams, user);

			if (!extraMetadatas.isEmpty()) {
				insertExtraMetadatas(queryResponse, extraMetadatas, user);
			}

			if (schemasRecordsServices != null) {

				if (solrParams.get("rows") == null) {
					paramList.add("rows=10");
				}

				if (solrParams.get("start") == null) {
					paramList.add("start=0");
				}

				searchEvent.setParams(paramList);
				searchEvent.setQTime(queryResponse.getQTime());
				searchEvent.setNumFound(queryResponse.getResults().getNumFound());

				if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
					SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory());
					searchEventServices.save(searchEvent);
				}
			}
		}

		NamedList skosConceptsNL = new NamedList();

		if (responseSkosConcept != null && responseSkosConcept.getSuggestions().size() > 0) {
			NamedList suggestionsNL = new NamedList();

			for (Locale locale : responseSkosConcept.getSuggestions().keySet()) {
				List<String> suggestionList = responseSkosConcept.getSuggestions().get(locale);
				NamedList localeSuggestionsNL = new NamedList();
				for (String suggestion : suggestionList) {
					localeSuggestionsNL.add("label", suggestion);
				}

				suggestionsNL.add(locale.getLanguage(), localeSuggestionsNL);
			}

			skosConceptsNL.add(ThesaurusService.SUGGESTIONS, suggestionsNL);
		}

		if (responseSkosConcept != null && responseSkosConcept.getDisambiguations().size() > 0) {
			NamedList disambiguationsNL = new NamedList();

			for (Locale locale : responseSkosConcept.getDisambiguations().keySet()) {
				List<String> disambiguationList = responseSkosConcept.getDisambiguations().get(locale);
				NamedList localeDisambiguationsNL = new NamedList();
				for (String disambiguation : disambiguationList) {
					localeDisambiguationsNL.add("label", disambiguation);
				}

				disambiguationsNL.add(locale.getLanguage(), localeDisambiguationsNL);
			}

			skosConceptsNL.add(ThesaurusService.DISAMBIGUATIONS, disambiguationsNL);
		}

		NamedList facetLabels = null;
		if (StringUtils.isNotBlank(facetWithLabels) && facetWithLabels.trim().toLowerCase().equals("true")) {
			facetLabels = getFacetLabels(queryResponse, queryCollections);
		}

		writeResponse(resp, solrParams, queryResponse, skosConceptsNL, null, facetLabels, null, null);
	}

	private List<String[]> removeExtraReferencedMetadatas(ModifiableSolrParams solrParams) {

		if (solrParams.get("fl.extraReferencedMetadatas") != null) {

			List<String[]> extras = new ArrayList<>();
			for (String extraReferencedMetadata : solrParams.get("fl.extraReferencedMetadatas").split(",")) {
				String item = extraReferencedMetadata.replace(" ", "");
				if (item.contains(".")) {
					String[] parts = split(item, ".");
					if (parts.length != 2) {
						throw new IllegalArgumentException("Bad 'fl.extraReferencedMetadatas' value : " + item);
					} else {
						extras.add(parts);
					}
				} else {
					throw new IllegalArgumentException("Bad 'fl.extraReferencedMetadatas' value : " + item);
				}
			}

			solrParams.remove("fl.extraReferencedMetadatas");
			return extras;
		} else {
			return Collections.emptyList();
		}

	}

	private void insertExtraMetadatas(QueryResponse queryResponse, List<String[]> extraMetadatas,
									  SystemWideUserInfos user) {

		for (SolrDocument document : queryResponse.getResults()) {

			String schemaCode = (String) document.getFieldValue("schema_s");
			String collection = (String) document.getFieldValue("collection_s");

			if (schemaCode != null && collection != null) {
				MetadataSchemaTypes types = modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
				MetadataSchema schema = types.getSchema(schemaCode);
				for (String[] extraMetadata : extraMetadatas) {
					Metadata metadata = schema.getMetadataWithCodeOrNull(extraMetadata[0]);
					if (metadata != null && metadata.getType() == REFERENCE && !metadata.isMultivalue()
						&& document.getFieldValue(metadata.getDataStoreCode()) != null) {
						String referencedMetadataDataStoreCode = extraMetadata[1];
						insertExtraMetadataInDocument(user, document, types, metadata, referencedMetadataDataStoreCode);
					}
				}
			}
		}
	}

	private void insertExtraMetadataInDocument(SystemWideUserInfos user, SolrDocument document,
											   MetadataSchemaTypes types, Metadata metadata,
											   String referencedMetadataDataStoreCode) {
		String id = (String) document.getFieldValue(metadata.getDataStoreCode());
		Record record = modelLayerFactory().newRecordServices().get(id, WARN_IF_DOES_NOT_EXIST, RETURNING_SUMMARY);
		if (record != null) {
			MetadataSchema referencedRecordSchema = types.getSchemaOf(record);
			Metadata referencedRecordMetadata = referencedRecordSchema.getMetadataByDatastoreCode(referencedMetadataDataStoreCode);

			if (referencedRecordMetadata == null) {
				throw new IllegalArgumentException("Bad 'fl.extraReferencedMetadatas' value : " + metadata.getLocalCode() + "." + referencedMetadataDataStoreCode);
			}

			if (referencedRecordSchema.getSchemaType().getCacheType().isSummaryCache() &&
				!referencedRecordMetadata.isAvailableInSummary()) {
				record = modelLayerFactory().newRecordServices().get(id, WARN_IF_DOES_NOT_EXIST);
			}

			if (record.get(referencedRecordMetadata) != null) {
				if (!hasUserAccessTo(user, record, referencedRecordSchema, referencedRecordMetadata)) {
					LOGGER.warn("User does not have access to metadata '" + referencedRecordMetadata.getCode() + "' of " + id);
				} else {
					String field = metadata.getLocalCode() + "." + referencedRecordMetadata.getDataStoreCode();
					document.addField(field, record.get(referencedRecordMetadata));
				}
			}
		}
	}

	private boolean hasUserAccessTo(SystemWideUserInfos systemWideUser, Record record,
									MetadataSchema referencedRecordSchema, Metadata referencedRecordMetadata) {

		if (!referencedRecordSchema.getSchemaType().hasSecurity()) {
			return true;
		}

		User user = modelLayerFactory().newUserServices().getUserInCollection(systemWideUser.getUsername(), record.getCollection());
		return referencedRecordMetadata.isSameLocalCodeThanAny(TITLE, CAPTION) ||
			   (user.hasReadAccess().on(record) && user.hasAccessToMetadata(referencedRecordMetadata, record));
	}

	private NamedList getFacetLabels(QueryResponse queryResponse, List<String> collections) {
		List<Metadata> specialMetadatas = asList(Schemas.COLLECTION);
		RecordServices recordServices = modelLayerFactory().newRecordServices();
		List<MetadataSchemaTypes> allCollectionsSchemaTypes = modelLayerFactory().getMetadataSchemasManager().getAllCollectionsSchemaTypes();
		List<Metadata> allMetadatas = new ArrayList<>();
		allMetadatas.addAll(specialMetadatas);
		allCollectionsSchemaTypes.stream().forEach(schemaTypes -> allMetadatas.addAll(schemaTypes.getAllMetadatas()));
		Map<String, Metadata> uniqueMetadatas = allMetadatas.stream()
				.collect(Collectors.toMap(Metadata::getDataStoreCode, Function.identity(), (meta1, meta2) -> meta1));

		NamedList facetLabelsNL = new NamedList();
		NamedList facetFieldsLabelsNL = new NamedList();

		List<FacetField> facetFields = Objects.firstNonNull(queryResponse.getFacetFields(), Collections.<FacetField>emptyList());
		for (FacetField facetField : facetFields) {
			Metadata facetMetadata = uniqueMetadatas.get(facetField.getName());
			if (facetMetadata != null) {
				Map<String, String> facetLabels = facetMetadata.getLabelsByLanguageCodes();
				NamedList valuesName = new NamedList();
				if (specialMetadatas.contains(facetMetadata)) {
					facetLabels = getFacetLabelsForSpecialMetadata(facetMetadata, facetField.getValues());
				}

				for (String languageCode : facetLabels.keySet()) {
					Locale locale = Language.withCode(languageCode).getLocale();
					Map<String, String> valueLabels = new HashMap<>();
					switch (facetMetadata.getType()) {
						case REFERENCE:
							valueLabels = facetField.getValues().stream().map(value -> recordServices.getDocumentByIdOrNull(value.getName())).filter(record -> record != null).collect(Collectors.toMap(record -> record.getId(),
									record -> StringUtils.defaultIfBlank(SchemaCaptionUtils.getShortCaptionForRecord(record, locale, false), "")));
							break;
						case ENUM:
							Class<? extends EnumWithSmallCode> enumClass = (Class<? extends EnumWithSmallCode>) facetMetadata.getEnumClass();
							EnumWithSmallCodeToCaptionConverter captionConverter = new EnumWithSmallCodeToCaptionConverter(enumClass);
							valueLabels = facetField.getValues().stream().map(value -> value.getName()).collect(
									Collectors.toMap(enumCode -> enumCode, enumCode -> captionConverter.convertToPresentation(enumCode, null, locale)));
							break;
						default:
							break;
					}

					if (specialMetadatas.contains(facetMetadata)) {
						valueLabels = getValueLabelsForSpecialMetadata(facetMetadata, facetField.getValues());
					}

					if (!valueLabels.isEmpty()) {
						valuesName.add(languageCode, valueLabels);
					}
				}

				if (valuesName.size() != 0) {
					facetFieldsLabelsNL.add(facetMetadata.getDataStoreCode(), valuesName);
				}
			}
		}

		if (facetFieldsLabelsNL.size() != 0) {
			facetLabelsNL.add("facet_fields", facetFieldsLabelsNL);
		}

		return facetLabelsNL;
	}

	private Map<String, String> getFacetLabelsForSpecialMetadata(Metadata facetMetadata, List<Count> values) {
		CollectionsManager collectionsManager = appLayerFactory().getCollectionsManager();
		List<String> collectionCodes = collectionsManager.getCollectionCodes();
		if (facetMetadata.getLocalCode().equals(Schemas.COLLECTION.getLocalCode())) {
			Map<Language, String> labels = modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collectionCodes.get(0)).getSchemaType(Collection.SCHEMA_TYPE).getLabels();
			Map<String, String> labelsByCode = new HashMap<>();
			labels.keySet().stream().forEach(language -> labelsByCode.put(language.getCode(), labels.get(language)));
			return labelsByCode;
		} else {
			return new HashMap<>();
		}
	}

	private Map<String, String> getValueLabelsForSpecialMetadata(Metadata facetMetadata, List<Count> values) {
		if (facetMetadata.getLocalCode().equals(Schemas.COLLECTION.getLocalCode())) {
			CollectionCodeToLabelConverter labelConverter = new CollectionCodeToLabelConverter();
			return values.stream().collect(Collectors.toMap(Count::getName,
					value -> labelConverter.getCollectionCaption(value.getName())));
		} else {
			return new HashMap<>();
		}
	}

	private void adjustForFreeText(List<String> collections, String freeText, ModifiableSolrParams solrParams) {
		if (freeText != null) {
			ConstellioEIMConfigs systemConfigs = modelLayerFactory().getSystemConfigs();
			MetadataSchemasManager schemas = modelLayerFactory().getMetadataSchemasManager();
			CollectionsListManager collectionsManager = modelLayerFactory().getCollectionsListManager();
			SearchBoostManager searchBoostManager = modelLayerFactory().getSearchBoostManager();
			String mainDataLanguage = modelLayerFactory().getConfiguration().getMainDataLanguage();

			Set<String> languages = new HashSet<>();
			Comparator<SearchBoost> boostComparator = new Comparator<SearchBoost>() {
				@Override
				public int compare(SearchBoost o1, SearchBoost o2) {
					return o1.getKey().compareTo(o2.getKey());
				}
			};
			Set<SearchBoost> fieldBoosts = new TreeSet<>(boostComparator);
			Set<SearchBoost> queryBoosts = new TreeSet<>(boostComparator);

			List<MetadataSchemaType> searchedSchemaTypes = new ArrayList<>();
			collections.stream().forEach(collection -> {
				languages.addAll(collectionsManager.getCollectionInfo(collection).getCollectionLanguesCodes());
				fieldBoosts.addAll(searchBoostManager.getAllSearchBoostsByMetadataType(collection));
				queryBoosts.addAll(searchBoostManager.getAllSearchBoostsByQueryType(collection));
				searchedSchemaTypes.addAll(schemas.getSchemaTypes(collection).getSchemaTypes());
			});

			addParamsForFreeTextSearch(solrParams, freeText, null, new ArrayList<>(languages), null,
					mainDataLanguage, new ArrayList<>(fieldBoosts), new ArrayList<>(queryBoosts), searchedSchemaTypes, systemConfigs);

			solrParams.add(CommonParams.DF, TITLE.getDataStoreCode());

			addParamsForHighlight(solrParams, MetadataSchemaTypes.getSearchableMetadatas(searchedSchemaTypes), new ArrayList<>(languages));
		}

		solrParams.set(CommonParams.Q, StringUtils.defaultString(freeText, "*:*"));
	}
}
