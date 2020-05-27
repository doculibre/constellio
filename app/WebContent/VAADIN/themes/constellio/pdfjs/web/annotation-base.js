"use strict";

function Annotation(x, y, width, height) {
	this.type = this.getType();	
	this.id = this.uuidV4();
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
}

Annotation.prototype.uuidV4 = function() {
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	  var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
	  return v.toString(16);
	});
};

Annotation.prototype.getId = function() {
	return this.id;
};

Annotation.prototype.getHtmlElementId = function() {
	return this.getType() + "-" + this.getId();
};

Annotation.prototype.isSameHtmlElement = function(htmlElement) {
	if (htmlElement instanceof jQuery) {
		htmlElement = htmlElement[0];
	}
	return htmlElement && htmlElement.id && htmlElement.id == this.getHtmlElementId();
};

Annotation.prototype.getType = function() {
	return "annotation";
};

Annotation.prototype.isHtmlElementSameType = function(htmlElement) {
	if (htmlElement instanceof jQuery) {
		htmlElement = htmlElement[0];
	}
	return htmlElement && htmlElement.id && htmlElement.id.startsWith(this.getType() + "-");
};

Annotation.prototype.getTagName = function() {
	return "div";
};

Annotation.prototype.isEditable = function() {
	var editable;
	if (this.editor) {
		editable = true;
	} else {
		editable = false;
	}
	return editable;
};

Annotation.prototype.getEditor = function() {
	return this.editor;
};

Annotation.prototype.setEditor = function(editor) {
	this.editor = editor;
};

Annotation.prototype.getCreationUser = function() {
	return this.creationUser;
};

Annotation.prototype.setCreationUser = function(creationUser) {
	this.creationUser = creationUser;
};

Annotation.prototype.getCreationDate = function() {
	return this.creationDate;
};

Annotation.prototype.setCreationDate = function(creationDate) {
	this.creationDate = creationDate;
};

Annotation.prototype.getLastModificationUser = function() {
	return this.lastModificationUser;
};

Annotation.prototype.setModificationUser = function(lastModificationUser) {
	this.lastModificationUser = lastModificationUser;
};

Annotation.prototype.getLastModificationDate = function() {
	return this.lastModificationDate;
};

Annotation.prototype.setLastModificationDate = function(lastModificationDate) {
	this.lastModificationDate = lastModificationDate;
};

Annotation.prototype.getX = function() {
	return this.x;
};

Annotation.prototype.setX = function(x) {
	this.x = x;
	if (this.htmlElement) {
		this.htmlElement.style.left = this.x + "%";
	}
};

Annotation.prototype.getY = function() {
	return this.y;
};

Annotation.prototype.setY = function(y) {
	this.y = y;
	if (this.htmlElement) {
		this.htmlElement.style.top = this.y + "%";
	}
};

Annotation.prototype.getWidth = function() {
	return this.width;
};

Annotation.prototype.setWidth = function(width) {
	this.width = width;
	if (this.htmlElement) {
		this.htmlElement.style.width = this.width + "%";
	}
};

Annotation.prototype.getHeight = function() {
	return this.height;
};

Annotation.prototype.setHeight = function(height) {
	this.height = height;
	if (this.htmlElement) {
		this.htmlElement.style.height = this.height + "%";
	}
};

Annotation.prototype.isAttached = function() {
	var attached;
	if (this.htmlElement && this.htmlElement.parentNode) {
		attached = true;
	} else {
		attached = false;
	}
	return attached;
};

Annotation.prototype.remove = function() {
	var removed;
	if (this.htmlElement) {
		if (this.htmlElement.parentNode) {
			this.htmlElement.parentNode.removeChild(this.htmlElement);
		}
		this.htmlElement = null;
		removed = true;		
	} else {
		removed = false;
	}
	return removed;
};

Annotation.prototype.bind = function(htmlElement) {
	var self = this;
	this.remove();
	this.htmlElement = htmlElement;
	this.htmlElement.classList.add("annotation");
	this.htmlElement.id = this.getHtmlElementId();
	this.htmlElement.style.left = this.getX() + "%";
	this.htmlElement.style.top = this.getY() + "%";
	this.htmlElement.style.width = this.getWidth() + "%";
	this.htmlElement.style.height = this.getHeight() + "%";
	
	this.deleteLink = document.createElement("a");
	this.deleteLink.innerHTML = "&#10006;";
	this.deleteLink.classList.add("annotation-delete-link");
	this.deleteLink.onclick = function(e) {
		self.remove();
	};	
	this.htmlElement.appendChild(this.deleteLink);
};	

Annotation.prototype.initHtmlElement = function() {
	var newHtmlElement = document.createElement(this.getTagName());
	this.bind(newHtmlElement);
};

Annotation.prototype.getHtmlElement = function() {
	if (!this.htmlElement) {
		this.initHtmlElement();
	}
	return this.htmlElement;
};

Annotation.prototype.annotationDefined = function(htmlElement) {
};

Annotation.prototype.getSaveCallback = function() {
};

Annotation.prototype.getCancelCallback = function() {
};	

Annotation.prototype.openEditor = function() {
	if (this.htmlElement) {
		this.editor.open(this, this, this.getSaveCallback, this.getCancelCallback);
	}
};

Annotation.prototype.annotationClicked = function(e) {
	if (e.target != this.deleteLink && this.isEditable()) {
		this.openEditor();
	}
};	

Annotation.prototype.querySelector = function() {
	return "#" + this.getId();
};	

Annotation.prototype.getDebugString = function() {
	return "x:" + this.getX() + ", y:" + this.getY() + ", width: " + this.getWidth() + ", height: " + this.getHeight();
};

Annotation.prototype.debug = function() {
	console.info(this.getDebugString());
};
