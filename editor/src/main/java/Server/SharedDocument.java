package Server;

import java.util.ArrayList;
import java.util.List;

public class SharedDocument {
    private final List<String> lines = new ArrayList<>() ;

    public synchronized String getline(int i) {
        return lines.get(i-1) ; 
    }

    public synchronized List<String> getAllLines() {
        return new ArrayList<>(lines) ; 
    }
    public synchronized void modifyline(int i, String text) {
        lines.set(i -1 , text) ; 
    }
    
    public synchronized void deleteline(int i) {
        lines.remove(i-1) ;
    }

    public synchronized void addline(int i, String text) {
        lines.add(i-1,text) ; 
    }

    public synchronized int size() {
        return lines.size() ; 
    }
}
