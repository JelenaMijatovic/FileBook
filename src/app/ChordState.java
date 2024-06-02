package app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import servent.message.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;

	private Map<String, Integer> myFiles;
	private Map<String, Integer> backupFiles;
	private Set<Integer> friends;
	private ServentInfo[] backupSuccessors;
	ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		backupSuccessors = new ServentInfo[2];

		predecessorInfo = null;
		valueMap = new HashMap<>();
		myFiles = new HashMap<>();
		backupFiles = new HashMap<>();
		friends = new HashSet<>();
		allNodeInfo = new ArrayList<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		this.valueMap = welcomeMsg.getValues();
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		timerService.scheduleAtFixedRate(() -> {
			try {
				for (ServentInfo si : backupSuccessors) {
					if (si != null)
						ping(si.getListenerPort());
				}
			} catch (Throwable e) {

			}
		}, 10000, AppConfig.WEAK_lIMIT, TimeUnit.MILLISECONDS);
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}

	public Map<String, Integer> getMyFiles() {
		return myFiles;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}

	public Set<Integer> getFriends() {
		return friends;
	}

	public void stopTimer() {
		timerService.shutdown();
	}
	
	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		backupSuccessors[0] = successorTable[0];
		int i = 1;
		while (i < successorTable.length) {
			if (successorTable[i].getChordId() != successorTable[0].getChordId()) {
				backupSuccessors[1] = successorTable[i];
				break;
			}
			i++;
		}
		AppConfig.timestampedStandardPrint(String.valueOf(backupSuccessors[0].getChordId()));
		AppConfig.timestampedStandardPrint(String.valueOf(backupSuccessors[1].getChordId()));
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (!newList2.isEmpty()) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	/**
	 * The add file operation. Creates file and stores path and visibility locally. Asks two closest successors to backup files
	 */
	public void addFile(String path, int visibility) { //predecessor will have a copy of your file, if its down send file further
		File newFile = new File(AppConfig.root+"/"+path);
		try {
			if (newFile.createNewFile()) {
				myFiles.put(newFile.getPath(), visibility);
				AppConfig.timestampedStandardPrint("Created file " + path);
				for (ServentInfo si : backupSuccessors) {
					if (si != null) {
						CopyMessage cm = new CopyMessage(AppConfig.myServentInfo.getListenerPort(), si.getListenerPort(), path);
						MessageUtil.sendMessage(cm);
					}
				}
			} else {
				AppConfig.timestampedErrorPrint("add_file: File with filename " + path + " already present on servent");
            }
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint(new RuntimeException(e).getMessage());
        }
	}

	/**
	 * The backup file operation. Creates file in backup and stores the local path and original owner
	 * If backup creation fails, sends notice to original sender
	 */
	public void backupFile(String path, int originalSenderPort) {
		if (!Files.exists(Path.of(AppConfig.root + "/backup/" + originalSenderPort))) {
			try {
				Files.createDirectories(Path.of(AppConfig.root + "/backup/" + originalSenderPort));
			} catch (IOException e) {
				AppConfig.timestampedErrorPrint(new RuntimeException(e).getMessage());
				//warn
				return;
			}
		}
		File newFile = new File(AppConfig.root+"/backup/"+originalSenderPort+"/"+path);
		try {
			if (newFile.createNewFile()) {
				backupFiles.put(newFile.getPath(), originalSenderPort);
				//AppConfig.timestampedStandardPrint("Created backup " + path);
			} else {
				AppConfig.timestampedErrorPrint("add_file: Backup of " + path + " from servent " + originalSenderPort + " already present");
			}
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint(new RuntimeException(e).getMessage());
			//warn
		}
	}

	/**
	 * Gathers this servent's files. Includes private files if sender is ourselves or a friend, otherwise only public files
	 */
	public String listFiles(int port) {
		String files = "";
		if (AppConfig.myServentInfo.getListenerPort() == port || friends.contains(port)) {
			for (String f : myFiles.keySet()) {
				files = files.concat(f + ",");
			}
			files = files.substring(0,files.length()-1);
		} else {
			for (Map.Entry<String, Integer> e : myFiles.entrySet()) {
				if (e.getValue() == 0) {
					files = files.concat(e.getKey() + ",");
				}
			}
			files = files.substring(0,files.length()-1);
		}
		return files;
	}

	/**
	 * The get files operation. Asks the requested servent for their files if they're our neighbour, otherwise sends the request forwards
	 */
	public void getFiles(int OrigSenderPort, int requestedPort) {
		for (ServentInfo si : successorTable) {
			if (si.getListenerPort() == requestedPort) {
				GetFilesMessage gm = new GetFilesMessage(AppConfig.myServentInfo.getListenerPort(), si.getListenerPort(), OrigSenderPort, requestedPort);
				MessageUtil.sendMessage(gm);
				return;
			}
		}
		GetFilesMessage gm = new GetFilesMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), OrigSenderPort, requestedPort);
		MessageUtil.sendMessage(gm);
	}

	/**
	 * The delete file operation. Tries to delete file locally, and sends notice to successors to delete copies
	 */
	public void removeFile(String path) {
		File file = new File(AppConfig.root + "/" + path);
		String filepath = file.getPath();
		if (file.delete()) {
			myFiles.remove(filepath);
			AppConfig.timestampedStandardPrint("Removed file " + path);
			for (ServentInfo si : backupSuccessors) {
				if (si != null) {
					RemoveMessage rm = new RemoveMessage(AppConfig.myServentInfo.getListenerPort(), si.getListenerPort(), path);
					MessageUtil.sendMessage(rm);
				}
			}/*
			while (count < 2 && i < successorTable.length) {
				if (successorTable[i].getChordId() != curr) {
					RemoveMessage rm = new RemoveMessage(AppConfig.myServentInfo.getListenerPort(), successorTable[i].getListenerPort(), path);
					MessageUtil.sendMessage(rm);
					count++;
				}
				curr = successorTable[i].getChordId();
				i++;
			}*/
		} else {
			AppConfig.timestampedErrorPrint("remove_file: file " + path + " couldn't be succesfully deleted");
		}
	}

	/**
	 * The delete backup operation. Deletes local backup and removes path from memory
	 */
	public void removeBackup(String path, int originalSenderPort) {
		File file = new File(AppConfig.root+"/backup/"+originalSenderPort+"/"+path);
		String filepath = file.getPath();
		if (file.delete()) {
			backupFiles.remove(filepath);
			//AppConfig.timestampedStandardPrint("Deleted backup " + path);
		} else {
			AppConfig.timestampedErrorPrint("remove_file: backup " + path + " from servent " + originalSenderPort + " couldn't be succesfully deleted");
		}
	}

	/**
	 * Pings target servent
	 */
	public void ping(int port) {
		PingMessage pm = new PingMessage(AppConfig.myServentInfo.getListenerPort(), port);
		MessageUtil.sendMessage(pm);
	}

}
