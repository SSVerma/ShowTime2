import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@Composable
fun MediaItem(
    title: String,
    posterImageUrl: String,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
    indicator: (@Composable () -> Unit)? = null,
    onOverflowIconClick: (() -> Unit)? = null,
    titleMaxLines: Int = 1,
    titleTextStyle: TextStyle = MaterialTheme.typography.subtitle1,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        MediaPoster(
            posterImageUrl = posterImageUrl,
            indicator = indicator,
            onOverflowIconClick = onOverflowIconClick,
            onClick = onClick,
            modifier = posterModifier
                .width(DefaultMediaPosterWidth)
                .aspectRatio(TmdbPosterAspectRatio)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            style = titleTextStyle,
            modifier = Modifier.widthIn(max = DefaultMediaPosterWidth)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MediaPoster(
    posterImageUrl: String,
    modifier: Modifier = Modifier,
    indicator: (@Composable () -> Unit)? = null,
    onOverflowIconClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Box {
            NetworkImage(
                url = posterImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
            indicator?.let { it() }
            onOverflowIconClick?.let {
                IconButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .size(24.dp),
                    onClick = it
                ) {
                    Icon(imageVector = AppIcons.MoreVert, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun ValueIndicator(value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colors.background,
        modifier = modifier
            .padding(start = 4.dp, top = 4.dp)
            .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(50))
    ) {
        Text(
            text = value,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ScoreIndicator(score: Float) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colors.background)
    ) {
        CircularProgressIndicator(
            progress = score / 100f,
            strokeWidth = 1.dp,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopStart)
        )
        Text(
            text = "${score.toInt()}%",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.caption.copy(fontSize = 10.sp)
        )
    }
}

private val DefaultMediaPosterWidth = 140.dp