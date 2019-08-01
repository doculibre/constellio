package com.constellio.app.ui.pages.management.searchConfig;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		VerticalLayout mainLayout = new VerticalLayout();

		CssLayout collectionSectionLayout = new CustomCssLayout();
		ConstellioEIMConfigs eimConfigs = new ConstellioEIMConfigs(getConstellioFactories().getAppLayerFactory().getModelLayerFactory());
		if (eimConfigs.isLearnToRankFeatureActivated()) {
			collectionSectionLayout.addComponents(createStatisticsButton(), createSearchBoostByMetadatasButton(), createSeachBoostByQueryButton(),
					createSolrFeatureRequestButton(), createFacetsManagementButton());
		} else {
			collectionSectionLayout.addComponents(createStatisticsButton(), createSearchBoostByMetadatasButton(), createSeachBoostByQueryButton(), createFacetsManagementButton());
		}

		//layout.addComponents(createStatisticsButton(), createSearchBoostByMetadatasButton(), createSeachBoostByQueryButton(), createFacetsManagementButton());

		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			collectionSectionLayout.addComponent(createCapsulesManagementButton());
			if (presenter.canManageCorrectorExclusions()) {
				collectionSectionLayout.addComponent(createSpellCheckerExclusionsManagementButton());
			}
			if (presenter.isThesaurusConfigurationButtonVisible()) {
				collectionSectionLayout.addComponent(createThesaurusConfigurationButton());
			}
			collectionSectionLayout.addComponents(createSynonymsManagementButton());
			collectionSectionLayout.addComponents(createElevationManagementButton());
		}

		List<NavigationItem> collectionItems = presenter.getCollectionItems();
		for (NavigationItem navigationItem : collectionItems) {
			buildButton(collectionSectionLayout, navigationItem);
		}

		mainLayout.addComponent(collectionSectionLayout);

		List<NavigationItem> systemItems = presenter.getSystemItems();
		if (presenter.isSystemSectionTitleVisible() && !systemItems.isEmpty()) {
			Label systemSectionTitle = new Label($("SearchConfigurationView.systemSectionTitle"));
			systemSectionTitle.addStyleName(ValoTheme.LABEL_H1);
			mainLayout.addComponent(systemSectionTitle);

			if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
				CssLayout systemSection = new CustomCssLayout();
				mainLayout.setSpacing(true);
				mainLayout.addComponent(systemSection);

				for (NavigationItem navigationItem : systemItems) {
					buildButton(systemSection, navigationItem);
				}
			}
		}

		return mainLayout;
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

	private Button createThesaurusConfigurationButton() {
		return presenter.isThesaurusConfigurationButtonVisible() ?
			   createLink($("AdminView.thesaurus"), new Button.ClickListener() {
				   @Override
				   public void buttonClick(Button.ClickEvent event) {
					   presenter.thesaurusConfigurationButtonClicked();
				   }
			   }, "config/thesaurus") :
			   null;
	}

	private Button createSolrFeatureRequestButton() {
		User user = getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices()
				.getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
		return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ?
			   createLink($("AdminView.solrFeature"), new Button.ClickListener() {

				   @Override
				   public void buttonClick(Button.ClickEvent event) {
					   navigate().to().solrFeatures();
				   }
			   }, "config/boost-text-search") :
			   null;
	}

	//	private Button createBoostRequestButton() {
	//		return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ?
	//				createLink($("AdminView.searchBoostByQuery"), new Button.ClickListener() {
	//
	//					@Override
	//					public void buttonClick(Button.ClickEvent event) {
	//						navigate().to().searchBoostByQuerys();
	//					}
	//				}, "config/boost-text-search") :
	//				null;
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

	private Button createStatisticsButton() {
		return presenter.isStatisticsButtonVisible() ?
			   createLink($("StatisticsView.viewTitle"), new Button.ClickListener() {
				   @Override
				   public void buttonClick(ClickEvent event) {
					   presenter.statisticsButtonClicked();
				   }
			   }, "config/chart_column") :
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
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
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

	private void buildButton(Layout layout, final NavigationItem item) {
		Button button = new Button($("SearchConfigurationView." + item.getCode()), new ThemeResource(item.getIcon()));
		button.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		button.addStyleName(item.getCode());
		ComponentState state = presenter.getStateFor(item);
		button.setEnabled(state.isEnabled());
		button.setVisible(state.isVisible());
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				item.activate(navigate());
			}
		});
		layout.addComponent(button);
	}
}
