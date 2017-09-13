package com.constellio.app.ui.pages.synonyms;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.management.TemporaryRecord.ListTemporaryRecordView;
import com.constellio.model.entities.records.wrappers.User;

public class ViewSynonymsPresenter extends BasePresenter<ViewSynonymsView> {
    public ViewSynonymsPresenter(ViewSynonymsView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
