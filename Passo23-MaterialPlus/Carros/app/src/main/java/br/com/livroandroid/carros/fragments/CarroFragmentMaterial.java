package br.com.livroandroid.carros.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import br.com.livroandroid.carros.CarrosApplication;
import br.com.livroandroid.carros.R;
import br.com.livroandroid.carros.activity.MapaActivity;
import br.com.livroandroid.carros.activity.VideoActivity;
import br.com.livroandroid.carros.domain.Carro;

import br.com.livroandroid.carros.fragments.dialog.DeletarCarroDialog;
import br.com.livroandroid.carros.fragments.dialog.EditarCarroDialog;
import livroandroid.lib.utils.AndroidUtils;
import livroandroid.lib.utils.IntentUtils;


public class CarroFragmentMaterial extends BaseFragmentMaterialDetailPattern {
    private Carro carro;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carro_material, container, false);

        setHasOptionsMenu(true);
        //setRetainInstance(true);

        // Recebe o carro pelos argumentos e atualiza a interface
        Bundle args = getArguments();
        if (args != null && args.containsKey("carro")) {
            Carro carro = (Carro) args.getSerializable("carro");
            setCarro(carro);
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        initViews(view,toolbar);

        return view;
    }

    // Método público chamado pela activity para atualizar os dados do carro
    public void setCarro(Carro carro) {
        this.carro = carro;

        updateValues(carro);

    }

    protected void updateValues(Carro c) {

        tHeaderTitle.setText(c.nome);
        tHeaderSubTitle.setText(c.tipo);

        layoutPicture.setBackgroundColor(AndroidUtils.getMaterialThemePrimaryColor(getContext()));

        Picasso.with(getContext()).load(c.urlFoto).fit().into(imgPicture);
        //imgPicture.setImageResource(R.drawable.ferrari1);
        recomputePhotoAndScrollingMetrics();

        final boolean isFavorito = false;

        if(fabAddToFavoritos != null) {
            fabAddToFavoritos.setVisibility(View.VISIBLE);
        }

        showStarredDeferred(isFavorito, false);

        // Triplica a descriçao para ter bastante coisa.
        carro.desc += "\n --- \n " + carro.desc + "\n --- \n " + carro.desc;

        tTextDetails.setText(carro.desc);
        tTextDetails.setVisibility(View.VISIBLE);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, 0); // trigger scroll handling
                mScrollViewChild.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_carro, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            //toast("Editar: " + carro.nome);
            EditarCarroDialog.show(getFragmentManager(), carro, new EditarCarroDialog.Callback() {
                @Override
                public void onCarroUpdated(Carro carro) {
                    toast("Carro [" + carro.nome + "] atualizado.");
                    CarrosApplication.getInstance().setPrecisaAtualizar(carro.tipo, true);
                    // Atualiza o título com o novo nome
                    getActionBar().setTitle(carro.nome);
                }
            });
            return true;
        } else if (item.getItemId() == R.id.action_remove) {
            //toast("Deletar: " + carro.nome);

            DeletarCarroDialog.show(getFragmentManager(), carro, new DeletarCarroDialog.Callback() {
                @Override
                public void onCarroDeleted(Carro carro) {
                    toast("Carro [" + carro.nome + "] deletado.");
                    CarrosApplication.getInstance().setPrecisaAtualizar(carro.tipo, true);
                    // Fecha a activity
                    getActivity().finish();
                }
            });

            return true;
        } else if (item.getItemId() == R.id.action_share) {
            toast("Compartilhar");
        } else if (item.getItemId() == R.id.action_maps) {
            // Abre outra activity com VideoView
            Intent intent = new Intent(getContext(), MapaActivity.class);
            intent.putExtra("carro", carro);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_video) {
            // URL do vídeo
            final String url = carro.urlVideo;
            toast("URL: " + url);
            // Lê a view que é a âncora do popup
            View menuItemView = getActivity().findViewById(item.getItemId());
            if (menuItemView != null && url != null) {
                // Cria o PopupMenu posicionado na âncora
                PopupMenu popupMenu = new PopupMenu(getActionBar().getThemedContext(), menuItemView);
                popupMenu.inflate(R.menu.menu_popup_video);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_video_browser) {
                            // Abre o vídeo no browser
                            IntentUtils.openBrowser(getContext(), url);
                        } else if (item.getItemId() == R.id.action_video_player) {
                            // Abre o vídeo no Player de Vídeo Nativo
                            IntentUtils.showVideo(getContext(), url);
                        } else if (item.getItemId() == R.id.action_video_videoview) {
                            // Abre outra activity com VideoView
                            Intent intent = new Intent(getContext(), VideoActivity.class);
                            intent.putExtra("carro", carro);
                            startActivity(intent);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
