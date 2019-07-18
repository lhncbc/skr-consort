package gov.nih.nlm.consort.pmc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

public class ExtractionBySectionHeader {
	private static Logger log = Logger.getLogger(ExtractionBySectionHeader.class.getName());	
	
	private static Map<Class<? extends SemanticItem>,List<String>> annTypes =  new HashMap<Class<? extends SemanticItem>,List<String>>();
	private static XMLReader xmlReader = new XMLReader();
	
	public static Map<String,String> NORM_SECTION_LABELS = new HashMap<>();
	
	private static Map<String,List<String>>  ITEM_TITLE_MAP = new HashMap<>();
	private static Map<String,List<String>>  STRICT_ITEM_TITLE_MAP = new HashMap<>();
	private static List<String> instanceStrs = new ArrayList<>();
	
	private static boolean STRICT_MODE = true;
	
		
	static {
		ITEM_TITLE_MAP.put("3a",Arrays.asList("design")); // Trial design
		ITEM_TITLE_MAP.put("3b",Arrays.asList("change|method","change|protocol","change|design","change|treatment","change|intervention","change|recruitment","change|eligibility","change|plan")); // Changes to Trial Design
		ITEM_TITLE_MAP.put("4a",Arrays.asList("participant","population","patient","inclusion","exclusion","subject","control group","eligibility")); // Eligibility Criteria
		ITEM_TITLE_MAP.put("4b",Arrays.asList("recruitment","setting")); // Data Collection Setting
		ITEM_TITLE_MAP.put("5",Arrays.asList("intervention","treatment")); // Interventions
		ITEM_TITLE_MAP.put("6a",Arrays.asList("outcome","measure","assessment","measurement","endpoint")); // Outcomes
		ITEM_TITLE_MAP.put("6b",Arrays.asList("change|outcome","change|endpoint")); // Changes To Outcomes
		ITEM_TITLE_MAP.put("7a",Arrays.asList("sample size")); // Sample Size Determination
		ITEM_TITLE_MAP.put("7b",Arrays.asList("interim","stopping")); // Interim Analyses - Stopping Guidelines
		ITEM_TITLE_MAP.put("8a",Arrays.asList("sequence|generation","allocation|sequence")); // Sequence Generation
		ITEM_TITLE_MAP.put("8b",Arrays.asList("randomis","randomiz")); //Randomization Type
		ITEM_TITLE_MAP.put("9",Arrays.asList("concealment")); // Allocation Concealment Mechanism
		ITEM_TITLE_MAP.put("10",Arrays.asList("randomis","randomiz","random|implementation")); // Randomization Implementation
		ITEM_TITLE_MAP.put("11a",Arrays.asList("blinding","masking")); //Blinding Procedure
		ITEM_TITLE_MAP.put("11b",Arrays.asList("similarity|intervention","similarity|treatment")); //Similarity of Interventions
		ITEM_TITLE_MAP.put("12a",Arrays.asList("statistical","statistics","analys","data analys")); // Statistical Methods for Outcome Comparison
		ITEM_TITLE_MAP.put("12b",Arrays.asList("subgroup analys")); // Statistical Methods For Other Analyses
//		ITEM_TITLE_MAP.put("24", Arrays.asList("protocol")); //Protocol 
		
		STRICT_ITEM_TITLE_MAP.put("3a",Arrays.asList("study design","trial design","design")); // Trial design
		STRICT_ITEM_TITLE_MAP.put("3b",Arrays.asList("changes to methods after trial commencement","changes in methods after trial commencement","changes to methods from trial registration stage",
				"changes to trial design","changes to trial design and treatment","changes to original trial design","changes to the trial design","changes in interventions",
				"important changes to methods after trial commencement","protocol changes","change in the protocol","changes to the study protocol","study protocol changes",
				"changes to projected recruitment rate","methodological changes to study protocol","changes from published protocol","changes from published protocol","	changes to eligibility criteria",
				"changes in protocol","changes to protocol","changes in intervention","changes from the original protocol","amendments/changes to the original protocol","changes to the original analysis plan",
				"amendments/changes to the original study protocol","changes from planned analysis in protocol","protocol amendments")); // Changes to Trial Design
		STRICT_ITEM_TITLE_MAP.put("4a",Arrays.asList("participants","patients","study population", "subjects","study participants","exclusion criteria", "inclusion criteria","patient selection","inclusion and exclusion criteria","study subjects","eligibility criteria")); // Eligibility Criteria
		STRICT_ITEM_TITLE_MAP.put("4b",Arrays.asList("data collection","recruitment","recruitment and follow-up","setting","study setting")); // Data Collection Setting
		STRICT_ITEM_TITLE_MAP.put("5",Arrays.asList("intervention","interventions","treatment")); // Interventions
		STRICT_ITEM_TITLE_MAP.put("6a",Arrays.asList("outcomes","outcome measures","measures","assessments","measurements","secondary outcomes","primary outcomes","primary outcome measures","endpoints","secondary outcome measures","primary outcome","study outcomes")); // Outcomes
		STRICT_ITEM_TITLE_MAP.put("6b",Arrays.asList("changes to trial outcomes","changes to outcomes","changes of trial outcomes after commencement",
				"changes in trial outcomes after commencement","changes in trial outcomes after trial commencement")); // Changes To Outcomes
		STRICT_ITEM_TITLE_MAP.put("7a",Arrays.asList("sample size","sample size calculation")); // Sample Size Determination
		STRICT_ITEM_TITLE_MAP.put("7b",Arrays.asList("interim analysis","interim analyses","stopping rules","interim monitoring","interim analysis and study monitoring","data monitoring plan and stopping rules",
				"interim analyses and stopping rules","data monitoring committee and interim analyses","stopping guidelines and interim analysis","interim analysis and stopping rules","interim analyses and stopping guidelines",
				"interim analysis/early stopping rules")); // Interim Analyses - Stopping Guidelines
		STRICT_ITEM_TITLE_MAP.put("8a",Arrays.asList("sequence generation","randomization—sequence generation","randomization–sequence generation", "randomization: sequence generation", "randomization sequence generation",
				"random allocation sequence","generation of allocation sequence", "randomization – sequence generation","sequence allocation generation","randomisation sequence generation",
				"allocation sequence generation","randomization –sequence generation","randomization-sequence generation","randomization−sequence generation","randomization − sequence generation","allocation sequence")); // Sequence Generation
		STRICT_ITEM_TITLE_MAP.put("8b",Arrays.asList("randomization","randomisation","randomisation procedure","randomization procedures","randomization procedure","randomization scheme","method of randomization",
				"randomization process","method of randomisation","randomisation process")); //Randomization Type
		STRICT_ITEM_TITLE_MAP.put("9",Arrays.asList("allocation concealment","allocation concealment mechanism","concealment of allocation","randomisation – allocation concealment",
				"randomization – allocation concealment","randomization–allocation concealment","concealment mechanism", "allocation and concealment","concealment mechanism and implementation",
				"allocation concealment and implementation","randomization-allocation concealment","allocation concealment and implementation","randomization-allocation concealment",
				"concealment of group allocation to participants","randomization: allocation concealment","allocation concealment and mechanism","implementation and allocation concealment",
				"randomisation-allocation concealment","concealment","concealment of assignment","randomization−allocation concealment")); // Allocation Concealment Mechanism
		STRICT_ITEM_TITLE_MAP.put("10",Arrays.asList("randomization","randomisation","randomisation procedure","randomization procedures","randomization procedure","randomization process","randomisation process",
				"randomization—implementation","randomization – implementation","randomization: implementation","randomization–implementation","randomization-implementation","implementation of randomization",
				"randomisation implementation","randomization−implementation")); // Randomization Implementation
		STRICT_ITEM_TITLE_MAP.put("11a",Arrays.asList("blinding","masking","blinding procedure","blinding procedures","blinding/masking","treatment masking (blinding)","single-blinding","physician blinding",
				"blinding and protection against bias","masking of trial arm","blinding – single blind","masking and blinding","intervention masking","blinding and masking","double blinding","blinding of study treatments",
				"methods of blinding", "blinding (masking)","treatment blinding")); //Blinding Procedure
		STRICT_ITEM_TITLE_MAP.put("11b",Arrays.asList("similarity of the interventions","similarity of interventions")); //Similarity of Interventions
		STRICT_ITEM_TITLE_MAP.put("12a",Arrays.asList("statistical analyses","statistical analysis","statistical methods","data analysis","data analyses","statistics","analysis","analyses")); // Statistical Methods for Outcome Comparison
		STRICT_ITEM_TITLE_MAP.put("12b",Arrays.asList("subgroup analyses","subgroup analysis","sensitivity and subgroup analyses","secondary/subgroup analyses","intention to treat, per-protocol and subgroup analysis")); // Statistical Methods For Other Analyses
//		STRICT_ITEM_TITLE_MAP.put("24", Arrays.asList("study protocol")); //Protocol 
	}

	
/*	private static int getParagraphBegin(Sentence sent) {
		Span sents = sent.getSpan();
		String t = sent.getDocument().getText();
		int next = t.indexOf("\n",sents.getBegin());
		int prev = t.substring(0,next).lastIndexOf("\n")+ 1 ;
		Matcher m = TEXT_PATTERN.matcher(t.substring(prev));
		if (m.find())
			return prev + m.start();
		return -1;
	}
	
	private static int getParagraphEnd(Sentence sent) {
		Span sents = sent.getSpan();
		String t = sent.getDocument().getText();
		return  t.indexOf("\n",sents.getEnd());
	}
	
	public static boolean inLimitationParagraph(Sentence sent) {
		int pb = getParagraphBegin(sent);
		if (pb == -1) return false;
		Sentence piSent = sent.getDocument().getSubsumingSentence(new Span(pb,pb+1));
		if (piSent == null) {
			log.severe("SENT NULL");
			return false;
		}
		int ind = sent.getDocument().getSentences().indexOf(piSent);
		int endInd = sent.getDocument().getSentences().indexOf(sent);
		for (int i = ind; i <= endInd; i++ ) {
			Sentence s = sent.getDocument().getSentences().get(i);
			if (limitationIntroductorySentence(s)) return true;

		}
		return false;
	}
	
	public static boolean limitationIntroductorySentence(Sentence sent) {
		String text = sent.getText().toLowerCase();
		Matcher m = LIMITATION_BEGIN_PATTERN.matcher(text);
		if  (m.find()) return true;
		if (sent.getWords().size() <= 10) {
			m = LIMITATION_ANY_PATTERN.matcher(text);
			if  (m.find()) return true;
		}
		return false;
	}*/
	
	
	private static List<String> getLabels(Section section) {
		if (section == null) return null;
		String title = section.getTitle();
		List<String> labels = new ArrayList<>();
		if (title != null) {
			String name = title.toLowerCase();
			if ((STRICT_MODE)) {
				labels = Utils.getKeysFromValueString(STRICT_ITEM_TITLE_MAP, name);
			} else {
				labels = Utils.getKeysFromValueSubstring(ITEM_TITLE_MAP, name);
			}
		}
		return labels;
	}
	
	public  static void processFile(String filename)  throws Exception  {
		if (new File(filename).length() == 0) return;
		Document doc = xmlReader.load(filename, false,SemanticItemFactory.class, annTypes, null);
		List<Sentence> sentences = doc.getSentences();
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
				String title = innermost.getTitle();
				if (innermost.getTitleSpan() != null && Span.overlap(innermost.getTitleSpan(), sent.getSpan())) continue;
				List<String> labels = getLabels(innermost);
				
				if (labels.size() == 0) {
					List<Section> ancestors = Utils.getAncestorSections(innermost);
					for (Section anc: ancestors) {
						labels = getLabels(anc);
						title = anc.getTitle();
						if (labels.size() > 0) break;
					}
				}
				if (labels.size() == 0) {
					instanceStrs.add(doc.getId() + "|" + sent.getId() + "|0|" + innermost.getTitle() + "|" + sent.getText().replaceAll( "(?m)\r?\n", " ")	);
				}
				else {
					Collections.sort(labels);
					instanceStrs.add(doc.getId() + "|" + sent.getId() + "|" + String.join(",", labels) + "|" +  innermost.getTitle() + "|" + sent.getText().replaceAll( "(?m)\r?\n", " "));
				}
			} else {
				System.err.println("Not Methods: " + topName + "|" + sent.getText());
			}
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
//				if (filename.contains("PMC1175844") == false) continue;
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
		if (args.length > 2) {
			STRICT_MODE = Boolean.parseBoolean(args[2]);
		}
		processDirectory(inDirName);
		
		PrintWriter pw = new PrintWriter(outFile);
		for (String s: instanceStrs) {
			pw.write(s + "\n");
		}
		pw.flush();
		pw.close(); 
	}
}
