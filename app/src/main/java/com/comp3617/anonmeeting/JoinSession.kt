package com.comp3617.anonmeeting

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_join_session.*

class JoinSession : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_session)
    }

    fun onAdd(v: View){
        //val list2 = ArrayList<Tasks>()
        val retIntent = Intent()
        val sessionId = sessJoinText.text.toString()
        val name = nameJoin.text.toString()
        retIntent.putExtra("sessionID",sessionId)
        retIntent.putExtra("Name",name)
        setResult(Activity.RESULT_OK,retIntent)
        finish()
    }
}
