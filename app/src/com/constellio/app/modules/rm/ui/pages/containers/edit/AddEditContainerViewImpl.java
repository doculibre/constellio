package com.constellio.app.modules.rm.ui.pages.containers.edit;

import com.constellio.app.modules.rm.ui.components.container.ContainerFormImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditContainerViewImpl extends BaseViewImpl implements AddEditContainerView {
	protected AddEditContainerPresenter presenter;

	private VerticalLayout layout;
	private ContainerFormImpl form;
	private RecordVO container;

	public AddEditContainerViewImpl() {
		presenter = new AddEditContainerPresenter(this);
	}

	@Override
	public void reloadWithContainer(RecordVO container) {
		this.container = container;
		ContainerFormImpl newForm = buildForm();
		layout.replaceComponent(form, newForm);
		form = newForm;
	}

	@Override
	public void setType(String type) {
		form.getTypeField().setValue(type);
	}

	@Override
	public RecordVO getUpdatedContainer() {
		form.commit();
		return container;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		container = presenter.forParams(event.getParameters()).getContainerRecord();
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelRequested();
			}
		};
	}

	@Override
	protected String getTitle() {
		return presenter.isEditMode() ? $("EditContainerViewImpl.editViewTitle"):$("EditContainerViewImpl.addViewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		form = buildForm();
		layout = new VerticalLayout(form);
		return layout;
	}

	private ContainerFormImpl buildForm() {
		ContainerFormImpl form = newForm();
		prepareTypeField(form.getTypeField());
		prepareDecommissioningTypeField(form.getDecommissioningTypeField());
		prepareAdministrativeUnitField(form.getAdministrativeUnitField());
		prepareCapacityField(form.getCapacityField());
		return form;
	}

	private void prepareTypeField(final Field<String> field) {
		field.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.typeSelected(field.getValue());
			}
		});
	}

	private void prepareCapacityField(final Field<String> field) {
		field.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String value = (String) event.getProperty().getValue();
				if(value == null) {
					container = getUpdatedContainer();
					container.set(ContainerRecord.CAPACITY, null);
					reloadWithContainer(container);
				} else if(value.matches("-?\\d+(\\.\\d+)?")) {
					container = getUpdatedContainer();
					container.set(ContainerRecord.CAPACITY, Double.parseDouble(value));
					reloadWithContainer(container);
				}
			}
		});
	}

	private void prepareDecommissioningTypeField(Field<String> field) {
		field.setVisible(presenter.canEditDecommissioningType());
	}

	private void prepareAdministrativeUnitField(Field<String> field) {
		field.setVisible(presenter.canEditAdministrativeUnit());
	}

	private ContainerFormImpl newForm() {
		return new ContainerFormImpl(container, presenter) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelRequested();
			}
		};
	}

	public Component buildMultipleModeWindowContent() {
		return new VerticalLayout();
	}
}
