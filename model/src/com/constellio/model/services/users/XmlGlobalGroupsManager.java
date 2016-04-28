package com.constellio.model.services.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Document;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.XmlGlobalGroup;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_InvalidParent;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_ParentNotFound;

public class XmlGlobalGroupsManager implements GlobalGroupsManager, ConfigUpdatedEventListener {
	public static final String CONFIG_FILE = "/globalGroups.xml";
	private final ConfigManager configManager;
	Map<String, GlobalGroup> cache = new HashMap<>();
	Map<String, List<String>> cacheRelation = new HashMap<>();

	public XmlGlobalGroupsManager(ConfigManager configManager) {
		this.configManager = configManager;

	}

	@Override
	public void initialize() {
		registerListener(configManager);
	}

	@Override
	public GlobalGroup create(String code, String name, List<String> collections, String parent, GlobalGroupStatus status) {
		return new XmlGlobalGroup(code, name, collections, parent, status);
	}

	@Override
	public GlobalGroup create(String code, String parent, GlobalGroupStatus status) {
		return create(code, code, Collections.<String>emptyList(), parent, status);
	}

	@Override
	public void addUpdate(GlobalGroup globalGroup) {
		if (globalGroup.getParent() != null) {
			if (getGlobalGroupWithCode(globalGroup.getParent()) == null) {
				throw new GlobalGroupsManagerRuntimeException_ParentNotFound();
			} else if (globalGroup.getParent().equals(globalGroup.getCode()) || isInHierarchy(globalGroup.getCode(),
					globalGroup.getParent())) {
				throw new GlobalGroupsManagerRuntimeException_InvalidParent(globalGroup.getParent());
			}
		}
		configManager.updateXML(CONFIG_FILE, newAddUpdateGlobalGroupsDocumentAlteration(globalGroup));
	}

	@Override
	public void logicallyRemoveGroup(GlobalGroup globalGroup) {
		configManager.updateXML(CONFIG_FILE, newRemoveGlobalGroupDocumentAlteration(getHierarchy(globalGroup.getCode())));
	}

	@Override
	public GlobalGroup getGlobalGroupWithCode(String code) {
		return cache.get(code);
	}

	@Override
	public GlobalGroup getActiveGlobalGroupWithCode(String code) {
		GlobalGroup group = getGlobalGroupWithCode(code);
		if (group == null || group.getStatus() != GlobalGroupStatus.ACTIVE) {
			return null;
		} else {
			return group;
		}
	}

	@Override
	public List<GlobalGroup> getActiveGroups() {
		List<GlobalGroup> activeGlobalGroups = new ArrayList<>();
		for (GlobalGroup globalGroup : getAllGroups()) {
			if (globalGroup.getStatus() == GlobalGroupStatus.ACTIVE) {
				activeGlobalGroups.add(globalGroup);
			}
		}
		return Collections.unmodifiableList(activeGlobalGroups);
	}

	@Override
	public List<GlobalGroup> getAllGroups() {
		List<GlobalGroup> globalGroups = new ArrayList<>(cache.values());
		return Collections.unmodifiableList(globalGroups);
	}

	@Override
	public void activateGlobalGroupHierarchy(GlobalGroup globalGroup) {
		for (GlobalGroup group : getHierarchy(globalGroup.getCode())) {
			group = group.withStatus(GlobalGroupStatus.ACTIVE);
			addUpdate(group);
		}
	}

	boolean isInHierarchy(String hierarchyCode, String code) {
		if (getChildrenOf(hierarchyCode).contains(code)) {
			return true;
		} else {
			for (String child : getChildrenOf(hierarchyCode)) {
				return isInHierarchy(child, code);
			}
			return false;
		}
	}

	@Override
	public List<GlobalGroup> getHierarchy(String hierarchyCode) {
		List<GlobalGroup> hierarchy = new ArrayList<>();
		List<String> hierarchyCodes = new ArrayList<>();
		getHierarchy(hierarchyCode, hierarchyCodes);
		for (String code : hierarchyCodes) {
			GlobalGroup group = getGlobalGroupWithCode(code);
			if (group != null) {
				hierarchy.add(getGlobalGroupWithCode(code));
			}
		}
		return hierarchy;
	}

	List<String> getHierarchy(String hierarchyCode, List<String> hierarchyCodes) {
		hierarchyCodes.add(hierarchyCode);
		for (String child : getChildrenOf(hierarchyCode)) {
			hierarchyCodes.add(child);
			hierarchyCodes = getHierarchy(child, hierarchyCodes);
		}
		return hierarchyCodes;
	}

	List<String> getChildrenOf(String code) {
		List<String> result = new ArrayList<>();
		if (cacheRelation.containsKey(code)) {
			result = cacheRelation.get(code);
		}
		return result;

	}

	@Override
	public void onConfigUpdated(String configPath) {
		load();
	}

	private void load() {
		Document document = configManager.getXML(CONFIG_FILE).getDocument();
		GlobalGroupsReader reader = newGlobalGroupsReader(document);
		cache = Collections.unmodifiableMap(reader.readAll());
		loadCacheRelations();
	}

	private void loadCacheRelations() {
		cacheRelation = new HashMap<>();
		List children;
		for (Entry<String, GlobalGroup> globalGroupEntry : cache.entrySet()) {
			if (globalGroupEntry.getValue().getParent() != null) {
				String parent = globalGroupEntry.getValue().getParent();
				children = cacheRelation.get(parent);
				if (children == null) {
					children = new ArrayList();
				}
				children.add(globalGroupEntry.getKey());
				cacheRelation.put(parent, children);
			}
		}
	}

	DocumentAlteration newAddUpdateGlobalGroupsDocumentAlteration(final GlobalGroup globalGroup) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newGlobalGroupsWriter(document).addUpdate(globalGroup);
			}
		};
	}

	DocumentAlteration newRemoveGlobalGroupDocumentAlteration(final List<GlobalGroup> globalGroupCodes) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newGlobalGroupsWriter(document).logicallyRemove(globalGroupCodes);
			}
		};
	}

	DocumentAlteration newRemoveCollectionDocumentAlteration(final String collection) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newGlobalGroupsWriter(document).removeCollection(collection);
			}
		};
	}

	void registerListener(ConfigManager configManager) {
		if (!configManager.exist(CONFIG_FILE)) {
			createEmptyGlobalGroupsConfig();
		}
		load();
		configManager.registerListener(CONFIG_FILE, this);
	}

	void createEmptyGlobalGroupsConfig() {
		Document document = new Document();
		GlobalGroupsWriter writer = newGlobalGroupsWriter(document);
		writer.createEmptyGlobalGroups();
		configManager.add(CONFIG_FILE, document);
	}

	GlobalGroupsWriter newGlobalGroupsWriter(Document document) {
		return new GlobalGroupsWriter(document);
	}

	GlobalGroupsReader newGlobalGroupsReader(Document document) {
		return new GlobalGroupsReader(document);
	}

	@Override
	public void close() {

	}

	@Override
	public void removeCollection(String collection) {
		configManager.updateXML(CONFIG_FILE, newRemoveCollectionDocumentAlteration(collection));
	}
}
