package com.alexsci.android.lambdarunner.ui.list_functions

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
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.ui.ExpandableItemArrayAdapter
import com.alexsci.android.lambdarunner.ui.edit_json.EditJsonActivity
import com.amazonaws.regions.Regions

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
        val lambdaClientBuilder = LambdaClientBuilder("us-east-1", accessKey)
        val lambdaClient = lambdaClientBuilder.getClient(this)

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

                val expandableListAdapter = ExpandableFunctionArrayAdapter(
                    this,
                    lambdaClientBuilder,
                    listFunctionsResult.success.functions
                )

                listView.adapter = expandableListAdapter
            }
        })

        // Start showing the list
        listFunctionsViewModel.list()
    }
}

class ExpandableFunctionArrayAdapter(
    _context: Context,
    private val clientBuilder: LambdaClientBuilder,
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
            val intent = Intent(context, EditJsonActivity::class.java)
            intent.putExtra(EditJsonActivity.EXTRA_JSON_SCHEMA, "{\"\$schema\": \"http://json-schema.org/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } }, \"required\": [ \"name\" ] }")
            intent.putExtra(EditJsonActivity.EXTRA_LAMBDA_CLIENT_BUILDER, clientBuilder)
            intent.putExtra(EditJsonActivity.EXTRA_LAMBDA_FUNCTION_NAME, title.text.toString())
            context.startActivity(intent)
        }

        return convertView
    }

}

