package com.faculdadedombosco.eduardopinheiro.walkdog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.faculdadedombosco.eduardopinheiro.walkdog.models.Coordenadas;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapaSolicitanteActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {

    private final String TAG = "WalkDog v1";

    private TextView mLocationView;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private ChildEventListener mPasseioListener;
    private Marker marker;
    private String idPasseio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapa_solicitante);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapSolicitante);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final String idPasseio = getIntent().getStringExtra("idPasseio");
        this.idPasseio = idPasseio;

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mDatabase.child("passeios").child(idPasseio).child("localizacao").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Coordenadas coordenadas = dataSnapshot.getValue(Coordenadas.class);

                LatLng localizacao = new LatLng(coordenadas.getLatitude(), coordenadas.getLongitude());

                mMap.addMarker(new MarkerOptions()
                        .position(localizacao)
                        .title("Passeador"));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(localizacao)
                        .zoom(18)
                        .build();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // AO BUSCAR CACHORRO, SOLICITANTE PODE VER ONDE O PASSEADOR ESTA
        ValueEventListener buscouCachorroListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean buscouCachorro = dataSnapshot.getValue(Boolean.class);

                if (buscouCachorro != null && buscouCachorro.equals(true)) {
                    watchPasseador(idPasseio);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("passeios").child(idPasseio).child("buscouCachorro").addValueEventListener(buscouCachorroListener);

        // AO ENTREGAR CACHORRO, ABRE TELA PARA SOLICITANTE AVALIAR
        ValueEventListener entregouCachorroListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean entregouCachorro = dataSnapshot.getValue(Boolean.class);

                if (entregouCachorro != null && entregouCachorro.equals(true)) {
                    Toast.makeText(MapaSolicitanteActivity.this, "Cachorro devolvido. Avalie o passeador!!!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MapaSolicitanteActivity.this, AvaliarActivity.class);
                    intent.putExtra("idPasseio", idPasseio);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("passeios").child(idPasseio).child("entregouCachorro").addValueEventListener(entregouCachorroListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void watchPasseador (final String idPasseio) {
        mDatabase.child("passeios").child(idPasseio).child("localizacao").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Coordenadas coordenadasPasseador = dataSnapshot.getValue(Coordenadas.class);

                LatLng localizacao = new LatLng(coordenadasPasseador.getLatitude(), coordenadasPasseador.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(localizacao)
                        .zoom(17)
                        .build();

                // NA PRIMEIRA CRIA O MARKER
                if (marker == null) {
                    marker = mMap.addMarker(new MarkerOptions().position(localizacao));
                } else {
                    // DEPOIS APENAS MOVE ELE DE ACORDO COM A ALTERACAO NO FIREBASE
                    animateMarker(marker, localizacao, true);
                }
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}