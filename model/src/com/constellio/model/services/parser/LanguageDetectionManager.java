/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
