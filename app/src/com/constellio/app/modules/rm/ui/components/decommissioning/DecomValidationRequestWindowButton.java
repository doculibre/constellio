package com.constellio.app.modules.rm.ui.components.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

public class DecomValidationRequestWindowButton extends WindowButton {
    private final DecommissioningListPresenter presenter;

    private ListAddRemoveRecordLookupField users;
    private TextArea comments;
    private CheckBox checkBox;

    public DecomValidationRequestWindowButton(DecommissioningListPresenter presenter) {
        super(i18n.$("DecomAskForValidationWindowButton.buttonCaption"),
                i18n.$("DecomAskForValidationWindowButton.windowCaption"));
        this.presenter = presenter;
    }

    @Override
    protected Component buildWindowContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
        users.setCaption($("DecomAskForValidationWindowButton.usersCaption"));
        users.setRequired(true);
        //users.setRequiredError($("DecomAskForValidationWindowButton.error.users"));
        layout.addComponent(users);
        checkBox = new CheckBox($("DecomAskForValidationWindowButton.addCommentToDecomlistCaption"));
        layout.addComponent(checkBox);
        comments = new TextArea($("DecomAskForValidationWindowButton.commentsCaption"));
        comments.setSizeFull();
        layout.addComponent(comments);

        BaseButton sendButton = new BaseButton($("DecomAskForValidationWindowButton.okButton")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                if (presenter.validationRequested(users.getValue(), comments.getValue(), checkBox.getValue())) {
                    getWindow().close();
                }
            }
        };
        sendButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponent(sendButton);
        BaseButton cancelButton = new BaseButton($("DecomAskForValidationWindowButton.cancelButton")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                getWindow().close();
            }
        };
        buttonsLayout.addComponent(cancelButton);
        buttonsLayout.setSpacing(true);
        layout.addComponent(buttonsLayout);
        layout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);

        return layout;
    }
}
