package priv.syb.updated.Inferface;


import android.content.DialogInterface;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import priv.syb.updated.DownloadResponseHandler;
import priv.syb.updated.UpdateDownloadRequest;


/**
 * Created by：su on 2017/6/15 17:44
 * 619389279@qq.com
 */
public class DialogClickListener implements DialogInterface.OnClickListener, UpdatedDownloadListener {
    private String downloadUrl;
    private String localPath;

    public DialogClickListener(String downloadUrl, String localPath) {
        this.downloadUrl = downloadUrl;
        this.localPath = localPath;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:

                break;
            case DialogInterface.BUTTON_NEUTRAL:
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }

    private void downLoad() {
        UpdateDownloadRequest request = new UpdateDownloadRequest(downloadUrl, localPath, this);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        Future<?> future = threadPoolExecutor.submit(request);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onProgressChanged(int progress) {

    }

    @Override
    public void onFinished(float completeSize) {

    }

    @Override
    public void onFailure(DownloadResponseHandler.FailureCode code, Exception e) {

    }
}