package com.constellio.app.ui.framework.components.converters;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.data.io.services.facades.IOServices;
import com.vaadin.data.util.converter.Converter;

public class TempFileUploadToContentVersionVOConverter implements Converter<Object, Object> {

	@SuppressWarnings("unchecked")
	@Override
	public Object convertToModel(Object value, Class<? extends Object> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		Object model;
		if (value instanceof TempFileUpload) {
			TempFileUpload tempFileUpload = (TempFileUpload) value;
			model = toContentVO(tempFileUpload);
		} else if (value instanceof Collection) {
			List<ContentVersionVO> contentVersionVOs = new ArrayList<ContentVersionVO>();
			Collection<Object> collectionValue = (Collection<Object>) value;
			for (Object collectionElement : collectionValue) {
				if (collectionElement instanceof TempFileUpload) {
					TempFileUpload tempFileUpload = (TempFileUpload) collectionElement;
					contentVersionVOs.add(toContentVO(tempFileUpload));
				} else {
					contentVersionVOs.add((ContentVersionVO) collectionElement);
				}
			}
			model = contentVersionVOs;
		} else {
			model = value;
		}
		return model;
	}

	@Override
	public Object convertToPresentation(Object value, Class<? extends Object> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value;
	}

	@Override
	public Class<Object> getModelType() {
		return Object.class;
	}

	@Override
	public Class<Object> getPresentationType() {
		return Object.class;
	}

	private ContentVersionVO toContentVO(final TempFileUpload tempFileUpload) {
		String fileName = tempFileUpload.getFileName();
		String mimeType = tempFileUpload.getMimeType();
		long length = tempFileUpload.getLength();
		InputStreamProvider inputStreamProvider = new InputStreamProvider() {
			@Override
			public InputStream getInputStream(String streamName) {
				IOServices ioServices = ConstellioFactories.getInstance().getIoServicesFactory().newIOServices();
				try {
					return ioServices.newFileInputStream(tempFileUpload.getTempFile(), streamName);
				} catch (FileNotFoundException e) {
					return null;
				}
			}

			@Override
			public void deleteTemp() {
				tempFileUpload.delete();
			}
		};
		return tempFileUpload != null ?
				new ContentVersionVO(null, null, fileName, mimeType, length, null, null, null, null, null, null,
						inputStreamProvider) :
				null;
	}

}
