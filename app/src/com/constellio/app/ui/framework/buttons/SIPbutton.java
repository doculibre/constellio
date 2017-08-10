package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SIPbutton extends WindowButton {
    private List<RecordVO> objectList = new ArrayList<>();
    private CheckBox deleteCheckBox;

    public SIPbutton(String caption, String windowCaption) {
        super(caption, windowCaption);
    }

    @Override
    protected Component buildWindowContent() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(buildDeleteItemCheckbox());
        mainLayout.addComponent(buildButtonComponent());
        return mainLayout;
    }

    public void addAllObject(RecordVO... objects) {
        objectList.addAll(asList(objects));
    }

    private HorizontalLayout buildDeleteItemCheckbox(){
        HorizontalLayout layout = new HorizontalLayout();
        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));
        layout.addComponents(deleteCheckBox);
        return layout;
    }

    private HorizontalLayout buildButtonComponent() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button cancelButton = new BaseButton($("cancel")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                getWindow().close();
            }
        };
        Button continueButton = new BaseButton($("ok")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                continueButtonClicked();
            }
        };
        buttonLayout.addComponents(cancelButton, continueButton);
        return buttonLayout;
    }

    public void continueButtonClicked(){

    }
}
