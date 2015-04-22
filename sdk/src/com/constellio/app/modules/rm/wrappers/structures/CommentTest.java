/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;

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
		comment.setDateTime(TimeProvider.getLocalDateTime());
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
		comment.setDateTime(nowDateTime);

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
		comment.setDateTime(null);
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
		comment.setUser(null);
		comment.setDateTime(null);
		comment.setMessage(null);

		String stringValue = factory.toString(comment);
		Comment builtComment = (Comment) factory.build(stringValue);
		String stringValue2 = factory.toString(builtComment);

		assertThat(builtComment).isEqualTo(comment);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtComment.isDirty()).isFalse();
	}
}
