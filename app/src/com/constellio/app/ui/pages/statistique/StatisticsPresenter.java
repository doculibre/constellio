package com.constellio.app.ui.pages.statistique;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class StatisticsPresenter extends BasePresenter<StatisticsView> {

    public StatisticsPresenter(StatisticsView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }


}
