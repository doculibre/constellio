package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
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
		detail = new DecomListFolderDetail();

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

		String serialized = toStringWithOldStructure(detail, true);
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

		String serialized = toStringWithOldStructure(detail, false);
		DecomListFolderDetail deserialized = (DecomListFolderDetail) factory.build(serialized);

		assertThat(deserialized).isEqualTo(detail);
	}

	private String toStringWithOldStructure(ModifiableStructure structure, boolean included) {
		DecomListFolderDetail decomListFolderDetail = (DecomListFolderDetail) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, decomListFolderDetail.getFolderId());
		writeString(stringBuilder, String.valueOf(included));
		writeString(stringBuilder, null);
		writeString(stringBuilder, null);
		writeString(stringBuilder, null);
		writeString(stringBuilder, decomListFolderDetail.getContainerRecordId());
		writeString(stringBuilder, String.valueOf(decomListFolderDetail.isReversedSort()));
		writeDouble(stringBuilder, decomListFolderDetail.getFolderLinearSize());
		writeString(stringBuilder, "" + decomListFolderDetail.isPlacedInContainer() == null ?
								   String.valueOf(false) :
								   String.valueOf(decomListFolderDetail.isPlacedInContainer()));
		return stringBuilder.toString();
	}

	private void writeDouble(StringBuilder stringBuilder, Double value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(String.valueOf(value));
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(":", "~~~"));
		}
	}
}
