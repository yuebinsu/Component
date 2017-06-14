package priv.syb.updated;


import java.io.IOException;

import priv.syb.updated.Inferface.UpdatedDownloadListener;

/**
 * Created by：su on 2017/6/12 10:00
 * 619389279@qq.com
 */
public class UpdateDownloadRequest implements Runnable {
    private String localPath;
    private boolean isDownloading = false;

    private DownloadResponseHandler downloadResponseHandler;

    public UpdateDownloadRequest(String downloadUrl, String localPath, UpdatedDownloadListener listener) {
        this.localPath = localPath;
        this.isDownloading = true;
        downloadResponseHandler = new DownloadResponseHandler(downloadUrl, listener);
    }

    private void makeRequest() throws IOException {

        if (!Thread.currentThread().isInterrupted()) {
            downloadResponseHandler.sendResponseMessage(localPath);
        }
    }

    @Override
    public void run() {

        try {
            makeRequest();
        } catch (IOException ignored) {

        }
    }


}
