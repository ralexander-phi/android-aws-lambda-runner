package com.alexsci.android.lambdarunner.ui.list_functions

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.aws.lambda.InvokeFunctionRequest
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.ui.ExpandableItemArrayAdapter
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider

class ListFunctionsActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_ACCESS_KEY = "com.alexsci.android.lambdarunner.ui.list_functions.access_key"
    }

    private lateinit var listFunctionsViewModel: ListFunctionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_items)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val listView = findViewById<ListView>(R.id.list)

        val accessKey = intent.getStringExtra(EXTRA_ACCESS_KEY)
        val secretKey = KeyManagement.getInstance(this).getKey(accessKey)
        val creds = BasicAWSCredentials(accessKey, secretKey)
        val credsProvider = StaticCredentialsProvider(creds)
        val lambdaClient = LambdaClient(credsProvider)

        listFunctionsViewModel = ViewModelProviders.of(
            this,
            ListFunctionsViewModelFactory(lambdaClient)
        )
            .get(ListFunctionsViewModel::class.java)

        listFunctionsViewModel.listResult.observe(this@ListFunctionsActivity, Observer {
            val listFunctionsResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (listFunctionsResult.error != null) {
                Toast.makeText(this, "Oops! " + listFunctionsResult.error.toString(), Toast.LENGTH_LONG).show()
            }
            if (listFunctionsResult.success != null) {
                Toast.makeText(this, "Yay! " + listFunctionsResult.success.toString(), Toast.LENGTH_LONG).show()

                val expandableListAdapter = ExpandableFunctionArrayAdapter(this, lambdaClient, listFunctionsResult.success.functions)
                listView.adapter = expandableListAdapter
            }
        })

        // Start showing the list
        listFunctionsViewModel.list()
    }
}

class ExpandableFunctionArrayAdapter(
    _context: Context,
    private val client: LambdaClient, // TODO - remove...
    _items: List<Function>):
    ExpandableItemArrayAdapter<Function>(_context, _items) {

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

        val currentItem = getItem(position) as Function

        title.setText(currentItem.functionName)
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
            InvokeFunctionTask(
                context,
                client,
                title.text.toString(),
                "{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}"
            ).execute(null)
        }

        return convertView
    }

}

class InvokeFunctionTask(
    private val context: Context,
    private val client: LambdaClient,
    private val functionName: String,
    private val payload: String
):
        AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        val request = InvokeFunctionRequest(functionName, payload, logType="Tail")
        val result = client.invoke(request)
        return result.payload
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result != null) {
            Toast.makeText(context, "Got: $result", Toast.LENGTH_LONG).show()
        }
    }
}

