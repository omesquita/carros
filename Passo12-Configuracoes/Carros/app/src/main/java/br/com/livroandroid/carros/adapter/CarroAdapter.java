package br.com.livroandroid.carros.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import br.com.livroandroid.carros.R;
import br.com.livroandroid.carros.domain.Carro;
import livroandroid.lib.utils.IOUtils;
import livroandroid.lib.utils.SDCardUtils;

// Herda de RecyclerView.Adapter e declara o tipo genérico <CarroAdapterV2.CarrosViewHolder>
public class CarroAdapter extends RecyclerView.Adapter<CarroAdapter.CarrosViewHolder> {
    protected static final String TAG = "livroandroid";
    private final List<Carro> carros;
    private final Context context;

    private CarroOnClickListener carroOnClickListener;

    public CarroAdapter(Context context, List<Carro> carros, CarroOnClickListener carroOnClickListener) {
        this.context = context;
        this.carros = carros;
        this.carroOnClickListener = carroOnClickListener;
    }

    @Override
    public int getItemCount() {
        return this.carros.size();
    }

    @Override
    public CarrosViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Infla a view do layout
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_carro, viewGroup, false);

        CardView cardView = (CardView) view.findViewById(R.id.card_view);

        // Cria o ViewHolder
        CarrosViewHolder holder = new CarrosViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final CarrosViewHolder holder, final int position) {
        // Atualiza a view
        final Carro c = carros.get(position);

        holder.tNome.setText(c.nome);
        holder.progress.setVisibility(View.VISIBLE);

        Picasso.with(context).load(c.urlFoto).fit().into(holder.img, new Callback() {
            @Override
            public void onSuccess() {
                holder.progress.setVisibility(View.GONE);

                saveFotoCarroToSdCard(c, holder.img);
            }

            private void saveFotoCarroToSdCard(final Carro c, ImageView img) {
                // Depois do download, pega o Bitmap que está no ImageView
                final Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();

                // Cria uma pequena thread para salvar o arquivo
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onBitmapLoaded.run: " + c.urlFoto);

                        // Cria um arquivo em /storage/sdcard
                        File file = SDCardUtils.getPublicFileWithType("carros", c.nome + ".png", Environment.DIRECTORY_PICTURES);
                        Log.d(TAG,"File: " + file);

                        c.urlFotoUri = Uri.fromFile(file).toString();

                        // Salva o arquivo
                        IOUtils.writeBitmap(file, bitmap);

                    }
                }).start();
            }

            @Override
            public void onError() {
                holder.progress.setVisibility(View.GONE);
            }
        });

        // Click
        if (carroOnClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    carroOnClickListener.onClickCarro(holder.itemView, position); // A variável position é final
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    carroOnClickListener.onLongClickCarro(holder.itemView, position); // A variável position é final
                    return true;
                }
            });
        }

        // Linha selecionada
        if (c.selected) {
            holder.itemView.setBackgroundResource(R.drawable.seletor_contextual_action_bar);
        } else {
            holder.itemView.setBackground(null);
        }
    }

    public interface CarroOnClickListener {
        public void onClickCarro(View view, int idx);
        public void onLongClickCarro(View view, int idx);
    }

    // ViewHolder com as views
    public static class CarrosViewHolder extends RecyclerView.ViewHolder {
        public TextView tNome;
        ImageView img;
        ProgressBar progress;

        public CarrosViewHolder(View view) {
            super(view);
            // Cria as views para salvar no ViewHolder
            tNome = (TextView) view.findViewById(R.id.text);
            img = (ImageView) view.findViewById(R.id.img);
            progress = (ProgressBar) view.findViewById(R.id.progressImg);
        }
    }
}
