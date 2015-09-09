package com.remoty.gui;

import android.support.v4.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

	LinearLayout configurations_layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_my_configurations, container, false);

		configurations_layout = (LinearLayout) parentView.findViewById(R.id.configurations_layout);

		// generateTestConfigurations() will be replaced by the actual user configurations
		updateConfigurationsList(generateTestConfigurations());

		return parentView;
	}

	private void updateConfigurationsList(List<ConfigurationInfo> configurations) {

		configurations_layout.removeAllViews();

		if (configurations.isEmpty()) {
			TextView textView = new TextView(getActivity());
			textView.setText("You don't have any configurations yet :( ");
			configurations_layout.addView(textView);
			return;
		}

		for (ConfigurationInfo config : configurations) {

			Button button = createConfigurationButton(config);
			configurations_layout.addView(button);
		}
	}

	private Button createConfigurationButton(final ConfigurationInfo config) {

		Button button = new Button(MainActivity.Instance);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		button.setLayoutParams(params);

		button.setTextColor(Color.DKGRAY);

		button.setPadding(0, 0, 0, 10);

		button.setText(config.getName());

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Fragment fragment = RemoteControlFragment.newInstance(config);

				switchToFragment(fragment);
			}
		});

		return button;
	}

	private void switchToFragment(Fragment fragment) {

		if (!ServiceManager.getInstance().getConnectionManager().hasSelection()) {

			Toast.makeText(getActivity(), "No connection. Should open ConnectPage.", Toast.LENGTH_LONG).show();

			// TODO: Review this from  the point of view of UX
//			openConnectPage();

			return;
		}

		// TODO: Open the new fragment.
		// NOTE: A configuration page is going to be launched so the screen should not contain the action bar and the tabs
		// You should either hide them and show them back when we return to the previous fragment (this can be problematic...)
		// or we should define a different layout for the configurations fragments which will replace the layout of the main
		// activity (I think this is better because the layouts will be automatically restored when the fragment closes)
		// This needs research...

		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

		// TODO: This is strange... the Drive button remains there...
		transaction.replace(R.id.fragment_my_configurations, fragment);

		transaction.addToBackStack(null);
		transaction.commit();
	}

	//TEST METHODS

	public List<ConfigurationInfo> generateTestConfigurations() {

		List<ConfigurationInfo> configurations = new LinkedList<>();

		ConfigurationInfo c = new ConfigurationInfo("Drive", "drive_config_file");

		configurations.add(c);

		return configurations;
	}

	//END TEST METHODS
}
