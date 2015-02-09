package br.com.livroandroid.carros.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.io.File;
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
import livroandroid.lib.utils.IOUtils;
import livroandroid.lib.utils.SDCardUtils;

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
        //if (AndroidUtils.isNetworkAvailable(getContext())) {
            startTask("carros", new GetCarrosTask(refresh), R.id.swipeToRefresh);
        //} else {
          //  alert(R.string.error_conexao_indisponivel);
            //swipeLayout.setRefreshing(false);
        //}
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
                    // Seleciona o carro
                    c.selected = !c.selected;
                    updateActionModeTitle();
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onLongClickCarro(View view, int idx) {
                if (actionMode != null) { return; }
                // Liga a action bar de contexto
                actionMode = getActionBarActivity().startSupportActionMode(getActionModeCallback());
                Carro c = carros.get(idx);
                c.selected = true;
                // Solicita ao android para desenhar a lista novamente
                recyclerView.getAdapter().notifyDataSetChanged();
                // Atualiza o título para mostrar a quantidade de carros selecionados
                updateActionModeTitle();
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

    // Atualiza o título da action bar
    private void updateActionModeTitle() {
        if (actionMode != null) {
            actionMode.setTitle("Selecione os carros.");
            List<Carro> selectedCarros = getSelectedCarros();
            if (selectedCarros.size() == 1) {
                actionMode.setSubtitle("1 carro selecionado");
            } else if (selectedCarros.size() > 1) {
                actionMode.setSubtitle(selectedCarros.size() + " carros selecionados");
            }
        }
    }
    // Retorna a lista de caros selecionados
    private List<Carro> getSelectedCarros() {
        List<Carro> list = new ArrayList<Carro>();
        for (Carro c : carros) {
            if (c.selected) {
                list.add(c);
            }
        }
        log(list.size() + " carros selecionados");
        return list;
    }

    private ActionMode.Callback getActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_frag_carros_context, menu);
                MenuItem shareItem = menu.findItem(R.id.action_share);

                /*ShareActionProvider share = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

                shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/*");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Texto para compartilhar");
                share.setShareIntent(shareIntent);*/
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
                            db.delete(c); // Deleta o carro do banco
                            carros.remove(c); // Remove da lista
                        }
                    } finally {
                        db.close();
                    }
                    // Atualiza a lista depois de deletar os carros
                    recyclerView.getAdapter().notifyDataSetChanged();
                    toast("Carros excluídos com sucesso");
                } else if (item.getItemId() == R.id.action_share) {
                    toast("Compartilhar: " + selectedCarros);

                    startTask("compartilhar",new CompartilharTask(selectedCarros));
                }
                // Encerra o action mode
                mode.finish();
                return true;
            }
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Limpa o estado
                actionMode = null;
                // Configura todos os carros para não selecionados
                for (Carro c : carros) {
                    c.selected = false;
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        };
    }

    private class CompartilharTask extends BaseFragment.BaseTask {
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        private final List<Carro> selectedCarros;

        public CompartilharTask(List<Carro> selectedCarros) {
            this.selectedCarros = selectedCarros;
        }

        @Override
        public Object execute() throws Exception {
            if(selectedCarros != null) {
                for (Carro c : selectedCarros) {
                    // Faz o download da foto do carro para arquivo
                    String url = c.urlFoto;
                    String fileName = url.substring(url.lastIndexOf("/"));
                    // Cria o arquivo no sdcard
                    File file = SDCardUtils.getPrivateFile(getContext(),"carros",fileName);
                    IOUtils.downloadToFile(c.urlFoto,file);
                    // Salva a Uri para compartilhar a foto
                    imageUris.add(Uri.fromFile(file));
                }
            }
            return null;
        }
        @Override
        public void updateView(Object o) {
            // Cria a intent com a foto dos carros
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
            // Cria o Intent Chooser com as opções
            startActivity(Intent.createChooser(shareIntent, "Enviar Carros"));
        }
    }
}