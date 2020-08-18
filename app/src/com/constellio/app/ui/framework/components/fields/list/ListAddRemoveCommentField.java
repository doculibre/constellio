package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.comment.CommentField;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class ListAddRemoveCommentField extends ListAddRemoveField<Comment, CommentField> {

	public static final String STYLE_NAME = "list-add-remove-comment";

	public static final String USER_PROPERTY = "user";

	public static final String DATE_PROPERTY = "commentDate";

	private JodaDateTimeToStringConverter dateTimeConverter = new JodaDateTimeToStringConverter();

	public ListAddRemoveCommentField() {
		super();
		addStyleName(STYLE_NAME);
	}

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected void addValue(Comment value) {
		if (value != null) {
			if (!isCancelAddValueAndSetValueToNull(value)) {
				List<Comment> listValue = value instanceof List ? (List<Comment>) value : new ArrayList<>(Arrays.asList(value));
				for (Comment listValueItem : listValue) {
					valuesAndButtonsContainer.addItemAt(0, listValueItem);
				}
			}
			addEditField.setValue(null);
			super.notifyValueChange();
		}
	}

	@Override
	protected CommentField newAddEditField() {
		return new CommentField();
	}

	@Override
	protected Component newCaptionComponent(Comment itemId, String caption) {
		Label captionLabel = new Label(itemId.getMessage());
		captionLabel.setContentMode(ContentMode.HTML);
		return captionLabel;
	}

	@Override
	protected List<?> getExtraColumnPropertyIds() {
		return Arrays.asList(USER_PROPERTY, DATE_PROPERTY);
	}

	@Override
	protected Property<?> getExtraColumnProperty(Object itemId, Object propertyId) {
		Property<?> property;
		Comment comment = itemId != null ? getListElementValue(itemId) : null;
		if (USER_PROPERTY.equals(propertyId)) {
			String userId = comment.getUserId();
			property = new ObjectProperty<>(new ReferenceDisplay(userId));
		} else if (DATE_PROPERTY.equals(propertyId)) {
			LocalDateTime commentDateTime = comment.getCreationDateTime();
			String commentDateTimeStr = dateTimeConverter.convertToPresentation(commentDateTime, String.class, getLocale());
			property = new ObjectProperty<>(new Label(commentDateTimeStr));
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return property;
	}

	@Override
	protected Class<?> getExtraColumnType(Object propertyId) {
		Class<?> type;
		if (USER_PROPERTY.equals(propertyId)) {
			type = ReferenceDisplay.class;
		} else if (DATE_PROPERTY.equals(propertyId)) {
			type = Label.class;
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return type;
	}

	@Override
	protected void setMainLayoutWidth(VerticalLayout mainLayout) {
		mainLayout.setWidth("100%");
	}

	@Override
	protected int getExtraColumnWidth(Object propertyId) {
		int width;
		if (USER_PROPERTY.equals(propertyId)) {
			width = 200;
		} else if (DATE_PROPERTY.equals(propertyId)) {
			width = 174;
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return width;
	}

}
