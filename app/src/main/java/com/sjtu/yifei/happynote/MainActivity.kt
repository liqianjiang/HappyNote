package com.sjtu.yifei.happynote

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.sjtu.yifei.base.BaseActivity
import com.sjtu.yifei.base.util.setupActionBar
import com.sjtu.yifei.route.Routerfit
import gl.center.testrouter.router.RouterService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(true)
            setTitle(R.string.app_name)
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fab.setOnClickListener { _->
            //打开编辑器
            Routerfit.register(RouterService::class.java).openEditorUi()
        }
    }

}
