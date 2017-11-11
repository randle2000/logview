package com.sln.logview;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogAppender extends AppenderBase<ILoggingEvent> {
	
	private Logger logger = new Logger("main");;
	
	@Override
	protected void append(ILoggingEvent loggingEvent) {
		final String context = "[" + loggingEvent.getThreadName() + "]";
		final String message = loggingEvent.getLoggerName() + " - " + loggingEvent.getFormattedMessage();
		Level level; 

		switch (loggingEvent.getLevel().toInt()) {
			case ch.qos.logback.classic.Level.DEBUG_INT:
				level = Level.DEBUG;
				break;
			case ch.qos.logback.classic.Level.INFO_INT:	
				level = Level.INFO;
				break;
			case ch.qos.logback.classic.Level.WARN_INT:	
				level = Level.WARN;
				break;
			case ch.qos.logback.classic.Level.ERROR_INT:
				level = Level.ERROR;
				break;
			default:
				level = Level.DEBUG;
				break;
		}
		logger.log(new LogRecord(level, context, message));
	}
	
	public Logger getLogger() {
		return logger;
	}
	
}
