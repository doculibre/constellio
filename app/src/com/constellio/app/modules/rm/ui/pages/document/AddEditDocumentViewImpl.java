package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.ui.components.document.DocumentForm;
import com.constellio.app.modules.rm.ui.components.document.DocumentFormImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditDocumentViewImpl extends BaseViewImpl implements AddEditDocumentView {

	private final AddEditDocumentPresenter presenter;
	private RecordVO recordVO;
	private ContentViewer contentViewer;
	private DocumentFormImpl recordForm;
	private I18NHorizontalLayout mainLayout;
	private boolean popup;

	public AddEditDocumentViewImpl() {
		this(null);
	}

	public AddEditDocumentViewImpl(RecordVO documentVO) {
		this(documentVO, false);
	}

	public AddEditDocumentViewImpl(RecordVO documentVO, boolean popup) {
		presenter = newPresenter(documentVO);
	}

	protected AddEditDocumentPresenter newPresenter(RecordVO documentVO) {
		return new AddEditDocumentPresenter(this, documentVO);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		} else {
			presenter.forParams(null);
		}
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		String titleKey;
		if (presenter.isAddView()) {
			titleKey = "AddEditDocumentView.addViewTitle";
		} else {
			titleKey = "AddEditDocumentView.editViewTitle";
		}
		return $(titleKey);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		newForm();
		
		mainLayout = new I18NHorizontalLayout();
		mainLayout.setSizeFull();
		
		ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT); 
		if (contentVersionVO != null) {
			mainLayout.setSpacing(true);
			
			contentViewer = new ContentViewer(recordVO, Document.CONTENT, contentVersionVO);
			mainLayout.addComponents(contentViewer, recordForm);
			
			recordForm.setWidth("700px");
			mainLayout.setExpandRatio(contentViewer, 1);
		} else {
			mainLayout.addComponent(recordForm);
		}
		return mainLayout;
	}

	private DocumentFormImpl newForm() {
		recordForm = new DocumentFormImpl(recordVO, (!presenter.isAddView() || presenter.isNewFileAtStart())) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}

			@Override
			public void reload() {
				recordForm = newForm();
				mainLayout.replaceComponent(this, recordForm);
			}

			@Override
			public void commit() {
				for (Field<?> field : fieldGroup.getFields()) {
					try {
						field.commit();
					} catch (SourceException | InvalidValueException e) {
					}
				}
			}
		};

		for (final Field<?> field : recordForm.getFields()) {
			if (field instanceof CustomDocumentField) {
				field.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.customFieldValueChanged((CustomDocumentField<?>) field);
					}
				});
			}
		}

		return recordForm;
	}

	@Override
	public DocumentForm getForm() {
		return recordForm;
	}

	@Override
	public void openAgentURL(String agentURL) {
		Page.getCurrent().open(agentURL, null);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

}
