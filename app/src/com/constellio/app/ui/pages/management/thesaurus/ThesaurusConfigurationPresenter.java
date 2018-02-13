package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class ThesaurusConfigurationPresenter extends BasePresenter<ThesaurusConfigurationView> {
    public ThesaurusConfigurationPresenter(ThesaurusConfigurationView view) {
        super(view);
    }


    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
