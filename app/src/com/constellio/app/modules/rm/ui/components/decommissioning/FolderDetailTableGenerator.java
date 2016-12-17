package com.constellio.app.modules.rm.ui.components.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
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

public class FolderDetailTableGenerator implements ColumnGenerator {
	public static final String CHECKBOX = "checkbox";
	public static final String FOLDER_ID = "id";
	public static final String FOLDER = "folder";
	public static final String RETENTION_RULE = "rule";
	public static final String CATEGORY_CODE = "categoryCode";
	public static final String SORT = "sort";
	public static final String MEDIUM = "medium";
	public static final String CONTAINER = "container";
	public static final String LINEAR_SIZE = "linearSize";
	public static final String VALIDATION_CHECKBOX = "validationCheckbox";

	private final DecommissioningListPresenter presenter;
	private final DecommissioningListViewImpl view;
	private final boolean packageable;
	private boolean displayRetentionRule;
	private boolean displayCategory;
	private boolean displaySort;
	private boolean displayValidation;

	public FolderDetailTableGenerator(DecommissioningListPresenter presenter, DecommissioningListViewImpl view,
			boolean packageable) {
		this.presenter = presenter;
		this.view = view;
		this.packageable = packageable;
		displayRetentionRule = false;
		displayCategory = true;
		displaySort = false;
		displayValidation = false;
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

	public BaseTable attachTo(BaseTable table) {
		List<String> visibleColumns = new ArrayList<>();
		boolean inValidationStatus = presenter.isInValidation();

		if (packageable) {
			table.addGeneratedColumn(CHECKBOX, this);
			table.setColumnHeader(CHECKBOX, "");
			table.setColumnAlignment(CHECKBOX, Align.CENTER);
			visibleColumns.add(CHECKBOX);
		}

		if (displayValidation) {
			table.addGeneratedColumn(VALIDATION_CHECKBOX, this);
			table.setColumnHeader(VALIDATION_CHECKBOX, $("DecommissioningListView.folderDetails.checkbox"));
			visibleColumns.add(VALIDATION_CHECKBOX);
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

		if (!inValidationStatus) {
			table.addGeneratedColumn(LINEAR_SIZE, this);
			table.setColumnHeader(LINEAR_SIZE, $("folderLinearSize"));
			visibleColumns.add(LINEAR_SIZE);
		}

		table.addGeneratedColumn(MEDIUM, this);
		table.setColumnHeader(MEDIUM, $("DecommissioningListView.folderDetails.medium"));
		visibleColumns.add(MEDIUM);

		if (!inValidationStatus) {
			table.addGeneratedColumn(CONTAINER, this);
			table.setColumnHeader(CONTAINER, $("DecommissioningListView.folderDetails.container"));
			visibleColumns.add(CONTAINER);
		}

		table.setVisibleColumns(visibleColumns.toArray());

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

		if (!(packageable && presenter.shouldAllowContainerEditing() && detail.isPackageable())) {
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
		included.setValue(detail.isFolderIncluded());
		included.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.setValidationStatus(detail, (boolean) included.getValue());
			}
		});
		return included;
	}

	private Component buildContainer(final FolderDetailVO detail) {
		if (detail.getMediumType() == FolderMediaType.ELECTRONIC) {
			return null;
		}

		String containerRecordId = detail.getContainerRecordId();
		if (containerRecordId != null && !(packageable && presenter.shouldAllowContainerEditing() && detail.isPackageable())) {
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
				presenter.folderPlacedInContainer(detail, (ContainerVO) container.getValue());
			}
		});

		return container;
	}
}
