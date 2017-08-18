/**
 * bpmn-js-seed - async
 *
 * This is an example script that loads a bpmn diagram <diagram.bpmn> and opens
 * it using the bpmn-js viewer.
 *
 * YOU NEED TO SERVE THIS FOLDER VIA A WEB SERVER (i.e. Apache) FOR THE EXAMPLE TO WORK.
 * The reason for this is that most modern web browsers do not allow AJAX requests ($.get and the like)
 * of file system resources.
 */
(function(BpmnViewer, $) {

  // create viewer
  var bpmnViewer = new BpmnViewer();
  bpmnViewer.attachTo('#bpmn-viewer-canvas');


  // import function
  function importXML(xml) {
    console.log('viewer : importXml : ' + xml);

    console.log('2 viewer : importXml : ' + xml);
    // import diagram
    bpmnViewer.importXML(xml, function(err) {
      if (err) {
        console.log('could not render diagram', err);
      } else {
        console.log('rendered');
      }

       var canvas = bpmnViewer.get('canvas');


       // zoom to fit full viewport
      canvas.zoom('fit-viewport');

    });
  }


  // load external diagram file via AJAX and import it
  //$.get('diagram.bpmn', importXML, 'text');
  console.log('bpmnDiagramURL : ' + bpmnDiagramURL);
   $.get(bpmnDiagramURL, importXML, 'text');


})(window.BpmnJS, window.jQuery);