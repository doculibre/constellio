package com.constellio.model.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class StringNormalizer {

	public static String normalize(String name) {
		if (!Normalizer.isNormalized(name, Form.NFC)) {
			return Normalizer.normalize(name, Normalizer.Form.NFC);
		}
		return name;
	}

}
