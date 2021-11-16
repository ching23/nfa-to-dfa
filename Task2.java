// ###############################################################################################################################
// Caitlin ################################################################################################################

// import extensions
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Task2 {

	// line variables from text file
	static String line = null;
	static String line1 = null;
	static String line2 = null;
	static String line3 = null;
	static String line4 = null;
	static String line5 = null;
	static String line6 = null;

	// get these from text file (in order)
	static String [] states; // array for NFA states
	static String [] finalState; // array for NFA final state
	static String [] alphabets; // array for NFA alphabet
	static String startState; // array for NFA start state
	static String [] transitions; // array for NFA transitions
	static String [] inputString; // array for NFA string input
	static ArrayList<Transition> tranList = new ArrayList<>(); // array for new DFA transitions

	// main function 
	public static void main(String[] args) throws IOException {
		// allows program to read the file
		FileReader file = new FileReader("in1.in");
		BufferedReader buffReader = new BufferedReader(file);	
		
		// if states is not empty	
		while((line = buffReader.readLine())!= null){
			//line = states
			tranList.clear();
			line1 = buffReader.readLine(); // NFA final state 
			line2 = buffReader.readLine(); // NFA alphabet
			line3 = buffReader.readLine(); // NFA start state
			line4 = buffReader.readLine(); // NFA transitions
			line5 = buffReader.readLine(); // NFA input string
			line6 = buffReader.readLine(); // should be empty

			// if a line is empty, print empty line and continue
			if(!checkIfEmpty()){
				System.err.print("empty line.");
				continue;
			}
			states = line.split(",");
			finalState = line1.split(",");
			
			// if cannot check final state, continue
			if(!checkFinalState()){
				continue;
			}

			alphabets = line2.split(",");
			startState = line3;

			// if cannot check start state, print invalid start state and continue
			if(!checkStartState()){
				System.err.println(startState + " is an invalid state state.");
				continue;
			}

			transitions = line4.split("#"); // print a # to split each transition
			
			boolean error = false;
			boolean error2 = false;

			// check for errors
			for(String transition : transitions){
				error2 = false;
				String [] tranArray = transition.split(",");
				// error if transition length is not 3
				if(tranArray.length != 3){
					error = true;
					break;
				}

				// error if there are no states
				for ( int i = 0 ; i < 2 ; i++){
					if(!inArray(tranArray[i], states)){
						error2 = true;
						System.err.println("Error. Transition " + tranArray[i] + " is not included.");
						break;
					}	
				}

				// error if there is no alphabet
				if(!inArray(tranArray[2], alphabets) &&!tranArray[2].equals("$")){
					error2 = true;
					System.err.println("Error. Transition " + tranArray[2] + " is not included.");
				}
				if(error2){
					break;
				}
				tranList.add(new Transition(tranArray[0],tranArray[1],tranArray[2]));
			}

			// if there is an error, print the error and continue
			if(error){
				System.err.println("Error. Transition must be size of 3.");
				continue;
			}
			// if there is an error, continue
			if(error2){
				continue;
			}

			inputString = line5.split("#");
			boolean error3 = false;
			String errorInput = "";
			for(String input : inputString){
				String [] inputArray = input.split(",");
				for(String inputAlphabet : inputArray){
					// if input string doesn't match the alphabet, break
					if(!inArray(inputAlphabet, alphabets)){
						error3 = true;
						errorInput = inputAlphabet;
						break;
					}
				}
			}

			// if there is an error within the input string, print the error and continue
			if(error3){
				System.err.println(errorInput + " is an invalid input string.");
				continue;
			}

			System.out.println("NFA Successfully Constructed"); // if a valid NFA, print so
			System.out.println("Constructing DFA: "); // if a DFA can be made, print so
			
			// create start state of DFA
			ArrayList<String> initialDFA = getAllEpsilonClosure(startState, 
				tranList.toArray(new Transition[tranList.size()]));
			// create DFA transitions by storing the transitions into a new array list
			ArrayList<Transition> NFATransitions = new ArrayList<>();
			// create all DFA states by storing the states into a new array list
			ArrayList<ArrayList<String>> allStates = new ArrayList<>();
			NFATransitions = makeTransitions(initialDFA, 
				tranList.toArray(new Transition[tranList.size()]), alphabets);
			
			// loop through all the NFA transitions to create new DFA transitions
			for(int i = 0; i < NFATransitions.size() ; i ++) {
				addStates(allStates, NFATransitions.get(i).fromAL);
				addStates(allStates, NFATransitions.get(i).toAL);
				addTransitions(NFATransitions, makeTransitions(NFATransitions.get(i).toAL, 
					tranList.toArray(new Transition[tranList.size()]), alphabets));
			}
			// print all the DFA states
			String DFAStates = "";
			for(int i = 0 ; i < allStates.size();i++) {
				ArrayList<String> stateInAllStates = allStates.get(i);
				DFAStates += printStates(stateInAllStates);
				if(i<allStates.size()-1) {
					// separate each state with a comma
					DFAStates += ",";
				}
			}

			// print the DFA states
			System.out.println("DFA States: " + DFAStates);
			
			// loop to find DFA all final states
			String DFAfinalState = "";
			for(int i = 0 ; i < allStates.size();i++) {
				ArrayList<String> stateInAllStates = allStates.get(i);
				if(hasAcceptState(finalState, stateInAllStates)) {
					DFAfinalState += printStates(stateInAllStates);
					if(i<allStates.size()-1) {
						DFAfinalState += ",";
					}
				}
			}
			// print the DFA final states
			System.out.println("DFA Final State(s): " + DFAfinalState);
			
			// print the alphabet
			System.out.println("DFA Alphabet: " + line2);
			
			// print the initial states
			String DFAInitState = printStates(initialDFA);
			System.out.println("DFA Initial State: " + DFAInitState);
			
			// print all transitions (DFA and NFA)
			String DFATransitions = "";
			for(int i = 0 ; i<NFATransitions.size();i++) {
				DFATransitions +=printStates(NFATransitions.get(i).fromAL);
				DFATransitions +=",";
				DFATransitions +=printStates(NFATransitions.get(i).toAL);
				DFATransitions +=",";
				DFATransitions +=NFATransitions.get(i).alphabet;
				if(i < NFATransitions.size() - 1) {
					DFATransitions +="#";
				}
			}

			// print DFA transitions
			System.out.println("DFA Transitions: " + DFATransitions);
			
			// print input string
			System.out.println("Input String: " + line5);
			
			constructAndSolveDFA(DFAStates, DFAfinalState, line2, DFAInitState, DFATransitions, line5);
		}
		buffReader.close();
	}
	
	public static void constructAndSolveDFA(String DFAstates, String DFAacceptStates, String DFAAlphabet, 
		String DFAinitState, String DFAtransitions, String DFAinput ) {
		
		// give lines new variable names
		String line = DFAstates;
		String line1 = DFAacceptStates;
		String line2 = DFAAlphabet;
		String line3 = DFAinitState;
		String line4 = DFAtransitions;
		String line5 = DFAinput;
		states = line.split(",");
		finalState = line1.split(",");
		
		// if there is no final state, return
		if(!checkFinalState()){
			return;
		}

		// separate each alphabet with a comma
		alphabets = line2.split(",");
		startState = line3;
		
		// if there is no start state, return
		if(!checkStartState()){
			System.err.println(startState + " is an invalid state state.");
			return;
		}

		// separate each transition with a #
		transitions = line4.split("#");
		boolean error = false;
		boolean error2 = false;

		// check for errors 
		for(String transition : transitions) {
			error2 = false;
			String [] tranArray = transition.split(",");
			if(tranArray.length != 3) {
				error = true;
				break;
			}

			// error if there are no states
			for ( int i = 0 ; i < 2 ;i++){
				if(!inArray(tranArray[i], states)) {
					error2 = true;
					System.err.println("Error. Transition " + tranArray[i] + " is not included.");
					break;
				}	
			}

			// error if other characters are included in DFA
			if(!inArray(tranArray[2], alphabets) &&!tranArray[2].equals("$")){
				error2 = true;
				System.err.println("Error. Transition " + tranArray[2] + " is not included.");
			}
			if(error2) {
				break;
			}
			tranList.add(new Transition(tranArray[0],tranArray[1],tranArray[2]));
		}

		// error if transition size is less than 3
		if(error) {
			System.err.println("Error. Transition must be size of 3.");
			return;
		}
		if(error2) {
			return;
		}

		inputString = line5.split("#");
		boolean error3 = false;
		String errorInput = "";
		for(String input : inputString){
			String [] inputArray = input.split(",");
			for(String inputAlphabet : inputArray){
				if(!inArray(inputAlphabet, alphabets)){
					error3 = true;

// ###############################################################################################################################
// Lilian ########################################################################################################################

					errorInput = inputAlphabet;
					break;
				}
			}
		}
		if(error3){
			System.err.println(errorInput + " is an invalid input string.");
			return;
		}
		boolean error4 = false;
		for(String state : states){
			for(String alphabet : alphabets){
				if(!existsTransition(state,alphabet)){
					error4 = true;
					System.err.println("Missing transition for state " + state + " on input " + alphabet );
					break;
				}
			}
		}
		if(error4){
			return;
		}
		System.out.println("DFA Successfully Constructed");
		for(String input : inputString){
			String result = processInput(input);
			if(inArray(result, finalState)){
				System.out.println("Accepted");
			} else {
				System.out.println("Rejected");
			}
		}
		System.out.println("");
	}

	private static void addStates(ArrayList<ArrayList<String>> allStates, ArrayList<String> someStates) {
			if(!allStates.contains(someStates)) {
				allStates.add(someStates);
			}		
	}

	public static void addTransitions(ArrayList<Transition> nFATransitions,ArrayList<Transition> newTransitions) {
		for(int i = 0 ; i< newTransitions.size() ; i++) {
			Collections.sort(newTransitions.get(i).fromAL);
			Collections.sort(newTransitions.get(i).toAL);
			int j;
			for (j = 0;j < nFATransitions.size(); j++) {
				Collections.sort(nFATransitions.get(j).fromAL);
				Collections.sort(nFATransitions.get(j).toAL);
				if(nFATransitions.get(j).fromAL.equals(newTransitions.get(i).fromAL) && nFATransitions.get(j).toAL.equals(newTransitions.get(i).toAL) && nFATransitions.get(j).alphabet.equals(newTransitions.get(i).alphabet)){
					break;
				}
			}
			if(j == nFATransitions.size()) {
				nFATransitions.add(newTransitions.get(i));
			}
		}
	}

	private static String processInput(String input) {
		String currentState = startState;
		String [] inputArray = input.split(",");
		for(int i = 0 ; i< inputArray.length ;i++){
			for(int j = 0 ; j < tranList.size() ; j++){
				if(tranList.get(j).from.equals(currentState) && tranList.get(j).alphabet.equals(inputArray[i])){
					currentState = tranList.get(j).to;
					break;
				}
			}
		}
		return currentState;
	}
	private static boolean checkStartState() {
		return inArray(startState, states);
	}
	private static boolean checkFinalState() {
		for(String goal : finalState){
			if(goal.equals("")){
				continue;
			}
			if(!inArray(goal,states)){
				System.err.println("Invalid accept state "+goal);
				return false;
			}
		}
		return true;
	}
	private static boolean checkIfEmpty() {
		if(line1 == "") {
			System.err.println("First line is an");
			return false;
		}
		if(line2 == "") {
			System.err.println("Second line is an");
			return false;
		}
		if(line3 == "") {
			System.err.println("Third line is an");
			return false;
		}
		if(line4 == "") {
			System.err.println("Fourth line is an");
			return false;
		}
		if(line5 == "") {
			System.err.println("Fifth line is an");
			return false;
		}
		if(line6 == "") {
			System.err.println("Last line is not an");
			return false;
		}
		return true;
	}
	
	private static boolean inArray(String s , String [] array){
		for(int i = 0 ; i < array.length;i++){
			if(array[i].equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<String> getEpsilonClosure(String state,Transition[]transitions){
		ArrayList<String> result = new ArrayList<>();
		result.add(state);
		for(int i = 0 ; i<transitions.length;i++) {
			if(transitions[i].alphabet.equals("$") && transitions[i].from.equals(state)&&!result.contains(transitions[i].to)) {
				result.add(transitions[i].to);
			}
		}
		return result;
	}
	
	public static ArrayList<String> getAllEpsilonClosure(String state,Transition[]transitions){
		ArrayList<String> result = getEpsilonClosure(state, transitions);
		for(int i = 0 ; i < result.size();i++) {
			ArrayList<String> newOutcome = getEpsilonClosure(result.get(i), transitions);
			for(int j = 0 ; j<newOutcome.size();j++) {
				if (!result.contains(newOutcome.get(j))) {
					result.add(newOutcome.get(j));
				}
			}
		}
		return result;
	}
	
	public static boolean hasAcceptState(String[] acceptStates, ArrayList<String> stateOfStates) {
		for(int i = 0 ; i < stateOfStates.size();i++) {
			for( int j = 0 ; j < acceptStates.length ;j++) {
				if(acceptStates[j].equals(stateOfStates.get(i))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static ArrayList<String> getStatesForGivenInput(ArrayList<String> stateOfStates, Transition[]transitions, String alphabet){
		ArrayList<String> result = new ArrayList<>();
		for(int i = 0 ; i < stateOfStates.size() ; i++) {
			for(int j = 0 ; j < transitions.length ;j++) {
				if(transitions[j].alphabet.equals(alphabet) && transitions[j].from.equals(stateOfStates.get(i))&&!result.contains(transitions[j].to)) {
					result.add(transitions[j].to);
					addIfNotContains(result, getAllEpsilonClosure(transitions[j].to, transitions));
				}
			}
		}
		return result;
	}
	
	public static void addIfNotContains(ArrayList<String> result, ArrayList<String> arrayToBeAdded) {
		for(int i = 0; i<arrayToBeAdded.size();i++) {
			if(!result.contains(arrayToBeAdded.get(i))) {
				result.add(arrayToBeAdded.get(i));
			}
		}
	}
	
	public static ArrayList<Transition> makeTransitions(ArrayList<String> stateOfStates,Transition[]transitions,String[]alphabets) {
		ArrayList<Transition> result= new ArrayList<>();
		for(int i = 0 ; i< alphabets.length ; i++) {
			ArrayList<String> toStates = getStatesForGivenInput(stateOfStates, transitions, alphabets[i]);
			if(toStates.size() == 0) {
				toStates.add("Dead");
			}
			result.add(new Transition(stateOfStates, toStates, alphabets[i]));
		}
		return result;
	}

	public static String printStates(ArrayList<String>states) {
		String r = "";
		for(int i = 0 ; i<states.size();i++) {
			r+=states.get(i);
			if(i < states.size() - 1) {
				r+="*";
			}
		}
		return r;
	}
	
	private static boolean existsTransition(String state, String alphabet) {
		for(int i = 0 ; i < tranList.size() ; i++){
			if(tranList.get(i).from.equals(state) && tranList.get(i).alphabet.equals(alphabet)){
				return true;
			}
		}
		return false;
	}
}

class Transition {
	String from;
	String to;
	ArrayList<String> fromAL;
	ArrayList<String> toAL;
	String alphabet;

	public Transition(ArrayList<String> from, ArrayList<String> to, String alphabet){
		this.fromAL = from;
		this.toAL = to;
		this.alphabet = alphabet;
	}

	public Transition(String from, String to, String alphabet){
		this.from = from;
		this.to = to;
		this.alphabet = alphabet;
	}
}