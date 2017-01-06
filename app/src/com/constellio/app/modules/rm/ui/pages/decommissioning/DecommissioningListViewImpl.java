package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.decommissioning.ContainerDetailTableGenerator;
import com.constellio.app.modules.rm.ui.components.decommissioning.DecomValidationRequestWindowButton;
import com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator;
import com.constellio.app.modules.rm.ui.components.decommissioning.ValidationsGenerator;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListViewImpl extends BaseViewImpl implements DecommissioningListView {
	public static final String PROCESS = "process";
	public static final String APPROVAL_BUTTON = "approval";
	public static final String APPROVAL_REQUEST_BUTTON = "approvalRequest";
	public static final String VALIDATION_BUTTON = "validation";
	public static final String VALIDATION_REQUEST_BUTTON = "sendValidationRequest";
	public static final String REMOVE_FOLDERS_BUTTON = "removeFolders";
	public static final String ADD_FOLDERS_BUTTON = "addFolders";

	private final DecommissioningListPresenter presenter;

	private RecordVO decommissioningList;
	private BeanItemContainer<ContainerVO> containerVOs;

	private Component validationComponent;
	private BaseTable validations;
	private Component foldersToValidateComponent;
	private BaseTable foldersToValidate;
	private Component packageableFolderComponent;
	private BaseTable packageableFolders;
	private Component processableFolderComponent;
	private BaseTable processableFolders;
	private Component excludedFolderComponent;
	private BaseTable excludedFolders;

	private Button process;
	private Button validationRequest;
	private Button validation;
	private Button approval;
	private Button approvalRequest;
	private Button removeFolders;
	private Button addFolders;

	public DecommissioningListViewImpl() {
		presenter = new DecommissioningListPresenter(this);
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
	protected Component buildMainComponent(ViewChangeEvent event) {
		RecordDisplay display = new RecordDisplay(decommissioningList);

		containerVOs = new BeanItemContainer<>(ContainerVO.class, presenter.getContainers());

		List<DecomListValidation> validations = presenter.getValidations();
		validationComponent = buildValidatorsComponent(validations);
		validationComponent.setVisible(!validations.isEmpty());

		if (presenter.isValidationRequestedForCurrentUser()) {
			List<FolderDetailVO> foldersToValidate = presenter.getFoldersToValidate();
			foldersToValidateComponent = buildFoldersToValidateComponent(foldersToValidate);
			foldersToValidateComponent.setVisible(!foldersToValidate.isEmpty());

			packageableFolders = new BaseTable("DecommissioningListView.packageableFolders");
			packageableFolderComponent = new VerticalLayout(packageableFolders);
			packageableFolderComponent.setVisible(false);

			processableFolders = new BaseTable("DecommissioningListView.processableFolders");
			processableFolderComponent = new VerticalLayout(processableFolders);
			processableFolderComponent.setVisible(false);
		} else {
			List<FolderDetailVO> packageableFolders = presenter.getPackageableFolders();
			packageableFolderComponent = buildPackageableFolderComponent(packageableFolders);
			packageableFolderComponent.setVisible(!packageableFolders.isEmpty());

			List<FolderDetailVO> processableFolders = presenter.getProcessableFolders();
			processableFolderComponent = buildProcessableFolderComponent(processableFolders);
			processableFolderComponent.setVisible(!processableFolders.isEmpty());

			foldersToValidate = new BaseTable("DecommissioningListView.foldersToValidate");
			foldersToValidateComponent = new VerticalLayout(foldersToValidate);
			foldersToValidateComponent.setVisible(false);
		}

		List<FolderDetailVO> excludedFolders = presenter.getExcludedFolders();
		excludedFolderComponent = buildExcludedFolderComponent(excludedFolders);
		excludedFolderComponent.setVisible(!excludedFolders.isEmpty());

		List<DecomListContainerDetail> containerDetails = presenter.getContainerDetails();
		Component containerComponent = buildContainerComponent(containerDetails);
		containerComponent.setVisible(!containerDetails.isEmpty());

		RecordCommentsEditorImpl comments = new RecordCommentsEditorImpl(decommissioningList, DecommissioningList.COMMENTS);
		comments.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.refreshList();
			}
		});

		VerticalLayout layout = new VerticalLayout(display, validationComponent, packageableFolderComponent,
				processableFolderComponent, foldersToValidateComponent, excludedFolderComponent, containerComponent, comments);
		layout.setSpacing(true);
		layout.setWidth("100%");

		return layout;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(buildEditButton());
		buttons.add(buildDeleteButton());
		buttons.add(buildValidationRequestButton());
		buttons.add(buildValidationButton());
		buttons.add(buildProcessButton());
		buttons.add(buildApprovalRequestButton());
		buttons.add(buildApprovalButton());
		buttons.add(buildPrintButton());
		buttons.add(buildDocumentsCertificateButton());
		buttons.add(buildFoldersCertificateButton());
		buttons.add(buildRemoveFoldersButton());
		buttons.add(buildAddFoldersButton());
		return buttons;
	}

	@Override
	public void updateProcessButtonState(boolean processable) {
		process.setEnabled(processable);
	}

	@Override
	public void setProcessable(FolderDetailVO folderVO) {
		removeFolderFromComponent(folderVO, excludedFolders, excludedFolderComponent);
		removeFolderFromComponent(folderVO, packageableFolders, packageableFolderComponent);
		addFolderToComponent(folderVO, processableFolders, processableFolderComponent);
	}

	@Override
	public void setPackageable(FolderDetailVO folderVO) {
		removeFolderFromComponent(folderVO, excludedFolders, excludedFolderComponent);
		removeFolderFromComponent(folderVO, processableFolders, processableFolderComponent);
		addFolderToComponent(folderVO, packageableFolders, packageableFolderComponent);
	}

	private void removeFolderFromComponent(FolderDetailVO folder, BaseTable table, Component component) {
		if (table.containsId(folder)) {
			table.removeItem(folder);
			table.setCaption($("DecommissioningListView.folderDetails", table.size()));
			table.setPageLength(table.size());
			component.setVisible(table.size() > 0);
		}
	}

	private void addFolderToComponent(FolderDetailVO folder, BaseTable table, Component component) {
		if (!table.containsId(folder)) {
			table.addItem(folder);
			table.setCaption($("DecommissioningListView.folderDetails", table.size()));
			table.setPageLength(table.size());
			component.setVisible(true);
		} else {
			table.refreshRowCache();
		}
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

			@Override
			protected String getConfirmDialogMessage() {
				return presenter.getDeleteConfirmMessage();
			}
		};
		button.setEnabled(presenter.isDeletable());
		return button;
	}

	private Button buildValidationRequestButton() {
		validationRequest = new DecomValidationRequestWindowButton(presenter);
		validationRequest.setEnabled(presenter.canSendValidationRequest());
		validationRequest.addStyleName(VALIDATION_REQUEST_BUTTON);
		return validationRequest;
	}

	private Button buildRemoveFoldersButton() {
		removeFolders = new DeleteButton($("DecommissioningListView.removeFromList")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				List<FolderDetailVO> selected = new ArrayList<>();
				for (Object itemId : packageableFolders.getItemIds()) {
					FolderDetailVO folder = (FolderDetailVO) itemId;
					if (folder.isSelected()) {
						folder.setSelected(false);
						selected.add(folder);
					}
				}
				for (Object itemId : processableFolders.getItemIds()) {
					FolderDetailVO folder = (FolderDetailVO) itemId;
					if (folder.isSelected()) {
						folder.setSelected(false);
						selected.add(folder);
					}
				}
				for (Object itemId : excludedFolders.getItemIds()) {
					FolderDetailVO folder = (FolderDetailVO) itemId;
					if (folder.isSelected()) {
						folder.setSelected(false);
						selected.add(folder);
					}
				}
				presenter.removeFoldersButtonClicked(selected);
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("DecommissioningListView.removeFromListConfirmation");
			}

			@Override
			public boolean isEnabled() {
				return !presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed();
			}
		};
		return removeFolders;
	}

	private Button buildAddFoldersButton() {
		addFolders = new AddButton($("DecommissioningListView.addToList")) {
			@Override
			protected void buttonClick(ClickEvent clickEvent) {
				presenter.addFoldersButtonClicked();
			}

			@Override
			public boolean isEnabled() {
				return !presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed();
			}
		};
		return addFolders;
	}

	private Button buildValidationButton() {
		validation = new ConfirmDialogButton(null, $("DecommissioningListView.validate"), false) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DecommissioningListView.confirmValidation");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.validateButtonClicked();
			}
		};
		validation.setEnabled(presenter.canValidate());
		validation.addStyleName(VALIDATION_BUTTON);
		return validation;
	}

	private Button buildProcessButton() {
		process = new ConfirmDialogButton(null, $("DecommissioningListView.process"), false) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DecommissioningListView.confirmProcessing");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.processButtonClicked();
			}
		};
		process.setEnabled(presenter.isProcessable());
		process.addStyleName(PROCESS);
		return process;
	}

	private Button buildApprovalRequestButton() {
		approvalRequest = new ConfirmDialogButton(null, $("DecommissioningListView.approvalRequest"), false) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DecommissioningListView.confirmApprovalRequest");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.approvalRequestButtonClicked();
			}
		};
		approvalRequest.setEnabled(presenter.canSendApprovalRequest());
		approvalRequest.addStyleName(APPROVAL_REQUEST_BUTTON);
		return approvalRequest;
	}

	private Button buildApprovalButton() {
		approval = new ConfirmDialogButton(null, $("DecommissioningListView.approve"), false) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DecommissioningListView.confirmApproval");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.approvalButtonClicked();
			}
		};
		approval.setEnabled(presenter.canApprove());
		approval.addStyleName(APPROVAL_BUTTON);
		return approval;
	}

	private Button buildPrintButton() {
		ReportButton button = new ReportButton("Reports.DecommissioningList", presenter);
		button.setCaption($("DecommissioningListView.print"));
		button.addStyleName(ValoTheme.BUTTON_LINK);
		return button;
	}

	private Button buildDocumentsCertificateButton() {
		ContentButton button = new ContentButton($("DecommissioningListView.documentsCertificate"),
				presenter.getDocumentsReportContentId(),
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

	private Button buildFoldersCertificateButton() {
		ContentButton button = new ContentButton($("DecommissioningListView.foldersCertificate"),
				presenter.getFoldersReportContentId(),
				presenter.getFoldersReportContentName()) {
			@Override
			public boolean isVisible() {
				return presenter.isFoldersCertificateButtonVisible();
			}

		};
		button.setCaption($("DecommissioningListView.foldersCertificate"));
		button.addStyleName(ValoTheme.BUTTON_LINK);
		return button;
	}

	private Component buildValidatorsComponent(List<DecomListValidation> requests) {
		Label header = new Label($("DecommissioningListView.validators"));
		header.addStyleName(ValoTheme.LABEL_H2);

		validations = buildValidationTable(requests);

		VerticalLayout layout = new VerticalLayout(header, validations);
		layout.setSpacing(true);

		return layout;
	}

	private BaseTable buildValidationTable(List<DecomListValidation> requests) {
		BeanItemContainer<DecomListValidation> container = new BeanItemContainer<>(DecomListValidation.class, requests);

		BaseTable table = new BaseTable("DecommissioningListView.validationTable", $("DecommissioningListView.validations", container.size()), container);
		table.setPageLength(container.size());
		table.setWidth("100%");

		return new ValidationsGenerator(presenter).attachTo(table);
	}

	private Component buildPackageableFolderComponent(List<FolderDetailVO> folders) {
		Label header = new Label($("DecommissioningListView.containerizableFolders"));
		header.addStyleName(ValoTheme.LABEL_H2);

		Label label = new Label($("DecommissioningListView.containerSelector"));
		final ComboBox container = buildContainerSelector();
		if (container.size() > 0) {
			container.setValue(containerVOs.getIdByIndex(container.size() - 1));
		}

		Button placeFolders = new Button($("DecommissioningListView.placeInContainer"));
		placeFolders.setEnabled(container.size() > 0);
		placeFolders.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				List<FolderDetailVO> selected = new ArrayList<>();
				for (Object itemId : packageableFolders.getItemIds()) {
					FolderDetailVO folder = (FolderDetailVO) itemId;
					if (folder.isSelected()) {
						folder.setSelected(false);
						selected.add(folder);
					}
				}
				ContainerVO containerVO = (ContainerVO) container.getValue();
				for (FolderDetailVO folder : selected) {
					presenter.folderPlacedInContainer(folder, containerVO);
				}
			}
		});

		Button createContainer = new Button($("DecommissioningListView.createContainer"));
		createContainer.addStyleName(ValoTheme.BUTTON_LINK);
		createContainer.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containerCreationRequested();
			}
		});

		Button searchContainer = new Button($("DecommissioningListView.searchContainer"));
		searchContainer.addStyleName(ValoTheme.BUTTON_LINK);
		searchContainer.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containerSearchRequested();
			}
		});

		HorizontalLayout controls = new HorizontalLayout(label, container, placeFolders, createContainer, searchContainer);
		controls.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		controls.setSpacing(true);
		controls.setVisible(presenter.shouldAllowContainerEditing());

		packageableFolders = buildFolderTable(folders, presenter.shouldAllowContainerEditing());

		VerticalLayout layout = new VerticalLayout(header, controls, packageableFolders);
		layout.setSpacing(true);

		return layout;
	}

	private Component buildFoldersToValidateComponent(List<FolderDetailVO> folders) {
		Label header = new Label($("DecommissioningListView.foldersToValidate"));
		header.addStyleName(ValoTheme.LABEL_H2);

		foldersToValidate = buildFolderTable(folders, false);

		VerticalLayout layout = new VerticalLayout(header, foldersToValidate);
		layout.setSpacing(true);

		return layout;
	}

	private Component buildProcessableFolderComponent(List<FolderDetailVO> folders) {
		Label header = new Label(presenter.isProcessed() ?
				$("DecommissioningListView.processedFolders") :
				$("DecommissioningListView.processableFolders"));
		header.addStyleName(ValoTheme.LABEL_H2);

		processableFolders = buildFolderTable(folders, presenter.shouldAllowContainerEditing());

		VerticalLayout layout = new VerticalLayout(header, processableFolders);
		layout.setSpacing(true);

		return layout;
	}

	private Component buildExcludedFolderComponent(List<FolderDetailVO> folders) {
		Label header = new Label($("DecommissioningListView.excludedFolders"));
		header.addStyleName(ValoTheme.LABEL_H2);

		excludedFolders = buildFolderTable(folders, false);

		VerticalLayout layout = new VerticalLayout(header, excludedFolders);
		layout.setSpacing(true);

		return layout;
	}

	private BaseTable buildFolderTable(List<FolderDetailVO> folders, boolean containerizable) {
		BeanItemContainer<FolderDetailVO> container = new BeanItemContainer<>(FolderDetailVO.class, folders);
		BaseTable table = new BaseTable("DecommissioningListView.folderTable", $("DecommissioningListView.folderDetails", container.size()), container);
		table.setPageLength(container.size());
		table.setWidth("100%");

		return new FolderDetailTableGenerator(presenter, this, containerizable)
				.displayingRetentionRule(presenter.shouldDisplayRetentionRuleInDetails())
				.displayingCategory(presenter.shouldDisplayCategoryInDetails())
				.displayingSort(presenter.shouldDisplaySort())
				.displayingValidation(presenter.shouldDisplayValidation())
				.attachTo(table);
	}

	private Component buildContainerComponent(List<DecomListContainerDetail> containerDetails) {
		Label header = new Label($("DecommissioningListView.containers"));
		header.addStyleName(ValoTheme.LABEL_H2);

		BaseTable containers = buildContainerTable(containerDetails);

		VerticalLayout layout = new VerticalLayout(header, containers);
		layout.setSpacing(true);

		return layout;
	}

	private BaseTable buildContainerTable(List<DecomListContainerDetail> containers) {
		BeanItemContainer<DecomListContainerDetail> container = new BeanItemContainer<>(
				DecomListContainerDetail.class, containers);

		BaseTable table = new BaseTable("DecommissioningListView.containerDetails", $("DecommissioningListView.containerDetails", container.size()), container);
		table.setPageLength(container.size());
		table.setWidth("100%");

		return new ContainerDetailTableGenerator(presenter).attachTo(table);
	}

	public ComboBox buildContainerSelector() {
		ComboBox container = new ComboBox();
		container.setContainerDataSource(containerVOs);
		container.setItemCaptionPropertyId("caption");
		container.setNullSelectionAllowed(false);
		return container;
	}
}
