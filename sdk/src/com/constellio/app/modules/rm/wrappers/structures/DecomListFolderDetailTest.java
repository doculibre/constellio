package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DecomListFolderDetailTest extends ConstellioTest {
	private static final String NULL = "~null~";
	DecomListFolderDetailFactory factory;
	DecomListFolderDetail detail;

	@Before
	public void setUp() {
		factory = new DecomListFolderDetailFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		detail = new DecomListFolderDetail();
		assertThat(detail.isDirty()).isFalse();

		detail = new DecomListFolderDetail();
		detail.setFolderId("01");
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setFolderExcluded(true);
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setContainerRecordId("containerRecordId");
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setReversedSort(true);
		assertThat(detail.isDirty()).isTrue();

		detail = new DecomListFolderDetail();
		detail.setFolderLinearSize(123.0);
		assertThat(detail.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		detail = new DecomListFolderDetail()
				.setFolderId("01")
				.setFolderExcluded(true)
				.setContainerRecordId("containerRecordId")
				.setReversedSort(true)
				.setFolderLinearSize(123.4d);

		String serialized = factory.toString(detail);
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {
		detail = new DecomListFolderDetail().setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		String serialized = factory.toString(detail);
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}

	@Test
	public void whenConvertingStructureTheOldWayWithFolderDetailStatusSetToTrueThenConvertedSuccessfully()
			throws Exception {
		detail = new DecomListFolderDetail()
				.setFolderId("01")
				.setFolderExcluded(false)
				.setContainerRecordId("containerRecordId")
				.setReversedSort(true)
				.setFolderLinearSize(123.4d);

		String serialized = "01:true:~null~:~null~:~null~:containerRecordId:true:123.4:false";
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}

	@Test
	public void whenConvertingStructureTheOldWayWithFolderDetailStatusSetToFalseThenConvertedSuccessfully()
			throws Exception {
		detail = new DecomListFolderDetail()
				.setFolderId("01")
				.setFolderExcluded(true)
				.setContainerRecordId("containerRecordId")
				.setReversedSort(true)
				.setFolderLinearSize(123.4d);

		String serialized = "01:false:~null~:~null~:~null~:containerRecordId:true:123.4:false";
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}

}
