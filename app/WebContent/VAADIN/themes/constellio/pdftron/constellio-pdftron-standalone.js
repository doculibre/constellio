function openInConstellio() {
	var documentId = getDocumentId();
	var displayDocumentUrl = getConstellioUrl() + "#!displayDocument/" + documentId;
	window.open(displayDocumentUrl);
	//alert("Open in Constellio: " + displayDocumentUrl);
}

function getUrlParams(urlPath) {
    var vars = {};
    var parts = urlPath.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

function getUrlParam(urlPath, paramName){
    var paramValue = "";
    if (urlPath.indexOf(paramName) > -1) {
        paramValue = getUrlParams(urlPath)[paramName];
    }
    return paramValue;
}

function getAddressBarParam(paramName) {
	var addressBarUrl = window.location.href;
	return getUrlParam(addressBarUrl, paramName);
}

function getDocumentContentUrl() {
	var addressBarParam = getAddressBarParam("documentPath");
	var documentContentUrl = decodeURIComponent(addressBarParam);
	console.info("Document content URL: " + documentContentUrl);
	return documentContentUrl;
}

function getDocumentId() {
	var documentContentUrl = getDocumentContentUrl();
	return getUrlParam(documentContentUrl, "recordId");
}

function getConstellioUrl() {
	var documentContentUrl = getDocumentContentUrl();
	return documentContentUrl.split("/constellio")[0] + "/constellio";
}

WebViewer(
  {
    path: './lib',
    initialDoc: getDocumentContentUrl(),
  },
  document.getElementById('viewer')
).then(function(instance) {
	//instance.disableAnnotations();
	instance.setLanguage("fr");
	instance.setReadOnly(true);
    
	instance.setHeaderItems(header => {
      var openInConstellioButton = {
		type: 'actionButton',
		title: 'Ouvrir dans Constellio',
        img: '../../../constellio-icon.png',
        onClick: function() {
            openInConstellio();
        }
      }

      header.push(openInConstellioButton);
    });
});
