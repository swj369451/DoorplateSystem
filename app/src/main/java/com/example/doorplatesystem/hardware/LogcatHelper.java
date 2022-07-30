package com.example.doorplatesystem.hardware;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * log日志统计保存
 * 日志最多保存3天，如果内存不足，逐个删除近3+i 天日志
 *
 * @author songzhenzhen
 */

public class LogcatHelper {
    private static LogcatHelper instance = null;
    private int order = 0;
    private int mPId;
    private String pathLogcat;
    private LogThread mLogThread = null;
    private String mylogfilename = ".log";
    private int SDCARD_LOG_FILE_SAVE_DAYS = 3;
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

   
    public void init(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            pathLogcat = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "HN_Log";
        } else {
			pathLogcat = context.getFilesDir().getAbsolutePath() + File.separator + "HN_Log";
        }
        File file = new File(pathLogcat);
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (availableSpace()) {
                delFile(order);
            } else {
                deleteDirectory(pathLogcat);
            }
        }
    }

  
    private boolean availableSpace() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
       
        long totalBlocks = blockSize * blockCount / 1024;
      
        long availableBlocks = availCount * blockSize / 1024;
        if (availableBlocks < totalBlocks) {
            return true;
        } else {
            return false;
        }
    }

    public static LogcatHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LogcatHelper(context);
        }
        return instance;
    }

    private LogcatHelper(Context context) {
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        if (mLogThread == null)
            mLogThread = new LogThread(String.valueOf(mPId), pathLogcat);
        mLogThread.start();
    }

    public void stop() {
        if (mLogThread != null) {
            mLogThread.stopLogs();
            mLogThread = null;
        }
    }

    private class LogThread extends Thread {
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogThread(String pid, String dir) {
            mPID = pid;
            try {
                out = new FileOutputStream(new File(dir, MyDate.getFileName() + mylogfilename));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
//            cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
//            cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
            cmds = "logcat  | grep \"()\"";//打印所有日志信息

        }

        public void stopLogs() {
            mRunning = false;
            this.interrupt();
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!availableSpace()) {
                        delFile(order);
                    }
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((MyDate.getDateEN() + "  " + line + "\n").getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

    public static class MyDate {
        public static String getFileName() {
            String date = logfile.format(new Date(System.currentTimeMillis()));
            return date;
        }

        public static String getDateEN() {
            String date1 = myLogSdf.format(new Date(System.currentTimeMillis()));
            return date1;
        }
    }

    public void delFile(int order) {
        try {
            String needDelFiel = logfile.format(getDateBefore(order));
            needDelFiel = needDelFiel.substring(0, 8);
            int needDelTime = Integer.parseInt(needDelFiel);

            File dirFile = new File(pathLogcat);
            if (dirFile.exists() || dirFile.isDirectory()) {
                File[] files = dirFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        String fileName = files[i] + "";
                        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
                        fileName = fileName.substring(0, 8);
                        int filetime = Integer.parseInt(fileName);
                        if (filetime <= needDelTime) {
                            files[i].delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   
    private Date getDateBefore(int order) {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS + order);
        order++;
        if (order == SDCARD_LOG_FILE_SAVE_DAYS) {
            order = 0;
        }
        return now.getTime();
    }

    public void deleteDirectory(String filePath) {
      
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (dirFile.exists() || dirFile.isDirectory()) {
            File[] files = dirFile.listFiles();
           
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                 
                    files[i].delete();
                }
            }
        }
        dirFile.delete();
    }
}

