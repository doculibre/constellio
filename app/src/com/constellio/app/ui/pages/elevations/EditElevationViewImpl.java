package com.constellio.app.ui.pages.elevations;

import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.model.services.search.Elevations;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditElevationViewImpl extends BaseViewImpl implements EditElevationView {
    EditElevationPresenter editElevationPresenter;

    BaseTable baseTable;
    ButtonsContainer buttonsContainer;
    IndexedContainer indexedContainer;

    public static final String INFORMATION = "information";
    public static final String EXCLUDED = "-Excluded";
    public static final String RAISED = "-Raised";
    public static final String SPACE_4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String SPACES_8 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    Map<Integer, Object> containerMapperWithElevationObject = new HashMap<>();

    public EditElevationViewImpl() {
        editElevationPresenter = new EditElevationPresenter(this);
    }

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
    }

    @Override
    protected String getTitle() {
        return $("EditElevationViewImpl.title");
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();

        indexedContainer = new IndexedContainer();
        buttonsContainer = new ButtonsContainer(indexedContainer);

        indexedContainer.addContainerProperty(INFORMATION, Label.class, null);

        baseTable = new BaseTable(EditElevationViewImpl.class.getName());
        baseTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
        baseTable.setColumnHeader(INFORMATION, $("EditElevationViewImpl.query"));
        baseTable.setContainerDataSource(buttonsContainer);
        baseTable.setSizeFull();
        baseTable.setSortEnabled(false);
        buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                        DisplayButton displayButton = new DisplayButton() {
                            @Override
                            protected void buttonClick(ClickEvent event) {
                                ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create($("EditElevationViewImpl.d.confirmation.dialog.title"),
                                        $("EditElevationViewImpl.d.confirmation"), $("OK"), $("cancel"), null);

                                confirmDialog.getOkButton().addClickListener(new ClickListener() {
                                    @Override
                                    public void buttonClick(ClickEvent event) {
                                        Integer index = (Integer) itemId;

                                        Object containedObject = containerMapperWithElevationObject.get(index);
                                        if(containedObject instanceof Elevations.QueryElevation.DocElevation){
                                            Elevations.QueryElevation.DocElevation docElevation = (Elevations.QueryElevation.DocElevation) containedObject;
                                            editElevationPresenter.deleteDocElevation(docElevation);
                                        } else if (containedObject instanceof String) {
                                            String containedObjectString = (String) containedObject;
                                            String informationValue = ((Label) baseTable.getContainerProperty(index, INFORMATION).getValue()).getValue();
                                            if(informationValue.equals(SPACE_4 + $("EditElevationViewImpl.exclud"))) {
                                                editElevationPresenter.deleteAllExclution(containedObjectString);
                                            }
                                            else if (informationValue.equals(SPACE_4 + $("EditElevationViewImpl.raise"))) {
                                                editElevationPresenter.deleteAllElevation(containedObjectString);
                                            } else {
                                                editElevationPresenter.deleteQuery(containedObjectString);
                                            }
                                        }
                                        navigateTo().editElevation();
                                    }
                                });

                                confirmDialog.show(UI.getCurrent(), new ConfirmDialog.Listener() {
                                    @Override
                                    public void onClose(ConfirmDialog dialog) {
                                    }
                                }, true);
                            }
                };
                displayButton.setIcon(new ThemeResource("images/icons/actions/delete.png"));

                return displayButton;
            }
        });

        for(String query : editElevationPresenter.getAllQuery()) {

            addOneItemToTableAndSetValue(query, query);
            baseTable.setColumnExpandRatio(INFORMATION, 1);
            baseTable.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID,  60);

            List<Elevations.QueryElevation.DocElevation> docExcluded = editElevationPresenter.getExclusions(query);
            if(docExcluded.size() > 0) {
                addOneItemToTableAndSetValue(query, SPACE_4 + $("EditElevationViewImpl.exclud"));
                addItemsToTable(query, docExcluded);
            }

            List<Elevations.QueryElevation.DocElevation> docElevations = editElevationPresenter.getElevations(query);
            if(docElevations.size() > 0) {
                addOneItemToTableAndSetValue(query, SPACE_4 + $("EditElevationViewImpl.raise"));
                addItemsToTable(query, docElevations);
            }
        }

        verticalLayout.addComponent(baseTable);
        verticalLayout.setSizeFull();

        return verticalLayout;
    }

    private void addOneItemToTableAndSetValue(Object valueToAdd, String value) {
        Object addedItemNumber = baseTable.addItem();

        containerMapperWithElevationObject.put((Integer)addedItemNumber, valueToAdd);
        Label label = new Label(value);
        label.setContentMode(ContentMode.HTML);
        indexedContainer.getContainerProperty(addedItemNumber, INFORMATION).setValue(label);
    }

    private void addItemsToTable(String query, List<Elevations.QueryElevation.DocElevation> docExcluded) {
        for(Iterator<Elevations.QueryElevation.DocElevation> iterator = docExcluded.iterator(); iterator.hasNext();) {
            Elevations.QueryElevation.DocElevation docElevation = iterator.next();
            docElevation.setQuery(query);
            addOneItemToTableAndSetValue(docElevation, SPACES_8 + editElevationPresenter.getRecordTitle(docElevation.getId()));
        }
    }
}
