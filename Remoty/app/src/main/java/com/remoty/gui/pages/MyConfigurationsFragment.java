package com.remoty.gui.pages;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remoty.R;
import com.remoty.common.other.ConfigurationInfo;
import com.remoty.gui.debug.DebugFragment;
import com.remoty.gui.items.ConfigurationsListAdapter;

import java.util.LinkedList;
import java.util.List;


public class MyConfigurationsFragment extends DebugFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_my_configurations, container, false);

		RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.configurations_layout);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		ConfigurationsListAdapter adapter = new ConfigurationsListAdapter(getContext());
		adapter.setConfigurationInfos(generateTestConfigurations());

		recyclerView.setAdapter(adapter);

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
