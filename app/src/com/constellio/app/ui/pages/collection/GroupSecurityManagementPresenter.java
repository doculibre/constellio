package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.ui.builders.GroupToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.thesaurus.ThesaurusService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class GroupSecurityManagementPresenter extends SingleSchemaBasePresenter<SecurityManagement> {

	RecordVODataProvider groupDataProvider;
	Boolean allItemsSelected = false;
	Boolean allItemsDeselected = false;
	private boolean active = true;
	private Set<String> allRecordIds;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private Group user;

	private GroupToVOBuilder groupVOBuilder;

	private Map<String, String> params = null;

	private transient ConstellioEIMConfigs eimConfigs;
	private transient RMConfigs rmConfigs;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;
	private transient MetadataSchemasManager metadataSchemasManager;
	private transient RecordServices recordServices;
	private transient ModelLayerCollectionExtensions extensions;
	private transient RMModuleExtensions rmModuleExtensions;

	private Set<String> selectedRecordIds = new HashSet<>();
	SortOrder sortOrder = SortOrder.ASCENDING;

	public GroupSecurityManagementPresenter(SecurityManagement view, RecordVO recordVO) {
		super(view, Group.DEFAULT_SCHEMA);
		initTransientObjects();
		if (recordVO != null) {
			forParams(recordVO.getId());
		}
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		groupVOBuilder = new GroupToVOBuilder();
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		user = appLayerFactory.getModelLayerFactory().newUserServices().getGroupInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);

	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public int getGroupCount() {
		return this.groupDataProvider.size();
	}

	public void viewAssembled() {

		view.setDataProvider(groupDataProvider);
	}

	public void forParams(String params) {

		MetadataSchemaVO groupSchemaVO = schemaVOBuilder.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(groupSchemaVO.getCode(), groupVOBuilder);
		groupDataProvider = new RecordVODataProvider(Arrays.asList(groupSchemaVO), voBuilders, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getGroupsQuery();
			}

			@Override
			public boolean isSearchCache() {
				return eimConfigs.isOnlySummaryMetadatasDisplayedInTables();
			}
		};
		view.setDataProvider(groupDataProvider);

	}


	void recordsDroppedOn(List<RecordVO> droppedRecordVOs, RecordVO targetFolderRecordVO) {

		//TODO
		//is it useful to drag and drop groups?
	}

	boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	boolean isAllItemsDeselected() {
		return allItemsDeselected;
	}

	public boolean isSelected(RecordVO recordVO) {
		return allItemsSelected || selectedRecordIds.contains(recordVO.getId());
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return this.active;
	}

	void selectAllClicked() {
		allItemsSelected = true;
		allItemsDeselected = false;
	}

	void deselectAllClicked() {
		allItemsSelected = false;
		allItemsDeselected = true;
		selectedRecordIds.clear();
	}

	public void recordSelectionChanged(RecordVO recordVO, Boolean selected) {
		String recordId = recordVO.getId();
		if (allItemsSelected && !selected) {
			allItemsSelected = false;

			for (String currentRecordId : getOrFetchAllRecordIds()) {
				if (!selectedRecordIds.contains(currentRecordId)) {
					selectedRecordIds.add(currentRecordId);
					allRecordIds.add(currentRecordId);
				}
			}
		} else if (selected) {
			if (allItemsDeselected) {
				allItemsDeselected = false;
			}
			selectedRecordIds.add(recordId);
			if (selectedRecordIds.size() == getOrFetchAllRecordIds().size()) {
				allItemsSelected = true;
			}
		} else {
			selectedRecordIds.remove(recordId);
			if (!allItemsSelected && selectedRecordIds.isEmpty()) {
				allItemsDeselected = true;
			}
		}
	}

	private Set<String> getOrFetchAllRecordIds() {
		if (allRecordIds == null) {
			List<String> allRecordIdsList = searchServices().searchRecordIds(getGroupsQuery());
			allRecordIds = new HashSet<>(allRecordIdsList);
		}

		return allRecordIds;
	}

	private LogicalSearchQuery getGroupsQuery() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		MetadataSchemaType groupSchemaType = getGroupSchema();

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(groupSchemaType)
				.where(Schemas.COLLECTION).isEqualTo(collection);

		if (this.active) {
			condition = condition.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		} else {
			condition = condition.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		}

		query.setCondition(condition);

		return query;
	}

	private MetadataSchemaType getGroupSchema() {
		return schemaType(Group.SCHEMA_TYPE);
	}

	protected String filterSolrOperators(String expression) {
		String groupSearchExpression = expression;

		if (StringUtils.isNotBlank(groupSearchExpression) && groupSearchExpression.startsWith("\"") && groupSearchExpression.endsWith("\"")) {
			groupSearchExpression = ClientUtils.escapeQueryChars(groupSearchExpression);
			groupSearchExpression = "\"" + groupSearchExpression + "\"";
		}

		return groupSearchExpression;
	}

	public void clearSearch() {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(Group.DEFAULT_SCHEMA);

		MetadataSchemaVO groupSchemaVO = schemaVOBuilder.build(schemaType(schemaTypeCode).getDefaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(groupSchemaVO.getCode(), groupVOBuilder);
		groupDataProvider = new RecordVODataProvider(Arrays.asList(groupSchemaVO), voBuilders, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getGroupsQuery();
			}
		};
		view.setDataProvider(groupDataProvider);

		view.reloadContent();
	}

	public List<String> getAutocompleteSuggestions(String text) {
		List<String> suggestions = new ArrayList<>();
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			int minInputLength = 3;
			int maxResults = 10;
			String[] excludedRequests = new String[0];
			String collection = view.getCollection();

			SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory);
			ThesaurusManager thesaurusManager = modelLayerFactory.getThesaurusManager();
			ThesaurusService thesaurusService = thesaurusManager.get(collection);

			List<String> statsSuggestions = searchEventServices
					.getMostPopularQueriesAutocomplete(text, maxResults, excludedRequests);
			suggestions.addAll(statsSuggestions);
			if (thesaurusService != null && statsSuggestions.size() < maxResults) {
				int thesaurusMaxResults = maxResults - statsSuggestions.size();
				List<String> thesaurusSuggestions = thesaurusService
						.suggestSimpleSearch(text, view.getSessionContext().getCurrentLocale(), minInputLength,
								thesaurusMaxResults, true, searchEventServices);
				suggestions.addAll(thesaurusSuggestions);
			}
		}
		return suggestions;
	}

	public int getAutocompleteBufferSize() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		return modelLayerFactory.getSystemConfigs().getAutocompleteSize();
	}

	public void changeGroupDataProvider(String value) {
		MetadataSchemaVO groupsSchemaVO = schemaVOBuilder.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(groupsSchemaVO.getCode(), groupVOBuilder);
		groupDataProvider = new RecordVODataProvider(Arrays.asList(groupsSchemaVO), voBuilders, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				String groupSearchExpression = filterSolrOperators(value);
				if (!StringUtils.isBlank(value)) {
					LogicalSearchQuery logicalSearchQuery;
					logicalSearchQuery = getGroupsQuery().setFreeTextQuery(groupSearchExpression);
					if (!"*".equals(value)) {
						logicalSearchQuery.setHighlighting(true);
					}
					return logicalSearchQuery;
				} else {
					return getGroupsQuery();
				}
			}
		};
		view.setDataProvider(groupDataProvider);
		view.reloadContent();
	}
}
