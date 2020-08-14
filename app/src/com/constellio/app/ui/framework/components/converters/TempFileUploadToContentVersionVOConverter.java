package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.vaadin.data.util.converter.Converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

	protected ContentVersionVO toContentVO(final TempFileUpload tempFileUpload) {
		String fileName = tempFileUpload.getFileName();
		String mimeType = tempFileUpload.getMimeType();
		long length = tempFileUpload.getLength();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		IOServices ioServices = constellioFactories.getIoServicesFactory().newIOServices();
		ContentManager contentManager = constellioFactories.getModelLayerFactory().getContentManager();

		File tempFile = tempFileUpload.getTempFile();
		try {
			InputStream tempFileIn = ioServices.newFileInputStream(tempFile, "TempFileUploadToContentVersionVOConverter.toContentVO");
			ContentManager.ContentVersionDataSummaryResponse uploadResponse = contentManager.upload(tempFileIn, fileName);
			ContentVersionDataSummary contentVersionDataSummary = uploadResponse.getContentVersionDataSummary();
			ioServices.closeQuietly(tempFileIn);
			final String hash = contentVersionDataSummary.getHash();
			InputStreamProvider inputStreamProvider = new InputStreamProvider() {
				@Override
				public InputStream getInputStream(String streamName) {
					ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
					ContentManager contentManager = constellioFactories.getModelLayerFactory().getContentManager();
					return contentManager.getContentInputStream(hash, streamName);
				}

				@Override
				public void deleteTemp() {
				}
			};
			boolean hasFoundDuplicate = uploadResponse.hasFoundDuplicate();
			return tempFileUpload != null ?
				   new ContentVersionVO(null, shouldSetHashForTemporaryFiles() ? hash : null, fileName, mimeType, length, null, null, null, null, null, null,
						   inputStreamProvider).setHasFoundDuplicate(hasFoundDuplicate).setDuplicatedHash(hasFoundDuplicate ? hash : null) :
				   null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			tempFileUpload.delete();
		}
	}

	protected boolean shouldSetHashForTemporaryFiles() {
		return false;
	}
}
