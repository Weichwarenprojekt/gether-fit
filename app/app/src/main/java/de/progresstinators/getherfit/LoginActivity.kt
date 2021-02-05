package de.progresstinators.getherfit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import de.progresstinators.getherfit.settings.User
import de.progresstinators.getherfit.shared.BaseActivity

class LoginActivity : BaseActivity() {

    /**
     * The log in request id
     */
    companion object {
        const val SIGN_IN = 1
    }

    /**
     * The sign in client
     */
    private var signInClient: GoogleSignInClient? = null

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Add the click action for the login button
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            val intent = signInClient!!.signInIntent
            startActivityForResult(intent, SIGN_IN)
        }

        // Create the sign in client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        signInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Check if signing in was successful
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account : GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) showMain(account)
            } catch (e: ApiException) {
                println(e.printStackTrace())
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Check if there was already a user logged in
     */
    override fun onStart() {
        super.onStart()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if(account != null) showMain(account)
    }

    /**
     * Show the main view
     */
    private fun showMain(account: GoogleSignInAccount) {
        User.logIn(account, signInClient!!)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}