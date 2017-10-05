package com.constellio.app.ui.pages.synonyms;

import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplaySynonymsViewImpl extends BaseViewImpl implements EditSynonymsView {

    DisplaySynonymsPresenter displaySynonymsPresenter;
    TextArea textArea;
    EditButton editButton;

    public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";

    public static final String SAVE_BUTTON = "base-form-save";

    public static final String CANCEL_BUTTON = "base-form_cancel";


    public DisplaySynonymsViewImpl() {
        displaySynonymsPresenter = new DisplaySynonymsPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();
        this.textArea = new TextArea();
        this.textArea.setValue(displaySynonymsPresenter.getSynonmsAsOneString());
        this.textArea.setHeight("600px");
        this.textArea.setWidth("95%");

        textArea.setEnabled(false);
        verticalLayout.setSpacing(true);
        verticalLayout.setHeight("100%");
        verticalLayout.setSizeFull();
        verticalLayout.addComponent(textArea);

        return verticalLayout;
    }

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
        List<Button> actionMenuButtons = super.buildActionMenuButtons(event);
        editButton = new EditButton( $("edit")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                displaySynonymsPresenter.editButtonClick();
            }
        };

        editButton.addStyleName(ValoTheme.BUTTON_LINK);

        actionMenuButtons.add(editButton);

        return actionMenuButtons;
    }

    @Override
    protected String getTitle() {
        return $("DisplaySynonymsViewImpl");
    }
}
