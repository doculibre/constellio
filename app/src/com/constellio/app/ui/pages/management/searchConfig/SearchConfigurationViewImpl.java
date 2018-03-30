package com.constellio.app.ui.pages.management.searchConfig;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Collections;
import java.util.List;

import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

public class SearchConfigurationViewImpl extends BaseViewImpl implements SearchConfigurationView {
	
	private SearchConfigurationPresenter presenter;
	
	public SearchConfigurationViewImpl() {
		this.presenter = new SearchConfigurationPresenter(this);
	}
	
	@Override
	protected String getTitle() {
		return $("SearchConfigurationView.title");
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();
		CssLayout layout = new CustomCssLayout();
		layout.addComponents(createSearchBoostByMetadatasButton(), createSeachBoostByQueryButton(), createFacetsManagementButton());

		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			layout.addComponent(createCapsulesManagementButton());
			if (presenter.canManageCorrectorExclusions()) {
				layout.addComponent(createSpellCheckerExclusionsManagementButton());
			}
			layout.addComponent(createThesaurusConfigurationButton());
		}

        verticalLayout.addComponent(layout);

        if (presenter.isSystemSectionTitleVisible()) {
            Label systemSectionTitle = new Label($("SearchConfigurationView.systemSectionTitle"));
            systemSectionTitle.addStyleName(ValoTheme.LABEL_H1);
            verticalLayout.addComponent(systemSectionTitle);
        }  

        if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
        	CssLayout layoutSystemPilot = new CustomCssLayout();
            layoutSystemPilot.addComponents(createSynonymsManagementButton(), createElevationManagementButton());
            verticalLayout.setSpacing(true);
            verticalLayout.addComponent(layoutSystemPilot);
        }

		return verticalLayout;
	}

	private Button createSearchBoostByMetadatasButton() {
		return presenter.isBoostMetadataButtonVisible() ?
			createLink($("AdminView.searchBoostByMetadata"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.searchBoostByMetadatasButtonClicked();
				}
			}, "config/boost-metadata-search") :
			null;
	}

	private Button createSynonymsManagementButton() {
		return presenter.isSynonymsManagementButtonVisible() ?
			createLink($("AdminView.synonymsManagement"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.synonymsManagementButtonClicked();
				}
			}, "config/synonyms") :
			null;
	}

	private Button createElevationManagementButton() {
		return presenter.isElevationManagementButtonVisible() ?
			createLink($("AdminView.elevationManagement"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.elevationManagementButtonClicked();
				}
			}, "config/search-exclusions") :
			null;
	}

	private Button createThesaurusConfigurationButton(){
		return presenter.isThesaurusConfigurationButtonVisible() ?
			createLink($("AdminView.thesaurus"),new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.thesaurusConfigurationButtonClicked();
				}
			}, "config/thesaurus") : 
			null;
	}

	private Button createSpellCheckerExclusionsManagementButton() {
		return presenter.isSpellCheckerExclusionsManagementButtonVisible() ?
			createLink($("AdminView.excluded"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.spellCheckerExclusionsManagementButtonClicked();
				}
			}, "config/search-suggestions-exlusions") :
			null;
	}

	private Button createSeachBoostByQueryButton() {
		return presenter.isSearchBoostByQueryButtonVisible() ?
			createLink($("AdminView.searchBoostByQuery"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.searchBoostByQueryButtonClicked();
				}
			}, "config/boost-text-search") :
			null;
	}

	private Button createFacetsManagementButton() {
		return presenter.isFacetsManagementButtonVisible() ?
			createLink($("perm.core.manageFacets"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.facetsManagementButtonClicked();
				}
			}, "config/funnel") :
			null;
	}

	private Button createCapsulesManagementButton() {
		return presenter.isCapsulesManagementButtonVisible() ?
			createLink($("ListCapsuleView.title"), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					presenter.capsulesManagementButtonClicked();
				}
			}, "config/capsules") :
			null;
	}

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return getSearchConfigurationBreadCrumbTrail(this, null);
    }

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
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
						return $("SearchConfigurationView.title");
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
