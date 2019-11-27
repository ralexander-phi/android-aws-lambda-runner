package com.alexsci.android.lambdarunner.ui.list_functions

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.ui.common.BaseArrayAdapter
import com.alexsci.android.lambdarunner.ui.common.BaseListActivity
import com.alexsci.android.lambdarunner.ui.common.ViewHolder
import com.alexsci.android.lambdarunner.ui.run_lambda.RunLambdaActivity

class ListFunctionsActivity: BaseListActivity() {
    companion object {
        const val EXTRA_ACCESS_KEY = "access_key"
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
            this,
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
                    t.success.functions.toMutableList()
                )
            }
        }
    }
}

class FunctionArrayAdapter(
    private val clientBuilder: LambdaClientBuilder,
    data: MutableList<Function>
): BaseArrayAdapter<Function>(data) {
    override fun onBindViewHolder(holder: ViewHolder<Function>, position: Int) {
        super.onBindViewHolder(holder, position)

        val currentItem = holder.t!!
        val context = holder.view.context

        holder.title.text = currentItem.functionName
        holder.description.text = currentItem.description

        holder.run.setOnClickListener {
            val intent = Intent(context, RunLambdaActivity::class.java)
            intent.putExtra(RunLambdaActivity.EXTRA_FUNCTION_NAME, currentItem.functionName)
            intent.putExtra(RunLambdaActivity.EXTRA_LAMBDA_CLIENT_BUILDER, clientBuilder)
            context.startActivity(intent)
        }

        // Can't remove
        holder.remove.visibility = View.GONE
    }
}
