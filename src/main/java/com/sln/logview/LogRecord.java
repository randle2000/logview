package com.sln.logview;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogRecord {
	private final static SimpleDateFormat timestampFormatter = new SimpleDateFormat("HH:mm:ss");
	
    private Date   timestamp;
    private Level  level;
    private String context;
    private String message;

    public LogRecord(Level level, String context, String message) {
        this.timestamp = new Date();
        this.level     = level;
        this.context   = context;
        this.message   = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }

	@Override
	public String toString() {
		return (timestamp != null ? timestampFormatter.format(timestamp) + " " : "")
				+ (context != null ? context + " " : "") + (message != null ? message : "");
	}
    
}
