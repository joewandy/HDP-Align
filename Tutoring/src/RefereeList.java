import java.io.*;
import java.util.*;

public class RefereeList {
	
	public RefereeList () {
		refereeList = new Referee [maxReferees];	
	}
	
	public RefereeList (Referee [] refList) {
		refereeList = refList;
		numReferees = refList.length;
	}
	
	public void inputFromFile () {
		String fileName = "/home/joewandy/workspace/Tutoring/src/RefereesIn.txt";
		try {
			FileReader fr = null;
			try {
				fr = new FileReader (fileName);
				Scanner in = new Scanner (fr);
				while (in.hasNextLine()) {
					String line = in.nextLine();
					Referee ref = new Referee (line);
					refereeList[numReferees] = ref;
					numReferees++;
				}
			}
			finally {
				if (fr != null) fr.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	public Referee searchForReferee(String name) {
		Referee ref = null;
		for (int i=0; i < numReferees; i++)
			if (refereeList[i].getName().equals(name))
				ref = refereeList[i];
		return ref;
	}
	
	public void deleteReferee(Referee ref) {
		String name = ref.getName();
		boolean found = false;
		int i=0;
		while (i < numReferees && !found)
			if (refereeList[i].getName().equals(name)) {
				ref = refereeList[i];
				found = true;
			}
			else i++;
		if (found) {
			for (int j=i; j<numReferees-1; j++)
				refereeList[j] = refereeList[j+1];
			numReferees--;
		}		
	}
	
	public boolean addReferee(Referee ref) {
		String name = ref.getName();
		String ID = ref.getID();
		boolean done = false;
		boolean IDpresent = false;
		boolean namePresent = false;
		int i=numReferees-1;
		String ID2full="";
		String ID2="";
		while (i >=0 && !done) {
			Referee ref2 = refereeList[i];
			if (name.equals(ref2.getName())) {
				done=true;
				namePresent = true;
			}
			else{
				ID2full = ref2.getID();
				ID2 = ""+ID2full.charAt(0)+ID2full.charAt(1);
				if (ID.equals(ID2)) {
					done = true;
					IDpresent = true;
				}
				else if (ID.compareTo(ID2)>0)
					done = true;
				else
					i--;
			}
		}
		if (!namePresent) {
			if (IDpresent) {
				int ID2num = (int) Integer.parseInt(ID2full.substring(2,ID2full.length()));
				ID += (ID2num+1);
			}
			else
				ID = ID + "1";
			ref.setID(ID);
			if (numReferees == maxReferees) {
				maxReferees++;
				Referee [] newRefereeList = new Referee [maxReferees];
				System.arraycopy(refereeList, 0, newRefereeList, 0, refereeList.length);
				refereeList = newRefereeList;
			}
			for (int j=numReferees-1; j>i; j--)
				refereeList[j+1] = refereeList[j];
			if (i>=0)
				refereeList[i+1]=ref;
			else
				refereeList[0]=ref;
			numReferees++;
		}
		return namePresent;
	}				
	
	public int getNumReferees() {
		return numReferees;
	}
	
	public int getMaxNumAllocations() {
		int maxNumAllocations = -1;
		if (numReferees> 0) {
			maxNumAllocations = refereeList[0].getNumAllocations();
			for (int i=1; i<numReferees; i++) {
				int n = refereeList[i].getNumAllocations();
				if (n > maxNumAllocations)
					maxNumAllocations = n;
			}
		}
		return maxNumAllocations;
	}
	
	public int getNumAllocations(int i) {
		return refereeList[i].getNumAllocations();
	}
	
	public String getID(int i) {
		return refereeList[i].getID();
	}
	
	public Referee [] findPriority1(String level, String locality) {
		Referee [] priority1List = new Referee[numReferees];
		int numSuitableReferees = 0;
		for (int i=0; i<numReferees; i++) {
			Referee ref = refereeList[i];
			if (ref.isSuitable(level) && ref.getLocality().equals(locality)) {
				priority1List[numSuitableReferees] = ref;
				numSuitableReferees++;
			}
		}
		priority1List = sortList(priority1List);
		return priority1List;
	}
	
	public Referee [] findPriority2(String level, String locality) {
		Referee [] priority2List = new Referee[numReferees];
		int numSuitableReferees = 0;
		for (int i=0; i<numReferees; i++) {
			Referee ref = refereeList[i];
			if (ref.isSuitable(level) && ref.isAdjacent(locality)
					                  && ref.willVisit(locality)) {
				priority2List[numSuitableReferees] = ref;
				numSuitableReferees++;
			}
		}
		priority2List = sortList(priority2List);
		return priority2List;
	}
	
	public Referee [] findPriority3(String level, String locality) {
		Referee [] priority3List = new Referee[numReferees];
		int numSuitableReferees = 0;
		for (int i=0; i<numReferees; i++) {
			Referee ref = refereeList[i];
			if (ref.isSuitable(level) && !ref.isAdjacent(locality) &&
				!ref.getLocality().equals(locality) && ref.willVisit(locality)) {
				priority3List[numSuitableReferees] = ref;
				numSuitableReferees++;
			}
		}
		priority3List = sortList(priority3List);
		return priority3List;
	}
	
	public Referee [] sortList(Referee[] list) {
		int listLen = 0;
		while (list[listLen]!=null)
			listLen++;
		for (int i = 1; i < listLen; i++) {
			Referee next = list[i];
			int j = i;
			while (j > 0 && list[j - 1].getNumAllocations() > next.getNumAllocations())
			{
				list[j] = list[j - 1];
				j--;
			}
			list[j] = next;
		}
		Referee [] newList = new Referee[listLen];
		System.arraycopy(list, 0, newList, 0, listLen);
		list = newList;
		return list;	
	}
	
	public void writeFile() {
		String outputFileName = "RefereesOut.txt";
		PrintWriter writer = null;
		try {
			try {
				writer = new PrintWriter(outputFileName);
				writer.print(toString());
			}
			finally {
				if (writer!= null)
					writer.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i < numReferees; i++)
			s += refereeList[i].toString();
		return s;
	}
	
	private int numReferees = 0;
	private int maxReferees = 12;
	private Referee [] refereeList;
}
