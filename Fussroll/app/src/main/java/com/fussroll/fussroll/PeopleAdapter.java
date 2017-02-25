package com.fussroll.fussroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kunal on 8/2/17.
 */

class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder> {

    private List<Logs> listLogs;
    private LayoutInflater layoutInflater;
    private Context context;
    private HashMap<String, String> contactsNames;
    private RefreshContacts refreshContacts;
    String mobile;
    private String name;
    private String userMobile;

    PeopleAdapter(Context context, List<Logs> listLogs, RefreshContacts refreshContacts, String userMobile) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.listLogs = listLogs;
        contactsNames = refreshContacts.getHashMapContactsNames();
        this.refreshContacts = refreshContacts;
        this.userMobile = userMobile;
    }

    @Override
    public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.people_row, parent, false);
        return new PeopleViewHolder(view);
    }

    void refreshContacts() {
        refreshContacts.refreshContacts(context);
    }

    @Override
    public void onBindViewHolder(PeopleViewHolder holder, int position) {
        if(listLogs.get(position).getCategory().equals("activity"))
            holder.logHere.setText(listLogs.get(position).getLog());
        else
            holder.logHere.setText(listLogs.get(position).getMeta()+" "+listLogs.get(position).getLog());
        holder.time.setText(listLogs.get(position).getTime());
        Picasso.with(context).load(listLogs.get(position).getLogImage()).into(holder.imageView);

        mobile = listLogs.get(position).getUserPhoneNumber();

        if(!mobile.equals(userMobile)) {

            if(!mobile.substring(0, mobile.length()-10).equals(userMobile.substring(0, mobile.length()-10))) {

                String mobile_1 = mobile.substring(1, mobile.length());

                //Log.i("people", mobile+" "+mobile_1);

                for(String phoneNumber : refreshContacts.finalPhoneNumbers) {
                    if(phoneNumber.equals(mobile) || phoneNumber.equals(mobile_1)) {
                        name = contactsNames.get(phoneNumber);
                        break;
                    }
                    else
                        name = mobile;
                }

            }
            else {

                String mobile_1 = mobile.substring(mobile.length()-10);
                String mobile_2 = mobile.substring(1, mobile.length());
                String mobile_3 = '0'+mobile.substring(mobile.length()-10);

                //Log.i("people", mobile+" "+mobile_1+" "+mobile_2+" "+mobile_3);

                for(String phoneNumber : refreshContacts.finalPhoneNumbers) {
                    if(phoneNumber.equals(mobile) || phoneNumber.equals(mobile_1) || phoneNumber.equals(mobile_2) || phoneNumber.equals(mobile_3)) {
                        name = contactsNames.get(phoneNumber);
                        //Log.i("people", "name if : "+name);
                        break;
                    }
                    else {
                        name = mobile;
                        //Log.i("people", "name else : "+name);
                    }
                }

            }

        }
        else {
            name = "You";

        }

        holder.name.setText(name);

    }

    void addLog(Logs log) {
        this.listLogs.add(0, log);
    }

    @Override
    public int getItemCount() {
        return listLogs.size();
    }

    class PeopleViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView time, name, logHere;

        PeopleViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            time = (TextView) itemView.findViewById(R.id.textViewTime);
            name = (TextView) itemView.findViewById(R.id.textViewName);
            logHere = (TextView) itemView.findViewById(R.id.textViewLog);
        }
    }

    String getDate(int position) {

        return listLogs.get(position).getDate();

    }

}
