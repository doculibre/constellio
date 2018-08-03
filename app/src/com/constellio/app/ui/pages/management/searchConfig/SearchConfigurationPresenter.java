package com.constellio.app.ui.pages.management.searchConfig;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class SearchConfigurationPresenter extends BasePresenter<SearchConfigurationView> {

	public SearchConfigurationPresenter(SearchConfigurationView view) {
		super(view);
	}

	public SearchConfigurationPresenter(SearchConfigurationView view, ConstellioFactories constellioFactories,
										SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.hasAny(
				CorePermissions.MANAGE_SEARCH_BOOST,
				CorePermissions.MANAGE_VALUELIST,
				CorePermissions.ACCESS_SEARCH_CAPSULE,
				CorePermissions.MANAGE_FACETS,
				CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT,
				CorePermissions.MANAGE_SYNONYMS,
				CorePermissions.DELETE_CORRECTION_SUGGESTION
		).globally();
	}

	public boolean canManageCorrectorExclusions() {
		User user = getCurrentUser();
		return Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			   && user.has(CorePermissions.DELETE_CORRECTION_SUGGESTION).globally();
	}

	public boolean isSystemSectionTitleVisible() {
		User user = getCurrentUser();
		return Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			   && user.hasAny(CorePermissions.MANAGE_SYNONYMS, CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT).globally();
	}

	public boolean isBoostMetadataButtonVisible() {
		User user = getCurrentUser();
		return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally();
	}

	public boolean isSynonymsManagementButtonVisible() {
		User user = getCurrentUser();
		return user.has(CorePermissions.MANAGE_SYNONYMS).globally();
	}

	public boolean isElevationManagementButtonVisible() {
		User user = getCurrentUser();
		return Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			   && user.has(CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT).globally();
	}

	public boolean isSearchBoostByQueryButtonVisible() {
		User user = getCurrentUser();
		return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally();
	}

	public boolean isFacetsManagementButtonVisible() {
		User user = getCurrentUser();
		return user.has(CorePermissions.MANAGE_FACETS).globally();
	}

	public boolean isCapsulesManagementButtonVisible() {
		User user = getCurrentUser();
		return Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			   && user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally();
	}

	public boolean isThesaurusConfigurationButtonVisible() {
		User user = getCurrentUser();
		return Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
			   && user.has(CorePermissions.MANAGE_THESAURUS).globally();
	}

	public boolean isSpellCheckerExclusionsManagementButtonVisible() {
		return true;
	}

	public void searchBoostByMetadatasButtonClicked() {
		view.navigate().to().searchBoostByMetadatas();
	}

	public void synonymsManagementButtonClicked() {
		view.navigate().to().displaySynonyms();
	}

	public void elevationManagementButtonClicked() {
		view.navigate().to().editElevation();
	}

	public void thesaurusConfigurationButtonClicked() {
		view.navigate().to().thesaurusConfiguration();
	}

	public void spellCheckerExclusionsManagementButtonClicked() {
		view.navigate().to().deleteExclusions();
	}

	public void searchBoostByQueryButtonClicked() {
		view.navigate().to().searchBoostByQuerys();
	}

	public void facetsManagementButtonClicked() {
		view.navigate().to().listFacetConfiguration();
	}

	public void capsulesManagementButtonClicked() {
		view.navigate().to().listCapsule();
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	public boolean isStatisticsButtonVisible() {
		return Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled();
	}

	public void statisticsButtonClicked() {
		view.navigate().to().statistics();

	}

	public List<NavigationItem> getCollectionItems() {
		return navigationConfig().getNavigation(SearchConfigurationView.COLLECTION_SECTION);
	}

	public List<NavigationItem> getSystemItems() {
		return navigationConfig().getNavigation(SearchConfigurationView.SYSTEM_SECTION);
	}

}
