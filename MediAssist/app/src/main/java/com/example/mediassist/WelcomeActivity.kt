package com.example.mediassist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediassist.ui.theme.MediAssistTheme
import com.google.accompanist.pager.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always show the welcome screen first
        showWelcomeScreen()
    }

    private fun showWelcomeScreen() {
        setContent {
            MediAssistTheme {
                WelcomeScreen()
            }
        }
    }
}

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.home_icon,
            title = "Welcome to MediAssist & start your health journey",
            description = "Manage meds easily & stay healthy with MediAssist app."
        ),
        OnboardingPage(
            imageRes = R.drawable.meds_icon,
            title = "Never Miss a Dose Again!",
            description = "Stay on top of your medication schedule with our smart medication reminder. Simply set your medicine name, dosage, and time, and we'll notify you when it's time to take it! Dismiss or snooze reminders with just a tap—your health, made effortless."
        ),
        OnboardingPage(
            imageRes = R.drawable.doc_icon,
            title = "Hassle-Free Doctor Appointments",
            description = "No more long waits! Browse available doctor slots, book your consultation in seconds, and get instant confirmation. Your appointments are saved in your calendar, so you'll never forget an important check-up again."
        ),
        OnboardingPage(
            imageRes = R.drawable.meds_icon,
            title = "Your Medication History at a Glance",
            description = "Easily track your medication history in a structured timeline. View past and current prescriptions, monitor dosages, and stay informed about your health—all in one place!"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        CustomPagerIndicator(pagerState = pagerState, pageCount = pages.size)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(Color(0xFF91C9F9)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(50.dp)
                .width(200.dp)
        ) {
            Text(text = "Get Started", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = page.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.description,
            fontSize = 22.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 22.sp
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomPagerIndicator(pagerState: PagerState, pageCount: Int) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 12.dp)
                    .height(8.dp)
                    .width(if (pagerState.currentPage == index) 24.dp else 12.dp)
                    .clip(CircleShape)
                    .background(
                        if (pagerState.currentPage == index) Color(0xFF91C9F9)
                        else Color.LightGray
                    )
                    .clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
            )
        }
    }
}
