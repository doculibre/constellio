package com.constellio.app.ui.pages.statistique;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class StatisticsViewImpl extends BaseViewImpl implements StatisticsView {
    public StatisticsViewImpl() {

    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        ComboBox comboBox = new ComboBox();

        comboBox.setCaption("Type de statistiques : ");

        comboBox.addItem("Salut");



        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(comboBox);


        return verticalLayout;
    }
}
