package com.bytedance.firstapp.ui.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bytedance.firstapp.data.model.Session

class SessionViewModel : ViewModel() {

    private val _sessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> = _sessions

    init {
        loadStaticSessions()
    }

    private fun loadStaticSessions() {
        val staticList = listOf(
            Session(title = "关于安卓开发的问题", lastMessagePreview = "好的，我们来构建这个非常实用且重要的..."),
            Session(title = "周末出游计划", lastMessagePreview = "我觉得去海边是个不错的选择，你觉得呢？"),
            Session(title = "项目紧急需求讨论", lastMessagePreview = "这个新功能必须在周五之前上线，大家加把劲。"),
            Session(title = "随便聊聊", lastMessagePreview = "哈哈哈哈那个电影太搞笑了！")
        )
        _sessions.value = staticList
    }
}
