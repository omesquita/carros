package br.com.livroandroid.carros.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.livroandroid.carros.CarrosApplication;
import br.com.livroandroid.carros.R;
import br.com.livroandroid.carros.activity.CarroActivity;
import br.com.livroandroid.carros.adapter.CarroAdapter;
import br.com.livroandroid.carros.domain.Carro;
import br.com.livroandroid.carros.domain.CarroDB;
import br.com.livroandroid.carros.domain.CarroService;
import livroandroid.lib.utils.AndroidUtils;

public class CarrosFragment extends BaseFragment {
    protected RecyclerView recyclerView;
    private List<Carro> carros;
    private LinearLayoutManager mLayoutManager;
    private String tipo;
    private SwipeRefreshLayout swipeLayout;

    private ActionMode actionMode;
    public Intent shareIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.tipo = getArguments().getString("tipo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carros, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        // Swipe to Refresh
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefresh);
        swipeLayout.setOnRefreshListener(OnRefreshListener());
        swipeLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        return view;
    }

    private SwipeRefreshLayout.OnRefreshListener OnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Atualiza ao fazer o gesto Swipe To Refresh
                taskCarros(true);
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Por padrão busca os carros do banco de dados.
        taskCarros(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (CarrosApplication.getInstance().isPrecisaAtualizar(this.tipo)) {
            // Faz a leitura novamente do banco de dados
            taskCarros(false);
            toast("Lista de carros atualizada!");
        }
    }

    private void taskCarros(boolean refresh) {
        recyclerView.setAdapter(null);
        // Busca os carros: Dispara a Task
        if (!refresh || AndroidUtils.isNetworkAvailable(getContext())) {
            startTask("carros", new GetCarrosTask(refresh), R.id.swipeToRefresh);
        } else {
            alert(R.string.error_conexao_indisponivel);
            swipeLayout.setRefreshing(false);
        }
    }

    private CarroAdapter.CarroOnClickListener onClickCarro() {
        return new CarroAdapter.CarroOnClickListener() {
            @Override
            public void onClickCarro(View view, int idx) {
                Carro c = carros.get(idx);
                if (actionMode == null) {
                    //Toast.makeText(getContext(), "Carro: " + c.nome, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), CarroActivity.class);
                    intent.putExtra("carro", c);
                    startActivity(intent);
                } else {
                    // Seleciona o carro e atualiza a lista
                    c.selected = !c.selected;
                    updateActionModeTitle();
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onLongClickCarro(View view, int idx) {
                if (actionMode != null) {
                    return;
                }

                //Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                actionMode = getActionBarActivity().startSupportActionMode(getActionModeCallback());

                Carro c = carros.get(idx);
                c.selected = true;
                recyclerView.getAdapter().notifyDataSetChanged();

                updateActionModeTitle();
            }
        };
    }

    private ActionMode.Callback getActionModeCallback() {
        return new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_frag_carros_context, menu);

                MenuItem shareItem = menu.findItem(R.id.action_share);
                ShareActionProvider share = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

                shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/*");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Texto para compartilhar");
                share.setShareIntent(shareIntent);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                List<Carro> selectedCarros = getSelectedCarros();
                if (item.getItemId() == R.id.action_remove) {
                    // Deleta os carros do banco
                    CarroDB db = new CarroDB(getContext());
                    try {
                        for (Carro c: selectedCarros) {
                            db.delete(c);
                            carros.remove(c);
                        }
                    } finally {
                        db.close();
                    }
                    // Atualiza a lista
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else if (item.getItemId() == R.id.action_share) {
                    toast("Compartilhar: " + selectedCarros);
                }
                // Encerra o action mode
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Limpa o ActionMode e carros selecionados
                actionMode = null;
                for (Carro c : carros) {
                    c.selected = false;
                }
                // Atualiza a lista
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        };
    }

    // Task para buscar os carros
    private class GetCarrosTask implements TaskListener<List<Carro>> {
        private boolean refresh;

        public GetCarrosTask(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        public List<Carro> execute() throws Exception {
            Thread.sleep(200);
            // Busca os carros em background (Thread)
            return CarroService.getCarros(getContext(), tipo, refresh);
        }

        @Override
        public void updateView(List<Carro> carros) {
            if (carros != null) {
                CarrosFragment.this.carros = carros;
                // Atualiza a view na UI Thread
                recyclerView.setAdapter(new CarroAdapter(getContext(), carros, onClickCarro()));
                //toast("update ("+carros.size()+"): " + carros);
            }
        }

        @Override
        public void onError(Exception e) {
            alert("Ocorreu algum erro ao buscar os dados.");
        }

        @Override
        public void onCancelled(String s) {

        }
    }

    private List<Carro> getSelectedCarros() {
        List<Carro> list = new ArrayList<Carro>();
        for (Carro c : carros) {
            if (c.selected) {
                list.add(c);
            }
        }
        return list;
    }

    private void updateActionModeTitle() {
        if (actionMode != null) {
            actionMode.setTitle("Selecione os carros.");
            List<Carro> selectedCarros = getSelectedCarros();
            if (selectedCarros.size() == 1) {
                actionMode.setSubtitle("1 carro selecionado");
            } else if (selectedCarros.size() > 1) {
                actionMode.setSubtitle(selectedCarros.size() + " carros selecionados");
            }
            updateShareIntent(selectedCarros);
        }
    }

    // Atualiza a share intent com os carros selecionados
    private void updateShareIntent(List<Carro> selectedCarros) {
        if(shareIntent != null) {

            ArrayList<Uri> imageUris = new ArrayList<Uri>();
            for (Carro c: selectedCarros) {
                imageUris.add(c.urlFotoUri);
            }

            // Texto com os carros
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Carros: " + selectedCarros);

            // Fotos dos carros
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
        }
    }
}