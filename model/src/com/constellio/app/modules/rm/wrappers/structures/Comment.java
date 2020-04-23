package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

public class Comment implements ModifiableStructure {

	String message;
	String userId;
	String username;
	LocalDateTime creationDateTime;
	LocalDateTime modificationDateTime;
	boolean dirty;

	public Comment(String message, User user, LocalDateTime creationDateTime) {
		this.message = message;
		this.userId = user.getId();
		this.username = user.getUsername();
		this.creationDateTime = creationDateTime;
	}

	public Comment() {
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUser(User user) {
		dirty = true;
		if (user != null) {
			userId = user.getId();
			username = user.getUsername();
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		dirty = true;
		this.message = message;
	}

	public LocalDateTime getCreationDateTime() {
		return creationDateTime;
	}

	public void setCreationDateTime(LocalDateTime creationDateTime) {
		dirty = true;
		this.creationDateTime = creationDateTime;
	}

	public LocalDateTime getModificationDateTime() {
		return modificationDateTime;
	}

	public void setModificationDateTime(LocalDateTime modificationDateTime) {
		dirty = true;
		this.modificationDateTime = modificationDateTime;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "Comment{" +
			   "message='" + message + '\'' +
			   ", borrowUserId='" + userId + '\'' +
			   ", borrowerUsername='" + username + '\'' +
			   ", creationDateTime=" + creationDateTime +
			   ", modificationDateTime=" + modificationDateTime +
			   ", dirty=" + dirty +
			   '}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}
}
