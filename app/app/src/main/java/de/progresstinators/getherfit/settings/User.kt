package de.progresstinators.getherfit.settings

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import de.progresstinators.getherfit.R
import java.io.InputStream
import java.net.URL

object User {
    /**
     * The first name of the user
     */
    var firstName: String = ""

    /**
     * The first name of the user
     */
    var lastName: String = ""

    /**
     * The email of the user
     */
    var email: String = ""

    /**
     * The email of the user
     */
    var image: Bitmap? = null

    /**
     * The user account
     */
    private lateinit var account: GoogleSignInAccount

    /**
     * Log in with a given google user and extract information
     *
     * @param account The corresponding google account
     */
    fun logIn(account: GoogleSignInAccount) {
        this.account = account
        firstName = account.givenName.toString()
        lastName = account.familyName.toString()
        email = account.email.toString()
        Thread {
            image = try {
                val input: InputStream = URL(account.photoUrl.toString()).openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                null
            }
        }.start()
    }

    /**
     * Log out the current user
     */
    fun logOut(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        val signInClient: GoogleSignInClient = GoogleSignIn.getClient(activity, gso)
        signInClient.signOut().addOnCompleteListener(activity) {
            Toast.makeText(activity, R.string.login_logout, Toast.LENGTH_LONG).show()
        }
    }
}