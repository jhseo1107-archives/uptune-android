package kr.kro.uptune.uptuneandroid

import android.content.Intent
import android.os.Bundle
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import java.util.concurrent.CountDownLatch
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job : Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_login)
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

    fun onLogin(view : View)
    {
        var idEditText : EditText = findViewById(R.id.idText)
        var pwEditText : EditText = findViewById(R.id.passwordText)

        var givenid = idEditText.text
        var givenpw = pwEditText.text

        var jsonObject = JSONObject()

        var parser = JSONParser()

        var url = URL("https://jhseo1107.kro.kr/uptune/login")
        var bwwrite = "mail=" + givenid + "&pw=" + givenpw

        launch {
            var loginButton : Button = findViewById(R.id.loginButton)
            var loginProgressBar : ProgressBar = findViewById(R.id.loginProgressBar)

            loginButton.visibility = View.GONE
            loginProgressBar.visibility = View.VISIBLE

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
                makeToast( "존재하지 않는 이메일입니다. 회원가입을 시도해주세요.", Toast.LENGTH_LONG)
                loginButton.visibility = View.VISIBLE
                loginProgressBar.visibility = View.GONE
            } else if (jsonObject.get("status").toString() == "403.8") {
                makeToast("해당 계정은 이용약관 위반으로 인해 영구정지되었습니다.", Toast.LENGTH_LONG)
                loginButton.visibility = View.VISIBLE
                loginProgressBar.visibility = View.GONE
            } else if (jsonObject.get("status").toString() == "403") {
                makeToast("이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT)
                loginButton.visibility = View.VISIBLE
                loginProgressBar.visibility = View.GONE
            } else if (jsonObject.get("status").toString() == "200") {
                changeScreen(HomeActivity::class.java)
            } else {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
                loginButton.visibility = View.VISIBLE
                loginProgressBar.visibility = View.GONE
            }
        }
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

    fun onRegister(view : View)
    {
        var intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}

