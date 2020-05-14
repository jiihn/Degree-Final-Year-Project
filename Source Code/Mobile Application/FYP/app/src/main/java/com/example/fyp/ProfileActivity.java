package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fyp.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.security.Key;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView image_profile;
    TextView first_name, last_name;
    TextView city, state, phoneNum, address, postcode;
    ImageButton editBtn;
    String selectedState, selectedCity;

    DatabaseReference reference, getUser;
    FirebaseUser fuser;
    String userId;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_profile = findViewById(R.id.profile_image);
        first_name = findViewById(R.id.fname);
        last_name = findViewById(R.id.lname);
        city = findViewById(R.id.city_txt);
        state = findViewById(R.id.state_txt);
        address = findViewById(R.id.address_txt);
        postcode = findViewById(R.id.postcode_txt);
        phoneNum = findViewById(R.id.phoneNum_txt);
        editBtn = findViewById(R.id.editBtn);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        userId = fuser.getUid();
        storageReference = FirebaseStorage.getInstance().getReference("profilePicture").child(fuser.getUid());
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                first_name.setText(user.getFirst_name());
                last_name.setText(user.getLast_name());
                city.setText(user.getCity());
                state.setText(user.getState());
                address.setText(user.getAddress());
                postcode.setText(user.getPostcode());
                phoneNum.setText(user.getPhone_number());

                if (user.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
    }

    public void showEditProfileDialog() {
        String options[] = {"Edit Profile Picture", "Edit First Name", "Edit Last Name", "Edit Phone Number", "Edit Address", "Edit Postcode", "Edit Location"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

        builder.setTitle("Choose Action");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openImage();
                } else if (which == 1) {
                    showEditDialog("first_name", "First Name");
                } else if (which == 2) {
                    showEditDialog("last_name", "Last Name");
                } else if (which == 3) {
                    showEditDialog("phone_number", "Phone Number");
                } else if (which == 4) {
                    showEditDialog("address", "Address");
                } else if (which == 5) {
                    showEditDialog("postcode", "Post Code");
                } else if (which == 6) {
                    showLocationDialog("state", "city", "Location");
                }
            }
        });

        builder.create().show();
    }

    public void showLocationDialog(final String state, final String city, final String title) {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        builder.setTitle(title);
        Spinner stateSpinner = mView.findViewById(R.id.state_spinner);
        final Spinner citySpinner = mView.findViewById(R.id.city_spinner);


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

        final String penang[] = {"Select a city", "Ayer Itam", "Balik Pulau", "Batu Ferringhi", "Batu Maung", "Bayan Lepas", "Bukit Mertajam", "Butterworth", "Gelugor",
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


        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(ProfileActivity.this, R.layout.support_simple_spinner_dropdown_item, stateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedState = stateList[position];

                if (position == 0) {
                    ArrayAdapter<String> cityAdapter1 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, def);
                    citySpinner.setAdapter(cityAdapter1);
                }

                if (position == 1) {
                    ArrayAdapter<String> cityAdapter1 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, johor);
                    citySpinner.setAdapter(cityAdapter1);
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = johor[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 2) {
                    ArrayAdapter<String> cityAdapter2 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, kedah);
                    citySpinner.setAdapter(cityAdapter2);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = kedah[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 3) {
                    ArrayAdapter<String> cityAdapter3 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, kelantan);
                    citySpinner.setAdapter(cityAdapter3);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = kelantan[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 4) {
                    ArrayAdapter<String> cityAdapter4 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, melaka);
                    citySpinner.setAdapter(cityAdapter4);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = melaka[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 5) {
                    ArrayAdapter<String> cityAdapter5 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, negerisembilan);
                    citySpinner.setAdapter(cityAdapter5);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = negerisembilan[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 6) {
                    ArrayAdapter<String> cityAdapter6 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, pahang);
                    citySpinner.setAdapter(cityAdapter6);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = pahang[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 7) {
                    ArrayAdapter<String> cityAdapter7 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, penang);
                    citySpinner.setAdapter(cityAdapter7);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = penang[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 8) {
                    ArrayAdapter<String> cityAdapter8 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, perak);
                    citySpinner.setAdapter(cityAdapter8);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = perak[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 9) {
                    ArrayAdapter<String> cityAdapter9 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, perlis);
                    citySpinner.setAdapter(cityAdapter9);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = perlis[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 10) {
                    ArrayAdapter<String> cityAdapter10 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, sabah);
                    citySpinner.setAdapter(cityAdapter10);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = sabah[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 11) {
                    ArrayAdapter<String> cityAdapter11 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, sarawak);
                    citySpinner.setAdapter(cityAdapter11);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = sarawak[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 12) {
                    ArrayAdapter<String> cityAdapter12 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, selangor);
                    citySpinner.setAdapter(cityAdapter12);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCity = selangor[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                if (position == 13) {
                    ArrayAdapter<String> cityAdapter13 = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, terengganu);
                    citySpinner.setAdapter(cityAdapter13);
                    selectedCity = citySpinner.getSelectedItem().toString();
                    citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        builder.setCancelable(false)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selectedState == "Select a state"){
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Update " + title +" Error: Please select a state", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(selectedCity == "Select a city"){
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Update " + title +" Error: Please select a city", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        HashMap<String, Object> result = new HashMap<>();
                        result.put(state, selectedState);
                        result.put(city, selectedCity);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Update Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pd.dismiss();
                        dialog.dismiss();
                    }
                });

        builder.setView(mView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showEditDialog(final String key, final String title) {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Update " + title);
        LinearLayout linearLayout = new LinearLayout(ProfileActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(ProfileActivity.this);
        editText.setHint("Enter " + title);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setCancelable(false);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(value)) {
                    if (key == "phone_number") {
                        String regexStr = "^[0-9]*$";
                        if (!value.matches(regexStr)) {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Please enter number only for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value.length() < 10) {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Please enter atlest 10 for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value.length() > 11) {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Please enter not more than 11 for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Edit Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else if (key == "postcode"){
                        String regexStr = "^[0-9]*$";
                        if (!value.matches(regexStr)) {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Please enter number only for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value.length() < 5 || value.length() > 5) {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Please enter atleast or no more than 5 number for " + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Edit Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        if (value.length() > 25) {
                            pd.dismiss();
                            Toast.makeText(ProfileActivity.this, "Please enter no more than 25 character for" + title, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        reference.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Edit Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    pd.dismiss();
                    Toast.makeText(ProfileActivity.this, "Edit Fail, Please Enter " + title, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.dismiss();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadimage() {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadimage();
            }
        }
    }

    public void status(final String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        getUser = FirebaseDatabase.getInstance().getReference().child("Users");

        getUser.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("status", status);

                    reference.updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
