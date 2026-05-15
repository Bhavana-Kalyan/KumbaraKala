package com.kumbarakala

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etName       = findViewById<EditText>(R.id.etName)
        val etVillage    = findViewById<EditText>(R.id.etVillage)
        val etPhone      = findViewById<EditText>(R.id.etPhone)
        val etGeneration = findViewById<EditText>(R.id.etGeneration)
        val btnSave      = findViewById<Button>(R.id.btnSave)
        val btnCatalog   = findViewById<Button>(R.id.btnGoToCatalog)

        val prefs = getSharedPreferences("artisan", Context.MODE_PRIVATE)
        etName.setText(prefs.getString("name", ""))
        etVillage.setText(prefs.getString("village", ""))
        etPhone.setText(prefs.getString("phone", ""))
        etGeneration.setText(prefs.getString("generation", ""))

        btnSave.setOnClickListener {
            val name       = etName.text.toString().trim()
            val village    = etVillage.text.toString().trim()
            val phone      = etPhone.text.toString().trim()
            val generation = etGeneration.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this,
                    "Please enter your name and phone number",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit()
                .putString("name", name)
                .putString("village", village)
                .putString("phone", phone)
                .putString("generation", generation)
                .apply()

            Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
        }

        btnCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogActivity::class.java))
        }
    }
}