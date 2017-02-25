package com.fussroll.fussroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ContactsListViewHolder> {

    private List<String> data;
    private List<String> data1;
    Context context;
    private LayoutInflater inflater;
    private ContactsListAdapter.ClickListener clickListener;

    ContactsListAdapter(Context context, List<String> data, List<String> data1) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.data1 = data1;
    }

    @Override
    public ContactsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contacts_list_row, parent, false);
        return new ContactsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsListViewHolder holder, int position) {

        holder.checkBox.setText(data.get(position));

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ContactsListViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;

        ContactsListViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    clickListener.onItemClick(compoundButton.getText().toString(), data1.get(getLayoutPosition()), getLayoutPosition());
                }
            });
        }
    }

    interface ClickListener {
        void onItemClick(String contact, String name, int position);
    }

}
