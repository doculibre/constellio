package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.thesaurus.SkosConcept;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationViewImpl extends BaseViewImpl implements ThesaurusConfigurationView {


	private FormBean formBean = new FormBean();

	@PropertyId("file")
	private BaseUploadField uploadSkosField;

	@PropertyId("rejectedTerms")
	private TextArea rejectedTermsField;

	private BaseDisplay baseDisplay;
	private DownloadContentVersionLink downloadContentVersionLink;

	private TabSheet tabSheet;

	private Button deleteSkosFileButton;

	private VerticalLayout skosFormLayout;
	private BaseForm<FormBean> skosForm;
	private BaseForm<FormBean> rejectedTermsForm;

	private ThesaurusConfigurationPresenter presenter;

	public ThesaurusConfigurationViewImpl() {
		presenter = new ThesaurusConfigurationPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ThesaurusConfigurationView.viewTitle");
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();

		tabSheet = new TabSheet();

		skosFormLayout = new VerticalLayout();
		skosFormLayout.setSizeFull();
		skosFormLayout.setSpacing(true);

		skosForm = buildSkosForm();

		if (presenter.isThesaurusConfiguration()) {
			thesaurusConfigurationPage(skosFormLayout);

			HorizontalLayout downloadableFileLayout = new I18NHorizontalLayout();
			downloadableFileLayout.setWidthUndefined();
			downloadableFileLayout.setSpacing(true);

			downloadContentVersionLink = new DownloadContentVersionLink(presenter.getContentVersionForDownloadLink());
			downloadContentVersionLink.setWidthUndefined();
			downloadContentVersionLink.setCaption($("ThesaurusConfigurationView.button.download"));
			downloadableFileLayout.addComponent(downloadContentVersionLink);

			deleteSkosFileButton = new DeleteButton($("ThesaurusConfigurationView.removeSkosFile")) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteSkosFileButtonClicked();
				}
			};
			deleteSkosFileButton.removeStyleName(ValoTheme.BUTTON_BORDERLESS);

			downloadableFileLayout.addComponent(deleteSkosFileButton);
			skosFormLayout.addComponent(downloadableFileLayout);

		} else {
			noThesaurusAvailableState(skosFormLayout);
		}

		skosFormLayout.addComponent(skosForm);

		tabSheet.addTab(skosFormLayout, $("ThesaurusConfigurationView.thesaurusFileAndInfo"));

		VerticalLayout rejectedTermsLayout = new VerticalLayout();
		rejectedTermsLayout.setSizeFull();
		rejectedTermsLayout.setSpacing(true);

		rejectedTermsForm = buildRejectedTermsForm();

		rejectedTermsLayout.addComponent(rejectedTermsForm);

		tabSheet.addTab(rejectedTermsLayout, $("ThesaurusConfigurationView.rejectedTerms"));
		mainLayout.addComponent(tabSheet);

		Component statsSheetContent = buildStatsSheetContent();
		tabSheet.addTab(statsSheetContent, $("ThesaurusConfigurationView.stats"));

		setSKOSSaveButtonEnabled(false);

		return mainLayout;
	}

	private Component buildStatsSheetContent() {
		int nbDocumentsWithAtLeastOneConcept = presenter.getDocumentsWithAConcept();
		int nbConceptsUsedAtLeastOnce = presenter.getUsedConcepts();
		int nbDocumentsWithoutAConcept = presenter.getDocumentsWithoutAConcept();
		final List<SkosConcept> unusedConcepts = presenter.getUnusedConcepts();

		Label nbDocumentsWithAtLeastOneConceptLabel = new Label($("ThesaurusConfigurationView.stats.nbDocumentsWithAtLeastOneConcept"));
		Label nbDocumentsWithAtLeastOneConceptComponent = new Label(MessageFormat.format("{0}", nbDocumentsWithAtLeastOneConcept));

		Label nbConceptsUsedAtLeastOnceLabel = new Label($("ThesaurusConfigurationView.stats.nbConceptsUsedAtLeastOnce"));
		Label nbConceptsUsedAtLeastOnceComponent = new Label(MessageFormat.format("{0}", nbConceptsUsedAtLeastOnce));

		Label nbDocumentsWithoutAConceptLabel = new Label($("ThesaurusConfigurationView.stats.nbDocumentsWithoutAConcept"));
		Label nbDocumentsWithoutAConceptComponent = new Label(MessageFormat.format("{0}", nbDocumentsWithoutAConcept));

		Label mostFrequentlyUsedConceptsLabel = new Label($("ThesaurusConfigurationView.stats.mostFrequentlyUsedConcepts"));
		DownloadLink mostFrequentlyUsedConceptsComponent = new DownloadLink(new StreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return presenter.getMostUsedConceptsInputStream(presenter.getMostUsedConcepts());
			}

		}, "mostFrequentlyUsedConcepts.csv"), $("download"));

		Label conceptsNotUsedLabel = new Label($("ThesaurusConfigurationView.stats.conceptsNotUsed", MessageFormat.format("{0}", unusedConcepts.size())));
		DownloadLink conceptsNotUsedComponent = new DownloadLink(new StreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return presenter.getUnusedConceptsInputStream(unusedConcepts);
			}

		}, "unusedConcepts.csv"), $("download"));

		List<CaptionAndComponent> captionsAndDisplayComponents = new ArrayList<>();
		captionsAndDisplayComponents.add(new CaptionAndComponent(nbDocumentsWithAtLeastOneConceptLabel, nbDocumentsWithAtLeastOneConceptComponent));
		captionsAndDisplayComponents.add(new CaptionAndComponent(nbConceptsUsedAtLeastOnceLabel, nbConceptsUsedAtLeastOnceComponent));
		captionsAndDisplayComponents.add(new CaptionAndComponent(nbDocumentsWithoutAConceptLabel, nbDocumentsWithoutAConceptComponent));
		captionsAndDisplayComponents.add(new CaptionAndComponent(mostFrequentlyUsedConceptsLabel, mostFrequentlyUsedConceptsComponent));
		captionsAndDisplayComponents.add(new CaptionAndComponent(conceptsNotUsedLabel, conceptsNotUsedComponent));

		BaseDisplay statsSheetContent = new BaseDisplay(captionsAndDisplayComponents);
		statsSheetContent.setSizeFull();
		return statsSheetContent;
	}

	private void noThesaurusAvailableState(VerticalLayout verticalLayoutSkosFile) {
		Label noThesaurusAvalible = new Label($("ThesaurusConfigurationView.noThesaurusAvailable") + "<br /> ");
		noThesaurusAvalible.setContentMode(ContentMode.HTML);
		verticalLayoutSkosFile.addComponent(noThesaurusAvalible);
	}

	public void setSKOSSaveButtonEnabled(boolean enabled) {
		skosForm.getSaveButton().setEnabled(enabled);
	}

	public void toNoThesaurusAvailable() {
		VerticalLayout noThesaurusAvalibleVerticalLayout = new VerticalLayout();
		noThesaurusAvailableState(noThesaurusAvalibleVerticalLayout);

		noThesaurusAvalibleVerticalLayout.addComponent(skosForm);

		tabSheet.replaceComponent(skosFormLayout, noThesaurusAvalibleVerticalLayout);
		this.skosFormLayout = noThesaurusAvalibleVerticalLayout;
	}

	private void thesaurusConfigurationPage(VerticalLayout verticalLayoutSkosFile) {
		List<BaseDisplay.CaptionAndComponent> listCaptionAndComponent = new ArrayList<>();

		BaseDisplay.CaptionAndComponent captionAndComponent = new BaseDisplay
				.CaptionAndComponent(new Label($("ThesaurusConfigurationView.about")), new Label(presenter.getAbout()));
		listCaptionAndComponent.add(captionAndComponent);

		captionAndComponent = new BaseDisplay
				.CaptionAndComponent(new Label($("ThesaurusConfigurationView.title")), new Label(presenter.getTitle()));
		listCaptionAndComponent.add(captionAndComponent);

		captionAndComponent = new BaseDisplay
				.CaptionAndComponent(new Label($("ThesaurusConfigurationView.description")), new Label(presenter.getDescription()));
		listCaptionAndComponent.add(captionAndComponent);

		captionAndComponent = new BaseDisplay
				.CaptionAndComponent(new Label($("ThesaurusConfigurationView.date")), new Label(presenter.getDate()));
		listCaptionAndComponent.add(captionAndComponent);

		captionAndComponent = new BaseDisplay
				.CaptionAndComponent(new Label($("ThesaurusConfigurationView.creator")), new Label(presenter.getCreator()));
		listCaptionAndComponent.add(captionAndComponent);

		baseDisplay = new BaseDisplay(listCaptionAndComponent);
		verticalLayoutSkosFile.addComponent(baseDisplay);

		verticalLayoutSkosFile.setSpacing(true);
	}

	public void unloadDescriptionsField() {
		VerticalLayout newVerticalLayoutSkosFile = new VerticalLayout();
		noThesaurusAvailableState(newVerticalLayoutSkosFile);
	}

	public void loadDescriptionFieldsWithFileValue() {
		VerticalLayout newVerticalLayoutSkosFile = new VerticalLayout();
		thesaurusConfigurationPage(newVerticalLayoutSkosFile);
		newVerticalLayoutSkosFile.addComponent(skosForm);
		tabSheet.replaceComponent(skosFormLayout, newVerticalLayoutSkosFile);

		skosFormLayout = newVerticalLayoutSkosFile;

	}

	private BaseForm<FormBean> buildSkosForm() {
		uploadSkosField = new BaseUploadField();
		uploadSkosField.setUploadButtonCaption($("ThesaurusConfigurationView.upload"));
		uploadSkosField.setCaption($("ThesaurusConfigurationView.file"));
		uploadSkosField.setImmediate(true);

		uploadSkosField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				TempFileUpload value = (TempFileUpload) uploadSkosField.getValue();
				presenter.valueChangeInFileSelector(value);
			}
		});

		uploadSkosField.addValidator(new Validator() {
			@Override
			public void validate(Object value) throws InvalidValueException {
				// there to not get a NullPointerException
			}
		});
		BaseForm<FormBean> baseForm = new BaseForm<FormBean>(formBean, this, uploadSkosField) {
			@Override
			protected void saveButtonClick(FormBean viewObject) throws ValidationException {
				presenter.thesaurusFileUploaded((TempFileUpload) uploadSkosField.getValue());
			}

			@Override
			protected void cancelButtonClick(FormBean viewObject) {
				navigateTo().searchConfiguration();
			}
		};
		baseForm.setSizeFull();

		return baseForm;
	}

	private BaseForm<FormBean> buildRejectedTermsForm() {
		rejectedTermsField = new BaseTextArea();
		rejectedTermsField.setCaption($("ThesaurusConfigurationView.rejectedTerms"));
		rejectedTermsField.setWidth("100%");
		rejectedTermsField.setHeight("300px");

		BaseForm<FormBean> rejectedTermsForm = new BaseForm<FormBean>(formBean, this, rejectedTermsField) {
			@Override
			protected void saveButtonClick(FormBean viewObject) throws ValidationException {
				try {
					presenter.saveRejectedTermsButtonClicked(rejectedTermsField.getValue());
				} catch (RecordServicesException e) {
					showError(e);
					e.printStackTrace();
				}
			}

			@Override
			protected void cancelButtonClick(FormBean viewObject) {
				rejectedTermsField.setValue(presenter.getRejectedTerms());
			}
		};

		rejectedTermsField.setValue(presenter.getRejectedTerms());
		rejectedTermsForm.setSizeFull();
		return rejectedTermsForm;
	}

	public void showError(Exception exeption) {
		this.showErrorMessage($("ThesaurusConfigurationView.saveUnexpectedError"));
	}

    @Override
    public void removeAllTheSelectedFile() {
        uploadSkosField.setValue(null);
    }

	public class FormBean {

		private TempFileUpload file;

		private String rejectedTerms;

		public TempFileUpload getFile() {
			return file;
		}

		public void setFile(TempFileUpload file) {
			this.file = file;
		}

		public String getRejectedTerms() {
			return rejectedTerms;
		}

		public void setRejectedTerms(String rejectedTerms) {
			this.rejectedTerms = rejectedTerms;
		}

	}

}
