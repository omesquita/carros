package br.com.livroandroid.carros.activity;

import android.os.Bundle;

import br.com.livroandroid.carros.R;
import br.com.livroandroid.carros.domain.Carro;
import br.com.livroandroid.carros.fragments.CarroFragment;

public class CarroActivity extends BaseActivity {

    private Carro carro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_carro);

        // Configura a Toolbar como a action bar
        setUpToolbar();

        if(savedInstanceState != null) {
            carro = (Carro) savedInstanceState.getSerializable("carro");
        } else {
            this.carro = (Carro) getIntent().getSerializableExtra("carro");
        }

        // Atualiza o carro no fragment
        CarroFragment cf = (CarroFragment) getSupportFragmentManager().findFragmentById(R.id.CarroFragment);
        cf.setCarro(carro);

        // Título da toolbar e botão up navigation
        if(carro != null) {
            getSupportActionBar().setTitle(carro.nome);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("carro",carro);
    }
}
