package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Logs {

	private static Logger logger;

	private static Logger getLogger() {
		if(logger == null){
		    init(Level.INFO);
		}
		return logger;
	}

	public static void initSimple() {
		init(Level.FINEST, false, null);
	}
	public static void init(Level level) {
	    init(level, true, (File)null);
	}
	public static void init(Level level, String optionalOutputFile) {
		init(level, StringUtils.isEmpty(optionalOutputFile) ? (File)null : new File(optionalOutputFile));
	}
	public static synchronized void init(Level level, File optionalOutputFile) {
		init(level, true, optionalOutputFile);
	}
	public static synchronized void init(Level level, boolean logLevelAndTime, File optionalOutputFile) {
		logger = null;
		LogManager.getLogManager().reset();

	    logger = Logger.getLogger(Logs.class.getSimpleName());
	    logger.setUseParentHandlers(false);

	    Formatter formatter;
	    if(logLevelAndTime) {
	    	formatter = new Formatter(){
	    		DateFormat df = DateUtil.getFormatDateTime();
	    		public String format(LogRecord record) {
	    			StringBuilder msg = new StringBuilder();
	    			msg.append(df.format(new Date(record.getMillis())));
	    			msg.append(" [");
	    			msg.append(record.getLevel().toString());
	    			msg.append("] ");
	    			if(record.getThrown() != null){
	    				if(record.getMessage() != null)
	    					msg.append(formatMessage(record)).append(": ");
	    				ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    				record.getThrown().printStackTrace(new PrintStream(baos));
	    				msg.append(baos.toString());
	    			}else{
	    				msg.append(formatMessage(record));
	    			}
	    			msg.append('\n');
	    			return msg.toString();
	    		}
	    	};
	    }else {
	    	formatter = new Formatter(){
	    		public String format(LogRecord record) {
	    			StringBuilder msg = new StringBuilder();
	    			if(record.getThrown() != null){
	    				if(record.getMessage() != null)
	    					msg.append(formatMessage(record)).append(": ");
	    				ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    				record.getThrown().printStackTrace(new PrintStream(baos));
	    				msg.append(baos.toString());
	    			}else{
	    				msg.append(formatMessage(record));
	    			}
	    			msg.append('\n');
	    			return msg.toString();
	    		}
	    	};
	    }

        if (optionalOutputFile == null) {
            logger.addHandler(new Handler() {
                public void publish(LogRecord record) {
                    if(record.getLevel().intValue() >= level.intValue())
                        System.out.print(formatter.format(record));
                }
                public void flush(){}
                public void close(){}
            });
        } else {
            System.out.println("Logging into " + optionalOutputFile);
            try {
                FileUtils.deleteQuietly(optionalOutputFile);
                FileUtils.mkDirsForFile(optionalOutputFile);
				FileHandler fileHandler = new FileHandler(optionalOutputFile.getPath());
                fileHandler.setFormatter(formatter);
                fileHandler.setLevel(level);
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        logger.setLevel(level);
	}

	public static synchronized void info(Object message) {
		getLogger().info(message.toString());
	}

	public static synchronized void fine(Object message) {
		getLogger().fine(message.toString());
	}

	public static synchronized void finer(Object message) {
		getLogger().finer(message.toString());
	}

	public static synchronized void finest(Object message) {
		getLogger().finest(message.toString());
	}

	public static synchronized void warn(Object message) {
        getLogger().warning(message.toString());
    }

    public static synchronized void severe(Object message) {
        getLogger().severe(message.toString());
    }

    public static synchronized void severe(Throwable t) {
        //String msg = t.getMessage();
        //if(msg == null) msg = ExceptionUtils.getRootCauseMessage(t);
        //getLogger().log(Level.SEVERE, msg, t);
        getLogger().log(Level.SEVERE, null, t);
    }

    public static void severe(String msg, Throwable t) {
        getLogger().log(Level.SEVERE, msg, t);
    }

    public static boolean acceptsFine() {
        return getLogger().isLoggable(Level.FINE);
    }

//    public static void main(String[] args) {
//        Logs.severe("a");
//        Logs.severe(new RuntimeException());
//        Logs.severe(new RuntimeException("b"));
//        Logs.severe("d", new RuntimeException());
//        Logs.severe("d", new RuntimeException("e"));
//        Logs.severe(new RuntimeException(new IllegalStateException(new IllegalArgumentException("internal"))));
//        Logs.severe("int", new RuntimeException(new IllegalStateException(new IllegalArgumentException("internal"))));
//    }
}
