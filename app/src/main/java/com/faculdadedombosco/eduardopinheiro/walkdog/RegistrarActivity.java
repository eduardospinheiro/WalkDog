package com.faculdadedombosco.eduardopinheiro.walkdog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.faculdadedombosco.eduardopinheiro.walkdog.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrarActivity extends AppCompatActivity {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private AutoCompleteTextView mNomeView;
    private AutoCompleteTextView mCpfView;
    private AutoCompleteTextView mTelefoneView;
    private View mProgressView;
    private View mLoginFormView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "Registrar";
    private DatabaseReference mDatabase;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void registrar (View view) {
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mCpfView = findViewById(R.id.cpf);
        mNomeView = findViewById(R.id.nome);
        mTelefoneView = findViewById(R.id.telefone);

        final String email = mEmailView.getText().toString();
        final String senha = mPasswordView.getText().toString();
        final String nome = mNomeView.getText().toString();
        final String cpf = mCpfView.getText().toString();
        final String telefone = mTelefoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nome)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mNomeView;
            cancel = true;
        }

        if (TextUtils.isEmpty(telefone)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mTelefoneView;
            cancel = true;
        }

        if (TextUtils.isEmpty(cpf)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mCpfView;
            cancel = true;
        } else if (!isCpfValid(cpf)) {
            mEmailView.setError("CPF Inválido.");
            focusView = mCpfView;
            cancel = true;
        }

        if (TextUtils.isEmpty(senha)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(senha) && !isPasswordValid(senha)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            progress = new ProgressDialog(this);
            progress.setTitle("Carregando");
            progress.setMessage("Cadastrando usuário, um momento...");
            progress.setCancelable(false);
            progress.show();

            // CRIAR O USUARIO
            mAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // LOGA O USUARIO NO FIREBASE PARA TERMOS O ID NA TABELA DE USUARIO TAMBEM
                    mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.getResult().getUser() != null) {
                                FirebaseUser currentUser = task.getResult().getUser();
                                Usuario usuario = new Usuario();
                                usuario.setCpf(cpf);
                                usuario.setNome(nome);
                                usuario.setTelefone(telefone);
                                usuario.setEmail(email);
                                usuario.setId(currentUser.getUid());

                                mDatabase.child("usuarios").child(currentUser.getUid()).setValue(usuario);

                                Toast.makeText(RegistrarActivity.this, "Usuário cadastrado com sucesso.",
                                        Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        }
                    });
                }
            });
        }
    }

    private boolean isCpfValid(String cpf) {
        //TODO: Replace this with your own logic
        return cpf.length() == 11;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }
}
