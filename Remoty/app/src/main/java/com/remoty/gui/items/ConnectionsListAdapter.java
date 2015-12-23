package com.remoty.gui.items;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.remoty.R;
import com.remoty.common.other.ServerInfo;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.gui.pages.MainActivity;

import java.util.LinkedList;
import java.util.List;


public class ConnectionsListAdapter extends RecyclerView.Adapter<ConnectionsListAdapter.ViewHolder> {

	private LinkedList<ServerInfo> servers;
	private LayoutInflater inflater;
	private ServiceManager serviceManager;

	// Provide a reference to the views for each data item
	// providing access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView mServerName;
		public ImageView mServerIcon;
		public LinearLayout mContainer;

		public ViewHolder(View v) {
			super(v);
			mServerName = (TextView) v.findViewById(R.id.server_name);
			mServerIcon = (ImageView) v.findViewById(R.id.server_icon);
			mContainer = (LinearLayout) v.findViewById(R.id.container);
		}
	}

	public ConnectionsListAdapter(Context context) {

		inflater = LayoutInflater.from(context);
		serviceManager = ServiceManager.getInstance();
		servers = new LinkedList<>();
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
			holder.mServerIcon.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
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

					// If the previous selected server is the current one -> deselect
					if (selection.equals(servers.get(position))) {

						MainActivity.Instance.serverDeselected();
						holder.mServerIcon.setImageResource(android.R.color.transparent);

						// Refresh listview (in case the current connection was lost and the server isn't available anymore)
						notifyDataSetChanged();
					}
					else {
						// Otherwise if selected a new server -> deselect and select the new one
						MainActivity.Instance.serverDeselected();
						MainActivity.Instance.serverSelected(new ServerInfo(servers.get(position).ip,
								servers.get(position).port, servers.get(position).name));
					}
				}
				else {

					// If there's no previous connection -> select
					MainActivity.Instance.serverSelected(new ServerInfo(servers.get(position).ip, servers.get(position).port,
							servers.get(position).name));

					holder.mServerIcon.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
				}
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
