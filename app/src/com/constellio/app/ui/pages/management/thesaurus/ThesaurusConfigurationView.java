package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.ui.pages.base.BaseView;

interface ThesaurusConfigurationView extends BaseView {

	void removeAllTheSelectedFile();

	void loadDescriptionFieldsWithFileValue();

	void toNoThesaurusAvailable();

	void setSKOSSaveButtonEnabled(boolean enabled);

}
