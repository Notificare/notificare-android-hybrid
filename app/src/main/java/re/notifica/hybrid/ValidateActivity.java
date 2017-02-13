package re.notifica.hybrid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;

public class ValidateActivity extends AppCompatActivity {
    private String token;
    private ProgressDialog dialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate);

        token = Notificare.shared().parseValidateUserIntent(getIntent());

        builder = new AlertDialog.Builder(this);

        dialog = ProgressDialog.show(this, getString(R.string.app_name),
                getString(R.string.validating_account), true);
        dialog.show();

        if (token == null) {

            dialog.dismiss();
            builder.setMessage(R.string.error_validate_token)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            builder.create();
            builder.show();

        } else {
            Notificare.shared().validateUser(token, new NotificareCallback<Boolean>(){
                @Override
                public void onError(NotificareError arg0) {
                    dialog.dismiss();
                    builder.setMessage(arg0.getMessage())
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                   builder.create();
                   builder.show();
                }

                @Override
                public void onSuccess(Boolean result) {
                    dialog.dismiss();
                    builder.setMessage(R.string.success_validate_account)
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    builder.create();
                    builder.show();
                }
            });
        }

    }

}
