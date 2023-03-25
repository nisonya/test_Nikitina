package com.example.test_nikitina;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.test_nikitina.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    TextView txt;
    MusclesAdapter musclesAdapter;
    ExersiseAdapter exersiseAdapter;
    RecyclerView musclesList, exersiseList;
    ArrayList<Muscles> muscles;
    ArrayList<Exersise> exersises;
    List<Integer> id_grp= Base.getId_group();
    List<Integer> id_musle= Base.getId_musle();
    List<String> names= Base.getName_muscle();
    List<String> name_exer= Base.getName_exer();
    List<String> photo= Base.getPhoto();
    List<Integer> descr= Base.getDescr();
    private static final String FILE_NAME="MY_FILE_NAME";
    private static final String URL_STRING="URL_STRING";
    String url_FB;
    SharedPreferences sPref;
    SharedPreferences.Editor ed;
    private FirebaseRemoteConfig mfirebaseRemoteConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //проверка сохранена ли ссылка
        if((getSharedPrefStr()=="0")||(getSharedPrefStr()=="")) {
            System.out.println("SharedPre 00000000000000000");
            Toast.makeText(MainActivity.this,"SharedPre 00000000000000000", Toast.LENGTH_SHORT).show();
            //подключение к FireBase
            getFireBaseUrlConnection();
            getURLStr();
            //проверка эмулятора и есть ли ссылка на FireBase
            //if ((url_FB == "")||checkIsEmu()) {
            if (url_FB == "0") {
                System.out.println("FireBase 00000000000000000");
                Toast.makeText(MainActivity.this,"FireBase 00000000000000000", Toast.LENGTH_SHORT).show();
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                txt = (TextView) findViewById(R.id.textView);
                //recycler
                musclesList = findViewById(R.id.RecyclerMuscles);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                musclesList.setLayoutManager(layoutManager);
                //recycler listener
                MusclesAdapter.OnMusclesClickListener onMusclesClickListener = new MusclesAdapter.OnMusclesClickListener() {
                    @Override
                    public void onMusclesClick(Muscles muslesItem) {
                        Toast.makeText(getApplicationContext(), String.valueOf(muslesItem.getId()),
                                Toast.LENGTH_SHORT).show();
                    }
                };

                //add muscles data
                muscles = new ArrayList<>();
                getMuscleForGroup(1);
                // recycler adapter
                musclesAdapter = new MusclesAdapter(muscles, onMusclesClickListener);
                musclesList.setAdapter(musclesAdapter);
                //recycler for Exersise
                exersiseList = findViewById(R.id.RecyclerExersise);
                LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                exersiseList.setLayoutManager(layoutManager2);
                //add muscles data
                exersises = new ArrayList<>();
                getExForMuscles(1);
                // recycler adapter
                exersiseAdapter = new ExersiseAdapter(exersises);
                exersiseList.setAdapter(exersiseAdapter);
                //bottom navigation
                binding.bottomNavigationView.setOnItemSelectedListener(item -> {

                    switch (item.getItemId()) {
                        case R.id.chest:
                            txt.setText("Chest + triceps");
                            getMuscleForGroup(2);
                            musclesList.setAdapter(musclesAdapter);
                            break;
                        case R.id.legs:
                            txt.setText("Legs + shoulders");
                            getMuscleForGroup(1);
                            musclesList.setAdapter(musclesAdapter);
                            break;
                        case R.id.biceps:
                            txt.setText("Biceps + back");
                            getMuscleForGroup(3);
                            musclesList.setAdapter(musclesAdapter);
                            break;
                    }
                    return true;
                });
            }
            else{ //Ссылка есть, сохраняем её в SharedPreferences и запускаем WebView
                System.out.println("FireBase 111111111111111");
                Toast.makeText(MainActivity.this,"FireBase 111111111111111", Toast.LENGTH_SHORT).show();
                ed = sPref.edit();
                ed.putString(URL_STRING, url_FB);
                ed.apply();
                browse();
            }
        }else{
            System.out.println("SharedPre 1111111111111");
            Toast.makeText(MainActivity.this,"SharedPre 1111111111111", Toast.LENGTH_SHORT).show();
            //проверка на подключение к интернету
            if(!hasConnection(this)){
                Intent intent = new Intent(MainActivity.this, InernetNone.class);
                startActivity(intent);
            }
            else{//запускаем WebView
                browse();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //myDBManeger.openDB();
    }

    public void browse(){
        Intent intent = new Intent(MainActivity.this, MainActivity3.class);
        startActivity(intent);
    }
    public void getMuscleForGroup(int id_group){
        muscles.clear();
        for(int i = 0; i<12; i++){
            if(id_grp.get(i)==id_group){
                Muscles mMuscle = new Muscles(i+1,id_grp.get(i),names.get(i));
                muscles.add(mMuscle);
            }
        }
    }
    public void getExForMuscles(int id_muscle){
        exersises.clear();
        for(int i = 0;i < id_musle.size(); i++){
            if(id_musle.get(i)==id_muscle){
                Exersise mExersise= new Exersise(i+1,id_musle.get(i),name_exer.get(i), photo.get(i), descr.get(i));
                exersises.add(mExersise);
            }
        }
    }

    private boolean checkIsEmu() {
        if (BuildConfig.DEBUG) return false;
        String phoneModel = Build.MODEL;
        String buildProduct = Build.PRODUCT;
        String buildHardware = Build.HARDWARE;
        String brand = Build.BRAND;
        return (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.toLowerCase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || buildHardware.equals("goldfish")
                || brand.contains("google")
                || buildHardware.equals("vbox86")
                || buildProduct.equals("sdk")
                || buildProduct.equals("google_sdk")
                || buildProduct.equals("sdk_x86")
                || buildProduct.equals("vbox86p")
                || Build.BOARD.toLowerCase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.toLowerCase(Locale.getDefault()).contains("nox")
                || buildHardware.toLowerCase(Locale.getDefault()).contains("nox")
                || buildProduct.toLowerCase(Locale.getDefault()).contains("nox"))
                || (brand.startsWith("generic") && Build.DEVICE.startsWith("generic"));
    }

    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }
    public void getURLStr(){
        mfirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if(task.isSuccessful()){
                            boolean updated = task.getResult();
                            Log.i("Fire", String.valueOf(task.getResult()));
                            if(task.getResult()) {
                                url_FB = mfirebaseRemoteConfig.getString("key_URL");
                                System.out.println(url_FB + "-_-_-_---_-_-_-------_-------_-_---_--_____---");
                            }else{
                                Toast.makeText(MainActivity.this,"String is clear", Toast.LENGTH_SHORT).show();
                                url_FB ="";
                                System.out.println(url_FB + "-_-_-_---_-_-_-------_-------_-_---_--_____---");
                            }
                        }
                        else{
                            Toast.makeText(MainActivity.this,"Failed", Toast.LENGTH_SHORT).show();
                            url_FB ="";
                        }
                    }
                });
    }

    public String getSharedPrefStr(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String url_SP = sPref.getString(URL_STRING,"");
        return url_SP;
    }

    public void getFireBaseUrlConnection(){
        //подключение к FireBase
        mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mfirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mfirebaseRemoteConfig.setDefaultsAsync(R.xml.url_values);
    }
}