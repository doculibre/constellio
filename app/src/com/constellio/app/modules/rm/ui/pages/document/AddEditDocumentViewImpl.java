package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.ui.components.document.DocumentForm;
import com.constellio.app.modules.rm.ui.components.document.DocumentFormImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.framework.components.viewers.VisibilityChangeEvent;
import com.constellio.app.ui.framework.components.viewers.VisibilityChangeListener;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.UserDocument;
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

	public static final String RECORD_FORM_WIDTH = "700px";
	private final AddEditDocumentPresenter presenter;
	private RecordVO recordVO;
	private ContentViewer contentViewer;
	private DocumentFormImpl recordForm;
	private I18NHorizontalLayout mainLayout;
	private boolean popup;
	private boolean isUserDocumentViewer = false;
	private boolean isDuplicateDocumentViewer = false;

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
		RecordVO userDocumentRecordVO = presenter.getUserDocumentRecordVO();
		RecordVO duplicateDocumentRecordVO = presenter.getDuplicateDocumentRecordVO();
		if (contentVersionVO != null && !presenter.isAddView()
			|| userDocumentRecordVO != null || duplicateDocumentRecordVO != null) {
			mainLayout.setSpacing(true);

			if (userDocumentRecordVO != null) {
				contentViewer = new ContentViewer(userDocumentRecordVO, UserDocument.CONTENT, userDocumentRecordVO.get(UserDocument.CONTENT));
			} else if (duplicateDocumentRecordVO != null) {
				contentViewer = new ContentViewer(duplicateDocumentRecordVO, Document.CONTENT, duplicateDocumentRecordVO.get(Document.CONTENT));
				isDuplicateDocumentViewer = true;
			} else {
				contentViewer = new ContentViewer(recordVO, Document.CONTENT, contentVersionVO);
			}

			contentViewer.setWidth("100%");
			contentViewer.setHeight("100%");

			mainLayout.addComponents(recordForm);

			if (contentViewer.isViewerComponentVisible()) {
				mainLayout.addComponent(contentViewer, 0);
				recordForm.setWidth(RECORD_FORM_WIDTH);
				mainLayout.setExpandRatio(contentViewer, 1);

				contentViewer.addImageViewerVisibilityChangeListener(new VisibilityChangeListener() {
					@Override
					public void onVisibilityChange(VisibilityChangeEvent visibilityChangeEvent) {
						if (contentViewer != null && !visibilityChangeEvent.isNewVisibilily()) {
							if (!contentViewer.isViewerComponentVisible() && mainLayout.getComponentIndex(contentViewer) >= 0) {
								mainLayout.removeComponent(contentViewer);
								contentViewer = null;
								visibilityChangeEvent.setRemoveThisVisiblityLisener(true);
								recordForm.setImmediate(true);
								recordForm.setWidth("100%");
							}
						}
					}
				});
			}

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
				Component oldRecordForm = recordForm;

				recordForm = newForm();

				if ((isUserDocumentViewer && presenter.getUserDocumentRecordVO() == null)
					|| (isDuplicateDocumentViewer && presenter.getDuplicateDocumentRecordVO() == null)) {
					isDuplicateDocumentViewer = false;
					isUserDocumentViewer = false;
					mainLayout.removeComponent(contentViewer);
					contentViewer = null;
				} else if (contentViewer != null && contentViewer.isViewerComponentVisible()) {
					recordForm.setWidth(RECORD_FORM_WIDTH);
					mainLayout.setExpandRatio(contentViewer, 1);
				} else {

				}

				mainLayout.replaceComponent(oldRecordForm, recordForm);
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
