"use strict";

function Annotation(x, y, width, height) {
	this.type = this.getType();	
	this.id = this.uuidV4();
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	this.readOnly = false;	
	this.baked = false;
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

Annotation.prototype.isReadOnly = function() {
	return this.readOnly;
};

Annotation.prototype.setReadOnly = function(readOnly) {
	if (this.readOnly != readOnly) {
		this.readOnly = readOnly;

		if (this.htmlElement) {
			if (!this.readOnly) {
				this.htmlElement.classList.add("annotation-resizable");
				this.htmlElement.classList.add("annotation-movable");
			} else {
				this.htmlElement.classList.remove("annotation-resizable");
				this.htmlElement.classList.remove("annotation-movable");
			}
		}
		if (this.deleteLink) {
			this.deleteLink.style.display = this.readOnly ? "none" : "";
		}
	}
};

Annotation.prototype.isBaked = function() {
	return this.baked;
};

Annotation.prototype.setBaked = function(baked) {
	this.baked = baked;
};

Annotation.prototype.getBakeUser = function() {
	return this.bakeUser;
};

Annotation.prototype.setBakeUser = function(bakeUser) {
	this.bakeUser = bakeUser;
};

Annotation.prototype.getBakeDate = function() {
	return this.bakeDate;
};

Annotation.prototype.setBakeDate = function(bakeDate) {
	this.bakeDate = bakeDate;
};

Annotation.prototype.getBakeInfoI10nKey = function() {
	return "annotation.bakeInfo";
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
	var applyTooltip = false;
	this.remove();
	this.htmlElement = htmlElement;
	this.htmlElement.classList.add("annotation");
	if (!this.readOnly) {
		this.htmlElement.classList.add("annotation-resizable");
		this.htmlElement.classList.add("annotation-movable");
	}
	if (this.baked) {
		this.htmlElement.classList.add("annotation-baked");
	}
	if (this.bakeUser && this.bakeDate) {
		var bakeInfoKey = this.getBakeInfoI10nKey();
		var bakeInfo = this.i10n(bakeInfoKey, "By ({{bakeUser}} on {{bakeDate}})", {
			bakeUser: self.bakeUser,
			bakeDate: self.bakeDate
		  });
		this.htmlElement.title = bakeInfo;
		this.htmlElement.classList.add("tooltip");
	}
	this.htmlElement.id = this.getHtmlElementId();
	this.htmlElement.style.left = this.getX() + "%";
	this.htmlElement.style.top = this.getY() + "%";
	this.htmlElement.style.width = this.getWidth() + "%";
	this.htmlElement.style.height = this.getHeight() + "%";
	
	this.deleteLink = document.createElement("a");
	this.deleteLink.innerHTML = "&#10006;";
	this.deleteLink.classList.add("annotation-delete-link");
	this.deleteLink.style.display = this.readOnly ? "none" : "";
	this.deleteLink.onclick = function(e) {
		if (!self.isReadOnly()) {
			self.remove();
		}
	};	
	this.htmlElement.appendChild(this.deleteLink);
	
	if (this.htmlElement.className.indexOf("tooltip") != -1) {
		$(this.htmlElement).tooltipster();
	}
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

Annotation.prototype.i10n = function(key, defaultValue, replacements) {
	var value;
	var mozL10n = document.mozL10n || document.webL10n;
	if (mozL10n) {
        value = mozL10n.get(key, replacements, defaultValue);
        if (!value || value.indexOf("{{") == 0) {
            value = defaultValue;
        }
	} else {
		value = defaultValue;
	}
	return value;
};
