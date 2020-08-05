package kr.kro.uptune.uptuneandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        finishAndRemoveTask()
        Process.killProcess(Process.myPid())
    }
    

}
