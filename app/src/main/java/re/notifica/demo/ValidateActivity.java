package re.notifica.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

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
                    .setPositiveButton("OK", (dialog, id) -> finish());
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
                            .setPositiveButton("OK", (dialog, id) -> finish());
                   builder.create();
                   builder.show();
                }

                @Override
                public void onSuccess(Boolean result) {
                    dialog.dismiss();
                    builder.setMessage(R.string.success_validate_account)
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> finish());
                    builder.create();
                    builder.show();
                }
            });
        }

    }

}
