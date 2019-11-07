package com.alexsci.android.lambdarunner.ui.list_functions

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
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.ui.common.BaseListActivity
import com.alexsci.android.lambdarunner.ui.edit_json.EditJsonActivity

class ListFunctionsActivity: BaseListActivity() {
    companion object {
        const val EXTRA_ACCESS_KEY = "com.alexsci.android.lambdarunner.ui.list_functions.access_key"
    }

    private lateinit var listFunctionsViewModel: ListFunctionsViewModel

    private lateinit var lambdaClientBuilder: LambdaClientBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessKey = intent.getStringExtra(EXTRA_ACCESS_KEY)
        lambdaClientBuilder = LambdaClientBuilder("us-east-1", accessKey!!)
        val lambdaClient = lambdaClientBuilder.getClient(this)

        listFunctionsViewModel = ViewModelProviders.of(
            this,
            ListFunctionsViewModelFactory(lambdaClient)
        ).get(ListFunctionsViewModel::class.java)

        this.title = "Functions"

        listFunctionsViewModel.listResult.observe(
            this@ListFunctionsActivity,
            FunctionListObserver()
        )
    }

    override fun onStart() {
        super.onStart()

        // Start showing the list
        listFunctionsViewModel.list()
    }

    inner class FunctionListObserver: BaseListObserver<ListFunctionsResult>() {
        override fun onSuccess(t: ListFunctionsResult) {
            loadingProgressBar.isVisible = false
            if (t.success!!.functions.isEmpty()) {
                recyclerView.isVisible = false
                noKeysMessage.isVisible = true
            } else {
                recyclerView.isVisible = true
                noKeysMessage.isVisible = false
                recyclerView.adapter = FunctionArrayAdapter(
                    lambdaClientBuilder,
                    t.success.functions
                )
            }
        }
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
