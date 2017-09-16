
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Timer;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Poet {
	char[] punctuation = {'.', ',', '!'};
	AlanPanel Alan;
	static ArrayList<String> dicts = new ArrayList<String>();

	public static void main(String[] args) throws IOException{
		dicts.add("Twi Dictionary.txt");
		dicts.add("Frost Dictionary.txt");
		dicts.add("Moby Dicktionary.txt");
		dicts.add("Shakespeare Dictionary.txt");
		dicts.add("50shades Dictionary.txt");
		Poet Alan = new Poet();
		String filename = "TWILIGHT.txt";
		String prevfilename = "";
		String prevdictfilename = "";
		String printDictToFileName = "";
		HashMap<String, ArrayList<NextWord>> dict = null;
		Alan.buildGUI();
		
		
		//System.out.println("For file: " + filename);
		//System.out.println("Total number of tokens in text: " + numWords);
		//System.out.println("Number of unique trigrams: " + trigramDict.keySet().size());
		//System.out.println("Number of unique bigrams: " + bigramDict.keySet().size());
		//System.out.println("Number of unique singletons: " + singletonDict.keySet().size());
		/*
		System.out.println("Triplets Dict: " + trigramDict);
		System.out.println("Bigrams Dict: " + bigramDict);
		System.out.println("Singletons Dict" + singletonDict);
		*/
		
		Scanner y = new Scanner(System.in);
		boolean hasExited = false;
		while(!hasExited) {
			System.out.println("Would you like to: (1) Read a file to create a dictionary? (2) Create a piece from an existing file? (3) Combine two existing dictionaries?");
			String next = y.nextLine();
			if(Integer.parseInt(next) == 1){
				System.out.println("What file would you like to read from?");
				filename = y.nextLine();
				//read file
				if(!filename.equals(prevfilename)){
					prevfilename = filename;
					String text = Alan.readFile(filename);
					//create dictionary
					long startTime = System.currentTimeMillis();
					dict = Alan.concatDictionaries(text);
					long endTime = System.currentTimeMillis();
					System.out.println("Time to create dictionaries from original text: " + (endTime - startTime) + " milliseconds");
					printDictToFileName = filename + " Dictionary.txt";
					Alan.printHashMapTo(printDictToFileName, dict);
					System.out.println("Dictionary created at " + printDictToFileName);
					dicts.add(printDictToFileName);
				}
				
			}
			else if(Integer.parseInt(next) == 2){
				System.out.println("What dictionary would you like to use?");
				System.out.println("Options: ");
				for(int i = 0; i < dicts.size(); i++){
					System.out.println((i + 1) + ". " + dicts.get(i));
				}
				String dictfilename = dicts.get(Integer.parseInt(y.nextLine()) - 1);
				if(!dictfilename.equals(prevdictfilename)){
					long startTime = System.currentTimeMillis();
					dict = Alan.dictFromProcessedFile(dictfilename);
					long endTime = System.currentTimeMillis();
					System.out.println("Time to read dictionaries from processed text: " + (endTime - startTime) + "milliseconds");
				}
				prevdictfilename = dictfilename;
			}
			else if(Integer.parseInt(next) == 3){
				System.out.println("What dictionaries would you like to use? Please separate numbers with commas, no spaces.");
				System.out.println("Options: ");
				for(int i = 0; i < dicts.size(); i++){
					System.out.println((i + 1) + ". " + dicts.get(i));
				}
				String dictionaries = y.nextLine();
				String[] dictionaryarr = dictionaries.split(",");
				dict = new HashMap<String, ArrayList<NextWord>>();
				printDictToFileName = "";
				for(int j = 0; j < dictionaryarr.length; j++){
					String dictfilename = dicts.get(Integer.parseInt(dictionaryarr[j]) - 1);
					dict.putAll(Alan.dictFromProcessedFile(dictfilename));
					printDictToFileName += dictfilename + ", ";
				}
				
				printDictToFileName = printDictToFileName.substring(0, printDictToFileName.length() - 2) + " Dictionary.txt";
				Alan.printHashMapTo(printDictToFileName, dict);
				System.out.println("Dictionary created at " + printDictToFileName);
				dicts.add(printDictToFileName);
				
			}
			
			System.out.println("About how many words would you like your poem to have?");
			int numWordsInPoem = 100;
			String numWordsInput = y.nextLine();
			System.out.println("numWordsInput read as: " + numWordsInput);
			try {
				numWordsInPoem = Integer.parseInt(numWordsInput);
			}
			catch (NumberFormatException e) {
				System.out.println("That was not a number! Defaulting to 100 words.");
			}
			
			
			System.out.println("Enter three+ starting words.");
			String first = y.nextLine();
			String s = Alan.writersWorkshop(first, dict, numWordsInPoem);
			
			System.out.println();
			System.out.println(s);
			System.out.println("Type EXIT to exit. Type anything else to go again!");
			if(y.nextLine().equals("EXIT")) {
				hasExited = true;
			}
			
		}
		
		System.out.println("Thanks for writing with us! Bye bye.");
		y.close();
	}
	
	private String readFile(String filename) throws IOException{
		Scanner s = new Scanner(new File(filename));
		String text = "";
		int numWords = 0;
		while(s.hasNext()) {
			text += s.next() + " ";
			numWords++;
		}
		s.close();
		
		return text;
	}
	
	private HashMap<String, ArrayList<NextWord>> concatDictionaries(String text){
		HashMap<String, ArrayList<NextWord>> trigramDict = generateStateMapForNGrams(text, 3);
		HashMap<String, ArrayList<NextWord>> bigramDict = generateStateMapForNGrams(text, 2);
		HashMap<String, ArrayList<NextWord>> singletonDict = generateStateMapForNGrams(text, 1);
		HashMap<String, ArrayList<NextWord>> dict = new HashMap<String, ArrayList<NextWord>>();
		dict.putAll(trigramDict);
		dict.putAll(bigramDict);
		dict.putAll(singletonDict);
		return dict;
	}
	
	//The ArrayList<NextWord> stores a sorted list of ranges for those words
	private HashMap<String, ArrayList<NextWord>> generateStateMapForNGrams(String toRead, int n) {
		
		HashMap<String, HashMap<String, Integer>> countDict = new HashMap<String, HashMap<String, Integer>>();
		String curState = "";
		Scanner s = new Scanner(toRead);
		for(int i=0; i<n; i++) {
			if(s.hasNext()) {
				curState += " " + s.next();
			}
		}
		curState = curState.substring(1);
		
		while(s.hasNext()) {
			//Check if countDict containsKey
			if(!countDict.containsKey(curState)) {
				countDict.put(curState, new HashMap<String, Integer>());
			}
			String next = s.next();
			addStringToCountDict(next, countDict.get(curState));
			curState = pushNextWord(curState, next);
		}
		
		HashMap<String, ArrayList<NextWord>> rangeDict = new HashMap<String, ArrayList<NextWord>>();
		for(String state: countDict.keySet()) {
			rangeDict.put(state, getProbabilityList(countDict.get(state)));
		}
		s.close();
		return rangeDict;
	}

	private ArrayList<NextWord> getProbabilityList(HashMap<String, Integer> dict) {
		double totalCount = 0;
		for(String str: dict.keySet()) {
			totalCount += dict.get(str);
		}
		
		if(totalCount==0) {
			System.out.println("Tried to call getProbabilityList on an empty dictionary.");
			return null;
		}
		
		
		double curRange = 0;
		ArrayList<NextWord> probabilityList = new ArrayList<NextWord>();
		for(String str: dict.keySet()) {
			curRange += (double)(dict.get(str))/totalCount;
			probabilityList.add(new NextWord(str, curRange));
		}
		Collections.sort(probabilityList);;
		return probabilityList;
	}
	
	private void addStringToCountDict(String str, HashMap<String, Integer> dict) {
		if(dict.containsKey(str)) {
			dict.put(str, dict.get(str) + 1);
		}
		else {
			dict.put(str, 1);
		}
	}
	
	private String pushNextWord(String curState, String next) {
		String pushed = "";
		String[] words = curState.split(" ");
		for(int i=1; i<words.length; i++) {
			pushed += words[i] + " ";
		}
		return pushed + next;
	}

	//MARK: Writing Methods	

	public String writersWorkshop(String firstWords, HashMap<String, ArrayList<NextWord>> dict, int poemLength){
		ArrayList<String> poem = new ArrayList<String>();
		Random r = new Random();
		//currentState = last 3 words of poem
		String currentState = firstWords;
		Scanner s = new Scanner(firstWords);
		boolean indialogue = false;
		
		while(s.hasNext()){
			poem.add(s.next());
		}
		
		String newestWord = null;
		
		for(int i = 0; i < poemLength; i++){
			
			//if the 3-word complete state has at least 3 options following it
			if(dict.containsKey(currentState) && dict.get(currentState).size() > 3){
				ArrayList<NextWord> words = dict.get(currentState);
				boolean stillLooking = true;
				double rand = r.nextDouble();
				for(int j = 0; j < words.size() && stillLooking; j++){
					if(rand <= words.get(j).freq){
						stillLooking = false;
						poem.add(words.get(j).word);
						currentState = currentState.split(" ")[1] + " " + currentState.split(" ")[2] + " " + words.get(j).word;
						newestWord = words.get(j).word;
					}
				}
			}
			
			String twoWordState = currentState.split(" ")[1] + " " + currentState.split(" ")[2];
			String lastWord = currentState.split(" ")[2];
			
			//if the 2-word state has at least 3 options following it
			if(dict.containsKey(twoWordState) && dict.get(twoWordState).size() > 2){
				ArrayList<NextWord> words = dict.get(twoWordState);
				boolean stillLooking = true;
				double rand = r.nextDouble();
				for(int j = 0; j < words.size() && stillLooking; j++){
					if(rand <= words.get(j).freq){
						stillLooking = false;
						poem.add(words.get(j).word);
						currentState = twoWordState + " " + words.get(j).word;
						newestWord = words.get(j).word;
					}
				}
			}
			
			
			//else just look for words following the previous word
			
			else{
				ArrayList<NextWord> words = dict.get(lastWord);
				if(words == null){
					poem.add("and");
					System.out.println("and");
					currentState = twoWordState + " and";
					newestWord = "and";
				}
				
				else{
					boolean stillLooking = true;
					double rand = r.nextDouble();
					for(int j = 0; j < words.size() && stillLooking; j++){
						if(rand <= words.get(j).freq){
							stillLooking = false;
							poem.add(words.get(j).word);
							currentState = twoWordState + " " + words.get(j).word;
							newestWord = words.get(j).word;
						}
					}
				}
			}
			
			
			boolean endsWithPunc = false;
			boolean endsWithQuote = false;
			
			for(int j = 0; j < punctuation.length; j++){
				if(currentState.charAt(currentState.length() - 1) == punctuation[j]){
					endsWithPunc = true;
				}
			}
			
			if(currentState.charAt(currentState.length() - 1) == '"'){
				endsWithQuote = true;
			}
			
			if(indialogue && endsWithPunc){
				currentState += "\"";
				newestWord += "\"";
				poem.set(poem.size() - 1, newestWord);
			}
			
			if(!indialogue && endsWithQuote){
				currentState = currentState.substring(0, currentState.length() - 1);
				newestWord = newestWord.substring(0, newestWord.length() - 1);
				poem.set(poem.size() - 1, newestWord);
			}
			
			if(newestWord.contains("\"")){
				if(indialogue){
					indialogue = false;
				}
				else{
					indialogue = true;
				}
			}
			
			
			
		}
		s.close();
		
		
		String poemComplete = "";
		
		for(int i = 0; i < poem.size(); i++){
			if(poem.get(i).charAt(poem.get(i).length() - 1) == '.'){
				poemComplete += poem.get(i) + "\n";
			}
			else{
				poemComplete += poem.get(i) + " ";
			}
		}
		
		return poemComplete;
	}
	
	//MARK: Printing HashMaps Methods
	private void printHashMapTo(String outputFileName, HashMap<String, ArrayList<NextWord>> dict) throws IOException {

		//Format of file:
		//First, the number of entries in the dict
		//Then for each entry:
		//word #ofFollowers
		//# follower
		//# follower
		// . . .
		//# follower
		//word #ofFollowers
		
		PrintWriter out = new PrintWriter(new File(outputFileName));
		out.println(dict.keySet().size());
		for(String str: dict.keySet()) {
			ArrayList<NextWord> followers = dict.get(str);
			out.println(str + " " + followers.size());
			for(NextWord follower: followers) {
				out.println(follower.freq + " " + follower.word);
			}
		}
		out.close();
	}
	
	private HashMap<String, ArrayList<NextWord>> dictFromProcessedFile(String inputFileName) throws IOException {
		HashMap<String, ArrayList<NextWord>> dict = new HashMap<String, ArrayList<NextWord>>();
		Scanner s = new Scanner(new File(inputFileName));
		int numEntries = Integer.parseInt(s.nextLine());
		
		
		for(int i=0; i<numEntries; i++) {
			//Find what the key is
			String key = "";
			String[] keyAndNum = s.nextLine().split(" ");
			for(int q=0; q<keyAndNum.length-1; q++) {
				key += " " + keyAndNum[q];
			}
			key = key.substring(1);
			int numFollowers = Integer.parseInt(keyAndNum[keyAndNum.length - 1]);

			ArrayList<NextWord> followers = new ArrayList<NextWord>(numFollowers);
			for(int j=0; j<numFollowers; j++) {
				double freqRange = Double.parseDouble(s.next());
				String followingWord = s.next();
				followers.add(new NextWord(followingWord, freqRange));
				s.nextLine();
			}
			dict.put(key, followers);
		}		
		s.close();
		return dict;
	}

	private void buildGUI() throws IOException{
		JFrame window = new JFrame("Alan Faulkner");
		Alan = new AlanPanel();
		
		window.add(Alan);
		window.setSize(256, 700);
		Alan.setBackground(Color.CYAN);
		Alan.setPreferredSize(new Dimension(300, 700));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		window.setResizable(false);
		window.setVisible(true);
	}

	class AlanPanel extends JPanel{
		BufferedImage alan;
		BufferedImage alan2;
		BufferedImage alan1mid;
		BufferedImage alan2mid;
		BufferedImage currentAlan;
		String currentDialogue;
		String onscreenDialogue;
		String onscreenDialogue2;
		String onscreenDialogue3;
		String onscreenDialogue4;
		Font f = new Font("Cambria", 20, 20);
		
		class UpdateGraphics extends TimerTask{

			@Override
			public void run() {
				if(currentAlan.equals(alan)){
					currentAlan = alan1mid;
				}
				else if(currentAlan.equals(alan1mid)){
					currentAlan = alan2;
				}
				else if(currentAlan.equals(alan2)){
					currentAlan = alan2mid;
				}
				else{
					currentAlan = alan;
				}
				
				//stack helped so much here
				FontMetrics fm = AlanPanel.this.getFontMetrics(f);
				
				if(currentDialogue.length() > 0){
					if(fm.stringWidth(onscreenDialogue) > 180){
						if(fm.stringWidth(onscreenDialogue2) > 180){
							if(fm.stringWidth(onscreenDialogue3) > 185){
								onscreenDialogue4 += currentDialogue.charAt(0);
								currentDialogue = currentDialogue.substring(1);
							}
							else{
								onscreenDialogue3 += currentDialogue.charAt(0);
								currentDialogue = currentDialogue.substring(1);
							}
						}
						else{
							onscreenDialogue2 += currentDialogue.charAt(0);
							currentDialogue = currentDialogue.substring(1);
						}
					}
					else{
						onscreenDialogue += currentDialogue.charAt(0);
						currentDialogue = currentDialogue.substring(1);
					}
				}
				
				
				
				
				repaint();
			}
			
		}
		
		public AlanPanel() throws IOException{
			super();
			alan = ImageIO.read(new File("Alan.png"));
			alan2 = ImageIO.read(new File("Alan2.png"));
			alan1mid = ImageIO.read(new File("Alan.png"));
			alan2mid = ImageIO.read(new File("Alan2.png"));
			currentAlan = alan;
			currentDialogue = "Welcome to Writer's Workshop! My name is Alan. What can I help you create today?";
			onscreenDialogue = "";
			onscreenDialogue2 = "";
			onscreenDialogue3 = "";
			onscreenDialogue4 = "";
			Timer timer = new Timer();
			timer.schedule(new UpdateGraphics(), 0, 200);       
	        this.setFont(f);
		}
		
		public void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        g.drawImage(currentAlan, 0, 0, null);
	        g.setColor(Color.WHITE);
	        g.drawString(onscreenDialogue, 30, 70);
	        g.drawString(onscreenDialogue2, 30, 100);
	        g.drawString(onscreenDialogue3, 30, 130);
	        g.drawString(onscreenDialogue4, 30, 160);
	    } 
		
	}
	
	class NextWord implements Comparable<NextWord> {
		String word;
		double freq;
		
		NextWord(String wordIn, double freqIn) {
			word = wordIn;
			freq = freqIn;
		}

		@Override
		public int compareTo(NextWord o) {
			if(freq > o.freq) {
				return 1;
			}
			if(freq == o.freq) {
				return 0;
			}
			return -1;
		}
		
		public String toString() {
			return freq + ":" + word;
		}	

	}
}


