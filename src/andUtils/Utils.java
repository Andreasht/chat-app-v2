package andUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public final class Utils {
	private Utils() {}

	public static boolean isEven(int i) {
		return i%2 == 0;
	}

	public static void cls() throws InterruptedException, IOException {
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
	}

	public static ArrayList<Integer> toIntArray(ArrayList<String> in) {
		ArrayList<Integer> out = new ArrayList<>();
		try {
			in.forEach(String -> out.add(Integer.parseInt(String)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	private static HashMap<Integer, Integer> sortByValue(Map<Integer, Integer> unsortedMap, final boolean order) {
		//Ascending: input true
		//Descending: input false
		List<Entry<Integer, Integer>> list = new LinkedList<>(unsortedMap.entrySet());
		list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
				? o1.getKey().compareTo(o2.getKey())
						: o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
						? o2.getKey().compareTo(o1.getKey())
								: o2.getValue().compareTo(o1.getValue()));
		return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
	}

	public static HashMap<Character, Integer> sortCharMapByValue(HashMap<Character, Integer> unsortedMap, final boolean order) {

		//Ascending: input true
		//Descending: input false
		List<Entry<Character, Integer>> list = new LinkedList<>(unsortedMap.entrySet());
		list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
				? o1.getKey().compareTo(o2.getKey())
				: o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
				? o2.getKey().compareTo(o1.getKey())
				: o2.getValue().compareTo(o1.getValue()));
		return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
	}
	
	private static HashMap<Integer,Integer> getIntFrequencies(ArrayList<Integer> list) {
		HashMap<Integer,Integer> map = new HashMap<>();
		list.forEach(Integer -> map.put(Integer, map.getOrDefault(Integer, 0)+1));
		return map;
	}
	
	public static ArrayList<Entry<Integer,Integer>> getMostFrequentInt(ArrayList<Integer> list) {
		HashMap<Integer,Integer> map = getIntFrequencies(list);
		HashMap<Integer,Integer> sortedMap = sortByValue(map, false);
		List<Entry<Integer,Integer>> l = new LinkedList<>(sortedMap.entrySet());
		ArrayList<Entry<Integer,Integer>> outList = new ArrayList<>();
		for(Entry<Integer, Integer> entry : l) {
			if(Objects.equals(l.get(0).getValue(), entry.getValue())) {
				outList.add(entry);
			}
		}
		return outList;
	}

	public static void rmChar(ArrayList<Character> list, char c) {
		list.removeAll(Collections.singleton(Character.toLowerCase(c)));
		list.removeAll(Collections.singleton(Character.toUpperCase(c)));
	}

	private static void setUIFont(String fontName, int style, int size){
		javax.swing.plaf.FontUIResource f = new javax.swing.plaf.FontUIResource(fontName, style, size);
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put (key, f);
		}
	}

	public static void makeGUILookNice(String fontName, int style, int size) {
		try {
			setUIFont(fontName, style, size);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
