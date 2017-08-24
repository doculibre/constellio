package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.decommissioning.ContainerDetailTableGenerator;
import com.constellio.app.modules.rm.ui.components.decommissioning.DecomValidationRequestWindowButton;
import com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator;
import com.constellio.app.modules.rm.ui.components.decommissioning.ValidationsGenerator;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPbutton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Component containerComponent;
	private BaseTable containerTable;
	private ComboBox containerComboBox;

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
		containerComponent = buildContainerComponent(containerDetails);
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
		buttons.add(buildAddFoldersButton());
		buttons.add(buildRemoveFoldersButton());
		buttons.add(buildCreateSIPARchivesButton());
		return buttons;
	}

	private Button buildOrderFoldersToValidateButton() {
		Button button = new LinkButton($("DecommissioningListView.order")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.reorderRequested(OrderDecommissioningListPresenter.TableType.TO_VALIDATE);
			}
		};
		return button;
	}

	private Button buildOrderProcessableFoldersButton() {
		Button button = new LinkButton($("DecommissioningListView.order")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.reorderRequested(OrderDecommissioningListPresenter.TableType.PROCESSABLE);
			}
		};
		return button;
	}

	private Button buildOrderPackageableFoldersButton() {
		Button button = new LinkButton($("DecommissioningListView.order")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.reorderRequested(OrderDecommissioningListPresenter.TableType.PACKAGEABLE);
			}
		};
		return button;
	}

	private Button buildOrderExcludedFoldersButton() {
		Button button = new LinkButton($("DecommissioningListView.order")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.reorderRequested(OrderDecommissioningListPresenter.TableType.EXCLUDED);
			}
		};
		return button;
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

	private void addContainerToComponent(DecomListContainerDetail newContainerDetail, BaseTable table, Component component) {
		boolean wasFound = false;
		for(Object object: table.getItemIds()) {
			DecomListContainerDetail detail = (DecomListContainerDetail) object;
			if(detail.getContainerRecordId().equals(newContainerDetail.getContainerRecordId())) {
				detail.setAvailableSize(newContainerDetail.getAvailableSize());
				wasFound = true;
				break;
			}
		}
		if(!wasFound) {
			table.addItem(newContainerDetail);
			table.setCaption($("DecommissioningListView.containerDetails", table.size()));
			table.setPageLength(table.size());
			component.setVisible(true);
		}
		table.refreshRowCache();
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
		removeFolders.setEnabled(!presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed());
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
		addFolders.setEnabled(!presenter.isInValidation() && !presenter.isApproved() && !presenter.isProcessed());
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
		containerComboBox = buildContainerSelector();
		if (containerComboBox.size() > 0) {
			containerComboBox.setValue(containerVOs.getIdByIndex(containerComboBox.size() - 1));
		}
		containerComboBox.setEnabled(presenter.canCurrentUserManageContainers());
		containerComboBox.setVisible(presenter.canCurrentUserManageContainers());

		Button placeFolders = new Button($("DecommissioningListView.placeInContainer"));
		placeFolders.setEnabled(containerComboBox.size() > 0 && presenter.canCurrentUserManageContainers());
		placeFolders.setVisible(presenter.canCurrentUserManageContainers());
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
				ContainerVO containerVO = (ContainerVO) containerComboBox.getValue();
				for (FolderDetailVO folder : selected) {
					try {
						presenter.folderPlacedInContainer(folder, getContainer(containerVO));
					} catch (Exception e) {
					}
				}
			}
		});


		Button createContainer = new Button($("DecommissioningListView.createContainer"));
		createContainer.addStyleName(ValoTheme.BUTTON_LINK);
		createContainer.setEnabled(presenter.canCurrentUserManageContainers());
		createContainer.setVisible(presenter.canCurrentUserManageContainers());
		createContainer.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containerCreationRequested();
			}
		});

		Button searchContainer = new Button($("DecommissioningListView.searchContainer"));
		searchContainer.addStyleName(ValoTheme.BUTTON_LINK);
		searchContainer.setEnabled(presenter.canCurrentUserManageContainers());
		searchContainer.setVisible(presenter.canCurrentUserManageContainers());
		searchContainer.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containerSearchRequested();
			}
		});

		Button autoFillContainers = new Button($("DecommissioningListView.autoFillContainers"));
		autoFillContainers.addStyleName(ValoTheme.BUTTON_LINK);
		autoFillContainers.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Map<String, Double> linearSizes = new HashMap<>();
				for (Object itemId : packageableFolders.getItemIds()) {
					FolderDetailVO currentFolder = (FolderDetailVO) itemId;
					if(currentFolder.getLinearSize() == null) {
						showErrorMessage($("DecommissioningListView.allFoldersMustHaveLinearSize"));
						return;
					}
					linearSizes.put(currentFolder.getFolderId(), currentFolder.getLinearSize());
				}
				presenter.autoFillContainersRequested(linearSizes);
				presenter.refreshView();
			}
		});
		autoFillContainers.setEnabled(presenter.canCurrentUserManageContainers());
		autoFillContainers.setVisible(presenter.canCurrentUserManageContainers());

		HorizontalLayout controls = new HorizontalLayout(label, containerComboBox, placeFolders, createContainer, searchContainer, autoFillContainers);
		controls.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		controls.setSpacing(true);
		controls.setVisible(presenter.shouldAllowContainerEditing());

		packageableFolders = buildFolderTable(folders, presenter.shouldAllowContainerEditing());

		VerticalLayout layout = new VerticalLayout(header, controls, packageableFolders, buildOrderPackageableFoldersButton());
		layout.setSpacing(true);

		return layout;
	}

	private Component buildFoldersToValidateComponent(List<FolderDetailVO> folders) {
		Label header = new Label($("DecommissioningListView.foldersToValidate"));
		header.addStyleName(ValoTheme.LABEL_H2);

		foldersToValidate = buildFolderTable(folders, true);

		VerticalLayout layout = new VerticalLayout(header, foldersToValidate, buildOrderFoldersToValidateButton());
		layout.setSpacing(true);

		return layout;
	}

	private Button buildCreateSIPARchivesButton(){
		SIPbutton button = new SIPbutton($("SIPButton.caption"), $("SIPButton.caption"), this);
		button.setAllObject(presenter.getFoldersVO().toArray(new FolderVO[0]));
		return button;
	}

	private Component buildProcessableFolderComponent(List<FolderDetailVO> folders) {
		Label header = new Label(presenter.isProcessed() ?
				$("DecommissioningListView.processedFolders") :
				$("DecommissioningListView.processableFolders"));
		header.addStyleName(ValoTheme.LABEL_H2);

		processableFolders = buildFolderTable(folders, presenter.shouldAllowContainerEditing());

		BaseButton removeFromTheBox = new BaseButton($("DecommissioningListView.removeFromFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				List<FolderDetailVO> selected = new ArrayList<>();
				boolean areAllSelectedPackageable = true;
				for (Object itemId : processableFolders.getItemIds()) {
					FolderDetailVO folder = (FolderDetailVO) itemId;
					if (folder.isSelected()) {
						folder.setSelected(false);
						selected.add(folder);
					}
				}
				for (FolderDetailVO folder : selected) {
					try {
						presenter.removeFromContainer(folder);
						if(folder.isPackageable()) {
							setPackageable(folder);
						} else {
							areAllSelectedPackageable = false;
						}
					} catch (Exception e) {
					}
				}
				if(!areAllSelectedPackageable) {
					showErrorMessage($("DecommissioningListView.someFoldersAreNotPackageable"));
				}
			}
		};
		removeFromTheBox.setEnabled(presenter.canCurrentUserManageContainers());
		removeFromTheBox.setVisible(presenter.canCurrentUserManageContainers() && presenter.shouldAllowContainerEditing() && !packageableFolders.isEmpty());

		VerticalLayout layout = new VerticalLayout(header);

		if(!presenter.isInValidation() && !presenter.isInApprobation()) {
			layout.addComponent(removeFromTheBox);
		}

		layout.addComponents(processableFolders, buildOrderProcessableFoldersButton());

		layout.setSpacing(true);

		return layout;
	}

	private Component buildExcludedFolderComponent(List<FolderDetailVO> folders) {
		Label header = new Label($("DecommissioningListView.excludedFolders"));
		header.addStyleName(ValoTheme.LABEL_H2);

		excludedFolders = buildFolderTable(folders, false);

		VerticalLayout layout = new VerticalLayout(header, excludedFolders, buildOrderExcludedFoldersButton());
		layout.setSpacing(true);

		return layout;
	}

	private BaseTable buildFolderTable(List<FolderDetailVO> folders, boolean containerizable) {
		BeanItemContainer<FolderDetailVO> container = new BeanItemContainer<>(FolderDetailVO.class, folders);
		container.setItemSorter(buildItemSorter());
		BaseTable table = new BaseTable("DecommissioningListView.folderTable", $("DecommissioningListView.folderDetails", container.size()), container) {

		};
		table.setPageLength(container.size());
		table.setWidth("100%");

		return new FolderDetailTableGenerator(presenter, this, containerizable)
				.withExtension(presenter.getFolderDetailTableExtension())
				.displayingRetentionRule(presenter.shouldDisplayRetentionRuleInDetails())
				.displayingCategory(presenter.shouldDisplayCategoryInDetails())
				.displayingSort(presenter.shouldDisplaySort())
				.displayingValidation(presenter.shouldDisplayValidation())
				.displayingOrderNumber(true)
				.attachTo(table);
	}

	private DefaultItemSorter buildItemSorter() {
		if (presenter.getFolderDetailTableExtension() != null) {
			return 	new DefaultItemSorter() {
				@Override
				protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2) {
					// Get the properties to compare

					final Property<?> property1 = item1.getItemProperty(propertyId);
					final Property<?> property2 = item2.getItemProperty(propertyId);

					// Get the values to compare
					final Object value1 = (property1 == null) ? null : property1.getValue();
					final Object value2 = (property2 == null) ? null : property2.getValue();

					if(FolderDetailTableGenerator.FOLDER_ID.equals(propertyId) && StringUtils.isNumeric((String) value1) && StringUtils.isNumeric((String) value2)) {
						try {
							if(sortDirection) {
								return Integer.compare(Integer.parseInt((String) value1), Integer.parseInt((String) value2));
							} else {
								return Integer.compare(Integer.parseInt((String) value2), Integer.parseInt((String) value1));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					int returnedValue = super.compareProperty(propertyId, sortDirection, item1, item2);

					if (value1 == null) {
						if (value2 == null) {
							return 0;
						} else {
							return -returnedValue;
						}
					} else if (value2 == null) {
						return -returnedValue;
					}

					return returnedValue;
				}
			};
		} else {
			return 	new DefaultItemSorter() {
				@Override
				protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2) {
					// Get the properties to compare
					int returnedValue = super.compareProperty(propertyId, sortDirection, item1, item2);
					final Property<?> property1 = item1.getItemProperty(propertyId);
					final Property<?> property2 = item2.getItemProperty(propertyId);

					// Get the values to compare
					final Object value1 = (property1 == null) ? null : property1.getValue();
					final Object value2 = (property2 == null) ? null : property2.getValue();
					if (value1 == null) {
						if (value2 == null) {
							return 0;
						} else {
							return -returnedValue;
						}
					} else if (value2 == null) {
						return -returnedValue;
					}

					return returnedValue;
				}
			};
		}
	}

	private Component  buildContainerComponent(List<DecomListContainerDetail> containerDetails) {
		Label header = new Label($("DecommissioningListView.containers"));
		header.addStyleName(ValoTheme.LABEL_H2);

		containerTable = buildContainerTable(containerDetails);

		VerticalLayout layout = new VerticalLayout(header, containerTable);
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

	public FolderDetailVO getPackageableFolder(String folderId) {
		FolderDetailVO folder = null;
		for (Object itemId : packageableFolders.getItemIds()) {
			FolderDetailVO currentFolder = (FolderDetailVO) itemId;
			if(folderId.equals((currentFolder.getFolderId()))) {
				folder = currentFolder;
				break;
			}
		}
		return folder;
	}

	public ContainerVO getContainer(ContainerRecord containerRecord) {
		ContainerVO containerVO = null;
		for (Object itemId : containerVOs.getItemIds()) {
			ContainerVO currentContainer = (ContainerVO) itemId;
			if(containerRecord.getId().equals((currentContainer.getId()))) {
				containerVO = currentContainer;
				break;
			}
		}
		if(containerVO == null) {
			containerVO = new ContainerVO(containerRecord.getId(), containerRecord.getTitle(), containerRecord.getAvailableSize());
		}
		return containerVO;
	}

	public ContainerVO getContainer(ContainerVO containerRecord) {
		ContainerVO containerVO = null;
		for (Object itemId : containerVOs.getItemIds()) {
			ContainerVO currentContainer = (ContainerVO) itemId;
			if(containerRecord.getId().equals((currentContainer.getId()))) {
				containerVO = currentContainer;
				break;
			}
		}
		if(containerVO == null) {
			containerVO = new ContainerVO(containerRecord.getId(), containerRecord.getCaption(), containerRecord.getAvailableSize());
		}
		return containerVO;
	}

	public void addUpdateContainer(ContainerVO containerVO, DecomListContainerDetail newContainerDetail) {
		boolean wasFound = false;
		if(containerVO != null) {
			for(int i = 0; i < containerVOs.getItemIds().size(); i++) {
				if(containerVOs.getIdByIndex(i).getId().equals(containerVO.getId())) {
					containerVOs.addBean(containerVO);
					if(containerComboBox != null && containerComboBox.getValue().equals(containerVOs.getIdByIndex(i))) {
						containerComboBox.setValue(containerVO);
					}
					containerVOs.removeItem(containerVOs.getIdByIndex(i));
					wasFound = true;
					break;
				}
			}

			if(!wasFound) {
				containerVOs.addItem(containerVO);
				presenter.addContainerToDecommissioningList(containerVO);
				newContainerDetail = presenter.getContainerDetail(containerVO.getId());
			}
			addContainerToComponent(newContainerDetail, containerTable, containerComponent);
		}
	}
}
