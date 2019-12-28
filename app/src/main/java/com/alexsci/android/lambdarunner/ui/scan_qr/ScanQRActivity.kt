package com.alexsci.android.lambdarunner.ui.scan_qr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.ui.add_key.dialogs.QRCodeHelpDialog
import com.google.android.gms.vision.barcode.Barcode
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import info.androidhive.barcode.BarcodeReader

class ScanQRActivity: AppCompatActivity(), BarcodeReader.BarcodeReaderListener {
    companion object {
        const val SCAN_REQUIREMENTS_EXTRA = "ScanRequirements"
        const val DETECTED_JSON_EXTRA = "DetectedJSON"
        const val DETECTED_ACCESS_KEY_ID = "AccessKeyId"
        const val DETECTED_SECRET_ACCESS_KEY = "SecretAccessKey"
    }

    enum class ScanRequirements {
        IS_AWS_CREDS,
        IS_JSON
    }

    private lateinit var barcodeReader: BarcodeReader
    private lateinit var scanRequirements: ScanRequirements

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scan_qr)
        setSupportActionBar(findViewById(R.id.toolbar))

        scanRequirements = ScanRequirements.valueOf(intent.getStringExtra( SCAN_REQUIREMENTS_EXTRA)!!)

        when (scanRequirements) {
            ScanRequirements.IS_JSON -> supportActionBar?.title = "Scan JSON QR Code"
            ScanRequirements.IS_AWS_CREDS -> supportActionBar?.title = "Scan AWS Creds QR Code"
        }

        barcodeReader = supportFragmentManager.findFragmentById(R.id.barcode_fragment) as BarcodeReader
    }

    override fun onBitmapScanned(sparseArray: SparseArray<Barcode>?) {}
    override fun onScannedMultiple(barcodes: MutableList<Barcode>?) {}

    override fun onScanned(barcode: Barcode?) {
        val scannedText = barcode?.rawValue

        when (scanRequirements) {
            ScanRequirements.IS_AWS_CREDS -> {
                if (scannedText != null && scannedText.contains("\n")) {
                    val parts = scannedText.split("\n")
                    if (parts.size >= 2) {
                        val ak = parts[0]
                        val sec = parts[1]
                        // https://docs.aws.amazon.com/IAM/latest/APIReference/API_AccessKey.html
                        // Guess generously for Secret Key
                        if (ak.length in 16..128 && sec.length > 5) {
                            // Success! Send the credentials back
                            val intent = Intent()
                            intent.putExtra(DETECTED_ACCESS_KEY_ID, ak)
                            intent.putExtra(DETECTED_SECRET_ACCESS_KEY, sec)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                            return
                        }
                    }
                }
                runOnUiThread {
                    QRCodeHelpDialog.showQRCodeHint(this)
                }
            }
            ScanRequirements.IS_JSON -> {
                try {
                    JsonParser().parse(scannedText)
                    // Success! Send the JSON back
                    val intent = Intent()
                    intent.putExtra(DETECTED_JSON_EXTRA, scannedText)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                    return
                } catch (e: JsonSyntaxException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Found QR Code, but content is not JSON", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCameraPermissionDenied() {
        Toast.makeText(applicationContext, "Camera permission was not granted", Toast.LENGTH_SHORT).show()
    }

    override fun onScanError(errorMessage: String?) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
