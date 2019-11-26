package com.alexsci.android.lambdarunner.ui.list_keys

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.ui.add_key.AddKeyActivity
import com.alexsci.android.lambdarunner.ui.common.BaseArrayAdapter
import com.alexsci.android.lambdarunner.ui.common.BaseListActivity
import com.alexsci.android.lambdarunner.ui.common.ViewHolder
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement

class ListKeysActivity: BaseListActivity() {

    private lateinit var listKeysViewModel: ListKeysViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listKeysViewModel = ViewModelProviders.of(
            this,
            ListKeysViewModelFactory(KeyManagement.getInstance(this))
        ).get(ListKeysViewModel::class.java)

        this.title = "AWS Credentials"

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

    inner class KeyListObserver: BaseListObserver<ListKeysResult>() {
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
}

class KeyArrayAdapter(
    private val listKeysViewModel: ListKeysViewModel,
    data: MutableList<Key>
): BaseArrayAdapter<Key>(data) {
    override fun onBindViewHolder(holder: ViewHolder<Key>, position: Int) {
        super.onBindViewHolder(holder, position)

        val currentItem = holder.t!!
        val context = holder.view.context

        holder.title.text = currentItem.keyName
        holder.description.text = currentItem.description

        holder.run.setOnClickListener {
            val intent = Intent(context, ListFunctionsActivity::class.java)
            intent.putExtra(ListFunctionsActivity.EXTRA_ACCESS_KEY, currentItem.keyName)
            context.startActivity(intent)
        }

        holder.remove.setOnClickListener {
            listKeysViewModel.remove(currentItem)
        }
    }


}

