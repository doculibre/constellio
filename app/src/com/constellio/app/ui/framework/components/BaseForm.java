package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class BaseForm<T> extends CustomComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseForm.class);

	public static final String BASE_FORM = "base-form";

	public static final String STYLE_FIELD = "base-form-field";

	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";

	public static final String SAVE_BUTTON = "base-form-save";

	public static final String CANCEL_BUTTON = "base-form_cancel";

	protected T viewObject;

	protected Item item;

	protected VerticalLayout formLayout;

	protected HorizontalLayout buttonsLayout;

	protected Button saveButton;

	protected Button cancelButton;

	protected List<Field<?>> fields = new ArrayList<Field<?>>();

	protected FieldGroup fieldGroup;

	protected TabSheet tabSheet;

	private Map<String, VerticalLayout> tabs = new HashMap<>();

	private boolean useTabSheet;

	public BaseForm(final T viewObject, Serializable objectWithMemberFields, Field<?>... fields) {
		this(viewObject, new MemberFieldBinder(objectWithMemberFields), fields);
	}

	public BaseForm(final T viewObject, List<FieldAndPropertyId> fieldsAndPropertyIds) {
		this(viewObject, new FieldAndPropertyIdBinder(fieldsAndPropertyIds), toFields(fieldsAndPropertyIds));
	}

	private BaseForm(final T viewObject, FieldBinder binder, Field<?>... fields) {
		super();
		this.viewObject = viewObject;
		for (Field<?> field : fields) {
			this.fields.add(field);
		}

		setSizeFull();
		addStyleName(BASE_FORM);

		item = newItem(viewObject);

		formLayout = new VerticalLayout();
		formLayout.setSpacing(true);

		fieldGroup = new FieldGroup(item) {
			@Override
			protected void configureField(Field<?> field) {
				if (field instanceof AbstractField) {
					AbstractField<?> abstractField = (AbstractField<?>) field;
					abstractField.setValidationVisible(false);
				}
				field.setBuffered(isBuffered());
				// Do not alter the readOnly and enabled states of the field
			}
		};
		//		fieldGroup.setBuffered(false);
		binder.bind(fieldGroup);

		tabSheet = new TabSheet();
		// First pass to see if all the fields have the same tab status
		boolean tabFound = false;
		for (Field<?> field : fields) {
			Object propertyId = fieldGroup.getPropertyId(field);
			if (StringUtils.isNotBlank(getTabCaption(field, propertyId))) {
				tabFound = true;
				break;
			}
		}
		if (tabFound) {
			useTabSheet = true;
		}

		boolean firstField = true;
		for (Field<?> field : fields) {
			if (firstField) {
				field.focus();
				firstField = false;
			}
			field.addStyleName(STYLE_FIELD);

			OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
				@Override
				public void onEnterKeyPressed() {
					trySave();
				}
			};
			if (field instanceof TextField) {
				onEnterHandler.installOn((TextField) field);
			} else if (field instanceof DateField) {
				onEnterHandler.installOn((DateField) field);
			} else if (field instanceof ComboBox) {
				onEnterHandler.installOn((ComboBox) field);
			}

			addToDefaultLayoutOrTabSheet(field);
		}

		buttonsLayout = new HorizontalLayout();
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);

		saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				trySave();
			}
		});

		cancelButton = new Button($("cancel"));
		cancelButton.addStyleName(CANCEL_BUTTON);
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				cancelButtonClick(viewObject);
			}
		});

		setCompositionRoot(formLayout);
		if (tabSheet.iterator().hasNext()) {
			formLayout.addComponent(tabSheet);
		}
		formLayout.addComponent(buttonsLayout);
		buttonsLayout.addComponents(saveButton, cancelButton);
	}

	private void addToDefaultLayoutOrTabSheet(Field<?> field) {
		VerticalLayout fieldLayout;
		if (useTabSheet) {
			Object propertyId = fieldGroup.getPropertyId(field);
			String groupLabel = getTabCaption(field, propertyId);
			String tabCaption;
			if (StringUtils.isBlank(groupLabel)) {
				tabCaption = $("BaseForm.defaultTab");
			} else if (!groupLabel.matches("\\w.*")) {
				tabCaption = groupLabel;
			} else {
				tabCaption = $("BaseForm.defaultTabIcon") + " " + groupLabel;
			}
			Resource tabIcon = getTabIcon(tabCaption);
			fieldLayout = tabs.get(tabCaption);
			if (fieldLayout == null) {
				fieldLayout = new VerticalLayout();
				fieldLayout.setWidth("100%");
				tabs.put(tabCaption, fieldLayout);
				fieldLayout.setSpacing(true);
				if (tabIcon != null) {
					tabSheet.addTab(fieldLayout, tabCaption, tabIcon);
				} else {
					tabSheet.addTab(fieldLayout, tabCaption);
				}
			}
		} else {
			fieldLayout = formLayout;
		}
		fieldLayout.addComponent(field);
	}

	public void commit() {
		for (Field<?> field : fieldGroup.getFields()) {
			try {
				field.commit();
			} catch (SourceException | InvalidValueException e) {
				// Ignore the error
			}
		}
	}

	/**
	 * If this method is overriden and doesn't return null anymore, a tab sheet will be used to display the fields.
	 *
	 * @param field The field that will be added under the tab
	 * @param propertyId The property id attached to the field
	 * @return The caption of the tab under which the field will be added
	 */
	protected String getTabCaption(Field<?> field, Object propertyId) {
		return null;
	}

	protected Resource getTabIcon(String tabCaption) {
		return null;
	}

	public List<Field<?>> getFields() {
		return fields;
	}

	public Field<?> getField(String id) {
		for (Field<?> field : fields) {
			if (id.equals(field.getId())) {
				return field;
			}
		}
		return null;
	}

	public T getViewObject() {
		return viewObject;
	}

	protected Item newItem(T viewObject) {
		return new BeanItem<T>(viewObject);
	}

	private void trySave() {
		clearBackendValidators();
		for (Field<?> field : fields) {
			if (field instanceof AbstractField) {
				AbstractField<?> abstractField = (AbstractField<?>) field;
				abstractField.setValidationVisible(true);
			}
		}
		if (fieldGroup.isValid()) {
			try {
				fieldGroup.commit();
				try {
					saveButtonClick(viewObject);
				} catch (Exception e) {

					ValidationErrors errors = MessageUtils.getValidationErrors(e);

					if (errors != null) {
						showBackendValidationException(errors);
					} else {
						showErrorMessage(MessageUtils.toMessage(e));
						LOGGER.warn(e.getMessage(), e);
					}
				}
			} catch (CommitException e) {
				showErrorMessage(MessageUtils.toMessage(e));
				LOGGER.warn(e.getMessage(), e);
			}
		} else {
			Field<?> firstFieldWithError = null;
			StringBuilder missingRequiredFields = new StringBuilder();
			for (Field<?> field : fieldGroup.getFields()) {
				if (!field.isValid() && field.isRequired() && isEmptyValue(field.getValue())) {
					field.setRequiredError($("requiredField"));
					if(missingRequiredFields.length() != 0) {
						missingRequiredFields.append("<br/>");
					}
					missingRequiredFields.append($("requiredFieldWithName", "\"" + field.getCaption() + "\""));
					if (firstFieldWithError == null) {
						firstFieldWithError = field;
					}
				}
			}
			if (firstFieldWithError != null) {
				firstFieldWithError.focus();
				showErrorMessage(missingRequiredFields.toString());
			}
		}
	}

	private boolean isEmptyValue(Object value) {
		boolean emptyValue;
		if (value == null) {
			emptyValue = true;
		} else if (value instanceof String) {
			emptyValue = StringUtils.isBlank((String) value);
		} else if (value instanceof Collection) {
			emptyValue = ((Collection<?>) value).isEmpty();
		} else {
			emptyValue = false;
		}
		return emptyValue;
	}

	protected void showBackendValidationException(ValidationErrors validationErrors) {
		Set<String> globalErrorMessages = new HashSet<String>();
		for (ValidationError validationError : validationErrors.getValidationErrors()) {
			String errorMessage = $(validationError);
			globalErrorMessages.add(errorMessage);
		}

		if (!globalErrorMessages.isEmpty()) {
			StringBuffer globalErrorMessagesSB = new StringBuffer();
			for (String globalErrorMessage : globalErrorMessages) {
				globalErrorMessage = $(globalErrorMessage);
				if (globalErrorMessagesSB.length() != 0) {
					globalErrorMessagesSB.append("<br />");
				}
				globalErrorMessagesSB.append(globalErrorMessage);
			}
			showErrorMessage(globalErrorMessagesSB.toString());
		}
	}

	protected void clearBackendValidators() {
		for (Field<?> field : fields) {
			for (Validator validator : new ArrayList<Validator>(field.getValidators())) {
				if (validator instanceof BackendValidator) {
					field.removeValidator(validator);
				}
			}
		}
	}

	protected void showErrorMessage(String message) {
		Notification notification = new Notification(message + "<br/><br/>" + $("clickToClose"), Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	protected abstract void saveButtonClick(T viewObject)
			throws ValidationException;

	protected abstract void cancelButtonClick(T viewObject);

	public static class FieldAndPropertyId implements Serializable {

		public final Field<?> field;

		public final Object propertyId;

		public FieldAndPropertyId(Field<?> field, Object propertyId) {
			super();
			this.field = field;
			this.propertyId = propertyId;
		}

	}

	private interface FieldBinder extends Serializable {

		void bind(FieldGroup fieldGroup);

	}

	private static class MemberFieldBinder implements FieldBinder {

		private Serializable objectWithMemberFields;

		public MemberFieldBinder(Serializable objectWithMemberFields) {
			this.objectWithMemberFields = objectWithMemberFields;
		}

		public void bind(FieldGroup fieldGroup) {
			fieldGroup.bindMemberFields(objectWithMemberFields);
		}

	}

	private static class FieldAndPropertyIdBinder implements FieldBinder {

		private List<FieldAndPropertyId> fieldsAndPropertyIds;

		public FieldAndPropertyIdBinder(List<FieldAndPropertyId> fieldsAndPropertyIds) {
			this.fieldsAndPropertyIds = fieldsAndPropertyIds;
		}

		public void bind(FieldGroup fieldGroup) {
			for (FieldAndPropertyId fieldAndPropertyId : fieldsAndPropertyIds) {
				Field<?> field = fieldAndPropertyId.field;
				Object propertyId = fieldAndPropertyId.propertyId;
				fieldGroup.bind(field, propertyId);
			}
		}

	}

	private static Field<?>[] toFields(List<FieldAndPropertyId> fieldsAndPropertyIds) {
		Field<?>[] fields = new Field<?>[fieldsAndPropertyIds.size()];
		for (int i = 0; i < fieldsAndPropertyIds.size(); i++) {
			FieldAndPropertyId fieldAndPropertyId = fieldsAndPropertyIds.get(i);
			fields[i] = fieldAndPropertyId.field;
		}
		return fields;
	}

	protected static class BackendValidator extends AbstractValidator<Object> {

		public BackendValidator(String errorMessage) {
			super(errorMessage);
		}

		@Override
		protected boolean isValidValue(Object value) {
			return false;
		}

		@Override
		public Class<Object> getType() {
			return Object.class;
		}

	}

}
