package com.constellio.app.ui.pages.synonyms;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DisplaySynonymsViewImpl extends BaseViewImpl implements EditSynonymsView {

    DisplaySynonymsPresenter presenter;
    TextArea textArea;
    EditButton editButton;

    public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";

    public static final String SAVE_BUTTON = "base-form-save";

    public static final String CANCEL_BUTTON = "base-form_cancel";


    public DisplaySynonymsViewImpl() {
        presenter = new DisplaySynonymsPresenter(this);
    }

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();
        this.textArea = new TextArea();
        this.textArea.setValue(presenter.getSynonmsAsOneString());
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
                presenter.editButtonClick();
            }
        };

        editButton.addStyleName(ValoTheme.BUTTON_LINK);

        actionMenuButtons.add(editButton);

        return actionMenuButtons;
    }

    @Override
    protected String getTitle() {
        return $("DisplaySynonymsView.title");
    }

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}
    
}
