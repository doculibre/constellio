package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationViewImpl extends BaseViewImpl implements ThesaurusConfigurationView {
    WindowButton refusalWindow;
    DownloadContentVersionLink downloadContentVersionLink;
    BaseDisplay baseDisplay;

    TabSheet tabSheet;

    TextArea deniedTerms;
    Button saveButton;
    Button cancelButton;
    Button deleteButton;

    VerticalLayout verticalLayoutSkosFile;
    BaseForm baseFormForUpload;

    ThesaurusConfigurationPresenter thesaurusConfigurationPresenter;

    private FormBean formBean = new FormBean();

    @PropertyId("file")
    private BaseUploadField upload;

    public ThesaurusConfigurationViewImpl() {
        thesaurusConfigurationPresenter = new ThesaurusConfigurationPresenter(this);
    }


    @Override
    protected String getTitle() {
        return $("ThesaurusConfigurationViewImpl");
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout mainLayout = new VerticalLayout();
        Label title = new Label("<h3 style=\"Font-Weight: Bold;\">" + $("ThesaurusConfigurationView.thesaurus") + "</h3>");
        title.setContentMode(ContentMode.HTML);
        mainLayout.addComponent(title);
        verticalLayoutSkosFile = new VerticalLayout();
        tabSheet = new TabSheet();
        baseFormForUpload = baseFormForFileUpload();
        if(thesaurusConfigurationPresenter.haveThesaurusConfiguration()) {
            thesaurusConfigurationPage(verticalLayoutSkosFile);
            downloadContentVersionLink = new DownloadContentVersionLink(thesaurusConfigurationPresenter.getContentVersionForDownloadLink());
            downloadContentVersionLink.setCaption($("ThesaurusConfigurationView.button.download"));
            verticalLayoutSkosFile.addComponent(downloadContentVersionLink);
            deleteButton = new Button($("ThesaurusConfigurationView.removeSkosFile"));
            deleteButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    thesaurusConfigurationPresenter.deleteButtonClick();
                }
            });
            verticalLayoutSkosFile.addComponent(deleteButton);
        } else {
            noThesaurusAvalibleState(verticalLayoutSkosFile);
        }

        verticalLayoutSkosFile.addComponent(baseFormForUpload);

        tabSheet.addTab(verticalLayoutSkosFile, $("ThesaurusConfigurationViewImpl.thesaurusFileAndInfo"));

        VerticalLayout verticalLayoutRefusalTerms = new VerticalLayout();
        Label refusedTermsTitle = new Label("<h3 style=\"Font-Weight: Bold;\">" + $("ThesaurusConfigurationView.termsrefusal") + "</h3>");
        refusedTermsTitle.setContentMode(ContentMode.HTML);
        verticalLayoutRefusalTerms.addComponent(refusedTermsTitle);
        verticalLayoutRefusalTerms.setSizeFull();
        deniedTerms = new TextArea();
        deniedTerms.setValue(thesaurusConfigurationPresenter.getDenidedTerms());
        deniedTerms.setWidth("100%");
        deniedTerms.setHeight("300px");

        saveButton = new Button($("save"));

        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    thesaurusConfigurationPresenter.saveDenidedTerms(deniedTerms.getValue());
                } catch (RecordServicesException e) {
                    showError(e);
                    e.printStackTrace();
                }
            }
        });

        cancelButton = new Button($("cancel"));

        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                deniedTerms.setValue(thesaurusConfigurationPresenter.getDenidedTerms());
            }
        });

        verticalLayoutRefusalTerms.addComponent(deniedTerms);
        verticalLayoutRefusalTerms.setExpandRatio(deniedTerms, 1);
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponent(saveButton);
        buttonLayout.addComponent(cancelButton);
        buttonLayout.setHeight("50px");
        verticalLayoutRefusalTerms.addComponent(buttonLayout);
        verticalLayoutRefusalTerms.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);

        tabSheet.addTab(verticalLayoutRefusalTerms, $("ThesaurusConfigurationView.termsrefusal"));
        mainLayout.addComponent(tabSheet);

        enableSKOSSaveButton(false);

        return mainLayout;
    }

    private void noThesaurusAvalibleState(VerticalLayout verticalLayoutSkosFile) {
        Label noThesaurusAvalible = new Label($("ThesaurusConfigurationView.noThesaurusAvalible") + "<br /> ");
        noThesaurusAvalible.setContentMode(ContentMode.HTML);
        verticalLayoutSkosFile.addComponent(noThesaurusAvalible);
    }

    public void enableSKOSSaveButton(boolean enabled) {
        baseFormForUpload.getSaveButton().setEnabled(enabled);
    }

    public void toNoThesaurusAvalible() {
        VerticalLayout noThesaurusAvalibleVerticalLayout = new VerticalLayout();
        noThesaurusAvalibleState(noThesaurusAvalibleVerticalLayout);

        noThesaurusAvalibleVerticalLayout.addComponent(baseFormForUpload);

        tabSheet.replaceComponent(verticalLayoutSkosFile, noThesaurusAvalibleVerticalLayout);
        this.verticalLayoutSkosFile = noThesaurusAvalibleVerticalLayout;
    }

    private void thesaurusConfigurationPage(VerticalLayout verticalLayoutSkosFile) {
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
        verticalLayoutSkosFile.addComponent(baseDisplay);

        verticalLayoutSkosFile.setSpacing(true);
    }

    public void unloadDescriptionsField() {
        VerticalLayout newVerticalLayoutSkosFile = new VerticalLayout();
        noThesaurusAvalibleState(newVerticalLayoutSkosFile);
    }

    public void loadDescriptionFieldsWithFileValue(){
        VerticalLayout newVerticalLayoutSkosFile = new VerticalLayout();
        thesaurusConfigurationPage(newVerticalLayoutSkosFile);
        newVerticalLayoutSkosFile.addComponent(baseFormForUpload);
        tabSheet.replaceComponent(verticalLayoutSkosFile, newVerticalLayoutSkosFile);

        verticalLayoutSkosFile = newVerticalLayoutSkosFile;

    }

    public BaseForm baseFormForFileUpload() {
        upload = new BaseUploadField();
        upload.setUploadButtonCaption($("ThesaurusConfigurationView.upload"));
        upload.setMultiValue(false);
        upload.setCaption($("ThesaurusConfigurationView.File"));
        upload.setImmediate(true);

        upload.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                TempFileUpload value = (TempFileUpload) upload.getValue();
                thesaurusConfigurationPresenter.valueChangeInFileSelector(value);
            }
        });

        upload.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                // there to not get a NullPointerException
            }
        });
        BaseForm baseForm = new BaseForm(formBean, this, upload) {
            @Override
            protected void saveButtonClick(Object viewObject) throws ValidationException {
               thesaurusConfigurationPresenter.saveNewThesaurusFile((TempFileUpload) upload.getValue());
            }

            @Override
            protected void cancelButtonClick(Object viewObject) {
                navigateTo().searchConfiguration();
            }
        };

        return baseForm;
    }

    public void showError(Exception exeption) {
        this.showErrorMessage($("ThesaurusConfigurationView.saveUnexpectedError"));
    }

    @Override
    protected Component buildActionMenu(ViewChangeListener.ViewChangeEvent event) {
        return super.buildActionMenu(event);
    }

    @Override
    public void removeAllTheSelectedFile() {
        upload.setValue(null);
    }

    public class FormBean {
        private TempFileUpload file;

        public TempFileUpload getFile() {
            return file;
        }

        public void setFile(TempFileUpload file) {
            this.file = file;
        }

    }

}
