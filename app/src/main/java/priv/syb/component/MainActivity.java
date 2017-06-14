package priv.syb.component;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import priv.syb.updated.DownloadResponseHandler;
import priv.syb.updated.util.StorageUtils;
import priv.syb.updated.UpdateDownloadRequest;
import priv.syb.updated.Inferface.UpdatedDownloadListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                UpdateDownloadRequest request = new UpdateDownloadRequest("http://xapps.yktcards.com/AppSys/package/app-release_20170525155340T4533.apk",
                        StorageUtils.getCacheDirectory(this).toString(), new UpdatedDownloadListener() {
                    @Override
                    public void onStart() {
                        Log.d("onStart", "onStart");
                    }

                    @Override
                    public void onProgressChanged(int progress) {
                        Log.d("onProgressChanged", progress + "");
                    }

                    @Override
                    public void onFinished(float completeSize) {
                        Log.d("onFinished", completeSize + "");
                    }

                    @Override
                    public void onFailure(DownloadResponseHandler.FailureCode code, Exception e) {
                        Log.d("onFinished", code + "---------" + e.toString());

                    }
                });
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                Future<?> future = threadPoolExecutor.submit(request);
                break;
        }
    }
}
