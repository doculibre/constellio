"use strict";

function SignatureDataStore(config) {
	this.config = config;
	var self = this;
	
	if (this.config) {
		this.getSignatureServiceUrl = this.config["getSignatureServiceUrl"];
		this.saveSignatureServiceUrl = this.config["saveSignatureServiceUrl"];
	}
	if (this.getSignatureServiceUrl) {
		this.callGetSignatureImageUrlService(false, function(signatureImageUrl) {
			if (signatureImageUrl) {
				self.setSignatureImageUrl(signatureImageUrl, true);
			}
			self.callGetSignatureImageUrlService(true, function(initialsImageUrl) {
				if (initialsImageUrl) {
					self.setInitialsImageUrl(initialsImageUrl, true);
				}
			});
		});
	}
}

SignatureDataStore.prototype.callGetSignatureImageUrlService = function(initials, success, fail) {
	var adjustedUrl = this.addInitialsParam(this.getSignatureServiceUrl, initials);
	$.ajaxQueue(adjustedUrl)
	.done(function(data, textStatus, jqXHR) {
		var signatureImageUrl = data;
		success(signatureImageUrl);			
	})
	.fail(function(jqXHR, textStatus, errorThrown) {
		if (fail) {
			fail(textStatus, errorThrown);
		} else {
			console.error("Error while getting user signature: " + textStatus);
			console.error(errorThrown);
		}
	});  
};	

SignatureDataStore.prototype.callSaveSignatureImageUrlService = function(imageUrl, initials, success, fail) {
	var adjustedUrl = this.addInitialsParam(this.saveSignatureServiceUrl, initials);
	var imageUrlParam = imageUrl;
	$.ajaxQueue({
		url: adjustedUrl,
		data: imageUrlParam,
		method: "POST",
		contentType: "application/json; charset=utf-8",
		dataType: "json"
	})
	.done(function(data, textStatus, jqXHR) {
		if (success) {
			success();
		}
	})
	.fail(function(jqXHR, textStatus, errorThrown) {
		if (fail) {
			fail(textStatus, errorThrown);
		} else {
			console.error("Error while saving user signature: " + textStatus);
			console.error(errorThrown);
		}
	});  
};	

SignatureDataStore.prototype.addInitialsParam = function(serviceUrl, initials) {
	var result = serviceUrl;
	if (result.indexOf("?") != -1) {
		result += "&";
	} else {
		result += "?";
	}
	result += "initials=" + initials;
	return result;
};

SignatureDataStore.prototype.clear = function() {
	this.removeSignatureImageUrl();
	this.removeInitialsImageUrl();
};

SignatureDataStore.prototype.getSignatureImageUrl = function() {
	return this.getStorage().getItem("signatureImageUrl");
};

SignatureDataStore.prototype.setSignatureImageUrl = function(signatureImageUrl, noServiceCall) {
	this.getStorage().setItem("signatureImageUrl", signatureImageUrl);
	if (this.saveSignatureServiceUrl && !noServiceCall) {
		this.callSaveSignatureImageUrlService(signatureImageUrl, false);
	}
};

SignatureDataStore.prototype.removeSignatureImageUrl = function() {
	this.getStorage().removeItem("signatureImageUrl");
	if (this.saveSignatureServiceUrl) {
		this.callSaveSignatureImageUrlService("", false);
	}
};

SignatureDataStore.prototype.getInitialsImageUrl = function() {
	return this.getStorage().getItem("initialsImageUrl");
};

SignatureDataStore.prototype.setInitialsImageUrl = function(initialsImageUrl, noServiceCall) {
	this.getStorage().setItem("initialsImageUrl", initialsImageUrl);	
	if (this.saveSignatureServiceUrl && !noServiceCall) {
		this.callSaveSignatureImageUrlService(initialsImageUrl, true);
	}
};

SignatureDataStore.prototype.removeInitialsImageUrl = function() {
	this.getStorage().removeItem("initialsImageUrl");	
	if (this.saveSignatureServiceUrl) {
		this.callSaveSignatureImageUrlService("", true);
	}
};

SignatureDataStore.prototype.isStorageAvailable = function(type) {
    var storage;
    try {
        storage = window[type];
        var x = '__storage_test__';
        storage.setItem(x, x);
        storage.removeItem(x);
        return true;
    } catch(e) {
        return e instanceof DOMException && (
            // everything except Firefox
            e.code === 22 ||
            // Firefox
            e.code === 1014 ||
            // test name field too, because code might not be present
            // everything except Firefox
            e.name === 'QuotaExceededError' ||
            // Firefox
            e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
            // acknowledge QuotaExceededError only if there's something already stored
            (storage && storage.length !== 0);
    }
};

SignatureDataStore.prototype.getStorage = function() {
	var storage;
	if (this.isStorageAvailable("localStorage")) {
		storage = window.localStorage;
	} else if (this.isStorageAvailable("sessionStorage")) {
		storage = window.sessionStorage;
	} else if (this.tempStorage) {
		storage = this.tempStorage;
	} else {
		this.tempStorage = {
			items: [],
			
			setItem: function(key, item) {
				this.items[key] = item;
			},
			
			getItem: function(key) {
				return this.items[key] ? this.items[key] : null;
			},
			
			removeItem: function(key) {
				delete this.items[key];
			},
			
			clear: function() {
				this.items = [];
			},
			
			key: function(index) {
				return Object.keys(this.items)[index];
			}
		};
		storage = this.tempStorage;
	}
	return storage;
};
