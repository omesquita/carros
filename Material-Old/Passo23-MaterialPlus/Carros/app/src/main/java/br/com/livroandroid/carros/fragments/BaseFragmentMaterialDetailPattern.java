package br.com.livroandroid.carros.fragments;

import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.samples.apps.iosched.ui.widget.AddToScheduleFABFrameLayout;
import com.google.samples.apps.iosched.ui.widget.ObservableScrollView;
import com.google.samples.apps.iosched.util.LUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.livroandroid.carros.R;

/**
 * Created by ricardo on 14/01/15.
 */
public class BaseFragmentMaterialDetailPattern extends BaseFragment implements
        ObservableScrollView.Callbacks {

    protected static final float PHOTO_ASPECT_RATIO = 1.7777777f;

    public static final String TRANSITION_NAME_PHOTO = "photo";

    protected Handler mHandler = new Handler();

    protected boolean mStarred;

    protected View mScrollViewChild;
    protected TextView tHeaderTitle;
    protected TextView tHeaderSubTitle;

    protected ObservableScrollView mScrollView;
    protected AddToScheduleFABFrameLayout fabAddToFavoritos;

    protected TextView tTextDetails;

    protected View layoutHeader;
    protected View mDetailsContainer;

    protected List<Runnable> mDeferredUiOperations = new ArrayList<Runnable>();

    protected int mPhotoHeightPixels;
    protected int mHeaderHeightPixels;
    protected int mAddScheduleButtonHeightPixels;

    protected View layoutPicture;
    protected ImageView imgPicture;

    protected float mMaxHeaderElevation;
    protected float mFABElevation;

    /**
     * A subclasse precisa chamar esse método.
     *
     * @param view
     * @param toolbar
     */
    protected void initViews(View view, final Toolbar toolbar) {

        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });

        mFABElevation = getResources().getDimensionPixelSize(R.dimen.fab_elevation);
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.session_detail_max_header_elevation);

        mScrollView = (ObservableScrollView) view.findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }

        mScrollViewChild = view.findViewById(R.id.layoutScrollView);
        mScrollViewChild.setVisibility(View.INVISIBLE);

        mDetailsContainer = view.findViewById(R.id.layoutDetails);
        layoutHeader = view.findViewById(R.id.layoutHeader);
        tHeaderTitle = (TextView) view.findViewById(R.id.tHeaderTitle);
        tHeaderSubTitle = (TextView) view.findViewById(R.id.tHeaderSubTitle);
        layoutPicture = view.findViewById(R.id.layoutPicture);
        imgPicture = (ImageView) view.findViewById(R.id.imgPicture);

        tTextDetails = (TextView) view.findViewById(R.id.tTextContents);

        fabAddToFavoritos = (AddToScheduleFABFrameLayout) view.findViewById(R.id.add_favoritos_button);
        fabAddToFavoritos.setDrawableOnOff(R.drawable.add_schedule_fab_ripple_background_on,
                R.drawable.add_schedule_fab_ripple_background_off, R.color.white,R.color.accent);
        if(fabAddToFavoritos != null) {
            fabAddToFavoritos.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean starred = !mStarred;
                            showStarred(starred, true);
                        }
                    });
        }

        ViewCompat.setTransitionName(imgPicture, TRANSITION_NAME_PHOTO);
    }

    protected void recomputePhotoAndScrollingMetrics() {
        mHeaderHeightPixels = layoutHeader.getHeight();

        mPhotoHeightPixels = 0;
        mPhotoHeightPixels = (int) (imgPicture.getWidth() / PHOTO_ASPECT_RATIO);
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);

        ViewGroup.LayoutParams lp;
        lp = layoutPicture.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            layoutPicture.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScrollView == null) {
            return;
        }

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
    }

    protected ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if(fabAddToFavoritos != null) {
                mAddScheduleButtonHeightPixels = fabAddToFavoritos.getHeight();
            }
            recomputePhotoAndScrollingMetrics();
        }
    };

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);

        ViewCompat.setTranslationY(layoutHeader,newTop);
        //layoutHeader.setTranslationY(newTop);
        if(fabAddToFavoritos != null) {
            fabAddToFavoritos.setTranslationY(newTop + mHeaderHeightPixels
                    - mAddScheduleButtonHeightPixels / 2);
        }

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);
        }

        ViewCompat.setElevation(layoutHeader, gapFillProgress * mMaxHeaderElevation);
        if(fabAddToFavoritos != null) {
            ViewCompat.setElevation(fabAddToFavoritos, gapFillProgress * mMaxHeaderElevation
                    + mFABElevation);
        }

        // Move background photo (parallax effect)
        ViewCompat.setTranslationY(layoutPicture,scrollY * 0.5f);
        //layoutPicture.setTranslationY(scrollY * 0.5f);
    }

    public static float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }

        return (value - min) / (float) (max - min);
    }

    protected void showStarredDeferred(final boolean starred, final boolean allowAnimate) {
        mDeferredUiOperations.add(new Runnable() {
            @Override
            public void run() {
                //showStarred(starred, allowAnimate);
            }
        });
    }

    protected void showStarred(boolean starred, boolean allowAnimate) {
        mStarred = starred;

        fabAddToFavoritos.setChecked(mStarred, allowAnimate);

        ImageView iconView = (ImageView) fabAddToFavoritos.findViewById(R.id.add_schedule_icon);

        // Cores e Drawable do FAB button dos favoritos
        int colorFilterWhenChecked = R.color.accent;
        int drawableId = R.drawable.add_schedule_fab_icon_anim;
        int button_icon_checkedId = R.drawable.add_schedule_button_icon_checked;
        int button_icon_uncheckedId = R.drawable.add_schedule_button_icon_unchecked;

        getLUtils().setOrAnimatePlusCheckIcon(iconView, starred, allowAnimate,colorFilterWhenChecked,drawableId,button_icon_checkedId,button_icon_uncheckedId);
    }

    protected LUtils getLUtils() {
        return LUtils.getInstance(getActionBarActivity());
    }
}
