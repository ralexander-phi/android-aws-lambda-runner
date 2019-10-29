package com.alexsci.android.lambdarunner.ui.list_keys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.ui.ExpandableItemArrayAdapter
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement

class ListKeysActivity: AppCompatActivity() {

    private lateinit var listKeysViewModel: ListKeysViewModel
    private lateinit var headerTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_items)

        headerTextView = findViewById(R.id.header)
        loadingProgressBar = findViewById(R.id.loading)
        listView = findViewById(R.id.list)

        headerTextView.text = "Available Keys"

        listKeysViewModel = ViewModelProviders.of(
            this,
            ListKeysViewModelFactory(KeyManagement.getInstance(this))
        ).get(ListKeysViewModel::class.java)

        listKeysViewModel.listResult.observe(this@ListKeysActivity, Observer {
            val listKeysResult = it ?: return@Observer

            loadingProgressBar.visibility = View.GONE
            if (listKeysResult.error != null) {
                Toast.makeText(this, "Oops! " + listKeysResult.error.toString(), Toast.LENGTH_LONG).show()
            }
            if (listKeysResult.success != null) {
                val expandableListAdapter = ExpandableKeyArrayAdapter(this, listKeysResult.success.keys)
                listView.adapter = expandableListAdapter
            }
        })

        // Start showing the list
        listKeysViewModel.list()
    }
}

class ExpandableKeyArrayAdapter(
    _context: Context,
    _items: List<Key>):
        ExpandableItemArrayAdapter<Key>(_context, _items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.list_item, parent, false)
        }

        val title: TextView = convertView!!.findViewById(R.id.title)
        val description: TextView = convertView.findViewById(R.id.description)
        val buttonPanel: LinearLayout = convertView.findViewById(R.id.buttonPanel)
        val remove: Button = convertView.findViewById(R.id.remove)
        val run: Button = convertView.findViewById(R.id.run)

        val currentItem = getItem(position) as Key

        title.setText(currentItem.keyName)
        description.setText(currentItem.description)

        title.setOnClickListener {
            if (description.visibility == View.GONE) {
                description.visibility = View.VISIBLE
                buttonPanel.visibility = View.VISIBLE
            } else {
                description.visibility = View.GONE
                buttonPanel.visibility = View.GONE
            }
        }

        remove.setOnClickListener { items.remove(currentItem) }

        run.setOnClickListener {
            val intent = Intent(context, ListFunctionsActivity::class.java)
            intent.putExtra(ListFunctionsActivity.EXTRA_ACCESS_KEY, currentItem.keyName)
            context.startActivity(intent)
        }

        return convertView
    }

}

