package kr.kro.uptune.uptuneandroid

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@RuntimePermissions
class VideoUploadActivity : AppCompatActivity() {

    var fileUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_upload)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onSelectVidClick(view: View) {

        var intent = Intent()
        intent.setType("video/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 1)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("vidup", "onActivityResult fired")

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.v("vidup", "success")
            fileUri = data?.data

            Log.v("vidup", RealPath.getRealPath(this.applicationContext, fileUri))
            makeToast(RealPath.getRealPath(this.applicationContext, fileUri), Toast.LENGTH_LONG)
        }
        else
        {
            Log.v("vidup", "fail")
        }
    }

    fun onUploadVidClick(view: View) {
        val parser = JSONParser()
        var jsonObject = JSONObject()
        val url = URL("https://jhseo1107.kro.kr/uptune/writeTrend")

        var boundary = "WebAppBoundary"
        var LINE_FEED = "\r\n"

        var context = this.applicationContext

        GlobalScope.launch {
            var conn = url.openConnection() as HttpsURLConnection

            jsonObject = withContext(Dispatchers.IO) {
                conn.setRequestProperty("content-type", "multipart/form-data;charset=utf-8;boundary="+boundary)
                conn.requestMethod = "POST"
                conn.doInput = true; conn.doOutput = true;
                conn.useCaches = true

                var os = conn.outputStream
                var bw = BufferedWriter(OutputStreamWriter(os, "UTF-8"))

                Log.v("vidup", fileUri?.path.toString())
                Log.v("vidup", fileUri?.lastPathSegment.toString())

                bw.append("--"+boundary).append(LINE_FEED)
                bw.append("Content-Disposition: form-data; name=\"file\"; filename=\""+RealPath.getRealPath(context, fileUri).split("/").last()+"\"").append(LINE_FEED)
                bw.append(LINE_FEED)
                bw.append("> "+RealPath.getRealPath(context, fileUri))
                bw.append("--"+boundary+"--")
                bw.flush()

                bw.close()
                os.flush()
                os.close()

                conn.connect()

                var br = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                parser.parse(br) as JSONObject
            }

            Log.v("vidup", jsonObject.toJSONString())

            if (jsonObject.get("status").toString() == "403") {
                changeScreen(MainActivity::class.java)
                return@launch
            } else if (jsonObject.get("status").toString() != "200") {
                makeToast("알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG)
                return@launch
            }
            changeScreen(TrendListActivity::class.java)
        }
    }

    fun makeToast(content: String, type: Int) {
        Toast.makeText(this.applicationContext, content, type).show()

        this.applicationContext
    }

    fun changeScreen(activity: Class<*>) {
        var intent = Intent(this, activity)
        startActivity(intent)
    }
}
