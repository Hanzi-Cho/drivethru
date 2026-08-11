package com.hanzi.drivethru.data.menu

import android.content.Context

class MenuRepositorySelector(
    private val context: Context,
) {
    fun select(): MenuRepository {
        val fakeRepository = FakeMenuRepository()
        return runCatching {
            FirebaseMenuRepository(context, fakeRepository)
        }.getOrDefault(fakeRepository)
    }
}
