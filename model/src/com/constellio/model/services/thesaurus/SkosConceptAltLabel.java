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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SkosConceptAltLabel implements Serializable {

	private SkosConcept skosConcept;

	private Locale locale;

	private Set<String> values = new HashSet<>();

	private Set<String> parsedForSearchValues = new HashSet<>();

	public SkosConcept getSkosConcept() {
		return skosConcept;
	}

	public void setSkosConcept(SkosConcept skosConcept) {
		this.skosConcept = skosConcept;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Set<String> getValues() {
		return Collections.unmodifiableSet(values);
	}

	public Set<String> getValuesParsedForSearch() {
		return Collections.unmodifiableSet(parsedForSearchValues);
	}

	public void setValues(Set<String> values) {
		this.values = values;
		this.parsedForSearchValues = SkosUtil.parseForSearch(values);
	}

	public void addValue(String value) {
		this.values.add(value);
		this.parsedForSearchValues.add(SkosUtil.parseForSearch(value));
	}
}
