package gov.nih.nlm.consort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.Section;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Span;
import gov.nih.nlm.ling.core.SurfaceElement;
import gov.nih.nlm.ling.core.SynDependency;
import gov.nih.nlm.ling.util.FileUtils;

public class Utils {
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
	
	public static Map<String,String> loadNormalizedSectionNames(String filename) throws IOException {
		final List<String> lines = FileUtils.linesFromFile(filename, "UTF8");
		Map<String,String> normMap = new HashMap<>();
		for (String l: lines) {
			String[] els = l.split("\\|");
			String text = els[0];
			String norm = els[1];
			normMap.put(text.toLowerCase(),norm);
		}
		return normMap;
	}
	
	
	public static Map<String,List<String>> loadSampleSentences(String filename) throws IOException {
		final List<String> lines = FileUtils.linesFromFile(filename, "UTF8");
		Map<String,List<String>> sampleSentenceMap = new HashMap<>();
		for (String l: lines) {
			String[] els = l.split("\\|");
			String id = els[0];
			String sent = els[2];
			List<String> existing = sampleSentenceMap.get(id);
			if (existing == null) existing = new ArrayList<>();
			existing.add(sent.trim());
			sampleSentenceMap.put(id,existing);
		}
		return sampleSentenceMap;
	}

	
	public static boolean containsSubstring(List<String> list, String str) {
		if (list == null) return false;
		for (String l: list) {
			if (str.contains(l)) return true;
			boolean multi = false;
			if (l.contains("|")) multi = true;
			if (multi) {
				String[] ss = l.split("\\|");
				for (int i=0; i < ss.length; i++) {
					if (str.contains(ss[i]) == false) return false;
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsString(List<String> list, String str) {
		if (list == null) return false;
		for (String l: list) {
			if (l.equals(str)) return true;
		}
		return false;
	}
	
	public static String getKeyFromValueString(Map<String,List<String>> list, String str) {
		if (list == null) return null;
		for (String s: list.keySet()) {
			List<String> values = list.get(s);
//			if (values.contains(str)) return s;
			if (containsString(values,str)) return s;
		}
		return null;
	}
	
	public static String getKeyFromValueSubstring(Map<String,List<String>> list, String str) {
		if (list == null) return null;
		for (String s: list.keySet()) {
			List<String> values = list.get(s);
			if (containsSubstring(values,str)) return s;
		}
		return null;
	}
	
	public static List<String> getKeysFromValueString(Map<String,List<String>> list, String str) {
		if (list == null) return new ArrayList<>();
		List<String> out = new ArrayList<>();
		for (String s: list.keySet()) {
			List<String> values = list.get(s);
//			if (values.contains(str)) out.add(s);
			if (containsString(values,str)) out.add(s);
		}
		return out;
	}
	
	public static List<String> getKeysFromValueSubstring(Map<String,List<String>> list, String str) {
		if (list == null) return new ArrayList<>();
		List<String> out = new ArrayList<>();
		for (String s: list.keySet()) {
			List<String> values = list.get(s);
			if (containsSubstring(values,str)) out.add(s);
		}
		return  out;
	}
	
	public static Section getSectionWithName(Section section, String sectionName) {
		if (section.getTitle() != null && section.getTitle().equalsIgnoreCase(sectionName)) return section;
		List<Section> subs = section.getSubSections();
		if (subs == null || subs.size() == 0) return null;
		for (Section sub: subs) {
			Section st = getSectionWithName(sub,sectionName);
			if (st != null) return st;
		}
		return null;
	}
	
	public static Section getSectionWithName(Document doc,String sectionName) {
		List<Section> sections = doc.getSections();
		for (Section s: sections) {
			Section t =  getSectionWithName(s,sectionName);
			if (t != null) return t;
		}
		return null;
	}
		

	
	public static List<Sentence> getSectionSentences(Document doc, String sectionName) {
		Section sect = getSectionWithName(doc,sectionName);
		List<Sentence> sectSentences = new ArrayList<>();
		if (sect != null) {
			Span sp  = sect.getTextSpan();
			sectSentences = doc.getAllSubsumingSentences(sp);
		}
		return sectSentences;
	}
	
	public static List<Sentence> getSectionTitleSentences(Document doc, String sectionName) {
		Section sect = getSectionWithName(doc,sectionName);
		List<Sentence> sectSentences = new ArrayList<>();
		if (sect != null) {
			Span sp  = sect.getTitleSpan();
			sectSentences = doc.getAllSubsumingSentences(sp);
		}
		return sectSentences;
	}
	
	public static Section getTopSection(Sentence sent) {
		Document doc = sent.getDocument();
		if (doc.getSections() == null) return null;
		for (Section s: doc.getSections()) {
			Span ss = s.getTextSpan();
			if (Span.subsume(ss, sent.getSpan()))  {
				return s;
			}
			Span tss = s.getTitleSpan();
			if (tss == null) continue;
			if (Span.subsume(tss, sent.getSpan()))  {
				return s;
			}
		}
		return null;
	}
	
	public static Section getParentSection(Section section) {
		Document doc = section.getDocument();
		Section parent = null;
		for (Section s: doc.getSections()) {
			if (s.equals(section)) return null;
			 parent = getParentSection(s,section);
			if (parent != null) break;
		}
		return parent;
	}
	
	public static Section getParentSection(Section ancestor,Section section) {
		List<Section> subs = ancestor.getSubSections();
		if (subs == null) return null;
		for (Section sub: subs) {
			if (sub.equals(section)) return ancestor;
			Section par =  getParentSection(sub,section);
			if (par != null) return par;
		}
		return null;
	}
	
	public static List<Section> getAncestorSections(Section section) {
		Section parent = getParentSection(section);	
		List<Section> ancestors = new ArrayList<>();
		while (parent != null) {
			ancestors.add(parent);
			parent = getParentSection(parent);
		}
		return ancestors;
	}
	
	public static  Section getSection(Document doc, Sentence sentence) {
		List<Section> sections = doc.getSections();
		if (sections == null) 			
			return null;
		for (Section sect: sections) {
			Section subsuming = getLowestSubsumingSection(sect,sentence.getSpan());
			if (subsuming != null) return subsuming;
		}
		return null;
	}
	
	private static Section getLowestSubsumingSection(Section section, Span sp) {
		if (section.getSubSections().size() == 0) {
			if ((section.getTitleSpan() != null && Span.subsume(section.getTitleSpan(), sp)) ||
				(section.getTextSpan() != null && Span.subsume(section.getTextSpan(), sp)))
				return section;
			return null;
		} else {
			for (Section sub : section.getSubSections()) {
				Section a = getLowestSubsumingSection(sub,sp);
				if (a != null) return a;
			}
//			return section;
		}
		// The section has subsections, but this span is in the main paragraphs of the section, and not the subsections
		if ((section.getTitleSpan() != null && Span.subsume(section.getTitleSpan(), sp)) ||
				(section.getTextSpan() != null && Span.subsume(section.getTextSpan(), sp)))
				return section;
		return null;
	}
	
}
