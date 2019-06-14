package com.constellio.app.ui.pages.search.savedSearch;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Collections.singletonList;

public class SavedSearchPresenter extends SingleSchemaBasePresenter<SavedSearchView> {

	public SavedSearchPresenter(SavedSearchView view) {
		super(view, SavedSearch.DEFAULT_SCHEMA);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public RecordVODataProvider getUserSearchesDataProvider() {
		return new RecordVODataProvider(savedSearchSchemaVO(), new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {

			@Override
			public LogicalSearchQuery getQuery() {
				MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);
				return new LogicalSearchQuery(from(schema)
						.where(schema.getMetadata(SavedSearch.USER)).isEqualTo(getCurrentUser())
						.andWhere(schema.getMetadata(SavedSearch.PUBLIC)).isFalse()
						.andWhere(schema.getMetadata(SavedSearch.TEMPORARY)).isFalseOrNull())
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getPublicSearchesDataProvider() {
		return new RecordVODataProvider(savedSearchSchemaVO(), new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {

			@Override
			public LogicalSearchQuery getQuery() {
				MetadataSchema schema = schema(SavedSearch.DEFAULT_SCHEMA);

				User currentUser = getCurrentUser();

				LogicalSearchCondition isCreator = where(schema.get(SavedSearch.USER)).isEqualTo(currentUser);
				LogicalSearchCondition isSharedUser =
						where(schema.get(SavedSearch.SHARED_USERS)).isContaining(singletonList(currentUser.getId()));
				LogicalSearchCondition isSharedGroup =
						where(schema.get(SavedSearch.SHARED_GROUPS)).isIn(currentUser.getUserGroupsOrEmpty());
				LogicalSearchCondition isNotRestrictedAndNotCreator =
						where(schema.get(SavedSearch.RESTRICTED)).isFalseOrNull()
								.andWhere(schema.get(SavedSearch.USER)).isNull();

				return new LogicalSearchQuery(from(schema)
						.whereAllConditions(
								where(schema.getMetadata(SavedSearch.PUBLIC)).isTrue(),
								where(schema.getMetadata(SavedSearch.TEMPORARY)).isFalseOrNull(),
								anyConditions(isCreator, isSharedUser, isSharedGroup, isNotRestrictedAndNotCreator)))
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	private MetadataSchemaVO savedSearchSchemaVO() {
		return new MetadataSchemaToVOBuilder()
				.build(schema(SavedSearch.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
	}

	public void searchModificationRequested(String id, String newTitle, boolean publicSearch, List<String> sharedGroups,
											List<String> sharedUsers) {
		SavedSearch savedSearch = new SavedSearch(getRecord(id), types());
		if (newTitle != null) {
			savedSearch.setTitle(newTitle);
		}
		savedSearch.setPublic(publicSearch);
		savedSearch.setSharedGroups(!sharedGroups.isEmpty() ? sharedGroups : null);
		savedSearch.setSharedUsers(!sharedUsers.isEmpty() ? sharedUsers : null);
		addOrUpdate(savedSearch.getWrappedRecord());
		view.navigate().to().listSavedSearches();
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		delete(getRecord(recordVO.getId()));
		view.navigate().to().listSavedSearches();
	}

	public void searchButtonClicked(RecordVO recordVO) {
		switch (recordVO.<String>get(SavedSearch.SEARCH_TYPE)) {
			case SimpleSearchView.SEARCH_TYPE:
				view.navigate().to().simpleSearchReplay(recordVO.getId());
				break;
			case AdvancedSearchView.SEARCH_TYPE:
				view.navigate().to().advancedSearchReplay(recordVO.getId());
				break;
		}
	}

	public boolean hasUserAcessToDeletePublicSearches() {
		return getCurrentUser().has(CorePermissions.DELETE_PUBLIC_SAVED_SEARCH).globally();
	}

	public boolean isSavedSearchEditable(boolean publicSearch) {
		if (!publicSearch) {
			return true;
		}
		return hasUserAccessToModifyPublicSearches();
	}

	private boolean hasUserAccessToModifyPublicSearches() {
		return getCurrentUser().has(CorePermissions.MODIFY_PUBLIC_SAVED_SEARCH).globally();
	}
}
