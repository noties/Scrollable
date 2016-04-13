package ru.noties.scrollable.sample;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 28.03.2015.
 */
public class ConfigurationFragment extends BaseFragment {

    static final String TAG = "tag.ConfigurationFragment";
    private static final String FRICTION_PATTERN = "Current: %sF";

    public static ConfigurationFragment newInstance(int color) {
        final Bundle bundle = new Bundle();
        bundle.putInt(ARG_COLOR, color);

        final ConfigurationFragment fragment = new ConfigurationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ConfigurationFragmentCallbacks mCallbacks;
    private ScrollView mScrollView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof ConfigurationFragmentCallbacks)) {
            throw new IllegalStateException("Holding activity must implement ConfigurationFragmentCallbacks");
        }

        mCallbacks = (ConfigurationFragmentCallbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        return inflater.inflate(R.layout.fragment_configuration, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle sis) {
        super.onViewCreated(view, sis);

        mScrollView = findView(view, R.id.scroll_view);

        // friction range bar
        // open dialog item

        final SeekBar seekBar       = findView(view, R.id.seek_bar);
        final TextView seekBarHint  = findView(view, R.id.seek_bar_value_hint);

        final SeekBarHelper seekBarHelper = new SeekBarHelper();
        seekBarHelper.init(seekBar, new OnGlobalFrictionChangedListener() {
            @Override
            public void onChanged(float friction) {
                seekBarHint.setText(String.format(FRICTION_PATTERN, friction));
                mCallbacks.onFrictionChanged(friction);
            }
        });

        final View openDialog = view.findViewById(R.id.configuration_open_dialog);
        openDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.openDialog(seekBarHelper.getCurrentFriction(seekBar));
            }
        });

        final View insideFragment = view.findViewById(R.id.configuration_inside_fragment);
        insideFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.openActivity(ScrollableInsideFragmentActivity.makeIntent(getActivity()));
            }
        });
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mScrollView != null && mScrollView.canScrollVertically(direction);
    }

    @Override
    public CharSequence getTitle(Resources r) {
        return r.getString(R.string.fragment_configuration_title);
    }

    @Override
    public String getSelfTag() {
        return TAG;
    }

    @Override
    public void onFlingOver(int y, long duration) {
        if (mScrollView != null) {
            mScrollView.smoothScrollBy(0, y);
        }
    }

    private static class SeekBarHelper {

        static final float MIN      = BuildConfig.MIN_FRICTION;
        static final float START    = BuildConfig.START_FRICTION;
        static final float STEP     = BuildConfig.FRICTION_STEP;

        void init(final SeekBar seekBar, final OnGlobalFrictionChangedListener listener) {
            final int startLevel = (int) (START / STEP);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    final float friction = (STEP * progress) + MIN;
                    listener.onChanged(friction);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBar.setProgress(startLevel);
        }

        float getCurrentFriction(SeekBar seekBar) {
            final int progress = seekBar.getProgress();
            return (STEP * progress) + MIN;
        }
    }

    private interface OnGlobalFrictionChangedListener {
        void onChanged(float friction);
    }
}
