package com.constellio.app.modules.es.connectors.smb.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class SmbUrlComparatorAcceptanceTest extends ConstellioTest {
	@Test
	public void whenComparingEmptyAndNonEmptyThenEmptyBeforeNonEmpty() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String emptyString = "";
		String shareUrl = "smb://ip/share/";
		assertThat(urlComparator.compare(emptyString, shareUrl)).isLessThan(0);
	}

	@Test
	public void whenComparingShareAndFileThenShareBeforeFile() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String fileUrl = "smb://ip/share/file";
		String shareUrl = "smb://ip/share/";
		assertThat(urlComparator.compare(shareUrl, fileUrl)).isLessThan(0);
	}

	@Test
	public void whenComparingShareAndFolderThenShareBeforeFolder() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String folderUrl = "smb://ip/share/folder/";
		String shareUrl = "smb://ip/share/";
		assertThat(urlComparator.compare(shareUrl, folderUrl)).isLessThan(0);
	}

	@Test
	public void whenComparingFolderAndFolderThenFolderIsEqualToFolder() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String folderUrl = "smb://ip/share/folder/";
		assertThat(urlComparator.compare(folderUrl, folderUrl)).isEqualTo(0);
	}

	@Test
	public void whenComparingFolderArticleAndFolderSubsetThenArticleBeforeSubset() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String folderArticleUrl = "smb://ip/share/article/";
		String folderSubsetUrl = "smb://ip/share/subset/";
		assertThat(urlComparator.compare(folderArticleUrl, folderSubsetUrl)).isLessThan(0);
	}

	@Test
	public void whenComparingFolderArtAndFolderSubsetThenArtBeforeSubset() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String folderArtUrl = "smb://ip/share/art/";
		String folderSubsetUrl = "smb://ip/share/subset/";
		assertThat(urlComparator.compare(folderArtUrl, folderSubsetUrl)).isLessThan(0);
	}

	@Test
	public void whenComparingSeedsThenSeedABeforSeedB() {
		SmbUrlComparator urlComparator = new SmbUrlComparator();
		String seedAUrl = "smb://ip/shareA/";
		String seedBUrl = "smb://ip/shareB/";
		assertThat(urlComparator.compare(seedAUrl, seedBUrl)).isLessThan(0);
	}
}