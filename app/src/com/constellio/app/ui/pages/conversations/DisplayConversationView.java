package com.constellio.app.ui.pages.conversations;

import com.constellio.app.modules.rm.ui.pages.extrabehavior.ProvideSecurityWithNoUrlParamSupport;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayConversationView extends BaseView, ProvideSecurityWithNoUrlParamSupport {
	String CONVERSATION_ID_PARAM_KEY = "CONVERSATION_ID";
	String TARGETED_MESSAGE_ID_PARAM_KEY = "TARGETED_MESSAGE_ID";
}
