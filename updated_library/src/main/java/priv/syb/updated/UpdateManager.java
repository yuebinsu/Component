package priv.syb.updated;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import priv.syb.updated.Inferface.IUpdateParser;

/**
 * Created by：Administrator on 2017/6/13 11:20
 * 619389279@qq.com
 */
public class UpdateManager {
    private Activity mActivity;
    private AlertDialog mAlertDialog;
    private String mTitle = "提示";
    private UpdateType mUpdateType;
    private String mContent;
    private boolean canceledOnOutside = true;
    private boolean cancelable = true;

    public UpdateManager(Activity activity) {
        mActivity = activity;

    }

    private void createDialog() {
        //类型为不更新则不提示
        if (mAlertDialog == null && mUpdateType != UpdateType.NotUpdate && !mActivity.isFinishing()) {
            mAlertDialog = new AlertDialog.Builder(mActivity).create();
            mAlertDialog.setTitle(mTitle);
            float density = mActivity.getResources().getDisplayMetrics().density;
            TextView tv = new TextView(mActivity);
            tv.setMovementMethod(new ScrollingMovementMethod());
            tv.setVerticalScrollBarEnabled(true);
            tv.setTextSize(14);
            tv.setMaxHeight((int) (250 * density));
            mAlertDialog.setView(tv, (int) (25 * density), (int) (15 * density), (int) (25 * density), 0);
            if (mUpdateType == UpdateType.Force) {
                mAlertDialog.setCancelable(false);
                mAlertDialog.setCanceledOnTouchOutside(false);
            } else {
                mAlertDialog.setCancelable(cancelable);
                mAlertDialog.setCanceledOnTouchOutside(canceledOnOutside);
            }
        }
    }


    public void setFouceType() {

    }

    public static class Builder {
        private UpdateManager mManager;
        private Activity mActivity;

        public Builder(Activity activity) {
            mActivity = activity;
        }

        public Builder setTitle(@Nullable String title) {
            mManager.mTitle = title;
            return this;
        }

        public void setTitle(@StringRes int title) {
            mManager.mTitle = mActivity.getString(title);
        }

        public Builder setUpdateType(UpdateType type) {
            mManager.mUpdateType = type;
            return this;
        }

        public Builder setContent(@Nullable String content) {
            mManager.mContent = content;
            return this;
        }

        public Builder setContent(@StringRes int content) {
            mManager.mContent = mActivity.getString(content);
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean cancel) {
            mManager.canceledOnOutside = cancel;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mManager.cancelable = cancelable;
            return this;
        }


        public UpdateManager build() {
            return new UpdateManager(mActivity);
        }


    }

    private enum UpdateType {
        Force, Optional, NotUpdate
    }
}
