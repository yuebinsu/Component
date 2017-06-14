package priv.syb.updated.Inferface;

import priv.syb.updated.bean.UpdateInfo;

/**
 * Created by：Administrator on 2017/6/13 15:44
 * 619389279@qq.com
 */
public interface IUpdateParser {
    UpdateInfo parse(String source) throws Exception;

}
