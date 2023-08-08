package com.test.span

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.test.span.databinding.TestBinding
import com.test.span.ui.Label
import com.test.span.ui.TYPE_BOLD
import com.test.span.ui.setIdentityGradeSpan

class MainActivity : FragmentActivity() {
    private lateinit var binding: TestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = TestBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        val labels = arrayListOf<Label>().apply {
            add(Label(content = "心爱的小摩托", contentColor = "#A4A983", contentFontSize = 12))
            add(Label(R.mipmap.ic_nv))
            add(Label(R.mipmap.dengji, content = "54", contentColor = "#FFFFFF", contentStyle = TYPE_BOLD, contentBackgroundResId = R.mipmap.gradle_1))
            add(Label(content = "长老", contentColor = "#FFFFFF", contentStyle = TYPE_BOLD, contentBackgroundResId = R.mipmap.zhanglao))
            add(Label(R.mipmap.ic_nv))
            add(Label(R.mipmap.ic_nv))
        }
        setIdentityGradeSpan("以下UI为用户聊天室会话文字消息设计，昵称后面的标签可以动态增加减少。", binding.msgTv, labels)
    }
}