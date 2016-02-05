package com.constellio.app.modules.rm.ui.components.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.BooleanLabel;
import com.constellio.app.ui.framework.components.LocalDateLabel;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

public class ValidationsGenerator implements ColumnGenerator {
	public static final String VALIDATOR = "validator";
	public static final String VALIDATION_REQUEST_DATE = "validationRequestDate";
	public static final String VALIDATED = "validated";
	public static final String VALIDATION_DATE = "validationDate";
	public static final String REMOVE = "remove";

	private final DecommissioningListPresenter presenter;

	public ValidationsGenerator(DecommissioningListPresenter presenter) {
		this.presenter = presenter;
	}

	public Table attachTo(Table table) {
		table.addGeneratedColumn(VALIDATOR, this);
		table.setColumnHeader(VALIDATOR, $("DecomValidatorsTable.username"));
		table.setColumnExpandRatio(VALIDATOR, 1);

		table.addGeneratedColumn(VALIDATION_REQUEST_DATE, this);
		table.setColumnHeader(VALIDATION_REQUEST_DATE, $("DecomValidatorsTable.requestDate"));

		table.addGeneratedColumn(VALIDATED, this);
		table.setColumnHeader(VALIDATED, $("DecomValidatorsTable.valid"));

		table.addGeneratedColumn(VALIDATION_DATE, this);
		table.setColumnHeader(VALIDATION_DATE, $("DecomValidatorsTable.validationDate"));

		if (presenter.canRemoveValidationRequest()) {
			table.addGeneratedColumn(REMOVE, this);
			table.setColumnHeader(REMOVE, "");
			table.setColumnWidth(REMOVE, 50);

			table.setVisibleColumns(
					VALIDATOR, VALIDATION_REQUEST_DATE, VALIDATED, VALIDATION_DATE, REMOVE);
		} else {
			table.setVisibleColumns(VALIDATOR, VALIDATION_REQUEST_DATE, VALIDATED, VALIDATION_DATE);
		}

		return table;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		DecomListValidation validation = (DecomListValidation) itemId;

		switch ((String) columnId) {
		case VALIDATOR:
			return new ReferenceDisplay(validation.getUserId());
		case VALIDATION_REQUEST_DATE:
			return new LocalDateLabel(validation.getRequestDate());
		case VALIDATED:
			return new BooleanLabel(validation.isValidated());
		case VALIDATION_DATE:
			return new LocalDateLabel(validation.getValidationDate());
		case REMOVE:
			return buildRemove(validation);
		}

		return null;
	}

	private Component buildRemove(final DecomListValidation validation) {
		DeleteButton button = new DeleteButton() {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.validationRemoved(validation);
			}
		};
		button.setEnabled(!validation.isValidated());
		return button;
	}
}
