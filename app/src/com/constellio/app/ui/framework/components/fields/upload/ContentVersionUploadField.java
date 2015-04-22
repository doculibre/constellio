/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.components.fields.upload;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.converters.TempFileUploadToContentVersionVOConverter;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.themes.ValoTheme;

public class ContentVersionUploadField extends BaseUploadField {
	
	public ContentVersionUploadField() {
		this(false);
	}

	public ContentVersionUploadField(boolean multiValue) {
		setConverter(new TempFileUploadToContentVersionVOConverter());
		setMultiValue(multiValue);
	}

	@Override
	protected Object getItemId(TempFileUpload tempFileUpload) {
		return getConverter().convertToModel(tempFileUpload, ContentVersionVO.class, getLocale());
	}

	@Override
	protected Component getItemCaption(Object itemId) {
		ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
		HorizontalLayout itemLayout = new HorizontalLayout();
		itemLayout.setSpacing(true);
		
		Component captionComponent = new DownloadContentVersionLink(contentVersionVO); 
		itemLayout.addComponent(captionComponent);

		if (isMajorVersionField(contentVersionVO)) {
			OptionGroup majorVersionField = new OptionGroup();
			majorVersionField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			majorVersionField.addItem(true);
			majorVersionField.addItem(false);
			majorVersionField.setItemCaption(true, $("yes"));
			majorVersionField.setItemCaption(false, $("no"));
			majorVersionField.setCaption($("ContentVersionUploadField.majorVersion"));
			majorVersionField.setRequired(true);
			majorVersionField.setImmediate(true);
			majorVersionField.setPropertyDataSource(new NestedMethodProperty<Boolean>(contentVersionVO, "majorVersion"));
			itemLayout.addComponent(majorVersionField);
		}

		return itemLayout;
	}
	
	protected boolean isMajorVersionField(ContentVersionVO contentVersionVO) {
		return contentVersionVO.getHash() == null;
	} 

	@Override
	protected void deleteTempFile(Object itemId) {
		// Nothing to do, will be deleted by the linked TempUploadFile
		ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
		contentVersionVO.getInputStreamProvider().deleteTemp();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate()
			throws InvalidValueException {
		super.validate();
		Object convertedValue = getConvertedValue();
		if (convertedValue instanceof List) {
			List<ContentVersionVO> convertedList = (List<ContentVersionVO>) convertedValue;
			for (ContentVersionVO contentVersionVO : convertedList) {
				validate(contentVersionVO);
			}
		} else if (convertedValue != null) {
			validate((ContentVersionVO) convertedValue);
		}
	}
	
	private void validate(ContentVersionVO contentVersionVO) throws InvalidValueException {
		if (isMajorVersionField(contentVersionVO) && contentVersionVO.getHash() == null && contentVersionVO.isMajorVersion() == null) {
			throw new InvalidValueException($("ContentVersionUploadField.majorVersion"));
		}
	}

}
