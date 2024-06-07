package app;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Suzuki-Kasami token
 */
public class SKToken {

    /**
     * Stores the most recent Request Numbers for which the token was granted
     */
    Integer[] lastRequests;
    /**
     * Queue of processes waiting for the token
     */
    Queue<Integer> queue;

    public SKToken() {
        lastRequests = new Integer[AppConfig.SERVENT_COUNT];
        for (int i = 0; i < AppConfig.SERVENT_COUNT; i++) {
            lastRequests[i] = 0;
        }
        queue = new LinkedList<>();
    }

    public Integer[] getLastRequests() {
        return lastRequests;
    }

    public Queue<Integer> getQueue() {
        return queue;
    }

    @Override
    public String toString() {
        String tok = "";
        for (Integer i : lastRequests) {
            tok = tok.concat(i + ",");
        }
        tok = tok.substring(0, tok.length()-1);
        tok = tok.concat(":");
        for (Integer i : queue) {
            tok = tok.concat(i + ",");
        }
        if (!tok.endsWith(":")) {
            tok = tok.substring(0, tok.length()-1);
        } else {
            tok = tok.concat("N");
        }
        return tok;
    }

    public void buildTokenfromString(String lastRequestsS, String queueS) {
        AppConfig.timestampedStandardPrint(lastRequestsS + queueS);
        String[] splitLR = lastRequestsS.split(",");
        if (splitLR.length == AppConfig.SERVENT_COUNT) {
            int i = 0;
            for (String s : splitLR) {
                lastRequests[i] = Integer.parseInt(s);
                i++;
            }
        } else {
            AppConfig.timestampedErrorPrint("Wrong length of token last requests string");
        }
        queue.clear();
        if (!queueS.equals("N")) {
            String[] splitQ = queueS.split(",");
            for (int i = splitQ.length-1; i >= 0; i--) {
                queue.add(Integer.parseInt(splitQ[i]));
            }
        }
    }
}
