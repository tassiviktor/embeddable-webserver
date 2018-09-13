package hu.viktortassi.httpserver;

import hu.viktortassi.httpserver.exceptions.TooMuchThreadException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadManager {

    class ThreadCleaner extends TimerTask {

        public ThreadCleaner() {
        }

        @Override
        public void run() {
            cleanDefunctThreads(true);
        }
    }

    List<Object> threadList = new LinkedList<>();
    int maxThreads = 10;

    ThreadManager(int i) {
        maxThreads = i;

        Timer timer = new Timer();
        timer.schedule(new ThreadCleaner(), 0, 5000);

    }

    public synchronized <T> T createThread(Class<T> threadClass) throws InstantiationException, IllegalAccessException, TooMuchThreadException {
        if (threadList.size() >= maxThreads) {
            cleanDefunctThreads(false);
        }
        if (threadList.size() >= maxThreads) {
            throw new TooMuchThreadException("Maximum available thread reached and request cannot be served.");
        }

        T item = threadClass.newInstance();
        threadList.add(item);        

        return item;
    }

    public synchronized Integer threadCount() {
        return threadList.size();
    }

    public synchronized void cleanDefunctThreads(boolean all) {
        if (threadList.size() < 1) {
            return;
        }

        for (Iterator<Object> iterator = threadList.iterator(); iterator.hasNext();) {
            Object t = iterator.next();
            if (!((Thread) t).isAlive() && !((Thread) t).isDaemon()) {
                iterator.remove();
            }
            if (!all && maxThreads < threadList.size()) {
                break;
            }
        }

    }
    
}
