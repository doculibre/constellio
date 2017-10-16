package com.constellio.app.ui.pages.management.searchConfig;

import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
        user = getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
        layout.addComponents(createBoostMetadataButton(), createBoostRequestButton(), createFacetteButton(), createCapsuleButton());

        verticalLayout.addComponent(layout);

        if(getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getSystemConfigurationsManager()
                .getValue(ConstellioEIMConfigs.ADVANCED_SEARCH_CONFIGS).toString().equalsIgnoreCase("true")) {
            Label systemSectionTitle = new Label($("SearchConfigurationViewImpl.systemSectionTitle"));
            systemSectionTitle.addStyleName(ValoTheme.LABEL_H1);
            verticalLayout.addComponent(systemSectionTitle);

            CssLayout layoutSystemPilot = new CustomCssLayout();
            layoutSystemPilot.addComponents(createSynonymsButton(), createElevationManagementButton());
            verticalLayout.setSpacing(true);
            verticalLayout.addComponent(layoutSystemPilot);
        }

        return verticalLayout;
    }

    private Button createBoostMetadataButton() {
        return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ? createLink($("AdminView.searchBoostByMetadata"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().searchBoostByMetadatas();
            }
        }, "config/boost-metadata-search") : null;
    }

    private Button createSynonymsButton() {
        return  createLink($("AdminView.synonymesManagement"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().displaySynonyms();
            }
        }, "config/synonyms");
    }

    private Button createElevationManagementButton() {
        return  createLink($("AdminView.elevationManagement"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().editElevation();
            }
        }, "config/search-exclusions");

    }

    private Button createBoostRequestButton() {
        return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ? createLink($("AdminView.searchBoostByQuery"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().searchBoostByQuerys();
            }
        }, "config/boost-text-search") : null;
    }

    private Button createFacetteButton() {
        return user.has(CorePermissions.MANAGE_VALUELIST).globally() ? createLink($("perm.core.manageFacets"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().listFacetConfiguration();
            }
        }, "config/funnel") : null;
    }

    private Button createCapsuleButton() {
        return user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally() ? createLink($("ListCapsuleViewImpl.title"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().listCapsule();
            }
        }, "config/capsules") : null;
    }

    private class CustomCssLayout extends CssLayout {
        @Override
        public void addComponents(Component... components) {
            for (Component component : components) {
                if (component != null) {
                    super.addComponent(component);
                }
            }
        }
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
