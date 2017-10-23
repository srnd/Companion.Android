package org.srnd.companion

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import org.srnd.companion.auth.TicketScanActivity
import org.srnd.companion.util.AccountAdder

class LoginActivity : AppCompatActivity() {
    private var emailInput: EditText? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if((application as CompanionApplication).isSignedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_login)

        val scanBtn = findViewById<Button>(R.id.scanTicketButton)
        scanBtn.setOnClickListener {
            val intent = Intent(this, TicketScanActivity::class.java)
            startActivity(intent)
        }

        emailInput = findViewById<EditText>(R.id.email_input)
        emailInput!!.clearFocus()

        val lookupBtn = findViewById<Button>(R.id.lookup_button)
        lookupBtn.setOnClickListener {
            lookupTicket()
        }

        dialog = ProgressDialog(this)
    }

    private fun lookupTicket() {
        dialog!!.setTitle(getString(R.string.loading_title))
        dialog!!.setMessage(getString(R.string.loading_registration))
        dialog!!.show()

        Fuel.get("/login", listOf("email" to emailInput!!.text)).responseJson { _, _, result ->
            val res = result.get().obj()

            dialog!!.hide()

            if(res.getBoolean("ok")) {
                val reg = res.getJSONObject("registration")
                AccountAdder.addAccount(this, reg)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, R.string.error_scan, Toast.LENGTH_LONG).show()
            }
        }
    }
}
