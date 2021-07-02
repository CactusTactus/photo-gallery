package com.example.photogallery

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Visibility
import com.example.photogallery.databinding.FragmentPhotoPageBinding

private const val ARGUMENT_URI = "photo_page_url"

class PhotoPageFragment : VisibleFragment() {
    private lateinit var binding: FragmentPhotoPageBinding
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.getParcelable(ARGUMENT_URI) ?: Uri.EMPTY
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoPageBinding.inflate(layoutInflater, container, false)
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "Android"
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.apply {
                        if (newProgress == 100) {
                            visibility = View.GONE
                        } else {
                            visibility = View.VISIBLE
                            progress = newProgress
                        }
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    (activity as AppCompatActivity).supportActionBar?.subtitle = title
                }
            }
            webViewClient = WebViewClient()
            loadUrl(uri.toString())
        }
        return binding.root
    }

    fun canMoveToPreviousWebPage() = binding.webView.canGoBack()

    fun moveToPreviousWebPage() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        }
    }

    companion object {
        fun newInstance(uri: Uri): PhotoPageFragment {
            return PhotoPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARGUMENT_URI, uri)
                }
            }
        }
    }
}