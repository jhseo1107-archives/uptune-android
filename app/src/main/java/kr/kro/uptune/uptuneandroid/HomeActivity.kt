package kr.kro.uptune.uptuneandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity() , CoroutineScope{

    private lateinit var job : Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_home)

        var url = URL("https://jhseo1107.kro.kr/uptune/getMain")

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
            if(jsonObject.get("status").toString() == "403")
            {
                changeScreen(MainActivity::class.java)
            }
            else if(jsonObject.get("status").toString() != "200")
            {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
            }

            var classarray : JSONArray = jsonObject.get("class") as JSONArray
            var trendarray : JSONArray = jsonObject.get("trend") as JSONArray

            launch { //Class
                var classnum = classarray.size

                var classProgressBar = findViewById(R.id.classProgressBar) as ProgressBar
                var classNothing = findViewById(R.id.classNothing) as TextView

                var classObjectList : ArrayList<ConstraintLayout> = ArrayList<ConstraintLayout>()

                classObjectList.add(findViewById(R.id.class1) as ConstraintLayout)
                classObjectList.add(findViewById(R.id.class2) as ConstraintLayout)
                classObjectList.add(findViewById(R.id.class3) as ConstraintLayout)

                if(classnum == 0)
                {
                    classProgressBar.visibility = View.GONE
                    classNothing.visibility = View.VISIBLE
                    return@launch
                }

                for(i in 0 until classarray.size)
                {
                    var tempConLay = classObjectList.get(i)

                    tempConLay.visibility = View.VISIBLE

                    var objectId = tempConLay.getChildAt(2) as TextView
                    var objectTitle = tempConLay.getChildAt(3) as TextView
                    var objectPercentage = tempConLay.getChildAt(4) as TextView

                    objectId.text = (classarray.get(i) as JSONObject).getInt("id").toString()
                    objectTitle.text = (classarray.get(i) as JSONObject).getString("name")
                    objectPercentage.text = (classarray.get(i) as JSONObject).getInt("precentage").toString()
                }
            }

            launch { //Trend

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        finishAndRemoveTask()
        Process.killProcess(Process.myPid())
    }

    fun makeToast(content : String, type : Int)
    {
        Toast.makeText(this.applicationContext, content, type).show()
    }

    fun changeScreen(activity : Class<*>)
    {
        var intent = Intent(this, activity)
        startActivity(intent)
        finish()
    }

}
