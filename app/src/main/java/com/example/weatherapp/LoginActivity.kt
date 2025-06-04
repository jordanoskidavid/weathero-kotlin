package com.example.weatherapp

import BaseActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Initialize Facebook callback manager
        callbackManager = CallbackManager.Factory.create()

        // Configure Google Sign In with account selection
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            LoginScreen(
                onLogin = { email, password ->
                    loginUser(email, password)
                },
                onGoogleLogin = {
                    signInWithGoogle()
                },
                onFacebookLogin = {
                    signInWithFacebook()
                },
                onNavigateToRegister = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                }
            )
        }
    }

    private fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, getString(R.string.enter_email_password), Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success
                    Toast.makeText(this, getString(R.string.login_successfully), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WeatherActivity::class.java))
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        task.exception?.message ?: getString(R.string.login_unsuccessfully),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun signInWithGoogle() {
        // Create a new GoogleSignInClient instance to force account selection
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        val tempGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign out from current session to show account picker
        tempGoogleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = tempGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            listOf("email", "public_profile")
        )

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    Toast.makeText(this@LoginActivity, "Facebook login error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    // Get additional user info from Facebook Graph API
                    val request = GraphRequest.newMeRequest(token) { obj, _ ->
                        try {
                            val name = obj?.getString("name")
                            val email = obj?.getString("email")
                            Log.d(TAG, "Facebook user: $name, $email")

                            Toast.makeText(this, "Welcome ${name ?: user?.displayName}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, WeatherActivity::class.java))
                            finish()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing Facebook user data", e)
                            Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, WeatherActivity::class.java))
                            finish()
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email")
                    request.parameters = parameters
                    request.executeAsync()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Optional: Add this method to completely reset Google account selection
    private fun clearGoogleAccountCache() {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            Toast.makeText(this, getString(R.string.google_cache_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, getString(R.string.firebase_auth_wgoogle) + account.id)
                Log.d(TAG, getString(R.string.authentication_email) + account.email)
                Log.d(TAG, getString(R.string.authentication_idtoken) + (account.idToken != null))
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, getString(R.string.google_login_failed) + e.statusCode, e)
                val errorMessage = when (e.statusCode) {
                    10 -> "Developer error. Check SHA-1 fingerprint and google-services.json configuration."
                    12501 -> getString(R.string.login_canceled)
                    12502 -> getString(R.string.login_inprogress)
                    7 -> getString(R.string.check_your_internet)
                    else -> "Google sign in failed: ${e.message} (Code: ${e.statusCode})"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WeatherActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onGoogleLogin: () -> Unit,
    onFacebookLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun doLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = context.getString(R.string.fill_all_fields)
        } else {
            errorMessage = null
            isLoading = true
            onLogin(email, password)
            isLoading = false
        }
    }

    fun doGoogleLogin() {
        isLoading = true
        onGoogleLogin()
        isLoading = false
    }

    fun doFacebookLogin() {
        isLoading = true
        onFacebookLogin()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.login_to_account),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color(0xFF00BFFF),
                unfocusedLabelColor = Color.Black,
                cursorColor = Color(0xFF00BFFF),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color(0xFF00BFFF),
                unfocusedLabelColor = Color.Black,
                cursorColor = Color(0xFF00BFFF),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = { if (!isLoading) doLogin() },
            shape = RoundedCornerShape(8.dp),
            color = if (isLoading) Color.Gray else Color(0xFF00BFFF),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.login),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = stringResource(id = R.string.dont_have_account),
            modifier = Modifier
                .clickable { if (!isLoading) onNavigateToRegister() }
                .padding(top = 8.dp),
            color = if (isLoading) Color.Gray else Color(0xFF00BFFF),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.or_continue_with),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            onClick = { if (!isLoading) doGoogleLogin() },
            shape = RoundedCornerShape(8.dp),
            color = if (isLoading) Color.Gray else Color(0xFFEA4335),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.google),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            onClick = { if (!isLoading) doFacebookLogin() },
            shape = RoundedCornerShape(8.dp),
            color = if (isLoading) Color.Gray else Color(0xFF1565C0),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.facebook),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}