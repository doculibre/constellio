package com.constellio.app.ui.pages.management.searchConfig;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;
import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Collections;
import java.util.List;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SearchConfigurationViewImpl extends BaseViewImpl implements AdminViewGroup {
	private User user;

	@Override
	protected String getTitle() {
		return $("SearchConfigurationViewImpl.title");
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();
		CssLayout layout = new CustomCssLayout();
		user = getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices()
				.getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
		layout.addComponents(createBoostMetadataButton(), createBoostRequestButton(), createFacetteButton());

		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			layout.addComponent(createCapsuleButton());
			layout.addComponent(createCorrectorExclusion());
		}



        verticalLayout.addComponent(layout);

        if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()
				&& user.hasAny(CorePermissions.MANAGE_SYNONYMS, CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT).globally()) {
            Label systemSectionTitle = new Label($("SearchConfigurationViewImpl.systemSectionTitle"));
            systemSectionTitle.addStyleName(ValoTheme.LABEL_H1);
            verticalLayout.addComponent(systemSectionTitle);
        }  

        if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
        	CssLayout layoutSystemPilot = new CustomCssLayout();
            layoutSystemPilot.addComponents(createSynonymsButton(), createElevationManagementButton());
            verticalLayout.setSpacing(true);
            verticalLayout.addComponent(layoutSystemPilot);
        }

		return verticalLayout;
	}

	private Button createBoostMetadataButton() {
		return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ?
				createLink($("AdminView.searchBoostByMetadata"), new Button.ClickListener() {

					@Override
					public void buttonClick(Button.ClickEvent event) {
						navigate().to().searchBoostByMetadatas();
					}
				}, "config/boost-metadata-search") :
				null;
	}

	private Button createSynonymsButton() {
		return user.has(CorePermissions.MANAGE_SYNONYMS).globally() ?
				createLink($("AdminView.synonymesManagement"), new Button.ClickListener() {

					@Override
					public void buttonClick(Button.ClickEvent event) {
						navigate().to().displaySynonyms();
					}
				}, "config/synonyms") :
				null;
	}

	private Button createElevationManagementButton() {
		return user.has(CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT).globally() ?
				createLink($("AdminView.elevationManagement"), new Button.ClickListener() {

					@Override
					public void buttonClick(Button.ClickEvent event) {
						navigate().to().editElevation();
					}
				}, "config/search-exclusions") :
				null;

	}

	private Button createCorrectorExclusion() {
		return createLink($("AdminView.excluded"), new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				navigate().to().deleteExclusionsImpl();
			}
		}, "config/search-suggestions-exlusions");
	}

	private Button createBoostRequestButton() {
		return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ?
				createLink($("AdminView.searchBoostByQuery"), new Button.ClickListener() {

					@Override
					public void buttonClick(Button.ClickEvent event) {
						navigate().to().searchBoostByQuerys();
					}
				}, "config/boost-text-search") :
				null;
	}

	private Button createFacetteButton() {
		return user.has(CorePermissions.MANAGE_FACETS).globally() ?
				createLink($("perm.core.manageFacets"), new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						navigate().to().listFacetConfiguration();
					}
				}, "config/funnel") :
				null;
	}

	private Button createCapsuleButton() {
		return user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally() ?
				createLink($("ListCapsuleViewImpl.title"), new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						navigate().to().listCapsule();
					}
				}, "config/capsules") :
				null;
	}

	public static TitleBreadcrumbTrail getSearchConfigurationBreadCrumbTrail(BaseView view, String title) {
		return new TitleBreadcrumbTrail(view, title) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermeiateItems() {
				return Collections.singletonList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("SearchConfigurationViewImpl.title");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().searchConfiguration();
					}
				});
			}
		};
	}
}
