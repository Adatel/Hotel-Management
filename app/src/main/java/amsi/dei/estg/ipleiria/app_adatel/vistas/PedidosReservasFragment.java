package amsi.dei.estg.ipleiria.app_adatel.vistas;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import amsi.dei.estg.ipleiria.app_adatel.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PedidosReservasFragment extends Fragment {


    public PedidosReservasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listas, container, false);


    }

}
