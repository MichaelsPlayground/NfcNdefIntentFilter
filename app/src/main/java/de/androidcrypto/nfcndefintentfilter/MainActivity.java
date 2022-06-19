package de.androidcrypto.nfcndefintentfilter;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    Button writeNfc;
    com.google.android.material.textfield.TextInputLayout inputField1Decoration, inputField2Decoration, inputField3Decoration;
    com.google.android.material.textfield.TextInputEditText resultNfcReading;

    Intent writeNfcIntent;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeNfc = findViewById(R.id.btnMainWriteNfcNdefTag);
        writeNfcIntent = new Intent(MainActivity.this, WriteNdefActivity.class);
        resultNfcReading = findViewById(R.id.etMainResult);

        writeNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(writeNfcIntent);
            }
        });

        //Initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC Capabilities",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();
        }
        // Create a PendingIntent object so the Android system can
        // populate it with the details of the tag when it is scanned.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, new Intent(this, getClass()).addFlags(
                            Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, new Intent(this, getClass()).addFlags(
                            Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        if (getIntent().getAction() != null) {
            // tag received when app is not running and not in the foreground:
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
                NdefRecord record = msgs[0].getRecords()[0];
                byte[] payload = record.getPayload();
                String payloadString = parseTextrecordPayload(payload);
                resultNfcReading.setText(payloadString);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        //Onpause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Currently in tag READING mode
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
            confirmDisplayedContentOverwrite(msgs[0]);
        } else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDisplayedContentOverwrite(final NdefMessage msg) {
        final String data = resultNfcReading.getText().toString().trim();

        new AlertDialog.Builder(this)
                .setTitle("New tag found!")
                .setMessage("Do you want to show the content of this tag?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // use the current values in the NDEF payload to update the text fields
                        String payload = parseTextrecordPayload(msg.getRecords()[0].getPayload());
                        resultNfcReading.setText(payload);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        resultNfcReading.setText(data);
                        dialog.cancel();
                    }
                }).show();
    }

    NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }
        } else {
            //Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

    private static String parseTextrecordPayload(byte[] ndefPayload) {
        int languageCodeLength = Array.getByte(ndefPayload, 0);
        int ndefPayloadLength = ndefPayload.length;
        byte[] languageCode = new byte[languageCodeLength];
        System.arraycopy(ndefPayload, 1, languageCode, 0, languageCodeLength);
        byte[] message = new byte[ndefPayloadLength - 1 - languageCodeLength];
        System.arraycopy(ndefPayload, 1 + languageCodeLength, message, 0, ndefPayloadLength - 1 - languageCodeLength);
        return new String(message, StandardCharsets.UTF_8);
    }
}