package priv.syb.updated;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by：Administrator on 2017/6/12 10:03
 * 619389279@qq.com
 */
public class DownloadResponseHandler {
    private UpdatedDownloadListener listener;
    private String downloadUrl;
    protected static final int SUCCESS_MESSAGE = 0;
    private static final int FAILURE_MESSAGE = 1;
    private static final int START_MESSAGE = 2;
    private static final int FINISH_MESSAGE = 3;
    protected static final int NETWORK_OFF = 4;
    private static final int PROGRESS_CHANGED = 5;

    private float completeSize = 0;
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K

    private Handler handler;

    DownloadResponseHandler(String url, UpdatedDownloadListener listener) {
        this.listener = listener;
        this.downloadUrl = url;
        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                handleSelfMessage(msg);
            }
        };

    }


    private void sendMessage(Message msg) {
        if (handler != null) {
            handler.sendMessage(msg);
        } else {
            handleSelfMessage(msg);
        }

    }

    private Message obtainMessage(int responseMessge, Object response) {
        Message msg = null;
        if (handler != null) {
            msg = handler.obtainMessage(responseMessge, response);
        } else {
            msg = Message.obtain();
            msg.what = responseMessge;
            msg.obj = response;
        }
        return msg;

    }

    private void handleSelfMessage(Message msg) {

        Object[] response;
        switch (msg.what) {
            case START_MESSAGE:
                onStart();
                break;
            case FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                handleFailureMessage((FailureCode) response[0], (Exception) response[1]);
                break;
            case PROGRESS_CHANGED:
                response = (Object[]) msg.obj;
                handleProgressChangedMessage((Integer) response[0]);
                break;
            case FINISH_MESSAGE:
                onFinish();
                break;
        }
    }

    private void sendonStartMessage() {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    private void sendFinishMessage() {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }

    private void sendProgressChangedMessage(int progress) {
        sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[]{progress}));

    }

    private void sendFailureMessage(Exception e, FailureCode failureCode) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode, e}));

    }

    private void onStart() {
        listener.onStart();
    }

    private void handleProgressChangedMessage(int progress) {
        listener.onProgressChanged(progress);
    }


    private void onFinish() {
        listener.onFinished(completeSize);

    }

    private void handleFailureMessage(FailureCode failureCode, Exception e) {
        onFailure(failureCode, e);

    }

    private void onFailure(FailureCode failureCode, Exception e) {
        listener.onFailure(failureCode, e);
    }

    void sendResponseMessage(String locadPath) {
        completeSize = 0;
        FileOutputStream out = null;
        InputStream is=null;
        sendonStartMessage();
        try {
            // long bytesum = 0;
            URL url = new URL(downloadUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();
            long currentLength = urlConnection.getContentLength();
            if (!Thread.currentThread().isInterrupted()) {
                is=urlConnection.getInputStream();
                int byteread = 0;
                String apkName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, downloadUrl.length());
                File apkFile = new File(locadPath, apkName);
                out = new FileOutputStream(apkFile);
                byte[] buffer = new byte[BUFFER_SIZE];

                int oldProgress = 0;

                while ((byteread = is.read(buffer)) != -1) {
                    completeSize += byteread;
                    out.write(buffer, 0, byteread);

                    int progress = (int) (completeSize * 100L / currentLength);
                    // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                    if (progress != oldProgress) {
                        sendProgressChangedMessage(progress);
                    }
                    oldProgress = progress;
                }
                sendFinishMessage();
            }
        } catch (UnknownHostException e) {

            sendFailureMessage(e, FailureCode.UnknownHost);

        }catch (SecurityException e){
            sendFailureMessage(e,FailureCode.MissPerMiss);
        }
        catch (ConnectException e) {

            sendFailureMessage(e, FailureCode.connectionTimeout);

        } catch (SocketException e) {

            sendFailureMessage(e, FailureCode.Socket);

        } catch (SocketTimeoutException e) {

            sendFailureMessage(e, FailureCode.SocketTimeout);

        } catch (FileNotFoundException e) {

            sendFailureMessage(e, FailureCode.FileNotFound);

        } catch (IOException e) {
            sendFailureMessage(e, FailureCode.IO);
        }catch (Exception e){
            sendFailureMessage(e,FailureCode.OtherEX);
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                sendFailureMessage(e, FailureCode.IO);
            }
        }
    }

    /**
     * 下载过程中的异常
     */
    public enum FailureCode {
        UnknownHost, Socket, SocketTimeout, connectionTimeout, IO, FileNotFound,MissPerMiss ,OtherEX

    }
}
