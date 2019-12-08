package com.alexsci.android.lambdarunner.ui.common

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class BaseListActivity: AppCompatActivity() {
    protected lateinit var loadingProgressBar: ProgressBar
    protected lateinit var noKeysMessage: TextView
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var addButton: FloatingActionButton
    protected lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_items)
        setSupportActionBar(findViewById(R.id.toolbar))

        toolbar = findViewById(R.id.toolbar)
        loadingProgressBar = findViewById(R.id.loading)
        noKeysMessage = findViewById(R.id.no_keys_message)
        addButton = findViewById(R.id.add_button)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@BaseListActivity)
            addItemDecoration(VerticalSpaceItemDecorator())
        }
    }

    abstract inner class BaseListObserver<T:BaseListResult<*>>: Observer<T> {
        override fun onChanged(t: T) {
            when {
                t.error != null -> onError(t)
                t.success != null -> onSuccess(t)
                else -> onWaiting(t)
            }
        }

        open fun onError(t: T) {
            loadingProgressBar.isVisible = false
            recyclerView.isVisible = false
            noKeysMessage.isVisible = true
            noKeysMessage.text = "Oops! ${t.error}"
        }

        open fun onWaiting(t: T) {
            loadingProgressBar.isVisible = true
            recyclerView.isVisible = false
            noKeysMessage.isVisible = false
        }

        abstract fun onSuccess(t: T)
    }
}
