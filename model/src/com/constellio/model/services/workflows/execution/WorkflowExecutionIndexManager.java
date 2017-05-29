package com.constellio.model.services.workflows.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.workflows.execution.WorkflowExecutionIndexRuntimeException.WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound;
import com.constellio.model.services.workflows.execution.xml.WorkflowExecutionIndexReader;
import com.constellio.model.services.workflows.execution.xml.WorkflowExecutionIndexWriter;
import com.constellio.model.services.workflows.execution.xml.WorkflowExecutionReader;
import com.constellio.model.services.workflows.execution.xml.WorkflowExecutionWriter;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;

public class WorkflowExecutionIndexManager
		implements StatefulService, OneXMLConfigPerCollectionManagerListener<Map<String, Boolean>> {

	private static final String XML_EXTENSION = ".xml";
	private static String INDEX_CONFIG = "/workflows/execution/index.xml";
	private static String EXECUTION_FOLDER = "/workflows/execution";
	private static OneXMLConfigPerCollectionManager<List<WorkflowExecution>> oneXMLConfigPerCollectionManager;
	private final ConfigManager configManager;
	private final CollectionsListManager collectionsListManager;
	private final ConstellioCacheManager cacheManager; 
	private final Map<String, Boolean> workflowsExecutionsMap;

	public WorkflowExecutionIndexManager(ConfigManager configManager, CollectionsListManager collectionsListManager, ConstellioCacheManager cacheManager) {
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.cacheManager = cacheManager;
		this.workflowsExecutionsMap = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		ConstellioCache cache = cacheManager.getCache(WorkflowExecutionIndexManager.class.getName());
		oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager(configManager, collectionsListManager,
				INDEX_CONFIG, xmlConfigReader(), this, cache);
	}

	public void addUpdate(WorkflowExecution workflowExecution) {
		String collection = workflowExecution.getCollection();
		String filePath =
				collection + File.separator + EXECUTION_FOLDER + File.separator + workflowExecution.getId() + XML_EXTENSION;
		if (!workflowsExecutionsMap.containsKey(collection + workflowExecution.getId())) {
			oneXMLConfigPerCollectionManager.updateXML(collection,
					newAddWorkflowExcecutionIndexDocumentAlteration(workflowExecution));
		} else {
			oneXMLConfigPerCollectionManager.updateXML(collection,
					newUpdateWorkflowExcecutionIndexDocumentAlteration(workflowExecution));
			configManager.delete(filePath);
		}
		configManager.createXMLDocumentIfInexistent(filePath, newAddWorkflowExcecutionDocumentAlteration(workflowExecution));
	}

	public WorkflowExecution getWorkflow(String collection, String id) {
		String correctedId = correctId(collection, id);
		if (!workflowsExecutionsMap.containsKey(collection + correctedId)) {
			throw new WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound(correctedId, collection);
		}
		String filePath = collection + File.separator + EXECUTION_FOLDER + File.separator + correctedId + XML_EXTENSION;
		Document document = configManager.getXML(filePath).getDocument();
		WorkflowExecution workflowExecution = newWorkflowExecutionReader(document).read(collection, correctedId, document);
		return workflowExecution;
	}

	public void remove(WorkflowExecution workflowExecution) {
		String collection = workflowExecution.getCollection();
		if (!workflowsExecutionsMap.containsKey(collection + workflowExecution.getId())) {
			throw new WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound(workflowExecution.getId(), collection);
		}
		String filePath =
				collection + File.separator + EXECUTION_FOLDER + File.separator + workflowExecution.getId() + XML_EXTENSION;
		oneXMLConfigPerCollectionManager.updateXML(collection,
				newRemoveWorkflowExcecutionIndexDocumentAlteration(workflowExecution));
		configManager.delete(filePath);
	}

	public List<WorkflowExecution> getNextWorkflowIdsWaitingForSystemProcessing(String collection) {
		List<WorkflowExecution> workflowExecutions = new ArrayList<>();
		for (Map.Entry<String, Boolean> workflowExecutionMap : workflowsExecutionsMap.entrySet()) {
			if (workflowExecutionMap.getValue()) {
				workflowExecutions.add(getWorkflow(collection, workflowExecutionMap.getKey()));
			}
		}
		return workflowExecutions;
	}

	private String correctId(String collection, String id) {
		if (id.startsWith(collection)) {
			return id.substring(collection.length());
		} else {
			return id;
		}
	}

	public void markAsWaitingForSystem(String collection, String id) {
		WorkflowExecution workflowExecution = getWorkflow(collection, id);
		workflowExecution.setMarkAsWaitingForSystem(true);
		addUpdate(workflowExecution);
	}

	public void markAsNotWaitingForSystem(String collection, String id) {
		WorkflowExecution workflowExecution = getWorkflow(collection, id);
		workflowExecution.setMarkAsWaitingForSystem(false);
		addUpdate(workflowExecution);
	}

	@Override
	public void onValueModified(String collection, Map<String, Boolean> workflowExecutionsMap) {
		this.workflowsExecutionsMap.clear();
		for (Map.Entry<String, Boolean> workflowExecutionMap : workflowExecutionsMap.entrySet()) {
			workflowsExecutionsMap
					.put(collection + workflowExecutionMap.getKey(), Boolean.valueOf(workflowExecutionMap.getValue()));
		}
	}

	public void createCollectionWorkflowsExecutionIndex(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				WorkflowExecutionIndexWriter writer = newWorkflowExecutionIndexWriter(document);
				writer.createEmptyWorkflowsExecutionIndex();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	private XMLConfigReader<Map<String, Boolean>> xmlConfigReader() {
		return new XMLConfigReader<Map<String, Boolean>>() {
			@Override
			public Map<String, Boolean> read(String collection, Document document) {
				return newWorkflowExecutionIndexReader(document).getWorkflowsExcecution();
			}
		};
	}

	DocumentAlteration newAddWorkflowExcecutionIndexDocumentAlteration(final WorkflowExecution workflowExecution) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newWorkflowExecutionIndexWriter(document).add(workflowExecution);
			}
		};
	}

	DocumentAlteration newUpdateWorkflowExcecutionIndexDocumentAlteration(final WorkflowExecution workflowExecution) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newWorkflowExecutionIndexWriter(document).update(workflowExecution);
			}
		};
	}

	DocumentAlteration newRemoveWorkflowExcecutionIndexDocumentAlteration(final WorkflowExecution workflowExecution) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newWorkflowExecutionIndexWriter(document).remove(workflowExecution);
			}
		};
	}

	DocumentAlteration newAddWorkflowExcecutionDocumentAlteration(final WorkflowExecution workflowExecution) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newWorkflowExecutionWriter(document).add(workflowExecution);
			}
		};
	}

	private WorkflowExecutionIndexReader newWorkflowExecutionIndexReader(Document document) {
		return new WorkflowExecutionIndexReader(document);
	}

	private WorkflowExecutionIndexWriter newWorkflowExecutionIndexWriter(Document document) {
		return new WorkflowExecutionIndexWriter(document);
	}

	private WorkflowExecutionWriter newWorkflowExecutionWriter(Document document) {
		return new WorkflowExecutionWriter(document);
	}

	private WorkflowExecutionReader newWorkflowExecutionReader(Document document) {
		return new WorkflowExecutionReader(document);
	}

	@Override
	public void close() {

	}
}
