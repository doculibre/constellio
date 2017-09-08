package com.constellio.app.ui.pages.management.searchConfig;

import com.constellio.app.services.migrations.CoreNavigationConfiguration;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchConfigurationViewImpl extends BaseViewImpl implements AdminViewGroup {
    private User user;

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        CssLayout layout = new CssLayout();
        user = getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
        layout.addComponents(createBoostMetadataButton(), createBoostRequestButton(), createFacetteButton());
        return layout;
    }

    private Button createBoostMetadataButton(){
        return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ? createLink($("AdminView.searchBoostByMetadata"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().searchBoostByMetadatas();
            }
        }, CoreNavigationConfiguration.SEARCH_BOOST_BY_METADATA_ICON) : null;
    }

    private Button createBoostRequestButton() {
        return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ? createLink($("AdminView.searchBoostByQuery"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().searchBoostByQuerys();
            }
        }, CoreNavigationConfiguration.SEARCH_BOOST_BY_QUERY_ICON) : null;
    }

    private Button createFacetteButton(){
        return user.has(CorePermissions.MANAGE_VALUELIST).globally() ? createLink($("perm.core.manageFacets"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().listFacetConfiguration();
            }
        }, CoreNavigationConfiguration.FACET_CONFIGURATION_ICON) : null;
    }
}
