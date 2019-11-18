package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsci.android.lambdarunner.R

internal abstract class SimpleViewModelFactory(
    private val clazz: Class<*>
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(clazz)) {
            return buildModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    abstract fun <T> buildModel(): T
}

internal class JsonObjectViewModelFactory:
    SimpleViewModelFactory(JsonObjectViewModel::class.java) {
    override fun <T> buildModel(): T {
        return JsonObjectViewModel() as T
    }
}

internal class JsonArrayViewModelFactory:
    SimpleViewModelFactory(JsonArrayViewModel::class.java) {
    override fun <T> buildModel(): T {
        return JsonArrayViewModel() as T
    }

}

private class JsonStringViewModelFactory:
    SimpleViewModelFactory(JsonStringViewModel::class.java) {
    override fun <T> buildModel(): T {
        return JsonStringViewModel() as T
    }
}

private class JsonNumberViewModelFactory:
    SimpleViewModelFactory(JsonNumberViewModel::class.java) {
    override fun <T> buildModel(): T {
        return JsonNumberViewModel() as T
    }
}

private class JsonBooleanViewModelFactory:
    SimpleViewModelFactory(JsonBooleanViewModel::class.java) {
    override fun <T> buildModel(): T {
        return JsonBooleanViewModel() as T
    }
}

private class JsonNullViewModelFactory:
    SimpleViewModelFactory(JsonNull::class.java) {
    override fun <T> buildModel(): T {
        return JsonNull() as T
    }
}

internal abstract class JsonViewModel<T:JsonType>: ViewModel() {
    abstract fun getData(): MutableLiveData<T>
}

internal class JsonObjectViewModel: JsonViewModel<JsonObject>() {
    private val _data: MutableLiveData<JsonObject> = MutableLiveData<JsonObject>().apply {
        value = JsonObject()
    }

    override fun getData(): MutableLiveData<JsonObject> {
        return _data
    }

    fun put(k: String, v: JsonType) {
        _data.postValue(
            _data.value!!.apply {
                value[k] = v
            })
    }
}

internal class JsonArrayViewModel: JsonViewModel<JsonArray>() {
    private val _data: MutableLiveData<JsonArray> = MutableLiveData<JsonArray>().apply {
        value = JsonArray()
    }

    override fun getData(): MutableLiveData<JsonArray> {
        return _data
    }

    fun showAlertDialog(context: Context) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle("Select a type")

        val view = LayoutInflater.from(context).inflate(R.layout.create_json_type, null)
        val stringEdit = view.findViewById<EditText>(R.id.string_value)
        val numberEdit = view.findViewById<EditText>(R.id.number_value)
        val booleanEdit = view.findViewById<ToggleButton>(R.id.boolean_value)

        fun hideInputs() {
            numberEdit.isVisible = false
            stringEdit.isVisible = false
            booleanEdit.isVisible = false
        }

        fun showOnly(view: View) {
            hideInputs()
            view.isVisible = true
        }

        hideInputs()

        alertBuilder.setView(view)

        val jsonTypesArray = JsonTypes.values().map { v -> v.name }.toTypedArray()
        alertBuilder.setSingleChoiceItems(
            jsonTypesArray,
            0
        ) { dialog, which ->
            val selected = jsonTypesArray[which]
            when (selected) {
                JsonTypes.Object.name -> { hideInputs() }
                JsonTypes.Array.name -> { hideInputs() }
                JsonTypes.String.name -> { showOnly(stringEdit) }
                JsonTypes.Number.name -> { showOnly(numberEdit) }
                JsonTypes.Boolean.name -> { showOnly(booleanEdit) }
                JsonTypes.Null.name -> { hideInputs() }
            }
        }
        val alert = alertBuilder.create()
        alert.setButton(
            DialogInterface.BUTTON_POSITIVE,
            "OK"
        ) { dialog, which ->
            val alertDialog = dialog as AlertDialog
            val listView = alertDialog.listView
            val selected = listView.adapter.getItem(listView.checkedItemPosition) as String

            Log.i(EditJsonActivity.LOG_TAG, selected)
            when (selected) {
                JsonTypes.Object.name -> add(JsonObject())
                JsonTypes.Array.name -> add(JsonArray())
                JsonTypes.String.name -> add(JsonString(stringEdit.text.toString()))
                JsonTypes.Number.name -> add(JsonNumber(numberEdit.text.toString().toDouble()))
                JsonTypes.Boolean.name -> add(JsonBoolean(booleanEdit.isChecked))
                JsonTypes.Null.name -> add(JsonNull())
            }
        }
        alert.show()
    }

    private fun add(v: JsonType) {
        _data.postValue(
            _data.value!!.apply {
                value.add(v)
            })
    }
}

internal class JsonStringViewModel: JsonViewModel<JsonString>() {
    private val _data: MutableLiveData<JsonString> = MutableLiveData<JsonString>().apply {
        value = JsonString("")
    }

    override fun getData(): MutableLiveData<JsonString> {
        return _data
    }

    fun set(v: String) {
        _data.postValue(JsonString(v))
    }
}

internal class JsonNumberViewModel: JsonViewModel<JsonNumber>() {
    private val _data: MutableLiveData<JsonNumber> = MutableLiveData<JsonNumber>().apply {
        value = JsonNumber(0.0)
    }

    override fun getData(): MutableLiveData<JsonNumber> {
        return _data
    }

    fun set(v: Double) {
        _data.postValue(JsonNumber(v))
    }
}

internal class JsonBooleanViewModel: JsonViewModel<JsonBoolean>() {
    private val _data: MutableLiveData<JsonBoolean> = MutableLiveData<JsonBoolean>().apply {
        value = JsonBoolean(false)
    }

    override fun getData(): MutableLiveData<JsonBoolean> {
        return _data
    }

    fun set(v: Boolean) {
        _data.postValue(JsonBoolean(v))
    }
}

private class JsonNullViewModel: JsonViewModel<JsonNull>() {
    private val _data: MutableLiveData<JsonNull> = MutableLiveData<JsonNull>().apply {
        value = JsonNull()
    }

    override fun getData(): MutableLiveData<JsonNull> {
        return _data
    }
}
