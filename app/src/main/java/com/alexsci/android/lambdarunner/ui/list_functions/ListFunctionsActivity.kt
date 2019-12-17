package com.alexsci.android.lambdarunner.ui.list_functions

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Either
import com.alexsci.android.lambdarunner.*
import com.alexsci.android.lambdarunner.aws.RegionInfo
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.ui.common.BaseArrayAdapter
import com.alexsci.android.lambdarunner.ui.common.ToolbarHelper
import com.alexsci.android.lambdarunner.ui.common.VerticalSpaceItemDecorator
import com.alexsci.android.lambdarunner.ui.common.ViewHolder
import com.alexsci.android.lambdarunner.ui.list_keys.ListKeysActivity
import com.alexsci.android.lambdarunner.ui.run_lambda.RunLambdaActivity
import com.alexsci.android.lambdarunner.util.preferences.PreferencesUtil
import com.amazonaws.regions.Regions

class ListFunctionsActivity: AppCompatActivity() {
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var noKeysMessage: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var listFunctionsViewModel: ListFunctionsViewModel

    private lateinit var preferences: PreferencesUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load preferences, or launch activity to select them
        preferences = PreferencesUtil(this)

        setContentView(R.layout.activity_list_functions)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingProgressBar = findViewById(R.id.loading)
        noKeysMessage = findViewById(R.id.no_keys_message)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ListFunctionsActivity)
            addItemDecoration(VerticalSpaceItemDecorator())
        }

        setupRegionSpinners()

        listFunctionsViewModel = ViewModelProviders.of(
            this,
            ListFunctionsViewModelFactory(this)
        ).get(ListFunctionsViewModel::class.java)

        listFunctionsViewModel.listResult.observe(
            this,
            FunctionListObserver()
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    private fun setupRegionSpinners() {
        val regionGroupSpinner = findViewById<Spinner>(R.id.region_group)
        val regionCodeSpinner = findViewById<Spinner>(R.id.region_code)

        // Get previous selection, or default values
        val selectedRegionGroup =
            preferences.get(SHARED_PREFERENCE_REGION_GROUP, "All Regions")

        val regionGroupNames = RegionInfo.groups()

        regionGroupSpinner.adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            regionGroupNames
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        regionGroupSpinner.setSelection(regionGroupNames.indexOf(selectedRegionGroup))
        regionGroupSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedGroup = regionGroupNames[position]
                preferences.set(SHARED_PREFERENCE_REGION_GROUP, selectedGroup)
                val regionCodes = RegionInfo.regionCodesForRegionGroup(selectedGroup)
                regionCodeSpinner.adapter = ArrayAdapter<String>(
                    this@ListFunctionsActivity,
                    android.R.layout.simple_spinner_item,
                    regionCodes
                ).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                // If the region we previously had selected is in this group, keep it selected
                val selectedRegionCode =
                    preferences.get(SHARED_PREFERENCE_REGION, Regions.DEFAULT_REGION.getName())
                if (regionCodes.contains(selectedRegionCode)) {
                    regionCodeSpinner.setSelection(regionCodes.indexOf(selectedRegionCode))
                }
            }
        }

        regionCodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val region = parent!!.adapter.getItem(position) as String
                preferences.set(SHARED_PREFERENCE_REGION, region)
                onRegionSelected(region)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toolbarResult = ToolbarHelper().onOptionsItemSelected(this, item)
        return if (toolbarResult != null) {
            toolbarResult
        } else {
            return when (item.itemId) {
                android.R.id.home -> {
                    startActivity(Intent(this, ListKeysActivity::class.java))
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun onRegionSelected(region: String) {
        val accessKeyId = preferences.get(SHARED_PREFERENCE_ACCESS_KEY_ID)
        if (accessKeyId == null) {
            startActivity(Intent(this, ListKeysActivity::class.java))
        } else {
            listFunctionsViewModel.list(accessKeyId, region)
        }
    }

    inner class FunctionListObserver : Observer<Either<Exception, MutableList<Function>>> {
        override fun onChanged(t: Either<Exception, MutableList<Function>>) {
            when (t) {
                is Either.Left -> onError(t.a)
                is Either.Right -> onSuccess(t.b)
                else -> onWaiting()
            }
        }

        private fun onError(e: Exception) {
            loadingProgressBar.isVisible = false
            recyclerView.isVisible = false
            noKeysMessage.isVisible = true
            noKeysMessage.text = e.toString()
        }

        private fun onWaiting() {
            loadingProgressBar.isVisible = true
            recyclerView.isVisible = false
            noKeysMessage.isVisible = false
        }

        private fun onSuccess(results: MutableList<Function>) {
            loadingProgressBar.isVisible = false
            if (results.isEmpty()) {
                recyclerView.isVisible = false
                noKeysMessage.isVisible = true
                val region = preferences.get(SHARED_PREFERENCE_REGION)
                noKeysMessage.text = "No functions found in $region."
            } else {
                recyclerView.isVisible = true
                noKeysMessage.isVisible = false
                recyclerView.adapter = FunctionArrayAdapter(results)
            }
        }
    }

    inner class FunctionArrayAdapter(
        data: MutableList<Function>
    ) : BaseArrayAdapter<Function>(data) {
        override fun onBindViewHolder(holder: ViewHolder<Function>, position: Int) {
            super.onBindViewHolder(holder, position)

            val currentItem = holder.t!!
            val context = holder.view.context

            holder.title.text = currentItem.functionName
            holder.description.text = currentItem.description

            holder.run.setImageResource(R.drawable.arrow_right_bold)
            holder.run.setOnClickListener {
                preferences.set(SHARED_PREFERENCE_FUNCTION_NAME, currentItem.functionName)
                context.startActivity(Intent(context, RunLambdaActivity::class.java))
            }

            // Can't remove
            holder.remove.visibility = View.GONE
        }
    }
}
