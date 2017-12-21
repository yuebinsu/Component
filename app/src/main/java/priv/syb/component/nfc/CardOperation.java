package priv.syb.component.nfc;

import android.nfc.tech.IsoDep;
import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import priv.syb.component.utils.log.L;

/**
 * Created by：Administrator on 2017/12/18 09:14
 * 619389279@qq.com
 */
public class CardOperation {
    protected final static int SFI_EXTRA = 21;

    public Object cardOperationInto(CardCmd.CMDTYPE cmdtype, IsoDep isoDep, String strCmd) {
        Object response = null;
        switch (cmdtype) {
            case READ_CARD_INFO:
                L.d("READ_CARD_INFO");
                response = readCardInfo(isoDep);
                break;

            case SEND_CARD_CMD:
                L.d("SEND_CARD_CMD");
                response = sendCmdToCard(isoDep, strCmd);
                break;
        }
        return response;
    }

    private Object readCardInfo(IsoDep isoDep) {
        final Iso7816.Tag isoTag = new Iso7816.Tag(isoDep);
        Map<String, Object> stringMap = new HashMap<>();
        try {
            isoTag.connect();
            isoTag.getATR();
            //选取目录状态,选取钱包状态不为正常，
            if (!isoTag.SelectMainDir().isOkey() || !isoTag.SelectEP().isOkey()) {
                L.d("%s,doReadCard into, error init ");
                stringMap.put("读取卡数据", "失败");
                stringMap.put("原因", !isoTag.SelectMainDir().isOkey() ? "选取目录状态失败" : !isoTag.SelectEP().isOkey() ? "选取钱包状态失败" : "其他");
                return null;
            }
            Iso7816.Response info = isoTag.readBinary(SFI_EXTRA);
            L.d("%s,doReadCard info=" + info);
            stringMap.put("读取SFI指定的二进制文件数据", info);
            Iso7816.Response cardNo = isoTag.getCardno();
            L.d("cardNo" +cardNo.getBytes());
            stringMap.put("获取卡号", cardNo);
            Iso7816.Response balance = isoTag.getBalance(true);
            stringMap.put("读取电子钱包余额", balance);
            Iso7816.Response cardInfo = isoTag.getCardInfo();
            stringMap.put("cardInfo", cardInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringMap;
    }

    private Object sendCmdToCard(IsoDep isoDep, String cmd) {
        final Iso7816.Tag isoTag = new Iso7816.Tag(isoDep);
        Map<String, Object> objectMap = new HashMap<>();
        try {
            isoTag.connect();
            isoTag.getATR();
            byte[] bcmd = new byte[256];
            if (TextUtils.isEmpty(cmd)) {
                objectMap.put("异常,", "发送数据为空");
                return objectMap;
            }
            Util.AsciiToHex(cmd.getBytes(), bcmd, cmd.length());
            Iso7816.Response response = new Iso7816.Response(isoTag.transceive(bcmd));
            L.d("Iso7816 response:" + response);
            objectMap.put("Iso7816 response:", response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectMap;
    }
}
