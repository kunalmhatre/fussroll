package com.fussroll.fussroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by kunal on 26/1/17.
 */

class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivitiesViewHolder> {

    private LayoutInflater inflater;
    private List<Logs> listLogs;
    private Context context;

    ActivitiesAdapter(Context context, List<Logs> listLogs) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.listLogs = listLogs;
    }

    @Override
    public ActivitiesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activities_row, parent, false);
        return new ActivitiesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ActivitiesViewHolder holder, int position) {

        if(listLogs.get(position).getCategory().equals("activity"))
            holder.textViewLog.setText(listLogs.get(position).getLog());
        else
            holder.textViewLog.setText(listLogs.get(position).getMeta()+" "+listLogs.get(position).getLog());
        holder.textViewTime.setText(listLogs.get(position).getTime());
        Picasso.with(context).load(listLogs.get(position).getLogImage()).into(holder.imageView);
        //holder.imageView.setImageResource(listLogs.get(position).getLogImage());

    }

    void addLog(Logs log) {

        this.listLogs.add(0, log);

    }

    void setListLogs(List<Logs> listLogs) {
        this.listLogs = listLogs;
    }

    @Override
    public int getItemCount() {
        return listLogs.size();
    }

    class ActivitiesViewHolder extends RecyclerView.ViewHolder {

        TextView textViewLog;
        TextView textViewTime;
        ImageView imageView;
        RelativeLayout relativeLayout;

        ActivitiesViewHolder(View itemView) {
            super(itemView);
            textViewLog = (TextView) itemView.findViewById(R.id.textViewLog);
            textViewTime = (TextView) itemView.findViewById(R.id.textViewTime);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
        }
    }

    String getDate(int position) {

        return listLogs.get(position).getDate();

    }

}
