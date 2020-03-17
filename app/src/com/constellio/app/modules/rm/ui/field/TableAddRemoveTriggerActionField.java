package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.modules.rm.ui.field.TableAddRemoveTriggerActionFieldPresenter.TriggerActionContainer;
import com.constellio.app.modules.rm.ui.pages.trigger.TriggerActionWindowDisplay;
import com.constellio.app.modules.rm.ui.pages.trigger.TriggerActionWindowForm;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class TableAddRemoveTriggerActionField extends CustomField<List<String>> {
	public static final String TRIGGER_ACTION_BUTTONS = "triggerActionButtons";

	private RecordVOTable recordVOTable;
	private WindowButton addAction;
	private TableAddRemoveTriggerActionFieldPresenter presenter;
	private TriggerActionContainer dataSource;

	public TableAddRemoveTriggerActionField(ConstellioFactories constellioFactories, RecordVO triggerRecordVO) {
		this.presenter = new TableAddRemoveTriggerActionFieldPresenter(this, constellioFactories, triggerRecordVO);
		this.dataSource = presenter.getCurrentContainer();
	}

	@Override
	protected Component initContent() {
		this.addAction = new TriggerActionWindowForm(presenter, true) {

			@Override
			protected Component buildWindowContent() {
				this.setTriggerActionVO(presenter.newTriggerAction());
				return super.buildWindowContent();
			}
		};

		addAction.addStyleName(ValoTheme.BUTTON_LINK);

		recordVOTable = new RecordVOTable(dataSource);
		recordVOTable.setWidth("100%");
		recordVOTable.setHeight("200px");

		recordVOTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVO triggerActions = (RecordVO) event.getItemId();

				getUI().addWindow(new TriggerActionWindowDisplay(triggerActions));
			}
		});

		recordVOTable.setColumnHeader(TriggerAction.TITLE, $("TableAddRemoveTriggerActionField.title"));
		recordVOTable.setColumnHeader(TriggerAction.TYPE, $("TableAddRemoveTriggerActionField.type"));
		recordVOTable.addGeneratedColumn(TRIGGER_ACTION_BUTTONS, new ActionColumnGenerator());
		recordVOTable.setColumnWidth(TRIGGER_ACTION_BUTTONS, 90);
		recordVOTable.setColumnHeader(TRIGGER_ACTION_BUTTONS, "");

		VerticalLayout mainVLayout = new VerticalLayout();
		mainVLayout.setWidth("100%");
		mainVLayout.addComponents(addAction, recordVOTable);
		mainVLayout.setExpandRatio(recordVOTable, 1);
		mainVLayout.setComponentAlignment(addAction, Alignment.MIDDLE_RIGHT);


		return mainVLayout;
	}

	public class ActionColumnGenerator implements ColumnGenerator {
		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			RecordVO triggerActionVO = (RecordVO) itemId;


			Button editButton = new EditButton() {
				@Override
				protected void buttonClick(ClickEvent event) {
					TriggerActionWindowForm triggerActionWindowForm = new TriggerActionWindowForm(presenter, false);
					triggerActionWindowForm.setTriggerActionVO(triggerActionVO);
					triggerActionWindowForm.click();
				}
			};

			Button deleteButton = new DeleteButton() {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteTriggerAction(triggerActionVO);
				}
			};

			HorizontalLayout buttonMainHLayout = new HorizontalLayout();

			buttonMainHLayout.addComponents(editButton, deleteButton);

			return buttonMainHLayout;
		}
	}

	public List<RecordVO> getTriggerActionVOListToSave() {
		return presenter.getTriggerActionVOListToSave();
	}

	public List<RecordVO> getTriggerActionVOListToDelete() {
		return presenter.getTriggerActionVOListToDelete();
	}

	public TriggerActionContainer getTriggerContainer() {
		return dataSource;
	}

	@Override
	protected void setInternalValue(List<String> newValue) {
		super.setInternalValue(newValue);
		if (newValue == null || newValue.isEmpty()) {
			dataSource.removeAllItems();
			dataSource.forceRefresh();
		} else {
			dataSource.setContent(newValue);
		}
	}

	@Override
	protected List<String> getInternalValue() {
		if (dataSource != null) {
			return dataSource.getItemIds().stream().map(element -> ((RecordVO) element).getId()).collect(Collectors.toList());
		} else {
			return Arrays.asList();
		}
	}

	@Override
	public Class<? extends List<String>> getType() {
		return (Class) List.class;
	}
}
