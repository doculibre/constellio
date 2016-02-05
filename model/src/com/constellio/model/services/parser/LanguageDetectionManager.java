package com.constellio.model.services.parser;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.services.parser.LanguageDetectionServicesRuntimeException.LanguageDetectionManagerRuntimeException_CannotDetectLanguage;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class LanguageDetectionManager implements StatefulService {

	private static Logger LOGGER = LoggerFactory.getLogger(LanguageDetectionManager.class);

	private static boolean schemasLoaded;

	private File languageProfiles;

	public LanguageDetectionManager(File languageProfiles) {
		this.languageProfiles = languageProfiles;
	}

	@Override
	public void initialize() {
		if (!schemasLoaded) {
			try {
				DetectorFactory.loadProfile(languageProfiles);

				//Langdetect uses random sampling for avoiding local noises(person name, place name and so on),
				//so the language detections of the same document might differ for every time.
				//This feature is disabled since it cause to much random behaviors
				DetectorFactory.setSeed(0);

			} catch (LangDetectException e) {
				throw new LanguageDetectionServicesRuntimeException("Cannot load schemas", e);
			}
			schemasLoaded = true;
		}
	}

	public String tryDetectLanguage(String content) {
		try {
			return detectLanguage(content);
		} catch (LanguageDetectionServicesRuntimeException e) {
			LOGGER.info(e.getMessage());
			return null;
		}
	}

	public String detectLanguage(String content) {
		try {

			Detector detector = DetectorFactory.create();
			if (content.length() > 200) {
				int firstSpaceAfter200Characters = content.indexOf(" ", 200);
				if (firstSpaceAfter200Characters != -1) {
					content = content.substring(0, firstSpaceAfter200Characters);
				}
			}
			detector.append(content);
			return detector.detect();

		} catch (LangDetectException e) {
			throw new LanguageDetectionManagerRuntimeException_CannotDetectLanguage(content, e);
		}
	}

	@Override
	public void close() {

	}
}
