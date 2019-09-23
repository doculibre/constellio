package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class ListAddRemoveField<T extends Serializable, F extends AbstractField<?>> extends CustomField<List<T>> {
	public static final String STYLE_NAME = "list-add-remove";
	private static final String ERROR_STYLE_NAME = STYLE_NAME + "-error";
	public static final String ADD_EDIT_FIELD_LAYOUT_STYLE_NAME = STYLE_NAME + "-add-edit-field-layout";
	public static final String ADD_EDIT_FIELD_STYLE_NAME = STYLE_NAME + "-add-edit-field";
	public static final String ADD_BUTTON_STYLE_NAME = STYLE_NAME + "-add-button";
	public static final String EDIT_BUTTON_STYLE_NAME = STYLE_NAME + "-edit-button";
	public static final String REMOVE_BUTTON_STYLE_NAME = STYLE_NAME + "-remove-button";
	public static final String TABLE_STYLE_NAME = STYLE_NAME + "-table";
	public static final String VALUES_STYLE_NAME = STYLE_NAME + "-values";
	protected static final String CAPTION_PROPERTY_ID = "caption";
	private VerticalLayout mainLayout;
	private HorizontalLayout addEditFieldLayout;
	protected F addEditField;
	private Button addButton;
	protected ButtonsContainer<ValuesContainer> valuesAndButtonsContainer;
	protected ValuesContainer valuesContainer;
	protected Table valuesTable;
	private Converter<String, T> itemConverter;
	private boolean delayedFocus = false;
	private Boolean delayedReadOnly = null;
	private Boolean delayedEnabled = null;
	private List<T> delayedNewFieldValue;

	public ListAddRemoveField() {
		setWidth("100%");
		setHeightUndefined();
	}

	@Override
	public String getRequiredError() {
		return addEditField.getRequiredError();
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		addEditField.setRequiredError(requiredMessage);
		addStyleName(ERROR_STYLE_NAME);
	}

	@Override
	public String getConversionError() {
		return addEditField.getConversionError();
	}

	@Override
	public void setConversionError(String valueConversionError) {
		addEditField.setConversionError(valueConversionError);
		addStyleName(ERROR_STYLE_NAME);
	}

	@Override
	public ErrorMessage getComponentError() {
		return addEditField.getComponentError();
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		addEditField.setComponentError(componentError);
	}

	@Override
	public boolean isValid() {
		boolean valid;
		if (!addEditField.isValid() || !super.isValid()) {
			valid = false;
		} else {
			valid = !(isRequired() && isRequiredValueMissing());
		}
		return valid;
	}

	private boolean isRequiredValueMissing() {
		boolean requiredValueMissing;
		if (isRequired()) {
			if (getValue() == null) {
				requiredValueMissing = true;
			} else if (isRequired() && getInternalValue() instanceof List) {
				requiredValueMissing = getInternalValue().isEmpty();
			} else {
				requiredValueMissing = false;
			}
		} else {
			requiredValueMissing = false;
		}
		return requiredValueMissing;
	}

	@Override
	public void validate()
			throws InvalidValueException {
		try {
			addEditField.validate();
			super.validate();
			if (isRequiredValueMissing()) {
				throw new InvalidValueException($("requiredField"));
			}
			removeStyleName(ERROR_STYLE_NAME);
		} catch (InvalidValueException e) {
			throw e;
		}
	}

	public final Button getAddButton() {
		return addButton;
	}

	public final F getAddEditField() {
		return addEditField;
	}

	public final HorizontalLayout getAddEditFieldLayout() {
		return addEditFieldLayout;
	}

	@Override
	public void focus() {
		if (addEditField != null) {
			addEditField.focus();
			delayedFocus = false;
		} else {
			delayedFocus = true;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Class getType() {
		return List.class;
	}

	public Converter<String, T> getItemConverter() {
		return itemConverter;
	}

	public void setItemConverter(Converter<String, T> itemConverter) {
		this.itemConverter = itemConverter;
	}

	@SuppressWarnings("unchecked")
	protected void tryAdd() {
		addEditField.commit();
		T value = (T) addEditField.getPropertyDataSource().getValue();
		addValue(value);
		removeStyleName(ERROR_STYLE_NAME);
	}

	@SuppressWarnings("unchecked")
	protected void addValue(T value) {
		if (value != null) {
			if (!isCancelAddValueAndSetValueToNull(value)) {
				List<T> listValue = value instanceof List ? (List<T>) value : new ArrayList<>(Arrays.asList(value));
				for (T listValueItem : listValue) {
					valuesAndButtonsContainer.addItem(listValueItem);
				}
			}
			addEditField.setValue(null);
			notifyValueChange();
		}
	}

	protected boolean isCancelAddValueAndSetValueToNull(T value) {
		return false;
	}

	protected void removeValue(Object value) {
		if (value != null) {
			valuesAndButtonsContainer.removeItem(value);
			notifyValueChange();
		}
	}

	protected void removeValue(T value) {
		if (value != null) {
			valuesAndButtonsContainer.removeItem(value);
			notifyValueChange();
		}
	}

	@SuppressWarnings("unchecked")
	protected void notifyValueChange() {
		if (!isBuffered() && getPropertyDataSource() != null) {
			getPropertyDataSource().setValue(getInternalValue());
		} else {
			valueChange(new ValueChangeEvent(this));
		}
	}

	protected boolean isEditPossible() {
		return true;
	}

	protected boolean isEditButtonVisible(T item) {
		return true;
	}

	protected boolean isDeleteButtonVisible(T item) {
		return true;
	}

	protected List<?> getExtraColumnPropertyIds() {
		return null;
	}

	@SuppressWarnings("unchecked")
	protected T getListElementValue(Object itemId) {
		return (T) itemId;
	}

	protected Property<?> getExtraColumnProperty(Object itemId, Object propertyId) {
		throw new UnsupportedOperationException("No extra property");
	}

	protected Class<?> getExtraColumnType(Object propertyId) {
		throw new UnsupportedOperationException("No extra property");
	}

	protected int getExtraColumnWidth(Object propertyId) {
		return -1;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected Component initContent() {
		addStyleName(STYLE_NAME);

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_NAME + "-main-layout");
		mainLayout.setHeightUndefined();
		mainLayout.setSpacing(true);
		setMainLayoutWidth(mainLayout);

		addEditFieldLayout = new HorizontalLayout();
		addEditFieldLayout.addStyleName(ADD_EDIT_FIELD_LAYOUT_STYLE_NAME);
		addEditFieldLayout.setSpacing(true);

		addEditField = newAddEditField();
		if (delayedFocus) {
			addEditField.focus();
		}

		addEditField.setPropertyDataSource(new ObjectProperty(null, Object.class));
		addEditField.addStyleName(ADD_EDIT_FIELD_STYLE_NAME);

		OnEnterKeyHandler onEnterKeyHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				tryAdd();
			}
		};
		if (addEditField instanceof TextField) {
			onEnterKeyHandler.installOn((TextField) addEditField);
		} else if (addEditField instanceof DateField) {
			onEnterKeyHandler.installOn((DateField) addEditField);
		} else if (addEditField instanceof ComboBox) {
			onEnterKeyHandler.installOn((ComboBox) addEditField);
		}

		addButton = new AddButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				tryAdd();
			}
		};
		addButton.addStyleName(ADD_BUTTON_STYLE_NAME);
		addButton.setVisible(isAddButtonVisible());

		setValuesContainer();
		valuesAndButtonsContainer = new ButtonsContainer(valuesContainer);

		if (isEditPossible()) {
			valuesAndButtonsContainer.addButton(addEditButton());
		}
		valuesAndButtonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						removeValue((T) itemId);
					}
				};
				if (!isDeleteButtonVisible((T) itemId)) {
					deleteButton.setVisible(false);
				}
				deleteButton.setEnabled(!ListAddRemoveField.this.isReadOnly() && ListAddRemoveField.this.isEnabled());
				deleteButton.addStyleName(REMOVE_BUTTON_STYLE_NAME);
				return deleteButton;
			}
		});

		valuesTable = new BaseTable(getClass().getName());
		valuesTable.addStyleName(TABLE_STYLE_NAME);
		valuesTable.setContainerDataSource(valuesAndButtonsContainer);
		valuesTable.setPageLength(0);

		valuesTable.setWidth("100%");
		valuesTable.setNullSelectionAllowed(false);
		valuesTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		valuesTable.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		valuesTable.setColumnExpandRatio(CAPTION_PROPERTY_ID, 1);

		Collection<?> extraColumnPropertyIds = getExtraColumnPropertyIds();
		if (extraColumnPropertyIds != null) {
			for (Object extraPropertyIds : extraColumnPropertyIds) {
				int extraColumnWidth = getExtraColumnWidth(extraPropertyIds);
				if (extraColumnWidth > 0) {
					valuesTable.setColumnWidth(extraPropertyIds, extraColumnWidth);
				}
			}
		}

		int buttonsWidth;
		if (isEditPossible()) {
			buttonsWidth = 80;
		} else {
			buttonsWidth = 47;
		}
		valuesTable.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, buttonsWidth);

		mainLayout.addComponents(addEditFieldLayout, valuesTable);
		addEditFieldLayout.addComponents(addEditField, addButton);

		addEditFieldLayout.setExpandRatio(addEditField, 1);
		addEditFieldLayout.setVisible(isAddEditFieldVisible());

		if (delayedReadOnly != null) {
			setReadOnly(delayedReadOnly);
		}
		if (delayedEnabled != null) {
			setEnabled(delayedEnabled);
		}

		if (delayedNewFieldValue != null) {
			List<T> newFieldValue = delayedNewFieldValue;
			delayedNewFieldValue = null;
			setValue(newFieldValue);
		}
		return mainLayout;
	}

	protected void setValuesContainer() {
		valuesContainer = new ValuesContainer(new ArrayList<T>());
	}

	protected boolean isAddEditFieldVisible() {
		return true;
	}

	protected void setMainLayoutWidth(VerticalLayout mainLayout) {
		mainLayout.setWidth("99%");
	}

	protected ContainerButton addEditButton() {
		return new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				EditButton editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						removeValue((T) itemId);
						((Field<T>) addEditField).setValue(getConvertedValueFor(itemId));
						addEditField.focus();
					}
				};
				if (!isEditButtonVisible((T) itemId)) {
					editButton.setVisible(false);
				}
				editButton.setEnabled(!ListAddRemoveField.this.isReadOnly() && ListAddRemoveField.this.isEnabled());
				editButton.addStyleName(EDIT_BUTTON_STYLE_NAME);
				return editButton;
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<T> getInternalValue() {
		List<T> internalValue;
		if (valuesAndButtonsContainer != null) {
			internalValue = (List<T>) valuesAndButtonsContainer.getItemIds();
		} else {
			internalValue = null;
		}
		return internalValue;
	}

	@Override
	protected void setInternalValue(List<T> newValue) {
		if (valuesAndButtonsContainer != null) {
			List<T> newValueCopy = new ArrayList<>();
			if (newValue != null) {
				newValueCopy.addAll(newValue);
			}
			valuesAndButtonsContainer.removeAllItems();
			for (T t : newValueCopy) {
				valuesAndButtonsContainer.addItem(t);
			}
		} else {
			delayedNewFieldValue = newValue;
		}
	}

	@Override
	public List<T> getValue() {
		List<T> value = getInternalValue();
		if (value != null && value.isEmpty()) {
			//			value = null;
		}
		return value;
	}

	@Override
	public void setValue(List<T> newFieldValue)
			throws ReadOnlyException, ConversionException {
		super.setValue(newFieldValue);
	}

	protected boolean isAddButtonVisible() {
		return true;
	}

	@Override
	public void discard()
			throws SourceException {
		addEditField.discard();
		super.discard();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		if (addEditField != null) {
			addEditField.setReadOnly(readOnly);
			addButton.setEnabled(!readOnly);
			for (Button containerButton : getContainerButtons()) {
				containerButton.setEnabled(!readOnly);
			}
		} else {
			delayedReadOnly = readOnly;
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (addEditField != null) {
			addEditField.setEnabled(enabled);
			addButton.setEnabled(enabled);
			for (Button containerButton : getContainerButtons()) {
				containerButton.setEnabled(enabled);
			}
		} else {
			delayedEnabled = enabled;
		}
	}

	private List<Button> getContainerButtons() {
		List<Button> containerButtons = new ArrayList<Button>();
		Collection<?> itemIds = valuesAndButtonsContainer.getItemIds();
		for (Object itemId : itemIds) {
			List<Button> itemButtons = valuesAndButtonsContainer.getButtons(itemId);
			containerButtons.addAll(itemButtons);
		}
		return containerButtons;
	}

	@Override
	public void commit()
			throws SourceException, InvalidValueException {
		List<T> internalValue = getInternalValue();
		if (internalValue != null && internalValue.isEmpty()) {
			setInternalValue(null);
		}
		tryAdd();
		super.commit();
	}

	protected abstract F newAddEditField();

	protected Class<?> getCaptionComponentClass() {
		return Label.class;
	}

	protected Component newCaptionComponent(T itemId, String caption) {
		Label captionLabel = new Label(caption);
		captionLabel.setContentMode(ContentMode.HTML);
		return captionLabel;
	}

	protected String getItemCaption(Object itemId) {
		String caption;
		if (itemConverter != null) {
			Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
			caption = itemConverter.convertToPresentation((T) itemId, String.class, locale);
		} else {
			caption = itemId.toString();
		}
		return caption;
	}

	protected class ValuesContainer extends IndexedContainer {

		public ValuesContainer(List<T> values) {
			super(values);
			addContainerProperty(CAPTION_PROPERTY_ID, getCaptionComponentClass(), null);
			List<?> extraPropertyIds = getExtraColumnPropertyIds();
			if (extraPropertyIds != null) {
				for (Object extraPropertyId : extraPropertyIds) {
					Class<?> extraPropertyType = getExtraColumnType(extraPropertyId);
					addContainerProperty(extraPropertyId, extraPropertyType, null);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Property<?> getContainerProperty(final Object itemId, Object propertyId) {
			if (itemId != null) {
				if (CAPTION_PROPERTY_ID.equals(propertyId)) {
					String caption = getItemCaption(itemId);
					Component captionComponent = newCaptionComponent((T) itemId, caption);
					return new ObjectProperty<Component>(captionComponent, Component.class);
				} else {
					return getExtraColumnProperty(itemId, propertyId);
				}
			} else {
				return new ObjectProperty<>(null);
			}
		}

	}

	protected T getConvertedValueFor(Object value) {
		return (T) value;
	}

}
