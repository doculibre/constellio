package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfFileNamePanel extends VerticalLayout {
	@PropertyId("pdfFileName")
	private TextField pdfFileNameField;
	@PropertyId("includeMetadatas")
	private CheckBox includeMetadatasField;
	@PropertyId("generatePdfA")
	private CheckBox generatePdfAField;

	private final PdfInfos pdfInfos;
	private final Window window;

	private boolean cancelled;

	private List<PdfFileNameListener> listeners;

	public PdfFileNamePanel(Window window) {
		this.pdfInfos = new PdfInfos();
		this.window = window;
		this.cancelled = false;
		this.listeners = new ArrayList<>();

		setSpacing(true);
		addStyleName("no-scroll");

		init();
	}

	public void init() {
		window.setHeight("250px");

		pdfFileNameField = new BaseTextField($("PdfFileNamePanel.pdfFileName"));
		pdfFileNameField.setRequired(true);
		pdfFileNameField.setRequiredError($("PdfFileNamePanel.pdfFileName.required"));

		includeMetadatasField = new CheckBox($("PdfFileNamePanel.includeMetadatas"));
		generatePdfAField = new CheckBox($("PdfFileNamePanel.generatePdfA"));

		BaseForm<PdfInfos> baseForm = new BaseForm<PdfInfos>(pdfInfos, this, pdfFileNameField,
				includeMetadatasField, generatePdfAField) {
			@Override
			protected String getSaveButtonCaption() {
				return $("PdfFileNamePanel.saveButton");
			}

			@Override
			protected void saveButtonClick(PdfInfos viewObject) throws ValidationException {
				cancelled = false;

				if (window != null) {
					window.close();
				}

				for (PdfFileNameListener listener : listeners) {
					listener.pdfFileNameFinished(viewObject);
				}
			}

			@Override
			protected void cancelButtonClick(PdfInfos viewObject) {
				cancelled = true;

				if (window != null) {
					window.close();
				}

				for (PdfFileNameListener listener : listeners) {
					listener.pdfFileNameCancelled();
				}
			}
		};

		pdfFileNameField.setValue(new SimpleDateFormat("yyyy-MM-dd_HH_mm'.pdf'").format(new Date()));

		addComponent(baseForm);
	}

	public PdfInfos getPdfInfos() {
		return pdfInfos;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void addPdfFileNameListener(PdfFileNameListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removePdfFileNameListener(PdfFileNameListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	public class PdfInfos {
		private String pdfFileName;
		private boolean includeMetadatas;
		private boolean generatePdfA;

		public String getPdfFileName() {
			return pdfFileName;
		}

		public void setPdfFileName(String pdfFileName) {
			this.pdfFileName = pdfFileName;
		}

		public boolean isIncludeMetadatas() {
			return includeMetadatas;
		}

		public void setIncludeMetadatas(boolean includeMetadatas) {
			this.includeMetadatas = includeMetadatas;
		}

		public boolean isGeneratePdfA() {
			return generatePdfA;
		}

		public void setGeneratePdfA(boolean generatePdfA) {
			this.generatePdfA = generatePdfA;
		}
	}

	public interface PdfFileNameListener {
		public void pdfFileNameFinished(PdfInfos pdfInfos);

		public void pdfFileNameCancelled();
	}
}
