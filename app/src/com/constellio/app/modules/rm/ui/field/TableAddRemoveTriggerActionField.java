package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.modules.rm.ui.field.TableAddRemoveTriggerActionFieldPresenter.TriggerContainer;
import com.constellio.app.modules.rm.ui.pages.trigger.TriggerActionWindowForm;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableAddRemoveTriggerActionField extends CustomField<List<String>> {

	private ConstellioFactories constellioFactories;
	private RecordVOTable recordVOTable;
	private WindowButton addAction;
	private TableAddRemoveTriggerActionFieldPresenter presenter;
	private TriggerContainer dataSource;

	public TableAddRemoveTriggerActionField(ConstellioFactories constellioFactories, RecordVO triggerRecordVO) {
		this.constellioFactories = constellioFactories;
		this.presenter = new TableAddRemoveTriggerActionFieldPresenter(this, constellioFactories, triggerRecordVO);
		this.dataSource = presenter.getContainer();
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
		VerticalLayout mainVLayout = new VerticalLayout();
		mainVLayout.setWidth("100%");
		mainVLayout.addComponents(addAction, recordVOTable);
		mainVLayout.setExpandRatio(recordVOTable, 1);
		mainVLayout.setComponentAlignment(addAction, Alignment.MIDDLE_RIGHT);


		return mainVLayout;
	}

	public List<RecordVO> getTriggerActionToSave() {
		return presenter.getTriggerActionToSave();
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
