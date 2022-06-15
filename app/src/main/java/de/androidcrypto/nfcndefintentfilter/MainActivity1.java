package de.androidcrypto.nfcndefintentfilter;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

//public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
public class MainActivity1 extends AppCompatActivity {

    Button writeNfc;
    com.google.android.material.textfield.TextInputLayout inputField1Decoration, inputField2Decoration, inputField3Decoration;
    com.google.android.material.textfield.TextInputEditText typeDescription, inputField1, inputField2, inputField3, resultNfcWriting;
    SwitchMaterial addTimestampToData;
    AutoCompleteTextView autoCompleteTextView;

    Intent writeNfcIntent;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeNfc = findViewById(R.id.btnMainWriteNfcNdefTag);
        writeNfcIntent = new Intent(MainActivity1.this, WriteNdefActivity.class);

        inputField1 = findViewById(R.id.etMainInputline1);
        inputField1Decoration = findViewById(R.id.etMainInputline1Decoration);
        inputField2 = findViewById(R.id.etMainInputline2);
        inputField2Decoration = findViewById(R.id.etMainInputline2Decoration);
        inputField3 = findViewById(R.id.etMainInputline3);
        inputField3Decoration = findViewById(R.id.etMainInputline3Decoration);
        resultNfcWriting = findViewById(R.id.etMainResult);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        writeNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(writeNfcIntent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, new Intent(this, getClass()).addFlags(
                            Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, new Intent(this, getClass()).addFlags(
                            Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, new Intent(this, this.getClass()), PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, new Intent(this, this.getClass()), PendingIntent.FLAG_ONE_SHOT);
        }
*/
        /*
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled())
                showWirelessSettings();

            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }
                // Process the messages array.
                System.out.println("*** NDEF messages found: " + messages.length);
                resultNfcWriting.setText("NDEF messages found: " + messages.length);
            } else {
                inputField2.setText("raw messages == null");
            }
        } else {
            inputField3.setText("no intent.get action");
        }
    }

/*
    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    // This method is run in another thread when a card is discovered
    // !!!! This method cannot cannot direct interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    @Override
    public void onTagDiscovered(Tag tag) {
        // Read and or write to Tag here to the appropriate Tag Technology type class
        // in this example the card should be an Ndef Technology Type

        Ndef mNdef = Ndef.get(tag);

        // Check that it is an Ndef capable card
        if (mNdef != null) {
            NdefMessage ndefMessage;
            NdefRecord ndefRecord1;
            // nfc ndef writing depends on the type
            String choiceString = autoCompleteTextView.getText().toString();
            String inputData1 = inputField1.getText().toString();
            boolean addTimestamp = addTimestampToData.isChecked();
            switch (choiceString) {
                case "Text": {
                    String data = inputData1;
                    //if (addTimestamp) data = data + " " + Utils.getTimestamp();
                    ndefRecord1 = NdefRecord.createTextRecord("en", data);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "URI": {
                    String data = inputData1;
                    ndefRecord1 = NdefRecord.createUri(data);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Telefone number": {
                    String data = inputData1;
                    String completeData = "tel:" + data;
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Email": {
                    String data1 = inputData1;
                    String data2 = inputField2.getText().toString();
                    String data3 = inputField3.getText().toString();
                    String completeData = "mailto:" + Uri.encode(data1) + "?subject=" +
                            Uri.encode(data2);
                    //if (addTimestamp) completeData = completeData + Uri.encode(" " + Utils.getTimestamp());
                    completeData = completeData + "&body=" + Uri.encode(data3);
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "StreetView": {
                    String data = inputData1;
                    String completeData = "google.streetview:cbll=" + data;
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Coordinate": {
                    String data = inputData1;
                    String completeData = "geo:" + data;
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Coordinate userinfo": {
                    String data1 = inputData1;
                    String data2 = Uri.encode(inputField2.getText().toString());
                    String completeData = "geo:0,0?q=" + data1 + "(" + data2 + ")";
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Address": {
                    String data1 = Uri.encode(inputData1);
                    String data2 = Uri.encode(inputField2.getText().toString());
                    String data3 = Uri.encode(inputField3.getText().toString());
                    String completeData = "geo:0,0?q=" + data1 + "+" + data2 + "+" + data3;
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Google navigation": {
                    String data1 = Uri.encode(inputData1);
                    String data2 = Uri.encode(inputField2.getText().toString());
                    String data3 = Uri.encode(inputField3.getText().toString());
                    String completeData = "google.navigation:q=" + data1 + "+" + data2 + "+" + data3;
                    ndefRecord1 = NdefRecord.createUri(completeData);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }
                case "Application": {
                    String data = inputData1;
                    ndefRecord1 = NdefRecord.createApplicationRecord(data);
                    ndefMessage = new NdefMessage(ndefRecord1);
                    break;
                }

                default:
                    throw new IllegalStateException("Unexpected value: " + choiceString);
            }


            // the tag is written here
            try {
                mNdef.connect();
                mNdef.writeNdefMessage(ndefMessage);
                // Success if got to here
                runOnUiThread(() -> {
                    resultNfcWriting.setText("write to NFC success");
                    Toast.makeText(getApplicationContext(),
                            "write to NFC success",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (FormatException e) {
                runOnUiThread(() -> {
                    resultNfcWriting.setText("failure FormatException: " + e);
                    Toast.makeText(getApplicationContext(),
                            "FormatException: " + e,
                            Toast.LENGTH_SHORT).show();
                });
                // if the NDEF Message to write is malformed
            } catch (TagLostException e) {
                runOnUiThread(() -> {
                    resultNfcWriting.setText("failure TagLostException: " + e);
                    Toast.makeText(getApplicationContext(),
                            "TagLostException: " + e,
                            Toast.LENGTH_SHORT).show();
                });
                // Tag went out of range before operations were complete
            } catch (IOException e) {
                // if there is an I/O failure, or the operation is cancelled
                runOnUiThread(() -> {
                    resultNfcWriting.setText("failure IOException: " + e);
                    Toast.makeText(getApplicationContext(),
                            "IOException: " + e,
                            Toast.LENGTH_SHORT).show();
                });
            } finally {
                // Be nice and try and close the tag to
                // Disable I/O operations to the tag from this TagTechnology object, and release resources.
                try {
                    mNdef.close();
                } catch (IOException e) {
                    // if there is an I/O failure, or the operation is cancelled
                    runOnUiThread(() -> {
                        resultNfcWriting.setText("failure IOException: " + e);
                        Toast.makeText(getApplicationContext(),
                                "IOException: " + e,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            // Make a Sound
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150,10));
            } else {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(),
                        "mNdef is null",
                        Toast.LENGTH_SHORT).show();
            });
        }
    }
*/
}