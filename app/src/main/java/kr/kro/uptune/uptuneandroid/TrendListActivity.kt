package kr.kro.uptune.uptuneandroid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class TrendListActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_trend_list)

        var url = URL("https://jhseo1107.kro.kr/uptune/getTrends")

        var parser = JSONParser()
        var jsonObject = JSONObject()

        launch {
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

            var toptrendarray: JSONArray = jsonObject.get("toptrend") as JSONArray
            var rectrendarray: JSONArray = jsonObject.get("rectrend") as JSONArray

            launch { //toptrend
                var ttnum = toptrendarray.size

                var ttProgressBar = findViewById(R.id.ttrendProgressBar) as ProgressBar
                var ttNothing = findViewById(R.id.ttrendNothing) as TextView

                ttProgressBar.visibility = View.GONE

                var trendObjectList: ArrayList<LinearLayout> = ArrayList<LinearLayout>()

                trendObjectList.add(findViewById(R.id.ttrend1Layout) as LinearLayout)
                trendObjectList.add(findViewById(R.id.ttrend2Layout) as LinearLayout)
                trendObjectList.add(findViewById(R.id.ttrend3Layout) as LinearLayout)
                trendObjectList.add(findViewById(R.id.ttrend4Layout) as LinearLayout)

                if (ttnum == 0) {
                    ttNothing.visibility = View.VISIBLE
                    return@launch
                }

                for (i in 0 until ttnum) {
                    var tempConLay = trendObjectList.get(i)

                    tempConLay.visibility = View.VISIBLE

                    var objectId = tempConLay.getChildAt(0) as TextView
                    var objectTitle = tempConLay.getChildAt(1) as TextView
                    var objectInfo = tempConLay.getChildAt(2) as TextView

                    var jsonObject = toptrendarray.get(i) as JSONObject

                    objectId.text = jsonObject.get("id").toString()
                    objectTitle.text = jsonObject.get("name").toString()
                    objectInfo.text = String.format("작성자 : %s 작성일 : %s 좋아요 : %s", jsonObject.get("writer").toString(), jsonObject.get("timestamp").toString(), jsonObject.get("likes").toString())
                }
            }

            launch { //Trend
                var rtnum = rectrendarray.size

                var rtProgressBar = findViewById(R.id.rtrendProgressBar) as ProgressBar
                var rtNothing = findViewById(R.id.rtrendNothing) as TextView

                rtProgressBar.visibility = View.GONE

                var rtObjectList: ArrayList<LinearLayout> = ArrayList<LinearLayout>()

                rtObjectList.add(findViewById(R.id.rtrend1Layout))
                rtObjectList.add(findViewById(R.id.rtrend2Layout))
                rtObjectList.add(findViewById(R.id.rtrend3Layout))
                rtObjectList.add(findViewById(R.id.rtrend4Layout))

                if (rtnum == 0) {
                    rtNothing.visibility = View.VISIBLE
                    return@launch
                }

                for (i in 0 until rtnum) {
                    var tempLinLay = rtObjectList.get(i)

                    tempLinLay.visibility = View.VISIBLE

                    var objectId = tempLinLay.getChildAt(0) as TextView
                    var objectTitle = tempLinLay.getChildAt(1) as TextView
                    var objectInfo = tempLinLay.getChildAt(2) as TextView

                    var jsonObject = rectrendarray.get(i) as JSONObject

                    objectId.text = jsonObject.get("id").toString()
                    objectTitle.text = jsonObject.get("name").toString()
                    objectInfo.text = String.format("작성자: %s 작성일: %s 좋아요: %s",jsonObject.get("writer").toString() ,jsonObject.get("timestamp").toString(), jsonObject.get("likes").toString())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun onTrendClick(view : View)
    {
        var linLay = view as LinearLayout

        var trendid = (linLay.getChildAt(0) as TextView).text.toString()


        var intent = Intent(this, TrendViewActivity::class.java)

        var bndl = Bundle()

        bndl.putString("trendid", trendid)

        intent.putExtras(bndl)

        startActivity(intent)
    }

    fun onRecTrendClick(view : View)
    {

    }

    fun onVideoUploadClick(view : View)
    {

    }

    fun makeToast(content: String, type: Int) {
        Toast.makeText(this.applicationContext, content, type).show()
    }

    fun changeScreen(activity: Class<*>) {
        var intent = Intent(this, activity)
        startActivity(intent)
    }

    fun writeInternal(filename: String, content: String) {
        try {
            var fos: FileOutputStream = this.applicationContext.openFileOutput(filename, Context.MODE_PRIVATE)
            fos.write(content.toByteArray())
            fos.close()
        } catch (e: IOException) {
            throw e
        }

    }
}
