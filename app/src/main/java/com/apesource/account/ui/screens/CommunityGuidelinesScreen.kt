package com.apesource.account.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apesource.account.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityGuidelinesScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社区公约", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuidelineSection(
                title = "一、总则",
                content = "欢迎加入本记账社区。本公约旨在营造一个积极、健康、互助的社区环境，所有用户在使用过程中应遵守本公约。"
            )

            GuidelineSection(
                title = "二、用户行为规范",
                content = "1. 请勿发布任何违法、违规或不良信息。\n" +
                        "2. 请勿进行人身攻击、辱骂、歧视等不当行为。\n" +
                        "3. 请勿发布广告、垃圾信息或进行恶意刷屏。\n" +
                        "4. 尊重他人隐私，不得擅自公开他人信息。\n" +
                        "5. 鼓励分享记账经验与技巧，互帮互助。"
            )

            GuidelineSection(
                title = "三、内容规范",
                content = "1. 用户发布的记账数据仅限个人使用，请勿上传敏感财务信息。\n" +
                        "2. 不得利用本应用进行任何违法违规的财务操作。\n" +
                        "3. 应用内分享的内容应真实、准确，不得故意误导他人。"
            )

            GuidelineSection(
                title = "四、知识产权",
                content = "本应用的所有内容（包括但不限于界面设计、图标、代码等）均受知识产权保护。未经许可，不得复制、修改、传播或用于商业用途。"
            )

            GuidelineSection(
                title = "五、免责声明",
                content = "1. 本应用仅提供记账工具服务，不对用户的财务决策承担任何责任。\n" +
                        "2. 用户应自行确保所记录数据的准确性和安全性。\n" +
                        "3. 如因不可抗力或系统维护导致服务中断，本应用不承担相关责任。"
            )

            GuidelineSection(
                title = "六、违规处理",
                content = "对于违反本公约的用户，我们有权视情节轻重采取警告、限制功能使用、封禁账号等措施。如对处理结果有异议，可通过意见反馈渠道申诉。"
            )

            GuidelineSection(
                title = "七、附则",
                content = "本公约的最终解释权归本应用所有。我们保留随时修改本公约的权利，修改后的公约将在应用内公布并生效。"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "如有任何疑问或建议，欢迎通过「意见反馈」功能联系我们。",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GuidelineSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}
