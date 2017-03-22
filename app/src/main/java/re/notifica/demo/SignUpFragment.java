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
public class SignUpFragment extends Fragment {

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private ProgressDialog dialog;
    private AlertDialog.Builder builder;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_signup);
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        Button signupButton = (Button) rootView.findViewById(R.id.buttonSignup);

        nameField = (EditText) rootView.findViewById(R.id.nameField);
        emailField = (EditText) rootView.findViewById(R.id.emailField);
        passwordField = (EditText) rootView.findViewById(R.id.passField);
        confirmPasswordField = (EditText) rootView.findViewById(R.id.confirmPassField);

        builder = new AlertDialog.Builder(getActivity());

        Typeface lightFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Light.ttf");
        Typeface regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Regular.ttf");
        emailField.setTypeface(lightFont);
        passwordField.setTypeface(lightFont);
        nameField.setTypeface(lightFont);
        confirmPasswordField.setTypeface(lightFont);
        signupButton.setTypeface(lightFont);

        rootView.findViewById(R.id.buttonSignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSignUp();
            }
        });


        return rootView;
    }

    /**
     * Do SignUp
     */
    public void doSignUp() {

        final String name = nameField.getText().toString();
        final String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

            builder.setMessage(R.string.error_sign_up)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog dialogInfo = builder.create();
            dialogInfo.show();

        } else if (!password.equals(confirmPassword)) {

            builder.setMessage(R.string.error_pass_not_match)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            confirmPasswordField.setText("");
                        }
                    });
            AlertDialog dialogInfo = builder.create();
            dialogInfo.show();

        } else if (password.length() < 6) {

            builder.setMessage(R.string.error_pass_too_short)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            passwordField.setText("");
                            confirmPasswordField.setText("");
                        }
                    });
            AlertDialog dialogInfo = builder.create();
            dialogInfo.show();

        } else if (!email.contains("@")) {

            builder.setMessage(R.string.error_invalid_email)
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

            Notificare.shared().createAccount(email, password, name, new NotificareCallback<Boolean>(){

                @Override
                public void onError(NotificareError arg0) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    builder.setMessage(arg0.getMessage())
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                    emailField.setText("");
                                }
                            });
                    builder.create();
                    builder.show();

                }

                @Override
                public void onSuccess(Boolean arg0) {
                    dialog.dismiss();
                    builder.setMessage(R.string.success_account_created)
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getFragmentManager().popBackStack();
                                }
                            });
                    builder.create();
                    builder.show();

                    ((MainActivity)getActivity()).createMemberCard(name, email.trim().toLowerCase());
                }

            });
        }
    }
}