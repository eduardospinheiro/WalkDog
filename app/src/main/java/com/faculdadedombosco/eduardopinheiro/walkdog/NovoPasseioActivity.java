package com.faculdadedombosco.eduardopinheiro.walkdog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.faculdadedombosco.eduardopinheiro.walkdog.models.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class NovoPasseioActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    String idPasseio = null;

    private DatabaseReference mDatabase;
    private ValueEventListener mPasseioListener;
    private static final String TAG = "NovoPasseioActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private Coordenadas mCoordenadasUsuario;
    private ValueEventListener passeiosListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_passeio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaMapaPasseador();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String userIdNotification = status.getSubscriptionStatus().getUserId();

        mDatabase.child("recebedoresNotificacao").child(userIdNotification).setValue(userIdNotification);

        FirebaseUser user = mAuth.getCurrentUser();

        mDatabase.child("usuarios").child(user.getUid()).child("oneSignalPlayerId").setValue(userIdNotification);

        //SE VEM DE UMA PUSH NOTIFICATION TEM ID
        final String idPasseio = getIntent().getStringExtra("idPasseio");

        if (idPasseio != null) {
            DatabaseReference passeioRef = mDatabase.child("passeios").child(idPasseio);
            passeioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final ValueEventListener selfListener = this;
                    final Passeio passeio = dataSnapshot.getValue(Passeio.class);

                    new android.os.Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (passeio != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        confirm(passeio);
                                        mDatabase.child("passeios").child(idPasseio).removeEventListener(selfListener);
                                    }
                                });
                            } else {
                                watchPasseios();
                            }
                        }
                    },1000);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    watchPasseios();
                }
            });
        } else {
            this.watchPasseios();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_novo_passeio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.toString().equals("Logout")) {
            // A CLASSE App.java ESCUTA O LOGOUT E MANDA PRA LoginActivity AUTOMATICAMENTE
            mAuth.signOut();
            Intent intent = new Intent(NovoPasseioActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void irParaMapaPasseador() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        DatabaseReference nomeRef = mDatabase.child("usuarios").child(currentUser.getUid()).child("nome");
        nomeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Usuario usuario = new Usuario();
                Passeio passeio = new Passeio();

                String nome = (String) snapshot.getValue();
                usuario.setNome(nome);

                usuario.setId(currentUser.getUid());
                passeio.setUsuario(usuario);

                if (ActivityCompat.checkSelfPermission(NovoPasseioActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NovoPasseioActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                Coordenadas coordenadas = new Coordenadas();

                if (mLocation != null) {
                    double varLatitude = mLocation.getLatitude();
                    coordenadas.setLatitude(mLocation.getLatitude());
                    coordenadas.setLongitude(mLocation.getLongitude());
                } else {
                    coordenadas.setLatitude(-30.0354498);
                    coordenadas.setLongitude(-51.2265377);
                }

                passeio.setLocalizacao(coordenadas);
                passeio.setBuscouCachorro(false);
                passeio.setDataCriacao(new Date().getTime());

                // CRIA UM ID UNICO PARA O PASSEIO
                String uuid = UUID.randomUUID().toString();
                String idPasseio = currentUser.getUid() + uuid;
                idPasseio = idPasseio;

                passeio.setId(idPasseio);

                mDatabase.child("passeios").child(idPasseio).setValue(passeio);

                //salva o passeio e vai para  a proxima tela passando o ID criado para o passeio para poder monitorar
                Intent intent = new Intent(NovoPasseioActivity.this, MapaPasseadorActivity.class);
                intent.putExtra("idPasseio", idPasseio);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(30000); // Update location every second

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

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void watchPasseios () {
        ValueEventListener passeiosListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Passeio passeio = messageSnapshot.getValue(Passeio.class);

                    // PASSEIO CRIADO, PEGA OS VALORES E MOSTRA PRO USUARIO ACEITAR OU RECUSAR
                    // SOH MOSTRA O ALERT CASO NAO TENHA SIDO CRIADO POR TI, OU SEJA
                    // CASO TU NAO SEJA O PASSEADOR
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (passeio != null && passeio.getUsuario() != null && passeio.getUsuario().getId() != currentUser.getUid() && passeio.getSolicitante() == null) {
                        confirm(passeio);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // PEGA OS PASSEIOS CRIADOS A PARTIR DO MOMENTO DO LOGIN PARA EVITAR QUE ELE ENXERGUE PASSEIOS ANTIGOS
        mDatabase.child("passeios").orderByChild("dataCriacao").startAt(new Date().getTime()).limitToLast(1).addValueEventListener(passeiosListener);
        this.passeiosListener = passeiosListener;
    }

    public void confirm (final Passeio passeio) {
        final NovoPasseioActivity self = this;

        double nota = getMedia(passeio.getUsuario().getNotas());
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Novo Passeio")
        .setMessage("O usuário " + passeio.getUsuario().getNome() + "(Nota: " + nota + ") está saindo para passear deseja que ele leve seu cachorro?")
        .setPositiveButton("Sim", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verificaSolicitante(self, currentUser.getUid(), passeio);
            }

        })
        .setNegativeButton("Não", null)
        .show();
    }

    public void verificaSolicitante (final NovoPasseioActivity self, final String idUsuario, final Passeio passeio) {
        final ValueEventListener passeioSolicitanteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuarioSolicitanteExistente = dataSnapshot.getValue(Usuario.class);

                if (usuarioSolicitanteExistente == null) {
                    Usuario usuarioSolicitante = new Usuario();
                    usuarioSolicitante.setId(idUsuario);

                    if (ActivityCompat.checkSelfPermission(self, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(self, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    Coordenadas coordenadas = new Coordenadas();

                    if (mLocation != null) {
                        double varLatitude = mLocation.getLatitude();
                        coordenadas.setLatitude(mLocation.getLatitude());
                        coordenadas.setLongitude(mLocation.getLongitude());
                    }

                    usuarioSolicitante.setCoordenadas(coordenadas);

                    mDatabase.child("passeios").child(passeio.getId()).child("solicitante").setValue(usuarioSolicitante);

                    mDatabase.child("passeios").child(passeio.getId()).child("solicitante").removeEventListener(this);

                    watchAceitacao(passeio.getId());
                } else {
                    alert("Desculpe, já existe um solicitante para esse passeio.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("passeios").child(passeio.getId()).child("solicitante").addValueEventListener(passeioSolicitanteListener);
    }

    public void watchAceitacao (final String idPasseio) {
        ValueEventListener passeioAceitoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String passeioAceito = dataSnapshot.getValue(String.class);

                if (passeioAceito != null && passeioAceito.equals("S")) {
                    Intent intent = new Intent(NovoPasseioActivity.this, MapaSolicitanteActivity.class);
                    intent.putExtra("idPasseio", idPasseio);
                    startActivity(intent);
                } else if (passeioAceito != null && passeioAceito.equals("N")) {
                    alert("O passeador não aceitou o passeio!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("passeios").child(idPasseio).child("passeioAceito").addValueEventListener(passeioAceitoListener);
    }

    public void alert (String mensagem) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Aviso!")
        .setMessage(mensagem)
        .setPositiveButton("Ok", null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
    }

    public double getMedia (List<String> notas) {
        if (notas == null || notas.size() == 0) {
            return 5;
        }

        List<Integer> numbers = new ArrayList<Integer>();

        for (int i=0; i<notas.size(); i++) {
            int nota = Integer.valueOf(notas.get(i));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (passeiosListener != null) {
            mDatabase.child("passeios").removeEventListener(passeiosListener);
        }
    }
}
