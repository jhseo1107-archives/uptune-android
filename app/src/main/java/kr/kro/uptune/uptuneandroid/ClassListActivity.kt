package kr.kro.uptune.uptuneandroid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class ClassListActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job : Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_class_list)

        var url = URL("https://jhseo1107.kro.kr/uptune/getClasses")

        var jsonObject = JSONObject()
        var parser = JSONParser()

        launch {
            var conn = url.openConnection() as HttpsURLConnection

            jsonObject = withContext(Dispatchers.IO) {
                conn.setRequestProperty("content-type", "application/x-www-form-urlencoded")
                conn.requestMethod = "POST"
                conn.doInput = true; conn.doOutput = true;

                var os = conn.outputStream

                os.flush()
                os.close()

                conn.connect()

                var br = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                parser.parse(br) as JSONObject
            }
            if (jsonObject.get("status").toString() == "403") {
                changeScreen(MainActivity::class.java)
                return@launch
            } else if (jsonObject.get("status").toString() != "200") {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
                return@launch
            }

            var progressBar = findViewById(R.id.classListProgressBar) as ProgressBar

            progressBar.visibility = View.GONE

            var classArray = jsonObject.get("class") as JSONArray

            var classNum = classArray.size

            var classLinLay = findViewById(R.id.classLinLay) as LinearLayout
            classLinLay.orientation = LinearLayout.VERTICAL

            var inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            /*Log.v("Uptune", classNum.toString())

            for(i in 0 until classNum)
            {
                var tempObject = classArray.get(i) as JSONObject

                Log.v("Uptune", tempObject.toJSONString())

                var classObject = inflater.inflate(R.layout.layout_class, null, true) as View

                var id = classObject.findViewById(R.id.classId) as TextView
                var title = classObject.findViewById(R.id.classTitle) as TextView
                var percentage = classObject.findViewById(R.id.classPercentage) as TextView

                id.text = tempObject.get("id").toString()
                title.text = tempObject.get("name").toString()
                percentage.text = tempObject.get("percentage").toString()

                Log.v("Uptune", classObject.toString())

                classLinLay.addView(classObject)
            }*/
        }
    }

    fun onClassClick(view : View)
    {
        changeScreen(ShowClassActivity::class.java)
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
