package br.com.livroandroid.carros.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import br.com.livroandroid.carros.R;
import br.com.livroandroid.carros.domain.Carro;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class MapaFragment extends BaseFragment implements OnMapReadyCallback {


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Carro c = (Carro) getArguments().getSerializable("carro");
        
        if(c != null) {
            Log.d(TAG, "Carro " + c.nome + " > " + c.urlFoto + ", lat/lng " + c.latitude + "/" + c.longitude);

            LatLng location = new LatLng(Double.parseDouble(c.latitude), Double.parseDouble(c.longitude));

            map.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));

            map.addMarker(new MarkerOptions()
                    .title(c.nome)
                    .snippet(c.desc)
                    .position(location));

            // Other supported types include: MAP_TYPE_NORMAL,
            // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        
        
    }
}
