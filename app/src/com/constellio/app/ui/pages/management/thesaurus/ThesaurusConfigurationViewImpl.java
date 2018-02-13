package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import net.didion.jwnl.data.Exc;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationViewImpl extends BaseViewImpl implements ThesaurusConfigurationView {
    WindowButton refusalWindow;
    Button uploadButton;
    Button downloadButton;
    
    TextArea deniedTerms;
    Button saveButton;
    Button cancelButton;

    ThesaurusConfigurationPresenter thesaurusConfigurationPresenter;

    public ThesaurusConfigurationViewImpl() {
        thesaurusConfigurationPresenter = new ThesaurusConfigurationPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();
        if(thesaurusConfigurationPresenter.haveThesaurusConfiguration()) {
            downloadButton = new Button($("ThesaurusConfigurationView.button.download"));

            downloadButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {

                }
            });

            downloadButton.addStyleName(ValoTheme.BUTTON_LINK);

            verticalLayout.addComponent(downloadButton);
        }
        return verticalLayout;
    }

    public void showError(Exception exeption) {
        this.showErrorMessage($("ThesaurusConfigurationView.saveUnexpectedError"));
    }

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
        List<Button> actionMenuButtons = super.buildActionMenuButtons(event);
        WindowButton.WindowConfiguration configuration = new WindowButton.WindowConfiguration(true, true, "50%", "750px");
        refusalWindow = new WindowButton($("ThesaurusConfigurationView.button.termsrefusal"),
                $("ThesaurusConfigurationView.button.termsrefusal", configuration)) {
            @Override
            protected Component buildWindowContent() {
                VerticalLayout verticalLayout = new VerticalLayout();

                verticalLayout.setSizeFull();
                deniedTerms = new TextArea();
                deniedTerms.setValue(thesaurusConfigurationPresenter.getDenidedTerms());
                deniedTerms.setSizeFull();
                saveButton = new Button($("save"));

                saveButton.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        try {
                            thesaurusConfigurationPresenter.saveDenidedTerms(deniedTerms.getValue());
                        } catch (RecordServicesException e) {
                            showError(e);
                            e.printStackTrace();
                        }
                        refusalWindow.getWindow().close();
                    }
                });

                cancelButton = new Button($("cancel"));

                cancelButton.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        refusalWindow.getWindow().close();
                    }
                });

                verticalLayout.addComponent(deniedTerms);
                verticalLayout.setExpandRatio(deniedTerms, 1);
                HorizontalLayout horizontalLayout = new HorizontalLayout();
                horizontalLayout.addComponent(saveButton);
                horizontalLayout.addComponent(cancelButton);
                horizontalLayout.setHeight("50px");
                verticalLayout.addComponent(horizontalLayout);
                verticalLayout.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);

                return verticalLayout;
            }
        };

        

        uploadButton = new Button($("ThesaurusConfigurationView.button.upload"));

        uploadButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

            }
        });


        actionMenuButtons.add(refusalWindow);
        actionMenuButtons.add(uploadButton);



        return actionMenuButtons;
    }

    @Override
    protected Component buildActionMenu(ViewChangeListener.ViewChangeEvent event) {
        return super.buildActionMenu(event);
    }
}
