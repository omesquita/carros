package br.com.livroandroid.carros.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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


    private GoogleMap map;
    private Carro c;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        setHasOptionsMenu(true);

        this.c = (Carro) getArguments().getSerializable("carro");

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        if(c != null) {
            Log.d(TAG, "Carro " + c.nome + " > " + c.urlFoto + ", lat/lng " + c.latitude + "/" + c.longitude);

            LatLng location = new LatLng(Double.parseDouble(c.latitude), Double.parseDouble(c.longitude));

            //map.setMyLocationEnabled(true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_mapa, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(map != null && c != null) {
            if (item.getItemId() == R.id.action_location_carro) {
                // Posiciona mapa na localização da fábrica
                LatLng location = new LatLng(Double.parseDouble(c.latitude), Double.parseDouble(c.longitude));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
            }else if (item.getItemId() == R.id.action_location_directions) {
                // Posiciona mapa no usuário
                toast("directions");
            } else if (item.getItemId() == R.id.action_zoom_in) {
                toast("zoom +");
                map.animateCamera(CameraUpdateFactory.zoomIn());
            } else if (item.getItemId() == R.id.action_zoom_out) {
                toast("zoom -");
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
            else if (item.getItemId() == R.id.action_mapa_normal) {
                // Modo Normal
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                // Modo Satélite
            } else if (item.getItemId() == R.id.action_mapa_satelite) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (item.getItemId() == R.id.action_mapa_terreno) {
                // Modo Terreno
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (item.getItemId() == R.id.action_mapa_hibrido) {
                // Modo Híbrido
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
