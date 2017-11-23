package com.constellio.app.ui.pages.exclusion;

import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DeleteExclusionsImpl extends BaseViewImpl implements DeleteExclusionsView {
    BaseTable baseTable;
    ButtonsContainer buttonsContainer;
    IndexedContainer indexedContainer;

    public static final String SPACE_4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String SPACES_8 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String INFORMATION = "information";

    Map<Integer, Object> containerMapperWithElevationObject = new HashMap<>();
    DeleteExclusionsPresenter deleteExclusionsPresenter;

    public DeleteExclusionsImpl() {
        this.deleteExclusionsPresenter = new DeleteExclusionsPresenter(this);
    }

    @Override
    public String getCaption() {
        return $("DeleteExclusionsImpl.title");
    }

    protected boolean hasPageAccess(String params, User user) {
        return false;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();

        indexedContainer = new IndexedContainer();
        buttonsContainer = new ButtonsContainer(indexedContainer);

        indexedContainer.addContainerProperty(INFORMATION, Label.class, null);

        baseTable = new BaseTable(DeleteExclusionsImpl.class.getName());

        baseTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
        baseTable.setColumnHeader(INFORMATION, $("DeleteExclusionsImpl.excluded"));

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
                                ExclusionCollectionVO exclusion = (ExclusionCollectionVO) containerMapperWithElevationObject.get(itemId);
                                deleteExclusionsPresenter.deleteExclusion(exclusion);
                                navigateTo().deleteExclusionsImpl();
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
                displayButton.setCaption($("DeleteExclusionImpl.delete"));
                return displayButton;
            }
        });

        for(ExclusionCollectionVO exclusionCollectionVO : deleteExclusionsPresenter.getExcluded()) {
            baseTable.setColumnExpandRatio(INFORMATION, 1);
            baseTable.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID,  60);

            addExclusionTo(exclusionCollectionVO);
        }


        verticalLayout.addComponent(baseTable);
        verticalLayout.setSizeFull();

        return verticalLayout;
    }

    private void addExclusionTo(ExclusionCollectionVO exclusion) {
        Object addedItemNumber = baseTable.addItem();

        containerMapperWithElevationObject.put((Integer)addedItemNumber, exclusion);
        Label label = new Label(exclusion.getExclusion());
        label.setContentMode(ContentMode.HTML);
        indexedContainer.getContainerProperty(addedItemNumber, INFORMATION).setValue(label);
    }
}
