package priv.syb.component.utils.log;

import android.util.Log;

/**
 * Created by Bob on 2016/7/2 08:49
 * SkySeraph00@126.com
 */
public class L {

    public static void v(String message, Object...args) {
        boolean canPrint = LogManager.canPrint(LogManager.LOG_LEVEL_VERBOSE);
        if (canPrint) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.v(getTag(), message);
            }
        }
    }

    public static void d(String message, Object...args) {
        boolean canPrint = LogManager.canPrint(LogManager.LOG_LEVEL_DEBUG);
        if (canPrint) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.d(getTag(), message);
            }
        }
    }

    public static void i(String message, Object...args) {
        boolean canPrint = LogManager.canPrint(LogManager.LOG_LEVEL_INFO);
        if (canPrint) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.i(getTag(), message);
            }
        }
    }

    public static void w(String message, Object...args) {
        boolean canPrint = LogManager.canPrint(LogManager.LOG_LEVEL_WARN);
        if (canPrint) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.w(getTag(), message);
            }
        }
    }
    
    public static void w(Throwable e) {
        if (LogManager.canPrint(LogManager.LOG_LEVEL_WARN)) {
            Log.w(getTag(), "warning", e);
        }
    }

    public static void e(String message, Object...args) {
        boolean canPrint = LogManager.canPrint(LogManager.LOG_LEVEL_ERROR);
        if (canPrint) {
            message = formatMessage(message, args);
            if (canPrint) {
                Log.e(getTag(), message);
            }
        }
    }
    
    public static void e(Throwable e) {
        if (LogManager.canPrint(LogManager.LOG_LEVEL_ERROR)) {
            Log.e(getTag(), "error", e);
        }
    }
    
    private static String formatMessage(String message, Object...args) {
        if (message == null) {
            return "";
        }
        if (args != null && args.length > 0) {
            try {
                return String.format(message, args);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return message;
    }

    /**
     * 获取native日志tag
     * @return
     */
    private static String getTag() {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        String className = stackTrace.getClassName();

        StringBuffer sb = new StringBuffer();
        sb.append(className.substring(className.lastIndexOf('.') + 1));
        sb.append("").append(stackTrace.getMethodName());
        sb.append("#").append(stackTrace.getLineNumber());

        return sb.toString();
    }
}
