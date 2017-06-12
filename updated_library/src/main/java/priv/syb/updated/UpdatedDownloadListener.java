package priv.syb.updated;

/**
 * Created by：su on 2017/6/12 09:44
 * 619389279@qq.com
 */
public interface UpdatedDownloadListener {

    void onStart();

    void onProgressChanged(int progress);

    void onFinished(float completeSize);

    void onFailure( DownloadResponseHandler.FailureCode code,Exception e);

}
