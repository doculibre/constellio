var webViewerInstance = null;

const FitWidth = "FitWidth";

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(webViewerInstance !== null) {
        webViewerInstance.setReadOnly(isReadOnly);
    }
});

(window.setEnableAnnotations = function(enableAnnotations) {
    if(webViewerInstance !== null) {
        if(enableAnnotations) {
               webViewerInstance.enableAnnotations();
        } else {
            webViewerInstance.disableAnnotations()
        }
    }
});

$(() => {
    WebViewer({
            path: '/constellio/VAADIN/themes/constellio/pdftron/lib',
            initialDoc: documentContent,
        },
        document.getElementById(canvasId)).then(instance => {
            webViewerInstance = instance;
            instance.setAnnotationUser(name);
            instance.setAdminUser(admin);
            instance.setReadOnly(true);

            const {docViewer} = instance;
            const annotManager = instance.annotManager;

            annotManager.on('annotationChanged', (event, annotations, action) => {
                if (action === 'add' || action === 'modify' || action === 'delete') {
                     if(ignoreAnnotationChange) {
                        return;
                     }

                    const annotationsFile = annotManager.exportAnnotations({links: false, widgets: false});
                    $.post(documentAnnotationCallBack, {
                        'resourceKey': documentAnnotationRK,
                        'data': annotationsFile
                    })
                }
            });

            docViewer.on('documentLoaded', () => {
                instance.setFitMode(FitWidth);
                $.get(documentAnnotationUrl, (data) => {
                    if(data) {
                        annotManager.importAnnotations(data);
                    }
                    ignoreAnnotationChange = false;
                });
            });
        });
})
