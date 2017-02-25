package com.fussroll.fussroll;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kunal on 2/1/17.
 */

class CategoriesArrayAdapter extends BaseAdapter {

    private final String[] logs;
    private final int[] imageIDs;
    private final Activity context;

    CategoriesArrayAdapter(Activity context, String[] logs, int[] imageIDs) {
        //super(context, R.layout.logs_row, logs);
        this.context = context;
        this.logs = logs;
        this.imageIDs = imageIDs;
    }

    @Override
    public int getCount() {
        return logs.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(convertView == null)
            view = context.getLayoutInflater().inflate(R.layout.categories_row, null);

        TextView textView = (TextView) view.findViewById(R.id.textView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        textView.setText(logs[position]);
        imageView.setImageResource(imageIDs[position]);

        return view;
    }
}
