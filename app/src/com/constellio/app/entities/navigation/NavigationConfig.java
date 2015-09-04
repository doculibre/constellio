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
package com.constellio.app.entities.navigation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.data.utils.KeyListMap;

public class NavigationConfig implements Serializable {
	private final KeyListMap<String, NavigationItem> navigation;
	private final KeyListMap<String, PageItem> fragments;
	private final Set<String> codes;

	public NavigationConfig() {
		navigation = new KeyListMap<>();
		fragments = new KeyListMap<>();
		codes = new HashSet<>();
	}

	public void add(String group, NavigationItem item) {
		add(group, item, navigation);
	}

	public void replace(String group, NavigationItem item) {
		replace(group, item, navigation);
	}

	public void add(String group, PageItem item) {
		add(group, item, fragments);
	}

	public List<NavigationItem> getNavigation(String group) {
		return navigation.get(group);
	}

	public NavigationItem getNavigationItem(String group, String code) {
		for (NavigationItem item : getNavigation(group)) {
			if (code.equals(item.getCode())) {
				return item;
			}
		}
		return null;
	}

	public List<PageItem> getFragments(String group) {
		return fragments.get(group);
	}

	private <V extends CodedItem> void add(String group, V value, KeyListMap<String, V> map) {
		String code = group + "." + value.getCode();
		if (codes.contains(code)) {
			throw new Error("Item already in configuration: " + code);
		}
		codes.add(code);
		map.add(group, value);
	}

	private <V extends CodedItem> void replace(String group, V value, KeyListMap<String, V> map) {
		String code = group + "." + value.getCode();
		if (!codes.contains(code)) {
			throw new Error("Item not in configuration: " + code);
		}
		List<V> oldItems = map.get(group);
		List<V> items = new ArrayList<>(oldItems.size());
		for (V item : oldItems) {
			items.add(value.getCode().equals(item.getCode()) ? value : item);
		}
		map.set(group, items);
	}
}
