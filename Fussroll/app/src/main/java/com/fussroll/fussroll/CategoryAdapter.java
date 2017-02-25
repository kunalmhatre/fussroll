package com.fussroll.fussroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private LayoutInflater inflater;
    private String[] logs;
    private int[] imageIDs;
    private ClickListener clickListener;
    private int layout = 0;
    private Context context;

    CategoryAdapter(Context context, String[] logs, int[] imageIDs) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.logs = logs;
        this.imageIDs = imageIDs;
    }
    CategoryAdapter(Context context, String[] logs, int[] imageIDs, int layout) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.logs = logs;
        this.imageIDs = imageIDs;
        this.layout = layout;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(layout == 0)
            view = inflater.inflate(R.layout.logs_row, parent, false);
        else
            view = inflater.inflate(layout, parent, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Picasso.with(context).load(imageIDs[position]).into(holder.imageView);
        //holder.imageView.setImageResource(imageIDs[position]);
        holder.textView.setText(logs[position]);
    }

    @Override
    public int getItemCount() {
        return logs.length;
    }

    void setData(String[] logs, int[] imageIDs) {
        this.logs = logs;
        this.imageIDs = imageIDs;
    }

    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView;
        ImageView imageView;
        RelativeLayout relativeLayout;

        CategoryViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            relativeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(view, getLayoutPosition());
        }
    }

    interface ClickListener {
        void onItemClick(View view, int position);
    }
}
