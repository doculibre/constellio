package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class WizardConnectorInstanceViewImpl extends BaseViewImpl implements WizardConnectorInstanceView {

	private VerticalLayout mainLayout;
	private RecordVODataProvider connectorTypeDataProvider;
	private RecordVO recordVO;
	private RecordComboBox connectorTypeField;
	private RecordForm connectorForm;

	private WizardConnectorInstancePresenter presenter;

	public WizardConnectorInstanceViewImpl() {
		presenter = new WizardConnectorInstancePresenter(this);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		connectorTypeField = new RecordComboBox(ConnectorType.DEFAULT_SCHEMA);
		connectorTypeField.setCaption($("WizardConnectorInstanceView.connectorType"));
		connectorTypeField.setDataProvider(connectorTypeDataProvider);
		connectorTypeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String connectorTypeId = (String) connectorTypeField.getValue();
				presenter.connectorTypeSelected(connectorTypeId);
			}
		});

		mainLayout.addComponents(connectorTypeField);
		if (recordVO != null) {
			connectorForm = newForm();
			mainLayout.addComponent(connectorForm);
		}
		return mainLayout;
	}

	@Override
	public void setConnectorTypeDataProvider(RecordVODataProvider connectorTypeDataProvider) {
		this.connectorTypeDataProvider = connectorTypeDataProvider;
	}

	@Override
	public void refreshConnectorForm() {
		if (recordVO == null) {
			mainLayout.removeComponent(connectorForm);
			connectorForm = null;
		} else {
			RecordForm newConnectorForm = newForm();
			if (connectorForm != null) {
				mainLayout.replaceComponent(connectorForm, newConnectorForm);
			} else {
				mainLayout.addComponent(newConnectorForm);
			}
			connectorForm = newConnectorForm;
		}
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	private RecordForm newForm() {
		RecordForm recordForm = new RecordForm(recordVO, new ConnectorInstanceFieldFactory(), getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
		return recordForm;
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

}
