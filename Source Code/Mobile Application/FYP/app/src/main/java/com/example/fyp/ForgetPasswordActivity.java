package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgetPasswordActivity extends AppCompatActivity {

    EditText emailAddress;
    Button sendBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        emailAddress = findViewById(R.id.emailEt);
        sendBtn = findViewById(R.id.sendBtn);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
    }

    public void sendRequest(){
        progressBar.setVisibility(View.VISIBLE);
        String email= emailAddress.getText().toString();

        if(email.isEmpty()){
            emailAddress.setError("Please enter email address");
            emailAddress.requestFocus();
            progressBar.setVisibility(View.GONE);
        } else if(!isEmailValid(email)){
            emailAddress.setError("Please enter a valid email address");
            emailAddress.requestFocus();
            progressBar.setVisibility(View.GONE);
        } else if(isEmailValid(email)){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(ForgetPasswordActivity.this, "A reset has been sent to your email address", Toast.LENGTH_SHORT).show();
                                Intent intToLogin = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                                startActivity(intToLogin);
                            } else{
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(ForgetPasswordActivity.this, "Email address not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
