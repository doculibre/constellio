package com.constellio.app.ui.pages.management.schemas.type;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CannotDeleteWindow extends VerticalLayout{

    private static final String RECORD_ID = "recordId";
    private static final String RECORD_TITLE = "recordTitle";
    private static final String IS_IN_TRASH_QUESTION = "isInTrashQuestion";

    private Label cannotDeleteLabel;
    private Label recordAccessLabel;
//    private Button desactivateButton;
    private Button okButton;
    private Table recordsTable;
    private String recordAccessMessage;

    public CannotDeleteWindow(String cannotDeleteMessage, List<Record> records, boolean buildRecordsTable) {
        setSpacing(true);
        setWidth("90%");
        addStyleName("CannotDeleteWindow");
        cannotDeleteLabel = new Label(cannotDeleteMessage);
        cannotDeleteLabel.addStyleName(ValoTheme.LABEL_H2);
        cannotDeleteLabel.setVisible(cannotDeleteMessage != null);

        okButton = new Button("Ok");
        okButton.addStyleName("OkButton");
        okButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        okButton.setEnabled(true);
        okButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                closeWindow();
            }
        });

//        desactivateButton= new Button("Désactiver");
//        desactivateButton.addStyleName("desactivateButton");
//        desactivateButton.setEnabled(true);
//        desactivateButton.addClickListener(new Button.ClickListener() {
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                closeWindow();
//            }
//        });

        if(buildRecordsTable){
            if (recordAccessMessage != null){
                recordAccessLabel = new Label(recordAccessMessage);
                addComponents(cannotDeleteLabel,recordsTable,recordAccessLabel,okButton);
            }else{
                recordsTable = buildRecodsTable(records);
                addComponents(cannotDeleteLabel,recordsTable,okButton);
            }
        }else{
            addComponents(cannotDeleteLabel,okButton);
        }

//        HorizontalLayout buttons = new HorizontalLayout(okButton, desactivateButton);
//        buttons.setSpacing(true);
    }

    private Table buildRecodsTable(List<Record> records){
        Table table = new Table();
        table.setWidth("90%");
        table.addContainerProperty($(RECORD_ID), String.class,null);
        table.addContainerProperty($(RECORD_TITLE), String.class, null);
        table.addContainerProperty($(IS_IN_TRASH_QUESTION), String.class,null);

        int numberOfRecords = 0;
        for(Record record : records){
                    table.addItem(new String[]{record.getId(),record.getTitle(), record.isActive() ? $("isActive") : $("isInactive")}, numberOfRecords);
                    numberOfRecords++;
//                    recordAccessMessage = $("recordAcessMessage");
            }

        table.setPageLength(table.size());

        return table;
    }

    public Window openWindow() {
        Window warningWindow = new BaseWindow($("warning"), this);
        warningWindow.center();
        warningWindow.setModal(true);
        UI.getCurrent().addWindow(warningWindow);
        return warningWindow;
    }

    public void closeWindow() {
        Window window = (Window) getParent();
        window.close();
    }

}
