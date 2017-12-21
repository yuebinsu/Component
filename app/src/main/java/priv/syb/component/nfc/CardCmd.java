package priv.syb.component.nfc;

import android.content.Context;

/**
 * Created by：Administrator on 2017/12/18 09:43
 * 619389279@qq.com
 */
public class CardCmd {
    public CARDTYPE cardType;

    public CMDTYPE cmdType;

    public CHARGESTATUS chargeStatus;

    public enum CMDTYPE {
        READ_CARD_INFO,
        SEND_CARD_CMD,
        WRITE_CARD_INIT,
        WRITE_CARD_MONEY,
        READ_CARD_CASH_ONLY
    }

    public enum CHARGESTATUS {
        READ_CARD_CASH,
        WRITE_CARD_INIT,
        WRITE_CARD_MONEY,
        READ_CARD_CASH_ONLY
    }


    public enum CARDTYPE{
        UNKNOWNCITY("未知卡类型"),
        HN_BaoDaoTong(""),;

        private final String cardStr;
        CARDTYPE(String cardStr) {
            this.cardStr = cardStr;
        }
    }
}
