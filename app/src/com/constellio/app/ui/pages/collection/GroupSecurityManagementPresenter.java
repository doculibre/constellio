package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.ui.builders.GroupToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.GroupVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
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

	private GroupToVOBuilder groupVOBuilder;

	private transient ConstellioEIMConfigs eimConfigs;

	private Set<String> selectedRecordIds = new HashSet<>();
	
	private String searchFilter;
	
	private boolean viewAssembled = false;;

	public GroupSecurityManagementPresenter(SecurityManagement view, RecordVO recordVO) {
		super(view, Group.DEFAULT_SCHEMA);
		initTransientObjects();
		if (recordVO != null) {
			forParams(recordVO.getId());
		}
	}

	private void initTransientObjects() {
		groupVOBuilder = new GroupToVOBuilder();
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public int getGroupCount() {
		return this.groupDataProvider.size();
	}

	public void viewAssembled() {
		this.viewAssembled = true;
		view.reloadContent();
	}

	public void forParams(String params) {
		refreshTable();
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

	public void activeSelectionChanged(boolean active) {
		this.active = active;
		refreshTable();
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
		MetadataSchema groupDefaultSchema = schema(Group.DEFAULT_SCHEMA);

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(groupDefaultSchema)
				.where(Schemas.COLLECTION).isEqualTo(collection);

		if (this.active) {
			condition = condition.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		} else {
			condition = condition.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		}

		if (StringUtils.isBlank(searchFilter)) {
			condition = condition.andWhere(groupDefaultSchema.getMetadata(Group.PARENT)).isNull();
		}

		query.setCondition(condition);
		query.sortAsc(Schemas.TITLE);

		return query;
	}

	protected String filterSolrOperators(String expression) {
		String groupSearchExpression = expression;

		if (StringUtils.isNotBlank(groupSearchExpression) && groupSearchExpression.startsWith("\"") && groupSearchExpression.endsWith("\"")) {
			groupSearchExpression = ClientUtils.escapeQueryChars(groupSearchExpression);
			groupSearchExpression = "\"" + groupSearchExpression + "\"";
		}

		return groupSearchExpression;
	}

	public void clearSearchRequested() {
		this.searchFilter = null;
		refreshTable();
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

	public void searchRequested(String value) {
		this.searchFilter = value;
		refreshTable();
	}

	private String getGroupNiceTitle(RecordVO vo) {
		String path = getGroupPath(vo.getId());
		if (path.equals(vo.getTitle())) {
			return vo.getTitle();
		}
		return vo.getTitle() + " (" + path + ")";
	}

	private String getGroupPath(String groupId) {
		Group group = coreSchemas().getGroup(groupId);
		if (group.getParent() != null) {
			return getGroupPath(group.getParent()) + "\\" + group.getTitle();
		} else {
			return group.getTitle();
		}
	}
	
	private void refreshTable() {
		MetadataSchemaVO groupsSchemaVO = schemaVOBuilder.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(groupsSchemaVO.getCode(), groupVOBuilder);
		groupDataProvider = new RecordVODataProvider(Arrays.asList(groupsSchemaVO), voBuilders, modelLayerFactory, view.getSessionContext()) {
			@Override
			public RecordVO getRecordVO(int index) {
				RecordVO vo = super.getRecordVO(index);
				vo.setTitle(getGroupNiceTitle(vo));
				return vo;
			}

			public List<RecordVO> listRecordVOs(int startIndex, int numberOfItems) {
				List<RecordVO> vos = super.listRecordVOs(startIndex, numberOfItems);
				for (RecordVO vo : vos) {
					vo.setTitle(getGroupNiceTitle(vo));
				}
				return vos;
			}

			@Override
			public LogicalSearchQuery getQuery() {
				if (StringUtils.isNotBlank(searchFilter)) {
					String groupSearchExpression = filterSolrOperators(searchFilter);
					if (groupSearchExpression.split(" ").length < 2) {
						groupSearchExpression = groupSearchExpression + " OR " + groupSearchExpression + "*";
					}

					LogicalSearchQuery logicalSearchQuery =
							getGroupsQuery().setFreeTextQuery(groupSearchExpression).setPreferAnalyzedFields(true);

					if (!"*".equals(groupSearchExpression)) {
						logicalSearchQuery.setHighlighting(true);
					}
					return logicalSearchQuery;
				} else {
					return getGroupsQuery();
				}
			}

			@Override
			public boolean isSearchCache() {
				return eimConfigs.isOnlySummaryMetadatasDisplayedInTables();
			}
		};
		view.setDataProvider(groupDataProvider);
		if (viewAssembled) {
			view.reloadContent();
		}
	}

	public void displayButtonClicked(GroupVO entity) {
		view.navigate().to().displayGlobalGroup(entity.getCode());
	}
}
