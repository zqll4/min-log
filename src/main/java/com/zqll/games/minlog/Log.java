package com.zqll.games.minlog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;

public class Log {
    public static final int LEVEL_NONE = 6;
    public static final int LEVEL_ERROR = 5;
    public static final int LEVEL_WARN = 4;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_TRACE = 1;
    private static int level = LEVEL_DEBUG;
    private static boolean ERROR;
    private static boolean WARN;
    private static boolean INFO;
    private static boolean DEBUG;
    private static boolean TRACE;
    private static Logger logger;

    private Log() {
    }

    static {
        set(level);
        logger = new Logger();
    }

    public static void set(int level) {
        Log.level = level;
        ERROR = level <= LEVEL_ERROR;
        WARN = level <= LEVEL_WARN;
        INFO = level <= LEVEL_INFO;
        DEBUG = level <= LEVEL_DEBUG;
        TRACE = level <= LEVEL_TRACE;
    }

    public static void NONE() {
        set(LEVEL_NONE);
    }

    public static void ERROR() {
        set(LEVEL_ERROR);
    }

    public static void WARN() {
        set(LEVEL_WARN);
    }

    public static void INFO() {
        set(LEVEL_INFO);
    }

    public static void DEBUG() {
        set(LEVEL_DEBUG);
    }

    public static void TRACE() {
        set(LEVEL_TRACE);
    }

    public static void setLogger(Logger logger) {
        Log.logger = logger;
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        // 动态替换 `{}` 为 `{0}`, `{1}`, `{2}`, 等
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Object[]) {
                args[i] = Arrays.toString((Object[]) args[i]);
            } else {
                args[i] = String.valueOf(args[i]);
            }
            message = message.replaceFirst("\\{\\}", "{" + i + "}");
        }

        // 使用 MessageFormat 格式化最终的字符串
        return MessageFormat.format(message, args);
    }

    public static void error(String message, Throwable ex, Object... args) {
        if (ERROR) {
            logger.log(LEVEL_ERROR, format(message, args), ex);
        }
    }

    public static void error(String message, Object... args) {
        if (ERROR) {
            logger.log(LEVEL_ERROR, format(message, args), null);
        }
    }

    public static void warn(String message, Throwable ex, Object... args) {
        if (WARN) {
            logger.log(LEVEL_WARN, format(message, args), ex);
        }
    }

    public static void warn(String message, Object... args) {
        if (WARN) {
            logger.log(LEVEL_WARN, format(message, args), null);
        }
    }

    public static void info(String message, Throwable ex, Object... args) {
        if (INFO) {
            logger.log(LEVEL_INFO, format(message, args), ex);
        }
    }

    public static void info(String message, Object... args) {
        if (INFO) {
            logger.log(LEVEL_INFO, format(message, args), null);
        }
    }

    public static void debug(String message, Throwable ex, Object... args) {
        if (DEBUG) {
            logger.log(LEVEL_DEBUG, format(message, args), ex);
        }
    }

    public static void debug(String message, Object... args) {
        if (DEBUG) {
            logger.log(LEVEL_DEBUG, format(message, args), null);
        }
    }

    public static void trace(String message, Throwable ex, Object... args) {
        if (TRACE) {
            logger.log(LEVEL_TRACE, format(message, args), ex);
        }
    }

    public static void trace(String message, Object... args) {
        if (TRACE) {
            logger.log(LEVEL_TRACE, format(message, args), null);
        }
    }

    public static class Logger {
        private final long firstLogTime = System.currentTimeMillis();

        // ANSI color codes for different log levels
        private static final String RESET = "\u001B[0m";
        private static final String RED = "\u001B[31m";
        private static final String YELLOW = "\u001B[33m";
        private static final String GREEN = "\u001B[32m";
        private static final String BLUE = "\u001B[34m";
        private static final String CYAN = "\u001B[36m";
        private static final StringBuilder builder = new StringBuilder(256);

        public Logger() {
        }

        public void log(int level, String message, Throwable ex) {
            builder.setLength(0);

            // 获取当前线程的堆栈信息
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            // 找到调用日志的类的位置（跳过 Log 和 Logger 类本身）
            int callerIndex = 0; // 默认从第四个元素开始是调用 log 方法的地方
            while (!stackTrace[callerIndex].getClassName().equals(Log.class.getName())) {
                callerIndex++; // 跳过 Logger 或 Log 类的堆栈元素
            }

            StackTraceElement logCaller = stackTrace[++callerIndex];  // 找到实际调用日志的地方
            String className = simpleClassName(logCaller);
            String methodName = logCaller.getMethodName();
            int lineNumber = logCaller.getLineNumber();  // 获取调用日志的行号

            // Get current thread name
            String threadName = Thread.currentThread().getName();

            // Apply color based on the log level
            String color;
            switch (level) {
                case LEVEL_TRACE:
                    color = CYAN;
                    builder.append("TRACE");
                    break;
                case LEVEL_DEBUG:
                    color = BLUE;
                    builder.append("DEBUG");
                    break;
                case LEVEL_INFO:
                    color = GREEN;
                    builder.append("INFO");
                    break;
                case LEVEL_WARN:
                    color = YELLOW;
                    builder.append("WARN");
                    break;
                case LEVEL_ERROR:
                    color = RED;
                    builder.append("ERROR");
                    break;
                default:
                    color = RESET;
                    builder.append("LOG");
            }

            // Prepend thread name, class, method, and line number to the log message
            builder.insert(0, String.format("%s[%s] [%s#%s:%d]: ", color, threadName, className, methodName, lineNumber));
            builder.append(": ");

            builder.append(message).append(RESET);  // Append the actual log message and reset color

            // Handle exception if present
            if (ex != null) {
                StringWriter writer = new StringWriter(256);
                ex.printStackTrace(new PrintWriter(writer));
                builder.append('\n').append(writer.toString().trim());
            }

            this.print(builder.toString());
        }

        protected void print(String message) {
            System.out.println(message);
        }

        protected String simpleClassName(StackTraceElement stackTraceElement) {
            String className = stackTraceElement.getClassName();
            return className.substring(className.lastIndexOf(".") + 1);
        }
    }
}