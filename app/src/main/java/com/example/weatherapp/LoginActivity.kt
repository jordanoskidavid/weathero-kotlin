package com.example.weatherapp

import BaseActivity
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            LoginScreen(
                onLogin = { email, password ->
                    loginUser(email, password)
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
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun doLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = context.getString(R.string.fill_all_fields)
        } else {
            errorMessage = null
            onLogin(email, password)
        }
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
            onClick = { doLogin() },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF00BFFF),
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
                Text(
                    text = stringResource(id = R.string.login),
                    color = Color.White,
                    fontSize = 20.sp
                )
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
                .clickable { onNavigateToRegister() }
                .padding(top = 8.dp),
            color = Color(0xFF00BFFF),
            fontSize = 18.sp
        )
    }
}
