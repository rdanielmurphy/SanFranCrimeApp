package com.example.SanFranCrimeApp;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

// help with intellij and google play services here:
// http://www.jfarrell.net/2013/09/intellij-and-google-maps.html
//
public class MainActivity extends FragmentActivity {
    // log tag
    private static final String TAG = "Map Activity";

    private DrawerLayout _drawer;
    private ListView _lstView;
    private GoogleMap _map;

    // clustering objects
    private ClusterManager<CrimeClusterItem> _clusterManager;
    private CrimeClusterItem _clickedClusterItem;

    private final int OVERLAY_HEAT_MAP = 0;
    private final int OVERLAY_CLUSTERING = 1;
    private int _overlayType = OVERLAY_HEAT_MAP;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            //Remove title bar
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //Remove notification bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.main);

            _drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            _lstView = (ListView) findViewById(R.id.left_drawer);
            _map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            ArrayAdapter<CrimeTypeDto> adapter = new ArrayAdapter<CrimeTypeDto>(this, android.R.layout.simple_list_item_multiple_choice, new ArrayList<CrimeTypeDto>(DataBackend.getInstance(this).getCrimeTypes()));
            _lstView.setAdapter(adapter);
            _lstView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            _lstView.setItemChecked(0, true);

            // center on San Fran
            _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.775137, -122.409037), 11));

            //update options on close of left _drawer
            _drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View view, float v) {
                }

                @Override
                public void onDrawerOpened(View view) {
                }

                @Override
                public void onDrawerClosed(View view) {
                    try {
                        showOverlay();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(MainActivity.this, "Error changing overlays.  See log.", Toast.LENGTH_LONG);
                    }
                }

                @Override
                public void onDrawerStateChanged(int i) {
                }
            });
            // show popup if user single clicks
            _map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    final LatLng clickPos = new LatLng(latLng.latitude, latLng.longitude);

                    try {
                        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popup_element);
                        View layout = inflater.inflate(R.layout.popup, viewGroup);
                        DisplayMetrics outMetrics = new DisplayMetrics();
                        Display display = getWindowManager().getDefaultDisplay();
                        display.getMetrics(outMetrics);
                        final PopupWindow pw = new PopupWindow(MainActivity.this);
                        pw.setContentView(layout);
                        pw.setWidth((int) outMetrics.widthPixels - 50);
                        pw.setHeight((int) outMetrics.heightPixels / 2);
                        pw.setFocusable(true);

                        Button closeBtn = (Button) layout.findViewById(R.id.btnClosePopup);
                        closeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pw.dismiss();
                            }
                        });
                        // buttons which add images to map.  TODO: save these in a DB
                        Button addCameraBtn = (Button) layout.findViewById(R.id.btnAddCamera);
                        addCameraBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _map.addMarker(new MarkerOptions().position(
                                        clickPos).icon(BitmapDescriptorFactory.fromResource(R.drawable.camera)));
                                pw.dismiss();
                            }
                        });
                        Button addPatrolBtn = (Button) layout.findViewById(R.id.btnAddPatrol);
                        addPatrolBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _map.addMarker(new MarkerOptions().position(
                                        clickPos).icon(BitmapDescriptorFactory.fromResource(R.drawable.patrol)));
                                pw.dismiss();
                            }
                        });
                        // show popup at center of map
                        pw.showAtLocation(findViewById(R.id.map), Gravity.CENTER, 0, 0);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(MainActivity.this, "Error showing popop.  See log.", Toast.LENGTH_LONG);
                    }
                }
            });

            // show all crimes
            showOverlay();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "Error setting up map.  See log.", Toast.LENGTH_LONG);
        }
    }

    //show some overlay
    private void showOverlay() throws Exception {
        if (_overlayType == OVERLAY_HEAT_MAP)
            showHeatMap(getSelectedCrimeTypes());
        else
            showClusterMap(getSelectedCrimeTypes());
    }

    // get list of crime types selected in list view
    private List<CrimeTypeDto> getSelectedCrimeTypes() {
        List<CrimeTypeDto> types = new ArrayList<CrimeTypeDto>();

        int len = _lstView.getCount();
        SparseBooleanArray checked = _lstView.getCheckedItemPositions();
        for (int i = 0; i < len; i++) {
            if (checked.get(i))
                types.add((CrimeTypeDto) _lstView.getItemAtPosition(i));
        }

        return types;
    }

    //open left drawer to see crime types
    public void changeCrimeOptions(View view) {
        try {
            _drawer.openDrawer(_lstView);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "Could not load crime types.  See log.", Toast.LENGTH_LONG);
        }
    }

    //show either heat or clustering
    public void toggleHeatMarkers(View view) {
        try {
            if (_overlayType == OVERLAY_HEAT_MAP)
                _overlayType = OVERLAY_CLUSTERING;
            else
                _overlayType = OVERLAY_HEAT_MAP;

            showOverlay();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "Could not load crime types.  See log.", Toast.LENGTH_LONG);
        }
    }

    // create and show the clustering overlay
    private void showClusterMap(List<CrimeTypeDto> crimeTypes) throws Exception {
        // remove any other layers first
        _map.clear();

        // create clustering layer
        _clusterManager = new ClusterManager<CrimeClusterItem>(this, _map);

        // Point the _map's listeners at the listeners implemented by the cluster manager.
        _map.setOnCameraChangeListener(_clusterManager);
        _map.setInfoWindowAdapter(_clusterManager.getMarkerManager());
        // OnClusterItemClickListener is fired before setOnInfoWindowAdapter so take the marker object from there since we can't get it in InfoWindowAdapter
        _clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<CrimeClusterItem>() {
            @Override
            public boolean onClusterItemClick(CrimeClusterItem item) {
                _clickedClusterItem = item;
                return false;
            }
        });
        _map.setOnMarkerClickListener(_clusterManager);

        // set up window that opens when clicking on a marker
        _clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (_clickedClusterItem != null) {
                    // Getting view from the layout file info_window_layout
                    View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                    // Getting the position from the marker
                    LatLng latLng = marker.getPosition();

                    // Getting reference labels
                    TextView lblType = (TextView) v.findViewById(R.id.lblType);
                    TextView lblDesc = (TextView) v.findViewById(R.id.lblDesc);
                    TextView lblDate = (TextView) v.findViewById(R.id.lblDate);
                    TextView lblCase = (TextView) v.findViewById(R.id.lblCase);

                    lblType.setText(_clickedClusterItem.getType());
                    lblDesc.setText(_clickedClusterItem.getDescription());
                    lblDate.setText(_clickedClusterItem.getWhen());
                    lblCase.setText(_clickedClusterItem.getName());

                    // Returning the view containing InfoWindow contents
                    return v;
                }

                return null;
            }
        });

        // Add cluster items (markers) to the cluster manager.
        for (CrimeDto crime : DataBackend.getInstance(this).getCrimes(crimeTypes)) {
            CrimeClusterItem offsetItem = new CrimeClusterItem(crime);
            _clusterManager.addItem(offsetItem);
        }
    }

    // create and show the heat overlay
    private void showHeatMap(List<CrimeTypeDto> crimeTypes) throws Exception {
        // remove any other layers
        _map.clear();
        _map.setOnCameraChangeListener(null);
        _map.setInfoWindowAdapter(null);
        _map.setOnMarkerClickListener(null);
        if (_clusterManager != null)
            _clusterManager.clearItems();

        //create heat _map
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        List<CrimeDto> f = DataBackend.getInstance(this).getCrimes(crimeTypes);
        for (CrimeDto dto : f)
            list.add(new LatLng(dto.getLatitude(), dto.getLongitude()));

        // Create a heat _map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        mProvider.setRadius(30);
        mProvider.setOpacity(.85);

        // Add a tile overlay to the _map, using the heat _map tile provider.
        TileOverlay mOverlay = _map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        mOverlay.setVisible(true);
    }

    // object used in the clustering layer
    public class CrimeClusterItem implements ClusterItem {
        private final LatLng _position;
        private final CrimeDto _dto;

        public CrimeClusterItem(CrimeDto dto) {
            _position = new LatLng(dto.getLatitude(), dto.getLongitude());
            _dto = dto;
        }

        public String getType() {
            return _dto.getType();
        }

        public String getUrl() {
            return _dto.getUrl();
        }

        public String getName() {
            return _dto.getName();
        }

        public String getDescription() {
            return _dto.getDescription();
        }

        public String getWhen() {
            return _dto.getWhen().toString();
        }

        @Override
        public LatLng getPosition() {
            return _position;
        }
    }
}