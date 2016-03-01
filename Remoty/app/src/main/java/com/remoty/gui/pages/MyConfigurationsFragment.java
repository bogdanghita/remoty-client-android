package com.remoty.gui.pages;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.remoty.R;
import com.remoty.common.datatypes.ConfigurationEntry;
import com.remoty.common.other.Constants;
import com.remoty.gui.debug.DebugFragment;
import com.remoty.gui.items.ConfigurationsListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
		adapter.setConfigurationList(loadConfigurationsList());

		recyclerView.setAdapter(adapter);

		return parentView;
	}

	private List<ConfigurationEntry> loadConfigurationsList() {

		List<ConfigurationEntry> result = readConfigurationsList();

		if(result == null) {

			// TODO: if this happens the configurations list can't be loaded
			// show a message, and stop the app from crashing
		}

		return result;
	}

	private List<ConfigurationEntry> readConfigurationsList() {

		List<ConfigurationEntry> result = null;

		String configFile = Constants.CONFIG_FILE;

		try {
			InputStream is = getContext().getAssets().open(configFile);

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			Gson gson = new Gson();

			Type type = new TypeToken<List<ConfigurationEntry>>() {}.getType();

			result = gson.fromJson(reader, type);
		}
		catch (IOException e) {
			e.printStackTrace();

			Log.d(Constants.APP + Constants.CONFIG, "Error reading configuration file: " + configFile);
		}

		return result;
	}
}
