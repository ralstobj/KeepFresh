package com.rgw.keepfresh;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimerFragment.intentData, BarcodeUI.barcodeIntent {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.app_name));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragMan = getFragmentManager();
        TimerFragment mFragment = (TimerFragment) fragMan.findFragmentByTag("Timer");
        BarcodeUI barFrag = (BarcodeUI) fragMan.findFragmentByTag("UI");
        EditorFragment myFragment = (EditorFragment) fragMan.findFragmentByTag("Editor");
        boolean myFragTime = false;
        if (mFragment != null) {
            myFragTime = mFragment.isVisible();
        }
        boolean myEdit = false;
        if (myFragment != null) {
            myEdit = myFragment.isVisible();
        }
        boolean myBar = false;
        if (barFrag != null) {
            myBar = barFrag.isVisible();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (myBar) {
                getFragmentManager().beginTransaction().replace(R.id.content, myFragment).commit();
            } else {
                super.onBackPressed();
            }
            if (myFragTime) {
                setTitle(getString(R.string.app_name));
            }
            if (myEdit) {
                setTitle(getString(R.string.timer_fragment_title));
            }
        }
        hideKeyboard(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fragmentManager = getFragmentManager();
        int id = item.getItemId();
        if (id == R.id.timer) {
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, new TimerFragment(), "Timer").commit();
            setTitle(R.string.timer_fragment_title);

        } else if (id == R.id.nav_food_pantry) {
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, new FoodPantryLocations(), "Pantry").commit();
        } else if (id == R.id.nav_grocery_stores) {
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, new GroceryLocations(), "Grocery").commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void recieveUri(Uri uri) {
        EditorFragment editorFrag = (EditorFragment)
                getFragmentManager().findFragmentById(R.id.editor_frag);

        if (editorFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            editorFrag.setmCurrentProductUri(uri);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            EditorFragment newFragment = new EditorFragment();
            Bundle args = new Bundle();
            if (uri != null) {
                args.putString("uri", uri.toString());
                newFragment.setArguments(args);
            }

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.addToBackStack(null).replace(R.id.content, newFragment, "Editor").commit();
        }
    }

    @Override
    public void passBarcode(Barcode b) {
        EditorFragment editorFrag = (EditorFragment)
                getFragmentManager().findFragmentById(R.id.editor_frag);

        if (editorFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            editorFrag.setBarcodeText(b.displayValue);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            EditorFragment newFragment = (EditorFragment) getFragmentManager().findFragmentByTag("Editor");
            //Bundle args = new Bundle();
            //if(b != null) {
            //    args.putString("barcode",b.displayValue);
            //    newFragment.setArguments(args);
            // }

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.content, newFragment, "Editor").commit();
            newFragment.setBarcodeText(b.displayValue);
        }
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
