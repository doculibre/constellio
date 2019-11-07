package com.constellio.app.entities.navigation;

import com.constellio.data.utils.KeyListMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public void add(String group, NavigationItem item, int index) {
		add(group, item, index, navigation);
	}

	public void replace(String group, NavigationItem item) {
		replace(group, item, navigation);
	}

	public void replace(String group, PageItem item) {
		replace(group, item, fragments);
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

	public boolean hasNavigationItem(String group, String code) {
		return codes.contains(group + "." + code);
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

	private <V extends CodedItem> void add(String group, V value, int index, KeyListMap<String, V> map) {
		String code = group + "." + value.getCode();
		if (codes.contains(code)) {
			throw new Error("Item already in configuration: " + code);
		}
		codes.add(code);
		if (map.get(group).size() == 0) {
			map.add(group, value);
		} else {
			map.get(group).add(index, value);
		}
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
