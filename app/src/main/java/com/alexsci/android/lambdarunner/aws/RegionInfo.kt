package com.alexsci.android.lambdarunner.aws

import java.lang.RuntimeException

class RegionInfo {
    companion object {
        private val displayNames = mapOf(
            "US East" to mapOf(
                "us-east-1" to "N. Virginia",
                "us-east-2" to "Ohio"
            ),
            "US West" to mapOf(
                "us-west-1" to "N. California",
                "us-west-2" to "Oregon"
            ),
            "Asia Pacific" to mapOf(
                "ap-southeast-1" to "Singapore",
                "ap-southeast-2" to "Sydney",
                "ap-south-1" to "Mumbai",
                "ap-northeast-1" to "Tokyo",
                "ap-northeast-2" to "Seoul"
            ),
            "EU" to mapOf(
                "eu-west-1" to "Ireland",
                "eu-west-2" to "London",
                "eu-central-1" to "Frankfurt"
            ),
            "South America" to mapOf(
                "sa-east-1" to "Saõ Paulo"
            ),
            "Canada" to mapOf(
                "ca-central-1" to "Montreal"
            ),
            "GovCloud US" to mapOf(
                "us-gov-west-1" to "Northwest"
            ),
            "China" to mapOf(
                "cn-north-1" to "Beijing"
            )
        )

        fun groups(): Array<String> {
            return displayNames.keys.toTypedArray().sortedArray()
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
            return displayNames[groupName]!!.keys.toTypedArray().sortedArray()
        }
    }
}
