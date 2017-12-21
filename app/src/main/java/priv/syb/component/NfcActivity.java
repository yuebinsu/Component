package priv.syb.component;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import priv.syb.component.adapter.DateRcvAdapter;
import priv.syb.component.nfc.CardCmd;
import priv.syb.component.nfc.CardListener;
import priv.syb.component.nfc.CardManager;
import priv.syb.component.utils.log.L;

import static android.nfc.NfcAdapter.EXTRA_TAG;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

public class NfcActivity extends AppCompatActivity implements CardListener, View.OnClickListener {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private static String[][] TECHLISTS;
    private static IntentFilter[] TAGFILTERS;
    private RecyclerView rec_list;
    private EditText et_send_mes;
    private Button btn_send;
    private DateRcvAdapter dateRcvAdapter;
    private CardCmd.CMDTYPE cmdtype = CardCmd.CMDTYPE.SEND_CARD_CMD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        initView();
        initNfc();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        L.d("nfc connet");
        if (intent != null) {
            final Tag tag = (Tag) intent.getParcelableExtra(EXTRA_TAG);
            L.d("APDUDemo,cardOperation,tag=" + tag);
            if (tag != null) {
                String editText = et_send_mes.getText().toString().trim();

                    CardManager.cardOperation(NfcActivity.this, tag, cmdtype, editText, this);
                    dateRcvAdapter.snedMes(editText);
                    et_send_mes.setText("");

            }
        }
    }

    @Override
    public void onReadEvent(Object event, Object... obj) {
        L.d(event.toString() + "---" + obj.toString());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(event);
        dateRcvAdapter.retMes(event.toString());
        for (int i = 0; i < obj.length; i++) {
            //stringBuffer.append("\n");
            // stringBuffer.append(new Gson().toJson(obj[i]));
        }
        dateRcvAdapter.retMes(stringBuffer.toString().replace(",","\n"));
    }

    @Override
    public void onReadComplete(Object... objects) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < objects.length; i++) {
            stringBuffer.append("\n");
            stringBuffer.append(objects[i]);
        }
        dateRcvAdapter.retMes(stringBuffer.toString().replace(",","\n"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                    TAGFILTERS, TECHLISTS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
                this, this.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        setupBeam(true);
        try {
            TECHLISTS = new String[][]{{IsoDep.class.getName()},
                    {NfcF.class.getName()},};

            TAGFILTERS = new IntentFilter[]{new IntentFilter(
                    NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};
        } catch (Exception e) {
        }
    }

    @SuppressLint("NewApi")
    private void setupBeam(boolean enable) {

        final int api = Build.VERSION.SDK_INT;
        if (nfcAdapter != null && api >= ICE_CREAM_SANDWICH) {
            if (enable)
                nfcAdapter.setNdefPushMessage(createNdefMessage(), this);
        }
    }

    NdefMessage createNdefMessage() {

        String uri = "3play.google.com/store/apps/details?id=com.hna.ykt.app";
        byte[] data = uri.getBytes();

        // about this '3'.. see NdefRecord.createUri which need api level 14
        data[0] = 3;

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_URI, null, data);

        return new NdefMessage(new NdefRecord[]{record});
    }

    private void initView() {
        rec_list = (RecyclerView) findViewById(R.id.rec_list);
        et_send_mes = (EditText) findViewById(R.id.et_send_mes);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        dateRcvAdapter = new DateRcvAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rec_list.setLayoutManager(layoutManager);
        rec_list.setAdapter(dateRcvAdapter);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("读卡测试");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_format_list_bulleted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_send_cmd:
                        cmdtype = CardCmd.CMDTYPE.SEND_CARD_CMD;
                        et_send_mes.setVisibility(View.VISIBLE);
                        toolbar.setTitle(item.getTitle());
                        break;
                    case R.id.item_read_card_info:
                        cmdtype = CardCmd.CMDTYPE.READ_CARD_INFO;
                        et_send_mes.setVisibility(View.GONE);
                        toolbar.setTitle(item.getTitle());
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                submit();
                break;
        }
    }

    private void submit() {
        // validate
        String editText = et_send_mes.getText().toString().trim();
        if (TextUtils.isEmpty(editText)) {
            Toast.makeText(this, "editText不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        dateRcvAdapter.snedMes(editText);
        final Tag tag = (Tag) getIntent().getParcelableExtra(EXTRA_TAG);
        CardManager.cardOperation(this, tag, cmdtype, editText, this);
    }
}
