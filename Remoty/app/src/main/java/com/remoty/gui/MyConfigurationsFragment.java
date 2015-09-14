package com.remoty.gui;

import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.remotecontrol.ConfigurationInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alina on 9/4/2015.
 */
public class MyConfigurationsFragment extends DebugFragment {

	RecyclerView configurations_layout;
    ConfigurationsListAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_my_configurations, container, false);

		configurations_layout = (RecyclerView) parentView.findViewById(R.id.configurations_layout);

        mAdapter = new ConfigurationsListAdapter(MainActivity.Instance);

		// generateTestConfigurations() will be replaced by the actual user configurations

        mAdapter.setConfigurationInfos(generateTestConfigurations());

        configurations_layout.setLayoutManager(new LinearLayoutManager(MainActivity.Instance));

        configurations_layout.setAdapter(mAdapter);

		return parentView;
	}

	//TEST METHODS

	public List<ConfigurationInfo> generateTestConfigurations() {

		List<ConfigurationInfo> configurations = new LinkedList<>();

		ConfigurationInfo c = new ConfigurationInfo("Drive", "drive_config_file");

		configurations.add(c);

//        ConfigurationInfo c1 = new ConfigurationInfo("Dummy", "drive_config_file");
//
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);
//        configurations.add(c1);

		return configurations;
	}

	//END TEST METHODS
}
