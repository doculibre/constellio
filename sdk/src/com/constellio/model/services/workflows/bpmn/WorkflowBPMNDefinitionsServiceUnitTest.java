package com.constellio.model.services.workflows.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.sdk.tests.ConstellioTest;

public class WorkflowBPMNDefinitionsServiceUnitTest extends ConstellioTest {

	String file1Name = "firstFile.bpmn20.xml";
	String file2Name = "secondFile.bpmn20.xml";
	String otherFileName = "otherFile.txt";
	WorkflowBPMNDefinitionsService definitionsService;
	@Mock FoldersLocator foldersLocator;
	@Mock File bpmnFolder, bpmnFile1, bpmnFile2, otherFile;
	@Mock Document bpmnDocument1;
	@Mock WorkflowConfiguration workflowConfiguration;
	@Mock BPMNParser parser;
	Map<String, String> mapping = new HashMap<>();

	@Before
	public void setUp()
			throws Exception {
		definitionsService = spy(new WorkflowBPMNDefinitionsService(foldersLocator, null));
		mapping.put("entry1", "value1");

		when(foldersLocator.getBPMNsFolder()).thenReturn(bpmnFolder);
		when(bpmnFile1.getName()).thenReturn(file1Name);
		when(bpmnFile2.getName()).thenReturn(file2Name);
		when(otherFile.getName()).thenReturn(otherFileName);
		when(bpmnFolder.listFiles()).thenReturn(new File[] { bpmnFile1, bpmnFile2, otherFile });

		doReturn(bpmnDocument1).when(definitionsService).getDocumentFromFile(bpmnFile1);
		doReturn(bpmnFile1).when(definitionsService).getBPMNFile(file1Name);
		doReturn(parser).when(definitionsService).newBPMNParser(bpmnDocument1, mapping, workflowConfiguration);
	}

	@Test
	public void whenGettingAvailableWorkflowsThenAllWorkflowsReturned()
			throws Exception {
		List<String> returnedWorkflows = definitionsService.getAvailableWorkflowDefinitions();
		assertThat(returnedWorkflows).containsOnly(file1Name, file2Name);
	}

	@Test
	public void whenGettingWorkflowThenWorkflowReturned()
			throws Exception {
		definitionsService.getWorkflowDefinition(file1Name, mapping, workflowConfiguration);
		verify(parser).build();
	}
}
