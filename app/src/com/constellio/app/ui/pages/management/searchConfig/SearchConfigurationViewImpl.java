package com.constellio.app.ui.pages.management.searchConfig;

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
    protected String getTitle() {
        return $("SearchConfigurationViewImpl.title");
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        CssLayout layout = new CustomCssLayout();
        user = getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
        layout.addComponents(createBoostMetadataButton(), createBoostRequestButton(), createFacetteButton(), createCapsuleButton());
        return layout;
    }

    private Button createBoostMetadataButton(){
        return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ? createLink($("AdminView.searchBoostByMetadata"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().searchBoostByMetadatas();
            }
        }, "config/boost-metadata-search") : null;
    }

    private Button createBoostRequestButton() {
        return user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally() ? createLink($("AdminView.searchBoostByQuery"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().searchBoostByQuerys();
            }
        }, "config/boost-text-search") : null;
    }

    private Button createFacetteButton(){
        return user.has(CorePermissions.MANAGE_VALUELIST).globally() ? createLink($("perm.core.manageFacets"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigate().to().listFacetConfiguration();
            }
        }, "config/funnel") : null;
    }

    private Button createCapsuleButton(){
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
            for(Component component : components) {
                if(component != null) {
                    super.addComponent(component);
                }
            }
        }
    }
}
