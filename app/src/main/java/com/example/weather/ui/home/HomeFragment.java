package com.example.weather.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.weather.R;
import com.example.weather.RainActivity;
import com.example.weather.UserHomeActivity;
import com.example.weather.databinding.ActivityMapsBinding;
import com.example.weather.databinding.FragmentGalleryBinding;
import com.example.weather.databinding.FragmentHomeBinding;
import com.example.weather.ui.gallery.GalleryViewModel;
import com.example.weather.ui.map.MapsActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeFragment homeFragment;
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private CardView rainCard,floodCard;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rainCard = root.findViewById(R.id.weddingCard);
        floodCard = root.findViewById(R.id.photographyCard);

        rainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RainActivity.class);
                startActivity(intent);
            }
        });

        floodCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(requireContext());
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialogue_box, null
                );
                builder.setView(view);
                final AlertDialog alertDialog = builder.create();

                alertDialog.setCancelable(true);
                EditText lvl = view.findViewById(R.id.lvlMeasurement);
                Button enterLvl = view.findViewById(R.id.enterBtn);
                enterLvl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int level = 1560;

                        int userEnteredLevel = Integer.parseInt(lvl.getText().toString());
                        if (TextUtils.isEmpty(lvl.toString())){
                            lvl.setError("Enter Level...");
                        }

                        if (userEnteredLevel == (level) || userEnteredLevel > level){
                            Toast.makeText(requireContext(), "Flood is predicted...", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(requireContext(), "No Flood is predicted...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                alertDialog.show();
            }
        });


        Intent intent = new Intent(getContext(), MapsActivity.class);
        startActivity(intent);




        return root;
    }
}