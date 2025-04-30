package com.example.randommeowfacts

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.randommeowfacts.databinding.ActivityMainBinding
import com.google.gson.Gson

import java.net.URL
import javax.net.ssl.HttpsURLConnection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }

    fun fetchFact(view: View) {
        binding.loadingBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val url = URL("https://meowfacts.herokuapp.com/")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == 200) {
                    val inputStream = connection.inputStream.bufferedReader().use { it.readText() }

                    val fact = Gson().fromJson(inputStream, Request::class.java).data[0]

                    CoroutineScope(Dispatchers.Main).launch {
                        binding.randomFact.text = fact
                        binding.loadingBar.visibility = View.GONE

                    }

                } else {

                    CoroutineScope(Dispatchers.Main).launch {

                        Toast.makeText(view.context, "Connection error: ${connection.responseCode}", Toast.LENGTH_SHORT).show()
                        binding.loadingBar.visibility = View.GONE

                    }
                }
                connection.disconnect()
            } catch (e: Exception) {

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(view.context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.loadingBar.visibility = View.GONE
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("FACT_TEXT", binding.randomFact.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val fact = savedInstanceState.getString("FACT_TEXT")
        binding.randomFact.text = fact
    }
}