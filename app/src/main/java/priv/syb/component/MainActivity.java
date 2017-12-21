package priv.syb.component;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import priv.syb.updated.DownloadResponseHandler;
import priv.syb.updated.Inferface.UpdatedDownloadListener;
import priv.syb.updated.UpdateDownloadRequest;
import priv.syb.updated.UpdateManager;
import priv.syb.updated.util.StorageUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;
    private Button button2;
    private Button btn_nfc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        btn_nfc = (Button) findViewById(R.id.btn_nfc);
        btn_nfc.setOnClickListener(this);
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
            case R.id.button2:
                new UpdateManager.Builder(MainActivity.this)
                        .setTitle("版本更新")
                        .setCancelable(false).setCanceledOnTouchOutside(false)
                        .setContent("• 支持文字、贴纸、背景音乐，尽情展现欢乐气氛；\n• 两人视频通话支持实时滤镜，丰富滤镜，多彩心情；\n• 图片编辑新增艺术滤镜，一键打造文艺画风；\n• 资料卡新增点赞排行榜，看好友里谁是魅力之王。")
                        .setPackageSize("12.8MB")
                        .setTargetVersion("v6.92")
                        .setUpdateType(UpdateManager.UpdateType.Force)
                        .build();
                break;
            case R.id.btn_nfc:
                startActivity(new Intent(MainActivity.this, NfcActivity.class));
                break;
        }
    }
}
