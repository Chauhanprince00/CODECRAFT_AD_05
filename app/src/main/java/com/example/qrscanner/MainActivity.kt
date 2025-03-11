package com.example.qrscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.qrscanner.databinding.ActivityMainBinding
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isScannerInstall = false
    private lateinit var scanner:GmsBarcodeScanner
    private  var resultText:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerUiListner()
        initVars()
        installgooglescanner()

        binding.cardsearch.setOnClickListener {
            resultText?.let {
                if (it.startsWith("http") || it.startsWith("www")){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "No valid link found!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.cardcopy.setOnClickListener {
            resultText?.let {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("scanned text",it)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.cardshare.setOnClickListener {
            resultText?.let {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT,it)
                }
                startActivity(Intent.createChooser(shareIntent,"share via"))
            }
        }
    }

    private fun initVars() {
        val option = initializeGoogleScanner()
        scanner = GmsBarcodeScanning.getClient(this,option)
    }

    private fun installgooglescanner() {
        val ModuleInstall = ModuleInstall.getClient(this)
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(this))
            .build()

        ModuleInstall.installModules(moduleInstallRequest).addOnSuccessListener {
            isScannerInstall = true
        }.addOnFailureListener {
            isScannerInstall = true
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeGoogleScanner(): GmsBarcodeScannerOptions {
       return GmsBarcodeScannerOptions.Builder().
                setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom().build()
    }

    private fun registerUiListner() {
        binding.scanQR.setOnClickListener {
                if (isScannerInstall){
                    startScanning()
                }else{
                    Toast.makeText(this, "Please try again...", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun startScanning() {
        scanner.startScan().addOnSuccessListener {
            val result = it.rawValue
            result?.let {
                binding.textView.text = it
                resultText = it

            }
        }.addOnCanceledListener {
            Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }
}