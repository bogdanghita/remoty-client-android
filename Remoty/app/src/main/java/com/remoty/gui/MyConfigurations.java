package com.remoty.gui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.remoty.R;
import com.remoty.common.Configuration;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alina on 9/4/2015.
 */
public class MyConfigurations extends Fragment{

    LinkedList<Configuration> configurationsList = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View parentView = inflater.inflate(R.layout.fragment_my_configurations, container, false);

        LinearLayout configurations = (LinearLayout) parentView.findViewById(R.id.configurations);

        fillConfigurations(configurations);

        return parentView;
    }

    private void fillConfigurations(LinearLayout layout){

        // test
        configTest();

        if(configurationsList.isEmpty()){
            TextView textView = (TextView) layout.findViewById(R.id.configurations_message);
            textView.setText("You don't have any configurations yet :( ");
            return;
        }

        for(Configuration config : configurationsList){
            Button button = new Button(MainActivity.Instance);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            button.setLayoutParams(params);

            button.setTextColor(Color.DKGRAY);

            button.setPadding(0,0,0,10);

            button.setText(config.getName());

            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                }
            });

            ((LinearLayout) layout).addView(button);
        }
    }

    //TEST METHODS

    public void configTest(){
        Configuration c = new Configuration();
        c.setName("Drive");

        configurationsList.add(c);
    }

    //END TEST METHODS

}
