var webViewerInstance = false;
var annotationEnabled = true;
var isViewerReadOnly = true;

var FitWidth = "FitWidth";

(window.isWebViewerInstanceSet = function() {
    return window.webViewerInstance && true;
})

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(isWebViewerInstanceSet()) {
        window.webViewerInstance.setReadOnly(isReadOnly);
        isViewerReadOnly = isReadOnly;
    }
});

(window.setEnableAnnotations = function(enableAnnotations) {
    if(isWebViewerInstanceSet()) {
        if(enableAnnotations) {
               window.webViewerInstance.enableAnnotations();
               annotationEnabled=true;
        } else {
            window.webViewerInstance.disableAnnotations();
            annotationEnabled=false;
        }
    }
});

(window.rePullAnnotations = function() {
    if(isWebViewerInstanceSet()) {
        $.get(documentAnnotationUrl, function (data) {

            ignoreAnnotationChange = true;
            if (data) {
                window.webViewerInstance.docViewer.getAnnotationManager().importAnnotations(data);
            }
            ignoreAnnotationChange = false;
        });
    }
});

(window.resetToCurrentValues = function() {
    var viewerControl = document.getElementById(canvasId).childNodes[0].contentWindow.readerControl;

    if(isWebViewerInstanceSet() && window.webViewerInstance != viewerControl) {
        window.webViewerInstance = viewerControl;
        window.webViewerInstance.setAnnotationUser(name);
        window.webViewerInstance.setAdminUser(admin);

        window.webViewerInstance.setLanguage(language);

        registerAnnotationChanged(window.webViewerInstance);
        registerAnnotationLoaded(window.webViewerInstance);

        window.webViewerInstance.setFitMode(FitWidth);
        window.webViewerInstance.setReadOnly(isViewerReadOnly);
    }
});

(window.registerAnnotationChanged = function(instance) {

    var docViewer = instance.docViewer;
    var annotManager = docViewer.getAnnotationManager();

    annotManager.on('annotationChanged', function (event, annotations, action) {
        if (action === 'add' || action === 'modify' || action === 'delete') {
            if (ignoreAnnotationChange) {
                return;
            }
            debugger
            var annotationsFile = annotManager.exportAnnotations({links: false, widgets: false});

            $.post(documentAnnotationCallBack, {
                'resourceKey': documentAnnotationRK,
                'data': annotationsFile
            });
        }
    });
});

(window.registerAnnotationLoaded = function(instance) {

    var docViewer = instance.docViewer;

    docViewer.on('annotationsLoaded', function () {
        instance.setFitMode(FitWidth);
        $.get(documentAnnotationUrl, function (data) {
            var annotManager = docViewer.getAnnotationManager();

            ignoreAnnotationChange = true;
            if (data) {
                annotManager.importAnnotations(data);
            }
            ignoreAnnotationChange = false;

            setEnableAnnotations(annotationEnabled)

            if (searchTerm) {
                instance.searchTextFull(searchTerm);
            }

            instance.setReadOnly(isViewerReadOnly);
        });
    });
});

$(function () {
    var mapParams;
    debugger
    if (license) {
        mapParams = {
            licenseKey: license,
            path: '/constellio/VAADIN/themes/constellio/pdftron/lib',
            initialDoc: documentContent,
        }
    } else {
        mapParams = {
            path: '/constellio/VAADIN/themes/constellio/pdftron/lib',
            initialDoc: documentContent,
        }
    }

    WebViewer(mapParams,
        document.getElementById(canvasId)).then(function (instance) {
        debugger
        window.webViewerInstance = instance;

        instance.setAnnotationUser(name);
        instance.setAdminUser(admin);
        instance.setReadOnly(isReadOnly);
        instance.setLanguage(language);
        annotationEnabled = true;
        isViewerReadOnly = isReadOnly;

        registerAnnotationChanged(instance);
            registerAnnotationLoaded(instance);
        });
})
