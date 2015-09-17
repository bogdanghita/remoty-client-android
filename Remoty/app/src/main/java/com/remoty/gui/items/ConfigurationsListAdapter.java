package com.remoty.gui.items;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.common.other.ConfigurationInfo;
import com.remoty.gui.pages.MainActivity;
import com.remoty.gui.pages.RemoteControlActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alina on 9/13/2015.
 */
public class ConfigurationsListAdapter extends RecyclerView.Adapter<ConfigurationsListAdapter.ViewHolder> {

	// Provide a reference to the views for each data item
	// providing access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView mConfigName;
		public LinearLayout mNameContainer;
		public LinearLayout mItemContainer;

		public ViewHolder(View v) {
			super(v);
			mConfigName = (TextView) v.findViewById(R.id.configuration_name);
			mNameContainer = (LinearLayout) v.findViewById(R.id.configuration_name_holder);
			mItemContainer = (LinearLayout) v.findViewById(R.id.configuration_item_holder);
		}
	}

	LayoutInflater inflater;
	private LinkedList<ConfigurationInfo> configurationInfos;
	private int position;

	Context context;

	public ConfigurationsListAdapter(Context context) {

		this.context = context;

		inflater = LayoutInflater.from(context);

		configurationInfos = new LinkedList<>();
	}

	// update dataset
	public void setConfigurationInfos(List<ConfigurationInfo> s) {

		configurationInfos.clear();

		configurationInfos.addAll(s);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ConfigurationsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
																   int viewType) {
		// create a new view
		View v = inflater
				.inflate(R.layout.configurations_item, parent, false);

		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {

		// Getting element from data set at this position and updating content
		this.position = position;

		holder.mConfigName.setText(configurationInfos.get(position).getName());

		holder.mItemContainer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!ServiceManager.getInstance().getConnectionManager().hasSelection()) {

					Toast.makeText(MainActivity.Instance, "No connection. Should open ConnectPage.", Toast.LENGTH_LONG).show();

					// TODO: Think if we should open the connect page here

					return;
				}

				// Starting the remote control activity activity
				startRemoteControlActivity();
			}
		});
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return (configurationInfos.isEmpty()) ? 0 : configurationInfos.size();
	}

	private void startRemoteControlActivity() {

		// TODO: Open the new fragment.
		// NOTE: A configuration page is going to be launched so the screen should not contain the action bar and the tabs
		// You should either hide them and show them back when we return to the previous fragment (this can be problematic...)
		// or we should define a different layout for the configurations fragments which will replace the layout of the main
		// activity (I think this is better because the layouts will be automatically restored when the fragment closes)
		// This needs research...


//		FragmentTransaction transaction = MainActivity.Instance.getSupportFragmentManager().beginTransaction();
//
//		transaction.replace(R.id.fragment_my_configurations, fragment);
//
//		transaction.addToBackStack(null);
//		transaction.commit();

		Bundle args = new Bundle();

		ConfigurationInfo configuration = configurationInfos.get(position);

		args.putString(RemoteControlActivity.KEY_NAME, configuration.getName());
		args.putString(RemoteControlActivity.KEY_FILE, configuration.getFile());

		Intent intent = new Intent(context, RemoteControlActivity.class);
		intent.putExtras(args);
		context.startActivity(intent);
	}

}

