package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.ContentButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class DocumentDecommissioningListViewImpl extends BaseViewImpl implements DocumentDecommissioningListView {
	public static final String PROCESS = "process";

	private final DocumentDecommissioningListPresenter presenter;

	private RecordVO decommissioningList;

	private Button removeDocuments;

	private Button addDocuments;

	HashMap<Integer, Boolean> selected;

	public DocumentDecommissioningListViewImpl() {
		presenter = new DocumentDecommissioningListPresenter(this);
		selected = new HashMap<>();
	}

	@Override
	protected String getTitle() {
		return $("DecommissioningListView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigate().to(RMViews.class).decommissioning();
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		decommissioningList = presenter.forRecordId(event.getParameters()).getDecommissioningList();
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(buildEditButton());
		buttons.add(buildDeleteButton());
		if(presenter.processPermissionOnList()) {
			buttons.add(buildProcessButton());
		}
		buttons.add(buildDocumentsCertificateButton());
		if(presenter.creationPermissionOnList()) {
			buttons.add(buildAddDocumentsButton());
			buttons.add(buildRemoveDocumentsButton());
		}
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		RecordDisplay display = new RecordDisplay(decommissioningList);

		RecordCommentsEditorImpl comments = new RecordCommentsEditorImpl(decommissioningList, DecommissioningList.COMMENTS);
		comments.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.refreshList();
			}
		});

		RecordVODataProvider dataProvider = presenter.getDocuments();
		VerticalLayout layout = new VerticalLayout(display, buildDocumentTable(dataProvider), comments);
		for (int i = 0; i < dataProvider.size(); i++) {
			selected.put(new Integer(i), false);
		}

		layout.setSpacing(true);
		layout.setWidth("100%");

		return layout;
	}

	private Button buildEditButton() {
		Button button = new EditButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked();
			}
		};
		button.setEnabled(presenter.isEditable());
		return button;
	}

	private Button buildDeleteButton() {
		Button button = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked();
			}
		};
		button.setEnabled(presenter.isDeletable());
		return button;
	}

	private Button buildProcessButton() {
		Button process = new ConfirmDialogButton(null, $("DecommissioningListView.process"), false) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DecommissioningListView.confirmProcessing");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.processButtonClicked();
			}
		};
		process.setEnabled(presenter.isProcessableByUser());
		process.addStyleName(PROCESS);
		return process;
	}

	private Button buildDocumentsCertificateButton() {
		ContentButton button = new ContentButton("Reports.documentsCertificate", presenter.getDocumentsReportContentId(),
				presenter.getDocumentsReportContentName()) {
			@Override
			public boolean isVisible() {
				return presenter.isDocumentsCertificateButtonVisible();
			}

		};
		button.setCaption($("DecommissioningListView.documentsCertificate"));
		button.addStyleName(ValoTheme.BUTTON_LINK);
		return button;
	}

	private Component buildDocumentTable(RecordVODataProvider documents) {
		Label header = new Label(presenter.isProcessed() ?
								 $("DocumentDecommissioningListView.processedDocuments") :
								 $("DocumentDecommissioningListView.processableDocuments"));
		header.addStyleName(ValoTheme.LABEL_H2);

		Table table = new RecordVOTable(documents);
		table.setCaption($("DocumentDecommissioningListView.documents", documents.size()));
		table.setPageLength(documents.size());
		table.setWidth("100%");
		table.addGeneratedColumn("checkbox", new Table.ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final CheckBox checkBox = new CheckBox();
				checkBox.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						selected.put((Integer) itemId, checkBox.getValue());
					}
				});
				return checkBox;
			}
		});
		table.setColumnHeader("checkbox", "");
		ArrayList<Object> visibleColumns = new ArrayList<>(asList(table.getVisibleColumns()));
		visibleColumns.remove("checkbox");
		visibleColumns.add(0, "checkbox");
		table.setVisibleColumns(visibleColumns.toArray());

		VerticalLayout layout = new VerticalLayout(header, table);
		layout.setSpacing(true);

		return layout;
	}

	private Button buildRemoveDocumentsButton() {
		removeDocuments = new DeleteButton($("DocumentDecommissioningListView.removeFromList")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.removeDocumentsButtonClicked(selected);
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("DocumentDecommissioningListView.removeFromListConfirmation");
			}

			@Override
			public boolean isEnabled() {
				return !presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed();
			}
		};
		removeDocuments.setEnabled(!presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed());
		removeDocuments.addStyleName(ValoTheme.BUTTON_LINK);
		return removeDocuments;
	}

	private Button buildAddDocumentsButton() {
		addDocuments = new AddButton($("DocumentDecommissioningListView.addToList")) {
			@Override
			protected void buttonClick(ClickEvent clickEvent) {
				presenter.addDocumentsButtonClicked();
			}

			@Override
			public boolean isEnabled() {
				return !presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed();
			}
		};
		addDocuments.setEnabled(!presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed());
		return addDocuments;
	}
}
