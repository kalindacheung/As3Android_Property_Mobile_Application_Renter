package com.example.renter_app_g04

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.renter_app_g04.databinding.LoginScreenBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginScreen : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Login"

        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            this.login()
        }
    }

    private fun login() {
        if (binding.etEmail.text.isNotEmpty() && binding.etPassword.text.isNotEmpty()) {
            val emailFromUI = binding.etEmail.text.toString()
            val passwordFromUI = binding.etPassword.text.toString()

            auth.signInWithEmailAndPassword(emailFromUI, passwordFromUI)
                .addOnCompleteListener(this) {
                        task ->
                    if (task.isSuccessful) {
                        Snackbar.make(binding.root, "Login Successful", Snackbar.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Snackbar.make(binding.root, "Login Failed", Snackbar.LENGTH_SHORT).show()
                    }
                }
        } else {
            Snackbar.make(binding.root, "Ensure both forms are filled out", Snackbar.LENGTH_SHORT).show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}