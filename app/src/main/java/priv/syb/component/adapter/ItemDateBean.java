package priv.syb.component.adapter;

import java.io.Serializable;

/**
 * Created by：Administrator on 2017/6/30 15:24
 * 619389279@qq.com
 */
public class ItemDateBean implements Serializable {
    private int dateType;
    private String content;
    private String dateLengh;
    public static final int SEND_TEXT_TYPE = 1;
    public static final int RETURN_ALEXA_TYPE = 2;

    public int getDateType() {
        return dateType;
    }

    public void setDateType(int dateType) {
        this.dateType = dateType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateLengh() {
        return dateLengh;
    }

    public void setDateLengh(String dateLengh) {
        this.dateLengh = dateLengh;
    }
}
