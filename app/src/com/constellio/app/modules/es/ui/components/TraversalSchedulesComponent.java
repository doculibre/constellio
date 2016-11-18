package com.constellio.app.modules.es.ui.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.joda.time.DateTimeConstants;

import com.constellio.app.modules.es.model.connectors.structures.TraversalSchedule;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

public class TraversalSchedulesComponent extends CustomField<List<TraversalSchedule>> {
	public static final String WEEK_DAY_FIELD = "weekDay";
	public static final String START_TIME_FIELD = "startTime";
	public static final String END_TIME_FIELD = "endTime";
	public static final String DELETE_BUTTON = "delete";

	private Table table;
	private AddButton addButton;
	private VerticalLayout layout;

	private RecordVO connectorInstance;
	private MetadataVO metadataVO;

	private final BeanItemContainer<TraversalSchedule> container;

	public TraversalSchedulesComponent(RecordVO connectorInstance, final MetadataVO metadataVO) {
		super();
		this.connectorInstance = connectorInstance;
		this.metadataVO = metadataVO;
		layout = new VerticalLayout();
		layout.setSpacing(true);

		table = new Table();
		table.setWidth("100%");
		table.setPageLength(0);
		container = new BeanItemContainer<>(TraversalSchedule.class);
		table.setContainerDataSource(container);

		List<TraversalSchedule> traversalSchedules = getTraversalSchedules();
		if (traversalSchedules.isEmpty()) {
			traversalSchedules.add(new TraversalSchedule());
		}

		table.addItems(traversalSchedules);
		table.addGeneratedColumn(WEEK_DAY_FIELD, new WeekDayFieldGenerator());
		table.setColumnHeader(WEEK_DAY_FIELD, $("TraversalSchedule.weekDay"));
		table.addGeneratedColumn(START_TIME_FIELD, new TimeFieldGenerator());
		table.setColumnHeader(START_TIME_FIELD, $("TraversalSchedule.startTime"));
		table.addGeneratedColumn(END_TIME_FIELD, new TimeFieldGenerator());
		table.setColumnHeader(END_TIME_FIELD, $("TraversalSchedule.endTime"));
		table.addGeneratedColumn(DELETE_BUTTON, new DeleteButtonGenerator());
		table.setColumnHeader(DELETE_BUTTON, "");

		table.addValueChangeListener(new ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				List<TraversalSchedule> newValue = (List<TraversalSchedule>) table.getItemIds();
				TraversalSchedulesComponent.this.connectorInstance.set(metadataVO, newValue);
			}
		});

		table.setVisibleColumns(WEEK_DAY_FIELD, START_TIME_FIELD, END_TIME_FIELD, DELETE_BUTTON);
		addButton = new AddButton($("add")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				TraversalSchedule newTraversalSchedule = new TraversalSchedule();
				List<TraversalSchedule> traversalSchedules = getTraversalSchedules();
				int indexOfNewCopy;
				if (traversalSchedules.size() > 1) {
					indexOfNewCopy = traversalSchedules.size();
				} else {
					indexOfNewCopy = 1;
				}
				traversalSchedules.add(indexOfNewCopy, newTraversalSchedule);
				addItems();
			}
		};

		layout.addComponents(addButton, table);
		layout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
	}

	private void addItems() {
		table.removeAllItems();
		List<TraversalSchedule> traversalSchedules = getTraversalSchedules();
		for (TraversalSchedule traversalSchedule : traversalSchedules) {
			addItem(traversalSchedule);
		}
	}

	private void removeItem(TraversalSchedule traversalSchedule) {
		List<TraversalSchedule> traversalSchedules = getTraversalSchedules();
		traversalSchedules.remove(traversalSchedule);
		setValue(traversalSchedules);
	}

	private void addItem(TraversalSchedule traversalSchedule) {
		table.addItem(traversalSchedule);
	}

	public List<TraversalSchedule> getTraversalSchedules() {
		return connectorInstance.getList(metadataVO);
	}

	@Override
	public List<TraversalSchedule> getValue() {
		return connectorInstance.get(metadataVO);
	}

	@Override
	public void setValue(List<TraversalSchedule> newFieldValue)
			throws ReadOnlyException, ConversionException {
		connectorInstance.set(metadataVO, newFieldValue);
		addItems();
	}

	@Override
	protected Component initContent() {
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getType() {
		return List.class;
	}

	public static class WeekDayFieldGenerator implements ColumnGenerator {
		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			Component component = buildOperatorField((TraversalSchedule) itemId);
			//			source.getContainerProperty(itemId, columnId).setValue(component);
			return component;
		}

		private Component buildOperatorField(final TraversalSchedule traversalSchedule) {
			ComboBox comboBox = new ComboBox();
			comboBox.setPropertyDataSource(new NestedMethodProperty<Integer>(traversalSchedule, WEEK_DAY_FIELD));
			comboBox.addItem(DateTimeConstants.MONDAY);
			comboBox.setItemCaption(DateTimeConstants.MONDAY, $("TraversalSchedule.monday"));
			comboBox.addItem(DateTimeConstants.TUESDAY);
			comboBox.setItemCaption(DateTimeConstants.TUESDAY, $("TraversalSchedule.tuesday"));
			comboBox.addItem(DateTimeConstants.WEDNESDAY);
			comboBox.setItemCaption(DateTimeConstants.WEDNESDAY, $("TraversalSchedule.wednesday"));
			comboBox.addItem(DateTimeConstants.THURSDAY);
			comboBox.setItemCaption(DateTimeConstants.THURSDAY, $("TraversalSchedule.thursday"));
			comboBox.addItem(DateTimeConstants.FRIDAY);
			comboBox.setItemCaption(DateTimeConstants.FRIDAY, $("TraversalSchedule.friday"));
			comboBox.addItem(DateTimeConstants.SATURDAY);
			comboBox.setItemCaption(DateTimeConstants.SATURDAY, $("TraversalSchedule.saturday"));
			comboBox.addItem(DateTimeConstants.SUNDAY);
			comboBox.setItemCaption(DateTimeConstants.SUNDAY, $("TraversalSchedule.sunday"));
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);
			//			comboBox.setWidth("100px");
			return comboBox;
		}
	}

	public static class TimeFieldGenerator implements ColumnGenerator {
		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			return buildOperatorField((TraversalSchedule) itemId, (String) columnId);
		}

		private Component buildOperatorField(final TraversalSchedule traversalSchedule, String columnId) {
			ComboBox comboBox = new ComboBox();
			for (int h = 0; h < 24; h++) {
				comboBox.addItem(h + ":00");
				comboBox.addItem(h + ":30");
			}
			if (START_TIME_FIELD.equals(columnId)) {
				comboBox.setPropertyDataSource(new NestedMethodProperty<String>(traversalSchedule, START_TIME_FIELD));
			} else if (END_TIME_FIELD.equals(columnId)) {
				comboBox.setPropertyDataSource(new NestedMethodProperty<String>(traversalSchedule, END_TIME_FIELD));

			}

			return comboBox;
		}
	}

	public class DeleteButtonGenerator implements ColumnGenerator {
		public final Resource ICON_RESOURCE = new ThemeResource("images/commun/supprimer.gif");

		@Override
		public Object generateCell(final Table source, final Object itemId, Object columnId) {
			Button delete = new IconButton(ICON_RESOURCE, $("delete"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					removeItem((TraversalSchedule) itemId);
				}
			};
			delete.setEnabled(source.size() > 1);
			return delete;
		}
	}

	@Override
	protected void validate(List<TraversalSchedule> fieldValue)
			throws InvalidValueException {
		super.validate(fieldValue);
		for (TraversalSchedule traversalSchedule : fieldValue) {
			if (!traversalSchedule.isEmpty()) {
				if (!traversalSchedule.hasValuesInAllFields()) {
					throw new InvalidValueException($("TraversalSchedule.mustHaveValuesInAllFields"));
				} else if (!startTimeBeforeEndTime(traversalSchedule)) {
					throw new InvalidValueException($("TraversalSchedule.startTimeAfterEndTime"));
				}
			}
		}
	}

	private boolean startTimeBeforeEndTime(TraversalSchedule traversalSchedule) {
		int startTime = toInt(traversalSchedule.getStartTime());
		int endTime = toInt(traversalSchedule.getEndTime());
		if (startTime == 0 && endTime == 0) {
			return true;
		}
		return startTime < endTime;
	}

	private int toInt(String time) {
		String normalizedTime = time.replace(":", "");
		return Integer.parseInt(normalizedTime);
	}
}
