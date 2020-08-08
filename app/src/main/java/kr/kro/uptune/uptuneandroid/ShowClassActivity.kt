package kr.kro.uptune.uptuneandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

class ShowClassActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_class)

        var classListProgressBar = findViewById(R.id.classListProgressBar) as ProgressBar

        classListProgressBar.visibility = View.GONE
    }

    fun onClassVideoClick(view : View)
    {
        changeScreen(ClassVideoViewActivity::class.java)
    }

    fun makeToast(content: String, type: Int) {
        Toast.makeText(this.applicationContext, content, type).show()
    }

    fun changeScreen(activity: Class<*>) {
        var intent = Intent(this, activity)
        startActivity(intent)
    }
}
