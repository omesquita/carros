package br.com.livroandroid.carros.activity.prefs;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import br.com.livroandroid.carros.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ConfiguracoesV11Activivity extends android.app.Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Adiciona o fragment de configurações
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new PrefsFragment());
        ft.commit();

        boolean b = PrefsUtils.isCheckPushOn(this);
        Toast.makeText(this, " isCheckPushOn: " + b, Toast.LENGTH_SHORT).show();
    }

    public static class PrefsFragment extends android.preference.PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Carrega as configurações
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
