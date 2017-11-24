package com.faculdadedombosco.eduardopinheiro.walkdog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.faculdadedombosco.eduardopinheiro.walkdog.models.Coordenadas;
import com.faculdadedombosco.eduardopinheiro.walkdog.models.Notification;
import com.faculdadedombosco.eduardopinheiro.walkdog.models.NotificationContents;
import com.faculdadedombosco.eduardopinheiro.walkdog.models.NotificationData;
import com.faculdadedombosco.eduardopinheiro.walkdog.models.NotificationResponse;
import com.faculdadedombosco.eduardopinheiro.walkdog.models.Usuario;
import com.faculdadedombosco.eduardopinheiro.walkdog.services.NotificationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapaPasseadorActivity extends FragmentActivity implements
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
    private String idPasseio;
    private ValueEventListener solicitanteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapa_passeador);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String idPasseio = getIntent().getStringExtra("idPasseio");
        this.idPasseio = idPasseio;

        this.watchSolicitante(idPasseio);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // ENVIA NOTIFICACAO PARA TODO MUNDO
        DatabaseReference nomeRef = mDatabase.child("usuarios").child(currentUser.getUid());
        nomeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mDatabase.child("recebedoresNotificacao").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshotRecebedores) {
                        Log.d("A", dataSnapshotRecebedores.getValue().toString());

                        HashMap<String, String> map = (HashMap<String, String>) dataSnapshotRecebedores.getValue();

                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        double nota = getMedia(usuario.getNotas());

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://onesignal.com/api/v1/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        NotificationService service = retrofit.create(NotificationService.class);
                        Notification notification = new Notification();
                        notification.setApp_id("ff663e58-33b6-49a1-94d0-c70fad900aa8");

                        ArrayList includePlayersId = new ArrayList<String>();

                        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
                        String userIdNotification = status.getSubscriptionStatus().getUserId();

                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            String userId = entry.getKey();

                            if (!userId.equals(userIdNotification)) {
                                includePlayersId.add(userId);
                            }
                        }

                        notification.setInclude_player_ids(includePlayersId);

                        NotificationData data = new NotificationData();
                        data.setIdPasseio(idPasseio);
                        data.setIdUsuario(currentUser.getUid());
                        notification.setData(data);

                        NotificationContents contents = new NotificationContents();
                        contents.setEn("O usuário " + usuario.getNome() + "(Nota: " + nota + ") está saindo para passear deseja que ele leve seu cachorro?");
                        notification.setContents(contents);

                        List<com.faculdadedombosco.eduardopinheiro.walkdog.models.Button> buttons = new ArrayList<>();

                        com.faculdadedombosco.eduardopinheiro.walkdog.models.Button buttonSim = new com.faculdadedombosco.eduardopinheiro.walkdog.models.Button();
                        buttonSim.setId("id1");
                        buttonSim.setText("Sim");

                        buttons.add(buttonSim);

                        com.faculdadedombosco.eduardopinheiro.walkdog.models.Button buttonNao = new com.faculdadedombosco.eduardopinheiro.walkdog.models.Button();
                        buttonSim.setId("id2");
                        buttonSim.setText("Não");

                        buttons.add(buttonNao);

                        notification.setButtons(buttons);

                        service.create(notification).enqueue(new Callback<NotificationResponse>() {
                            @Override
                            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                                if (response.isSuccessful()) {
                                    Log.d("SUCESSO", response.body().toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                                Log.d("ERRO", t.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
        mLocationRequest.setInterval(5000);

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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

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
        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // centro de poa como default
        LatLng localizacao = new LatLng(-30.0354498,-51.2265377);

        // se conseguir pega pela location
        if (mLocation != null) {
            localizacao = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(localizacao)
                .zoom(17)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
        // A CADA (DEFINIDO no metodo onConnected<mLocationRequest.setInterval(5000)>) SEGUNDOS
        // PEGA A LOCALIZACAO DO PASSEADOR E ATUALIZA O PASSEIO
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

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // se conseguir pega pela location
        if (mLocation != null) {
            Coordenadas coordenadas = new Coordenadas();
            coordenadas.setLatitude(mLocation.getLatitude());
            coordenadas.setLongitude(mLocation.getLongitude());

            mDatabase.child("passeios").child(idPasseio).child("localizacao").setValue(coordenadas);
        }
    }

    public void watchSolicitante (final String idPasseio) {
        ValueEventListener solicitanteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();

                if (key.equals("buscouCachorro")|| key.equals("passeioAceito") || key.equals("entregueiCachorro")) {
                    return;
                }

                Usuario usuarioSolicitante = dataSnapshot.getValue(Usuario.class);

                // MOSTRA A CONFIRMACAO PARA O PASSEADOR E A LOCALIZACAO DO USUARIO QUE SOLICITOU
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
                if (usuarioSolicitante != null) {
                    confirm(usuarioSolicitante, currentUser.getUid(), idPasseio);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("passeios").child(idPasseio).child("solicitante").addValueEventListener(solicitanteListener);
        this.solicitanteListener = solicitanteListener;
    }

    public void confirm (final Usuario usuarioSolicitante, final String idUsuario, final String idPasseio) {
        final MapaPasseadorActivity self = this;

        mDatabase.child("usuarios").child(usuarioSolicitante.getId()).child("nome").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nome = dataSnapshot.getValue(String.class);

                new AlertDialog.Builder(self)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Novo Solicitante")
                .setMessage("O usuário " + nome + " deseja que você leve o cachorro dele, você aceita?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ATUALIZA A KEY NO BANCO
                        mDatabase.child("passeios").child(idPasseio).child("passeioAceito").setValue("S");

                        // PEGA A LOCALIZACAO DO SOLICITANTE
                        LatLng usuarioSolicitanteLocation = new LatLng(usuarioSolicitante.getCoordenadas().getLatitude(), usuarioSolicitante.getCoordenadas().getLongitude());

                        // ADICIONA MARCADOR NO SOLICITANTE
                        mMap.addMarker(new MarkerOptions()
                                .position(usuarioSolicitanteLocation)
                                .title("Solicitante"));

                        // MOVE O MAPA PARA O MARCADOR DO SOLICITANTE
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(usuarioSolicitanteLocation)
                                .zoom(18)
                                .build();

                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        Button btnBuscar = findViewById(R.id.btnBuscar);
                        btnBuscar.setText("BUSQUEI O CÃO");
                        btnBuscar.setEnabled(true);

                        mDatabase.child("passeios").child(idPasseio).removeEventListener(solicitanteListener);
                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("passeios").child(idPasseio).child("passeioAceito").setValue("N");
                        mDatabase.child("passeios").child(idPasseio).removeEventListener(solicitanteListener);
                    }

                })
                .show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void busqueiCachorro (View view) {
        Button btnBuscar = findViewById(R.id.btnBuscar);
        btnBuscar.setVisibility(View.GONE);

        Button btnEntregar = findViewById(R.id.btnEntregar);
        btnEntregar.setVisibility(View.VISIBLE);

        // ATUALIZA O BANCO E O SOLICITANTE COMECA A ACOMPANHAR O PASSEADOR
        mDatabase.child("passeios").child(this.idPasseio).child("buscouCachorro").setValue(true);
    }

    public void entregueiCachorro (View view) {
        // ATUALIZA O BANCO E VAI PARA TELA HOME
        mDatabase.child("passeios").child(this.idPasseio).child("entregouCachorro").setValue(true);
        Intent intent = new Intent(MapaPasseadorActivity.this, NovoPasseioActivity.class);
        startActivity(intent);
        finish();
    }

    public double getMedia (List<String> notas) {
        if (notas == null || notas.size() == 0) {
            return 5;
        }

        List<Integer> numbers = new ArrayList<Integer>();

        for (int i=0; i<notas.size(); i++) {
            int nota = Integer.valueOf(notas.get(i).replace(".0", ""));
            numbers.add(nota);
        }

        int sum = 0;

        for(int i=0; i < numbers.size() ; i++) {
            sum = sum + numbers.get(i);
        }

        double average = sum / numbers.size();

        if (average == 0) {
            return 5;
        }

        return average;
    }
}