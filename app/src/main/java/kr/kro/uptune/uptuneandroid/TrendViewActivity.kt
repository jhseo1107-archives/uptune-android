package kr.kro.uptune.uptuneandroid

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
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

        var url = URL("https://jhseo1107.kro.kr/uptune/getTrendVideo?trendid="+trendid)

        var jsonObject = JSONObject()
        var parser = JSONParser()

        var mediaController = MediaController(this)

                launch { // get
            var conn = url.openConnection() as HttpsURLConnection

            jsonObject = withContext(Dispatchers.IO) {
                conn.requestMethod = "GET"
                conn.doInput = true; conn.doOutput = true;

                conn.connect()

                var br = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                parser.parse(br) as JSONObject
            }
            if (jsonObject.get("status").toString() == "403") {
                changeScreen(MainActivity::class.java)
            } else if (jsonObject.get("status").toString() != "200") {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
                return@launch
            }

            var trendId = jsonObject.get("trendid").toString()
            var trendName = jsonObject.get("trendname").toString()
            var trendTimeStamp = jsonObject.get("trendtimestamp").toString()
            var trendWriter = jsonObject.get("trendwriter").toString()
            var trendExtension = jsonObject.get("trendextension").toString()
            var trendLikes = jsonObject.get("trendlikes").toString()
            var trendHasLiked = jsonObject.get("hasliked").toString()

            var commentArray = jsonObject.get("comment") as JSONArray

            launch { // 영상 처리
                var trendProgressBar = findViewById(R.id.trendProgressBar) as ProgressBar
                var infoLinLay = findViewById(R.id.trendInfoLayout) as LinearLayout
                var trendVideoView = findViewById(R.id.trendVideoView) as VideoView

                var objectTitle = infoLinLay.getChildAt(0) as TextView
                var objectInfo = infoLinLay.getChildAt(1) as TextView

                objectTitle.text = trendName
                objectInfo.text = String.format("작성자 : %s 작성일 : %s", trendWriter, trendTimeStamp)

                var videoUrl = "https://jhseo1107.kro.kr/uptune/Videos/"+trendId+"."+trendExtension

                mediaController.setAnchorView(trendVideoView)

                trendVideoView.setMediaController(mediaController)

                trendVideoView.setVideoURI(Uri.parse(videoUrl))

                Log.v("videoview","parsed "+Uri.parse(videoUrl))

                trendVideoView.start()

                Log.v("videoview", "started")

                trendProgressBar.visibility = View.GONE
                infoLinLay.visibility = View.VISIBLE
                trendVideoView.visibility = View.VISIBLE


            }

            launch { //댓글 처리

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun makeToast(content: String, type: Int) {
        Toast.makeText(this.applicationContext, content, type).show()
    }

    fun changeScreen(activity: Class<*>) {
        var intent = Intent(this, activity)
        startActivity(intent)
    }
}
