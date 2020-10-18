package io.github.angpysha.extendedcontrols

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.angpysha.extendedcontrolslibrary.Models.CurvedBarMenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val items = arrayOf(
            CurvedBarMenuItem("test1",R.drawable.ic_baseline_build_24,R.drawable.avd_settings),
            CurvedBarMenuItem("test2",R.drawable.ic_baseline_camera_24,R.drawable.avd_camera),
            CurvedBarMenuItem("test3",R.drawable.vd_info,R.drawable.avd_info)

        )

        curvedBottomBar.setMenuItems(items,0)
    }
}
