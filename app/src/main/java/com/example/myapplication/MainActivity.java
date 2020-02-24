package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public Button boton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boton = (Button) findViewById(R.id.boton);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               User myUser = (User) User.create("USUARIO");
                myUser.setNombre("Julen");
                myUser.setAp1("Mer");
                myUser.setAp2("Flo");
                myUser.setDni("734497819R");
                myUser.setEmail("julenmerchan@hotmail.com");
                myUser.setPassword("123456");
                myUser.setPuntuacion(0);
                myUser.setVotos(0);
                myUser.saveInBackground(); //Guardar el objeto
            }
        });
    }

    public void onclick(){
        User myUser = (User) User.create("User");
        myUser.setNombre("Julen");
        myUser.setAp1("Merchan");
        myUser.setAp2("Flores");
        myUser.setDni("73449719R");
        myUser.setEmail("julenmerchan@hotmail.com");
        myUser.setPassword("123456");
        myUser.setPuntuacion(0);
        myUser.setVotos(0);
        System.out.println("HOLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaa");
        myUser.saveInBackground();
        System.out.println("HOLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaa");
    }
}
