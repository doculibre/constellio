package com.constellio.app.modules.rm.ui.components.administrativeUnit;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.Locale;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;

public class UserFunctionItem implements Serializable {
	
	private String functionId;
	
	private String userId;
	
	private RecordIdToCaptionConverter idToCaptionConverter = new RecordIdToCaptionConverter();

	public UserFunctionItem(String functionId, String userId) {
		this.functionId = functionId;
		this.userId = userId;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		Locale locale = ConstellioUI.getCurrent().getLocale();
		String functionLabel = idToCaptionConverter.convertToPresentation(functionId, String.class, locale);
		String userLabel = idToCaptionConverter.convertToPresentation(userId, String.class, locale);
		
		StringBuilder sb = new StringBuilder();
		sb.append($("UserFunctionItem.caption", functionLabel, userLabel));
		return sb.toString();
	}
	
}
