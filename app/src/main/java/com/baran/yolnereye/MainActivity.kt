package com.baran.yolnereye

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val animatorSet = AnimatorSet()
        val buttonAnimation = AnimatorInflater.loadAnimator(this, R.animator.fade_in) as AnimatorSet
        val imageAnimation = AnimatorInflater.loadAnimator(this, R.animator.fade_in) as AnimatorSet

        buttonAnimation.setTarget(button)
        imageAnimation.setTarget(imageView)
        animatorSet.playTogether(buttonAnimation, imageAnimation)
        animatorSet.start()

        button.setOnClickListener {
            val intent = Intent(this,AccountActivity::class.java)
            startActivity(intent)
        }
    }
}
