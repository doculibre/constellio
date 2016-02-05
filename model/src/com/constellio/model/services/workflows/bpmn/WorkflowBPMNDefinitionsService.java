package com.constellio.model.services.workflows.bpmn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;

public class WorkflowBPMNDefinitionsService {

	private FoldersLocator foldersLocator;

	private FileService fileService;

	public WorkflowBPMNDefinitionsService(FoldersLocator foldersLocator,
			FileService fileService) {
		this.foldersLocator = foldersLocator;
		this.fileService = fileService;
	}

	public List<String> getAvailableWorkflowDefinitions() {
		List<String> bpmnFiles = new ArrayList<>();
		for (File bpmnFile : foldersLocator.getBPMNsFolder().listFiles()) {
			if (bpmnFile.getName().endsWith(".bpmn20.xml")) {
				bpmnFiles.add(bpmnFile.getName());
			}
		}
		return bpmnFiles;
	}

	public List<String> getAvailableWorkflowDefinitionMappingKeys(String bpmnFileName) {
		List<String> mappingKeys = new ArrayList<>();

		File bpmnFile = getBPMNFile(bpmnFileName);

		Document document = getDocumentFromFile(bpmnFile);
		Namespace activitiNamespace = document.getRootElement().getNamespace("activiti");

		Element rootElement = document.getRootElement();
		getMappingKeys(rootElement, mappingKeys, activitiNamespace);

		return mappingKeys;
	}

	private void getMappingKeys(Element element, List<String> mappingKeys, Namespace activitiNamespace) {
		String groupsString = element.getAttributeValue("candidateGroups", activitiNamespace);
		if (groupsString != null) {
			for (String groupName : groupsString.split(",")) {
				mappingKeys.add(groupName);
			}
		}
		for (Element childElement : element.getChildren()) {
			getMappingKeys(childElement, mappingKeys, activitiNamespace);
		}
	}

	public WorkflowDefinition getWorkflowDefinition(String bpmnFileName, Map<String, String> mapping,
			WorkflowConfiguration workflowConfiguration) {

		File bpmnFile = getBPMNFile(bpmnFileName);

		Document document = getDocumentFromFile(bpmnFile);

		return newBPMNParser(document, mapping, workflowConfiguration).build();
	}

	BPMNParser newBPMNParser(Document document, Map<String, String> mapping, WorkflowConfiguration workflowConfiguration) {
		return new BPMNParser(document, mapping, workflowConfiguration);
	}

	File getBPMNFile(String bpmnFileName) {
		return new File(foldersLocator.getBPMNsFolder(), bpmnFileName);
	}

	Document getDocumentFromFile(File file) {
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(file);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}
}
