package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RMMessage;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.components.conversations.ConversationFacetsHandler.FacetChanged.FacetChangedListener;
import com.constellio.app.ui.framework.components.conversations.ConversationFacetsHandler.FacetChanged.FacetChangedObservable;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryFacetFilters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.getLocale;

public class ConversationFacetsHandler {
	private final String collection;
	private final SessionContext sessionContext;
	private final ModelLayerFactory modelLayerFactory;

	private final FacetChangedObservable facetChangedObservable;

	private KeySetMap<String, String> facetSelections = new KeySetMap<>();
	private Map<String, Boolean> facetStatus = new HashMap<>();
	private String sortCriterion;
	private SortOrder sortOrder = SortOrder.ASCENDING;

	public ConversationFacetsHandler(String collection,
									 SessionContext sessionContext,
									 ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.sessionContext = sessionContext;
		this.modelLayerFactory = modelLayerFactory;

		this.facetChangedObservable = new FacetChangedObservable();
	}

	public void selectFacetValue(String facetId, String facetValue) {
		facetSelections.get(facetId).add(facetValue);

		fireFacetChangeEventIfRequested();
	}

	public void deselectFacetValue(String facetId, String facetValue) {
		facetSelections.get(facetId).remove(facetValue);

		fireFacetChangeEventIfRequested();
	}

	public void changeFacetValues(KeySetMap<String, String> facets) {
		facetSelections.clear();
		facetSelections.addAll(facets);

		fireFacetChangeEventIfRequested();
	}

	public void deselectFacet(String facetId) {
		facetSelections.get(facetId).clear();
		fireFacetChangeEventIfRequested();
	}

	public void openFacet(String facetId) {
		facetStatus.put(facetId, true);
	}

	public void closeFacet(String facetId) {
		facetStatus.put(facetId, false);
	}


	public void selectSortCriterion(String sortCriterion, SortOrder sortOrder) {
		setSortCriterion(sortCriterion);
		setSortOrder(sortOrder);

		fireFacetChangeEventIfRequested();
	}

	private void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getSortCriterion() {
		return sortCriterion;
	}

	private void setSortCriterion(String sortCriterion) {
		this.sortCriterion = sortCriterion;
	}

	public Map<String, List<FacetValue>> getFieldFacetValues(LogicalSearchQuery query) {
		setupQueryForFacetsSelection(query);

		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, new SerializableSearchCache(), true);
		return searchServices.getFieldFacetValues(query);
	}

	public Map<String, Integer> getQueryFacetsValues(LogicalSearchQuery query) {
		setupQueryForFacetsSelection(query);

		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, new SerializableSearchCache(), true);
		return searchServices.getQueryFacetsValues(query);
	}


	private LogicalSearchQuery setupQueryForFacetsSelection(LogicalSearchQuery query) {
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
		MetadataSchema messageSchema = schemasRecordsServices.schemaType(Message.SCHEMA_TYPE).getDefaultSchema();

		query.setNumberOfRows(0);

		query.addFieldFacets(Arrays.asList(
				messageSchema.get(Message.MESSAGE_AUTHOR).getDataStoreCode(),
				messageSchema.get(Message.HAS_URL_IN_MESSAGE).getDataStoreCode(),
				messageSchema.get(RMMessage.HAS_LINKED_DOCUMENTS).getDataStoreCode()
		));

		applyFacetsFilterOnQuery(query);

		return query;
	}

	public void addFacetChangedListener(FacetChangedListener listener) {
		facetChangedObservable.addListener(listener);
	}

	public void removeFacetChangedListener(FacetChangedListener listener) {
		facetChangedObservable.removeListener(listener);
	}

	public void applyFacetsFilterOnQuery(LogicalSearchQuery query) {
		LogicalSearchQueryFacetFilters filters = query.getFacetFilters();
		filters.clear();
		for (Entry<String, Set<String>> selection : facetSelections.getMapEntries()) {
			filters.selectedFieldFacetValues(selection.getKey(), selection.getValue());
		}
	}

	public List<FacetVO> getFacetsForQuery(LogicalSearchQuery query) {
		Map<String, List<FacetValue>> fieldFacetValues = getFieldFacetValues(query);

		List<FacetVO> facetVOS = new ArrayList<>();
		fieldFacetValues.forEach((key, values) -> {
			List<FacetValueVO> valueVOS = values.stream().map(value -> new FacetValueVO(key, value, getFacetValueLabel(key, value.getValue()))).collect(Collectors.toList());
			facetVOS.add(new FacetVO(key, getFacetLabel(key), valueVOS, true, 5));
		});

		return facetVOS;
	}

	private String getFacetLabel(String facetId) {

		String label;

		String labeli18nCode = "Conversation.displayFolderTab.facets.";

		if (facetId.startsWith(Message.MESSAGE_AUTHOR)) {
			labeli18nCode += Message.MESSAGE_AUTHOR;
		} else if (facetId.startsWith(Message.HAS_URL_IN_MESSAGE)) {
			labeli18nCode += Message.HAS_URL_IN_MESSAGE;
		} else if (facetId.startsWith(RMMessage.HAS_LINKED_DOCUMENTS)) {
			labeli18nCode += RMMessage.HAS_LINKED_DOCUMENTS;
		}

		label = $(labeli18nCode);
		if (StringUtils.isBlank(label)) {
			label = facetId;
		}

		return label;
	}

	private String getFacetValueLabel(String facetId, String facetValue) {
		String label = facetValue;

		if (facetId.endsWith("Id_s") || facetId.endsWith("Id_ss")) {
			Record record = modelLayerFactory.newRecordServices().get(facetValue);
			String keyShort = "caption." + record.getTypeCode() + ".record.short";
			String caption = SchemaCaptionUtils.getShortCaptionForRecord(record, getLocale(), true);
			if (keyShort.equals(caption)) {
				String key = "caption." + record.getTypeCode() + ".record";

				if (key.equals(caption)) {
					if (Category.SCHEMA_TYPE.equals(record.getTypeCode())) {
						label = record.<String>get(Schemas.CODE) + " - " + record.<String>get(Schemas.TITLE);
					} else {
						label = record.get(Schemas.TITLE);
					}
				} else {
					label = caption;
				}
			} else {
				label = caption;
			}
		} else if ("__TRUE__".equals(facetValue)) {
			label = $("Conversation.displayFolderTab.facets.value.true");
		} else if ("__FALSE__".equals(facetValue)) {
			label = $("Conversation.displayFolderTab.facets.value.false");
		}

		return label;
	}

	public KeySetMap<String, String> getFacetSelections() {
		return facetSelections;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	protected List<MetadataVO> getMetadataAllowedInSort(String schemaTypeCode) {
		MetadataSchemaType schemaType = schemaType(schemaTypeCode);
		return getMetadataAllowedInSort(schemaType);
	}

	protected List<MetadataVO> getMetadataAllowedInSort(MetadataSchemaType schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			if (metadata.isSortable()) {
				result.add(builder.build(metadata, sessionContext));
			}
		}
		return result;
	}

	public List<MetadataVO> getMetadataAllowedInSort() {
		MetadataSchemaTypes schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

		return getCommonMetadataAllowedInSort(Collections.singletonList(schemaTypes.getSchemaType(Message.SCHEMA_TYPE)));
	}

	private List<MetadataVO> getCommonMetadataAllowedInSort(List<MetadataSchemaType> schemaTypes) {
		List<MetadataVO> result = new ArrayList<>();
		Set<String> resultCodes = new HashSet<>();
		for (MetadataSchemaType metadataSchemaType : schemaTypes) {
			for (MetadataVO metadata : getMetadataAllowedInSort(metadataSchemaType)) {
				if (resultCodes.add(metadata.getLocalCode())) {
					result.add(metadata);
				}
			}
		}
		return result;
	}

	public String getSortCriterionValueAmong(List<MetadataVO> sortableMetadata) {
		if (sortCriterion == null) {
			return null;
		}
		if (!sortCriterion.startsWith("global_")) {
			return sortCriterion;
		} else {
			String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(sortCriterion);
			for (MetadataVO metadata : sortableMetadata) {
				if (metadata.getLocalCode().equals(localCode)) {
					return metadata.getCode();
				}
			}
		}
		return sortCriterion;
	}

	private MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	private MetadataSchemaType schemaType(String code) {
		return types().getSchemaType(code);
	}

	private void fireFacetChangeEventIfRequested() {
		facetChangedObservable.fire(new FacetChanged(this));
	}

	public static class FacetChanged extends EventArgs<ConversationFacetsHandler> {

		public FacetChanged(ConversationFacetsHandler sender) {
			super(sender);
		}

		public interface FacetChangedListener extends EventListener<FacetChanged> {
		}

		public static class FacetChangedObservable extends EventObservable<FacetChanged> {
		}
	}
}
