package com.fussroll.fussroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PeopleFragment extends Fragment {

    private PeopleAdapter peopleAdapter;
    private int newUpdates = 0;
    private String userPhone;
    String todaysDate;
    RecyclerView recyclerView;
    RelativeLayout relativeLayout;
    TextView textView, welcomeMessage;
    private Timer autoUpdate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        textView = (TextView) view.findViewById(R.id.textView);
        welcomeMessage = (TextView) view.findViewById(R.id.welcomeMessage);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        todaysDate = dateFormat.format(new Date());
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("request", Context.MODE_PRIVATE);
        userPhone = sharedPreferences.getString("mobile", "N/A");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPeople();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());
            List<Logs> listLogs = databaseHandler.getPeopleLogs();
            databaseHandler.close();

            if (listLogs != null) {

                welcomeMessage.setVisibility(View.GONE);

                newUpdates = listLogs.size();

                if (listLogs.size() != 0) {
                    if (todaysDate.equals(listLogs.get(0).getDate()))
                        textView.setText(R.string.today);
                    else
                        textView.setText(coolDateString(listLogs.get(0).getDate()));
                }

                RefreshContacts refreshContacts = new RefreshContacts();

                //Log.i("PeopleFragment", "Permission is granted");
                refreshContacts.refreshContacts(getActivity());
                peopleAdapter = new PeopleAdapter(getActivity(), listLogs, refreshContacts, userPhone);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(peopleAdapter);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (todaysDate.equals(peopleAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition()))) {
                            textView.setText(R.string.today);
                            relativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
                        } else {
                            textView.setText(coolDateString(peopleAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition())));
                            relativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.lightGray));
                        }
                    }
                });

                //For adding dividers in the list
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
                recyclerView.addItemDecoration(dividerItemDecoration);
            }

        }

        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible) {
            HomeActivity.fab.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.fab.show();
        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            if(peopleAdapter != null) {
                peopleAdapter.refreshContacts();
            }

            autoUpdate = new Timer();
            autoUpdate.schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshPeople();
                        }
                    });
                }
            }, 0, 1000);

            DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());
            List<Logs> listLogs = databaseHandler.getPeopleLogs();
            databaseHandler.close();
            if(listLogs != null) {

                welcomeMessage.setVisibility(View.GONE);

                if(peopleAdapter == null) {

                    newUpdates = listLogs.size();

                    if(listLogs.size() != 0) {
                        if(todaysDate.equals(listLogs.get(0).getDate()))
                            textView.setText(R.string.today);
                        else {
                            textView.setText(coolDateString(listLogs.get(0).getDate()));
                        }
                    }

                    RefreshContacts refreshContacts = new RefreshContacts();
                    refreshContacts.refreshContacts(getActivity());

                    peopleAdapter = new PeopleAdapter(getActivity(), listLogs, refreshContacts, userPhone);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(peopleAdapter);

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if(todaysDate.equals(peopleAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition()))) {
                                textView.setText(R.string.today);
                                relativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
                            }
                            else {
                                textView.setText(coolDateString(peopleAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition())));
                                relativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.lightGray));
                            }
                        }
                    });

                    //For adding dividers in the list
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
                    recyclerView.addItemDecoration(dividerItemDecoration);
                }
                else {
                    //Log.i("pf", "Before for "+Integer.toString(newUpdates)+" "+Integer.toString(listLogs.size()));
                    for(int i = listLogs.size()-newUpdates-1; i >= 0; i--) {
                        //Log.i("pf", listLogs.get(i).getLog());
                        peopleAdapter.addLog(listLogs.get(i));
                        peopleAdapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0);
                    }
                    newUpdates = listLogs.size();
                    //Log.i("pf", "After for "+Integer.toString(newUpdates)+" "+Integer.toString(listLogs.size()));
                    //Log.i("pf", listLogs.get(0).getLog());
                /*peopleAdapter.setListLogs(listLogs);
                peopleAdapter.notifyDataSetChanged();*/
                }
            }

        }
    }

    @Override
    public void onPause() {
        if(autoUpdate != null)
            autoUpdate.cancel();
        super.onPause();
    }

    void refreshPeople() {

        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());
        List<Logs> listLogs = databaseHandler.getPeopleLogs();
        databaseHandler.close();
        if(listLogs != null) {

            welcomeMessage.setVisibility(View.GONE);

            if(peopleAdapter == null) {

                newUpdates = listLogs.size();

                if(listLogs.size() != 0) {
                    if(todaysDate.equals(listLogs.get(0).getDate()))
                        textView.setText(R.string.today);
                    else
                        textView.setText(coolDateString(listLogs.get(0).getDate()));
                }

                RefreshContacts refreshContacts = new RefreshContacts();
                refreshContacts.refreshContacts(getActivity());

                peopleAdapter = new PeopleAdapter(getActivity(), listLogs, refreshContacts, userPhone);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(peopleAdapter);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if(todaysDate.equals(peopleAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition()))) {
                            textView.setText(R.string.today);
                            relativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
                        }
                        else {
                            textView.setText(coolDateString(peopleAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition())));
                            relativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.lightGray));
                        }
                    }
                });

                //For adding dividers in the list
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
                recyclerView.addItemDecoration(dividerItemDecoration);
            }
            else {
                //Log.i("pf", "Before for "+Integer.toString(newUpdates)+" "+Integer.toString(listLogs.size()));
                for(int i = listLogs.size()-newUpdates-1; i >= 0; i--) {
                    //Log.i("pf", listLogs.get(i).getLog());
                    peopleAdapter.addLog(listLogs.get(i));
                    peopleAdapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                }
                newUpdates = listLogs.size();
                //Log.i("pf", "After for "+Integer.toString(newUpdates)+" "+Integer.toString(listLogs.size()));
                //Log.i("pf", listLogs.get(0).getLog());
                /*peopleAdapter.setListLogs(listLogs);
                peopleAdapter.notifyDataSetChanged();*/
            }
        }

    }

    private String coolDateString(String date) {
        DateFormat dateFormatToParse = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date dateObject;
        try {
            dateObject = dateFormatToParse.parse(date);
        } catch (ParseException e) {
            dateObject = new Date();
            e.printStackTrace();
        }
        DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String dateMonth = dateFormat.format(dateObject);
        String startDate;
        switch (date.substring(date.length()-2, date.length())) {
            case "01":
                startDate = "1";
                break;
            case "02":
                startDate = "2";
                break;
            case "03":
                startDate = "3";
                break;
            case "04":
                startDate = "4";
                break;
            case "05":
                startDate = "5";
                break;
            case "06":
                startDate = "6";
                break;
            case "07":
                startDate = "7";
                break;
            case "08":
                startDate = "8";
                break;
            case "09":
                startDate = "9";
                break;
            default:
                startDate = date.substring(date.length()-2, date.length());
        }

        return  dateMonth+" "+startDate+", "+date.substring(0, 4);

    }
}
