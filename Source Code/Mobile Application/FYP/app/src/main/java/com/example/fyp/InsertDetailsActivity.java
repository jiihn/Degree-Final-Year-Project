package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class InsertDetailsActivity extends AppCompatActivity {

    private Spinner state, city;
    private EditText fname, lname, pNum, address, postcodetxt;
    private Button btnconfirm;
    String emailAdd;
    Intent intent;
    FirebaseAuth mFirebaseAuth;
    private String selectedState, selectedCity;
    private DatabaseReference databaseReference;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Insert Details");

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        fname = findViewById(R.id.fnameText);
        lname = findViewById(R.id.lnameText);
        pNum = findViewById(R.id.phoneNumText);
        address = findViewById(R.id.addressText);
        postcodetxt = findViewById(R.id.postcodeText);
        btnconfirm = findViewById(R.id.confirmbtn);
        state = findViewById(R.id.stateSpinner);
        city = findViewById(R.id.citySpinner);

        addItemsOnStateSpinner();

        btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetupUser();
            }
        });
    }

    private void SetupUser(){
        final String firstName = fname.getText().toString().trim();
        final String lastName = lname.getText().toString().trim();
        final String phoneNum= pNum.getText().toString().trim();
        final String add = address.getText().toString().trim();
        final String pcode = postcodetxt.getText().toString().trim();
        final String selectedState = state.getSelectedItem().toString().trim();
        final String selectedCity = city.getSelectedItem().toString().trim();

        if(TextUtils.isEmpty(firstName)){
            fname.setError("Please enter your first name");
            fname.requestFocus();
            return;
        }

        if(firstName.length() > 10){
            fname.setError("Please enter your first name not more than 10 characters");
            fname.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(lastName)){
            lname.setError("Please enter your last name");
            lname.requestFocus();
            return;
        }

        if(lastName.length() > 10){
            lname.setError("Please enter your last name not more than 10 characters");
            lname.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(phoneNum)){
            pNum.setError("Please enter your phone number");
            pNum.requestFocus();
            return;
        }

        if (phoneNum.length() < 10 || phoneNum.length() > 11) {
            pNum.setError("Please ensure your phone number is no less than 10 or more than 11");
            pNum.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(add)){
            address.setError("Please enter your address");
            address.requestFocus();
            return;
        }

        if (add.length() > 25) {
            address.setError("Address too long");
            address.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(pcode)){
            postcodetxt.setError("Please enter your post code");
            postcodetxt.requestFocus();
            return;
        }

        if(pcode.length() != 5){
            postcodetxt.setError("Please ensure your post code is 5 character long");
            postcodetxt.requestFocus();
            return;
        }

        if(selectedState == "Select a state")
        {
            state.performClick();
            Toast.makeText(InsertDetailsActivity.this, "Please select a state", Toast.LENGTH_LONG).show();
            return;
        } else if(selectedCity == "Select a city"){
            city.performClick();
            Toast.makeText(InsertDetailsActivity.this, "Please select a city", Toast.LENGTH_LONG).show();
            return;
        }

        String getEmail = mFirebaseAuth.getCurrentUser().getEmail();

       HashMap userMap = new HashMap();
        userMap.put("id",currentUserId);
        userMap.put("first_name",firstName);
        userMap.put("last_name",lastName);
        userMap.put("phone_number",phoneNum);
        userMap.put("address",add);
        userMap.put("city",selectedCity);
        userMap.put("postcode",pcode);
        userMap.put("email",getEmail);
        userMap.put("icNum","none");
        userMap.put("icURL","default");
        userMap.put("verified",false);
        userMap.put("rejected",false);
        userMap.put("state",selectedState);
        userMap.put("ewallet",0);
        userMap.put("imageURL", "default");

        databaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(InsertDetailsActivity.this,HomepageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(InsertDetailsActivity.this, "Information has been updated",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(InsertDetailsActivity.this, "Error occured, please ensure that you have entered your information correctly",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addItemsOnStateSpinner() {
        final String stateList[] = {"Select a state", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Pulau Penang", "Perak", "Perlis", "Sabah",
                "Sarawak", "Selangor", "Terengganu"};

        final String def[] = {"Select a city"};

        final String johor[] = {"Select a city", "Ayer Baloi", "Ayer Hitam", "Ayer Tawar 2", "Bandar Penawar", "Bandar Tenggara", "Batu Anam", "Batu Pahat", "Bekok Benut",
                "Bukit Gambir", "Bukit Pasir", "Chaah Endau", " Gelang Patah", "Gerisek", "Gugusan Taib Andak", "Jementah", "Johor Bahru", "Kahang", "Kluang",
                "Kota Tinggi", "Kukup", "Kulai", "Labis", "Layang-Layang", "Masai", "Mersing", "Muar", "Nusajaya", "Pagoh", "Paloh", "Panchor", "Parit Jawa",
                "Parit Raja", "Parit Sulong", "Pasir Gudang", "Pekan Nenas", "Pengerang", "Pontian", "Pulau Satu", "Rengam", "Rengit", "Segamat", "Semerah", "Senai",
                "Senggarang", "Seri Gading", "Seri Medan", "Simpang Rengam", "Sungai Mati", "Tangkak", "Ulu Tiram", "Yong Peng"};

        final String kedah[] = {"Select a city", "Alor Setar", "Ayer Hitam", "Baling", "Bandar Baharu", "Bedong", "Bukit Kayu Hitam", "Changloon", "Gurun", "Jeniang", "Jitra",
                "Karangan", "Kepala Batas", "Kodiang", "Kota Kuala Muda", "Kota Sarang Semut", "Kuala Kedah", "Kuala Ketil", "Kuala Nerang", "Kuala Pegang",
                "Kulim", "Kupang", "Langgar", "Langkawi", "Lunas", "Merbok", "Padang Serai", "Pendang", "Pokok Sena", "Serdang", "Sik", "Simpang Empat",
                "Sungai Petani", "Universiti Utara Malaysia", "Yan"};

        final String kelantan[] = {"Select a city", "Ayer Lanas", "Bachok", "Cherang Ruku", "Dabong", "Gua Musang", "Jeli", "Kem Desa Pahlawan", "Ketereh", "Kota Bharu",
                "Kuala Balah", "Kuala Krai", "Machang", "Melor", "Pasir Mas", "Pasir Puteh", "Pulai Chondong", "Rantau Panjang", "Selising", "Tanah Merah",
                "Temangan", "Tumpat", "Wakaf Bharu"};

        final String melaka[] = {"Select a city", "Alor Gajah", "Asahan", "Ayer Keroh", "Bemban", "Durian Tunggal", "Jasin", "Kem Trendak", "Kuala Sungai Baru", "Lubok China",
                "Masjid Tanah", "Merlimau", "Selandar", "Sungai Rambai", "Sungai Udang", "Tanjong Kling"};

        final String negerisembilan[] = {"Select a city", "Bahau", "Bandar Enstek", "Bandar Seri Jempol", "Batu Kikir", "Gemas", "Gemencheh", "Johol", "Kota",
                "Kuala Klawang", "Kuala Pilah", "Labu", "Linggi", "Mantin", "Niai", "Nilai", "Port Dickson", "Pusat Bandar Palong", "Rantau",
                "Rembau", "Rompin", "Seremban", "Si Rusa", "Simpang Durian", "Simpang Pertang", "Tampin", "Tanjong Ipoh"};

        final String pahang[] = {"Select a city", "Balok", "Bandar Bera", "Bandar Pusat Jengka", "Bandar Pusat Jengka", "Bandar Tun Abdul Razak", "Benta", "Bentong",
                "Brinchang", "Bukit Fraser", "Bukit Goh", "Bukit Kuin", "Chenor", "Chini", "Damak", "Dong", "Gambang", "Genting Highlands", "Jerantut",
                "Karak", "Kemayan", "Kuala Krau", "Kuala Lipis", "Kuala Rompin", "Kuantan", "Lanchang", "Lurah Bilut", "Maran", "Mentakab", "Muadzam Shah",
                "Padang Tengku", "Pekan", "Raub", "Ringlet", "Sega", "Sungai Koyan", "Sungai Lembing", "Tanah Rata", "Temerloh", "Triang"};

        final String penang[] = {"Select a city", "Ayer Itam", "Balik Pulau", "Batu Ferringhi", "Batu Maung", "Bayan Lepas", "Bukit Mertajam","Butterworth", "Gelugor",
                "Jelutong", "Kepala Batas", "Kubang Semang", "Nibong Tebal", "Penaga", "Penang Hill", "Perai", "Permatang Pauh", "Pulau Pinang",
                "Simpang Ampat", "Sungai Jawi", "Tanjong Bungah", "Tasek Gelugor", "USM Pulau Pinang"};

        final String perak[] = {"Select a city", "Ayer Tawar", "Bagan Datoh", "Bagan Serai", "Bandar Seri Iskandar", "Batu Gajah", "Batu Kurau", "Behrang Stesen", "Bidor",
                "Bota", "Bruas", "Changkat Jering", "Changkat Keruing", "Chemor", "Chenderiang", "Chenderong Balai", "Chikus", "Enggor", "Gerik", "Gopeng",
                "Hutan Melintang", "Intan", "Ipoh", "Jeram", "Kampung Gajah", "Kampung Kepayang", "Kamunting", "Kuala Kangsar", "Kuala Kurau",
                "Kuala Sepetang", "Lambor Kanan", "Langkap", "Lenggong", "Lumut", "Malim Nawar", "Manong", "Matang", "Padang Rengas", "Pangkor",
                "Pantai Remis", "Parit", "Parit Buntar", "Pengkalan Hulu", "Pusing", "Rantau Panjang", "Sauk", "Selama", "Selekoh", "Seri Manjong",
                "Simpang", "Simpang Ampat Semanggol", "Sitiawan", "Slim River", "Sungai Siput", "Sungai Sumun", "Sungkai", "Taiping", "Tanjong Malim",
                "Tanjong Piandang", "Tanjong Rambutan", "Tanjong Tualang", "Tapah", "Tapah Road", "Teluk Intan", "Temoh", "TLDM Lumut", "Trolak", "Trong",
                "Tronoh", "Ulu Bernam"};

        final String perlis[] = {"Select a city", "Arau", "Kaki Bukit", "Kangar", "Kuala Perlis", "Padang Besar", "Simpang Ampat"};

        final String sabah[] = {"Select a city", "Beaufort", "Beluran", "Beverly", "Bongawan", "Inanam", "Keningau", "Kota Belud", "Kota Kinabalu", "Kota Kinabatangan",
                "Kota Marudu", "Kuala Penyu", "Kudat", "Kunak", "Lahad Datu", "Likas", "Membakut", "Menumbok", "Nabawan", "Pamol", "Papar", "Penampang",
                "Putatan", "Ranau", "Sandakan", "Semporna", "Sipitang", "Tambunan", "Tamparuli", "Tanjung Aru", "Tawau", "Tenghilan", "Tenom", "Tuaran"};

        final String sarawak[] = {"Select a city", "Asajaya", "Balingian", "Baram", "Bau", "Bekenu", "Belaga", "Belaga", "Belawai", "Betong", "Bintangor", "Bintulu", "Dalat",
                "Daro", "Debak", "Engkilili", "Julau", "Kabong", "Kanowit", "Kapit", "Kota Samarahan", "Kuching", "Lawas", "Limbang", "Lingga", "Long Lama",
                "Lubok Antu", "Lundu", "Lutong", "Matu", "Miri", "Mukah", "Nanga Medamit", "Niah", "Pusa", "Roban", "Saratok", "Sarikei", "Sebauh",
                "Sebuyau", "Serian", "Sibu", "Siburan", "Simunjan", "Song", "Spaoh", "Sri Aman", "Sundar", "Tatau"};

        final String selangor[] = {"Select a city", "Ampang", "Bandar Baru Bangi", "Bandar Puncak Alam", "Banting", "Batang Kali", "Batu Arang", "Batu Caves", "Beranang",
                "Bestari Jaya", "Bukit Rotan", "Cheras", "Cyberjaya", "Dengkil", "Hulu Langat", "Jenjarom", "Jeram", "Kajang", "Kapar", "Kerling", "Klang",
                "KLIA", "Kuala Kubu Baru", "Kuala Selangor", "Kuang", "Pelabuhan Klang", "Petaling Jaya", "Puchong", "Pulau Carey", "Pulau Indah",
                "Pulau Ketam", "Rasa", "Rawang", "Sabak Bernam", "Sekinchan", "Semenyih", "Sepang", "Serdang", "Serendah", "Seri Kembangan", "Shah Alam",
                "Subang Jaya", "Sungai Ayer Tawar", "Sungai Besar", "Sungai Buloh", "Sungai Pelek", "Tanjong Karang", "Tanjong Sepat", "Telok Panglima Garang"};

        final String terengganu[] = {"Select a city", "Ajil", "Al Muktatfi Billah Shah", "Ayer Puteh", "Bukit Besi", "Bukit Payong", "Ceneh", "Chalok", "Cukai", "Dungun",
                "Jerteh", "Kampung Raja", "Kemasek", "Kerteh", "Ketengah Jaya", "Kijal", "Kuala Berang", "Kuala Besut", "Kuala Terengganu", "Marang",
                "Paka", "Permaisuri", "Sungai Tong"};



        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, stateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state.setAdapter(stateAdapter);

        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedState = stateList[position];

                if(position == 0){
                    ArrayAdapter<String> cityAdapter1 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, def);
                    city.setAdapter(cityAdapter1);
                }

                if(position == 1){
                    ArrayAdapter<String> cityAdapter1 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, johor);
                    city.setAdapter(cityAdapter1);
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = johor[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 2){
                    ArrayAdapter<String> cityAdapter2 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, kedah);
                    city.setAdapter(cityAdapter2);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = kedah[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 3){
                    ArrayAdapter<String> cityAdapter3 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, kelantan);
                    city.setAdapter(cityAdapter3);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = kelantan[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 4){
                    ArrayAdapter<String> cityAdapter4 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, melaka);
                    city.setAdapter(cityAdapter4);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = melaka[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 5){
                    ArrayAdapter<String> cityAdapter5 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, negerisembilan);
                    city.setAdapter(cityAdapter5);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = negerisembilan[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 6){
                    ArrayAdapter<String> cityAdapter6 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, pahang);
                    city.setAdapter(cityAdapter6);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = pahang[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 7){
                    ArrayAdapter<String> cityAdapter7 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, penang);
                    city.setAdapter(cityAdapter7);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = penang[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 8){
                    ArrayAdapter<String> cityAdapter8 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, perak);
                    city.setAdapter(cityAdapter8);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = perak[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 9){
                    ArrayAdapter<String> cityAdapter9 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, perlis);
                    city.setAdapter(cityAdapter9);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = perlis[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 10){
                    ArrayAdapter<String> cityAdapter10 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, sabah);
                    city.setAdapter(cityAdapter10);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = sabah[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 11){
                    ArrayAdapter<String> cityAdapter11 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, sarawak);
                    city.setAdapter(cityAdapter11);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = sarawak[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 12){
                    ArrayAdapter<String> cityAdapter12 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, selangor);
                    city.setAdapter(cityAdapter12);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = selangor[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if(position == 13){
                    ArrayAdapter<String> cityAdapter13 = new ArrayAdapter<String>(InsertDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, terengganu);
                    city.setAdapter(cityAdapter13);
                    selectedCity = city.getSelectedItem().toString();
                    city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = terengganu[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                return;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
