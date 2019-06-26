package com.constellio.app.modules.rm.ui.pages.userDocuments;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Window.CloseEvent;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.userDocument.DeclareUserContentContainerButton;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.ContentVersionDisplay;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.upload.BaseMultiFileUpload;
import com.constellio.app.ui.framework.components.table.RecordVOSelectionTableAdapter;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.Builder;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ListUserDocumentsViewImpl extends BaseViewImpl implements ListUserDocumentsView, DropHandler {

	public static final String STYLE_NAME = "user-documents";
	public static final String STYLE_LAYOUT = STYLE_NAME + "-layout";
	public static final String TABLE_STYLE_NAME = STYLE_NAME + "-table";

	List<RecordVODataProvider> dataProviders;

	private DragAndDropWrapper dragAndDropWrapper;
	private VerticalLayout mainLayout;
	private BaseMultiFileUpload multiFileUpload;
	private RecordVOLazyContainer userContentContainer;
	private ButtonsContainer<RecordVOLazyContainer> buttonsContainer;
	private SelectionTableAdapter userContentSelectTableAdapter;
	private RecordVOTable userContentTable;
	private Button deleteAllButton;
	private Builder<ContainerButton> classifyButtonFactory;

	private RecordIdToCaptionConverter recordIdToCaptionConverter = new RecordIdToCaptionConverter();

	private boolean inWindow;

	private ListUserDocumentsPresenter presenter;
	private Component quotaSpaceInfo;

	public ListUserDocumentsViewImpl() {
		this(false);
	}

	public ListUserDocumentsViewImpl(boolean inWindow) {
		this.inWindow = inWindow;
		presenter = new ListUserDocumentsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListUserDocumentsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		if (classifyButtonFactory == null) {
			classifyButtonFactory = new Builder<ContainerButton>() {
				@Override
				public ContainerButton build() {
					return new DeclareUserContentContainerButton(ListUserDocumentsViewImpl.this);
				}
			};
		}

		addStyleName(STYLE_NAME);
		setCaption($("UserDocumentsWindow.title"));

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_LAYOUT);
		mainLayout.setSpacing(true);

		multiFileUpload = new BaseMultiFileUpload() {
			@Override
			protected void handleFile(File file, String fileName, String mimeType, long length) {
				presenter.handleFile(file, fileName, mimeType, length);
				refreshAvailableSpace();
			}

			@Override
			public void drop(DragAndDropEvent event) {
				if (presenter.isSpaceLimitReached(event)) {
					showErrorMessage($("ListUserDocumentsView.spaceLimitReached"));
				} else {
					super.drop(event);
				}
			}

			@Override
			protected void onUploadWindowClosed(CloseEvent e) {
				presenter.refreshDocuments();
			}
		};
		multiFileUpload.setWidth("100%");

		userContentContainer = new RecordVOLazyContainer(dataProviders);
		buttonsContainer = new ButtonsContainer<RecordVOLazyContainer>(userContentContainer);

		buttonsContainer.addButton(classifyButtonFactory.build());
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						RecordVO recordVO = ((RecordVOItem) buttonsContainer.getItem(itemId)).getRecord();
						presenter.deleteButtonClicked(recordVO, true);
						refreshAvailableSpace();
					}
				};
			}
		});

		userContentTable = new RecordVOTable() {
			@Override
			protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
				Component metadataComponent;
				if (metadataValue.getMetadata().codeMatches(UserDocument.TITLE)) {
					metadataComponent = newCaptionComponent(recordVO);
				} else {
					metadataComponent = super.buildMetadataComponent(metadataValue, recordVO);
				}
				return metadataComponent;
			}

			@Override
			protected String getTitleColumnStyle(RecordVO recordVO) {
				String style;
				if (UserDocument.SCHEMA_TYPE.equals(recordVO.getSchema().getTypeCode())) {
					style = null;
				} else {
					style = super.getTitleColumnStyle(recordVO);
				}
				return style;
			}
		};
		userContentTable.setContainerDataSource(buttonsContainer);
		userContentTable.setWidth("100%");
		userContentTable.addStyleName(TABLE_STYLE_NAME);
		userContentTable.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		userContentTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");

		userContentSelectTableAdapter = new RecordVOSelectionTableAdapter(userContentTable) {
			@Override
			public void selectAll() {
				presenter.selectAllClicked();
			}

			@Override
			public void deselectAll() {
				presenter.deselectAllClicked();
			}

			@Override
			public boolean isAllItemsSelected() {
				return presenter.isAllItemsSelected();
			}

			@Override
			public boolean isAllItemsDeselected() {
				return presenter.isAllItemsDeselected();
			}

			@Override
			public boolean isSelected(Object itemId) {
				RecordVOItem item = (RecordVOItem) buttonsContainer.getItem(itemId);
				RecordVO recordVO = item.getRecord();
				return presenter.isSelected(recordVO);
			}

			@Override
			public void setSelected(Object itemId, boolean selected) {
				RecordVOItem item = (RecordVOItem) buttonsContainer.getItem(itemId);
				RecordVO recordVO = item.getRecord();
				presenter.selectionChanged(recordVO, selected);
				adjustSelectAllButton(selected);
			}
		};

		deleteAllButton = new DeleteButton($("ListUserDocumentsView.deleteAllButtonTitle")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("ListUserDocumentsView.deleteAllConfirmation");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				if (dialog.isConfirmed()) {
					List<RecordVO> records = new ArrayList<>();
					int size = userContentContainer.size();
					for (int i = 0; i < size; i++) {
						records.add(userContentContainer.getRecordVO(i));
					}
					if (dataProviders != null) {
						for (RecordVODataProvider dataProvider : dataProviders) {
							MetadataSchemaVO schema = dataProvider.getSchema();
							for (RecordVO recordVO : records) {
								if (recordVO.getSchema().getTypeCode().equals(schema.getTypeCode())) {
									presenter.deleteButtonClicked(recordVO, false);
								}
							}

							dataProvider.fireDataRefreshEvent();
							refreshAvailableSpace();
						}
					}
				}
			}

			@Override
			public boolean isEnabled() {
				return userContentContainer != null && userContentContainer.size() > 0;
			}
		};
		deleteAllButton.addStyleName(ValoTheme.BUTTON_LINK);
		if (presenter.isQuotaSpaceConfigActivated()) {
			quotaSpaceInfo = new Label(
					"<p style=\"color:green\">" + $("ListUserDocumentsView.availableSpaceMessage",
							presenter.getAvailableSpace()) + "</p>", ContentMode.HTML);
			mainLayout.addComponents(multiFileUpload, quotaSpaceInfo, deleteAllButton, userContentSelectTableAdapter);
		} else {
			mainLayout.addComponents(multiFileUpload, deleteAllButton, userContentSelectTableAdapter);
		}
		dragAndDropWrapper = new DragAndDropWrapper(mainLayout);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(multiFileUpload);
		return dragAndDropWrapper;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		String parameters = event != null ? event.getParameters() : null;
		presenter.forParams(parameters);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	protected Component newCaptionComponent(final RecordVO recordVO) {
		Component captionComponent;
		if (recordVO instanceof UserDocumentVO) {
			UserDocumentVO userDocumentVO = (UserDocumentVO) recordVO;
			ContentVersionVO contentVersionVO = userDocumentVO.getContent();
			if (contentVersionVO != null) {
				String filename = contentVersionVO.getFileName();
				captionComponent = new ContentVersionDisplay(recordVO, contentVersionVO, filename, presenter);
			} else {
				captionComponent = new Label("");
			}
		} else {
			captionComponent = new Label(recordIdToCaptionConverter.convertToPresentation(recordVO.getId(), String.class, getLocale()));
		}
		return captionComponent;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		setVisible(true);
		multiFileUpload.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return multiFileUpload.getAcceptCriterion();
	}

	public void setClassifyButtonFactory(Builder<ContainerButton> classifyButtonFactory) {
		this.classifyButtonFactory = classifyButtonFactory;
	}

	@Override
	public void setUserContent(List<RecordVODataProvider> dataProviders) {
		this.dataProviders = dataProviders;
	}

	@Override
	public void refresh() {
		for (RecordVODataProvider dataProvider : dataProviders) {
			dataProvider.fireDataRefreshEvent();
		}
	}

	@Override
	public boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.backgroundViewMonitor();
	}

	@Override
	public void showUploadMessage(String message) {
//		multiFileUpload.notifyMessage(message);
		showClickableMessage(message);
	}

	@Override
	public void showUploadErrorMessage(String message) {
//		multiFileUpload.notifyMessage(message);
		showErrorMessage(message);
	}

	@Override
	public boolean isInAWindow() {
		return inWindow;
	}

	private void refreshAvailableSpace() {
		if (presenter.isQuotaSpaceConfigActivated()) {
			Label quotaSpaceInfoAferRefresh = new Label(
					"<p style=\"color:green\">" + $("ListUserDocumentsView.availableSpaceMessage", presenter.getAvailableSpace())
					+ "</p>",
					ContentMode.HTML);
			mainLayout.replaceComponent(quotaSpaceInfo, quotaSpaceInfoAferRefresh);
			quotaSpaceInfo = quotaSpaceInfoAferRefresh;
		}
	}

}
