package com.constellio.app.ui.pages.conversations;

import com.constellio.app.modules.rm.ui.pages.extrabehavior.SecurityWithNoUrlParamSupport;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.conversations.ConversationViewImpl;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

import java.util.Map;

public class DisplayConversationViewImpl extends BaseViewImpl implements DisplayConversationView {

	private final DisplayConversationPresenter presenter;
	private final String collection;
	private final SessionContext sessionContext;

	public DisplayConversationViewImpl(SessionContext sessionContext, Map<String, String> paramsMap) {
		this.sessionContext = sessionContext;
		collection = sessionContext.getCurrentCollection();
		presenter = new DisplayConversationPresenter(collection, sessionContext, paramsMap);
	}

	public DisplayConversationViewImpl() {
		this(ConstellioUI.getCurrentSessionContext(), ParamUtils.getParamsMap());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return new ConversationViewImpl(collection, sessionContext, presenter.getConversationFacetHandler(), presenter.getConversationId(), presenter.getTargetedMessageId());
	}

	@Override
	public SecurityWithNoUrlParamSupport getSecurityWithNoUrlParamSupport() {
		return presenter;
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return false;
	}
}
