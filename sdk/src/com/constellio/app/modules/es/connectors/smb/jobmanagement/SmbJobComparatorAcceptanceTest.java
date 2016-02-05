package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDeleteJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDispatchJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbExistingFolderRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbModifiedDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewFolderRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNullJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbSeedJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbUnmodifiedDocumentRetrievalJob;
import com.constellio.sdk.tests.ConstellioTest;

public class SmbJobComparatorAcceptanceTest extends ConstellioTest {

	@Test
	public void givenNewParentAndNewChildDocumentWhenComparingThenChildBeforeParent() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbNewFolderRetrievalJob shareRetrievalJob = Mockito.mock(SmbNewFolderRetrievalJob.class);
		when(shareRetrievalJob.getUrl()).thenReturn("smb://ip/share/");
		when(shareRetrievalJob.getType()).thenReturn(SmbJobType.NEW_FOLDER_JOB);

		SmbNewDocumentRetrievalJob fileRetrievalJob = Mockito.mock(SmbNewDocumentRetrievalJob.class);
		when(fileRetrievalJob.getUrl()).thenReturn("smb://ip/share/file.txt");
		when(fileRetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		assertThat(jobComparator.compare(fileRetrievalJob, shareRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenNewParentAndNewChildFolderWhenComparingThenParentBeforeChild() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbNewFolderRetrievalJob shareRetrievalJob = Mockito.mock(SmbNewFolderRetrievalJob.class);
		when(shareRetrievalJob.getUrl()).thenReturn("smb://ip/share/");
		when(shareRetrievalJob.getType()).thenReturn(SmbJobType.NEW_FOLDER_JOB);

		SmbNewFolderRetrievalJob folderRetrievalJob = Mockito.mock(SmbNewFolderRetrievalJob.class);
		when(folderRetrievalJob.getUrl()).thenReturn("smb://ip/share/folder/");
		when(folderRetrievalJob.getType()).thenReturn(SmbJobType.NEW_FOLDER_JOB);

		assertThat(jobComparator.compare(shareRetrievalJob, folderRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenExistingParentAndNewChildDocumentWhenComparingThenChildBeforeParent() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbExistingFolderRetrievalJob shareRetrievalJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(shareRetrievalJob.getUrl()).thenReturn("smb://ip/share/");
		when(shareRetrievalJob.getType()).thenReturn(SmbJobType.EXISTING_FOLDER_JOB);

		SmbNewDocumentRetrievalJob newFileRetrievalJob = Mockito.mock(SmbNewDocumentRetrievalJob.class);
		when(newFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/newfile.txt");
		when(newFileRetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		assertThat(jobComparator.compare(shareRetrievalJob, newFileRetrievalJob)).isEqualTo(1);
	}

	@Test
	public void givenExistingParentAndNewChildFolderWhenComparingThenLowerLevelFirst() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbExistingFolderRetrievalJob shareRetrievalJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(shareRetrievalJob.getUrl()).thenReturn("smb://ip/share/");
		when(shareRetrievalJob.getType()).thenReturn(SmbJobType.EXISTING_FOLDER_JOB);

		SmbNewFolderRetrievalJob newFolderRetrievalJob = Mockito.mock(SmbNewFolderRetrievalJob.class);
		when(newFolderRetrievalJob.getUrl()).thenReturn("smb://ip/share/newfolder/");
		when(newFolderRetrievalJob.getType()).thenReturn(SmbJobType.NEW_FOLDER_JOB);

		assertThat(jobComparator.compare(shareRetrievalJob, newFolderRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenGrandParentAndNewChildDocumentWhenComparingThenGrandParentBeforeChild() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbExistingFolderRetrievalJob shareRetrievalJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(shareRetrievalJob.getUrl()).thenReturn("smb://ip/share/");

		SmbNewDocumentRetrievalJob fileRetrievalJob = Mockito.mock(SmbNewDocumentRetrievalJob.class);
		when(fileRetrievalJob.getUrl()).thenReturn("smb://ip/share/folder/file.txt");

		assertThat(jobComparator.compare(shareRetrievalJob, fileRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenSameLevelFoldersWhenComparingThenSortByAlphabeticalOrder() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbExistingFolderRetrievalJob folder1RetrievalJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(folder1RetrievalJob.getUrl()).thenReturn("smb://ip/share/folder1/");
		when(folder1RetrievalJob.getType()).thenReturn(SmbJobType.EXISTING_FOLDER_JOB);

		SmbExistingFolderRetrievalJob folder2RetrievalJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(folder2RetrievalJob.getUrl()).thenReturn("smb://ip/share/folder2/");
		when(folder2RetrievalJob.getType()).thenReturn(SmbJobType.EXISTING_FOLDER_JOB);

		assertThat(jobComparator.compare(folder1RetrievalJob, folder2RetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenNewFileAndModifiedFileWhenComparingThenNewFileBeforeModifiedFile() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbNewDocumentRetrievalJob newFileRetrievalJob = Mockito.mock(SmbNewDocumentRetrievalJob.class);
		when(newFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/newFile.txt");
		when(newFileRetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		SmbModifiedDocumentRetrievalJob modifiedFileRetrievalJob = Mockito.mock(SmbModifiedDocumentRetrievalJob.class);
		when(modifiedFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/modFile.txt");
		when(modifiedFileRetrievalJob.getType()).thenReturn(SmbJobType.MODIFIED_DOCUMENT_JOB);

		assertThat(jobComparator.compare(newFileRetrievalJob, modifiedFileRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenModifiedFileAndNonModifiedFileWhenComparingThenModifiedFileBeforeNonModifiedFile() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbModifiedDocumentRetrievalJob modifiedFileRetrievalJob = Mockito.mock(SmbModifiedDocumentRetrievalJob.class);
		when(modifiedFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/modFile.txt");
		when(modifiedFileRetrievalJob.getType()).thenReturn(SmbJobType.MODIFIED_DOCUMENT_JOB);

		SmbUnmodifiedDocumentRetrievalJob nonModifiedFileRetrievalJob = Mockito.mock(SmbUnmodifiedDocumentRetrievalJob.class);
		when(nonModifiedFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/nonFile.txt");
		when(nonModifiedFileRetrievalJob.getType()).thenReturn(SmbJobType.UNMODIFIED_DOCUMENT_JOB);

		assertThat(jobComparator.compare(modifiedFileRetrievalJob, nonModifiedFileRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenModifiedFildeAndDispatchWhenComparingThenModifiedBeforeDispatch() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbModifiedDocumentRetrievalJob modifiedFileRetrievalJob = Mockito.mock(SmbModifiedDocumentRetrievalJob.class);
		when(modifiedFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/modFile.txt");
		when(modifiedFileRetrievalJob.getType()).thenReturn(SmbJobType.MODIFIED_DOCUMENT_JOB);

		SmbDispatchJob dispatchJob = Mockito.mock(SmbDispatchJob.class);
		when(dispatchJob.getUrl()).thenReturn("smb://ip/share/folder/"); // Would still pass even if not considering level
		when(dispatchJob.getType()).thenReturn(SmbJobType.DISPATCH_JOB);

		assertThat(jobComparator.compare(modifiedFileRetrievalJob, dispatchJob)).isEqualTo(-1);
	}

	@Test
	public void givenDispatchAndUnmodifiedWhenComparingThenDispatchBeforeUnmodified() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbDispatchJob dispatchJob = Mockito.mock(SmbDispatchJob.class);
		when(dispatchJob.getUrl()).thenReturn("smb://ip/share/folder/");
		when(dispatchJob.getType()).thenReturn(SmbJobType.DISPATCH_JOB);

		SmbUnmodifiedDocumentRetrievalJob unmodifiedFileRetrievalJob = Mockito.mock(SmbUnmodifiedDocumentRetrievalJob.class);
		when(unmodifiedFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/folder/unmodifiedFile.txt");
		when(unmodifiedFileRetrievalJob.getType()).thenReturn(SmbJobType.UNMODIFIED_DOCUMENT_JOB);

		assertThat(jobComparator.compare(dispatchJob, unmodifiedFileRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void givenUnmodifiedFileAndExistingFolderWhenComparingThenFileBeforeFolder() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbUnmodifiedDocumentRetrievalJob unmodifiedFileRetrievalJob = Mockito.mock(SmbUnmodifiedDocumentRetrievalJob.class);
		when(unmodifiedFileRetrievalJob.getUrl()).thenReturn("smb://ip/share/folder/unmodifiedFile.txt");
		when(unmodifiedFileRetrievalJob.getType()).thenReturn(SmbJobType.UNMODIFIED_DOCUMENT_JOB);

		SmbExistingFolderRetrievalJob existingFolderJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(existingFolderJob.getUrl()).thenReturn("smb://ip/share/folder/");
		when(existingFolderJob.getType()).thenReturn(SmbJobType.EXISTING_FOLDER_JOB);

		assertThat(jobComparator.compare(unmodifiedFileRetrievalJob, existingFolderJob)).isEqualTo(-1);
	}

	@Test
	public void givenExistingFolderAndQueuedDeleteJobWhenComparingThenExistingFolderBeforeDelete() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbExistingFolderRetrievalJob existingFolderJob = Mockito.mock(SmbExistingFolderRetrievalJob.class);
		when(existingFolderJob.getUrl()).thenReturn("smb://ip/share/folder/");
		when(existingFolderJob.getType()).thenReturn(SmbJobType.EXISTING_FOLDER_JOB);

		SmbDeleteJob deleteJob = Mockito.mock(SmbDeleteJob.class);
		when(deleteJob.getUrl()).thenReturn("smb://ip/share/folder/deleteFile.txt");
		when(deleteJob.getType()).thenReturn(SmbJobType.DELETE_JOB);

		assertThat(jobComparator.compare(existingFolderJob, deleteJob)).isEqualTo(-1);
	}

	@Test
	public void givenQueuedDeleteJobAndNullJobWhenComparingThenDeleteBeforeNullJob() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbDeleteJob deleteJob = Mockito.mock(SmbDeleteJob.class);
		when(deleteJob.getUrl()).thenReturn("smb://ip/share/folder/deleteFile.txt");
		when(deleteJob.getType()).thenReturn(SmbJobType.DELETE_JOB);

		SmbNullJob nullJob = Mockito.mock(SmbNullJob.class);
		when(nullJob.getUrl()).thenReturn("smb://ip/share/folder/nulljob");
		when(nullJob.getType()).thenReturn(SmbJobType.NULL_JOB);

		assertThat(jobComparator.compare(deleteJob, nullJob)).isEqualTo(-1);
	}

	@Test
	public void givenSeedJobAndNewDocumentRetrievalJobWhenComparingThenSeedBeforeNewDocumenRetrieval() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbSeedJob seedJob = Mockito.mock(SmbSeedJob.class);
		when(seedJob.getUrl()).thenReturn("smb://ip/share/");
		when(seedJob.getType()).thenReturn(SmbJobType.SEED_JOB);

		SmbNewDocumentRetrievalJob newDocumentRetrievalJob = Mockito.mock(SmbNewDocumentRetrievalJob.class);
		when(newDocumentRetrievalJob.getUrl()).thenReturn("smb://ip/share/document");
		when(newDocumentRetrievalJob.getType()).thenReturn(SmbJobType.NEW_DOCUMENT_JOB);

		assertThat(jobComparator.compare(seedJob, newDocumentRetrievalJob)).isEqualTo(-1);
	}

	@Test
	public void given2SeedJobsWhenComparingThenSeedABeforeSeedB() {
		SmbJobComparator jobComparator = new SmbJobComparator();

		SmbSeedJob seedJobA = Mockito.mock(SmbSeedJob.class);
		when(seedJobA.getUrl()).thenReturn("smb://ip/shareA/");
		when(seedJobA.getType()).thenReturn(SmbJobType.SEED_JOB);

		SmbSeedJob seedJobB = Mockito.mock(SmbSeedJob.class);
		when(seedJobB.getUrl()).thenReturn("smb://ip/shareB/");
		when(seedJobB.getType()).thenReturn(SmbJobType.SEED_JOB);

		assertThat(jobComparator.compare(seedJobA, seedJobB)).isEqualTo(-1);
	}
}