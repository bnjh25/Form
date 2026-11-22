package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.FormPost
import com.example.ui.theme.CobaltAccent
import com.example.ui.theme.HeartRed
import kotlinx.coroutines.delay

@Composable
fun InstagramPostCard(
    post: FormPost,
    onDoubleTapLike: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenNotes: () -> Unit,
    onShare: () -> Unit,
    onToggleSave: () -> Unit,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var burstTrigger by remember { mutableIntStateOf(0) }
    val burstScale = remember { Animatable(0f) }
    val burstAlpha = remember { Animatable(0f) }

    LaunchedEffect(burstTrigger) {
        if (burstTrigger > 0) {
            burstAlpha.snapTo(1f)
            burstScale.snapTo(0.35f)
            burstScale.animateTo(
                targetValue = 1.15f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            delay(260L)
            burstAlpha.animateTo(0f, animationSpec = tween(220))
        }
    }

    val heartButtonScale by animateFloatAsState(
        targetValue = if (post.isLiked) 1.14f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heart_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feed_post_card_${post.id}")
    ) {
        // Post Header (Avatar, Handle, Verified Badge, Location, Overflow)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onInspect),
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                FormCanvasArtwork(
                    subject = post.subject,
                    palette = post.palette,
                    finish = post.finish,
                    compactMode = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onInspect)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.handle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (post.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = stringResource(R.string.cd_verified_form),
                            tint = CobaltAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = post.locationTag,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onInspect,
                modifier = Modifier.testTag("post_more_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = stringResource(R.string.cd_post_more),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Square 1:1 Form Exhibition Canvas with Double-Tap Like
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            burstTrigger++
                            onDoubleTapLike()
                        }
                    )
                }
                .testTag("post_canvas_${post.id}"),
            contentAlignment = Alignment.Center
        ) {
            FormCanvasArtwork(
                subject = post.subject,
                palette = post.palette,
                finish = post.finish,
                compactMode = false,
                showBadge = true,
                modifier = Modifier.fillMaxSize()
            )

            // Double-Tap Heart Pop Overlay
            if (burstAlpha.value > 0.01f) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.94f),
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer {
                            scaleX = burstScale.value
                            scaleY = burstScale.value
                            alpha = burstAlpha.value
                        }
                )
            }
        }

        // Classic Instagram Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.testTag("post_like_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(
                            if (post.isLiked) R.string.cd_unlike_post else R.string.cd_like_post
                        ),
                        tint = if (post.isLiked) HeartRed else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(26.dp)
                            .scale(heartButtonScale)
                    )
                }

                IconButton(
                    onClick = onOpenNotes,
                    modifier = Modifier.testTag("post_comment_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.cd_comment_post),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("post_share_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = stringResource(R.string.cd_share_post),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            IconButton(
                onClick = onToggleSave,
                modifier = Modifier.testTag("post_save_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(
                        if (post.isSaved) R.string.cd_unsave_post else R.string.cd_save_post
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Appreciation Count, Handle + Caption, Notes Link, Timestamp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val likeWord = if (post.displayLikes == 1) "appreciation" else "appreciations"
            Text(
                text = "${post.displayLikes} $likeWord",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(post.handle)
                    }
                    append("  ")
                    append(post.caption)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (post.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(5.dp))
                val notesLabel = if (post.notes.size == 1) {
                    "View 1 appreciation note"
                } else {
                    "View all ${post.notes.size} appreciation notes"
                }
                Text(
                    text = notesLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = onOpenNotes)
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.timeAgo.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    }
}
