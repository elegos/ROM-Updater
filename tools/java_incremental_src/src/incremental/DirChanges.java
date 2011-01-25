package incremental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class DirChanges {
	private ArrayList<String> newFiles;
	private ArrayList<String> modFiles;
	private ArrayList<String> rmFiles;
	
	public DirChanges() {
		newFiles = new ArrayList<String>();
		modFiles = new ArrayList<String>();
		rmFiles = new ArrayList<String>();
	}
	
	public ArrayList<String> getNewFiles() {
		return newFiles;
	}
	
	public ArrayList<String> getModifiedFiles() {
		return modFiles;
	}
	
	public ArrayList<String> getRemovedFiles() {
		return rmFiles;
	}
	
	public void setLists(Map<String, String> oldMap, Map<String, String> newMap) {
		String hash, key;
		
		Iterator<String> theIterator = oldMap.keySet().iterator();
		while(theIterator.hasNext()) {
			key = theIterator.next();
			if(newMap.containsKey(key)) { // file exists in the new version
				hash = newMap.get(key);
				if(!hash.equals(oldMap.get(key))) // files are different
					modFiles.add(key);
			}
			else // file doesn't exist in the new version
				rmFiles.add(key);
		}
		
		theIterator = newMap.keySet().iterator();
		while(theIterator.hasNext()) {
			key = theIterator.next();
			if(!oldMap.containsKey(key)) // file doesn't exist in the old version
				newFiles.add(key);
		}
		
		Collections.sort(newFiles);
		Collections.sort(modFiles);
		Collections.sort(rmFiles);
	}
}
