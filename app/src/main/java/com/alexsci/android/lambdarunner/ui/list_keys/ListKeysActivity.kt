package com.alexsci.android.lambdarunner.ui.list_keys

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.ui.add_key.AddKeyActivity
import com.alexsci.android.lambdarunner.ui.common.BaseListActivity
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
            } else {
                val listAdapter = KeyArrayAdapter(t.success.keys.toMutableList())
                recyclerView.isVisible = true
                noKeysMessage.isVisible = false
                recyclerView.adapter = listAdapter
            }
        }
    }

    private inner class KeyArrayAdapter(private val data: MutableList<Key>) :
        RecyclerView.Adapter<KeyArrayAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val context = holder.view.context
            val title: TextView = holder.view.findViewById(R.id.title)
            val description: TextView = holder.view.findViewById(R.id.description)
            val buttonPanel: LinearLayout = holder.view.findViewById(R.id.buttonPanel)
            val remove: Button = holder.view.findViewById(R.id.remove)
            val run: Button = holder.view.findViewById(R.id.run)

            val currentItem = data[position]

            title.text = currentItem.keyName
            description.text = currentItem.description

            // Start hidden
            description.visibility = View.GONE
            buttonPanel.visibility = View.GONE

            title.setOnClickListener {
                if (description.visibility == View.GONE) {
                    description.visibility = View.VISIBLE
                    buttonPanel.visibility = View.VISIBLE
                } else {
                    description.visibility = View.GONE
                    buttonPanel.visibility = View.GONE
                }
            }

            run.setOnClickListener {
                val intent = Intent(context, ListFunctionsActivity::class.java)
                intent.putExtra(ListFunctionsActivity.EXTRA_ACCESS_KEY, currentItem.keyName)
                context.startActivity(intent)
            }

            remove.setOnClickListener {
                listKeysViewModel.remove(currentItem)
            }
        }

        override fun getItemCount() = data.size
    }

}
