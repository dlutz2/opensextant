package org.opensextant;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

//import org.opensextant.extraction.PlacenameMatcher;

/* A "canonical" GATE application
 Takes a GATE application (GAPP) file and directory as input
 processes each file in the directory through the process defined in the GAPP.
 */
public class GATETestDriver {

	public static void main(String[] args) throws Exception {

		// start time
		Long start = System.nanoTime();

		// file containing the pre-defined GATE application (GAPP)
		File gappFile = new File(args[0]);

		// directory containing the files to be processed
		File inDir = new File(args[1]);

		// get the list of files to be processed
		Collection<File> files = FileUtils.listFiles(inDir, null, true);
		int numDocs = files.size();

		// count the total number of annotations found
		Integer totalAnnos = 0;

		// initialize GATE
		Gate.init();

		// load the saved application from the GAPP file
		CorpusController application = (CorpusController) PersistenceManager
				.loadObjectFromFile(gappFile);

		// the corpus (to be re-used)
		Corpus corpus = Factory.newCorpus("Test Corpus");

		// associate the corpus with the application
		application.setCorpus(corpus);

		// process the files, one at a time
		for (File f : files) {

			// create a GATE document from the file contents
			String contents = FileUtils.readFileToString(f);
			Document doc = Factory.newDocument(contents);

			// or create a GATE document directly from the file
			// Document doc = Factory.newDocument(f.toURI().toURL());

			// put the document in the corpus
			corpus.add(doc);

			// run the application
			application.execute();

			// remove the document from the corpus
			corpus.clear();

			// this is where we would do something with
			// the annotations found
			AnnotationSet results = doc.getAnnotations();

			for (Annotation a : results) {

			}

			totalAnnos = totalAnnos + results.size();

			// cleanup
			Factory.deleteResource(doc);
			contents = null;
			f = null;

		} // end file loop

		// cleanup
		Factory.deleteResource(corpus);
		application.cleanup();

		// This is a bad thing
		// PlacenameMatcher.shutdown();

		// finish time
		Long end = System.nanoTime();

		// print some summary stats
		Double totalDuration = (end - start) / 1000000000.0;
		Double rate = numDocs / totalDuration;

		System.out.println(numDocs + "\t" + totalAnnos + "\t" + totalDuration
				+ "\t" + rate);

	}

}
