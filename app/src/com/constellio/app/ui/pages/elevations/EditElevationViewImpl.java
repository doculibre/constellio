package com.constellio.app.ui.pages.elevations;

import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.search.Elevations;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.util.Iterator;
import java.util.List;

public class EditElevationViewImpl extends BaseViewImpl implements EditElevationView {
    EditElevationPresenter editElevationPresenter;

    BaseTable baseTable;
    ButtonsContainer buttonsContainer;
    IndexedContainer indexedContainer;

    public static final String INFORMATION = "information";
    public static final String EXCLUDED = "-Excluded";
    public static final String RAISED = "-Raised";
    public static final String SPACE_4 = "    ";
    public static final String SPACES_8 = "        ";


    public EditElevationViewImpl() {
        editElevationPresenter = new EditElevationPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();

        indexedContainer = new IndexedContainer();
        buttonsContainer = new ButtonsContainer(indexedContainer);

        indexedContainer.addContainerProperty(INFORMATION, String.class, null);

        baseTable = new BaseTable(EditElevationViewImpl.class.getName());

        baseTable.setSizeFull();
        baseTable.setContainerDataSource(buttonsContainer);

        buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                return new DisplayButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        Integer index = (Integer) itemId;

//                        RecordVO entity = buttonsContainer.getRecordVO(index);


//                        presenter.displayButtonClicked(entity);
                    }
                };
            }
        });

        for(String query : editElevationPresenter.getAllQuery()) {
            Item queryItem = baseTable.addItem(query);

            Item excludedLineItem = baseTable.addItem(query + EXCLUDED);

            baseTable.getContainerProperty(queryItem, INFORMATION).setValue(query);

            baseTable.getContainerProperty(excludedLineItem, INFORMATION).setValue(SPACE_4 + excludedLineItem);

            List<Elevations.QueryElevation.DocElevation> docExcluded = editElevationPresenter.getExclusions(query);

            addItemsToTable(query, docExcluded);

            List<Elevations.QueryElevation.DocElevation> docElevations = editElevationPresenter.getElevations(query);

            addItemsToTable(query, docElevations);
        }

        verticalLayout.addComponent(baseTable);
        verticalLayout.setSizeFull();

        return verticalLayout;
    }

    private void addItemsToTable(String query, List<Elevations.QueryElevation.DocElevation> docExcluded) {
        for(Iterator<Elevations.QueryElevation.DocElevation> iterator = docExcluded.iterator(); iterator.hasNext();) {
            Elevations.QueryElevation.DocElevation docElevation = iterator.next();
            docElevation.setQuery(query);
            Item lineItem = baseTable.addItem(docElevation.getId());

            baseTable.getContainerProperty(lineItem, INFORMATION).setValue(docElevation.getId());
        }
    }


}
