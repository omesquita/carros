package layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.livroandroid.carros.R;
import br.com.livroandroid.carros.fragments.BaseFragment;

public class CarrosFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carros, container, false);
        return view;
    }
}
