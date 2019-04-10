/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 * <p>
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.constellio.model.services.thesaurus;

import com.constellio.model.services.thesaurus.util.SkosUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class ThesaurusLabel {

	private String key;

	private Map<Locale, String> values = new HashMap<Locale, String>();

	private Map<Locale, String> parsedForSearchValues = new HashMap<Locale, String>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Map<Locale, String> getValues() {
		return Collections.unmodifiableMap(values);
	}

	public void setValues(Map<Locale, String> values) {
		this.values = values;
		this.parsedForSearchValues = new HashMap<>();
		for (Map.Entry<Locale, String> entry : values.entrySet()) {
			parsedForSearchValues.put(entry.getKey(), SkosUtil.parseForSearch(entry.getValue()));
		}
	}

	public String getValue(Locale locale) {
		return values.get(locale);
	}

	public void setValue(String value, Locale locale) {
		values.put(locale, value);
		parsedForSearchValues.put(locale, SkosUtil.parseForSearch(value));
	}

	public String getParsedForSearchValue(Locale locale) {
		return parsedForSearchValues.get(locale);
	}
}
