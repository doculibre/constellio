package com.constellio.app.ui.pages.synonyms;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SynonymsConfigurationsManager;

import java.util.Arrays;
import java.util.List;

public class EditSynonymsPresenter extends BasePresenter<EditSynonymsView> {
	List<String> synonyms;
	SynonymsConfigurationsManager synonymsConfigurationsManager;

	public EditSynonymsPresenter(EditSynonymsView view) {
		super(view);
		synonymsConfigurationsManager = modelLayerFactory.getSynonymsConfigurationsManager();
		this.synonyms = synonymsConfigurationsManager.getSynonyms(collection);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void saveButtonClicked(String synonymsAsOneString) {
		String[] stringList = synonymsAsOneString.split("\\r\\n|\\n|\\r");
		synonyms = Arrays.asList(stringList);
		synonymsConfigurationsManager.setSynonyms(collection, synonyms);
		view.navigate().to(CoreViews.class).displaySynonyms();
	}

	public String getSynonmsAsOneString() {
		StringBuilder stringBuilder = new StringBuilder();

		for (String string : synonyms) {
			stringBuilder.append(string).append("\n");
		}

		return stringBuilder.toString();
	}

	public void cancelButtonClicked() {
		view.navigate().to(CoreViews.class).displaySynonyms();
	}

}
