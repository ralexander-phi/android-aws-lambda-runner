package com.alexsci.android.lambdarunner.ui.list_functions

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
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.ui.edit_json.EditJsonActivity

class ListFunctionsActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_ACCESS_KEY = "com.alexsci.android.lambdarunner.ui.list_functions.access_key"
    }

    private lateinit var listFunctionsViewModel: ListFunctionsViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_items)
        val loading = findViewById<ProgressBar>(R.id.loading)

        val accessKey = intent.getStringExtra(EXTRA_ACCESS_KEY)
        val lambdaClientBuilder = LambdaClientBuilder("us-east-1", accessKey!!)
        val lambdaClient = lambdaClientBuilder.getClient(this)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@ListFunctionsActivity)
        }

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

                val listAdapter = FunctionArrayAdapter(
                    lambdaClientBuilder,
                    listFunctionsResult.success.functions
                )
                recyclerView.adapter = listAdapter
            }
        })

        // Start showing the list
        listFunctionsViewModel.list()
    }
}

class FunctionArrayAdapter(
    private val clientBuilder: LambdaClientBuilder,
    private val data: List<Function>
): RecyclerView.Adapter<FunctionArrayAdapter.ViewHolder>() {
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

        title.text = currentItem.functionName
        description.text = currentItem.description

        title.setOnClickListener {
            if (description.visibility == View.GONE) {
                description.visibility = View.VISIBLE
                buttonPanel.visibility = View.VISIBLE
            } else {
                description.visibility = View.GONE
                buttonPanel.visibility = View.GONE
            }
        }

        // Start hidden
        description.visibility = View.GONE
        buttonPanel.visibility = View.GONE

        run.setOnClickListener {
            val intent = Intent(context, ListFunctionsActivity::class.java)
            intent.putExtra(ListFunctionsActivity.EXTRA_ACCESS_KEY, currentItem.functionName)
            context.startActivity(intent)
        }

        run.setOnClickListener {
            val intent = Intent(context, EditJsonActivity::class.java)
            intent.putExtra(EditJsonActivity.EXTRA_JSON_SCHEMA, "{\"\$schema\": \"http://json-schema.org/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } }, \"required\": [ \"name\" ] }")
            intent.putExtra(EditJsonActivity.EXTRA_LAMBDA_CLIENT_BUILDER, clientBuilder)
            intent.putExtra(EditJsonActivity.EXTRA_LAMBDA_FUNCTION_NAME, title.text.toString())
            context.startActivity(intent)
        }

        remove.setOnClickListener {
            data.drop(position)
        }
    }

    override fun getItemCount() = data.size
}
