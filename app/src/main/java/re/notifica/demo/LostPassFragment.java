package re.notifica.demo;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;


/**
 * A simple {@link Fragment} subclass.
 */
public class LostPassFragment extends Fragment {

    private EditText emailField;
    private ProgressDialog dialog;
    private AlertDialog.Builder builder;

    public LostPassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_lostpass);
        View rootView = inflater.inflate(R.layout.fragment_lost_pass, container, false);

        Button lostPassButton = (Button) rootView.findViewById(R.id.buttonLostPass);
        Typeface lightFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Light.ttf");
        Typeface regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Regular.ttf");
        builder = new AlertDialog.Builder(getActivity());
        emailField = (EditText) rootView.findViewById(R.id.emailField);
        emailField.setTypeface(lightFont);
        lostPassButton.setTypeface(lightFont);
        rootView.findViewById(R.id.buttonLostPass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRecoverPassword();
            }
        });

        return rootView;
    }

    public void doRecoverPassword() {

        String email = emailField.getText().toString();

        if (TextUtils.isEmpty(email) ) {
            //info.setText(R.string.error_lost_pass);
            builder.setMessage(R.string.error_lost_pass)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog dialogInfo = builder.create();
            dialogInfo.show();
        }  else if (!email.contains("@")) {
            //info.setText(R.string.error_lost_pass);
            builder.setMessage(R.string.error_lost_pass)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                            emailField.setText("");
                        }
                    });
            AlertDialog dialogInfo = builder.create();
            dialogInfo.show();
        } else {
            dialog = ProgressDialog.show(getActivity(), "", getString(R.string.loader), true);

            Notificare.shared().sendPassword(email, new NotificareCallback<Boolean>(){

                @Override
                public void onError(NotificareError arg0) {

                    dialog.dismiss();
                    builder.setMessage(arg0.getMessage())
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    emailField.setText("");
                                }
                            });
                    AlertDialog dialogInfo = builder.create();
                    dialogInfo.show();
                }

                @Override
                public void onSuccess(Boolean arg0) {

                    builder.setMessage(R.string.success_email_found)
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getFragmentManager().popBackStack();
                                }
                            });
                    AlertDialog dialogInfo = builder.create();
                    dialogInfo.show();
                    dialog.dismiss();
                }

            });
        }

    }
}
