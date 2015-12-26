package com.remoty.gui.items;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.other.Constants;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.common.datatypes.ConfigurationInfo;
import com.remoty.gui.pages.RemoteControlActivity;

import java.util.LinkedList;
import java.util.List;


public class ConfigurationsListAdapter extends RecyclerView.Adapter<ConfigurationsListAdapter.ViewHolder> {

	// Provide a reference to the views for each data item providing access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView mConfigName;
		public RelativeLayout mNameContainer;
		public LinearLayout mItemContainer;
		public View mOptionsButton;

		public ViewHolder(View v) {
			super(v);

			mConfigName = (TextView) v.findViewById(R.id.configuration_name);
			mNameContainer = (RelativeLayout) v.findViewById(R.id.configuration_name_holder);
			mItemContainer = (LinearLayout) v.findViewById(R.id.configuration_item_holder);
			mOptionsButton = v.findViewById(R.id.options);
		}
	}

	private LayoutInflater inflater;
	private LinkedList<ConfigurationInfo> configurationList;

	Context context;

	public ConfigurationsListAdapter(Context context) {

		this.context = context;

		inflater = LayoutInflater.from(context);

		configurationList = new LinkedList<>();
	}

	// Update dataset
	public void setConfigurationList(List<ConfigurationInfo> s) {

		configurationList.clear();
		configurationList.addAll(s);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ConfigurationsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		// Create a new view
		View v = inflater.inflate(R.layout.configurations_item, parent, false);

		return new ViewHolder(v);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {

		// Setting configuration title
		holder.mConfigName.setText(configurationList.get(position).getName());

		// Setting layout click listener
		holder.mItemContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!ServiceManager.getInstance().getConnectionManager().hasSelection()) {

					Toast.makeText(holder.itemView.getContext(), "No connection. Should open ConnectPage.", Toast.LENGTH_LONG).show();

					// TODO: Think if we should open the connect page here

					return;
				}

				// Starting the remote control activity activity
				startRemoteControlActivity(position);
			}
		});

		// Setting options button click listener
		holder.mOptionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// Displaying options menu for current configuration
				showPopup(v, position);
			}
		});
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return (configurationList.isEmpty()) ? 0 : configurationList.size();
	}

	private void startRemoteControlActivity(int position) {

		Bundle args = new Bundle();

		ConfigurationInfo configuration = configurationList.get(position);

		args.putString(RemoteControlActivity.KEY_NAME, configuration.getName());
		args.putString(RemoteControlActivity.KEY_FILE, configuration.getFile());

		Intent intent = new Intent(context, RemoteControlActivity.class);
		intent.putExtras(args);
		context.startActivity(intent);
	}

	public void showPopup(View v, final int position) {

		PopupMenu popup = new PopupMenu(context, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_configuration_item, popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				Log.d(Constants.MENU, "Config: " + position + ", Menu item: " + item);

				return true;
			}
		});

		popup.show();
	}
}
