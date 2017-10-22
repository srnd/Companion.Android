package org.srnd.companion

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.srnd.companion.auth.TicketScanActivity
import org.srnd.companion.dayof.AllSetActivity
import org.srnd.companion.dayof.CheckInActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accountManager = AccountManager.get(this)

        val accounts: Array<Account> = accountManager.getAccountsByType("codeday.org")

        if(accounts.size > 0) {
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
    }
}
