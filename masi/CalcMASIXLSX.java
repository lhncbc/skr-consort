import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CalcMASIXLSX {
	
	public static double calcJ(Set<String> s1, Set<String> s2) {
		int n1 = s1.size();
		int n2 = s2.size();
		s1.retainAll(s2);
		int intersection = s1.size();
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
	
	public static double calcMASI(ArrayList<String> c1, ArrayList<String> c2) {
		double v = 0;
		Set<String> s1,s2;
		for(int i = 0; i < c1.size(); i++) {
			s1 = new HashSet<String>(Arrays.asList(c1.get(i).split(",")));
			s2 = new HashSet<String>(Arrays.asList(c2.get(i).split(",")));
			v += calcJ(s1,s2) * calcM(s1,s2);
		}
		return v/c1.size();
	}

	public static void main(String[] args) throws IOException {
		
		File excelFile = new File("masi.xlsx");
		FileInputStream fis = new FileInputStream(excelFile);
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIt = sheet.iterator();
		
		ArrayList<String> c1 = new ArrayList<String>();
		ArrayList<String> c2 = new ArrayList<String>();
		
		while(rowIt.hasNext()) {
			Row row = rowIt.next();
			c1.add(row.getCell(0).toString());
			c2.add(row.getCell(1).toString());
			
//			Iterator<Cell> cellIterator = row.cellIterator();
//		
//			while (cellIterator.hasNext()) {
//				Cell cell = cellIterator.next();
//				
//				System.out.print(cell.toString() + ";");
//			}
//			System.out.println();		
		}
		
		System.out.println(calcMASI(c1, c2));
		workbook.close();
		fis.close();
	}

}
