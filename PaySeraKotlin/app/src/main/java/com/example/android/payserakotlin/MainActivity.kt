package com.example.android.payserakotlin

import android.annotation.SuppressLint
import android.app.LoaderManager
import android.content.Loader
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.android.payserakotlin.R.array.currency_array
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<String> {

    private val PAYSERA_REQUEST_URL =  "http://api.evp.lt/currency/commercial/exchange/"

    private var savingsUSD = 1000.0
    private var savingsEUR = 0.0
    private var savingsJPY = 0.0

    private var commissionsUSD = 0.0
    private var commissionsEUR = 0.0
    private var commissionsJPY = 0.0

    private var fromCurrency = "USD"
    private var toCurrency = "USD"
    private var enoughSavings:Boolean = false

    private var fromAmount = 0.0
    private var commissionPay = 0.0
    private var freeConversions = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fAdapter = ArrayAdapter.createFromResource(this,
        currency_array, android.R.layout.simple_spinner_item)
        fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        from_currency_spinner.adapter = fAdapter

        val tAdapter = ArrayAdapter.createFromResource(this,
        currency_array, android.R.layout.simple_spinner_item)
        tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        to_currency_spinner.adapter = tAdapter

        updateSavings()

        convert_button.setOnClickListener {
            fromCurrency = from_currency_spinner.selectedItem.toString()
            toCurrency = to_currency_spinner.selectedItem.toString()
            val a = amount.text.toString()
            if (a!="") {
                fromAmount = java.lang.Double.parseDouble(a)
                loaderManager.initLoader(1, null, this@MainActivity)
            }
        }
    }



    @SuppressLint("SetTextI18n")
    private fun updateSavings(){
        USD.text = "%.2f".format(savingsUSD) + " USD"

        EUR.text = "%.2f".format(savingsEUR) + " EUR"

        JPY.text = "%.0f".format(savingsJPY) + " JPY"

        comUSD.text = ("%.2f".format(commissionsUSD)) + " USD"

        comEUR.text = "%.2f".format(commissionsEUR) + " EUR"

        comJPY.text = "%.0f".format(commissionsJPY) + " JPY"
    }

    fun convertCurrency(data:String){
        val toast = Toast.makeText(this@MainActivity, "Jūs konvertavote " + "%.2f".format(fromAmount) +
                " $fromCurrency į $data $toCurrency. Komisinis mokestis - " + "%.2f".format(commissionPay) +
                " " + fromCurrency +".", Toast.LENGTH_LONG)

        var amount = java.lang.Double.parseDouble(data)
        enoughSavings = false
        val commissionRate = 0.007
        commissionPay = 0.0

        if (freeConversions<=1)
        {
            commissionPay = fromAmount * commissionRate
        }

        when (fromCurrency){
            "USD" -> {
                savingsUSD = calculate(savingsUSD)
                if(enoughSavings) {commissionsUSD += commissionPay; toast.show()}
        }
            "EUR" -> {
                savingsEUR = calculate(savingsEUR)
                if(enoughSavings) {commissionsEUR += commissionPay; toast.show()}
            }
            "JPY" -> {
                savingsJPY = calculate(savingsJPY)
                if(enoughSavings) {commissionsJPY += commissionPay; toast.show()}
            }
        }

        when(toCurrency){
            "USD" -> {
                if (enoughSavings) { savingsUSD += amount}
            }
            "EUR" -> {
                if (enoughSavings) { savingsEUR += amount}
            }
            "JPY" -> {
                if (enoughSavings) { savingsJPY += amount}
            }
        }
        updateSavings()
    }

    private fun calculate(a:Double):Double{
        val toast = Toast.makeText(this@MainActivity, R.string.not_sufficient_funds, Toast.LENGTH_SHORT)
        return if(a >= fromAmount + commissionPay) {
            val b = a - (fromAmount + commissionPay); freeConversions--; enoughSavings = true; b
        }
        else { toast.show(); a }
    }

    private fun createUrl(a:String): URL? {
        var url:URL
        try{
            url = URL(a)
        } catch (e:MalformedURLException) {
            e.printStackTrace()
            return null
        }
        return url
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<String> {
        val baseUri = Uri.parse(PAYSERA_REQUEST_URL)
        val uriBuilder = baseUri.buildUpon()
        uriBuilder.appendPath("$fromAmount-$fromCurrency")
        uriBuilder.appendPath(toCurrency)
        uriBuilder.appendPath("latest")
        val url = createUrl(uriBuilder.toString())
        return ConvertLoader(this, url)
    }

    override fun onLoadFinished(loader: Loader<String>?, data: String) {
        if ((data != "0.00") && fromCurrency != toCurrency){
            convertCurrency(data)
        }
        loaderManager.destroyLoader(1)
    }

    override fun onLoaderReset(loader: Loader<String>?) {}

}
