package com.constellio.app.ui.pages.management.extractors.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.pages.management.extractors.converters.RegexConfigVOToStringConverter;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("unchecked")
public class ListAddRemoveRegexConfigField extends ListAddRemoveField<RegexConfigVO, RegexConfigFieldImpl> {

	public static final String VALUE = "value";
	public static final String REGEX = "regex";
	public static final String REGEX_CONFIG_TYPE = "regexConfigType";

	private RegexConfigVOToStringConverter converter = new RegexConfigVOToStringConverter();

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected RegexConfigFieldImpl newAddEditField() {
		return new RegexConfigFieldImpl();
	}

	@Override
	protected String getItemCaption(Object itemId) {
		return converter.convertToPresentation((RegexConfigVO) itemId, String.class, getLocale());
	}

	@Override
	protected List<?> getExtraColumnPropertyIds() {
		return Arrays.asList(REGEX, REGEX_CONFIG_TYPE, VALUE);
	}

	@Override
	protected Property<?> getExtraColumnProperty(Object itemId, Object propertyId) {
		Property<?> property;
		RegexConfigVO regexConfigVO = itemId != null ? getListElementValue(itemId) : null;
		if (REGEX.equals(propertyId)) {
			property = new ObjectProperty<>(regexConfigVO.getRegex());
		} else if (REGEX_CONFIG_TYPE.equals(propertyId)) {
			String value = String.valueOf(regexConfigVO.getRegexConfigType());
			property = new ObjectProperty<>($("RegexConfigType." + value));
		} else if (VALUE.equals(propertyId)) {
			property = new ObjectProperty<>(regexConfigVO.getValue());
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return property;
	}

	public void setMetadataOptions(List<MetadataVO> metadataVOs) {
		getAddEditField().setMetadataOptions(metadataVOs);
	}

	@Override
	protected Class<?> getExtraColumnType(Object propertyId) {
		Class<?> type;
		if (REGEX.equals(propertyId)) {
			type = String.class;
		} else if (REGEX_CONFIG_TYPE.equals(propertyId)) {
			type = String.class;
		} else if (VALUE.equals(propertyId)) {
			type = String.class;
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return type;
	}

	@Override
	protected int getExtraColumnWidth(Object propertyId) {
		int width;
		if (VALUE.equals(propertyId) ||
				REGEX.equals(propertyId) ||
				REGEX_CONFIG_TYPE.equals(propertyId)) {
			width = 250;
		} else {
			throw new IllegalArgumentException("Unrecognized propertyId : " + propertyId);
		}
		return width;
	}

}