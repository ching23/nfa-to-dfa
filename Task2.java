//package compilers;

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
	static String l1 = null;
	static String l2 = null;
	static String l3 = null;
	static String l4 = null;
	static String l5 = null;
	static String l6 = null;

	// get these from text file
	static String [] states; // array for NFA states
	static String [] finalState; // array for NFA final state
	static String [] alphabets; // array for NFA alphabet
	static String startState; // array for NFA start state
	static String [] transitions; // array for NFA transitions
	static String [] inputString; // array for NFA string input
	static ArrayList<Transition> transitionList = new ArrayList<>(); // array for new DFA transitions

	// main function 
	public static void main(String[] args) throws IOException {
		// allows program to read the file
		FileReader fileReader = new FileReader("in1.in");
		BufferedReader br = new BufferedReader(fileReader);	
		// if states is not empty	
		while((line = br.readLine())!= null){
			//line = states
			transitionList.clear();
			l1 = br.readLine(); // NFA final state 
			l2 = br.readLine(); // NFA alphabet
			l3 = br.readLine(); // NFA start state
			l4 = br.readLine(); // NFA transitions
			l5 = br.readLine(); // NFA input string
			l6 = br.readLine(); // should be empty

			// if a line is empty, print empty line and continue
			if(!checkLines()){
				System.err.print("empty line.");
				continue;
			}
			states = line.split(",");
			finalState = l1.split(",");
			
			// if cannot check final state, continue
			if(!checkGoal()){
				continue;
			}
			alphabets = l2.split(",");
			startState = l3;

			// if cannot check start state, print invalid start state and continue
			if(!checkStart()){
				System.err.println("Invalid start state " + startState);
				continue;
			}

			// print a # to split each transition
			transitions = l4.split("#");
			boolean error = false;
			boolean error2 = false;

			// check for errors
			for(String transition : transitions){
				error2 = false;
				String [] transitionArray = transition.split(",");
				if(transitionArray.length != 3){
					error = true;
					break;
				}

				// error if there are no states
				for ( int i = 0 ; i < 2 ; i++){
					if(!inArray(transitionArray[i], states)){
						error2 = true;
						System.err.println("Invalid transition. "+transitionArray[i]+" is not included in the states.");
						break;
					}	
				}

				// error if there is no alphabet
				if(!inArray(transitionArray[2], alphabets) &&!transitionArray[2].equals("$")){
					error2 = true;
					System.err.println("Invalid transition. "+transitionArray[2]+" is not included in the alphabet.");
				}
				if(error2){
					break;
				}
				transitionList.add(new Transition(transitionArray[0],transitionArray[1],transitionArray[2]));
			}

			// if there is an error, print the error and continue
			if(error){
				System.err.println("Invalid transition. Transitions should be of size 3");
				continue;
			}
			// if there is an error, continue
			if(error2){
				continue;
			}

			inputString = l5.split("#");
			boolean error3 = false;
			String badInput = "";
			for(String input : inputString){
				String [] inputArray = input.split(",");
				for(String inputAlphabet : inputArray){
					// if input string doesn't match the alphabet, break
					if(!inArray(inputAlphabet, alphabets)){
						error3 = true;
						badInput = inputAlphabet;
						break;
					}
				}
			}

			// if there is an error within the input string, print the error and continue
			if(error3){
				System.err.println("Invalid input string at " + badInput);
				continue;
			}

			System.out.println("NFA Constructed"); // if a valid NFA, print so
			System.out.println("Equivalent DFA: "); // if a DFA can be made, print so
			
			// create start state of DFA
			ArrayList<String> initialStateDFA = getAllEpsilonClosure(startState, 
				transitionList.toArray(new Transition[transitionList.size()]));
			// create DFA transitions by storing the transitions into a new array list
			ArrayList<Transition> NFATransitions = new ArrayList<>();
			// create all DFA states by storing the states into a new array list
			ArrayList<ArrayList<String>> allStates = new ArrayList<>();
			NFATransitions = makeTransitions(initialStateDFA, 
				transitionList.toArray(new Transition[transitionList.size()]), alphabets);
			
			// loop through all the NFA transitions to create new DFA transitions
			for(int i = 0; i < NFATransitions.size() ; i ++) {
				addToStates(allStates, NFATransitions.get(i).fromAL);
				addToStates(allStates, NFATransitions.get(i).toAL);
				addTransitionsIfNotExists(NFATransitions, makeTransitions(NFATransitions.get(i).toAL, 
					transitionList.toArray(new Transition[transitionList.size()]), alphabets));
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
			System.out.println(DFAStates);
			
			// loop to find DFA all final states
			String DFAGoals = "";
			for(int i = 0 ; i < allStates.size();i++) {
				ArrayList<String> stateInAllStates = allStates.get(i);
				if(hasAcceptState(finalState, stateInAllStates)) {
					DFAGoals += printStates(stateInAllStates);
					if(i<allStates.size()-1) {
						DFAGoals += ",";
					}
				}
			}
			// print the DFA final states
			System.out.println(DFAGoals);
			
			// print the alphabet
			System.out.println(l2);
			
			// print the initial states
			String DFAInitState = printStates(initialStateDFA);
			System.out.println(DFAInitState);
			
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
			System.out.println(DFATransitions);
			
			// print input string
			System.out.println(l5);
			
			constructAndSolveDFA(DFAStates, DFAGoals, l2, DFAInitState, DFATransitions, l5);
		}
		br.close();
	}
	
	public static void constructAndSolveDFA(String DFAstates, String DFAacceptStates, String DFAAlphabet, 
		String DFAinitState, String DFAtransitions, String DFAinput ) {
		
		// give lines new variable names
		String line = DFAstates;
		String l1 = DFAacceptStates;
		String l2 = DFAAlphabet;
		String l3 = DFAinitState;
		String l4 = DFAtransitions;
		String l5 = DFAinput;
		states = line.split(",");
		finalState = l1.split(",");
		
		// if there is no final state, return
		if(!checkGoal()){
			return;
		}

		// separate each alphabet with a comma
		alphabets = l2.split(",");
		startState = l3;
		
		// if there is no start state, return
		if(!checkStart()){
			System.err.println("Invalid start state " + startState);
			return;
		}

		// separate each transition with a #
		transitions = l4.split("#");
		boolean error = false;
		boolean error2 = false;

		// check for errors 
		for(String transition : transitions) {
			error2 = false;
			String [] transitionArray = transition.split(",");
			if(transitionArray.length != 3) {
				error = true;
				break;
			}

			// error if there are no states
			for ( int i = 0 ; i < 2 ;i++){
				if(!inArray(transitionArray[i], states)) {
					error2 = true;
					System.err.println("Invalid transition. "+transitionArray[i]+" is not included in the states.");
					break;
				}	
			}

			// error if other characters are included in DFA
			if(!inArray(transitionArray[2], alphabets) &&!transitionArray[2].equals("$")){
				error2 = true;
				System.err.println("Invalid transition. " + transitionArray[2] + " is not included in the alphabet.");
			}
			if(error2) {
				break;
			}
			transitionList.add(new Transition(transitionArray[0],transitionArray[1],transitionArray[2]));
		}

		// error if transition size is less than 3
		if(error) {
			System.err.println("Invalid transition. Transitions should be of size 3");
			return;
		}
		if(error2) {
			return;
		}

		inputString = l5.split("#");
		boolean error3 = false;
		String badInput = "";
		for(String input : inputString){
			String [] inputArray = input.split(",");
			for(String inputAlphabet : inputArray){
				if(!inArray(inputAlphabet, alphabets)){
					error3 = true;

// ###############################################################################################################################
// Lilian ########################################################################################################################

					badInput = inputAlphabet;
					break;
				}
			}
		}
		if(error3){
			System.err.println("Invalid input string at " + badInput);
			return;
		}
		boolean error4 = false;
		for(String state : states){
			for(String alphabet : alphabets){
				if(!existsTransition(state,alphabet)){
					error4 = true;
					System.err.println("Missing transition for state " + state+" on input " + alphabet );
					break;
				}
			}
		}
		if(error4){
			return;
		}
		System.out.println("DFA Constructed");
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

	private static void addToStates(ArrayList<ArrayList<String>> allStates, ArrayList<String> someStates) {
			if(!allStates.contains(someStates)) {
				allStates.add(someStates);
			}		
	}
	public static void addTransitionsIfNotExists(ArrayList<Transition> nFATransitions,ArrayList<Transition> newTransitions) {
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
			for(int j = 0 ; j < transitionList.size() ; j++){
				if(transitionList.get(j).from.equals(currentState) && transitionList.get(j).alphabet.equals(inputArray[i])){
					currentState = transitionList.get(j).to;
					break;
				}
			}
		}
		return currentState;
	}
	private static boolean checkStart() {
		return inArray(startState, states);
	}
	private static boolean checkGoal() {
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
	private static boolean checkLines() {
		if(l1 == ""){
			System.err.println("First line is an");
			return false;
		}
		if(l2 == ""){
			System.err.println("Second line is an");
			return false;
		}
		if(l3 == ""){
			System.err.println("Third line is an");
			return false;
		}
		if(l4 == ""){
			System.err.println("Fourth line is an");
			return false;
		}
		if(l5 == ""){
			System.err.println("Fifth line is an");
			return false;
		}
		if(l6 == ""){
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
		for(int i = 0 ; i < transitionList.size() ; i++){
			if(transitionList.get(i).from.equals(state) && transitionList.get(i).alphabet.equals(alphabet)){
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
	public Transition(String from, String to, String alphabet){
		this.from = from;
		this.to = to;
		this.alphabet = alphabet;
	}
	public Transition(ArrayList<String> from, ArrayList<String> to, String alphabet){
		this.fromAL = from;
		this.toAL = to;
		this.alphabet = alphabet;
	}
}