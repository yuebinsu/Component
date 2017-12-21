package priv.syb.component.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import priv.syb.component.R;

/**
 * Created by：Administrator on 2017/6/30 11:21
 * 619389279@qq.com
 */
public class DateRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<ItemDateBean> mItemDateBeen;
    private int SEND_TEXT = 1;
    private int ALEXA_Return = 2;

    public DateRcvAdapter() {
        mItemDateBeen = new ArrayList<>();
    }

    public void addItem(ItemDateBean dateBean) {
        mItemDateBeen.add(dateBean);
        notifyDataSetChanged();
    }
    public void snedMes(String mes){
        ItemDateBean dateBean=new ItemDateBean();
        dateBean.setDateType(ItemDateBean.SEND_TEXT_TYPE);
        dateBean.setContent(mes);
        mItemDateBeen.add(dateBean);
        notifyDataSetChanged();
    }
    public void retMes(String mes){
        ItemDateBean dateBean=new ItemDateBean();
        dateBean.setDateType(ItemDateBean.RETURN_ALEXA_TYPE);
        dateBean.setDateLengh(mes);
        mItemDateBeen.add(dateBean);
        notifyDataSetChanged();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SEND_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_send, null);
            return new SendViewHolder(view);
        } else if (viewType == ALEXA_Return) {
            View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_response, null);
            return new ReturnViewHolder(view2);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SendViewHolder) {
            ((SendViewHolder) holder).tv_sendText.setText(mItemDateBeen.get(position).getContent());
        } else if (holder instanceof ReturnViewHolder) {
            String countLengh = mItemDateBeen.get(position).getDateLengh();
            ((ReturnViewHolder) holder).tv_alexa_return.setText(countLengh);
        }
    }

    @Override
    public int getItemCount() {
        return mItemDateBeen.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItemDateBeen.get(position).getDateType();
    }


    public class SendViewHolder extends RecyclerView.ViewHolder {
        TextView tv_sendText;

        SendViewHolder(View rootView) {
            super(rootView);
            this.tv_sendText = (TextView) rootView.findViewById(R.id.tv_sendText);
        }

    }

    public class ReturnViewHolder extends RecyclerView.ViewHolder {
        TextView tv_alexa_return;

        ReturnViewHolder(View rootView) {
            super(rootView);
            this.tv_alexa_return = (TextView) rootView.findViewById(R.id.tv_alexa_return);
        }

    }
}
