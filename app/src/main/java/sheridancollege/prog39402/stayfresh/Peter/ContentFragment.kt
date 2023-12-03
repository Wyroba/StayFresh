package sheridancollege.prog39402.stayfresh.Peter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import sheridancollege.prog39402.stayfresh.R
import sheridancollege.prog39402.stayfresh.Chi.RecipeFragment
import sheridancollege.prog39402.stayfresh.Chi.SettingsFragment


class ContentFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_content, container, false)

        viewPager = view.findViewById(R.id.viewpager)
        tabs = view.findViewById(R.id.tabs)

        setupViewPager()

        return view
    }

    private fun setupViewPager() {
        val adapter = ContentPagerAdapter(childFragmentManager) // Use childFragmentManager

        val firstFragment = PantryFragment()
        val secondFragment = RecipeFragment()
        val thirdFragment = SettingsFragment()

        adapter.addFragment(firstFragment, "Pantry")
        adapter.addFragment(secondFragment, "Recipes")
        adapter.addFragment(thirdFragment, "Settings")

        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

    }

}