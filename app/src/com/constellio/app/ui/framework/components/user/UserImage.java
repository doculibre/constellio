package com.constellio.app.ui.framework.components.user;

import java.io.IOException;
import java.io.InputStream;

import com.constellio.app.modules.tasks.ui.components.TaskTable;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;

public class UserImage extends Embedded {

	public UserImage(String username) {
		ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
		UserPhotosServices photosServices = modelLayerFactory.newUserPhotosServices();
		
		Resource imageResource;
		if (photosServices.hasPhoto(username)) {
			imageResource = new StreamResource(readSourceStream(username), username + ".png");
		} else {
			imageResource = new ThemeResource("images/profiles/default.jpg");
		}
		setSource(imageResource);
	}

	private StreamSource readSourceStream(final String username) {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				return newUserPhotoInputStream(username);
			}
		};
	}

	private InputStream newUserPhotoInputStream(String username) {
		UserPhotosServices photosServices = ConstellioFactories.getInstance().getModelLayerFactory().newUserPhotosServices();
		try {
			return photosServices.getPhotoInputStream(username).create(TaskTable.class.getName() + ".UserPhoto");
		} catch (UserPhotosServicesRuntimeException_UserHasNoPhoto u) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
