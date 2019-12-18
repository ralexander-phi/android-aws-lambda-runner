package com.alexsci.android.lambdarunner.ui.list_keys

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_ACCESS_KEY_ID
import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.ui.add_key.AddKeyActivity
import com.alexsci.android.lambdarunner.ui.common.BaseArrayAdapter
import com.alexsci.android.lambdarunner.ui.common.BaseListActivity
import com.alexsci.android.lambdarunner.ui.common.ToolbarHelper
import com.alexsci.android.lambdarunner.ui.common.ViewHolder
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement
import com.alexsci.android.lambdarunner.util.preferences.PreferencesUtil

class ListKeysActivity: BaseListActivity() {

    private lateinit var preferences: PreferencesUtil
    private lateinit var listKeysViewModel: ListKeysViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferencesUtil(this)

        listKeysViewModel = ViewModelProviders.of(
            this,
            ListKeysViewModelFactory(KeyManagement.getInstance(this))
        ).get(ListKeysViewModel::class.java)

        toolbar.title = "Select AWS Credential"

        addButton.setOnClickListener {
            val intent = Intent(this.baseContext, AddKeyActivity::class.java)
            startActivity(intent)
        }

        listKeysViewModel.listResult.observe(
            this@ListKeysActivity,
            KeyListObserver()
        )
    }

    override fun onStart() {
        super.onStart()

        // Start showing the list
        listKeysViewModel.list()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toolbarResult = ToolbarHelper().onOptionsItemSelected(this, item)
        return if (toolbarResult != null) {
            toolbarResult
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    inner class KeyListObserver : BaseListObserver<ListKeysResult>() {
        override fun onSuccess(t: ListKeysResult) {
            loadingProgressBar.isVisible = false
            if (t.success!!.keys.isEmpty()) {
                recyclerView.isVisible = false
                noKeysMessage.isVisible = true
                noKeysMessage.setText(R.string.empty_key_list)
            } else {
                recyclerView.isVisible = true
                noKeysMessage.isVisible = false
                recyclerView.adapter = KeyArrayAdapter(
                    listKeysViewModel,
                    t.success.keys.toMutableList()
                )
            }
        }
    }

    inner class KeyArrayAdapter(
        private val listKeysViewModel: ListKeysViewModel,
        data: MutableList<Key>
    ) : BaseArrayAdapter<Key>(data) {
        override fun onBindViewHolder(holder: ViewHolder<Key>, position: Int) {
            super.onBindViewHolder(holder, position)

            val currentItem = holder.t!!
            val context = holder.view.context

            holder.title.text = currentItem.humanReadableName
            holder.description.text = currentItem.keyId

            holder.run.setOnClickListener {
                val accessKeyId = currentItem.keyId
                preferences.set(SHARED_PREFERENCE_ACCESS_KEY_ID, accessKeyId)
                context.startActivity(Intent(context, ListFunctionsActivity::class.java))
            }

            holder.remove.setOnClickListener {
                val builder = AlertDialog.Builder(this@ListKeysActivity)
                builder.setMessage("Are you sure you want to remove ${holder.title.text}?")
                builder.setPositiveButton("Remove") { dialog, _ ->
                    listKeysViewModel.remove(currentItem)
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                builder.create().show()
            }
        }
    }
}

