package com.constellio.app.ui.pages.statistique;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.synonyms.EditSynonymsView;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.calcite.schema.Statistic;

public class StatisticsPresenter extends BasePresenter<StatisticsViews> {

    public StatisticsPresenter(StatisticsViews view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return false;
    }
}
