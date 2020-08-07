package kr.kro.uptune.uptuneandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.URL
import kotlin.coroutines.CoroutineContext

class TrendViewActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_trend_view)

        var bndl = intent.extras

        var trendid = bndl?.getString("trendid")

        var url = URL("https://jhseo1107.kro.kr/uptune/getTrendVideo?id="+trendid)

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
