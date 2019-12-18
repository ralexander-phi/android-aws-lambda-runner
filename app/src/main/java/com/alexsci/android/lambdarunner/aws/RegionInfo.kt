package com.alexsci.android.lambdarunner.aws

import java.lang.RuntimeException
import java.util.*

class RegionInfo {
    companion object {
        private const val ALL_REGIONS_GROUP = "All Regions"

        private val displayNames: Map<String, Map<String, String>> = mapOf(
            "United States" to mapOf(
                "us-east-1" to "N. Virginia",
                "us-east-2" to "Ohio",
                "us-west-1" to "N. California",
                "us-west-2" to "Oregon"
            ),
            "Asia Pacific" to mapOf(
                "ap-northeast-1" to "Tokyo",
                "ap-northeast-2" to "Seoul",
                "ap-southeast-1" to "Singapore",
                "ap-southeast-2" to "Sydney",
                "ap-south-1" to "Mumbai"
            ),
            "Canada" to mapOf(
                "ca-central-1" to "Montreal"
            ),
            "China" to mapOf(
                "cn-north-1" to "Beijing"
            ),
            "European Union" to mapOf(
                "eu-central-1" to "Frankfurt",
                "eu-west-1" to "Ireland",
                "eu-west-2" to "London"
            ),
            "GovCloud" to mapOf(
                "us-gov-west-1" to "Northwest"
            ),
            "South America" to mapOf(
                "sa-east-1" to "Saõ Paulo"
            )
        )

        fun groups(): Array<String> {
            return arrayOf(ALL_REGIONS_GROUP).plus(displayNames.keys.toTypedArray())
        }

        fun groupForCode(regionCode: String): String {
            for (item in displayNames) {
                if (item.value.containsKey(regionCode)) {
                    return item.key
                }
            }
            throw RuntimeException("Unexpected region $regionCode")
        }

        fun displayNameForCode(regionCode: String): String {
            for (item in displayNames) {
                val displayName = item.value[regionCode]
                if (displayName != null) {
                    return displayName
                }
            }
            throw RuntimeException("Unexpected region $regionCode")
        }

        fun regionCodesForRegionGroup(groupName: String): Array<String> {
            if (! displayNames.containsKey(groupName)) {
                val res = LinkedList<String>()
                for (groupInfo in displayNames.values) {
                    res.addAll(groupInfo.keys)
                }
                return res.toTypedArray().sortedArray()
            } else {
                return displayNames[groupName]!!.keys.toTypedArray()
            }
        }
    }
}
