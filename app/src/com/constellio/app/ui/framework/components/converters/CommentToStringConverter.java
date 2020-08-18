package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.Locale;

public class CommentToStringConverter implements Converter<String, Comment> {

	@Override
	public Comment convertToModel(String value, Class<? extends Comment> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		Comment comment;
		if (StringUtils.isNotBlank(value)) {
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			String collection = sessionContext.getCurrentCollection();
			UserVO userVO = sessionContext.getCurrentUser();
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			UserServices userServices = modelLayerFactory.newUserServices();
			User user = userServices.getUserInCollection(userVO.getUsername(), collection);

			comment = new Comment();
			comment.setMessage(value);
			comment.setCreationDateTime(new LocalDateTime());
			comment.setUser(user);
		} else {
			comment = null;
		}
		return comment;
	}

	@Override
	public String convertToPresentation(Comment value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? value.getMessage() : null;
	}

	@Override
	public Class<Comment> getModelType() {
		return Comment.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}
