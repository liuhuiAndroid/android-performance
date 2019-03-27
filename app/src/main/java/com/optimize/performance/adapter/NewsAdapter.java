package com.optimize.performance.adapter;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.optimize.performance.R;
import com.optimize.performance.bean.NewsItem;
import com.optimize.performance.net.ConfigManager;
import com.optimize.performance.utils.LaunchTimer;
import com.optimize.performance.utils.LogUtils;
import com.optimize.performance.wakelock.WakeLockUtils;

import java.util.List;

import top.zibin.luban.Luban;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> mItems;
    private boolean mHasRecorded;
    private OnFeedShowCallBack mCallBack;

    public NewsAdapter(List<NewsItem> items) {
        this.mItems = items;
    }

    public void setItems(List<NewsItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public void setOnFeedShowCallBack(OnFeedShowCallBack callBack) {
        this.mCallBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item_constrainlayout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (position == 0 && !mHasRecorded) {
            mHasRecorded = true;
            holder.layout.getViewTreeObserver()
                    .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            holder.layout.getViewTreeObserver().removeOnPreDrawListener(this);
                            LogUtils.i("FeedShow");
                            LaunchTimer.endRecord("FeedShow");
                            if (mCallBack != null) {
                                mCallBack.onFeedShow();
                            }
                            return true;
                        }
                    });
        }

        NewsItem newsItem = mItems.get(position);

        // 以下代码是为了演示字符串的拼接
        String msgOld = newsItem.title + newsItem.targetId;// 原有方式

        StringBuilder builder = new StringBuilder();
        builder.append(newsItem.title)
                .append(newsItem.targetId);// 建议使用方式，不要小看这点优化
        String msgNew = builder.toString();

        holder.textView.setText(newsItem.title);
        Uri uri = Uri.parse(newsItem.imgurl);
        holder.imageView.setImageURI(uri);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ConfigManager.sOpenClick模拟的是功能的开关
                if(ConfigManager.sOpenClick){
                    // 此处模拟的是WakeLock使用的兜底策略
                    WakeLockUtils.acquire(holder.imageView.getContext());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WakeLockUtils.release();
                        }
                    },200);
                }
                // 以下代码是为了演示Luban这个库对图片压缩对流量方面的影响
//                Luban.with(holder.imageView.getContext())
//                        .load(Environment.getExternalStorageDirectory()+"/Android/1.jpg")
//                        .setTargetDir(Environment.getExternalStorageDirectory()+"/Android")
//                        .launch();

                // 以下代码是为了演示解决过度绘制问题，可以换成解决内存抖动等方面的代码
//                Intent intent = new Intent(holder.imageView.getContext(), SolveOverDrawActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                holder.imageView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        SimpleDraweeView imageView;
        ConstraintLayout layout;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tv_title);
            imageView = view.findViewById(R.id.iv_news);
            layout = view.findViewById(R.id.ll_out);
        }
    }

}
