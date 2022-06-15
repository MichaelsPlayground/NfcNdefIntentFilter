# NFC NDEF reader with Intent Filter


Intent filter in AndroidManifest.xml:
```plaintext
...
<intent-filter>
    <action android:name="android.nfc.action.NDEF_DISCOVERED"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <data android:mimeType="text/plain" />
</intent-filter>
...
```
