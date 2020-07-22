"use strict";

function SignaturePicker(signatureDataStore) {
	this.signatureDataStore = signatureDataStore;
};

SignaturePicker.prototype.openPicker = function(signHereAnnotation) {
	var self = this;
	this.windowElement = document.createElement("div");
	this.windowElement.classList.add("signature-picker-window");
	
	var windowContentElement = document.createElement("div");
	windowContentElement.classList.add("signature-picker-window-content");
	
	var closeButton = document.createElement("span");
	closeButton.innerHTML = "&#10006;";
	closeButton.classList.add("signature-picker-window-close-button");
	closeButton.onclick = function(e) {
		self.closeWindow.call(self);
	};

	var titleElement = document.createElement("div");
	titleElement.classList.add("signature-picker-window-title");
	titleElement.innerHTML = this.i10n("signaturePicker.title", "To save signatures in the PDF document, click on the Certify button.");

	var signHereButton;
	if (!signHereAnnotation) {
		signHereButton = document.createElement("button");
		signHereButton.setAttribute("data-l10n-id", "buttons.newSignHereAnnotation");
		signHereButton.innerHTML = this.i10n("buttons.newSignHereAnnotation", "New sign here zone");
		signHereButton.title=this.i10n("buttons.newSignHereAnnotation.tooltip", "Define a new sign here zone");
		signHereButton.classList.add("tooltip");
		signHereButton.classList.add("signature-picker-sign-here-button");
		signHereButton.onclick = function(e) {
			self.signHereAnnotationPicked();
			self.closeWindow();			
		};
	}
		
	var signatureLabelElement = document.createElement("div");
	signatureLabelElement.classList.add("signature-picker-signature-label");
	signatureLabelElement.innerHTML = this.i10n("signaturePicker.signature", "Use an existing signature or create a new one");
	
	var initialsLabelElement = document.createElement("div");
	initialsLabelElement.classList.add("signature-picker-initials-label");
	initialsLabelElement.innerHTML = this.i10n("signaturePicker.initials", "Use existing initials or create new ones");
	
	var signatureFields = this.buildFields(signHereAnnotation, false);
	var initialsFields = this.buildFields(signHereAnnotation, true);
	
	this.windowElement.appendChild(windowContentElement);
	windowContentElement.appendChild(closeButton);
	if (signHereButton) {
		windowContentElement.appendChild(signHereButton);
	}
	windowContentElement.appendChild(titleElement);
	windowContentElement.appendChild(signatureLabelElement);
	windowContentElement.appendChild(signatureFields);
	windowContentElement.appendChild(initialsLabelElement);
	windowContentElement.appendChild(initialsFields);
	
	document.body.appendChild(this.windowElement);
	$(".signature-picker-window .tooltip").tooltipster();
};

SignaturePicker.prototype.closeWindow = function() {
	this.windowElement.parentNode.removeChild(this.windowElement);
};

SignaturePicker.prototype.buildFields = function(signHereAnnotation, initials) {
	// Thumbnail
	// Upload Button
	// Draw Button
	// Type Button
	// Use Button (if exists)
	// Delete Button (if exists)
	var self = this;
	
	var tooltipPrefix;
	var signatureExists;
	if (initials) {
		tooltipPrefix = "initials";
		signatureExists = this.signatureDataStore.getInitialsImageUrl();
	} else {
		tooltipPrefix = "signature";
		signatureExists = this.signatureDataStore.getSignatureImageUrl();
	}
	
	var adjustVisibility = function() {
		self.setVisible(thumbnailElement, signatureExists);
		self.setVisible(useButton, signatureExists);
		self.setVisible(deleteButton, signatureExists);
	};
	
	var containerElement = document.createElement("div");
	if (initials) {
		containerElement.classList.add("signature-picker-initials");
	} else {
		containerElement.classList.add("signature-picker-signature");
	}
	containerElement.classList.add("signature-picker-fields");

	var thumbnailUrl = initials ? this.signatureDataStore.getInitialsImageUrl() : this.signatureDataStore.getSignatureImageUrl();
	
	var thumbnailElement = document.createElement("img");
	thumbnailElement.title = this.i10n("signaturePicker." + tooltipPrefix + ".use.tooltip", "Use");
	thumbnailElement.classList.add("tooltip");
	thumbnailElement.classList.add("signature-picker-thumbnail");
	thumbnailElement.src = thumbnailUrl;
	thumbnailElement.style.maxWidth = "200px";
	thumbnailElement.style.height = "auto";	
	thumbnailElement.style.maxHeight = "60px";	
	thumbnailElement.addEventListener("click", function(e) {
		useButton.click();
	}, false);
	
	var actionsElement = document.createElement("div");
	actionsElement.classList.add("signature-picker-fields-actions");
	
	var handleFiles = function(files) {
		for (var i = 0; i < files.length; i++) {
			var file = files.item(i);
			if (file.type.indexOf("image/") == 0) {
				var reader = new FileReader();
				reader.readAsDataURL(file);
				reader.onloadend = function() {
					var imageUrl = reader.result;
					thumbnailElement.src = imageUrl;
					if (initials) {
						self.signatureDataStore.setInitialsImageUrl(imageUrl);
					} else {
						self.signatureDataStore.setSignatureImageUrl(imageUrl);
					}
					thumbnailElement.src = imageUrl;
					if (!signatureExists) {
						signatureExists = true;
						adjustVisibility();
					}
				};
			}
		};
	};
	
	var inputField = document.createElement("input");
	inputField.classList.add("signature-picker-input");
	inputField.type = "file";
	inputField.accept = "image/*";	
	inputField.onchange = function(e) {
		handleFiles(inputField.files);
		e.preventDefault();
		e.stopPropagation();
	};
	this.setVisible(inputField, false);
	
	containerElement.addEventListener("drop", function(e) {
		var dt = e.dataTransfer;
		handleFiles(dt.files);
	}, false);
	;["dragenter", "dragover", "dragleave", "drop"].forEach(function(eventName) {
		containerElement.addEventListener(eventName, function(e) {
			e.preventDefault();
			e.stopPropagation();
		}, false);
	});
	;["dragenter", "dragover"].forEach(function(eventName) {
		containerElement.addEventListener(eventName, function(e) {
			containerElement.classList.add("highlight");
		}, false);
	});

	;["dragleave", "drop"].forEach(function(eventName) {
		containerElement.addEventListener(eventName, function(e) {
			containerElement.classList.remove("highlight");
		}, false);
	});
	
	var uploadButton = document.createElement("button");
	uploadButton.classList.add("signature-picker-upload-button");
	uploadButton.setAttribute("data-l10n-id", "buttons.upload");
	uploadButton.classList.add("tooltip");
	uploadButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".upload.tooltip", "Upload");
	uploadButton.onclick = function(e) {
		inputField.click();
	};
	
	var drawButton = document.createElement("button");
	drawButton.classList.add("signature-picker-draw-button");
	drawButton.setAttribute("data-l10n-id", "buttons.draw");
	drawButton.classList.add("tooltip");
	drawButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".draw.tooltip", "Draw");
	drawButton.onclick = function(e) {
		var saveCallback = function(imageUrl) {
			if (initials) {
				self.signatureDataStore.setInitialsImageUrl(imageUrl);
			} else {
				self.signatureDataStore.setSignatureImageUrl(imageUrl);
			}
		};
		self.drawAnnotationPicked(signHereAnnotation, initials, saveCallback);
		self.closeWindow();
	};
	
	var typeButton = document.createElement("button");
	typeButton.classList.add("signature-picker-type-button");
	typeButton.setAttribute("data-l10n-id", "buttons.type");
	typeButton.classList.add("tooltip");
	typeButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".type.tooltip", "Type");
	typeButton.onclick = function(e) {
		self.textAnnotationPicked(signHereAnnotation, initials);
		self.closeWindow();
	};
	
	var deleteButton = document.createElement("button");
	deleteButton.classList.add("signature-picker-delete-button");
	deleteButton.setAttribute("data-l10n-id", "buttons.delete");
	deleteButton.classList.add("tooltip");
	deleteButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".delete.tooltip", "Delete");
	deleteButton.onclick = function(e) {
		if (initials) {
			self.signatureDataStore.removeInitialsImageUrl();
		} else {
			self.signatureDataStore.removeSignatureImageUrl();
		}
		signatureExists = false;
		adjustVisibility();
	};
	
	var useButton = document.createElement("button");
	useButton.classList.add("signature-picker-use-button");
	useButton.setAttribute("data-l10n-id", "buttons.use");
	useButton.classList.add("tooltip");
	useButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".use.tooltip", "Use");
	useButton.onclick = function(e) {
		self.imageAnnotationPicked(signHereAnnotation, initials, thumbnailElement.src);
		self.closeWindow();
	};
	
	containerElement.appendChild(thumbnailElement);
	containerElement.appendChild(actionsElement);
	actionsElement.appendChild(inputField);
	actionsElement.appendChild(drawButton);
	actionsElement.appendChild(uploadButton);
	actionsElement.appendChild(deleteButton);
	//actionsElement.appendChild(useButton);
	//actionsElement.appendChild(typeButton);
	
	adjustVisibility();
	
	return containerElement;
};

SignaturePicker.prototype.i10n = function(key, defaultValue) {
	var value;
	var mozL10n = document.mozL10n || document.webL10n;
	if (mozL10n) {
        value = mozL10n.get(key, null, null);
        if (!value || value.indexOf("{{") == 0) {
            value = defaultValue;
        }
	} else {
		value = defaultValue;
	}
	return value;
};

SignaturePicker.prototype.signHereAnnotationPicked = function() {
	console.debug("Sign Here");
};	

SignaturePicker.prototype.drawAnnotationPicked = function(signHereAnnotation, initials, saveCallback) {
	console.debug("Draw " + (initials ? "initials" : "signature"));
};	

SignaturePicker.prototype.imageAnnotationPicked = function(signHereAnnotation, initials, imageUrl) {
	console.debug("Use " + (initials ? "initials" : "signature"));
};	

SignaturePicker.prototype.textAnnotationPicked = function(signHereAnnotation, initials) {
	console.debug("Type " + (initials ? "initials" : "signature"));
};	

SignaturePicker.prototype.setVisible = function(htmlElement, visible) {
	if (visible) {
		htmlElement.style.display = "";
	} else {
		htmlElement.style.display = "none";
	}
};

 /**
  * Conserve aspect ratio of the original region. Useful when shrinking/enlarging
  * images to fit into a certain area.
  *
  * @param {Number} srcWidth width of source image
  * @param {Number} srcHeight height of source image
  * @param {Number} maxWidth maximum available width
  * @param {Number} maxHeight maximum available height
  * @return {Object} { width, height }
  */
SignaturePicker.prototype.calculateAspectRatioFit = function(srcWidth, srcHeight, maxWidth, maxHeight) {
    var ratio = Math.min(maxWidth / srcWidth, maxHeight / srcHeight);
    return { width: srcWidth*ratio, height: srcHeight*ratio };
};

SignaturePicker.prototype.closeWindow = function() {
	this.windowElement.parentNode.removeChild(this.windowElement);
};	


