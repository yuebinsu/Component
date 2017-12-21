/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package priv.syb.component.nfc;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;

import priv.syb.component.utils.log.L;


public final class CardManager extends AsyncTask<Tag, Object, Object> {

    private CardListener mReaderListener;
    private Context mContext;
    private Bundle mBundle;
    private String strTrans;
    private CardCmd.CMDTYPE cmdtype;

    private CardManager(Context ct, CardCmd.CMDTYPE cmdtype, String strtrans, CardListener listener) {
        mReaderListener = listener;
        mContext = ct;
        // mBundle = bundle;
        this.cmdtype = cmdtype;
        this.strTrans = strtrans;
    }

    private CardManager(Context ct, String strtrans, CardListener listener) {
        mReaderListener = listener;
        mContext = ct;
        // mBundle = bundle;
        this.strTrans = strtrans;
    }

    @Override
    protected Object doInBackground(Tag... detectedTag) {
        return readCard((detectedTag == null || detectedTag.length == 0) ? null : (detectedTag[0]));
    }

    @Override
    protected void onProgressUpdate(Object... events) {
        if (mReaderListener != null)
            mReaderListener.onReadEvent(events[0], events, events);
    }

    @Override
    protected void onPostExecute(Object data) {
        L.d(data != null ? data.toString() : null);
        if (mReaderListener != null)
            mReaderListener.onReadComplete(data);
    }

    public static void cardOperation(Context ct, Tag tag, CardCmd.CMDTYPE cmdtype, String strTrans, CardListener listener) {
        new CardManager(ct, cmdtype,strTrans, listener).execute(tag);
    }

    public static void cardOperation(Context ct, String strTrans, CardListener listener) {
        new CardManager(ct, strTrans, listener).execute();
    }

    private Object readCard(Tag tag) {
        Object data = null;
        L.d("%s,into ReaderManager readCard,tag=" + tag);
        try {
            publishProgress("readLoading card");
            if (tag != null) {
                // L.d("%s,ID = " + NfcUtils.toHexString(tag.getId()), NfcUtils.NFC_TAG);
            }
            IsoDep isodep = null;
            if (tag != null) {
                isodep = IsoDep.get(tag);
                CardOperation cardOperation = new CardOperation();
                data = cardOperation.cardOperationInto(cmdtype, isodep, strTrans);
            }
            L.d("%s,isodep = " + isodep);
            publishProgress("read card complete");
        } catch (Exception e) {
            L.w("%s,--readCard, e = " + e);
            publishProgress("readCard error");
        }

        return data;
    }
/*    private Object readCard(Tag tag) {
        Object data = null;
        L.d("%s,into ReaderManager readCard,tag=" + tag);
        try {
            publishProgress("readLoading card");
            if (tag != null) {
                // L.d("%s,ID = " + NfcUtils.toHexString(tag.getId()), NfcUtils.NFC_TAG);
            }
            IsoDep isodep = null;
            if (tag != null) {
                isodep = IsoDep.get(tag);
                final Iso7816.Tag isoTag = new Iso7816.Tag(isodep);
                isoTag.connect();
                isoTag.getATR();
                //选择到主应用目录--3F00 选择电子钱包是否成功
//                if (!isoTag.SelectMainDir().isOkey()||!isoTag.SelectEP().isOkey()){
//                    L.d("%s,doReadCard into, error init ");
//                    publishProgress("readCard error");
//                    return null;
//                }
                byte[] bcmd = new byte[256];
                Util.AsciiToHex(strTrans.getBytes(), bcmd, strTrans.length());

                Iso7816.Response response = new Iso7816.Response(isoTag.transceive(bcmd));
                L.d("Iso7816 response:" + response);
                data = response;
                L.d("Iso7816 response.getBytes:" + response.getBytes());
            }
            L.d("%s,isodep = " + isodep);
            publishProgress("read card complete");
        } catch (Exception e) {
            L.w("%s,--readCard, e = " + e);
            publishProgress("readCard error");
        }

        return data;
    }*/
}


//class retval
//{
//    boolean isok;
//    byte
//}
//
//bool sendAsciiCmd(string strTrans , byte [] )
//{
//    byte[] bcmd = new byte[256];
//    Util.AsciiToHex(strTrans.getBytes(), bcmd, strTrans.length());
//    Iso7816.Response response = new Iso7816.Response(isoTag.transceive(bcmd));
//    if (response.isOkey())
//    {
//        return true;
//    }
//    return false;
//}
