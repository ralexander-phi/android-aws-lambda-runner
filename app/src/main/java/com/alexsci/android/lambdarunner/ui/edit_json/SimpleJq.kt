package com.alexsci.android.lambdarunner.ui.edit_json

import arrow.core.Either
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.util.*

abstract class SimpleJqPathParser {
    protected fun parse(path: String) {
        val tokenizer = StringTokenizer(path, ".[]", true)

        var expectProperty = false
        var expectOffset = false
        var expectEndOffset = false

        while (tokenizer.hasMoreTokens()) {
            when (val nextToken = tokenizer.nextToken()) {
                "." -> expectProperty = true
                "[" -> expectOffset = true
                "]" -> {
                    assert(expectEndOffset)
                    expectEndOffset = false
                }
                else -> when {
                    expectProperty -> {
                        onProperty(nextToken)
                        expectProperty = false
                    }
                    expectOffset -> {
                        assert(nextToken.toIntOrNull() != null)
                        onOffset(nextToken.toInt())
                        expectOffset = false
                        expectEndOffset = true
                    }
                    else -> {
                        throw IllegalStateException("Not expecting an offset or property name here: $nextToken")
                    }
                }
            }
        }
    }

    protected abstract fun onProperty(k: String)
    protected abstract fun onOffset(i: Int)
}

class JqLookup(root: JsonElement): SimpleJqPathParser() {
    var result: JsonElement = root

    override fun onProperty(k: String) {
        result = result.asJsonObject[k]
    }

    override fun onOffset(i: Int) {
        result = result.asJsonArray[i]
    }

    fun lookup(path: String): JsonElement {
        parse(path)
        return result
    }
}

class JqBreadCrumbs: SimpleJqPathParser() {
    private val _result: MutableList<BreadCrumbPart> = LinkedList()
    private var currPath = ""

    override fun onProperty(k: String) {
        currPath += ".${k}"
        _result.add(BreadCrumbPart(".${k}", currPath))
    }

    override fun onOffset(i: Int) {
        currPath += "[${i}]"
        _result.add(BreadCrumbPart("[${i}]", currPath))
    }

    fun getResults(path: String): MutableList<BreadCrumbPart> {
        _result.add(BreadCrumbPart("<root>", ""))
        parse(path)
        return _result
    }
}

class JqJsonUpdater(val root: JsonElement): SimpleJqPathParser() {
    private var currRoot = root
    private val elementStack = Stack<JsonElement>().also { it.push(root) }
    private val selectorStack = Stack<Either<String, Int>>()

    override fun onProperty(k: String) {
        currRoot = currRoot.asJsonObject[k]
        elementStack.push(currRoot)
        selectorStack.push(Either.left(k))
    }

    override fun onOffset(i: Int) {
        currRoot = currRoot.asJsonArray[i]
        elementStack.push(currRoot)
        selectorStack.push(Either.right(i))
    }

    fun remove(path: String): JsonElement {
        parse(path)
        assert(! elementStack.empty())
        assert(! selectorStack.empty())

        // Throw away the value we want to replace
        val oldValue = elementStack.pop()

        // Figure out how to update the parent element
        val selector = selectorStack.pop()
        when (selector) {
            is Either.Left -> {
                // The parent is an Object
                // Just remove the item by key
                elementStack.peek().asJsonObject.remove(selector.a)
                return elementStack.firstElement()
            }

            is Either.Right -> {
                // The parent is an Array
                // We need to create a new array that excludes the removed index
                val oldParentArray = elementStack.peek().asJsonArray
                val newParentArray = JsonArray()
                for (i in 0..oldParentArray.size()) {
                    if (i == selector.b) {
                        // Skip it
                    } else {
                        newParentArray.add(oldParentArray[i])
                    }
                }

                // We need to update the new array in it's parent
                return update(elementStack, selectorStack, newParentArray)
            }
        }
    }

    fun update(path: String, value: JsonElement): JsonElement {
        parse(path)
        return update(elementStack, selectorStack, value)
    }

    private fun update(
        elementStack: Stack<JsonElement>,
        selectorStack: Stack<Either<String, Int>>,
        newValue: JsonElement
    ): JsonElement {
        // Throw away the value we want to replace
        val oldValue = elementStack.pop()

        if (elementStack.empty()) {
            // We are replacing the root, just return our value
            assert(selectorStack.empty())
            return newValue
        }

        // Figure out how we can set the new value into the parent element
        val selector = selectorStack.pop()
        when (selector) {
            is Either.Left -> {
                // The parent is an Object
                // Get the parent and update the value
                elementStack.peek().asJsonObject.add(selector.a, newValue)
                // With the elements updated, return the original root
                return elementStack.firstElement()
            }

            is Either.Right -> {
                // The parent is an Array
                // We can't update the array, so let's make a copy with our value added at the correct place
                val oldParentArray = elementStack.peek().asJsonArray
                val newParentArray = JsonArray()
                for (i in 0..oldParentArray.size()) {
                    if (i == selector.b) {
                        newParentArray.add(newValue)
                    } else {
                        newParentArray.add(oldParentArray[i])
                    }
                }
                // Then update that array in it's parent (recurse)
                return update(elementStack, selectorStack, newParentArray)
            }

            else -> throw RuntimeException("Unexpected Either type")
        }
    }
}

