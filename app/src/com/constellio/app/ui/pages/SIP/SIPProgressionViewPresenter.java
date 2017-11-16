package com.constellio.app.ui.pages.SIP;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class SIPProgressionViewPresenter extends BasePresenter<SIPProgressionViewImpl> {

    public SIPProgressionViewPresenter(SIPProgressionViewImpl view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return false;
    }
}
