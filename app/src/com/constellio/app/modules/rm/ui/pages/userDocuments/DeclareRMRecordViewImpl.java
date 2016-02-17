package com.constellio.app.modules.rm.ui.pages.userDocuments;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.converters.RecordVOToCaptionConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.data.Item;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class DeclareRMRecordViewImpl extends BaseViewImpl implements DeclareRMRecordView {
	private static final String PATH_SEP = " / ";

	private UserDocumentVO userDocumentVO;
	private String newVersionDocumentId;
	private String folderId;
	private Map<DocumentVO, ContentVersionVO> duplicateVOs = new HashMap<>();
	private Map<DocumentVO, Double> similarDocumentVOs = new HashMap<>();
	private Map<FolderVO, Double> suggestedFolderVOs = new HashMap<>();
	private boolean deleteButtonVisible;
	private VerticalLayout mainLayout;
	private VerticalLayout duplicatesLayout;
	private CssLayout suggestionsLayout;
	private VerticalLayout newVersionLayout;
	private VerticalLayout newDocumentLayout;
	private HorizontalLayout buttonsLayout;
	private LookupRecordField lookupNewVersionDocumentIdField;
	private LookupRecordField lookupNewDocumentFolderIdField;
	private Table duplicateVOsTable;
	private Table similarDocumentVOsTable;
	private Table suggestedFolderVOsTable;
	private BaseButton deleteUserDocumentButton;
	private BaseButton cancelButton;
	private BaseButton declareButton;

	private DeclareRMRecordPresenter presenter;

	public DeclareRMRecordViewImpl(UserDocumentVO userDocumentVO) {
		this.userDocumentVO = userDocumentVO;
		this.presenter = new DeclareRMRecordPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("DeclareRMRecordView.title");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);
		mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		buildDuplicatesLayout();
		buildSuggestionsLayout();
		buildButtonsLayout();

		mainLayout.addComponents(duplicatesLayout, suggestionsLayout, buttonsLayout);

		return mainLayout;
	}

	@SuppressWarnings("unchecked")
	private void buildDuplicatesLayout() {
		duplicatesLayout = new VerticalLayout();
		duplicatesLayout.setSpacing(true);
		duplicatesLayout.setWidth("90%");
		duplicatesLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		if (duplicateVOs.isEmpty()) {
			duplicatesLayout.setVisible(false);
		} else {
			duplicateVOsTable = new RecordVOTable();
			duplicateVOsTable.addStyleName("duplicates");
			duplicateVOsTable.setCaption($("DeclareRMRecordView.duplicates"));
			duplicateVOsTable.addContainerProperty("document", Component.class, null);
			duplicateVOsTable.addContainerProperty("duplicateVersion", Component.class, null);
			duplicateVOsTable.addContainerProperty("currentVersion", Component.class, null);
			duplicateVOsTable.setColumnExpandRatio("document", 1);
			duplicateVOsTable.setColumnHeader("document", $("DeclareRMRecordView.duplicates.document"));
			duplicateVOsTable.setColumnHeader("duplicateVersion", $("DeclareRMRecordView.duplicates.duplicateVersion"));
			duplicateVOsTable.setColumnHeader("currentVersion", $("DeclareRMRecordView.duplicates.currentVersion"));
			duplicateVOsTable.setPageLength(0);
			duplicateVOsTable.setWidth("100%");

			for (final DocumentVO duplicateVO : duplicateVOs.keySet()) {
				ContentVersionVO duplicateContentVersionVO = duplicateVOs.get(duplicateVO);
				ContentVersionVO currentContentVersionVO = duplicateVO.getContent();

				ReferenceDisplay duplicateDisplay = new ReferenceDisplay(duplicateVO);
				if (duplicateDisplay.isEnabled()) {
					duplicateDisplay.addClickListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							closeWindow();
						}
					});
				}
				String path = getPath(duplicateVO);
				duplicateDisplay.setCaption(path);

				String duplicateContentVersion = duplicateContentVersionVO.getVersion();
				String currentContentVersion = currentContentVersionVO.getVersion();

				Label duplicateContentVersionLabel = new Label(duplicateContentVersion);
				if (!duplicateContentVersion.equals(currentContentVersion)) {
					duplicateContentVersionLabel.addStyleName("duplicate-warning");
				}
				Label currentContentVersionLabel = new Label(currentContentVersion);

				Item item = duplicateVOsTable.addItem(duplicateVO);
				item.getItemProperty("document").setValue(duplicateDisplay);
				item.getItemProperty("duplicateVersion").setValue(duplicateContentVersionLabel);
				item.getItemProperty("currentVersion").setValue(currentContentVersionLabel);
			}
			duplicatesLayout.addComponent(duplicateVOsTable);
		}
	}

	private String getPath(DocumentVO documentVO) {
		StringBuffer path = new StringBuffer();

		Locale locale = getLocale();
		RecordVOToCaptionConverter recordVOToCaptionConverter = new RecordVOToCaptionConverter();

		String documentCaption = recordVOToCaptionConverter.convertToPresentation(documentVO, String.class, locale);
		path.append(documentCaption);

		String folderId = documentVO.getFolder();
		if (folderId != null) {
			FolderVO folderVO = presenter.getFolderVO(folderId);
			String folderPath = getPath(folderVO);
			path.append(" (");
			path.append(folderPath);
			path.append(")");
		}

		return path.toString();
	}

	private String getPath(FolderVO folderVO) {
		StringBuffer path = new StringBuffer();

		Locale locale = getLocale();
		RecordVOToCaptionConverter recordVOToCaptionConverter = new RecordVOToCaptionConverter();

		String parentFolderId = folderVO.getParentFolder();
		while (parentFolderId != null) {
			FolderVO parentFolderVO = presenter.getFolderVO(parentFolderId);
			String folderCaption = recordVOToCaptionConverter.convertToPresentation(parentFolderVO, String.class, locale);
			if (path.length() > 0) {
				path.insert(0, PATH_SEP);
			}
			path.insert(0, folderCaption);
			parentFolderId = parentFolderVO.getParentFolder();
		}

		if (path.length() > 0) {
			path.append(PATH_SEP);
		}
		String folderCaption = recordVOToCaptionConverter.convertToPresentation(folderVO, String.class, locale);
		path.append(folderCaption);
		return path.toString();
	}

	private void buildSuggestionsLayout() {
		suggestionsLayout = new CssLayout();
		suggestionsLayout.setWidth("90%");
		buildNewVersionLayout();
		buildNewDocumentLayout();
		suggestionsLayout.addComponents(newVersionLayout, newDocumentLayout);
	}

	@SuppressWarnings("unchecked")
	private void buildNewVersionLayout() {
		newVersionLayout = new VerticalLayout();
		newVersionLayout.setSpacing(true);
		newVersionLayout.setWidth("50%");

		lookupNewVersionDocumentIdField = new LookupRecordField(Document.SCHEMA_TYPE);
		lookupNewVersionDocumentIdField.setCaption($("DeclareRMRecordView.newVersionDocument"));
		lookupNewVersionDocumentIdField.setPropertyDataSource(new NestedMethodProperty<>(this, "newVersionDocumentId"));

		similarDocumentVOsTable = new RecordVOTable();
		similarDocumentVOsTable.setCaption($("DeclareRMRecordView.similarDocuments"));
		similarDocumentVOsTable.addContainerProperty("document", Button.class, null);
		similarDocumentVOsTable.addContainerProperty("similarity", Label.class, null);
		similarDocumentVOsTable.setColumnExpandRatio("document", 1);
		similarDocumentVOsTable.setColumnHeader("document", $("DeclareRMRecordView.similarDocuments.document"));
		similarDocumentVOsTable.setColumnHeader("similarity", $("DeclareRMRecordView.similarDocuments.similarity"));
		similarDocumentVOsTable.setPageLength(0);
		similarDocumentVOsTable.setWidth("100%");

		for (final DocumentVO documentVO : similarDocumentVOs.keySet()) {
			String buttonCaption = getPath(documentVO);
			Double similarity = similarDocumentVOs.get(documentVO);

			BaseButton selectDocumentButton = new BaseButton(buttonCaption) {
				@Override
				public void buttonClick(ClickEvent event) {
					lookupNewDocumentFolderIdField.setValue(null);
					lookupNewVersionDocumentIdField.setValue(documentVO.getId());
				}
			};
			selectDocumentButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectDocumentButton.addExtension(new NiceTitle(selectDocumentButton, $("DeclareRMRecordView.selectDocument")));
			Resource documentVOIcon = FileIconUtils.getIcon(documentVO);
			selectDocumentButton.setIcon(documentVOIcon);

			String similarityText;
			if (similarity == 1) {
				similarityText = $("DeclareRMRecordView.similarDocuments.similarity.duplicate");
			} else {
				similarityText = String.format("%2.1f%%", similarity * 100);
			}
			Label similarityLabel = new Label(similarityText);
			Item item = similarDocumentVOsTable.addItem(documentVO);
			item.getItemProperty("document").setValue(selectDocumentButton);
			item.getItemProperty("similarity").setValue(similarityLabel);
		}

		newVersionLayout.addComponents(similarDocumentVOsTable, lookupNewVersionDocumentIdField);
	}

	@SuppressWarnings("unchecked")
	private void buildNewDocumentLayout() {
		newDocumentLayout = new VerticalLayout();
		newDocumentLayout.setSpacing(true);
		newDocumentLayout.setWidth("50%");

		lookupNewDocumentFolderIdField = new LookupRecordField(Folder.SCHEMA_TYPE);
		lookupNewDocumentFolderIdField.setCaption($("DeclareRMRecordView.newDocumentFolder"));
		lookupNewDocumentFolderIdField.setPropertyDataSource(new NestedMethodProperty<>(this, "folderId"));

		suggestedFolderVOsTable = new RecordVOTable();
		suggestedFolderVOsTable.setCaption($("DeclareRMRecordView.suggestedFolders"));
		suggestedFolderVOsTable.addContainerProperty("folder", Button.class, null);
//		suggestedFolderVOsTable.addContainerProperty("similarity", Label.class, null);
		suggestedFolderVOsTable.setColumnHeader("folder", $("DeclareRMRecordView.suggestedFolders.folder"));
//		suggestedFolderVOsTable.setColumnHeader("similarity", $("DeclareRMRecordView.suggestedFolders.similarity"));
		suggestedFolderVOsTable.setColumnExpandRatio("folder", 1);
		suggestedFolderVOsTable.setPageLength(0);
		suggestedFolderVOsTable.setWidth("100%");

		for (final FolderVO folderVO : suggestedFolderVOs.keySet()) {
			String buttonCaption = getPath(folderVO);
//			Double similarity = suggestedFolderVOs.get(folderVO);

			BaseButton selectFolderButton = new BaseButton(buttonCaption) {
				@Override
				public void buttonClick(ClickEvent event) {
					lookupNewVersionDocumentIdField.setValue(null);
					lookupNewDocumentFolderIdField.setValue(folderVO.getId());
				}
			};
			Resource folderVOIcon = FileIconUtils.getIcon(folderVO);
			selectFolderButton.setIcon(folderVOIcon);
			selectFolderButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectFolderButton.addExtension(new NiceTitle(selectFolderButton, $("DeclareRMRecordView.selectFolder")));

//			Label similarityLabel = new Label(String.format("%2.1f%%", similarity * 100));
			Item item = suggestedFolderVOsTable.addItem(folderVO);
			item.getItemProperty("folder").setValue(selectFolderButton);
//			item.getItemProperty("similarity").setValue(similarityLabel);
		}

		newDocumentLayout.addComponents(suggestedFolderVOsTable, lookupNewDocumentFolderIdField);
	}

	private void buildButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

		declareButton = new BaseButton($("DeclareRMRecordView.declareButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.declareButtonClicked();
			}
		};
		declareButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		deleteUserDocumentButton = new DeleteButton($("DeclareRMRecordView.deleteUserDocument")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteUserDocumentButtonClicked();
			}
		};
		deleteUserDocumentButton.removeStyleName(ValoTheme.BUTTON_BORDERLESS);
		deleteUserDocumentButton.setVisible(deleteButtonVisible);

		cancelButton = new BaseButton($("DeclareRMRecordView.cancel")) {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		};

		buttonsLayout.addComponents(declareButton, deleteUserDocumentButton, cancelButton);
	}

	@Override
	public UserDocumentVO getUserDocumentVO() {
		return userDocumentVO;
	}

	@Override
	public String getNewVersionDocumentId() {
		return newVersionDocumentId;
	}

	public void setNewVersionDocumentId(String newVersionDocumentId) {
		this.newVersionDocumentId = newVersionDocumentId;
	}

	@Override
	public String getFolderId() {
		return folderId;
	}

	@Override
	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	@Override
	public void setDuplicates(Map<DocumentVO, ContentVersionVO> duplicateVOs) {
		this.duplicateVOs = duplicateVOs;
	}

	@Override
	public void setSimilarDocuments(Map<DocumentVO, Double> similarDocumentVOs) {
		this.similarDocumentVOs = similarDocumentVOs;
	}

	@Override
	public void setSuggestedFolders(Map<FolderVO, Double> suggestedFolderVOs) {
		this.suggestedFolderVOs = suggestedFolderVOs;
	}

	@Override
	public void setDeleteButtonVisible(boolean visible) {
		this.deleteButtonVisible = visible;
	}

	@Override
	public void closeWindow() {
		Window window = (Window) getParent();
		window.close();
	}
}
