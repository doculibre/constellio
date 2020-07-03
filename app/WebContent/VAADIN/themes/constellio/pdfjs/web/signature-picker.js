"use strict";

function SignaturePicker(signatureDataStore, dropZoneManager, signaturesContainer) {
	this.signatureDataStore = signatureDataStore;
	this.dropZoneManager = dropZoneManager;
	this.signaturesContainer = signaturesContainer;
};

SignaturePicker.prototype.openPicker = function(signHereAnnotation) {
	this.removeChildren(this.signaturesContainer);
	this.buildSignatureContainer(signHereAnnotation, true);
	this.buildSignatureContainer(signHereAnnotation, false);
	this.setVisible(this.signaturesContainer, true);
};	

SignaturePicker.prototype.buildSignatureContainer = function(signHereAnnotation, signature) {
	var self = this;

	var tooltipPrefix;
	var signatureExists;
	if (signature) {
		tooltipPrefix = "signature";
		signatureExists = this.signatureDataStore.getSignatureImageUrl();
	} else {
		tooltipPrefix = "initials";
		signatureExists = this.signatureDataStore.getInitialsImageUrl();
	}

	var adjustVisibility = function() {
		self.setVisible(thumbnailElement, signatureExists);
		self.setVisible(deleteButton, signatureExists);
		self.setVisible(addButton, !signatureExists);
	};

	var useSignature = function() {
		var imageUrl = thumbnailElement.src;			
		var signatureImageAnnotation = new SignatureImageAnnotation(imageUrl);
		signatureImageAnnotation.setSignature(signature);
		if (signHereAnnotation) {
			signatureImageAnnotation.setX(signHereAnnotation.getX());
			signatureImageAnnotation.setY(signHereAnnotation.getY());
			signatureImageAnnotation.setWidth(signHereAnnotation.getWidth());
			signatureImageAnnotation.setHeight(signHereAnnotation.getHeight());
		}

		if (signHereAnnotation) {
			self.replaceSignHereAnnotation(signHereAnnotation, signatureImageAnnotation);
		} else {
			self.dropZoneManager.defineAnnotation(signatureImageAnnotation);
		}
	};

	var deleteSignature = function() {
		if (signature) {
			self.signatureDataStore.removeSignatureImageUrl();
		} else {
			self.signatureDataStore.removeInitialsImageUrl();
		}
		signatureExists = false;
		adjustVisibility();
	};

	var addSignature = function() {
		self.setVisible(self.signaturesContainer, false);
		if (signHereAnnotation) {
			self.buildAddSignatureWindow(signHereAnnotation, signHereAnnotation, signature);
		} else {
			// Use to collect x, y, width and height
			var placeHolderAnnotation = new Annotation();
			placeHolderAnnotation.annotationDefined = function() {
				self.buildAddSignatureWindow(placeHolderAnnotation, null, signature);
			};
			self.dropZoneManager.defineAnnotation(placeHolderAnnotation);
		}
	};

	var thumbnailElement = document.createElement("img");
	thumbnailElement.title = this.i10n("signaturePicker." + tooltipPrefix + ".use.tooltip", "Use");
	thumbnailElement.classList.add("tooltip");
	thumbnailElement.classList.add("signature-picker-thumbnail");
	thumbnailElement.src = thumbnailUrl;
	thumbnailElement.style.maxWidth = "200px";
	thumbnailElement.style.height = "auto";	
	thumbnailElement.style.maxHeight = "60px";	
	thumbnailElement.addEventListener("click", function(e) {
		useSignature();
	}, false);

	var deleteButton = document.createElement("button");
	deleteButton.classList.add("signature-picker-delete-button");
	deleteButton.setAttribute("data-l10n-id", "buttons.delete");
	deleteButton.classList.add("tooltip");
	deleteButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".delete.tooltip", "Delete");
	deleteButton.onclick = function(e) {
		deleteSignature();
	};

	var addButton = document.createElement("button");
	addButton.classList.add("signature-picker-add-button");
	addButton.setAttribute("data-l10n-id", "buttons.add");
	addButton.classList.add("tooltip");
	addButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".add.tooltip", "Add");
	addButton.onclick = function(e) {
		addSignature();
	};
};	

SignaturePicker.prototype.buildAddSignatureWindow = function(placeHolderAnnotation, signHereAnnotation, signature) {
	var self = this;

	var closeWindow = function() {
		windowElement.parentNode.removeChild(this.windowElement);
	};

	var adjustVisibility = function() {

	};

	var saveSignature = true;


	var windowElement = document.createElement("div");
	windowElement.classList.add("signature-picker-window");
	
	var windowContentElement = document.createElement("div");
	windowContentElement.classList.add("signature-picker-window-content");
	
	var closeButton = document.createElement("span");
	closeButton.innerHTML = "&#10006;";
	closeButton.classList.add("signature-picker-window-close-button");
	closeButton.onclick = function(e) {
		closeWindow();
	};
	
	var handleFiles = function(files) {
		for (var i = 0; i < files.length; i++) {
			var file = files.item(i);
			if (file.type.indexOf("image/") == 0) {
				var reader = new FileReader();
				reader.readAsDataURL(file);
				reader.onloadend = function() {
					var imageUrl = reader.result;
					if (saveSignature) {
						if (signature) {
							self.signatureDataStore.setSignatureImageUrl(imageUrl);
						} else {
							self.signatureDataStore.setInitialsImageUrl(imageUrl);
						}
					}
					closeWindow();
					
					var signatureImageAnnotation = new SignatureImageAnnotation(imageUrl);
					signatureImageAnnotation.setSignature(signature);
					if (signHereAnnotation) {
						signatureImageAnnotation.setX(signHereAnnotation.getX());
						signatureImageAnnotation.setY(signHereAnnotation.getY());
						signatureImageAnnotation.setWidth(signHereAnnotation.getWidth());
						signatureImageAnnotation.setHeight(signHereAnnotation.getHeight());
					}

					if (signHereAnnotation) {
						self.replaceSignHereAnnotation(signHereAnnotation, signatureImageAnnotation);
					} else {
						self.dropZoneManager.defineAnnotation(signatureImageAnnotation);
					}
				};
				break;
			}
		};
	};

	var formContainerElement = document.createElement("div");
	var setFormContainerContent = function(formHtmlElement) {
		self.removeChildren(formContainerElement);
		formContainerElement.appendChild(formHtmlElement);
	};

	var saveCheckBox = document.createElement("input");
	saveCheckBox.setAttribute("type", "checkbox");
	saveCheckBox.checked = saveSignature;
	saveCheckBox.addEventListener("change", function() {
		saveSignature = this.checked;
	});

	var actionButtonsElement = document.createElement("div"); 
	var applyButton = document.createElement("button");
	var cancelButton = document.createElement("button");

	var uploadSignature = function(files) {
	};

	var drawSignature = function() {
		var signaturePadAnnotation = new SignaturePadAnnotation();
		signaturePadAnnotation.setSignature(signature);
		signaturePadAnnotation.setX(placeHolderAnnotation.getX());
		signaturePadAnnotation.setY(placeHolderAnnotation.getY());
		signaturePadAnnotation.setWidth(placeHolderAnnotation.getWidth());
		signaturePadAnnotation.setHeight(placeHolderAnnotation.getHeight());
		
		applyButton.onclick = function(imageUrl) {
			var imageUrl = signaturePadAnnotation.editor.getImageUrl();
			if (saveSignature) {
				if (signature) {
					self.signatureDataStore.setSignatureImageUrl(imageUrl);
				} else {
					self.signatureDataStore.setInitialsImageUrl(imageUrl);
				}
			}
			closeWindow();

			if (signHereAnnotation) {
				self.replaceSignHereAnnotation(signHereAnnotation, signaturePadAnnotation);
			} else {
				self.replacePlaceHolderAnnotation(placeHolderAnnotation, signaturePadAnnotation);
			}
		};

		var formHtmlElement = signaturePadAnnotation.editor.getFormHtmlElement(signaturePadAnnotation, actionButtonsElement, applyButton, cancelButton);
		setFormContainerContent(formHtmlElement);	
		adjustVisibility();
	};

	var typeSignature = function() {
		var signatureTextAnnotation = new SignatureTextAnnotation();
		signatureTextAnnotation.setSignature(signature);
		signatureTextAnnotation.setX(placeHolderAnnotation.getX());
		signatureTextAnnotation.setY(placeHolderAnnotation.getY());
		signatureTextAnnotation.setWidth(placeHolderAnnotation.getWidth());
		signatureTextAnnotation.setHeight(placeHolderAnnotation.getHeight());
	
		applyButton.onclick = function(imageUrl) {
			var imageUrl = signatureTextAnnotation.editor.getImageUrl();
			if (saveSignature) {
				if (signature) {
					self.signatureDataStore.setSignatureImageUrl(imageUrl);
				} else {
					self.signatureDataStore.setInitialsImageUrl(imageUrl);
				}
			}
			closeWindow();

			if (signHereAnnotation) {
				self.replaceSignHereAnnotation(signHereAnnotation, signaturePadAnnotation);
			} else {
				self.replacePlaceHolderAnnotation(placeHolderAnnotation, signaturePadAnnotation);
			}
		};

		var formHtmlElement = signaturePadAnnotation.editor.getFormHtmlElement(signaturePadAnnotation, actionButtonsElement, applyButton, cancelButton);
		setFormContainerContent(formHtmlElement);	
		adjustVisibility();
	};


	var actionsElement = document.createElement("div");
	actionsElement.classList.add("signature-picker-fields-actions");
	
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
	
	var dropContainerElement = document.createElement("div");
	dropContainerElement.addEventListener("click", function(e) {
		inputField.click();
	}, false);
	dropContainerElement.addEventListener("drop", function(e) {
		var dt = e.dataTransfer;
		handleFiles(dt.files);
	}, false);
	;["dragenter", "dragover", "dragleave", "drop"].forEach(function(eventName) {
		dropContainerElement.addEventListener(eventName, function(e) {
			e.preventDefault();
			e.stopPropagation();
		}, false);
	});
	;["dragenter", "dragover"].forEach(function(eventName) {
		dropContainerElement.addEventListener(eventName, function(e) {
			dropContainerElement.classList.add("highlight");
		}, false);
	});

	;["dragleave", "drop"].forEach(function(eventName) {
		dropContainerElement.addEventListener(eventName, function(e) {
			dropContainerElement.classList.remove("highlight");
		}, false);
	});
	
	var uploadButton = document.createElement("button");
	uploadButton.classList.add("signature-picker-upload-button");
	uploadButton.setAttribute("data-l10n-id", "buttons.upload");
	uploadButton.classList.add("tooltip");
	uploadButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".upload.tooltip", "Upload");
	uploadButton.onclick = function(e) {
		uploadSignature();
	};
	
	var drawButton = document.createElement("button");
	drawButton.classList.add("signature-picker-draw-button");
	drawButton.setAttribute("data-l10n-id", "buttons.draw");
	drawButton.classList.add("tooltip");
	drawButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".draw.tooltip", "Draw");
	drawButton.onclick = function(e) {
		drawSignature();
	};
	
	var typeButton = document.createElement("button");
	typeButton.classList.add("signature-picker-type-button");
	typeButton.setAttribute("data-l10n-id", "buttons.type");
	typeButton.classList.add("tooltip");
	typeButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".type.tooltip", "Type");
	typeButton.onclick = function(e) {
		typeSignature();
	};

	var titleElement = document.createElement("div");
	titleElement.classList.add("signature-picker-window-title");
	titleElement.innerHTML = this.i10n("signaturePicker.title", "To save signatures in the PDF document, click on the Certify button.");
	
	windowElement.appendChild(windowContentElement);
	windowContentElement.appendChild(closeButton);
	windowContentElement.appendChild(titleElement);
	windowContentElement.appendChild(actionsElement);
	
	document.body.appendChild(this.windowElement);
	$(".signature-picker-window .tooltip").tooltipster();
};	

SignaturePicker.prototype.removeChildren = function(element) {
	var fc = element.firstChild;
	while (fc) {
		element.removeChild(fc);
		fc = element.firstChild;
	}
};	

SignaturePicker.prototype.signHereAnnotationPicked = function() {
    var signHereAnnotation = new SignHereAnnotation();
    this.manageSignHereAnnotation(signHereAnnotation);
    this.dropZoneManager.defineAnnotation(signHereAnnotation);
};		

SignaturePicker.prototype.manageSignHereAnnotation = function(signHereAnnotation) {
    var self = this;

    var signHereAnnotationEditor = new AnnotationEditor(); 
    signHereAnnotationEditor.open = function(annotation, callbackContext, saveCallback, cancelCallback) {
        self.openPicker(signHereAnnotation);
    };
    signHereAnnotation.setEditor(signHereAnnotationEditor);
};

SignaturePicker.prototype.replacePlaceHolderAnnotation = function(placeHolderAnnotation, newAnnotation) {
	placeHolderAnnotation.remove();
	this.dropZoneManager.loadAnnotation(newAnnotation);
	this.dropZoneManager.onAnnotationSaved(newAnnotation);
};	

SignaturePicker.prototype.replaceSignHereAnnotation = function(signHereAnnotation, newAnnotation) {
    var self = this;
    var defaultRemove = newAnnotation.remove;
    if (!defaultRemove) {
        defaultRemove = Object.getPrototypeOf(newAnnotation).remove;
    }    
    newAnnotation.remove = function(e) {
        if (defaultRemove.call(newAnnotation)) {
            self.dropZoneManager.loadAnnotation(signHereAnnotation);           
        }
    };    
    var defaultBind = newAnnotation.bind;
    if (!defaultBind) {
        defaultBind = Object.getPrototypeOf(newAnnotation).bind;
    }
    newAnnotation.bind = function(htmlElement) {
        defaultBind.call(newAnnotation, htmlElement);
        signHereAnnotation.remove();        
    };
    this.dropZoneManager.loadAnnotation(newAnnotation);
    this.dropZoneManager.onAnnotationSaved(newAnnotation);
};    






















SignaturePicker.prototype.openPickerOld = function(signHereAnnotation) {
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

	/*
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
	*/
		
	var signatureLabelElement = document.createElement("div");
	signatureLabelElement.classList.add("signature-picker-signature-label");
	signatureLabelElement.innerHTML = this.i10n("signaturePicker.signature", "Use an existing signature or create a new one");
	
	var initialsLabelElement = document.createElement("div");
	initialsLabelElement.classList.add("signature-picker-initials-label");
	initialsLabelElement.innerHTML = this.i10n("signaturePicker.initials", "Use existing initials or create new ones");
	
	var signatureFields = this.buildFields(signHereAnnotation, true);
	var initialsFields = this.buildFields(signHereAnnotation, false);
	
	this.windowElement.appendChild(windowContentElement);
	windowContentElement.appendChild(closeButton);
	/*
	if (signHereButton) {
		windowContentElement.appendChild(signHereButton);
	}
	*/
	windowContentElement.appendChild(titleElement);
	windowContentElement.appendChild(signatureLabelElement);
	windowContentElement.appendChild(signatureFields);
	windowContentElement.appendChild(initialsLabelElement);
	windowContentElement.appendChild(initialsFields);
	
	document.body.appendChild(this.windowElement);
	$(".signature-picker-window .tooltip").tooltipster();
};

SignaturePicker.prototype.buildFields = function(signHereAnnotation, signature) {
	// Thumbnail
	// Upload Button
	// Draw Button
	// Type Button
	// Use Button (if exists)
	// Delete Button (if exists)
	var self = this;
	
	var tooltipPrefix;
	var signatureExists;
	if (signature) {
		tooltipPrefix = "signature";
		signatureExists = this.signatureDataStore.getSignatureImageUrl();
	} else {
		tooltipPrefix = "initials";
		signatureExists = this.signatureDataStore.getInitialsImageUrl();
	}
	
	var adjustVisibility = function() {
		self.setVisible(thumbnailElement, signatureExists);
		self.setVisible(useButton, signatureExists);
		self.setVisible(deleteButton, signatureExists);
	};
	
	var containerElement = document.createElement("div");
	if (signature) {
		containerElement.classList.add("signature-picker-signature");
	} else {
		containerElement.classList.add("signature-picker-initials");
	}
	containerElement.classList.add("signature-picker-fields");

	var thumbnailUrl = signature ? this.signatureDataStore.getSignatureImageUrl() : this.signatureDataStore.getInitialsImageUrl();
	
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
					if (signature) {
						self.signatureDataStore.setSignatureImageUrl(imageUrl);
					} else {
						self.signatureDataStore.setInitialsImageUrl(imageUrl);
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
			if (signature) {
				self.signatureDataStore.setSignatureImageUrl(imageUrl);
			} else {
				self.signatureDataStore.setInitialsImageUrl(imageUrl);
			}
		};
		self.drawAnnotationPicked(signHereAnnotation, signature, saveCallback);
		self.closeWindow();
	};
	
	var typeButton = document.createElement("button");
	typeButton.classList.add("signature-picker-type-button");
	typeButton.setAttribute("data-l10n-id", "buttons.type");
	typeButton.classList.add("tooltip");
	typeButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".type.tooltip", "Type");
	typeButton.onclick = function(e) {
		var saveCallback = function(imageUrl) {
			if (signature) {
				self.signatureDataStore.setSignatureImageUrl(imageUrl);
			} else {
				self.signatureDataStore.setInitialsImageUrl(imageUrl);
			}
		};
		self.textAnnotationPicked(signHereAnnotation, signature, saveCallback);
		self.closeWindow();
	};
	
	var deleteButton = document.createElement("button");
	deleteButton.classList.add("signature-picker-delete-button");
	deleteButton.setAttribute("data-l10n-id", "buttons.delete");
	deleteButton.classList.add("tooltip");
	deleteButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".delete.tooltip", "Delete");
	deleteButton.onclick = function(e) {
		if (signature) {
			self.signatureDataStore.removeSignatureImageUrl();
		} else {
			self.signatureDataStore.removeInitialsImageUrl();
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
		self.imageAnnotationPicked(signHereAnnotation, signature, thumbnailElement.src);
		self.closeWindow();
	};
	
	containerElement.appendChild(thumbnailElement);
	containerElement.appendChild(actionsElement);
	actionsElement.appendChild(inputField);
	actionsElement.appendChild(drawButton);
	actionsElement.appendChild(uploadButton);
	//actionsElement.appendChild(useButton);
	actionsElement.appendChild(typeButton);
	actionsElement.appendChild(deleteButton);
	
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

SignaturePicker.prototype.drawAnnotationPicked = function(signHereAnnotation, signature, saveCallback) {
	console.debug("Draw " + (signature ? "signature" : "initials"));
};	

SignaturePicker.prototype.imageAnnotationPicked = function(signHereAnnotation, signature, imageUrl) {
	console.debug("Use " + (signature ? "signature" : "initials"));
};	

SignaturePicker.prototype.textAnnotationPicked = function(signHereAnnotation, signature, saveCallback) {
	console.debug("Type " + (signature ? "signature" : "initials"));
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
