package gov.nih.nlm.consort.pmc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import gov.nih.nlm.consort.Constants;
import gov.nih.nlm.consort.Utils;
import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.Section;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Span;
import gov.nih.nlm.ling.io.XMLReader;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.ling.sem.SemanticItemFactory;
import gov.nih.nlm.ling.util.FileUtils;

public class ExtractionByPhraseDocument {
	private static Logger log = Logger.getLogger(ExtractionByPhraseDocument.class.getName());	
	
	private static Map<Class<? extends SemanticItem>,List<String>> annTypes =  new HashMap<Class<? extends SemanticItem>,List<String>>();
	private static XMLReader xmlReader = new XMLReader();
	
	public static Map<String,String> NORM_SECTION_LABELS = new HashMap<>();
	
	private static Map<String,List<String>>  ITEM_TITLE_MAP = new HashMap<>();
	private static List<String> instanceStrs = new ArrayList<>();
		
	static {
		ITEM_TITLE_MAP.put("3a",Arrays.asList(	"randomized controlled trial","randomised controlled trial", "placebo controlled", "double blind", "parallel group","placebo-controlled","double-blind","parallel-group",
			"multicentre","multicenter")); // Trial design
		ITEM_TITLE_MAP.put("3b",Arrays.asList("no longer feasible","decision was taken", "decision was made","committee agreed")); // Changes to Trial Design
		ITEM_TITLE_MAP.put("4a",Arrays.asList("inclusion criteria","exclusion criteria","patients were eligible","aged","criteria were","history of","inclusion","excluded","eligible","for eligibility")); // Eligibility Criteria
		ITEM_TITLE_MAP.put("4b",Arrays.asList("study was conducted","conducted from","study area was","the study population","conducted in","carried out","enrolled in","enrolled into","came from","screened for",
				"recruited patients from"," recruited from")); // Data Collection Setting
		ITEM_TITLE_MAP.put("5",Arrays.asList("matching placebo","regimen","matching","mg/kg","mg","infusion","study medication","medication","administered","open-label",
				"patients were randomly","eligible patients","daily")); // Interventions
		ITEM_TITLE_MAP.put("6a",Arrays.asList("primary outcome was","primary outcomes were", "primary outcomes","primary outcome",
				"secondary outcomes","secondary outcome","secondary outcomes were","secondary outcome was",
				"primary objective","adverse effects","illness effects","outcome assessment","outcome was","after randomisation","after randomization","objective","questionnaire",
				"we defined","events","assessment","measurement","endpoint","primary endpoint","primary endpoints")); // Outcomes
		ITEM_TITLE_MAP.put("6b",Arrays.asList("original trial protocol","trial because of","early termination","the trial because","the original trial")); // Changes To Outcomes
		ITEM_TITLE_MAP.put("7a",Arrays.asList("sample size","power to detect","power","we estimated that","need a sample","sufficient power to","detect a difference","relative reduction","have sufficient power","dropout rate"
				)); // Sample Size Determination
		ITEM_TITLE_MAP.put("7b",Arrays.asList("monitoring","monitoring committee","data monitoring","ethics committee","interim","stopping","cessation","trial cessation","stopping rule","interim analysis","interim analyses",
				"committee judged","committee recommended","halt recruitment")); // Interim Analyses - Stopping Guidelines
		ITEM_TITLE_MAP.put("8a",Arrays.asList("computer-generated","computer generated","block size","sequence generation", "allocation sequence","randomization sequence","randomisation sequence","generator","code was generated","allocation of randomization", "allocation of randomisation", 
				"random number sequence","random number sequences","random number generator","sequential allocation", "random number","randomization codes","randomisation codes","codes in blocks")); // Sequence Generation
		ITEM_TITLE_MAP.put("8b",Arrays.asList("block size","stratified by","stratified","by stratification","stratification","blocks of","computer-generated randomization","computer-generated randomisation",
				"computer generated randomization","computer generated randomisation","computer-generated","computer generated","random number generator",
				"random number sequence","random number sequences","codes in blocks","randomisation codes","randomization codes","random number")); //Randomization Type
		ITEM_TITLE_MAP.put("9",Arrays.asList("concealment","known only to","sealed envelopes","sealed envelope","telephoning a","telephoning an","assigned box","assigned boxes",
				"telephone randomisation service","telephone randomization service","packed in sealed","secure online","accessing a secure")); // Allocation Concealment Mechanism
		ITEM_TITLE_MAP.put("10",Arrays.asList("randomisation system","randomization system","web-based randomisation","web-based randomization","web randomisation","web randomization","online randomisation","online randomization",
				"online web randomization","online web randomisation","minimisation","minimization","minimisation algorithm","web-based","randomization service","randomisation service",
				"randomization program","randomization programme","randomisation program","randomisation programme")); // Randomization Implementation"
		ITEM_TITLE_MAP.put("11a",Arrays.asList("to treatment allocation","masked to treatment","masked", "blinded to","blind to","blinding", "double-blinded to","masking","not have access")); //Blinding Procedure
		ITEM_TITLE_MAP.put("11b",Arrays.asList("tablets","numbered","identical","identically","match the size","manufactured to match")); //Similarity of Interventions
		ITEM_TITLE_MAP.put("12a",Arrays.asList("primary analysis","primary analyses","logistic regression","log-rank test","cox proportional","survival time","cox","curve","curves","log-rank","hazard","kaplan-meier","meier","kaplan",
				"proportional","efficacy analysis","efficacy analyses","residual","intention-to-treat","linear","schoenfeld","comparisons","odds ratios","odds ratio","correlation","ratios",
				"covariate","covariates","variables","changes from baseline","change from baseline","regression","exact test","fisher","binomial","two-sided","one-sided")); // Statistical Methods for Outcome Comparison
		ITEM_TITLE_MAP.put("12b",Arrays.asList("subgroup analyses","subgroup analysis","subgroup","unplanned","exploratory","between subgroups","prespecified subgroup","prespecified subgroups")); // Statistical Methods For Other Analyses
//		ITEM_TITLE_MAP.put("24", Arrays.asList("protocol")); //Protocol 
		
	}

	public static List<String> getLabels(Sentence sent) {
		String text = sent.getText().toLowerCase();
		List<String> labels = new ArrayList<>();
		for (String i: ITEM_TITLE_MAP.keySet()) {
			List<String> phrases = ITEM_TITLE_MAP.get(i);
			Collections.sort(phrases, new Comparator<String>() {
				public int compare(String a, String b) {
					int sa = a.split("[ ]+").length;
					int sb = b.split("[ ]+").length;
					if (sa == sb) return a.compareTo(b);
					return sb - sa;
				}
			});
			for (String p: phrases) {
				if (text.contains(p)) {
					labels.add(i);
					break;
				}
			}
		}
		Collections.sort(labels, new Comparator<String>() {
			public int compare(String a, String b) {
				Matcher ma = Constants.ID_PATTERN.matcher(a);
				Matcher mb = Constants.ID_PATTERN.matcher(b);
				int ida= -1; int idb = -1;
				String sa = ""; String sb = "";
				if (ma.find()) {
					ida = Integer.parseInt(ma.group(1));
					sa = ma.group(2);
				}
				if (mb.find()) {
					idb = Integer.parseInt(mb.group(1));
					sb = mb.group(2);
				}
				if (ida == idb ) {
					return sa.compareTo(sb);
				}
				return ida-idb;
			}
		});
		return labels;
	}
	
	public  static void processFile(String filename)  throws Exception  {
		if (new File(filename).length() == 0) return;
		Document doc = xmlReader.load(filename, false,SemanticItemFactory.class, annTypes, null);
		List<Sentence> sentences = doc.getSentences();
		Set<String> labelsFound = new HashSet<>();
		for (Sentence sent: sentences) {
//			if (sent.getText().length() < 20) continue;
//			if (sent.getWords() == null || sent.getWords().size() < 5) continue;
			Section top = Utils.getTopSection(sent);
			if (top == null) { 
				continue;
			}
			String topName = top.getTitle();
			if (topName == null) continue;
			topName = topName.toLowerCase();
			String normName = NORM_SECTION_LABELS.get(topName);
			if ((normName!= null && normName.equals("METHODS")) || Utils.containsSubstring(Constants.POTENTIAL_METHOD_TITLES,topName)) {
				Section innermost = Utils.getSection(doc,sent);
				if (innermost.getTitleSpan() != null && Span.overlap(innermost.getTitleSpan(), sent.getSpan())) continue;
				List<String> labels = getLabels(sent);
				labelsFound.addAll(labels);
			}
		}
		if (labelsFound.size() > 0) {
//			StringBuffer buf = new StringBuffer();
			List<String> found = new ArrayList<>(labelsFound);
			Collections.sort(found);
/*					for (int i=0; i < labels.size()-1;i++) {
				String l = labels.get(i);
				buf.append(Constants.CONSORT_ID_ITEMS.get(l) + ",");
			}
			buf.append(Constants.CONSORT_ID_ITEMS.get(labels.get(labels.size()-1)));*/
			for (String f: found) 
				instanceStrs.add(doc.getId() + "|" +  f);
		}
	}	
	
	public static void processDirectory(String dirname) throws IOException {
		try {
			List<String> files = FileUtils.listFiles(dirname, false, "xml");
			Collections.sort(files);
			int fileNum = 0;
			for (String filename: files) {
				String filenameNoExt = filename.replace(".xml", "");
				filenameNoExt = filenameNoExt.substring(filenameNoExt.lastIndexOf(File.separator)+1);
				log.info("Processing " + filenameNoExt + ":" + ++fileNum);
				try {
					processFile(filename);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException ie) {
			log.severe("Unable to read input files from " + dirname);
		}
	}
	
	
	private static void init() throws IOException {
		NORM_SECTION_LABELS = Utils.loadNormalizedSectionNames("resources/Structured-Abstracts-Labels-110613.txt");	
		Constants.loadCONSORTItems("resources/consort_id_mapping.txt");
	}
	
	public static void main(String[] args) throws IOException {
		init();
		String inDirName = args[0];
		String outFile = args[1];
		processDirectory(inDirName);
		
		PrintWriter pw = new PrintWriter(outFile);
		for (String s: instanceStrs) {
			pw.write(s + "\n");
		}
		pw.flush();
		pw.close(); 
	}
}
