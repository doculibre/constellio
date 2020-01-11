package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.ui.components.actionParameters.DynamicParametersField;
import com.constellio.app.modules.robots.ui.components.criteria.AdvancedSearchCriteriaField;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditRobotViewImpl extends BaseViewImpl implements AddEditRobotView {
	private final AddEditRobotPresenter presenter;
	private RobotForm form;
	private RecordVO robot;

	public AddEditRobotViewImpl() {
		this.presenter = new AddEditRobotPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		robot = presenter.forParams(event.getParameters()).getRobot();
	}

	@Override
	protected String getTitle() {
		return $(presenter.isAddMode() ? "AddEditRobotView.addViewTitle" : "AddEditRobotView.editViewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		form = new RobotForm(robot);
		prepareSchemaFilterField(form.getSchemaFilterField());
		prepareActionField(form.getActionField());
		prepareActionParametersField(form.getActionParametersField());
		prepareAutoExecuteField(form.getAutoExecuteField());
		return form;
	}

	private void prepareSchemaFilterField(final Field<String> schemaFilter) {
		schemaFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.schemaFilterSelected(schemaFilter.getValue());
			}
		});
		schemaFilter.setEnabled(presenter.canEditSchemaFilter());
	}

	private void prepareActionField(final ComboBox action) {
		action.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.actionSelected((String) action.getValue());
			}
		});
	}

	private void prepareActionParametersField(DynamicParametersField parameters) {
		boolean enabled = presenter.requiresActionParameters();
		parameters.setEnabled(enabled);
		parameters.setRequired(enabled);
	}

	private void prepareAutoExecuteField(Field<?> autoExecute) {
		autoExecute.setVisible(presenter.canAutoExecute());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked(robot);
			}
		};
	}

	@Override
	public void setCriteriaSchema(String schemaType) {
		form.getSearchCriteriaField().setSchemaType(schemaType);
	}

	@Override
	public void addEmptyCriterion() {
		form.getSearchCriteriaField().addEmptyCriterion();
	}

	@Override
	public void setAvailableActions(List<Choice> choices) {
		ComboBox box = form.getActionField();
		box.removeAllItems();
		for (Choice choice : choices) {
			box.addItem(choice.getValue());
			box.setItemCaption(choice.getValue(), choice.getCaption());
		}
	}

	@Override
	public void setActionParametersFieldEnabled(boolean enabled) {
		form.getActionParametersField().setEnabled(enabled);
		form.getActionParametersField().setRequired(enabled);
	}

	@Override
	public void resetActionParameters(RecordVO record) {
		form.getActionParametersField().resetWithRecord(record);
	}

	public class RobotForm extends RecordForm {
		public RobotForm(RecordVO record) {
			super(record, new RobotMetadataFieldFactory(presenter));
		}

		@Override
		protected void saveButtonClick(RecordVO viewObject)
				throws ValidationException {
			presenter.saveButtonClicked(viewObject);
		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {
			presenter.backButtonClicked(viewObject);
		}

		@SuppressWarnings("unchecked")
		public Field<String> getSchemaFilterField() {
			return (Field<String>) getField(Robot.SCHEMA_FILTER);
		}

		@SuppressWarnings("unchecked")
		public AdvancedSearchCriteriaField getSearchCriteriaField() {
			return (AdvancedSearchCriteriaField) getField(Robot.SEARCH_CRITERIA);
		}

		public ComboBox getActionField() {
			return (ComboBox) getField(Robot.ACTION);
		}

		public Field<?> getAutoExecuteField() {
			return getField(Robot.AUTO_EXECUTE);
		}

		public DynamicParametersField getActionParametersField() {
			return (DynamicParametersField) getField(Robot.ACTION_PARAMETERS);
		}
	}

	public static class RobotMetadataFieldFactory extends OverridingMetadataFieldFactory<AddEditRobotPresenter> {

		public RobotMetadataFieldFactory(AddEditRobotPresenter presenter) {
			super(presenter);
		}

		@Override
		protected Field<?> newSingleValueField(MetadataVO metadata, String recordId) {
			if (Robot.ACTION_PARAMETERS.equals(metadata.getLocalCode())) {
				DynamicParametersField field = new DynamicParametersField(presenter);
				postBuild(field, metadata);
				return field;
			}
			return super.newSingleValueField(metadata, recordId);
		}

		@Override
		protected Field<?> newMultipleValueField(MetadataVO metadata, String recordId) {
			if (isCriteria(metadata)) {
				AdvancedSearchCriteriaField field = new AdvancedSearchCriteriaField(presenter)
						.setSchemaType(presenter.getSchemaFilter());
				postBuild(field, metadata);
				return field;
			}
			return super.newMultipleValueField(metadata, recordId);
		}

		private boolean isCriteria(MetadataVO metadata) {
			return metadata.getStructureFactory() != null && metadata.getStructureFactory() instanceof CriterionFactory;
		}
	}
}
