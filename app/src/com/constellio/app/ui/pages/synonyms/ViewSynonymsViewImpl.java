package com.constellio.app.ui.pages.synonyms;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

public class ViewSynonymsViewImpl extends BaseViewImpl implements  ViewSynonymsView {

    ViewSynonymsPresenter viewSynonymsPresenter;

    public ViewSynonymsViewImpl() {
        viewSynonymsPresenter = new ViewSynonymsPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        return null;
    }
}
