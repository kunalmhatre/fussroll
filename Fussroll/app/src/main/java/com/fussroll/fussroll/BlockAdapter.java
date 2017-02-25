package com.fussroll.fussroll;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.BlockedContactsViewHolder> {

    private LayoutInflater inflater;
    private List<String> data;
    private List<String> data1;
    private ClickListener clickListener;

    BlockAdapter(Context context, List<String> data, List<String> data1) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.data1 = data1;
    }

    @Override
    public BlockedContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.blocked_contacts_row, parent, false);
        return new BlockedContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BlockedContactsViewHolder holder, int position) {

        if(data.get(position) != null)
            holder.textView.setText(data.get(position));
        else
            holder.textView.setText(data1.get(position));

    }

    int removeItem(String contact) {
        int position = data1.indexOf(contact);
        data1.remove(contact);
        data.remove(position);
        return position;
    }

    int addItem(String name, String contact) {
        data.add(name);
        data1.add(contact);
        return data.indexOf(name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class BlockedContactsViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        AppCompatButton appCompatButton;

        BlockedContactsViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);

            appCompatButton = (AppCompatButton) itemView.findViewById(R.id.button);

            appCompatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(clickListener != null)
                        clickListener.onItemUnblockClicked(data.get(getLayoutPosition()), data1.get(getLayoutPosition()));
                }
            });
        }
    }

    void setData(List<String> data, List<String> data1) {
        this.data = data;
        this.data1 = data1;
    }

    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    interface ClickListener {
        void onItemUnblockClicked(String contactName, String phoneNumber);
    }
}
