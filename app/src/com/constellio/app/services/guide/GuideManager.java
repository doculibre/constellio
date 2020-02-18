package com.constellio.app.services.guide;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.data.dao.managers.StatefulService;

import java.util.Locale;

public class GuideManager implements StatefulService {
	private final BaseView view;
	private Locale language;

	public GuideManager(BaseView baseView) {
		this.view = baseView;
	}

	@Override
	public void initialize() {
		language = view.getSessionContext().getCurrentLocale();

	}

	@Override
	public void close() {

	}

	public void setUrl() {

		//String guideUrl = view.getGuideUrl();

	}
}
