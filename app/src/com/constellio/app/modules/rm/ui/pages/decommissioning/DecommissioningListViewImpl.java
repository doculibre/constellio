/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.display.EnumWithSmallCodeDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DecommissioningListViewImpl extends BaseViewImpl implements DecommissioningListView {
	public static final String PROCESS = "process";

	private final DecommissioningListPresenter presenter;
	private RecordVO decommissioningList;
	private BeanItemContainer<ContainerVO> containerVOs;
	private Component packageableFolderComponent;
	private Table packageableFolders;
	private Component processableFolderComponent;
	private Table processableFolders;
	private Button process;

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
				navigateTo().decommissioning();
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

		List<FolderDetailVO> packageableFolders = presenter.getPackageableFolders();
		packageableFolderComponent = buildPackageableFolderComponent(packageableFolders);
		packageableFolderComponent.setVisible(!packageableFolders.isEmpty());

		List<FolderDetailVO> processableFolders = presenter.getProcessableFolders();
		processableFolderComponent = buildProcessableFolderComponent(processableFolders);
		processableFolderComponent.setVisible(!processableFolders.isEmpty());

		List<DecomListContainerDetail> containerDetails = presenter.getContainerDetails();
		Component containerComponent = buildContainerComponent(containerDetails);
		containerComponent.setVisible(!containerDetails.isEmpty());

		VerticalLayout layout = new VerticalLayout(
				display, packageableFolderComponent, processableFolderComponent, containerComponent);
		layout.setSpacing(true);
		layout.setWidth("100%");

		return layout;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(buildEditButton());
		buttons.add(buildDeleteButton());
		buttons.add(buildProcessButton());
		return buttons;
	}

	@Override
	public void updateProcessButtonState(boolean processable) {
		process.setEnabled(processable);
	}

	@Override
	public void setProcessable(FolderDetailVO folderVO) {
		removeFolderFromComponent(folderVO, packageableFolders, packageableFolderComponent);
		addFolderToComponent(folderVO, processableFolders, processableFolderComponent);
	}

	@Override
	public void setPackageable(FolderDetailVO folderVO) {
		removeFolderFromComponent(folderVO, processableFolders, processableFolderComponent);
		addFolderToComponent(folderVO, packageableFolders, packageableFolderComponent);
	}

	@Override
	public void removeFolder(FolderDetailVO folder) {
		removeFolderFromComponent(folder, packageableFolders, packageableFolderComponent);
		removeFolderFromComponent(folder, processableFolders, processableFolderComponent);
	}

	private void removeFolderFromComponent(FolderDetailVO folder, Table table, Component component) {
		if (table.containsId(folder)) {
			table.removeItem(folder);
			table.setCaption($("DecommissioningListView.folderDetails", table.size()));
			table.setPageLength(table.size());
			component.setVisible(table.size() > 0);
		}
	}

	private void addFolderToComponent(FolderDetailVO folder, Table table, Component component) {
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
		};
		button.setEnabled(presenter.isDeletable());
		return button;
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

	private Component buildProcessableFolderComponent(List<FolderDetailVO> folders) {
		Label header = new Label(presenter.isProcessed() ?
				$("DecommissioningListView.processedFolders") :
				$("DecommissioningListView.processableFolders"));
		header.addStyleName(ValoTheme.LABEL_H2);

		processableFolders = buildFolderTable(folders, false);

		VerticalLayout layout = new VerticalLayout(header, processableFolders);
		layout.setSpacing(true);

		return layout;
	}

	private Table buildFolderTable(List<FolderDetailVO> folders, boolean containerizable) {
		BeanItemContainer<FolderDetailVO> container = new BeanItemContainer<>(FolderDetailVO.class, folders);
		Table table = new Table($("DecommissioningListView.folderDetails", container.size()), container);
		table.setPageLength(container.size());
		table.setWidth("100%");

		FolderDetailTableGenerator generator = new FolderDetailTableGenerator(containerizable)
				.displayingRetentionRule(presenter.shouldDisplayRetentionRuleInDetails())
				.displayingCategory(presenter.shouldDisplayCategoryInDetails())
				.displayingSort(presenter.shouldDisplaySort());
		generator.attachTo(table);

		return table;
	}

	private Component buildContainerComponent(List<DecomListContainerDetail> containerDetails) {
		Label header = new Label($("DecommissioningListView.containers"));
		header.addStyleName(ValoTheme.LABEL_H2);

		Table containers = buildContainerTable(containerDetails);

		VerticalLayout layout = new VerticalLayout(header, containers);
		layout.setSpacing(true);

		return layout;
	}

	private Table buildContainerTable(List<DecomListContainerDetail> containers) {
		BeanItemContainer<DecomListContainerDetail> container = new BeanItemContainer<>(
				DecomListContainerDetail.class, containers);

		Table table = new Table($("DecommissioningListView.containerDetails", container.size()), container);
		table.setPageLength(container.size());
		table.setWidth("100%");

		ContainerDetailTableGenerator generator = new ContainerDetailTableGenerator();
		generator.attachTo(table);
		return table;
	}

	private ComboBox buildContainerSelector() {
		ComboBox container = new ComboBox();
		container.setContainerDataSource(containerVOs);
		container.setItemCaptionPropertyId("caption");
		container.setNullSelectionAllowed(false);
		return container;
	}

	public class FolderDetailTableGenerator implements ColumnGenerator {
		public static final String CHECKBOX = "checkbox";
		public static final String FOLDER_ID = "id";
		public static final String FOLDER = "folder";
		public static final String RETENTION_RULE = "rule";
		public static final String CATEGORY_CODE = "categoryCode";
		public static final String SORT = "SORT";
		public static final String MEDIUM = "medium";
		public static final String CONTAINER = "container";
		public static final String LINEAR_SIZE = "linearSize";
		public static final String REMOVE = "remove";

		private final boolean packageable;
		private boolean displayRetentionRule;
		private boolean displayCategory;
		private boolean displaySort;

		public FolderDetailTableGenerator(boolean packageable) {
			this.packageable = packageable;
			displayRetentionRule = false;
			displayCategory = true;
			displaySort = false;
		}

		public FolderDetailTableGenerator displayingRetentionRule(boolean displayRetentionRule) {
			this.displayRetentionRule = displayRetentionRule;
			return this;
		}

		public FolderDetailTableGenerator displayingCategory(boolean displayCategory) {
			this.displayCategory = displayCategory;
			return this;
		}

		public FolderDetailTableGenerator displayingSort(boolean displaySort) {
			this.displaySort = displaySort;
			return this;
		}

		public void attachTo(Table table) {
			List<String> visibleColumns = new ArrayList<>();

			if (packageable) {
				table.addGeneratedColumn(CHECKBOX, this);
				table.setColumnHeader(CHECKBOX, "");
				table.setColumnAlignment(CHECKBOX, Align.CENTER);
				visibleColumns.add(CHECKBOX);
			}

			table.addGeneratedColumn(FOLDER_ID, this);
			table.setColumnHeader(FOLDER_ID, $("DecommissioningListView.folderDetails.id"));
			visibleColumns.add(FOLDER_ID);

			table.addGeneratedColumn(FOLDER, this);
			table.setColumnHeader(FOLDER, $("DecommissioningListView.folderDetails.folder"));
			table.setColumnExpandRatio(FOLDER, 1);
			visibleColumns.add(FOLDER);

			if (displaySort) {
				table.addGeneratedColumn(SORT, this);
				table.setColumnHeader(SORT, $("DecommissioningListView.sort." + presenter.getSortAction()));
				table.setColumnAlignment(SORT, Align.CENTER);
				visibleColumns.add(SORT);
			}

			if (displayRetentionRule) {
				table.addGeneratedColumn(RETENTION_RULE, this);
				table.setColumnHeader(RETENTION_RULE, $("DecommissioningListView.folderDetails.retentionRule"));
				visibleColumns.add(RETENTION_RULE);
			}

			if (displayCategory) {
				table.addGeneratedColumn(CATEGORY_CODE, this);
				table.setColumnHeader(CATEGORY_CODE, $("DecommissioningListView.folderDetails.categoryCode"));
				visibleColumns.add(CATEGORY_CODE);
				table.sort(new String[] { CATEGORY_CODE }, new boolean[] { true });
			}

			table.addGeneratedColumn(LINEAR_SIZE, this);
			table.setColumnHeader(LINEAR_SIZE, $("folderLinearSize"));
			visibleColumns.add(LINEAR_SIZE);

			table.addGeneratedColumn(MEDIUM, this);
			table.setColumnHeader(MEDIUM, $("DecommissioningListView.folderDetails.medium"));
			visibleColumns.add(MEDIUM);

			table.addGeneratedColumn(CONTAINER, this);
			table.setColumnHeader(CONTAINER, $("DecommissioningListView.folderDetails.container"));
			visibleColumns.add(CONTAINER);

			if (presenter.isEditable()) {
				table.addGeneratedColumn(REMOVE, this);
				table.setColumnHeader(REMOVE, "");
				visibleColumns.add(REMOVE);
			}

			table.setVisibleColumns(visibleColumns.toArray());
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			FolderDetailVO detail = (FolderDetailVO) itemId;

			switch ((String) columnId) {
			case CHECKBOX:
				return buildCheckBox(detail);
			case FOLDER_ID:
				return new Label(detail.getFolderId());
			case FOLDER:
				return new ReferenceDisplay(detail.getFolderId());
			case SORT:
				return buildSort(detail);
			case RETENTION_RULE:
				return new ReferenceDisplay(detail.getRetentionRuleId());
			case CATEGORY_CODE:
				return new Label(detail.getCategoryCode());
			case MEDIUM:
				return new EnumWithSmallCodeDisplay<>(detail.getMediumType());
			case CONTAINER:
				return buildContainer(detail);
			case LINEAR_SIZE:
				return buildLinearSize(detail);
			case REMOVE:
				return buildRemove(detail);
			}

			return null;
		}

		private Component buildCheckBox(final FolderDetailVO detail) {
			final CheckBox checkBox = new CheckBox();
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					detail.setSelected(checkBox.getValue());
				}
			});
			return checkBox;
		}

		private Component buildSort(FolderDetailVO detail) {
			if (detail.isSortable()) {
				return presenter.isEditable() ? buildSortCheckBox(detail) : buildSortDisplay(detail);
			}
			return null;
		}

		private Component buildSortCheckBox(final FolderDetailVO detail) {
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(detail.isReversedSort());
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.folderSorted(detail, checkBox.getValue());
				}
			});
			return checkBox;
		}

		private Component buildSortDisplay(FolderDetailVO detail) {
			return new Label($(detail.isReversedSort() ? "yes" : "no"));
		}

		private Component buildLinearSize(final FolderDetailVO detail) {
			if (detail.getMediumType() == FolderMediaType.ELECTRONIC || detail.getMediumType() == FolderMediaType.UNKNOWN) {
				return null;
			}

			if (!(presenter.shouldAllowContainerEditing() && detail.isPackageable())) {
				Double linearSize = presenter.getLinearSize(detail);
				if (linearSize == null) {
					return null;
				}
				return new Label(linearSize.toString());
			}

			final TextField linearSizeTextField = new TextField();
			Double linearSize = detail.getLinearSize();
			if (linearSize != null) {
				linearSizeTextField.setValue(linearSize.toString());
			}
			linearSizeTextField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.setFolderLinearSize(detail, linearSizeTextField.getValue());
				}
			});
			return linearSizeTextField;
		}

		private Component buildContainer(final FolderDetailVO detail) {
			if (detail.getMediumType() == FolderMediaType.ELECTRONIC) {
				return null;
			}

			if (!(presenter.shouldAllowContainerEditing() && detail.isPackageable())) {
				return new ReferenceDisplay(detail.getContainerRecordId());
			}

			final ComboBox container = buildContainerSelector();
			String containerId = detail.getContainerRecordId();
			if (containerId != null) {
				for (Object containerVO : container.getItemIds()) {
					if (((ContainerVO) containerVO).getId().equals(containerId)) {
						container.setValue(containerVO);
						break;
					}
				}
			}
			container.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.folderPlacedInContainer(detail, (ContainerVO) container.getValue());
				}
			});

			return container;
		}

		private Component buildRemove(final FolderDetailVO detail) {
			return new DeleteButton() {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.folderRemoved(detail);
				}
			};
		}
	}

	public class ContainerDetailTableGenerator implements ColumnGenerator {
		public static final String IDENTIFIER = "identifier";
		public static final String FULL = "full";

		public void attachTo(Table table) {
			table.addGeneratedColumn(IDENTIFIER, this);
			table.setColumnHeader(IDENTIFIER, $("DecommissioningListView.containerDetails.id"));
			table.setColumnExpandRatio(IDENTIFIER, 1);

			table.addGeneratedColumn(FULL, this);
			table.setColumnHeader(FULL, $("DecommissioningListView.containerDetails.full"));
			table.setColumnAlignment(FULL, Align.CENTER);

			table.setVisibleColumns(IDENTIFIER, FULL);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			DecomListContainerDetail detail = (DecomListContainerDetail) itemId;

			switch ((String) columnId) {
			case IDENTIFIER:
				return new ReferenceDisplay(detail.getContainerRecordId());
			case FULL:
				return presenter.isEditable() ? buildFullCheckBox(detail) : buildFullDisplay(detail);
			}

			return null;
		}

		private Component buildFullDisplay(DecomListContainerDetail detail) {
			return new Label($(detail.isFull() ? "yes" : "no"));
		}

		private Component buildFullCheckBox(final DecomListContainerDetail detail) {
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(detail.isFull());
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.containerStatusChanged(detail, checkBox.getValue());
				}
			});
			return checkBox;
		}
	}

}
