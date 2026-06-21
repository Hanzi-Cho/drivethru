package com.hanzi.drivethru.app

import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.data.vehicle.FakeGearStateDataSource
import com.hanzi.drivethru.core.state.DriveThruStateStore

class DriveThruCarAppService : CarAppService() {
    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return DriveThruSession(
            DriveThruStateStore(
                gearStateDataSource = FakeGearStateDataSource(),
                menuRepository = FakeMenuRepository(),
            ),
        )
    }

    override fun createHostValidator(): HostValidator {
        return if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }
    }
}
