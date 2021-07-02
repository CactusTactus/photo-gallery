package com.example.photogallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.photogallery.databinding.ActivityPhotoPageBinding

class PhotoPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoPageBinding
    private lateinit var photoPageFragment: PhotoPageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.photo_fragment_container)
        if (currentFragment == null) {
            photoPageFragment = PhotoPageFragment.newInstance(intent.data ?: Uri.EMPTY)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.photo_fragment_container, photoPageFragment)
                .commit()
        } else {
            photoPageFragment = currentFragment as PhotoPageFragment
        }
    }

    override fun onBackPressed() {
        if (photoPageFragment.canMoveToPreviousWebPage()) {
            photoPageFragment.moveToPreviousWebPage()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}