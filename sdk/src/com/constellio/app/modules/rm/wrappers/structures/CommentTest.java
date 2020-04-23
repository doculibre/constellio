package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CommentTest extends ConstellioTest {

	@Mock User bob;
	CommentFactory factory;

	LocalDateTime nowDateTime = TimeProvider.getLocalDateTime();

	@Before
	public void setUp()
			throws Exception {
		factory = spy(new CommentFactory());

		when(bob.getId()).thenReturn("bobId");
		when(bob.getUsername()).thenReturn("bob");

	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		Comment comment = new Comment();
		assertThat(comment.isDirty()).isFalse();

		comment = new Comment();
		comment.setUser(bob);
		assertThat(comment.isDirty()).isTrue();

		comment = new Comment();
		comment.setCreationDateTime(nowDateTime);
		assertThat(comment.isDirty()).isTrue();

		comment = new Comment();
		comment.setMessage("Message");
		assertThat(comment.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		Comment comment = new Comment();
		comment.setUser(bob);
		comment.setCreationDateTime(nowDateTime);

		comment.setMessage("Message");

		String stringValue = factory.toString(comment);
		Comment builtComment = (Comment) factory.build(stringValue);
		String stringValue2 = factory.toString(builtComment);

		assertThat(builtComment).isEqualTo(comment);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtComment.isDirty()).isFalse();

	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		Comment comment = new Comment();
		comment.setUser(null);
		comment.setCreationDateTime(null);
		comment.setMessage(null);

		String stringValue = factory.toString(comment);
		Comment builtComment = (Comment) factory.build(stringValue);
		String stringValue2 = factory.toString(builtComment);

		assertThat(builtComment).isEqualTo(comment);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtComment.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		Comment comment = new Comment();

		String stringValue = factory.toString(comment);
		Comment builtComment = (Comment) factory.build(stringValue);
		String stringValue2 = factory.toString(builtComment);

		assertThat(builtComment).isEqualTo(comment);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtComment.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithEmptyStringThenIsBuildSuccessflly()
			throws Exception {

		Comment comment = new Comment();
		comment.setMessage("");

		String stringValue = factory.toString(comment);
		Comment builtComment = (Comment) factory.build(stringValue);
		String stringValue2 = factory.toString(builtComment);

		assertThat(builtComment).isEqualTo(comment);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtComment.isDirty()).isFalse();
	}

	@Test
	public void ensureBuilderWorksWithOldCommentStructure() {
		Comment comment = new Comment();
		comment.setMessage("Message");
		comment.setCreationDateTime(new LocalDateTime().withDate(2020, 4, 23).withTime(15, 34, 25, 619));
		comment.setUser(bob);

		String stringValueWithoutModificationDate = "bobId:bob:2020-04-23T15~~~34~~~25.619:Message";
		Comment builtComment = (Comment) factory.build(stringValueWithoutModificationDate);
		String stringValueWithoutModificationDate2 = factory.toString(builtComment);

		assertThat(builtComment).isEqualTo(comment);
		assertThat(stringValueWithoutModificationDate).isNotEqualTo(stringValueWithoutModificationDate2);
		assertThat(builtComment.isDirty()).isFalse();
	}
}
