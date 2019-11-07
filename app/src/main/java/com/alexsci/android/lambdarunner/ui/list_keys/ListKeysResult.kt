package com.alexsci.android.lambdarunner.ui.list_keys

import com.alexsci.android.lambdarunner.ui.common.BaseListResult

class ListKeysResult(
    success: KeyListView? = null,
    error: Int? = null
): BaseListResult<KeyListView>(success, error)