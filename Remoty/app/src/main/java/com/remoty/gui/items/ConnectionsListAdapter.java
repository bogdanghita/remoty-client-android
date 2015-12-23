package com.remoty.gui.items;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.remoty.R;
import com.remoty.common.other.ServerInfo;
import com.remoty.common.servicemanager.ServiceManager;

import java.util.LinkedList;
import java.util.List;


public class ConnectionsListAdapter extends RecyclerView.Adapter<ConnectionsListAdapter.ViewHolder> {

	private LinkedList<ServerInfo> servers;
	private LayoutInflater inflater;
	private ServiceManager serviceManager;

	private ServerSelectionListener serverSelectionListener;

	// Provide a reference to the views for each data item providing access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView mServerName;
		public ImageView mServerIcon;
		public RelativeLayout mContainer;

		public ViewHolder(View v) {
			super(v);
			mServerName = (TextView) v.findViewById(R.id.server_name);
			mServerIcon = (ImageView) v.findViewById(R.id.selection_state_icon);
			mContainer = (RelativeLayout) v.findViewById(R.id.container);
		}
	}

	public ConnectionsListAdapter(Context context, ServerSelectionListener serverSelectionListener) {

		inflater = LayoutInflater.from(context);
		serviceManager = ServiceManager.getInstance();
		servers = new LinkedList<>();

		this.serverSelectionListener = serverSelectionListener;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ConnectionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// create a new view
		View v = inflater.inflate(R.layout.connection_item, parent, false);

		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Set the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {

		// Setting text
		holder.mServerName.setText(servers.get(position).name);

		// Setting icon
		ServerInfo currentSelection = serviceManager.getConnectionManager().getSelection();
		boolean hasSelection = serviceManager.getConnectionManager().hasSelection();
		if (!servers.isEmpty() && hasSelection && currentSelection.equals(servers.get(position))) {
			holder.mServerIcon.setImageResource(R.drawable.ic_done_black_24dp);
		}
		else {
			holder.mServerIcon.setImageResource(android.R.color.transparent);
		}

		// Setting click listener
		holder.mContainer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				ServerInfo selection = serviceManager.getConnectionManager().getSelection();
				boolean hasSelection = serviceManager.getConnectionManager().hasSelection();

				// In case there is already a selected server
				if (hasSelection) {

					// Deselect current server
					serverSelectionListener.serverDeselected();

					// If a new server is selected
					if (!selection.equals(servers.get(position))) {

						// Select the new server
						serverSelectionListener.serverSelected(servers.get(position));
					}
				}
				else {

					// If there's no previous selection -> select
					serverSelectionListener.serverSelected(servers.get(position));
				}

				// Refresh listview
				notifyDataSetChanged();
			}
		});
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {

		return (servers.isEmpty()) ? 0 : servers.size();
	}

	// Update dataset
	public void updateServerList(List<ServerInfo> s) {

		servers.clear();
		servers.addAll(s);

		notifyDataSetChanged();
	}
}
