package com.constellio.app.modules.rm.ui.components.decommissioning;

import com.constellio.app.modules.rm.extensions.api.DecommissioningListFolderTableExtension;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleReferenceDisplay;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.ui.framework.components.display.EnumWithSmallCodeDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BooleanOptionGroup;
import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderDetailTableGenerator implements ColumnGenerator {
	public static final String CHECKBOX = "checkbox";
	public static final String FOLDER_ID = "folderId";
	public static final String PREVIOUS_ID = "previousId";
	public static final String FOLDER = "folder";
	public static final String RETENTION_RULE = "rule";
	public static final String CATEGORY_CODE = "categoryCode";
	public static final String SORT = "sort";
	public static final String ORDER = "order";
	public static final String MEDIUM = "medium";
	public static final String CONTAINER = "container";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String VALIDATION_CHECKBOX = "validationCheckbox";

	private final DecommissioningListPresenter presenter;
	private final DecommissioningListViewImpl view;
	private final boolean packageable;
	private DecommissioningListFolderTableExtension extension;
	private boolean displayRetentionRule;
	private boolean displayCategory;
	private boolean displaySort;
	private boolean displayValidation;
	private boolean displayOrderNumber;
	private Map<FolderDetailVO, Component> checkBoxMap;
	private Set<FolderDetailVO> selected;

	public FolderDetailTableGenerator(DecommissioningListPresenter presenter, DecommissioningListViewImpl view,
									  boolean packageable) {
		this.presenter = presenter;
		this.view = view;
		this.packageable = packageable;
		displayRetentionRule = false;
		displayCategory = true;
		displaySort = false;
		displayValidation = false;
		checkBoxMap = new HashMap<>();
		selected = new HashSet<>();
	}

	public FolderDetailTableGenerator withExtension(DecommissioningListFolderTableExtension extension) {
		this.extension = extension;
		return this;
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

	public FolderDetailTableGenerator displayingValidation(boolean displayValidation) {
		this.displayValidation = displayValidation;
		return this;
	}

	public FolderDetailTableGenerator displayingOrderNumber(boolean displayOrderNumber) {
		this.displayOrderNumber = displayOrderNumber;
		return this;
	}

	public BaseTable attachTo(BaseTable table) {
		List<String> visibleColumns = new ArrayList<>();
		boolean inValidationStatus = presenter.isInValidation();

		boolean askedForApprobation = presenter.isInApprobation() && !presenter.canApprove();
		if (!(askedForApprobation || presenter.isApproved() || presenter.isProcessed())) {
			table.addGeneratedColumn(CHECKBOX, this);
			table.setColumnHeader(CHECKBOX, "");
			table.setColumnAlignment(CHECKBOX, Align.CENTER);
			visibleColumns.add(CHECKBOX);
		}

		if (displayOrderNumber) {
			table.addGeneratedColumn(ORDER, this);
			table.setColumnHeader(ORDER, "#");
			table.setColumnAlignment(ORDER, Align.CENTER);
			visibleColumns.add(ORDER);
		}

		if (displayValidation) {
			table.addGeneratedColumn(VALIDATION_CHECKBOX, this);
			table.setColumnHeader(VALIDATION_CHECKBOX, $("DecommissioningListView.folderDetails.checkbox"));
			visibleColumns.add(VALIDATION_CHECKBOX);
		}

		table.addGeneratedColumn(FOLDER_ID, this);
		table.setColumnHeader(FOLDER_ID, $("DecommissioningListView.folderDetails.id"));
		visibleColumns.add(FOLDER_ID);

		if (extension != null) {
			table.addGeneratedColumn(PREVIOUS_ID, this);
			table.setColumnHeader(PREVIOUS_ID, $("DecommissioningListView.folderDetails.previousId"));
			visibleColumns.add(PREVIOUS_ID);
		}

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
		}

		table.addGeneratedColumn(LINEAR_SIZE, this);
		table.setColumnHeader(LINEAR_SIZE, $("folderLinearSize"));
		visibleColumns.add(LINEAR_SIZE);

		table.addGeneratedColumn(MEDIUM, this);
		table.setColumnHeader(MEDIUM, $("DecommissioningListView.folderDetails.medium"));
		visibleColumns.add(MEDIUM);

		if (presenter.canCurrentUserManageContainers() && !presenter.areContainersHidden()) {
			table.addGeneratedColumn(CONTAINER, this);
			table.setColumnHeader(CONTAINER, $("DecommissioningListView.folderDetails.container"));
			visibleColumns.add(CONTAINER);
		}

		table.setVisibleColumns(visibleColumns.toArray());

		if (extension != null) {
			table.sort(new String[]{CATEGORY_CODE, PREVIOUS_ID, FOLDER_ID}, new boolean[]{true, true, true});
		} else {
			table.sort(new String[]{CATEGORY_CODE}, new boolean[]{true});
		}
		return table;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		FolderDetailVO detail = (FolderDetailVO) itemId;

		switch ((String) columnId) {
			case CHECKBOX:
				return buildCheckBox(detail);
			case VALIDATION_CHECKBOX:
				return buildValidationColumn(detail);
			case FOLDER_ID:
				return new Label(detail.getFolderId());
			case PREVIOUS_ID:
				return new Label(extension.getPreviousId(detail));
			case FOLDER:
				return new ReferenceDisplay(detail.getFolderId());
			case SORT:
				return buildSort(detail);
			case RETENTION_RULE:
				return new RetentionRuleReferenceDisplay(detail.getRetentionRuleId());
			case CATEGORY_CODE:
				return new Label(detail.getCategoryCode());
			case MEDIUM:
				return new EnumWithSmallCodeDisplay<>(detail.getMediumType());
			case CONTAINER:
				return buildContainer(detail);
			case LINEAR_SIZE:
				return buildLinearSize(detail);
			case ORDER:
				return buildOrderNumber(detail);
		}

		return null;
	}

	private Object buildOrderNumber(FolderDetailVO detail) {
		return new Label(presenter.getOrderNumber(detail.getFolderId()));
	}

	private Component buildCheckBox(final FolderDetailVO detail) {
		final CheckBox checkBox = new CheckBox();
		checkBox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				detail.setSelected(checkBox.getValue());
				if (checkBox.getValue()) {
					selected.add(detail);
				} else {
					selected.remove(detail);
				}
				view.refreshButtons(selected.size(), detail);
			}
		});
		checkBoxMap.put(detail, checkBox);
		checkBox.setValue(detail.isSelected());
		return checkBox;
	}

	public Component getCheckBox(FolderDetailVO folderDetailVO) {
		return checkBoxMap.get(folderDetailVO);
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

		if (!(packageable && presenter.shouldAllowContainerEditing() && detail.isPackageable() && !presenter.isInValidation())) {
			Double linearSize = presenter.getLinearSize(detail);
			if (linearSize == null) {
				return null;
			}
			return new Label(linearSize.toString());
		}

		final BaseDoubleField field = new BaseDoubleField();
		field.setConvertedValue(detail.getLinearSize());
		field.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					presenter.linearSizeUpdated(detail, field.getConvertedValue());
				} catch (ConversionException e) {
					// Do nothing - the field will become hilighted in red
				}
			}
		});
		return field;
	}

	private Component buildValidationColumn(final FolderDetailVO detail) {
		final BooleanOptionGroup included = new BooleanOptionGroup();
		if (FolderDetailStatus.INCLUDED.equals(detail.getFolderDetailStatus())) {
			included.setValue(true);
		}
		if (FolderDetailStatus.EXCLUDED.equals(detail.getFolderDetailStatus())) {
			included.setValue(false);
		}
		included.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (!(boolean) included.getValue()) {
					presenter.removeFromContainer(detail);
				}
				presenter.setValidationStatusAndRefreshView(detail, (boolean) included.getValue());
			}
		});
		return included;
	}

	private Component buildContainer(final FolderDetailVO detail) {
		if (detail.getMediumType() == FolderMediaType.ELECTRONIC) {
			return null;
		}

		String containerRecordId = detail.getContainerRecordId();
		if (containerRecordId != null && !(packageable && presenter.shouldAllowContainerEditing() && detail.isPackageable() && !presenter.isInValidation())) {
			return new ReferenceDisplay(containerRecordId);
		}

		final ComboBox container = view.buildContainerSelector();
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
				try {
					presenter.folderPlacedInContainer(detail, view.getContainer((ContainerVO) container.getValue()));
				} catch (Exception e) {
					container.setValue(null);
				}
			}
		});

		return container;
	}
}
