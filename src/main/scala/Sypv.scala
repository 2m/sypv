import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.PlaylistItem

import scala.collection.JavaConversions._
import scala.collection.mutable

object Sypv extends App {

  val playlistId = args.head

  val credential = Auth.authorize(List("https://www.googleapis.com/auth/youtube"), "sypv")
  val youtube = new YouTube.Builder(Auth.HttpTransport, Auth.JsonFactory, credential)
    .setApplicationName("sort-youtube-playlist-videos")
    .build()

  val request = youtube.playlistItems.list("id,snippet")
  request.setPlaylistId(playlistId)
  request.setMaxResults(50)

  val items = mutable.Seq.empty[PlaylistItem]

  var nextPageToken = ""
  do {
    request.setPageToken(nextPageToken)
    val response = request.execute
    items.addAll(response.getItems)
    nextPageToken = response.getNextPageToken
  } while (nextPageToken != null)

  items.sortBy(_.getSnippet.getTitle).zipWithIndex.foreach {
    case (item, idx) =>
      item.getSnippet.setPosition(idx)
      youtube.playlistItems.update("snippet", item).execute
      if (idx % 20 == 0)
        println(s"Processing video #$idx in the playlist.")
  }
}
