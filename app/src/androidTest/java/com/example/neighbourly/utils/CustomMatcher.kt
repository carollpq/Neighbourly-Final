import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun hasItemCount(expectedCount: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("RecyclerView should have $expectedCount items")
        }

        override fun matchesSafely(item: View): Boolean {
            return (item as? RecyclerView)?.adapter?.itemCount == expectedCount
        }
    }
}
