"use strict";

function TextAnnotation(text, x, y, width, height) {
	Annotation.call(this, x, y, width, height);
	this.setText(text);
	this.setEditor(new TextAnnotationEditor(this.getText()));
}

TextAnnotation.prototype = Object.create(Annotation.prototype);
TextAnnotation.prototype.constructor = TextAnnotation;

TextAnnotation.prototype.getType = function() {
	return "text-annotation";
}

TextAnnotation.prototype.getText = function() {
	return this.text;	
};

TextAnnotation.prototype.setText = function(text) {
	if (!text) {
		text = "";
	}
	this.text = text;
	if (this.textNode) {
		this.textNode.nodeValue = text;
	}
};

TextAnnotation.prototype.toJSON = function() {
	var json = Annotation.prototype.toJSON.call(this);
	json.text = this.getText();
	if (this.imageUrl) {
		json.imageUrl = this.imageUrl;
	}
	return json;
};

TextAnnotation.prototype.fromJSON = function(json) {
	Annotation.prototype.fromJSON.call(this, json);
	this.text = json.text;
	if (json.imageUrl) {
		this.imageUrl = json.imageUrl;
	}
};

TextAnnotation.prototype.bind = function(htmlElement) {
	Annotation.prototype.bind.call(this, htmlElement);
	
	htmlElement.classList.add(this.getType());
	var textContent = this.getText();
	if (!textContent) {
		textContent = "";
	}
	this.textElement = document.createElement("span");
	this.textElement.style.display = "none";
	this.textElement.id = this.getHtmlElementId() + "-text";
	this.textNode = document.createTextNode(textContent);
	this.textElement.appendChild(this.textNode);
	htmlElement.appendChild(this.textElement); 
	
	if (this.imageUrl) {
		htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")";
	}
};

TextAnnotation.prototype.isSameHtmlElement = function(htmlElement) {
	var sameHtmlElement = Annotation.prototype.isSameHtmlElement.call(this, htmlElement);
	if (!sameHtmlElement) {
		if (htmlElement instanceof jQuery) {
			htmlElement = htmlElement[0];
		}
		sameHtmlElement = htmlElement && htmlElement.id && htmlElement.id == this.textElement.id;
	}
	return sameHtmlElement;
};

TextAnnotation.prototype.annotationDefined = function(htmlElement) {
	if (!this.getText()) {
		this.openEditor();
	}
};

TextAnnotation.prototype.getSaveCallback = function() {
	var editor = this.getEditor();
	this.setText(editor.getText());
	/*
	this.imageUrl = editor.getImageUrl();
	if (this.imageUrl) {
		this.htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")";  
	} else {
		this.htmlElement.style.backgroundImage = "";  
	}
	*/
};

TextAnnotation.prototype.getDebugString = function() {
	return "text: " + this.getText() + ", " + Annotation.prototype.getDebugString.call(this);
};
