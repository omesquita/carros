package br.com.livroandroid.carros.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;

import br.com.livroandroid.carros.R;

public class CarrosTabsFragment extends BaseFragment {

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private int currentPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("page");
            log("onCreate.currentPage: " + currentPage);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_carros, container, false);

        log("onCreateView");

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new TabsAdapter(getContext(), getChildFragmentManager()));

        mSlidingTabLayout = (SlidingTabLayout) getActivity().findViewById(R.id.sliding_tabs);

//        mSlidingTabLayout.setCustomTabView(R.layout.tab_layout, R.id.text);
        // Deixa as tabs com mesmo tamanho (layout_weight=1)
        mSlidingTabLayout.setDistributeEvenly(true);

        Resources res = getResources();
//        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.accent));
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int i) {
                // Cor da tab
                return getResources().getColor(R.color.red);
            }
        });


        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                log("onPageSelected: " + position);
                currentPage = position;
                show(true);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Recupera o estado
        mViewPager.setCurrentItem(currentPage);
        log("setCurrentItem: " + currentPage);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Salva o estado do fragment ao girar a tela
        outState.putInt("page", currentPage);
    }
}
