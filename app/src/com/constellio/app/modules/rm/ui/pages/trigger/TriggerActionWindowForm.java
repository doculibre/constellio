package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.modules.rm.ui.field.TableAddRemoveTriggerActionFieldPresenter;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import org.jetbrains.annotations.NotNull;

import static com.constellio.app.ui.i18n.i18n.$;

public class TriggerActionWindowForm extends WindowButton {
	private RecordVO triggerActionVO;
	private TableAddRemoveTriggerActionFieldPresenter presenter;
	private boolean isAdd;
	private Layout mainLayout;
	private RecordForm actionTriggerForm;

	public TriggerActionWindowForm(TableAddRemoveTriggerActionFieldPresenter presenter, boolean isAdd) {
		super($("add"), $("TableAddRemoveTriggerActionField.addTriggerActionWindowTitle"), new WindowConfiguration(true, true, "500px", "500px"));
		this.presenter = presenter;
		this.isAdd = isAdd;
	}


	public void setTriggerActionVO(RecordVO triggerActionVO) {
		this.triggerActionVO = triggerActionVO;
	}


	@Override
	protected Component buildWindowContent() {
		getWindow().setHeightUndefined();
		mainLayout = new VerticalLayout();
		actionTriggerForm = getNewActionTriggerForm(triggerActionVO);
		mainLayout.addComponent(actionTriggerForm);
		mainLayout.setHeightUndefined();
		return mainLayout;
	}

	@NotNull
	private RecordForm getNewActionTriggerForm(RecordVO triggerActionVO) {

		RecordForm recordForm = new RecordForm(triggerActionVO, presenter.getConstellioFactories()) {

			@Override
			protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
				presenter.addUpdateActionTrigger(viewObject);
				TriggerActionWindowForm.this.getWindow().close();
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				TriggerActionWindowForm.this.getWindow().close();
			}
		};

		recordForm.setHeightUndefined();
		Field<?> typeField = recordForm.getField(TriggerAction.TYPE);
		typeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				recordForm.commit();
				try {
					RecordVO newActionTriggerVO = presenter.changeSchemaAfterTypeChange(triggerActionVO, isAdd);
					if (newActionTriggerVO != null) {
						RecordForm newActionTriggerForm = getNewActionTriggerForm(newActionTriggerVO);
						mainLayout.replaceComponent(actionTriggerForm, newActionTriggerForm);
						actionTriggerForm = newActionTriggerForm;
					}
				} catch (OptimisticLockException e) {
					// FIXME
					throw new RuntimeException(e);
				}
			}
		});
		return recordForm;
	}
}
