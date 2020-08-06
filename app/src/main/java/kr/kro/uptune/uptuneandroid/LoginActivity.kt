package kr.kro.uptune.uptuneandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.*
import java.net.URL
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

        var givenid = idEditText.text.toString()
        var givenpw = pwEditText.text.toString()

        var jsonObject = JSONObject()

        var parser = JSONParser()

        var url = URL("https://jhseo1107.kro.kr/uptune/login")
        var bwwrite = "mail=" + givenid + "&pw=" + givenpw

        launch {
            var loginButton : Button = findViewById(R.id.loginButton)
            var loginProgressBar : ProgressBar = findViewById(R.id.loginProgressBar)
            var autoLoginCheck = findViewById(R.id.autoLoginCheck) as CheckBox

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
                GlobalScope.launch {
                    if (autoLoginCheck.isChecked) {
                        var jsonObject = JSONObject()

                        jsonObject.put("mail", givenid)
                        jsonObject.put("pw", givenpw)

                        try {
                            writeInternal("autologin.txt", jsonObject.toJSONString())
                        } catch (e: IOException) {
                            makeToast("자동로그인 정보 저장에 실패했습니다.", Toast.LENGTH_LONG)
                        }

                    }
                }
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
    }

    fun writeInternal(filename : String, content : String)
    {
        try{
            var fos : FileOutputStream = this.applicationContext.openFileOutput(filename, Context.MODE_PRIVATE)
            fos.write(content.toByteArray())
            fos.close()
        }
        catch (e : IOException)
        {
            throw e
        }

    }

    fun onRegister(view : View)
    {
        var intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}

