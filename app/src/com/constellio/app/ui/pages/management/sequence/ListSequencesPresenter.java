package com.constellio.app.ui.pages.management.sequence;

import static com.constellio.app.ui.i18n.i18n.$;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;

public class ListSequencesPresenter implements Serializable {
	
	private transient SequenceServices sequenceServices;
	
	private List<SequenceVO> sequenceVOs;

	private ListSequencesView view;
	
	public ListSequencesPresenter(ListSequencesView view) {
		this.view = view;
		initTransientObjects();

		Language sequenceLanguage = getSequenceLanguage();
		sequenceVOs = new ArrayList<>();
		List<AvailableSequence> availableSequences = getAvailableSequences();
		for (AvailableSequence availableSequence : availableSequences) {
			String sequenceId = availableSequence.getCode();
			String sequenceTitle = availableSequence.getTitles().get(sequenceLanguage);
			Long sequenceValue = sequenceServices.getLastSequenceValue(sequenceId);
			SequenceVO sequenceVO = new SequenceVO(sequenceId, sequenceTitle, sequenceValue);
			sequenceVOs.add(sequenceVO);
		}
		view.setSequenceVOs(sequenceVOs);
	}
	
	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		sequenceServices = new SequenceServices(constellioFactories, sessionContext);
	}
	
	private Language getSequenceLanguage() {
		Locale currentLocale = view.getSessionContext().getCurrentLocale();
		String currentLanguageCode = currentLocale.getLanguage();
		Language sequenceLanguage;
		if (Language.isSupported(currentLanguageCode)) {
			sequenceLanguage = Language.withCode(currentLanguageCode);
		} else {
			sequenceLanguage = Language.getAvailableLanguages().get(0);
		}
		return sequenceLanguage;
	}
	
	private List<AvailableSequence> getAvailableSequences() {
		List<AvailableSequence> availableSequences;
		String recordId = view.getRecordId();
		if (recordId != null) {
			availableSequences = sequenceServices.getAvailableSequences(recordId);
		} else {
			throw new IllegalArgumentException("recordId is required");
		}
		return availableSequences;
	}
	
	void saveButtonClicked() {
		for (SequenceVO sequenceVO : sequenceVOs) {
			String sequenceId = sequenceVO.getSequenceId();
			Long value = sequenceVO.getSequenceValue();
			sequenceServices.set(sequenceId, value);
		}
		view.closeWindow();
		view.showMessage($("ListSequencesView.sequencesSaved"));
	}
	
	void cancelButtonClicked() {
		view.closeWindow();
	}
	
	void windowCloseRequested() {
		view.closeWindow();
	}

}
