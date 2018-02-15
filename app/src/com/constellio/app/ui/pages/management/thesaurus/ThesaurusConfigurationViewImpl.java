package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.thesaurus.ThesaurusBuilder;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.easyuploads.UploadField;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationViewImpl extends BaseViewImpl implements ThesaurusConfigurationView {
    WindowButton refusalWindow;
    Button uploadButton;
    Button downloadButton;
    BaseDisplay baseDisplay;

    TextArea deniedTerms;
    Button saveButton;
    Button cancelButton;
    ModelLayerFactory modelLayerFactory;

    ThesaurusConfigurationPresenter thesaurusConfigurationPresenter;

    private FormBean formBean = new FormBean();

    @PropertyId("string")
    private UploadField upload;

    public ThesaurusConfigurationViewImpl() {
        thesaurusConfigurationPresenter = new ThesaurusConfigurationPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();
        if(thesaurusConfigurationPresenter.haveThesaurusConfiguration()) {
            List<BaseDisplay.CaptionAndComponent> listCaptionAndComponent = new ArrayList<>();

            BaseDisplay.CaptionAndComponent captionAndComponent = new BaseDisplay
                    .CaptionAndComponent(new Label($("ThesaurusConfigurationView.about")), new Label(thesaurusConfigurationPresenter.getAbout()));
            listCaptionAndComponent.add(captionAndComponent);

            captionAndComponent = new BaseDisplay
                    .CaptionAndComponent(new Label($("ThesaurusConfigurationView.title")), new Label(thesaurusConfigurationPresenter.getTitle()));
            listCaptionAndComponent.add(captionAndComponent);

            captionAndComponent = new BaseDisplay
                    .CaptionAndComponent(new Label($("ThesaurusConfigurationView.description")), new Label(thesaurusConfigurationPresenter.getDescription()));
            listCaptionAndComponent.add(captionAndComponent);

            captionAndComponent = new BaseDisplay
                    .CaptionAndComponent(new Label($("ThesaurusConfigurationView.date")), new Label(thesaurusConfigurationPresenter.getDate()));
            listCaptionAndComponent.add(captionAndComponent);

            captionAndComponent = new BaseDisplay
                    .CaptionAndComponent(new Label($("ThesaurusConfigurationView.creator")), new Label(thesaurusConfigurationPresenter.getCreator()));
            listCaptionAndComponent.add(captionAndComponent);

            baseDisplay = new BaseDisplay(listCaptionAndComponent);
            verticalLayout.addComponent(baseDisplay);

            downloadButton = new Button($("ThesaurusConfigurationView.button.download"));

            downloadButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    thesaurusConfigurationPresenter.downloadThesaurusFile();
                }
            });


            downloadButton.addStyleName(ValoTheme.BUTTON_LINK);

            verticalLayout.addComponent(downloadButton);
            verticalLayout.setSpacing(true);

        } else {
            Label noThesaurusAvalible = new Label($("ThesaurusConfigurationView.noThesaurusAvalible") + "<br /> ");
            noThesaurusAvalible.setContentMode(ContentMode.HTML);
            verticalLayout.addComponent(noThesaurusAvalible);
        }

        verticalLayout.addComponent(baseFormForFileUpload());

        return verticalLayout;
    }

    public BaseForm baseFormForFileUpload() {
        upload = new UploadField();
        upload.setButtonCaption($("ThesaurusConfigurationView.upload"));
        upload.setCaption($("ThesaurusConfigurationView.File"));
        BaseForm baseForm = new BaseForm(formBean, this, upload) {
            @Override
            protected void saveButtonClick(Object viewObject) throws ValidationException {
                thesaurusConfigurationPresenter.saveNewThesaurusFile(upload.getContentAsStream());
            }

            @Override
            protected void cancelButtonClick(Object viewObject) {
            }

            @Override
            protected boolean isCancelButtonVisible() {
                return false;
            }

            @Override
            protected String getSaveButtonCaption() {
                return $("ThesaurusConfigurationView.UploadThesaurusFile");
            }
        };

        return baseForm;
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

    public class FormBean {
        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

    }

}
