package kr.kro.uptune.uptuneandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar

class ClassVideoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_video_view)

        var videoProgressBar = findViewById(R.id.videoProgressBar) as ProgressBar

        videoProgressBar.visibility = View.GONE

        var html = "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/hMG1cg8wBmM\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>"
        var videoWebView = findViewById(R.id.videoWebView) as WebView

        videoWebView.settings.javaScriptEnabled = true
        videoWebView.loadData(html, "text/html", null)
    }
}
