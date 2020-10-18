package io.github.angpysha.extendedcontrols

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import io.github.angpysha.extendedcontrolslibrary.Models.CurvedBarMenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav_controller = findNavController(R.id.nav_host_fragment)

        val appBarConfig = AppBarConfiguration(setOf(
            R.id.firstFragment,
            R.id.itemFragment,
            R.id.thirdFragment,
            R.id.fourthFragment,
            R.id.messagesFragment
        ))

        setupActionBarWithNavController(nav_controller,appBarConfig)

        val items = arrayOf(
            CurvedBarMenuItem("test1",R.drawable.ic_baseline_build_24,R.drawable.avd_settings,R.id.firstFragment),
            CurvedBarMenuItem("test2",R.drawable.ic_baseline_camera_24,R.drawable.avd_camera,R.id.itemFragment),
            CurvedBarMenuItem("test3",R.drawable.vd_info,R.drawable.avd_info,R.id.thirdFragment),
            CurvedBarMenuItem("test4",R.drawable.vd_user,R.drawable.avd_user,R.id.fourthFragment),
            CurvedBarMenuItem("test5,",R.drawable.vd_message,R.drawable.avd_message,R.id.messagesFragment)

        )

        curvedBottomBar.setMenuItems(items,0)
        curvedBottomBar.setupWithNavController(nav_controller)
    }

    override fun onBackPressed() {
        if (curvedBottomBar!!.OnBackPressed())
             super.onBackPressed()
    }
}
