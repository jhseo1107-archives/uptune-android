package kr.kro.uptune.uptuneandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class RegisterActivity : AppCompatActivity() , CoroutineScope{

    private lateinit var job: Job

    override val coroutineContext : CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_register)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun onRegister(view : View)
    {
        var idEditText : EditText = findViewById(R.id.idText)
        var nameEditText : EditText = findViewById(R.id.nameText)
        var pwEditText : EditText = findViewById(R.id.passwordText)

        var givenid = idEditText.text
        var givenname = nameEditText.text
        var givenpw = pwEditText.text

        var parser = JSONParser()
        var jsonObject = JSONObject()

        var url = URL("https://jhseo1107.kro.kr/uptune/registerStart")
        var bwwrite = "mail=" + givenid + "&pw=" + givenpw + "&usrname=" + givenname

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

            if(jsonObject.get("status").toString() == "409")
            {
                makeToast("이미 회원가입된 이메일입니다. 다른 이메일을 이용해 주세요.", Toast.LENGTH_LONG)
            }
            else if(jsonObject.get("status").toString() == "200")
            {
                var mailInformTextView : TextView = findViewById(R.id.mailInformTextView)
                var authEditText : EditText = findViewById(R.id.authText)
                var authButton : Button = findViewById(R.id.authButton)
                var legalInformTextView : TextView = findViewById(R.id.legalInformTextView)

                mailInformTextView.visibility = View.VISIBLE
                authEditText.visibility = View.VISIBLE
                authButton.visibility = View.VISIBLE
                legalInformTextView.visibility = View.VISIBLE
            }
            else
            {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
            }

        }
    }

    fun onAuth(view : View)
    {
        var authEditText : EditText = findViewById(R.id.authText)

        var authValue = authEditText.text

        var url = URL("https://jhseo1107.kro.kr/uptune/registerFinish")
        var bwwrite = "auth="+authValue

        var parser = JSONParser()
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


            if(jsonObject.get("status").toString() == "403")
            {
                makeToast("보안코드가 잘못되었습니다.", Toast.LENGTH_LONG)
            }
            else if (jsonObject.get("status").toString() == "200")
            {
                makeToast("회원가입이 완료되었습니다, 로그인을 해주세요.", Toast.LENGTH_LONG)
                changeScreen(LoginActivity::class.java)
                finish()
            }
            else
            {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
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
}
