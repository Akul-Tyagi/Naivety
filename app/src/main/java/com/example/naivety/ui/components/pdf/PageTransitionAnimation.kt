//// app/src/main/java/com/example/naivety/ui/components/pdf/PageTransitionAnimation.kt
//package com.example.naivety.ui.components.pdf
//
//import android.annotation.SuppressLint
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//
//@SuppressLint("UnusedContentLambdaTargetStateParameter")
//@Composable
//fun PageTransitionAnimation(
//    page: Int,
//    direction: PageTransitionDirection,
//    content: @Composable AnimatedVisibilityScope.() -> Unit
//) {
//    AnimatedContent(
//        targetState = page,
//        transitionSpec = {
//            when (direction) {
//                PageTransitionDirection.HORIZONTAL -> {
//                    slideInHorizontally(
//                        animationSpec = tween(300),
//                        initialOffsetX = { if (targetState > initialState) it else -it }
//                    ) togetherWith  slideOutHorizontally(
//                        animationSpec = tween(300),
//                        targetOffsetX = { if (targetState > initialState) -it else it }
//                    )
//                }
//                PageTransitionDirection.VERTICAL -> {
//                    slideInVertically(
//                        animationSpec = tween(300),
//                        initialOffsetY = { if (targetState > initialState) it else -it }
//                    ) togetherWith  slideOutVertically(
//                        animationSpec = tween(300),
//                        targetOffsetY = { if (targetState > initialState) -it else it }
//                    )
//                }
//            }
//        }
//    ) {
//        content()
//    }
//}
//
//enum class PageTransitionDirection {
//    HORIZONTAL,
//    VERTICAL
//}