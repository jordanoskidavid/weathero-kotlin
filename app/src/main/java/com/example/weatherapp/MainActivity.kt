package com.example.weatherapp

import BaseActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.example.weatherapp.ui.theme.WeatherAppTheme
import java.util.*
import android.app.Activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth



class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load saved language preference and apply locale
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedLang = prefs.getString("lang", "en") ?: "en"
        updateLocale(savedLang)

        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToRegister = {
                            startActivity(Intent(this, RegisterActivity::class.java))
                        },
                        onNavigateToLogin = {
                            startActivity(Intent(this, LoginActivity::class.java))
                        },
                        onNavigateToSettings = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun updateLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = LocalContext.current.getSharedPreferences("settings", 0)
    val savedLangCode = prefs.getString("lang", "en") ?: "en"
    var currentLanguage by remember {
        mutableStateOf(
            when (savedLangCode) {
                "mk" -> "Македонски"
                else -> "English"
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF00BFFF))
    )
    {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp)
                    .padding(end = 16.dp, top = 16.dp)
                    .clickable { onNavigateToSettings() }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )

            Text(
                text = stringResource(id = R.string.welcome_to),
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = Color.White
            )

            Text(
                text = stringResource(id = R.string.app_name),
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.95f), shape = MaterialTheme.shapes.medium)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.access_your_weather_info),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC474),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.register),
                        fontSize = 20.sp
                    )
                }

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.login),
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signInAnonymously()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(context, WeatherActivity::class.java)
                                    context.startActivity(intent)
                                    if (context is Activity) {
                                        context.finish()
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.anonymous_login_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.anonymous), color = Color.Gray, fontSize = 18.sp)
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WeatherAppTheme {
        MainScreen(
            onNavigateToRegister = {},
            onNavigateToLogin = {},
            onNavigateToSettings = {}
        )
    }
}
