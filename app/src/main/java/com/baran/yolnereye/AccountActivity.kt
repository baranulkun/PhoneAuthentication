package com.baran.yolnereye

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.PhoneAuthCredential

class AccountActivity : AppCompatActivity() {

    private lateinit var numButton : Button
    private lateinit var numText : EditText
    private lateinit var verCont : Button
    private lateinit var verText : EditText
    private lateinit var auth : FirebaseAuth
    private lateinit var number : String
    private lateinit var verId : String
    private var isButtonEnabled = true
    private var remainingTime = 180

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        init()
        val editText = findViewById<EditText>(R.id.verText)
        verCont.visibility = View.INVISIBLE
        verText.visibility = View.INVISIBLE

        numButton.setOnClickListener() {
            number = numText.text.trim().toString()
            if (number.isNotEmpty()) {
                if (number.length == 10) {
                    number = "+90$number"
                    number = number.trim()
                    if (isButtonEnabled) {
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("Warning")
                        val num = number
                        builder.setMessage("Are you sure your number is $num? (In case it is wrong" +
                                "You will wait 3 minutes.)")

                        builder.setPositiveButton("Okay") { dialog, which ->
                            numButton.isEnabled = false
                            isButtonEnabled = false

                            val fadeInAnimation = AnimatorInflater.loadAnimator(this, R.animator.fade_in) as AnimatorSet
                            fadeInAnimation.setTarget(verCont)
                            fadeInAnimation.playTogether(
                                ObjectAnimator.ofFloat(verText, "alpha", 0f, 1f),
                            )
                            fadeInAnimation.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationStart(animation: Animator) {
                                    super.onAnimationStart(animation)
                                    verCont.visibility = View.VISIBLE
                                    verText.visibility = View.VISIBLE
                                }
                            })
                            fadeInAnimation.start()
                            startCountdown()
                            Toast.makeText(this, "Number = $number", Toast.LENGTH_SHORT).show()
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(number)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(this)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show()
                        }
                        builder.setNegativeButton("Cancel") { dialog, which ->
                        }
                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }

                } else {
                    Toast.makeText(this, "sample login: 5555555555", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "sample login: 5555555555", Toast.LENGTH_SHORT).show()
            }
        }

        verCont.setOnClickListener() {
            if (verText.text.isNotEmpty()){
                verifycode(verText.text.toString().replace("-", "").trim())
            }
            else
                Toast.makeText(this, "incorrect login", Toast.LENGTH_SHORT).show()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            var isTrue = true
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                if (input.length == 3 && isTrue) {
                    isTrue=false
                    val formattedInput = "$input-"
                    editText.setText(formattedInput)
                    editText.setSelection(formattedInput.length)
                }
                else if (input.length > 7) {
                    val formattedInput = input.substring(0, 7)
                    editText.setText(formattedInput)
                    editText.setSelection(formattedInput.length)
                }
                if(input.length == 2)
                    isTrue=true
            }
        })

        val editNumText = findViewById<EditText>(R.id.numText)

        editNumText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                 val input = s.toString()
                 if (input.length > 10) {
                    val formattedInput = input.substring(0, 10)
                     editNumText.setText(formattedInput)
                     editNumText.setSelection(formattedInput.length)
                }
            }
        })
    }
    private fun startCountdown() {
        val timer = object : CountDownTimer((remainingTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime--
                var min = remainingTime/60
                var sec = remainingTime%60
                numButton.text="Send Again($min:$sec)"
            }

            override fun onFinish() {
                numButton.isEnabled = true
                isButtonEnabled = true
                numButton.text="Send Again"
                remainingTime = 180 // 3 min
                verCont.visibility = View.INVISIBLE
                verText.visibility = View.INVISIBLE
            }
        }

        timer.start()
    }

    private fun init(){
        numButton = findViewById(R.id.numButton)
        numText = findViewById(R.id.numText)
        auth = FirebaseAuth.getInstance()
        verCont = findViewById(R.id.verCont)
        verText = findViewById(R.id.verText)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode
            if (code != null){
                Toast.makeText(this@AccountActivity, "Code: $code", Toast.LENGTH_SHORT).show()
                verifycode(code)
            }

        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@AccountActivity, "verification failed", Toast.LENGTH_SHORT).show() // Ekledim
        }

        override fun onCodeSent(
            @NonNull verificationId: String,
            @NonNull token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            verId = verificationId
            Toast.makeText(this@AccountActivity, "code sent", Toast.LENGTH_SHORT).show() // Ekledim
        }
    }

    private fun verifycode(code:String){
        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verId, code)
        signingByCredintals(credential)
    }

    private fun signingByCredintals(credential: PhoneAuthCredential) {
        val firebase: FirebaseAuth = FirebaseAuth.getInstance()
        firebase.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@AccountActivity, "login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AccountActivity, HomeActivity::class.java))
                } else {
                    Toast.makeText(this@AccountActivity, "login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if(currentUser!=null) {
            startActivity(Intent(this@AccountActivity, HomeActivity::class.java))
            finish()
        }
    }
}


