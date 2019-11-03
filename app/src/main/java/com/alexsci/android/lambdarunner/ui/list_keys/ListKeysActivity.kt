package com.alexsci.android.lambdarunner.ui.list_keys

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement

class ListKeysActivity: AppCompatActivity() {

    private lateinit var listKeysViewModel: ListKeysViewModel
    private lateinit var headerTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_items)

        listKeysViewModel = ViewModelProviders.of(
            this,
            ListKeysViewModelFactory(KeyManagement.getInstance(this))
        ).get(ListKeysViewModel::class.java)

        headerTextView = findViewById(R.id.header)
        loadingProgressBar = findViewById(R.id.loading)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@ListKeysActivity)
        }

        headerTextView.text = "Available Keys"

        listKeysViewModel.listResult.observe(this@ListKeysActivity, Observer {
            val listKeysResult = it ?: return@Observer

            loadingProgressBar.visibility = View.GONE
            if (listKeysResult.error != null) {
                Toast.makeText(this, "Oops! " + listKeysResult.error.toString(), Toast.LENGTH_LONG).show()
            }
            if (listKeysResult.success != null) {
                val listAdapter = KeyArrayAdapter(listKeysResult.success.keys)
                recyclerView.adapter = listAdapter
            }
        })

        // Start showing the list
        listKeysViewModel.list()
    }
}

class KeyArrayAdapter(private val data: List<Key>):
        RecyclerView.Adapter<KeyArrayAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

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
            data.drop(position)
        }
    }

    override fun getItemCount() = data.size
}


