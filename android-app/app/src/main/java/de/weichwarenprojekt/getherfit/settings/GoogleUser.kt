package de.weichwarenprojekt.getherfit.settings

import android.app.Activity
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import de.weichwarenprojekt.getherfit.R
import de.weichwarenprojekt.getherfit.data.User

object GoogleUser {

    /**
     * The client id for the google authentication
     */
    const val CLIENT_ID = "414056883619-63e6gab52n4jsnn5afgf8k532shddf1f.apps.googleusercontent.com"

    /**
     * The url of the server
     */
    private const val SERVER = "https://getherfit.p-scheede.de/api"

    /**
     * The request queue for scheduling HTTP requests
     */
    private lateinit var queue: RequestQueue

    /**
     * The actual user object
     */
    val user = User()

    /**
     * The user account
     */
    private lateinit var account: GoogleSignInAccount

    /**
     * Log in with a given google user and extract information
     *
     * @param activity The current context
     * @param account The corresponding google account
     */
    fun logIn(activity: Activity, account: GoogleSignInAccount) {
        // Initialize volley
        queue = Volley.newRequestQueue(activity)
        this.account = account
        loadUser()
    }

    /**
     * The standard class for an http request
     */
    class HttpRequest(
        method: Int,
        url: String,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) : StringRequest(method, url, listener, errorListener) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            println(account.idToken)
            if (account.idToken != null) headers["auth-token"] = account.idToken!!
            return headers
        }
    }

    /**
     * Load the user information
     */
    private fun loadUser() {
        val request = HttpRequest(
            Request.Method.GET,
            "${SERVER}/user/",
            { response ->
                println(response)
            },
            { error ->
                println(error)
            }
        )
        queue.add(request)
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