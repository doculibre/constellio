package com.constellio.app.modules.es.connectors.smb.queue;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDispatchJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbUnmodifiedRetrievalJob;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SmbJobQueueSortedImplAcceptanceTest extends ConstellioTest {
	// First traversal
	//
	// smb://ip/share/
	// smb://ip/share/file.txt
	// smb://ip/share/file3.txt
	// smb://ip/share/folder/
	// smb://ip/share/folder/another_file.txt
	//
	// Changes after first traversal and before second traversal
	//
	// smb://ip/share/file4.txt <--- New file (parent share)
	// smb://ip/share/file3.txt <--- Modified file (parent share)
	// smb://ip/share/file.txt <--- Non modified file (parent share)
	//
	// smb://ip/share/folder2/ <--- New folder (parent share)
	// smb://ip/share/folder2/file5.txt <--- New file (parent folder2)

	@Test
	public void givenFirstTraversalWhenGettingJobsThenGetJobsInExpectedOrder() {
		SmbJobQueue jobQueue = new SmbJobQueueSortedImpl();

		SmbNewRetrievalJob shareRetrievalJob = Mockito.mock(SmbNewRetrievalJob.class);
		when(shareRetrievalJob.getUrl()).thenReturn("smb://ip/share/");
		when(shareRetrievalJob.getType()).thenReturn(SmbJobType.NEW_FOLDER_JOB);

		SmbNewRetrievalJob file3RetrievalJob = Mockito.mock(SmbNewRetrievalJob.class);
		when(file3RetrievalJob.getUrl()).thenReturn("smb://ip/share/file3.txt");
		when(file3RetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		SmbNewRetrievalJob fileRetrievalJob = Mockito.mock(SmbNewRetrievalJob.class);
		when(fileRetrievalJob.getUrl()).thenReturn("smb://ip/share/file.txt");
		when(fileRetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		SmbDispatchJob folder2DispatchJob = Mockito.mock(SmbDispatchJob.class);
		when(folder2DispatchJob.getUrl()).thenReturn("smb://ip/share/folder/");
		when(folder2DispatchJob.getType()).thenReturn(SmbJobType.DISPATCH_JOB);

		// Add jobs in any (wrong) order
		jobQueue.add(folder2DispatchJob);
		jobQueue.add(fileRetrievalJob);
		jobQueue.add(file3RetrievalJob);
		jobQueue.add(shareRetrievalJob);

		// Get jobs in the expected order
		assertThat(jobQueue.poll()).isEqualTo(shareRetrievalJob);
		assertThat(jobQueue.poll()).isEqualTo(fileRetrievalJob);
		assertThat(jobQueue.poll()).isEqualTo(file3RetrievalJob);
		assertThat(jobQueue.poll()).isEqualTo(folder2DispatchJob);

	}

	@Test
	public void givenSecondTraversalWithChangesWhenGettingJobsThenGetJobsInExpectedOrder() {
		SmbJobQueue jobQueue = new SmbJobQueueSortedImpl();

		SmbNewRetrievalJob file4RetrievalJob = Mockito.mock(SmbNewRetrievalJob.class);
		when(file4RetrievalJob.getUrl()).thenReturn("smb://ip/share/file4.txt");
		when(file4RetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		SmbUnmodifiedRetrievalJob fileRetrievalJob = Mockito.mock(SmbUnmodifiedRetrievalJob.class);
		when(fileRetrievalJob.getUrl()).thenReturn("smb://ip/share/file.txt");
		when(fileRetrievalJob.getType()).thenReturn(SmbJobType.UNMODIFIED_JOB);

		SmbDispatchJob folder2DispatchJob = Mockito.mock(SmbDispatchJob.class);
		when(folder2DispatchJob.getUrl()).thenReturn("smb://ip/share/folder2/");
		when(folder2DispatchJob.getType()).thenReturn(SmbJobType.DISPATCH_JOB);

		// Add jobs in any (wrong) order
		jobQueue.add(folder2DispatchJob);
		jobQueue.add(fileRetrievalJob);
		jobQueue.add(file4RetrievalJob);

		// Get jobs in the expected order
		assertThat(jobQueue.poll()).isEqualTo(fileRetrievalJob);
		assertThat(jobQueue.poll()).isEqualTo(file4RetrievalJob);
		assertThat(jobQueue.poll()).isEqualTo(folder2DispatchJob);

	}
}