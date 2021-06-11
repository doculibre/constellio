package com.constellio.app.modules.rm.ui.components.pdf;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseForm.FieldAndPropertyId;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentPdfFormatSelectorWindowImpl extends BaseWindow implements DocumentPdfFormatSelectorWindow {

	private BaseForm form;

	private OptionGroup pdfFormatsField;

	private DocumentPdfFormatSelectorPresenter presenter;
	private final User user;
	private final Map<String, String> urlParams;

	private boolean pdfA;

	private static final boolean PDF = false;
	private static final boolean PDF_A = true;

	public DocumentPdfFormatSelectorWindowImpl(Document document, User user, Map<String, String> urlParams) {
		this.presenter = new DocumentPdfFormatSelectorPresenter(this, document);
		this.user = user;
		this.urlParams = urlParams;

		setModal(true);
		setWidth("350px");
		setHeight("150px");

		setCaption($("DocumentPdfFormatSelectorWindow.title"));

		pdfFormatsField = new OptionGroup();
		pdfFormatsField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		pdfFormatsField.addItem(PDF);
		pdfFormatsField.setItemCaption(PDF, $("DocumentPdfFormatSelectorWindow.pdf"));
		pdfFormatsField.addItem(PDF_A);
		pdfFormatsField.setItemCaption(PDF_A, $("DocumentPdfFormatSelectorWindow.pdfA"));
		pdfFormatsField.setMultiSelect(false);
		pdfFormatsField.setNullSelectionAllowed(false);
		pdfFormatsField.setRequired(true);
		pdfFormatsField.setImmediate(true);

		List<FieldAndPropertyId> fieldsAndPropertyIds = new ArrayList<FieldAndPropertyId>();
		fieldsAndPropertyIds.add(new FieldAndPropertyId(pdfFormatsField, "pdfA"));

		form = new BaseForm(this, fieldsAndPropertyIds) {
			@Override
			protected String getSaveButtonCaption() {
				return $("DocumentPdfFormatSelectorWindow.generate");
			}

			@Override
			protected void saveButtonClick(Object viewObject) throws ValidationException {
				presenter.generateButtonClicked(user, urlParams);
			}

			@Override
			protected void cancelButtonClick(Object viewObject) {
				presenter.cancelButtonClicked();
			}
		};

		form.setSizeFull();
		setContent(form);
	}

	@Override
	public void open() {
		UI.getCurrent().addWindow(this);
	}

	@Override
	public void createPdf() {
		setPdfA(false);
		presenter.generateButtonClicked(user, urlParams);
	}

	@Override
	public void showErrorMessage(String message) {
		((BaseViewImpl) ConstellioUI.getCurrent().getCurrentView()).showErrorMessage(message);
	}

	@Override
	public void showMessage(String message) {
		((BaseViewImpl) ConstellioUI.getCurrent().getCurrentView()).showMessage(message);
	}

	@Override
	public boolean isPdfA() {
		return pdfA;
	}

	@Override
	public void setPdfA(boolean pdfA) {
		this.pdfA = pdfA;
	}
}
