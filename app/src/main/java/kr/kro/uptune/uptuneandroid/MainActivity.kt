package kr.kro.uptune.uptuneandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.kro.uptune.uptuneandroid.R
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.coroutines.*
import kr.kro.uptune.uptuneandroid.LoginActivity
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.*
import java.lang.Exception
import java.net.CookieHandler
import java.net.CookieManager
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() , CoroutineScope {

    private lateinit var job : Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_main)
        val cookieManager = CookieManager()
        CookieHandler.setDefault(cookieManager)
        val handler = Handler()
        handler.postDelayed({

            if(!("autologin.txt" in fileList()))
            {
                changeScreen(LoginActivity::class.java)
            }
            for (x in fileList()) {
                if (x == "autologin.txt") {
                    try {
                        val fis = this.applicationContext.openFileInput("autologin.txt")
                        val br = BufferedReader(InputStreamReader(fis, "UTF-8"))
                        /*var sb = StringBuilder()
                        var line : String? = null
                        while({line = br.readLine(); line}() != null)
                        {
                            sb.append(line)
                        }*/

                        var content = br.readLine()

                        val parser = JSONParser()

                        var filejsonObject = parser.parse(content) as JSONObject
                        val id = filejsonObject.get("mail").toString()
                        val pw = filejsonObject.get("pw").toString()

                        if(id == "null")
                        {
                            changeScreen(LoginActivity::class.java)
                            return@postDelayed
                        }

                        var url = URL("https://jhseo1107.kro.kr/uptune/login")
                        var bwwrite = "mail=" + id + "&pw=" + pw

                        var jsonObject = JSONObject()

                        launch {

                            var conn = url.openConnection() as HttpsURLConnection

                            jsonObject = withContext(Dispatchers.IO) {
                                conn.setRequestProperty("content-type", "application/x-www-form-urlencoded")
                                conn.requestMethod = "POST"
                                conn.doInput = true; conn.doOutput = true;

                                var os = conn.outputStream
                                var bw = BufferedWriter(OutputStreamWriter(os, "UTF-8"))

                                bw.write(bwwrite)
                                bw.flush()
                                bw.close()
                                os.flush()
                                os.close()

                                conn.connect()

                                var br = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                                parser.parse(br) as JSONObject
                            }
                            if (jsonObject.get("status").toString() == "400") {
                                makeToast( "자동로그인 실패", Toast.LENGTH_LONG)
                                changeScreen(LoginActivity::class.java)
                            } else if (jsonObject.get("status").toString() == "403.8") {
                                makeToast("자동로그인 실패", Toast.LENGTH_LONG)
                                changeScreen(LoginActivity::class.java)
                            } else if (jsonObject.get("status").toString() == "403") {
                                makeToast("자동로그인 실패", Toast.LENGTH_SHORT)
                                changeScreen(LoginActivity::class.java)
                            } else if (jsonObject.get("status").toString() == "200") {
                                changeScreen(HomeActivity::class.java)
                            } else {
                                makeToast("알 수 없는 오류가 발생했습니다", Toast.LENGTH_LONG)
                                changeScreen(LoginActivity::class.java)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, 1000) //5 seconds
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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