package com.remoty.gui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.ConnectionManager;
import com.remoty.common.ServerInfo;
import com.remoty.services.TaskScheduler;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    public static final String TAG_SERVICES = "SERVICES";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mDrawerListItems;

    private ViewPager viewPager;
    private FragmentTabListener mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Drive", "Connect", "Score" };
    // TODO: think if we want the action bar in all fragments
    // TODO: also do some research on the action bar and AppCompatActivity

    // TODO: think of a status entry in connect page (so that the user knows its connection state and host if connected)

    // TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"

    // TODO: create a Communication Manager somewhere, and ensure the info about the connection state is not lost on activity restart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Drive"));
        tabLayout.addTab(tabLayout.newTab().setText("Connect"));
        tabLayout.addTab(tabLayout.newTab().setText("Score"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new FragmentTabListener
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
/*
         mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.navigation_drawer));
*/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerList = (ListView) findViewById(R.id.list);
        mDrawerListItems = getResources().getStringArray(R.array.drawer_list);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDrawerListItems));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "You selected an item", Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close){
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
                invalidateOptionsMenu();
                syncState();
            }
            public void onDrawerOpened(View v){
                super.onDrawerOpened(v);
                invalidateOptionsMenu();
                syncState();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        // TEST

//        testTaskScheduler();

        // END_TEST
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // TODO: handle the situation when there is no connection info.

        // Restoring the connection info from the saved instance
        ServerInfo connectionInfo = ServerInfo.retrieveFromBundle(savedInstanceState);
        ConnectionManager.setConnection(connectionInfo);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // TODO: handle the situation when there is no connection info.

        // Saving the connection info to the bundle
        ServerInfo connectionInfo = ConnectionManager.getConnection();
        ServerInfo.saveToBundle(connectionInfo, savedInstanceState);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.home:
            {
                if(mDrawerLayout.isDrawerOpen(mDrawerList)){
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
            }
            case R.id.action_love:
                break;
            case R.id.action_share:
                break;
            case R.id.action_settings:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        // This tells that the item click was handled by this method (it is useful since fragments
        // also have this method and it is also called). If the action was not handled then the
        // default case of the switch returned the result of the super implementation.
        return true;
    }

    @Override
    public void onBackPressed() {

        /* Forcing back stack pop (it seems that the back stack is not popped automatically, although
        it should be). TODO: check how this behaves on multiple devices */
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
     }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    public void buttonSubscribe(View view) {

    }

    public void buttonHelp(View view) {

    }

    public void buttonManualConnection(View view) {

    }

    // TEST METHODS

    public void testTaskScheduler() {

        TaskScheduler timer = new TaskScheduler();

        timer.start(new Runnable() {
            @Override
            public void run() {

                Log.d(TaskScheduler.TAG_TIMER, "Message executed. Time: " + System.currentTimeMillis());
            }
        }, 10);

        for (int i = 1; i <= 100; i += 10) {

            timer.setInterval(i);
            Log.d(TaskScheduler.TAG_TIMER, "Changed interval to: " + i);

            if (i == 1) {
                i = 0;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        timer.setInterval(200);
        Log.d(TaskScheduler.TAG_TIMER, "Changed interval to: " + 200);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.stop();
    }

    @Override
    public void onNavigationDrawerItemSelected(int poson){

    }


    // END TEST METHODS
}
