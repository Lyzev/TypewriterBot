package dev.lyzev.typewriterbot

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.microsoft.alm.secret.Credential
import com.microsoft.alm.storage.StorageProvider
import java.net.URI

/**
 * This class is a Composable function that creates a GUI for a TypewriterBot using the Compose library
 */
@Composable
@Preview
fun App(username: MutableState<String>, password: MutableState<String>) {
    var enabled by remember { mutableStateOf(true) }

    var username by remember { username }

    var password by remember { password }
    var passwordVisible by remember { mutableStateOf(false) }

    var wpm by remember { mutableStateOf(60) }

    var errorProbability by remember { mutableStateOf(2) }

    var levels by remember { mutableStateOf(10) }

    MaterialTheme(colors = darkColors()) {
        Scaffold {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display the GitHub icon and user's GitHub
                OutlinedButton({ openInBrowser(URI("https://github.com/Lyzev")) }) {
                    Row(
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource("github-mark-white.svg"), "GitHub", Modifier.width(32.dp), Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("Lyzev", color = Color.White, fontSize = 16.sp)
                    }
                }

                // Spacer for formatting
                Spacer(Modifier.height(5.dp))

                // Text field for username input
                OutlinedTextField(value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    placeholder = { Text("Username") })

                // Spacer for formatting
                Spacer(Modifier.height(5.dp))

                // Text field for password input
                OutlinedTextField(value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    placeholder = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton({ passwordVisible = !passwordVisible }) {
                            Icon(image, description)
                        }
                    })

                // Spacer for formatting
                Spacer(Modifier.height(5.dp))

                // Text field for typing speed input
                OutlinedTextField(
                    value = wpm.toString(),
                    onValueChange = { value ->
                        if (value.length in 1..4) wpm = value.filter { it.isDigit() }.toInt()
                        else if (value.isEmpty()) wpm = 1
                    },
                    label = { Text("Words per Minute") },
                    singleLine = true,
                    placeholder = { Text("Words per Minute") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Spacer for formatting
                Spacer(Modifier.height(5.dp))

                // Text field for error probability input
                OutlinedTextField(
                    value = errorProbability.toString(),
                    onValueChange = { value ->
                        if (value.length in 1..3) errorProbability = value.filter { it.isDigit() }.toInt()
                        else if (value.isEmpty()) errorProbability = 0
                    },
                    label = { Text("Error Probability in %") },
                    singleLine = true,
                    placeholder = { Text("Error Probability in %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Spacer for formatting
                Spacer(Modifier.height(5.dp))

                // Text field for number of levels input
                OutlinedTextField(
                    value = levels.toString(),
                    onValueChange = { value ->
                        if (value.length in 1..3) levels = value.filter { it.isDigit() }.toInt()
                        else if (value.isEmpty()) levels = 1
                    },
                    label = { Text("Levels") },
                    singleLine = true,
                    placeholder = { Text("Levels") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Spacer for formatting
                Spacer(Modifier.height(5.dp))

                // Button to start/stop the bot
                OutlinedButton({
                    if (TypewriterBot.isRunning) {
                        Thread {
                            enabled = false

                            // Stop the bot
                            TypewriterBot.stop()

                            enabled = true
                        }.start()
                    } else {
                        Thread {
                            enabled = false

                            // Calculate delay between keystrokes
                            val delay = 1000.0 / ((wpm * 5) / 60.0)

                            // Setups the bot
                            TypewriterBot.setup(
                                username,
                                password,
                                (delay * .95).toLong(), (delay * 1.05).toLong(),
                                errorProbability,
                                levels
                            )

                            enabled = true

                            // Starts the bot
                            TypewriterBot.start()
                        }.start()
                    }
                }, enabled = enabled) {
                    Text(if (TypewriterBot.isRunning) "Stop" else "Start", fontSize = 16.sp)
                }
            }
        }
    }
}

fun main() = application {
    // Get login credentials from windows credential manager
    val storage = StorageProvider.getCredentialStorage(true, StorageProvider.SecureOption.PREFER)
    val credential = storage["TypewriterBot"] ?: Credential("", "")
    val username = mutableStateOf(credential.Username)
    val password = mutableStateOf(credential.Password)

    // Creates a window
    Window(
        onCloseRequest = {
            // Save login credentials to windows credential manager
            storage.add("TypewriterBot", Credential(username.value, password.value))
            // Close the window
            exitApplication()
        }, state = rememberWindowState(
            WindowPlacement.Floating, false, WindowPosition(Alignment.Center), 350.dp, 500.dp
        ), resizable = false, title = "TypewriterBot by Lyzev", icon = painterResource("icon.ico")
    ) {
        App(username, password)
    }
}