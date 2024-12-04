package com.example.login;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapHandler implements OnMapReadyCallback {

    private final Context context;
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final String TAG = "MapHandler";

    // Listener para notificar la ubicación seleccionada
    public interface LocationSelectedListener {
        void onLocationSelected(LatLng location);
    }

    private LocationSelectedListener locationSelectedListener;

    public MapHandler(Context context, SupportMapFragment mapFragment) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    public void setLocationSelectedListener(LocationSelectedListener listener) {
        this.locationSelectedListener = listener;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // Manejar clics en el mapa
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            setMarkerAtLocation(latLng);

            // Notificar al listener sobre la ubicación seleccionada
            if (locationSelectedListener != null) {
                locationSelectedListener.onLocationSelected(latLng);
            }

            Log.d(TAG, "onMapClick: Ubicación seleccionada: " + latLng.latitude + ", " + latLng.longitude);
        });

        // Habilitar ubicación del usuario
        enableUserLocation();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    selectedLocation = currentLocation;
                    setMarkerAtLocation(currentLocation);

                    // Notificar al listener sobre la ubicación inicial
                    if (locationSelectedListener != null) {
                        locationSelectedListener.onLocationSelected(currentLocation);
                    }

                    Log.d(TAG, "enableUserLocation: Ubicación del usuario obtenida y asignada: " + currentLocation);
                } else {
                    Log.w(TAG, "enableUserLocation: No se pudo obtener la ubicación del usuario.");
                }
            });
        } else {
            ActivityCompat.requestPermissions((Medicion) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public LatLng getSelectedLocation() {
        return selectedLocation;
    }

    public void setMarkerAtLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            mMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(0x220000FF).fillColor(0x220000FF));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));
            Log.d(TAG, "setMarkerAtLocation: Marcador asignado en el mapa: " + latLng);
        }
    }
}
