package com.sln.logview;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

// https://stackoverflow.com/questions/24116858/most-efficient-way-to-log-messages-to-javafx-textarea-via-threads-with-simple-cu?noredirect=1&lq=1
public class Logger {
	private static final int MAX_LOG_ENTRIES = 3_000;

    private final BlockingDeque<LogRecord> logQueue = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);
    private final String context;

    public Logger(String context) {
        this.context = context;
    }
    
    public void drainTo(Collection<? super LogRecord> collection) {
    	logQueue.drainTo(collection);
    }

    public void log(LogRecord record) {
    	logQueue.offer(record);
    }

    public void debug(String msg) {
        log(new LogRecord(Level.DEBUG, context, msg));
    }

    public void info(String msg) {
        log(new LogRecord(Level.INFO, context, msg));
    }

    public void warn(String msg) {
        log(new LogRecord(Level.WARN, context, msg));
    }

    public void error(String msg) {
        log(new LogRecord(Level.ERROR, context, msg));
    }

}
