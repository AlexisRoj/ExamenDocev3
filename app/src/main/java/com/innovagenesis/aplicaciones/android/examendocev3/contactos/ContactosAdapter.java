package com.innovagenesis.aplicaciones.android.examendocev3.contactos;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.innovagenesis.aplicaciones.android.examendocev3.R;

import java.util.List;

/**
 * Clase encargada de rellenar el arrayList
 * Created by alexi on 18/05/2017.
 */

public class ContactosAdapter extends BaseAdapter{

    private final Context context;
    private final List<Contactos> contactos;

    public ContactosAdapter(Context context, List<Contactos> contactos) {
        this.context = context;
        this.contactos = contactos;
    }

    @Override
    public int getCount() {
        return contactos.size();
    }

    @Override
    public Object getItem(int position) {
        return contactos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** Instancia todos los elementos y los asigna*/
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        final ViewHolder holder = new ViewHolder();

        if (view == null)
            view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item,parent,false);

        holder.nombreContacto = (TextView) view.findViewById(R.id.txt_nombre_contacto);
        holder.telefonoContacto = (TextView)view.findViewById(R.id.txt_telefono_contacto);
        holder.linearLayout = (LinearLayout)view.findViewById(R.id.item_listview);
        holder.checkbox = (CheckBox)view.findViewById(R.id.checkbox);

        final Contactos persona = contactos.get(position);

        holder.nombreContacto.setText(persona.getNombre());
        holder.telefonoContacto.setText(persona.getNumero());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            /** Envia mensajes y activa los check*/
            @Override
            public void onClick(View v) {
                if (!holder.checkbox.isChecked()) {
                    holder.checkbox.setChecked(true);
                    Toast.makeText(context, "Contacto selecionado: \n"
                                    + persona.getNombre()
                                    + "\n"
                                    + persona.getNumero()
                            , Toast.LENGTH_SHORT).show();
                }else
                    holder.checkbox.setChecked(false);
            }
        });
        return view;
    }

    /** Clase holder que instancia los elementos*/
    private class ViewHolder {
        private TextView nombreContacto;
        private TextView telefonoContacto;
        private LinearLayout linearLayout;
        private CheckBox checkbox;
    }
}
