package com.constellio.app.ui.pages.synonyms;

import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.Button.ClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditSynonymsViewImpl extends BaseViewImpl implements EditSynonymsView {

    EditSynonymsPresenter editSynonymsPresenter;
    TextArea textArea;
    EditButton editButton;

    public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";

    public static final String SAVE_BUTTON = "base-form-save";

    public static final String CANCEL_BUTTON = "base-form_cancel";

    protected HorizontalLayout buttonsLayout;
    protected Button saveButton;
    protected Button cancelButton;

    public EditSynonymsViewImpl() {
        editSynonymsPresenter = new EditSynonymsPresenter(this);

    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();
        this.textArea = new TextArea();
        this.textArea.setValue(editSynonymsPresenter.getSynonmsAsOneString());
        this.textArea.setHeight("600px");
        this.textArea.setWidth("95%");
        buttonsLayout = new HorizontalLayout();
        buttonsLayout.addStyleName(BUTTONS_LAYOUT);
        buttonsLayout.setSpacing(true);

        saveButton = new Button(getSaveButtonCaption());
        saveButton.addStyleName(SAVE_BUTTON);
        saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editSynonymsPresenter.saveButtonClicked(textArea.getValue());
            }
        });

        cancelButton = new Button(getCancelButtonCaption());
        cancelButton.addStyleName(CANCEL_BUTTON);
        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editSynonymsPresenter.cancelButtonClicked();
            }
        });


        buttonsLayout.addComponents(saveButton, cancelButton);
        verticalLayout.setSpacing(true);
        verticalLayout.setHeight("100%");
        verticalLayout.setSizeFull();
        verticalLayout.addComponent(textArea);
        verticalLayout.addComponent(buttonsLayout);

        return verticalLayout;
    }


    protected String getSaveButtonCaption() {
        return $("save");
    }

    protected String getCancelButtonCaption() {
        return $("cancel");
    }

    @Override
    protected String getTitle() {
        return $("EditViewSynonymsViewImpl");
    }
}
