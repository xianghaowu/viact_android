package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_SHEET_PATH;
import static com.viact.viact_android.utils.Const.SHEET_TYPE_GMAP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.models.Sheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    final int REQUEST_CUSTOM_PERMISSION = 9998;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.map_sp_type)    Spinner sp_map_type;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.map_tv_direction)    TextView    tv_direction;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.map_ib_crop)    ImageButton     ib_crop;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.iv_crop_source)    ImageView iv_src;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.iv_crop_area)    ImageView     iv_area;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rl_crop_area)    RelativeLayout      view_crop;

    String[] map_types = {"Satellite", "Terrain", "Hybrid", "Normal"};
    int nMapType = GoogleMap.MAP_TYPE_SATELLITE;

    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    boolean bCrop = false;

    LatLng latLng;
    Point cur_pos;

    Project cur_proc;
    String sheet_name = "";
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        cur_proc = (Project) data.getParcelable("project");
        sheet_name = data.getString("sheet_name", "");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CUSTOM_PERMISSION);
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dbHelper = DatabaseHelper.getInstance(this);
        initLayout();
    }

    void initLayout() {
        view_crop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view_crop.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                src_width  = view_crop.getMeasuredWidth();
                src_height = view_crop.getMeasuredHeight();

            }
        });
        view_crop.setVisibility(View.INVISIBLE);
        view_crop.setOnTouchListener(dragdrop_area);
        sp_map_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    nMapType = GoogleMap.MAP_TYPE_SATELLITE;
                } else if (i == 1) {
                    nMapType = GoogleMap.MAP_TYPE_TERRAIN;
                } else if (i == 2) {
                    nMapType = GoogleMap.MAP_TYPE_HYBRID;
                } else {
                    nMapType = GoogleMap.MAP_TYPE_NORMAL;
                }
                refreshMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, R.layout.item_spinner_map, map_types);
        aa.setDropDownViewResource(R.layout.item_spinner_map_dropdown);
        //Setting the ArrayAdapter data on the Spinner
        sp_map_type.setAdapter(aa);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
    }

    void refreshMap(){
        mMap.setMapType(nMapType);
        tv_direction.setText(mMap.getCameraPosition().bearing + "");
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.map_ib_back) void onClickBack(){
        onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.map_ib_crop) void onClickCropMap(){
        bCrop = !bCrop;
        if (bCrop){
            if (captureGoogleMap()){
                ib_crop.setColorFilter(Color.rgb(255, 255, 0));

            } else {
                view_crop.setVisibility(View.INVISIBLE);
                bCrop = false;
            }
        } else {
            ib_crop.setColorFilter(Color.rgb(255, 255, 255));
            view_crop.setVisibility(View.INVISIBLE);

        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.map_btn_confirm) void onClickConfirm(){
        if (bm_src == null) {
            Toast.makeText(this, R.string.map_crop_current_location, Toast.LENGTH_SHORT).show();
            return;
        }
        int sx ;
        if (s_pos.x > e_pos.x) {
            sx = e_pos.x;
            e_pos.x = s_pos.x;
            s_pos.x = sx;
        }
        int sy;
        if (s_pos.y > e_pos.y){
            sy = e_pos.y;
            e_pos.y = s_pos.y;
            s_pos.y = sy;
        }
        if (cur_pos.x >= s_pos.x && cur_pos.x <= e_pos.x && cur_pos.y >= s_pos.y && cur_pos.y <= e_pos.y) {
            f_pos.x = (float) cur_pos.x/(e_pos.x - s_pos.x);
            f_pos.y = (float) cur_pos.y/(e_pos.y - s_pos.y);
            f_bearing = 360 - mMap.getCameraPosition().bearing;
            Bitmap bm_crop = Bitmap.createBitmap(bm_src, s_pos.x, s_pos.y, e_pos.x - s_pos.x, e_pos.y - s_pos.y);
            if (bm_crop != null)
                saveSheetBitmap(bm_crop);
        } else{
            Toast.makeText(this, R.string.map_crop_current_location, Toast.LENGTH_SHORT).show();
        }
    }

    Bitmap  bm_src;
    float   f_bearing = 0;
    PointF  f_pos = new PointF();
    Point   s_pos = new Point();
    Point   e_pos = new Point();
    int src_width, src_height;

    @SuppressLint("ClickableViewAccessibility")
    View.OnTouchListener dragdrop_area = (view, mevent) -> {
        if (mevent.getAction() == MotionEvent.ACTION_DOWN) {
            s_pos.x = (int)mevent.getX();
            s_pos.y = (int)mevent.getY();
            return true;
        } else if (mevent.getAction() == MotionEvent.ACTION_MOVE){
            e_pos.x = (int) mevent.getX();
            e_pos.y = (int) mevent.getY();
            moveCropArea(s_pos, e_pos);
            return true;
        } else if (mevent.getAction() == MotionEvent.ACTION_UP) {
//            e_pos.x = (int) mevent.getX();
//            e_pos.y = (int) mevent.getY();
//            moveCropArea(s_pos, e_pos);
//            return true;
        }
        return false;
    };

    void moveCropArea(Point sp, Point ep){
        int sx = sp.x;
        int ex = ep.x;
        if (sp.x > ep.x) {
            sx = ep.x;
            ex = sp.x;
        }
        int sy = sp.y;
        int ey = ep.y;
        if (sp.y > ep.y){
            sy = ep.y;
            ey = sp.y;

        }
        iv_area.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ex-sx, ey-sy);
        layoutParams.leftMargin = sx;
        layoutParams.topMargin = sy;
        iv_area.setLayoutParams(layoutParams);
        iv_area.requestLayout();
    }

    boolean captureGoogleMap(){
        GoogleMap.SnapshotReadyCallback callback = snapshot -> {
            // TODO Auto-generated method stub
            bm_src = snapshot;
            iv_src.setImageBitmap(snapshot);
            iv_src.setVisibility(View.VISIBLE);
            iv_area.setVisibility(View.GONE);
            view_crop.setVisibility(View.VISIBLE);

        };
        Projection projection = mMap.getProjection();
        cur_pos = projection.toScreenLocation(latLng);
        if (cur_pos.x >= 0 && cur_pos.x <= src_width && cur_pos.y >= 0 && cur_pos.y <= src_height){
            mMap.snapshot(callback);
            return true;
        } else {
            Toast.makeText(this, R.string.map_move_current_location, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    void saveSheetBitmap(Bitmap bm){
        String dir_name = EXT_STORAGE_SHEET_PATH;
        File dir = new File(dir_name);
        if (!dir.exists()){
            dir.mkdir();
        }
        long tsLong = System.currentTimeMillis()/60000;
        String ts = Long.toString(tsLong);
        String f_name = dir_name + cur_proc.name + "_" + sheet_name + "_" + ts + ".png";
        try (FileOutputStream out = new FileOutputStream(f_name)) {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            Sheet one = new Sheet();
            one.pro_id = cur_proc.id + "";
            one.name = sheet_name;
            one.path = f_name;
            one.type = SHEET_TYPE_GMAP;
            one.lt_loc = f_pos.toString();
            one.rb_loc = f_bearing + "";
            one.create_time = (long)System.currentTimeMillis()/1000 + "";
            one.update_time = one.create_time;
            dbHelper.addSheet(one);

            setResult(Activity.RESULT_OK, null);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show();
            procCancelCrop();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.map_btn_cancel) void procCancelCrop(){
        onClickCropMap();
    }

    @Override public void onBackPressed(){
        setResult(Activity.RESULT_CANCELED, null);
        super.onBackPressed();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(nMapType);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        float zoom = mMap.getCameraPosition().zoom;
        zoom = zoom < 17.0? 17:zoom;
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CUSTOM_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please check the permission on setting", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}