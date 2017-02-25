package com.fussroll.fussroll;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private LayoutInflater inflater;
    private List<String> data;
    private List<String> data1;
    private ClickListener clickListener;
    private Context context;
    private SharedPreferences sharedPreferences;

    ContactsAdapter(Context context, List<String> data, List<String> data1) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.data1 = data1;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contacts_row, parent, false);
        sharedPreferences = context.getSharedPreferences("request", Context.MODE_PRIVATE);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {

        //Getting list of people who are not on Fussroll, so that we can disable the textView and show the invitation button
        List<String> notOnFussrollList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("notOnFussroll", "").replaceAll("[\\[\\] ]", "").split(",")));
        notOnFussrollList.remove("");
        //Log.i("contacts", "list of contacts who not on Fussroll #"+notOnFussrollList+"#"+notOnFussrollList.size());

        holder.textView.setText(data.get(position));

        if(notOnFussrollList.contains(data1.get(position))) {
                //Log.i("contacts", "Not on Fussroll "+data.get(position)+"-"+data1.get(position));
                holder.textView.setEnabled(false);
                holder.appCompatButton.setVisibility(View.VISIBLE);
        } else {
            //Log.i("contacts", "On Fussroll "+data.get(position)+"-"+data1.get(position));
            holder.textView.setEnabled(true);
            holder.appCompatButton.setVisibility(View.GONE);
        }

        //Checking if user is on Fussroll - New check (we can keep the user's contact disable when he/she joins the Fussroll)
        isUser(data1.get(position), holder.appCompatButton, holder.textView, this);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        AppCompatButton appCompatButton;
        LinearLayout linearLayout;

        ContactsViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);

            appCompatButton = (AppCompatButton) itemView.findViewById(R.id.appCompatButton);

            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null)
                        clickListener.onItemClick(view, getLayoutPosition(), data1.get(getLayoutPosition()), data.get(getLayoutPosition()));
                }
            });
            appCompatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null)
                        clickListener.onItemInviteClick(data.get(getLayoutPosition()), data1.get(getLayoutPosition()));
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
        void onItemClick(View view, int position, String phoneNumber, String contactNumber);

        void onItemInviteClick(String contactName, String phoneNumber);
    }

    private void isUser(final String contact, final Button button, final TextView textView, final ContactsAdapter contactsAdapter) {

        final SharedPreferences sharedPreferences = context.getSharedPreferences("request", Context.MODE_PRIVATE);

        final String mobile = sharedPreferences.getString("mobile", "");
        String uid;

        try {
            uid = AESEncryption.decrypt(sharedPreferences.getString("uid", ""));
        } catch (Exception e) {
            uid = "";
            e.printStackTrace();
        }

        final String finalUid = uid;

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                context.getResources().getString(R.string.isUserAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("statusCode").equals("200")) {

                                final List<String> notOnFussrollList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("notOnFussroll", "").replaceAll("[\\[\\] ]", "").split(",")));
                                notOnFussrollList.remove("");

                                //Log.i("contacts", "user "+contact+" is on Fussroll");
                                if(button.getVisibility() == View.VISIBLE) {
                                    button.setVisibility(View.GONE);
                                    textView.setEnabled(true);
                                }

                                if(notOnFussrollList.contains(contact)) {
                                    //Log.i("contacts", "user "+contact+" was not on Fussroll, but now is");
                                    notOnFussrollList.remove(contact);
                                    editor.putString("notOnFussroll", notOnFussrollList.toString());
                                    editor.apply();
                                    //Log.i("contacts", "user "+contact+" is removed from not on Fussroll list");
                                    //Log.i("contacts", "updated list of not on Fussroll list "+notOnFussrollList.toString());
                                }

                            } else if (jsonObject.getString("statusCode").equals("404")) {

                                final List<String> notOnFussrollList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("notOnFussroll", "").replaceAll("[\\[\\] ]", "").split(",")));
                                notOnFussrollList.remove("");

                                //Log.i("contacts", "user "+contact+" is not on Fussroll");
                                if(button.getVisibility() != View.VISIBLE) {
                                    button.setVisibility(View.VISIBLE);
                                    textView.setEnabled(false);
                                }

                                if(!notOnFussrollList.contains(contact)) {
                                    //Log.i("contacts", "user "+contact+" is not on Fussroll and now we need to add the user in notOnFussroll list");
                                    if(notOnFussrollList.toString().equals("[]")) {
                                        editor.putString("notOnFussroll", "["+contact+"]");
                                        notOnFussrollList.add(contact);
                                    }
                                    else {
                                        notOnFussrollList.add(contact);
                                        editor.putString("notOnFussroll", notOnFussrollList.toString());
                                    }
                                    editor.apply();
                                    //Log.i("contacts", "user "+contact+" is added in not on Fussroll list");
                                    //Log.i("contacts", "updated list of not on Fussroll list "+notOnFussrollList.toString());
                                }

                            } else if (jsonObject.getString("statusCode").equals("401")) {
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                final AlertDialog.Builder verifyAgain = new AlertDialog.Builder(context);
                                verifyAgain.setMessage(R.string.verifyAgain);
                                verifyAgain.setPositiveButton(context.getString(R.string.verify), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("registered", "false");
                                        editor.putString("confirmed", "false");
                                        editor.apply();
                                        Intent intent = new Intent(context, RegisterActivity.class);
                                        context.startActivity(intent);
                                    }
                                });
                                if (ContactsFragment.activityCheck != 0) {
                                    verifyAgain.show();
                                } else
                                    Toast.makeText(context, context.getString(R.string.verify), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("uid", finalUid);
                params.put("contact", contact);
                return params;
            }
        };

        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null)
            MySingleton.getInstance(context).addToRequestQueue(stringRequest);

    }
}
