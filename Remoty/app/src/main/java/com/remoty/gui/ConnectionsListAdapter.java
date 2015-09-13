package com.remoty.gui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.remoty.R;
import com.remoty.common.ServerInfo;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alina on 9/12/2015.
 */
public class ConnectionsListAdapter extends RecyclerView.Adapter<ConnectionsListAdapter.ViewHolder> {
    private LinkedList<ServerInfo> servers;
    private LayoutInflater inflater;
    private ServiceManager serviceManager;
    private ConnectionManager connectionManager;
    private int position;


    // Provide a reference to the views for each data item
    // providing access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button mServerName;
        public ImageView mServerIcon;
        public LinearLayout mContainer;

        public ViewHolder(View v) {
            super(v);
            mServerName = (Button) v.findViewById(R.id.server_name);
            mServerIcon = (ImageView) v.findViewById(R.id.server_icon);
            mContainer = (LinearLayout) v.findViewById(R.id.container);
        }
    }

    public ConnectionsListAdapter(Context context, ServiceManager sm, ConnectionManager cm) {
        inflater =  LayoutInflater.from(context);

        servers = new LinkedList<>();

        // Initial message
        servers.add(new ServerInfo("0",0,"Searching devices ..."));

        connectionManager = cm;

        serviceManager = sm;
    }

    // update dataset
    public void setServerList(List<ServerInfo> s){
        servers.clear();

        servers.addAll(s);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ConnectionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = inflater
                .inflate(R.layout.connection_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from dataset at this position and update content
        this.position = position;

        holder.mServerName.setText(servers.get(position).name);

        holder.mServerName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // In case there is already a selected server
                if (serviceManager.getConnectionManager().hasSelection()) {

                    // if the previous selected server is the current one --- deselect
                    if (serviceManager.getConnectionManager().getSelection().equals(servers.get(position))) {
                        servers.remove(serviceManager.getConnectionManager().getSelection());
                        MainActivity.Instance.serverDeselected();
                        holder.mServerIcon.setImageResource(android.R.color.transparent);

                        // refresh listview (in case the current connexion was lost and the server
                        // isn't available anymore)
                        MainActivity.Instance.mAdapter.notifyDataSetChanged();
                    } else {
                        // otherwise if selected a new server --- deselect and select the current one
                        MainActivity.Instance.serverDeselected();
                        MainActivity.Instance.serverSelected(
                                new ServerInfo(servers.get(position).ip,
                                        servers.get(position).port,
                                        servers.get(position).name));
                    }
                } else {

                    // If there's no previous connection --- select
                    MainActivity.Instance.serverSelected(new ServerInfo(
                            servers.get(position).ip,
                            servers.get(position).port,
                            servers.get(position).name));
                    holder.mServerIcon.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
                }
            }
        });

        //mark the actual connection if it exists
        if(!servers.isEmpty() && serviceManager.getConnectionManager().hasSelection()
             && serviceManager.getConnectionManager().getSelection().equals(servers.get(position))){
            connectionStateIcon(holder);
        } else {
            holder.mServerIcon.setImageResource(android.R.color.transparent);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (servers.isEmpty()) ? 0 : servers.size();
    }


    public void connectionStateIcon(final ViewHolder holder){
        switch (serviceManager.getConnectionManager().getConnectionState()) {
            case ACTIVE: {
                holder.mServerIcon.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
                break;
            }
            case SLOW: {
                holder.mServerIcon.setImageResource(R.drawable.ic_network_cell_black_24dp);
                break;
            }
            case LOST: {
                holder.mServerIcon.setImageResource(R.drawable.ic_signal_cellular_null_black_24dp);
                break;
            }
            case NONE: {
                holder.mServerIcon.setImageResource(android.R.color.transparent);
                break;
            }
        }
    }

    public void generateServers(){
        // As you've guessed :D this is just for testing
        // !!! if you use them be careful cuz there'll be duplicates when deselecting servers
        servers.add(new ServerInfo("192.168.1.1", 8000, "Server1"));
        servers.add(new ServerInfo("192.168.1.132", 9000, "Server2"));
        servers.add(new ServerInfo("192.168.1.1", 8000, "Server3"));
        servers.add(new ServerInfo("192.168.1.132", 9000, "Server4"));
        servers.add(new ServerInfo("192.168.1.1", 8000, "Server5"));
        servers.add(new ServerInfo("192.168.1.132", 9000, "Server6"));
    }
}
