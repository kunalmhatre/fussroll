package com.fussroll.fussroll;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by kunal on 8/1/17.
 */
public class ActivitiesFragment extends Fragment {

    private ActivitiesAdapter activitiesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final TextView textView = (TextView) view.findViewById(R.id.textView);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String todaysDate = dateFormat.format(new Date());

        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());
        List<Logs> listLogs = databaseHandler.getLogs();
        databaseHandler.close();

        if(listLogs.size() != 0) {
            if(todaysDate.equals(listLogs.get(0).getDate()))
                textView.setText(R.string.today);
            else
                textView.setText(coolDateString(listLogs.get(0).getDate()));
        }

        activitiesAdapter = new ActivitiesAdapter(getActivity(), listLogs);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(activitiesAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(todaysDate.equals(activitiesAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition())))
                    textView.setText(R.string.today);
                else {
                    textView.setText(coolDateString(activitiesAdapter.getDate(linearLayoutManager1.findFirstVisibleItemPosition())));
                }
            }
        });

        //For adding dividers in the list
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

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
        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());
        List<Logs> listLogs = databaseHandler.getLogs();
        databaseHandler.close();
        activitiesAdapter.setListLogs(listLogs);
        activitiesAdapter.notifyDataSetChanged();
    }

    private String coolDateString(String date) {
        DateFormat dateFormatToParse = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
        switch (date.substring(0,2)) {
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
                startDate = date.substring(0,2);
        }

        return  dateMonth+" "+startDate+", "+date.substring(date.length() - 4);

    }
}
