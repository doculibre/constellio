package com.constellio.app.modules.es.ui.pages.mapping;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.TargetParams;
import com.constellio.app.modules.es.ui.entities.MappingVO;
import com.constellio.app.modules.es.ui.entities.MappingVO.FieldMapper;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
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
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AddEditMappingViewImpl extends BaseViewImpl implements AddEditMappingView {
	private final AddEditMappingPresenter presenter;
	private RecordVO instance;
	private MappingVO mapping;
	private ComboBox target;
	private VerticalLayout sources;
	private Button save;

	public AddEditMappingViewImpl() {
		presenter = new AddEditMappingPresenter(this);
	}

	@Override
	public void resetSources() {
		sources.removeAllComponents();
		buildFieldSelector(sources);
		save.setEnabled(presenter.canSave(mapping));
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		instance = presenter.forParams(event.getParameters()).getConnectorInstance();
		mapping = presenter.getMapping();
	}

	@Override
	protected String getTitle() {
		return presenter.isEditMode() ?
				$("AddEditMappingView.editViewTitle", instance.getTitle(), mapping.getMetadataLabel()) :
				$("AddEditMappingView.addViewTitle", instance.getTitle());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		sources = new VerticalLayout();
		sources.setSpacing(true);
		sources.setSizeFull();
		VerticalLayout layout = new VerticalLayout(buildMetadataChooser(), buildFieldSelector(sources), buildButtons());
		layout.setSpacing(true);
		layout.setSizeFull();
		return layout;
	}

	private Component buildMetadataChooser() {
		Label caption = new Label($("AddEditMappingView.metadata"));
		caption.addStyleName(ValoTheme.LABEL_BOLD);

		target = new ComboBox();
		target.setNullSelectionAllowed(false);
		for (MetadataVO metadata : presenter.getAvailableTargetMetadata()) {
			target.addItem(metadata);
			target.setItemCaption(metadata, metadata.getLabel());
			if (metadata.codeMatches(mapping.getMetadataLocalCode())) {
				target.select(metadata);
			}
		}
		target.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.metadataSelected(mapping, (MetadataVO) target.getValue());
			}
		});
		target.setEnabled(presenter.canEditMetadata());

		Button add = new TargetButton();
		add.addStyleName(ValoTheme.BUTTON_LINK);
		add.setEnabled(presenter.canEditMetadata());

		HorizontalLayout layout = new HorizontalLayout(caption, target, add);
		layout.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}

	private Component buildFieldSelector(VerticalLayout layout) {
		Button add = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.fieldAdditionRequested(mapping);
			}
		};
		add.setEnabled(presenter.canAddFieldsTo(mapping));
		layout.addComponents(add, buildFieldTable());
		layout.setComponentAlignment(add, Alignment.TOP_RIGHT);
		return layout;
	}

	private Component buildButtons() {
		save = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.saveButtonClicked(mapping);
			}
		};
		save.addStyleName(BaseForm.SAVE_BUTTON);
		save.addStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setEnabled(presenter.canSave(mapping));

		Button cancel = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		};

		HorizontalLayout layout = new HorizontalLayout(save, cancel);
		layout.setSpacing(true);
		layout.setSizeFull();

		return layout;
	}

	private Table buildFieldTable() {
		final List<ConnectorField> fields = presenter.getApplicableSourceFields(mapping);

		ButtonsContainer<BeanItemContainer<FieldMapper>> container = new ButtonsContainer<>(
				new BeanItemContainer<>(FieldMapper.class, mapping.getFieldMappers()));
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						FieldMapper mapper = (FieldMapper) itemId;
						presenter.fieldRemovalRequested(mapping, mapper.getField());
					}
				};
			}
		});

		Table table = new Table();
		table.setContainerDataSource(container);

		table.addGeneratedColumn("selector", new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				if (columnId == null) {
					return null;
				}
				final FieldMapper mapper = (FieldMapper) itemId;
				final ComboBox box = new ComboBox();
				box.setNullSelectionAllowed(false);
				box.setWidth("100%");
				for (ConnectorField field : fields) {
					box.addItem(field);
					box.setItemCaption(field, field.getLabel());
					if (field.getId().equals(mapper.getFieldId())) {
						box.setValue(field);
					}
				}
				box.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						mapper.setField((ConnectorField) box.getValue());
						save.setEnabled(presenter.canSave(mapping));
					}
				});
				return box;
			}
		});

		table.setVisibleColumns("selector", ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID);
		table.setColumnHeader("selector", $("AddEditMappingViewImpl.field"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setPageLength(Math.min(15, mapping.getFields().size()));
		table.setWidth("100%");

		return table;
	}

	private class TargetButton extends WindowButton {
		@PropertyId("code")
		private TextField code;

		@PropertyId("label")
		private TextField label;

		@PropertyId("type")
		private ComboBox type;

		@PropertyId("searchable")
		private CheckBox searchable;

		@PropertyId("advancedSearch")
		private CheckBox advancedSearch;

		@PropertyId("searchResults")
		private CheckBox searchResults;

		public TargetButton() {
			super($("AddEditMappingView.addMetadata"), $("AddEditMappingView.addMetadata"));
		}

		@Override
		protected Component buildWindowContent() {
			type = new ComboBox($("AddEditMappingView.metadata.type"));
			type.setNullSelectionAllowed(false);
			type.setRequired(true);
			for (MetadataValueType allowed : presenter.getApplicableTypes()) {
				type.addItem(allowed);
				type.setItemCaption(allowed, $(MetadataValueType.getCaptionFor(allowed)));
			}

			code = new BaseTextField($("AddEditMappingView.metadata.code"));
			code.setRequired(true);

			label = new BaseTextField($("AddEditMappingView.metadata.label"));
			label.setRequired(true);

			searchable = new CheckBox($("AddEditMappingView.metadata.searchable"));
			advancedSearch = new CheckBox($("AddEditMappingView.metadata.advancedSearch"));
			searchResults = new CheckBox($("AddEditMappingView.metadata.searchResults"));

			return new BaseForm<TargetParams>(
					new TargetParams(), this, type, code, label, searchable, advancedSearch, searchResults) {

				@Override
				protected void saveButtonClick(TargetParams targetParams)
						throws ValidationException {
					MetadataVO metadata = presenter.metadataCreationRequested(targetParams);
					target.addItem(metadata);
					target.setItemCaption(metadata, metadata.getLabel());
					target.setValue(metadata);
					getWindow().close();
				}

				@Override
				protected void cancelButtonClick(TargetParams targetParams) {
					getWindow().close();
				}
			};
		}
	}
}
