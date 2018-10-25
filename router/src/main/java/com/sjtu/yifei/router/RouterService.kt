package com.sjtu.yifei.router

import com.sjtu.yifei.annotation.Go
import com.sjtu.yifei.router.RouterPath.LAUNCHER_EDITOR
import com.sjtu.yifei.router.RouterPath.LAUNCHER_EYES
import com.sjtu.yifei.router.RouterPath.NEED_LOGIN

interface RouterService {

    @Go(LAUNCHER_EDITOR)
    fun openEditorUi():Boolean

    @Go(LAUNCHER_EYES)
    fun openEyesUi():Boolean

    @Go(NEED_LOGIN)
    fun getILoginProviderImpl(): ILoginProvider
}