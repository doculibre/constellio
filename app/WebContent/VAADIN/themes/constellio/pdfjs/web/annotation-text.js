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
		this.adjustFontSizeDynamically();
		this.convertTextToImage();
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
	
	htmlElement.classList.add("text-annotation");
	var textContent = this.getText();
	if (!textContent) {
		textContent = "";
	}
	this.textElement = document.createElement("span");
	this.textElement.id = this.getHtmlElementId() + "-text";
	this.textNode = document.createTextNode(textContent);
	this.textElement.appendChild(this.textNode);
	htmlElement.appendChild(this.textElement); 
	
	if (this.imageUrl) {
		this.textElement.style.display = "none";
		htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")";
	} else {
		this.adjustFontSizeDynamically();
	}
};	

TextAnnotation.prototype.adjustFontSizeDynamically = function() {
	if (true) return;
	if (this.textElement) {
		var newFontSize = (1 + (this.getWidth() / 100)) * 16;
		this.textElement.style.fontSize = newFontSize + "pt";
	} 
};

TextAnnotation.prototype.convertTextToImage = function() {
	var self = this;
	if (this.textElement && this.text) {		
		this.htmlElement.style.backgroundImage = "";
		this.textElement.style.display = "";
		
		var imageScale = 3;

		var bigCanvas = $("<div>").appendTo('body');  // This will be the 3x sized canvas we're going to render
		bigCanvas[0].classList.add("signature-text-annotation");
		bigCanvas[0].style.paddingBottom = "10px";
		var scaledElement = $(self.textElement).clone()
		.css({
			'transform': 'scale('+ imageScale + ',' + imageScale + ')',
			'transform-origin': '0 0'
		})
		.appendTo(bigCanvas);

		var oldWidth = scaledElement.width();
		var oldHeight = scaledElement.height();

		var newWidth = oldWidth * imageScale;
		var newHeight = oldHeight * imageScale;

		bigCanvas.css({
			'width': newWidth,
			'height': newHeight
		});
		setTimeout(function() {
			html2canvas(bigCanvas, {
				onrendered: function(canvas) {
					var imageUrl = canvas.toDataURL("image/png");
					if (imageUrl) {
						self.imageUrl = imageUrl;
						self.textElement.style.display = "none";
						self.htmlElement.style.backgroundImage = "url(" + imageUrl + ")";
					} else {
						self.imageUrl = null;
					}
					bigCanvas.remove();
				}
			});

			/*
			html2canvas(self.textElement, {
				onrendered: function(canvas) {
					var imageUrl = canvas.toDataURL("image/png");
					if (imageUrl) {
						self.imageUrl = imageUrl;
						self.textElement.style.display = "none";
						self.htmlElement.style.backgroundImage = "url(" + imageUrl + ")";
					} else {
						self.imageUrl = null;
					}
				}
			});
			*/
		}, 100);	
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
	this.setText(this.getEditor().getText());
};

TextAnnotation.prototype.getDebugString = function() {
	return "text: " + this.getText() + ", " + Annotation.prototype.getDebugString.call(this);
};
