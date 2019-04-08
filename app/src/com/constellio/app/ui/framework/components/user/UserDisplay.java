package com.constellio.app.ui.framework.components.user;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.vaadin.ui.Label;

public class UserDisplay extends I18NHorizontalLayout {
	
	public UserDisplay(String id) {
		if (id != null) {
			ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory(); 
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			
			Record userRecord = recordServices.getDocumentById(id);
			String collection = userRecord.getCollection();
			
			SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
			User user = schemasRecordsServices.wrapUser(userRecord);
			init(user.getUsername(), user.getFirstName(), user.getLastName());
		} else {
			setVisible(false);
		}	
	}

	public UserDisplay(UserVO userVO) {
		this(userVO.getUsername(), userVO.getFirstName(), userVO.getLastName());
	}
	
	public UserDisplay(String username, String firstName, String lastName) {
		init(username, firstName, lastName);
	}
	
	private void init(String username, String firstName, String lastName) {
		addStyleName("user-display");
		UserImage userImage = new UserImage(username);
		userImage.addStyleName("user-display-icon");
		userImage.setCaption(null);
		
		Label nameLabel = new Label(firstName + " " + lastName);
		nameLabel.addStyleName("user-display-name");
		addComponents(userImage, nameLabel);
	}

}
