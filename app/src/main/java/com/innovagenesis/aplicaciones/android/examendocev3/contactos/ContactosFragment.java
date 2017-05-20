package com.innovagenesis.aplicaciones.android.examendocev3.contactos;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.innovagenesis.aplicaciones.android.examendocev3.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactosFragment extends Fragment {

    private ListView mListView;
    private ProgressDialog pDialog;
    private android.os.Handler updateBarHandler;

    private ArrayList<Contactos> contactList;
    private Cursor cursor;
    private int counter = 0;

    private FloatingActionButton fab;

    public ContactosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contactos, container, false);

        contactList = new ArrayList<>();
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Leyendo");
        pDialog.setCancelable(false);
        pDialog.show();

        fab = (FloatingActionButton)view.findViewById(R.id.fab);
        mListView = (ListView) view.findViewById(R.id.list_view_contactos);
        updateBarHandler = new android.os.Handler();


        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
            }
        }).start();

        return view;
    }



    /**
     * Clase encargada de extraer los contactos del telefono
     */
    public void getContacts() {
        String phoneNumber;
        String email;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTAC_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Uri emailCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        // String emailCONTAC_ID =ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String data = ContactsContract.CommonDataKinds.Phone.DATA;

        //StringBuffer output;

        ContentResolver contentResolver = getActivity().getContentResolver();

        //Incializa el cursor
        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {

            counter = 0;
            while (cursor.moveToNext()) {
                String nombre = "";
                String numero = "";
                String correo = "";

                updateBarHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.setMessage("Leyendo :" + counter++ + "/" + cursor.getCount());
                    }
                });
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt
                        (cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    nombre = name;

                    Cursor phoneCursor = contentResolver.query
                            (PhoneCONTENT_URI, null, Phone_CONTAC_ID + "=?",
                                    new String[]{contact_id}, null);

                    /**
                     * Extrae el número
                     * */
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        numero = phoneNumber;
                    }
                    phoneCursor.close();
                    /**
                     * Extrae el email
                     * */

                    Cursor emailCursor = contentResolver.query
                            (emailCONTENT_URI, null, Phone_CONTAC_ID + "=?"
                                    , new String[]{contact_id}, null);

                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(data));
                        correo = email;
                    }
                    emailCursor.close();

                }
                //Crea la lista de contactos
                contactList.add(new Contactos( correo,nombre, numero));
            }

            getActivity().runOnUiThread(new Runnable() {
                /** Inicializa el hilo y carga el adapter*/
                @Override
                public void run() {
                    ContactosAdapter adapter = new ContactosAdapter(getContext(), contactList);
                    mListView.setAdapter(adapter);
                }
            });

            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }
    }

}