package com.remoty.gui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.remotecontrol.ConfigurationInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alina on 9/13/2015.
 */
public class ConfigurationsListAdapter extends RecyclerView.Adapter<ConfigurationsListAdapter.ViewHolder> {
	LayoutInflater inflater;
	private LinkedList<ConfigurationInfo> configurationInfos;
	private int position;


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


	public ConfigurationsListAdapter(Context context) {
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
		// - get element from dataset at this position and update content
		this.position = position;

		holder.mConfigName.setText(configurationInfos.get(position).getName());

		holder.mItemContainer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Fragment fragment = RemoteControlFragment.newInstance(configurationInfos.get(position));

				switchToFragment(fragment);
			}
		});
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return (configurationInfos.isEmpty()) ? 0 : configurationInfos.size();
	}

	private void switchToFragment(Fragment fragment) {

		if (!ServiceManager.getInstance().getConnectionManager().hasSelection()) {

//			Toast.makeText(MainActivity.Instance, "No connection. Should open ConnectPage.", Toast.LENGTH_LONG).show();

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

		FragmentTransaction transaction = MainActivity.Instance.getSupportFragmentManager().beginTransaction();

		transaction.replace(R.id.fragment_my_configurations, fragment);

		transaction.addToBackStack(null);
		transaction.commit();
	}

}

