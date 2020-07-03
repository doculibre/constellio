package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.ui.components.document.DocumentForm;
import com.constellio.app.modules.rm.ui.components.document.DocumentFormImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.pages.extrabehavior.ProvideSecurityWithNoUrlParamSupport;
import com.constellio.app.modules.rm.ui.pages.extrabehavior.SecurityWithNoUrlParamSupport;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.splitpanel.CollapsibleHorizontalSplitPanel;
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
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditDocumentViewImpl extends BaseViewImpl implements AddEditDocumentView, ProvideSecurityWithNoUrlParamSupport {

	public static final int RECORD_FORM_WIDTH = 50;
	public static final Unit RECORD_FORM_WIDTH_UNIT = Unit.PERCENTAGE;
	private final AddEditDocumentPresenter presenter;
	private RecordVO recordVO;
	private ContentViewer contentViewer;
	private DocumentFormImpl recordForm;
	private I18NHorizontalLayout mainLayout;
	private boolean isUserDocumentViewer = false;
	private boolean isDuplicateDocumentViewer = false;
	private Component contentMetadataComponent;
	private boolean inWindow;
	private CollapsibleHorizontalSplitPanel splitPanel;

	public AddEditDocumentViewImpl() {
		this(null);
	}

	public AddEditDocumentViewImpl(RecordVO documentVO) {
		this(documentVO, false);
	}

	public AddEditDocumentViewImpl(RecordVO documentVO, boolean inWindow) {
		this.inWindow = inWindow;
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
		if (contentViewer != null && !contentViewer.isViewerComponentVisible()
			&& contentMetadataComponent instanceof CollapsibleHorizontalSplitPanel
			&& ((CollapsibleHorizontalSplitPanel) contentMetadataComponent).getRealFirstComponent() == contentViewer) {
			mainLayout.replaceComponent(contentMetadataComponent, recordForm);
		}
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
		addStyleName("add-edit-document-view");
		
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
				contentViewer = new ContentViewer(getConstellioFactories().getAppLayerFactory(), userDocumentRecordVO, UserDocument.CONTENT,
						userDocumentRecordVO.get(UserDocument.CONTENT));

			} else if (duplicateDocumentRecordVO != null) {
				contentViewer = new ContentViewer(getConstellioFactories().getAppLayerFactory(),
						duplicateDocumentRecordVO, Document.CONTENT, duplicateDocumentRecordVO.get(Document.CONTENT));

				isDuplicateDocumentViewer = true;
			} else {
				contentViewer = new ContentViewer(getConstellioFactories().getAppLayerFactory(), recordVO, Document.CONTENT,
						contentVersionVO);
			}

			recordForm.setSizeFull();
			if (contentViewer.isViewerComponentVisible()) {
				adjustContentViewerSize();

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

				splitPanel = new CollapsibleHorizontalSplitPanel(DisplayDocumentViewImpl.class.getName());
				splitPanel.setFirstComponent(contentViewer);
				splitPanel.setSecondComponent(recordForm);
				splitPanel.setSecondComponentWidth(RECORD_FORM_WIDTH, RECORD_FORM_WIDTH_UNIT);
				contentMetadataComponent = splitPanel;
			} else {
				contentMetadataComponent = recordForm;
			}
		} else {
			contentMetadataComponent = recordForm;
		}
		mainLayout.addComponent(contentMetadataComponent);
		mainLayout.setExpandRatio(contentMetadataComponent, 1);
		return mainLayout;
	}

	private void replaceContentViewer(ContentViewer oldContentViewer, ContentViewer newContentViewer) {
		Component parent = oldContentViewer.getParent();
		if (parent instanceof CollapsibleHorizontalSplitPanel) {
			if (newContentViewer != null) {
				((CollapsibleHorizontalSplitPanel) parent).replaceComponent(oldContentViewer, newContentViewer);
			} else {
				((CollapsibleHorizontalSplitPanel) parent).removeComponent(oldContentViewer);
			}
		} else if (parent instanceof AbstractLayout) {
			if (newContentViewer != null) {
				((AbstractLayout) parent).replaceComponent(oldContentViewer, newContentViewer);
			} else {
				((AbstractLayout) parent).removeComponent(oldContentViewer);
			}
		}
	}

	private void replaceRecordForm(DocumentFormImpl oldRecordForm, DocumentFormImpl newRecordForm) {
		Component parent = oldRecordForm.getParent();
		if (parent instanceof CollapsibleHorizontalSplitPanel) {
			if (newRecordForm != null) {
				((CollapsibleHorizontalSplitPanel) parent).replaceComponent(oldRecordForm, newRecordForm);
			} else {
				((CollapsibleHorizontalSplitPanel) parent).removeComponent(oldRecordForm);
			}
		} else if (parent instanceof AbstractLayout) {
			if (newRecordForm != null) {
				((AbstractLayout) parent).replaceComponent(oldRecordForm, newRecordForm);
			} else {
				((AbstractLayout) parent).removeComponent(oldRecordForm);
			}
		}
	}

	private void adjustContentViewerSize() {
		if (inWindow) {
			contentViewer.setWidth("100%");
			//			int viewerHeight = Page.getCurrent().getBrowserWindowHeight() - 68;
			//			contentViewer.setHeight(viewerHeight + "px");

			final String functionId = "adjustContentViewerHeight";
			JavaScript.getCurrent().addFunction(functionId,
					new JavaScriptFunction() {
						@Override
						public void call(JsonArray arguments) {
							int splitterDivHeight = (int) arguments.getNumber(0);
							int newViewerHeight = splitterDivHeight - 4;
							contentViewer.setHeight(newViewerHeight + "px");
							splitPanel.setFirstComponentHeight(newViewerHeight, Unit.PIXELS);

							if (contentViewer.isVerticalScroll()) {
								splitPanel.addStyleName("first-component-no-vertical-scroll");
							}
						}
					});

			StringBuilder js = new StringBuilder();
			js.append("  var splitterDiv =  document.getElementsByClassName('v-splitpanel-hsplitter')[0];");
			js.append("  var splitterDivHeight =  constellio_getHeight(splitterDiv);");
			js.append(functionId + "(splitterDivHeight);");
			JavaScript.getCurrent().execute(js.toString());
		}
	}

	private DocumentFormImpl newForm() {
		recordForm = new DocumentFormImpl(recordVO, (!presenter.isAddView() || presenter.isNewFileAtStart()), AddEditDocumentViewImpl.this.getConstellioFactories()) {
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
				DocumentFormImpl oldRecordForm = recordForm;
				recordForm = newForm();
				if ((isUserDocumentViewer && presenter.getUserDocumentRecordVO() == null)
					|| (isDuplicateDocumentViewer && presenter.getDuplicateDocumentRecordVO() == null)) {
					isDuplicateDocumentViewer = false;
					isUserDocumentViewer = false;
					replaceContentViewer(contentViewer, null);
					contentViewer = null;
				}
				replaceRecordForm(oldRecordForm, recordForm);
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

	@Override
	protected boolean isBreadcrumbsVisible() {
		return !inWindow;
	}

	@Override
	public SecurityWithNoUrlParamSupport getSecurityWithNoUrlParamSupport() {
		return presenter;
	}
}
