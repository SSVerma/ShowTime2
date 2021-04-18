import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.showtime.data.domain.Movie
import com.ssverma.showtime.ui.common.AppIcons
import com.ssverma.showtime.ui.common.NetworkImage

@Composable
fun MovieItem(movie: Movie, modifier: Modifier = Modifier, imageModifier: Modifier = Modifier) {
    Column(modifier = modifier.width(DEFAULT_MOVIE_ITEM_WIDTH)) {
        MoviePoster(movie = movie, modifier = imageModifier)
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = movie.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
fun MoviePoster(movie: Movie, modifier: Modifier = Modifier) {
    Card {
        Box {
            NetworkImage(
                url = movie.posterImageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = modifier.aspectRatio(3 / 4f)
            )
            Popularity(votePercentage = movie.voteAvgPercentage)
            IconButton(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .size(24.dp),
                onClick = { /*TODO*/ }) {
                Icon(imageVector = AppIcons.MoreVert, contentDescription = null)
            }
        }
    }
}

@Composable
fun Popularity(
    votePercentage: Float,
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colors.surface)
    ) {
        CircularProgressIndicator(
            progress = votePercentage / 100f,
            modifier = Modifier
                .padding(2.dp)
                .size(36.dp)
                .align(Alignment.TopStart)
        )
        Text(
            text = "${votePercentage.toInt()}%",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.caption.copy(fontSize = 10.sp)
        )
    }
}

private val DEFAULT_MOVIE_ITEM_WIDTH = 200.dp