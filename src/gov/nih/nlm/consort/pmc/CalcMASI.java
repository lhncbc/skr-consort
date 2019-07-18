package gov.nih.nlm.consort.pmc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.nlm.ling.util.FileUtils;

public class CalcMASI {
	static List<String> annotators = Arrays.asList("Gerben","Graciela","Halil","Jodi","Mario","Tony");
	static Map<String, String> annotatorPairA = new HashMap<>();
	static Map<String, String> annotatorPairB = new HashMap<>();
	static Map<String,Double> masiScores = new HashMap<>();
	static Map<String,Map<String,Double>> masiScoresBySection = new HashMap<>();
	static Map<String,Map<String,Labeling>> annALabels = new HashMap<>();
	static Map<String,Map<String,Labeling>> annBLabels = new HashMap<>();
	
	static List<String> excludeItems = Arrays.asList("1a","1b");
	static List<String> itemIds = Arrays.asList("1a","1b","2a","2b","3a","3b","4a","4b","5","6a","6b","7a","7b","8a","8b","9","10","11a","11b","12a","12b","13a","13b","14a","14b","15","16","17a","17b","18","19","20","21","22","23","24","25");
	
	static List<List<String>> krippendorffList = new ArrayList<>();
	static Map<String,List<String>> sectionGrouping = new HashMap<>();
//	   3886 Results
//	   3705 Methods
//	   3577 Discussion
//	   1911 Back matter
//	   1134 Abstract
//	   1055 Introduction
//	    352 Patients and methods
//	    344 Summary
//	    169 RESULTS
//	    164 DISCUSSION
//	    158 Title
//	    155 METHODS
//	     84 Background
//	     82
//	     81 RESEARCH DESIGN AND METHODS
//	     60 Design and methods
//	     54 Subjects and methods
//	     49 Research Design and Methods
//	     47 Materials and methods
//	     38 Conclusions
//	     32 INTRODUCTION
//	     30 Supplementary Material
//	     11 Competing interests
//	      8 Authors' contributions
//	      6 Authors’ contributions
//	      5 Study limitations
//	      4 List of abbreviations
//	      3 CONCLUSIONS
//	      2 Study Highlights
//	      2 PROBLEMATIC_SPAN
//	      2 Abbreviations
//	      1 STUDY HIGHLIGHTS
	static List<String> excludedSections = Arrays.asList("title","abstract","summary","supplementary material","authors' contributions","authors’ contributions","list of abbreviations","abbreviations","study highlights");
	static Map<String,String> sectionMap = new HashMap<String,String>();
	
	static {
		sectionMap.put("results","Results");
		sectionMap.put("methods","Methods");
		sectionMap.put("discussion","Discussion");
		sectionMap.put("back matter","Other");
		sectionMap.put("abstract","Abstract");
		sectionMap.put("introduction","Introduction");
		sectionMap.put("patients and methods","Methods");
		sectionMap.put("summary","Abstract");
		sectionMap.put("title","Title");
		sectionMap.put("background","Introduction");
		sectionMap.put("", "Introduction");
//		     82
		sectionMap.put("research design and methods","Methods");
		sectionMap.put("design and methods","Methods");
		sectionMap.put("subjects and methods","Methods");
		sectionMap.put("materials and methods","Methods");
		sectionMap.put("conclusions","Discussion");
		sectionMap.put("supplementary material","Other");
		sectionMap.put("competing interests","Other");
		sectionMap.put("authors' contributions","Other");
		sectionMap.put("authors’ contributions","Other");
		sectionMap.put("study limitations","Discussion");
		sectionMap.put("list of abbreviations","Other");
		sectionMap.put("abbreviations","Other");
		
		sectionGrouping.put("Title", Arrays.asList("1a"));
		sectionGrouping.put("Abstract", Arrays.asList("1b"));
		sectionGrouping.put("Introduction", Arrays.asList("2a","2b"));
		sectionGrouping.put("Methods", Arrays.asList("3a","3b","4a","4b","5","6a","6b","7a","7b","8a","8b","9","10","11a","11b","12a","12b"));
		sectionGrouping.put("Results", Arrays.asList("13a","13b","14a","14b","15","16","17a","17b","18","19"));
		sectionGrouping.put("Discussion",Arrays.asList("20","21","22"));
		sectionGrouping.put("Other",Arrays.asList("23","24","25"));
	}
	
	public static double calcJ(Set<String> s1, Set<String> s2) {
		int n1 = s1.size();
		int n2 = s2.size();
		Set<String> ta = new HashSet<>(s1);
		Set<String> tb = new HashSet<>(s2);
		ta.retainAll(tb);
		int intersection = ta.size();
		return intersection*1.0/(n1 + n2 - intersection);
	}
	
	public static double calcM(Set<String> s1, Set<String> s2) {
		int n1 = s1.size();
		int n2 = s2.size();
		Set<String> s1_copy = new HashSet<String>();
		Set<String> s1_copy2 = new HashSet<String>();
		s1_copy.addAll(s1);
		s1_copy2.addAll(s1);
		s1.retainAll(s2);
		int intersection = s1.size();
		int union = n1 + n2 - intersection;
		s1_copy.removeAll(s2);
		int diff1 = s1_copy.size();
		s2.removeAll(s1_copy2);
		int diff2 = s2.size();
		if(intersection == union)
			return 1;
		if(diff1 == 0 || diff2 == 0)
			return 2/3;
		if(intersection > 0)
			return 1/3;
		return 0;
		
	}
	
	public static double calcM2(Set<String> s1, Set<String> s2) {
//		if (s1.size() > s2.size())
//			System.out.println("HERE.");
		if (same(s1,s2)) 
			return 1;
		if (subset(s1,s2) && s1.size() > 0 && s2.size() > 0) 
			return 2/3;
		if (intersect(s1,s2)) 
			return 1/3;
		return 0;
	}
	
	private static boolean same(Set<String> a, Set<String> b) {
/*		Set<String> ta = new HashSet<>(a);
		Set<String> tb = new HashSet<>(b);
		ta.retainAll(tb);
		return (ta.size() == tb.size());*/
		return a.containsAll(b) && b.containsAll(a);
	}
	
	private static boolean subset(Set<String> a, Set<String> b) {
		return a.containsAll(b) || b.containsAll(a);
	}
	
	private static boolean intersect(Set<String> a, Set<String> b) {
		Set<String> ta = new HashSet<>(a);
		Set<String> tb = new HashSet<>(b);
		ta.retainAll(tb);
		return (ta.size() >= 1);
	}
	
	public static double calcMASI(ArrayList<String> c1, ArrayList<String> c2) {
		double v = 0;
		Set<String> s1,s2;
		for(int i = 0; i < c1.size(); i++) {
			s1 = new HashSet<String>(Arrays.asList(c1.get(i).split(",")));
			s2 = new HashSet<String>(Arrays.asList(c2.get(i).split(",")));
//			System.out.println(c1.get(i) + "|" + s1 + "|" + c2.get(i) + "|" + s2);
			v += calcJ(s1,s2) * calcM2(s1,s2);
		}
		return v/c1.size();
	}
	
	public static double calcKappa(ArrayList<String> c1, ArrayList<String> c2) {
		double v = 0;
		Set<String> s1,s2;
		Map<String,Integer> aCnts = new HashMap<>();
		Map<String,Integer> bCnts = new HashMap<>();
		Map<String,Integer> agree = new HashMap<>();
		Set<String> seenAll = new HashSet<>();
		for(int i = 0; i < c1.size(); i++) {
			s1 = new HashSet<String>(Arrays.asList(c1.get(i).split(",")));
			s2 = new HashSet<String>(Arrays.asList(c2.get(i).split(",")));
			Set<String> seen = new HashSet<>();
			for (String s: s1) {
				int j =0;
				if (aCnts.containsKey(s)) j = aCnts.get(s);
				int k = 0; 
				if (bCnts.containsKey(s)) k = bCnts.get(s);
				int l = 0;
				if (agree.containsKey(s)) l = agree.get(s);
				if (s2.contains(s)) { 
					agree.put(s, ++l);
					aCnts.put(s, ++j); bCnts.put(s, ++k);
				} else {
					aCnts.put(s, ++j);
				}
				seen.add(s);
				seenAll.addAll(seen);
			}
			for (String s: s2) {
				int k = 0;
				if (seen.contains(s) == false) {
					if (bCnts.containsKey(s)) k = bCnts.get(s);
					bCnts.put(s, ++k);
				}
				seenAll.add(s);
			}
		}

		double chanceSum = 0;
		int agr = 0;
		for (String s: seenAll) {
			int acnt = (aCnts.containsKey(s) ? aCnts.get(s) : 0);
			int bcnt = (bCnts.containsKey(s) ? bCnts.get(s) : 0);
			agr += (agree.containsKey(s) ? agree.get(s) : 0);
			double chance = (double) (acnt * bcnt) / (double)c1.size();
			chanceSum += chance;
		}
		return (agr - chanceSum) / (double) (c1.size() - chanceSum);
	}
	
	public static Map<String,Double> calcPerItemMASI(ArrayList<String> c1, ArrayList<String> c2) {
		Map<String,Double> perItemMASI = new HashMap<>();
		for (String s: itemIds) {
			ArrayList<String> a1 = new ArrayList<String>();
			ArrayList<String> a2 = new ArrayList<String>();
			for (String c: c1) {
				if (c.equals(s)) a1.add(s);
				else a1.add("0");
			}
			for (String c: c2) {
				if (c.equals(s)) a2.add(s);
				else a2.add("0");
			}
			double masi = calcMASI(a1,a2);
			perItemMASI.put(s, masi);
		}
		return perItemMASI;
	}
	
	public static Map<String,Double> calcPerItemMASI2(ArrayList<String> c1, ArrayList<String> c2) {
		Map<String,Double> perItemMASI = new HashMap<>();
		for (String s: itemIds) {
			ArrayList<String> a1 = new ArrayList<String>();
			ArrayList<String> a2 = new ArrayList<String>();
			Set<Integer> id1 = getSentenceIdsWithItem(c1,s);
			Set<Integer> id2 = getSentenceIdsWithItem(c2,s);
			id1.addAll(id2);
			
			for (int i= 0; i < c1.size(); i++) {
				if (id1.contains(i)) a1.add(c1.get(i));
			}
			for (int i= 0; i < c2.size(); i++) {
				if (id1.contains(i)) a2.add(c2.get(i));
			}
			double masi = calcMASI(a1,a2);
			perItemMASI.put(s, masi);
		}
		return perItemMASI;
	}
	
	private static Set<Integer> getSentenceIdsWithItem(ArrayList<String> a, String id) {
		Set<Integer> ids = new HashSet<>();
		for (int i= 0; i < a.size(); i++) {
			Set<String> s1 = new HashSet<String>(Arrays.asList(a.get(i).split(",")));
			if (s1.contains(id)) ids.add(i);
		}
		return ids;
	}
	
	public static Map<String,Double> calcPerItemKappa(ArrayList<String> c1, ArrayList<String> c2) {
		Map<String,Double> perItemKappa = new HashMap<>();
		for (String s: itemIds) {
			ArrayList<String> a1 = new ArrayList<String>();
			ArrayList<String> a2 = new ArrayList<String>();
			for (String c: c1) {
				Set<String> s1 = new HashSet<String>(Arrays.asList(c.split(",")));
				if (s1.contains(s)) a1.add(s);
				else a1.add("0");
			}
			for (String c: c2) {
				Set<String> s1 = new HashSet<String>(Arrays.asList(c.split(",")));
				if (s1.contains(s)) a2.add(s);
				else a2.add("0");
			}
			double kappa = calcKappa(a1,a2);
			perItemKappa.put(s, kappa);
		}
		return perItemKappa;
	}
	
	public static void addToKrippendorffList(int aind, List<String> a, int bind,  List<String> b) {
		for (int i=0; i < a.size(); i++) {
			List<String> arr = new ArrayList<>();
			for (int j =0; j < annotators.size(); j++) {
				if (j== aind) arr.add(a.get(i));
				else if (j==bind) arr.add(b.get(i));
				else arr.add("");
			}
			krippendorffList.add(arr);
		}
	}
	
	public static double calcKrippendorff(String itemId) {
		List<List<String>> rating = new ArrayList<>();
		for (List<String> anns : krippendorffList) {
			List<String> out = new ArrayList<>();
			for (String ann: anns) {
				Set<String> s1 = new HashSet<String>(Arrays.asList(ann.split(",")));
				if (s1.contains(itemId)) out.add(itemId);
				else if (ann.equals("")) out.add("");
				else out.add("0");
			}
			rating.add(out);
		}
		List<List<Integer>> agreement = new ArrayList<>();
		
		for (List<String> r: rating) {
			int in = 0;
			for (String a: r)  {
				if (a.equals(itemId)) in++;
			}
			agreement.add(Arrays.asList(in, 2-in));
		}
		int r1 = 2;
		int n = agreement.size();
		int sumsq = 0;
		int iCnt = 0;
		int oCnt = 0;
		for (List<Integer> ag: agreement) {
			iCnt += ag.get(0);
			oCnt += 2- ag.get(0);
			for (int a: ag) {
				sumsq += a*a;
			}
		}
		double pa = (double) (sumsq - (n*r1)) / (double) (n*r1*(r1-1));
		double qi = (double) iCnt/(double) (n*r1);
		double qo =  (double) oCnt / (double)(n*r1);
		double pe = qi * qi  + qo * qo;
		return (double) (pa-pe) / (double) (1-pe);
	}
	
	public static Set<String> exclude(Set<String> elements) {
		if (excludeItems == null && excludeItems.size() == 0) {
			return elements;
		}
		Set<String> out = new HashSet<>();
		for (String s: elements) {
			if (excludeItems.contains(s)) continue;
			out.add(s);
		}
		if (out.size() == 0) out.add("0");
		return out;
	}

	private static String getSection(String item) {
		for (String s: sectionGrouping.keySet()) {
			if (sectionGrouping.get(s).contains(item)) return s;
		}
		return null;
	}
	public static void main(String[] args) throws IOException {
		String labelFile = args[0];
		
		boolean coarse = false;
		boolean excludeItems = true;
		boolean excludeSections = true;
		boolean excludeSectionHeaders =true;
		
		List<String> lines = FileUtils.linesFromFile(labelFile, "UTF-8");
		
		
		for (String l: lines) {
			if (l.contains("|") == false) continue;
			String[] els = l.split("\\|");
			if (excludeSections && excludedSections.contains(els[3].toLowerCase())) continue;
			String[] labArr = els[5].split(",");
			if (excludeSectionHeaders && els[3].equals(els[4])) continue;
			if (coarse) {
				// makes most sense to consider the following only:
				// 3a-3b, 4a-4b, 6a-6b, 7a-7b, 8a-8b (or 8a-8b-9,10), 11a-11b, 12a-12b, 13a-13b, 14a-14b, 17a-17b
				// A more strict case could 8a-8b (or 8a-8b,9,10), 12a-12b, 17a-17b
				for (int i=0; i < labArr.length; i++) {
					if (labArr[i].contains("a"))  labArr[i] =  labArr[i].substring(0,labArr[i].indexOf("a"));
					if (labArr[i].contains("b"))  labArr[i] = labArr[i].substring(0,labArr[i].indexOf("b"));
				}
			} 
			Set<String> labels = new HashSet<>();
			Collections.addAll(labels, labArr);
//			if (excludes != null && excludes.size() > 0)  labels.removeAll(excludes);
			if (excludeItems) labels = exclude(labels);
			Labeling lab = new Labeling(els[0],els[2],els[1],els[3],labels);
			if (annotatorPairA.containsKey(els[0]) == false) {
				annotatorPairA.put(els[0], els[1]);
			} else if (annotatorPairB.containsKey(els[0]) == false && annotatorPairA.get(els[0]).equals(els[1]) == false) {
				annotatorPairB.put(els[0], els[1]);
			}
			if (annotatorPairA.get(els[0]).equals(els[1])) {
				Map<String,Labeling> sentLabels = annALabels.get(els[0]);
				if (sentLabels == null) {
					sentLabels = new HashMap<>();
				}
				sentLabels.put(els[2],lab);
				annALabels.put(els[0],sentLabels);
			} else if (annotatorPairB.get(els[0]).equals(els[1])) {
				Map<String,Labeling> sentLabels = annBLabels.get(els[0]);
				if (sentLabels == null) {
					sentLabels = new HashMap<>();
				}
				sentLabels.put(els[2],lab);
				annBLabels.put(els[0],sentLabels);
			}
		}
		
		ArrayList<String> allA = new ArrayList<>();
		ArrayList<String> allB = new ArrayList<>();
		
		double masiAvg = 0.0;
		List<String> docids = new ArrayList<>(annotatorPairA.keySet());
		Collections.sort(docids);
		int i = 0;
		for (String k: docids) {
//			if (k.equals("PMC4797126") == false) continue;
			String annA = annotatorPairA.get(k);
			String annB = annotatorPairB.get(k);
			if (annA == null || annB == null) {
//				System.out.println(k + "|" + annA  + "|" + annB + "|" + "NOT DOUBLE ANNOTATED");
				continue;
			}
			Map<String,ArrayList<String>> sectionLabA = new HashMap<>();
			Map<String,ArrayList<String>> sectionLabB = new HashMap<>();
			
			ArrayList<String> labsA = new ArrayList<>();
			ArrayList<String> labsB = new ArrayList<>();
			
			Map<String,Labeling> a = annALabels.get(k);
			Map<String,Labeling> b = annBLabels.get(k);
			
			for (String s: a.keySet()) {
				String laba = String.join(",",a.get(s).getLabels());
				String labb = String.join(",",b.get(s).getLabels());
				labsA.add(laba);
				labsB.add(labb);
				
				String sname = a.get(s).getSection();
				String norm = sectionMap.get(sname.toLowerCase());
				if (norm == null) 
					System.out.println(sname + " NULL");
				ArrayList<String> exa = sectionLabA.get(norm);
				if (exa == null) exa = new ArrayList<>();
				exa.add(laba);
				sectionLabA.put(norm,exa);
				ArrayList<String> exb = sectionLabB.get(norm);
				if (exb == null) exb = new ArrayList<>();
				exb.add(labb);
				sectionLabB.put(norm,exb);
				
//				System.out.println(k +"|" + s + "|" +laba+ "|" + labb);
			}
			double masi = calcMASI(labsA,labsB);
			addToKrippendorffList(annotators.indexOf(annA),labsA,annotators.indexOf(annB),labsB);
			System.out.println(k + "|" + ++i + "|" + annA + "|"+ annB + "|" + "DocMASI" + "|" +  masi);
			for (String s: sectionLabA.keySet()) {
				ArrayList<String> seca = sectionLabA.get(s);
				ArrayList<String> secb = sectionLabB.get(s);
				double sectionMasi = calcMASI(seca,secb);
				System.out.println(k + "|" + i + "|" + annA + "|"+ annB + "|" + "SecMASI" + "|" +  s + "|" + sectionMasi);
			}
			Map<String,Double> perItemMasi = calcPerItemMASI2(labsA,labsB);
			Map<String,List<Double>> perGroupMasi = new HashMap<>();
			for (String p: perItemMasi.keySet()) {
				if (Double.isNaN(perItemMasi.get(p))) continue;
				System.out.println(k + "|" + i + "|" + annA + "|"+ annB + "|" + "ItemMASI" + "|" +  p + "|" +perItemMasi.get(p));
				String group = getSection(p);
			
				List<Double> groupMasis = new ArrayList<>();
				if (perGroupMasi.containsKey(group)) groupMasis = perGroupMasi.get(group);
				if ( Double.isNaN(perItemMasi.get(p))) continue;
				groupMasis.add(perItemMasi.get(p));
				perGroupMasi.put(group, groupMasis);
			}
			
			for (String g: perGroupMasi.keySet()) {
				List<Double> masis = perGroupMasi.get(g);
				double total = 0.0;
				for (Double d: masis) total += d;
				double grpMasi = total / (double) masis.size();
				System.out.println(k + "|" + i + "|" + annA + "|"+ annB + "|" + "GrpMASI" + "|" +  g + "|" + grpMasi);
			}
			
			allA.addAll(labsA);
			allB.addAll(labsB);
			masiAvg += masi;
			Map<String,Double> perItem = calcPerItemKappa(labsA,labsB);
			for (String p: perItem.keySet()) {
				if (Double.isNaN(perItem.get(p))) continue;
				System.out.println(k + "|" + i + "|" + annA + "|"+ annB + "|"  + "ItemKAPPA" + "|" + p + "|" + perItem.get(p));
			}

		}
		
		Map<String,Double> perItem = calcPerItemKappa(allA,allB);
		for (String p: perItem.keySet()) {
			System.out.println("Per Item Kappa " + p + ": " + perItem.get(p));
		}
		
		double allMasi = calcMASI(allA,allB);
		System.out.println("Micro-Masi: " + allMasi);
		masiAvg = (double) masiAvg / i;
		System.out.println("Macro-Masi: " + masiAvg);
		
		for (String id: itemIds) {				
			double krip = calcKrippendorff(id);
			System.out.println(id + "\t" + krip);
		}

	}

}
