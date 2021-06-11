package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
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

public class UserSecurityManagementPresenter extends SingleSchemaBasePresenter<SecurityManagement> {

	RecordVODataProvider userDataProvider;
	Boolean allItemsSelected = false;
	Boolean allItemsDeselected = false;
	private boolean active = true;
	private Set<String> allRecordIds;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private Map<String, String> paramsMap;

	private UserToVOBuilder userVOBuilder;

	private transient ConstellioEIMConfigs eimConfigs;

	private Set<String> selectedRecordIds = new HashSet<>();
	
	private String searchFilter;
	
	private boolean viewAssembled = false;

	public UserSecurityManagementPresenter(SecurityManagement view, RecordVO recordVO) {
		super(view, User.DEFAULT_SCHEMA);
		initTransientObjects();
		if (recordVO != null) {
			forParams(recordVO.getId());
		}
	}

	private void initTransientObjects() {
		userVOBuilder = new UserToVOBuilder();
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public int getUserCount() {
		return this.userDataProvider.size();
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
			List<String> allRecordIdsList = searchServices().searchRecordIds(getUsersQuery());
			allRecordIds = new HashSet<>(allRecordIdsList);
		}

		return allRecordIds;
	}

	private LogicalSearchQuery getUsersQuery() {
		MetadataSchemaType userSchemaType = schemaType(User.SCHEMA_TYPE);

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(userSchemaType)
				.where(Schemas.COLLECTION).isEqualTo(collection);

		if (this.active) {
			condition = condition.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		} else {
			condition = condition.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		}

		query.setCondition(condition);
		query.sortAsc(Schemas.TITLE);

		return query;
	}

	protected String filterSolrOperators(String expression) {
		String userSearchExpression = expression;

		if (StringUtils.isNotBlank(userSearchExpression) && userSearchExpression.startsWith("\"") && userSearchExpression.endsWith("\"")) {
			userSearchExpression = ClientUtils.escapeQueryChars(userSearchExpression);
			userSearchExpression = "\"" + userSearchExpression + "\"";
		}

		return userSearchExpression;
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
	
	private void refreshTable() {
		MetadataSchemaVO usersSchemaVO = schemaVOBuilder.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(usersSchemaVO.getCode(), userVOBuilder);
		userDataProvider = new RecordVODataProvider(Arrays.asList(usersSchemaVO), voBuilders, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				if (!StringUtils.isBlank(searchFilter)) {
					String userSearchExpression = filterSolrOperators(searchFilter);
					if (userSearchExpression.split(" ").length < 2) {
						userSearchExpression = userSearchExpression + " OR " + userSearchExpression + "*";
					}

					LogicalSearchQuery logicalSearchQuery =
							getUsersQuery().setFreeTextQuery(userSearchExpression).setPreferAnalyzedFields(true);

					if (!"*".equals(userSearchExpression)) {
						logicalSearchQuery.setHighlighting(true);
					}
					return logicalSearchQuery;
				} else {
					return getUsersQuery();
				}
			}

			@Override
			public boolean isSearchCache() {
				return eimConfigs.isOnlySummaryMetadatasDisplayedInTables();
			}
		};
		view.setDataProvider(userDataProvider);
		if (viewAssembled) {
			view.reloadContent();
		}
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	private String getParameters(UserCredentialVO entity) {
		Map<String, Object> params = new HashMap<>();
		params.put("username", entity.getUsername());
		return ParamUtils.addParams(NavigatorConfigurationService.COLLECTION_USER_LIST, params);
	}

	public void displayButtonClicked(UserVO entity) {
		//TODO
		//Temporary until UserVO is switched
		UserCredentialVO userEntity = (new UserCredentialToVOBuilder()).build(userServices().getUserInfos(entity.getUsername()));

		view.navigate().to().displayUserCredentialFromSecurityPage(userEntity.getUsername());
	}
}
