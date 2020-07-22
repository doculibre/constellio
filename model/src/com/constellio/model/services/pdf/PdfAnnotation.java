package com.constellio.model.services.pdf;

import java.awt.*;

public class PdfAnnotation implements Comparable<PdfAnnotation> {

	private int page;
	private Rectangle position;
	private String userId;
	private String username;
	private String imageData;
	private boolean signature;
	private boolean baked;

	public PdfAnnotation(int page, Rectangle position, String userId, String username, String imageData,
						 boolean signature, boolean baked) {
		this.page = page;
		this.position = position;
		this.userId = userId;
		this.username = username;
		this.imageData = imageData;
		this.signature = signature;
		this.baked = baked;
	}

	public int getPage() {
		return page;
	}

	public Rectangle getPosition() {
		return position;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getImageData() {
		return imageData;
	}

	public boolean isSignature() {
		return signature;
	}

	public boolean isBaked() {
		return baked;
	}

	public void setBaked(boolean baked) {
		this.baked = baked;
	}

	@Override
	public int compareTo(PdfAnnotation other) {
		int result;

		result = compare(page, other.getPage());
		if (result != 0) {
			return result;
		}

		result = compare(position.y, other.getPosition().y);
		if (result != 0) {
			return result;
		}

		result = compare(position.x, other.getPosition().x);
		return result;
	}

	private int compare(int thisValue, int otherValue) {
		if (thisValue != otherValue) {
			return thisValue < otherValue ? -1 : 1;
		}
		return 0;
	}
}
