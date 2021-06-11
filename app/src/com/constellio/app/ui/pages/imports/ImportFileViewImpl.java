package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportFileViewImpl extends BaseViewImpl implements ImportFileView {

	private File exampleFile;
	protected ImportFilePresenterInterface presenter;
	protected VerticalLayout mainLayout;
	private Link exampleFileLink;
	private BaseUploadField uploadField;
	private Button uploadButton;
	private ProgressBar progressBar;
	private Panel messagesPanel;
	private Label legacyIdIndexDisabledWarning;
	private EnumWithSmallCodeComboBox mode;
	private VerticalLayout messagesLayout;
	private CheckBox allowReferencesToNonExistingUsersCheckBox;
	private CheckBox mergeExistingRecordWithSameLegacyId;
	private int total;

	public ImportFileViewImpl() {
		initPresenter();
	}

	protected void initPresenter() {
		presenter = new ImportFilePresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		uploadField = new BaseUploadField();
		uploadField.setCaption(getUploadFieldCaption());

		uploadButton = new BaseButton($("ImportFileView.startImport")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				progressBar.setVisible(true);
				TempFileUpload upload = (TempFileUpload) uploadField.getValue();
				presenter.uploadButtonClicked(upload);
				progressBar.setVisible(false);
			}
		};
		uploadButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		mode = new EnumWithSmallCodeComboBox(ImportFileMode.class);
		mode.setImmediate(true);
		mode.setWidth("200px");
		mode.setConvertedValue(ImportFileMode.STRICT);

		allowReferencesToNonExistingUsersCheckBox = new CheckBox($("ImportFileView.allowReferencesToNonExistingUsersCheckBox"));
		allowReferencesToNonExistingUsersCheckBox.setValue(false);

		mergeExistingRecordWithSameLegacyId = new CheckBox($("ImportFileView.mergeExistingRecordWithSameLegacyId"));
		mergeExistingRecordWithSameLegacyId.setValue(false);

		progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);

		messagesLayout = new VerticalLayout();
		messagesLayout.setWidth("100%");
		messagesLayout.setSpacing(true);

		messagesPanel = new Panel($("ImportFileView.errors"), messagesLayout);
		messagesPanel.setVisible(false);

		legacyIdIndexDisabledWarning = new Label($("ImportFileView.legacyIdIndexDisabledWarning"));
		legacyIdIndexDisabledWarning.addStyleName("system-state-component-important-message");
		legacyIdIndexDisabledWarning.setVisible(presenter.isLegacyIdIndexDisabledWarningVisible());
		mainLayout.addComponents(legacyIdIndexDisabledWarning, mode, allowReferencesToNonExistingUsersCheckBox, mergeExistingRecordWithSameLegacyId, uploadField, uploadButton, progressBar, messagesPanel);
		mainLayout.setExpandRatio(messagesPanel, 1);
		//mainLayout.setComponentAlignment(exampleExcelFileLink, Alignment.TOP_RIGHT);
		mainLayout.setComponentAlignment(uploadButton, Alignment.BOTTOM_RIGHT);

		return mainLayout;
	}

	protected String getUploadFieldCaption() {
		return $("ImportFileView.excelFile");
	}

	@Override
	protected String getTitle() {
		return $("ImportFileView.viewTitle");
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

	@Override
	public void setExampleFile(File exampleFile) {
		this.exampleFile = exampleFile;
	}

	@Override
	public List<String> getSelectedCollections() {
		return Arrays.asList(getCollection());
	}

	@Override
	public ImportFileMode getImportFileMode() {
		return (ImportFileMode) mode.getConvertedValue();
	}

	@Override
	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public void setProgress(int progress) {
		float progressFloat;
		if (total > 0) {
			progressFloat = (float) progress / total;
		} else {
			progressFloat = 0;
		}
		progressBar.setValue(progressFloat);
	}

	@Override
	public void showImportCompleteMessage() {
		showMessage($("ImportFileView.importComplete"));
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		Label messageLabel = new Label(errorMessage);
		messageLabel.setContentMode(ContentMode.HTML);
		messagesPanel.setVisible(true);
		messagesLayout.addComponentAsFirst(messageLabel);
	}

	public boolean isAllowingReferencesToNonExistingUsers() {
		return Boolean.TRUE.equals(allowReferencesToNonExistingUsersCheckBox.getValue());
	}

	public boolean isMergeExistingRecordWithSameLegacyId() {
		return Boolean.TRUE.equals(mergeExistingRecordWithSameLegacyId.getValue());
	}
}
