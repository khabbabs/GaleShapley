package cs361;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;


public class StableMatching {



	/**
	 * Implementation of the Stable Matching Algorithm aka Gale shapely Algorithm.
	 * This Implementation only accepts males has the proposers.
	 * it reads in a text file of the following format
	 * Alan Jess Nora Sara Betty...
	 * .
	 * .
	 * .
	 * .
	 * Jess Alan Ron ...
	 * where the names are separated by one space.
	 * males need to be listed 1st and then the females.
	 * 
	 * @param .txt
	 * @author khabbab Saleem 
	 * 
	 */
	private FileReader fReader;
	private BufferedReader buffReader;
	private HashMap<String,Male> maleHash;
	private HashMap<String,Female> femaleHash;
	private LinkedList<Male> singleMaleList;
	private TreeMap<String,String> finalPairs;
	//private LinkedList<Female> femaleList; 
	private int parsingIterator =0;
	public static void main(String[] args) {
		StableMatching GsAlgo = new StableMatching();
		try {
			GsAlgo.textParser(args[0]);
		} catch (IOException e) {
			System.out.println("testParser Didnt work");
			e.printStackTrace();
		}
	}
	/*
	 * Parses the file into lines, each line is then split into an array
	 * of words which is then passed to the assembleFMhashes Method.
	 */
	public void textParser(String filePath) throws IOException {
		String currRecord;
		maleHash = new HashMap<String,Male>();
		femaleHash = new HashMap<String,Female>();
		singleMaleList = new LinkedList<Male>();
		finalPairs = new TreeMap<String,String>();
		try {
			fReader = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		buffReader = new BufferedReader(fReader);
		long start,end;
		double finalTime;
		start = System.nanoTime();
		while ((currRecord = buffReader.readLine()) != null) {
			assembleFMHashes(currRecord.split(" "));
		}
		end = System.nanoTime();
		finalTime = (end - start)/ Math.pow(10, 9);
		System.out.println("Parsing Time : "+finalTime);
      //where the GaleShapley Algorithm is applied to the data structures.
		applyGs();
	  //where the printing is called, please comment it out for large data sets.
		printFinalPairs();
	}

/*
 * input is parsed and given in the form of a string array where
 * each entry in its own cell in the array, this distinction is
 * made because of the names being separated by spaces.
 * 
 * takes the input increments the parsingIterator which counts down
 * the line number, this distinguishes between the males and the females.
 * 
 * - when the method is called first all the Female objects are created,
 * along with one male. 
 * 
 */

	private void assembleFMHashes(String[] input){
		this.parsingIterator++;
		for(int i=0;i<input.length;i++){
			if(parsingIterator <= input.length-1 ){
				if(i==0){
					maleHash.put(input[0], new Male(input[0]));
					singleMaleList.add(maleHash.get(input[0]));
				}
				else if(!femaleHash.containsKey(input[i])){
					femaleHash.put(input[i], new Female(input[i]));
				}
				if(i!=0){
					maleHash.get(input[0]).addfemaleToQueue((femaleHash.get(input[i])));
				}
			}
			else {
				//by this all the new males and females should be added to their respective hashs 
				//now all thats left is to build up the female Queues
				if(i!=0){
					femaleHash.get(input[0]).addMaleToLinkedList((maleHash.get(input[i])));
				}
			}
		}
	}

	/*
	 * applies the Gale Shapley Algorithm.
	 * 
	 *  checks the List of single males, if its not empty. it grabs the first male m from the list, and removes him.
	 *  then it grabs the first female f from m's perference List which is located in the Male Object. if f is not yet 
	 *  engaged then m and f become engaged, else if f is engaged then it checks the perference list of f to compare 
	 *  its current partner to the proposer. if the proposer ranks higher then the partner, partner becomes free and 
	 *  is added to the single male list, else the proposer is added back to the single male list.
	 * 
	 */
	private void applyGs(){
		Male m;
		Female f;

		long start,end;
		double duration;
		start =System.nanoTime();
		while(singleMaleList.size()!=0){

			m = singleMaleList.remove();
			f = m.preferenceQueue.remove();
			if(f.engagedTo==null){
				f.engagedTo=m;
			}
			else{
				if(f.prefernceList.indexOf(f.engagedTo) < f.prefernceList.indexOf(m)){
					singleMaleList.add(m);
				}
				else{
					singleMaleList.add(f.engagedTo);
					f.engagedTo=m;
				}
			}
		}
		assembleFinalPairs();
		end = System.nanoTime();
		duration = (end - start) / Math.pow(10, 9);
		System.out.println("total time :"+duration);
	}
/*
 * The Male object consist of its name and a List of Females
 * which is a Queue. 
 */
	private class Male {
		private String name;
		private Queue<Female> preferenceQueue;

		public Male(String name){
			this.name=name;
			preferenceQueue = new LinkedList<Female>();
		}
		public void addfemaleToQueue(Female p) {
			this.preferenceQueue.add(p);
		}
	}
/*
 * The Female Object consist of its name, a reference to the Male object its 
 * currently paired with, if she's single it references to NULL. It also has
 * a LinkedList of preferable males. 
 * 
 */
	private class Female {
		private Male engagedTo;
		private String name;
		private LinkedList<Male> prefernceList;

		public Female(String name){
			this.name=name;
			prefernceList = new LinkedList<Male>();
		}
		public void setEngagedTo(Male engMale){
			this.engagedTo=engMale;
		}
		public void addMaleToLinkedList(Male p) {
			this.prefernceList.add(p);
		}
	}
/* this method is initiated when the Gs algorithm is completed.
 * iterates over the HashMap(femaleHash), then adds its name
 * and the its pairs name in the TreeMap (finalPair)	
 */
	
	private void assembleFinalPairs(){
		for(Map.Entry<String, Female> entry : femaleHash.entrySet()){
			finalPairs.put(entry.getKey(), entry.getValue().engagedTo.name);
		}
	}
	
	/*
	 * prints all the final pairs
	 * 
	 */
	private void printFinalPairs(){
		for(Map.Entry<String, String> entry : finalPairs.entrySet()){
			System.out.println("("+entry.getKey()+","+entry.getValue()+")");
		}
	}
}
