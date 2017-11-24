package com.faculdadedombosco.eduardopinheiro.walkdog;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.faculdadedombosco.eduardopinheiro.walkdog.models.Passeio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AvaliarActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String idPasseio;
    public RatingBar ratingBar;
    public TextView comentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avaliar);

        idPasseio = getIntent().getStringExtra("idPasseio");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        comentarios = (TextView) findViewById(R.id.comentarios);
    }

    public void avaliar (View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        ValueEventListener passeioListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Passeio passeio = dataSnapshot.getValue(Passeio.class);

                if (passeio != null) {
                    String rating = String.valueOf(ratingBar.getRating());

                    List notasList = new ArrayList();
                    notasList.add(rating);

                    // ADICIONA A LISTA PARA O FIREBASE NAO RECRIAR
                    if (passeio.getUsuario() != null && passeio.getUsuario().getNotas() != null) {
                        for (int i=0; i<passeio.getUsuario().getNotas().size(); i++) {
                            String nota = passeio.getUsuario().getNotas().get(i);

                            notasList.add(nota);
                        }
                    }

                    // ADICIONA A NOTA A LISTA DO USUARIO
                    mDatabase.child("usuarios").child(passeio.getUsuario().getId()).child("notas").setValue(notasList);

                    // ATRIBUI COMENTARIO AO PASSEIO
                    mDatabase.child("passeios").child(passeio.getId()).child("comentario").setValue(comentarios.getText().toString());

                    Toast.makeText(AvaliarActivity.this, "Passeio avaliado com sucesso.",
                        Toast.LENGTH_SHORT).show();

                    mDatabase.child("passeios").child(idPasseio).removeEventListener(this);

                    Intent intent = new Intent(AvaliarActivity.this, NovoPasseioActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("passeios").child(this.idPasseio).addValueEventListener(passeioListener);
    }

}
