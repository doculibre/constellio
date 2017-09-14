package com.constellio.app.ui.pages.synonyms;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchConfigurationsManager;

import java.util.Arrays;
import java.util.List;

public class DisplaySynonymsPresenter extends BasePresenter<EditSynonymsView> {
    List<String> synonyms;
    SearchConfigurationsManager searchConfigurationsManager;

    public DisplaySynonymsPresenter(EditSynonymsView view) {
        super(view);
        searchConfigurationsManager = modelLayerFactory.getSearchConfigurationsManager();
        this.synonyms = searchConfigurationsManager.getSynonyms();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }


    public String getSynonmsAsOneString() {
        StringBuilder stringBuilder = new StringBuilder();

        for(String string : synonyms) {
            stringBuilder.append(string).append("\n");
        }

        return stringBuilder.toString();
    }

    public void editButtonClick() {
        view.navigate().to(CoreViews.class).editSynonyms();
    }
}
