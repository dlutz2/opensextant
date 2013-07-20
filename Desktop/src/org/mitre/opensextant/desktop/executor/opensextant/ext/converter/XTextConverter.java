package org.mitre.opensextant.desktop.executor.opensextant.ext.converter;

import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.xtext.ConversionListener;
import org.mitre.opensextant.xtext.ConvertedDocument;
import org.mitre.opensextant.xtext.XText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XTextConverter {

	private static Logger log = LoggerFactory.getLogger(XTextConverter.class);
	private XText converter;
	
	public XTextConverter(String tmpRoot) throws ProcessingException {
        converter = new XText();

        // TOOD: Rework application setup. 
        //     all inputs here should be user settable.
        converter.archiveRoot = tmpRoot+File.separator + "archives";
        converter.tempRoot = tmpRoot+File.separator+ "files";
        
        converter.zone_web_content = false;
        converter.save = true;
        
        converter.save_in_folder = true;
        converter.save_in_archive_root = true;
        
        ConvertedDocument.overwrite = false;
        

        // Complications:  Where do we save converted items?
        //
        try {
            converter.setup();
        } catch (IOException ioerr) {
            throw new ProcessingException("Document converter could not start", ioerr);
        }
        
	}
	
	public List<Document> convert(File input) throws IOException, ResourceInstantiationException {
		
	    // we only want to set the save in folder flags for archives
        boolean originalSIF = converter.save_in_folder;
        boolean originalSIAR = converter.save_in_archive_root;
	    if (!converter.isArchive(input.getAbsolutePath())) {
	        converter.save_in_folder = false;
	        converter.save_in_archive_root = false;
	    }
	    
		final List<ConvertedDocument> documents = new ArrayList<ConvertedDocument>();
		converter.setConversionListener(new ConversionListener() {
			
			@Override
			public void handleConversion(ConvertedDocument doc) {
				documents.add(doc);
			}
		});
		converter.extract_text(input.getAbsolutePath());
		
		List<Document> extractions = new ArrayList<Document>();
		for (ConvertedDocument converted : documents) {
	        Document document;
			if (converted.textpath != null) {
	        	document = Factory.newDocument(new File(converted.textpath).toURI().toURL());
	        } else {
	            if (converted.payload == null) {
	                log.error("Both payload and textpath URL are null. FILE=" + converted.filepath);
	                return null;
	            }
	            document = Factory.newDocument(converted.payload);
	        }
	        document.getFeatures().put(OpenSextantSchema.FILEPATH_FLD, converted.filepath);
	        extractions.add(document);
		}
		
        converter.save_in_folder = originalSIF;
        converter.save_in_archive_root =  originalSIAR;

		return extractions;
		
	}
	
}
